package org.netty.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netty.config.Config;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksAuthResponse;
import io.netty.handler.codec.socks.SocksAuthScheme;
import io.netty.handler.codec.socks.SocksAuthStatus;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdRequestDecoder;
import io.netty.handler.codec.socks.SocksCmdType;
import io.netty.handler.codec.socks.SocksInitResponse;
import io.netty.handler.codec.socks.SocksRequest;

@ChannelHandler.Sharable
public final class SocksServerHandler extends
		SimpleChannelInboundHandler<SocksRequest> {

	private static Log logger = LogFactory.getLog(SocksServerHandler.class);

	private Config config;

	public SocksServerHandler(Config config) {
		this.config = config;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx,
			SocksRequest socksRequest) throws Exception {
		switch (socksRequest.requestType()) {
		case INIT: {
			logger.info("localserver init");
			ctx.pipeline().addFirst(new SocksCmdRequestDecoder());
			ctx.write(new SocksInitResponse(SocksAuthScheme.NO_AUTH));
			break;
		}
		case AUTH:
			ctx.pipeline().addFirst(new SocksCmdRequestDecoder());
			ctx.write(new SocksAuthResponse(SocksAuthStatus.SUCCESS));
			break;
		case CMD:
			SocksCmdRequest req = (SocksCmdRequest) socksRequest;
			if (req.cmdType() == SocksCmdType.CONNECT) {
				logger.info("localserver connect");
				ctx.pipeline().addLast(new SocksServerConnectHandler(config));
				ctx.pipeline().remove(this);
				ctx.fireChannelRead(socksRequest);
			} else {
				ctx.close();
			}
			break;
		case UNKNOWN:
			ctx.close();
			break;
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		throwable.printStackTrace();
		SocksServerUtils.closeOnFlush(ctx.channel());
	}
}
