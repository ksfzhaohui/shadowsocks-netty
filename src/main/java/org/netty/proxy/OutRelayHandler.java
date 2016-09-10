package org.netty.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * localserver接受到数据发送数据给remoteserver
 * 
 * @author zhaohui
 * 
 */
public final class OutRelayHandler extends ChannelInboundHandlerAdapter {

	private final Channel relayChannel;
	private SocksServerConnectHandler connectHandler;

	public OutRelayHandler(Channel relayChannel,
			SocksServerConnectHandler connectHandler) {
		this.relayChannel = relayChannel;
		this.connectHandler = connectHandler;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (relayChannel.isActive()) {
			ByteBuf bytebuff = (ByteBuf) msg;
			if (!bytebuff.hasArray()) {
				int len = bytebuff.readableBytes();
				byte[] arr = new byte[len];
				bytebuff.getBytes(0, arr);
				connectHandler.sendRemote(arr, arr.length, relayChannel);
			}
		} else {
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		if (relayChannel.isActive()) {
			SocksServerUtils.closeOnFlush(relayChannel);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
