package org.netty.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netty.config.Config;
import org.netty.encryption.CryptFactory;
import org.netty.encryption.ICrypt;

@ChannelHandler.Sharable
public final class SocksServerConnectHandler extends
		SimpleChannelInboundHandler<SocksCmdRequest> {

	private static Log logger = LogFactory
			.getLog(SocksServerConnectHandler.class);
	
	public static final int BUFFER_SIZE = 16384;

	private final Bootstrap b = new Bootstrap();
	private ICrypt _crypt;
	private ByteArrayOutputStream _remoteOutStream;
	private ByteArrayOutputStream _localOutStream;
	private Config config;

	public SocksServerConnectHandler(Config config) {
		this.config = config;
		this._crypt = CryptFactory.get(config.get_method(), config.get_password());
		this._remoteOutStream = new ByteArrayOutputStream(BUFFER_SIZE);
		this._localOutStream = new ByteArrayOutputStream(BUFFER_SIZE);
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx,
			final SocksCmdRequest request) throws Exception {
		Promise<Channel> promise = ctx.executor().newPromise();
		promise.addListener(new GenericFutureListener<Future<Channel>>() {
			@Override
			public void operationComplete(final Future<Channel> future)
					throws Exception {
				final Channel outboundChannel = future.getNow();
				if (future.isSuccess()) {
					final InRelayHandler inRelay = new InRelayHandler(ctx
							.channel(), SocksServerConnectHandler.this);
					final OutRelayHandler outRelay = new OutRelayHandler(
							outboundChannel, SocksServerConnectHandler.this);

					ctx.channel().writeAndFlush(getSuccessResponse(request))
							.addListener(new ChannelFutureListener() {
								@Override
								public void operationComplete(
										ChannelFuture channelFuture) {
									sendConnectRemoteMessage(request, outboundChannel);
									
									ctx.pipeline().remove(SocksServerConnectHandler.this);
									outboundChannel.pipeline().addLast(inRelay);
									ctx.pipeline().addLast(outRelay);
								}
							});
				} else {
					ctx.channel().writeAndFlush(
							new SocksCmdResponse(SocksCmdStatus.FAILURE,
									request.addressType()));
					SocksServerUtils.closeOnFlush(ctx.channel());
				}
			}
		});
		
		final Channel inboundChannel = ctx.channel();
		b.group(inboundChannel.eventLoop()).channel(NioSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.handler(new DirectClientHandler(promise));

		b.connect(config.get_ipAddr(), config.get_port()).addListener(
				new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future)
							throws Exception {
						if (!future.isSuccess()) {
							ctx.channel().writeAndFlush(getFailureResponse(request));
							SocksServerUtils.closeOnFlush(ctx.channel());
						}
					}
				});
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		SocksServerUtils.closeOnFlush(ctx.channel());
	}

	private SocksCmdResponse getSuccessResponse(SocksCmdRequest request) {
		return new SocksCmdResponse(SocksCmdStatus.SUCCESS,
				request.addressType());
	}

	private SocksCmdResponse getFailureResponse(SocksCmdRequest request) {
		return new SocksCmdResponse(SocksCmdStatus.FAILURE,
				request.addressType());
	}

	/**
	 * localserver和remoteserver进行connect发送的数据
	 * 
	 * @param request
	 * @param outboundChannel
	 */
	private void sendConnectRemoteMessage(SocksCmdRequest request,
			Channel outboundChannel) {
		ByteBuf buff = Unpooled.directBuffer();
		request.encodeAsByteBuf(buff);
		if (!buff.hasArray()) {
			int len = buff.readableBytes();
			byte[] arr = new byte[len];
			buff.getBytes(0, arr);
			byte[] data = remoteByte(arr);
			sendRemote(data, data.length, outboundChannel);
		}
	}

	/**
	 * localserver和remoteserver进行connect发送的数据
	 * 
	 * +-----+-----+-------+------+----------+----------+
     * | VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
     * +-----+-----+-------+------+----------+----------+
     * |  1  |  1  | X'00' |  1   | Variable |    2     |
     * +-----+-----+-------+------+----------+----------+
	 * 
	 * 需要跳过前面3个字节
	 * 
	 * @param data
	 * @return
	 */
	private byte[] remoteByte(byte[] data) {
		int dataLength = data.length;
		dataLength -= 3;
		byte[] temp = new byte[dataLength];
		System.arraycopy(data, 3, temp, 0, dataLength);
		return temp;
	}

	/**
	 * 给remoteserver发送数据--需要进行加密处理
	 * 
	 * @param data
	 * @param length
	 * @param channel
	 */
	public void sendRemote(byte[] data, int length, Channel channel) {
		_crypt.encrypt(data, length, _remoteOutStream);
		byte[] sendData = _remoteOutStream.toByteArray();
		channel.writeAndFlush(Unpooled.wrappedBuffer(sendData));
		logger.info("sendRemote message:length = " + length+",channel = " + channel);
	}

	public void sendLocal(byte[] data, int length, Channel outboundChannel) {
		_crypt.decrypt(data, length, _localOutStream);
		byte[] sendData = _localOutStream.toByteArray();
		outboundChannel.writeAndFlush(Unpooled.wrappedBuffer(sendData));
		logger.info("sendLocal message:length = " + length + ",channel = " + outboundChannel);
	}

}
