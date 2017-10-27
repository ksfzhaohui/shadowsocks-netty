package org.netty.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socks.SocksInitRequestDecoder;
import io.netty.handler.codec.socks.SocksMessageEncoder;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;

public final class SocksServerInitializer extends ChannelInitializer<SocketChannel> {

	private SocksMessageEncoder socksMessageEncoder;
	private SocksServerHandler socksServerHandler;
	private GlobalTrafficShapingHandler trafficHandler;

	public SocksServerInitializer(GlobalTrafficShapingHandler trafficHandler) {
		this.trafficHandler = trafficHandler;
		socksMessageEncoder = new SocksMessageEncoder();
		socksServerHandler = new SocksServerHandler();
	}

	@Override
	public void initChannel(SocketChannel socketChannel) throws Exception {
		ChannelPipeline p = socketChannel.pipeline();
		p.addLast(new SocksInitRequestDecoder());
		p.addLast(socksMessageEncoder);
		p.addLast(socksServerHandler);
		p.addLast(trafficHandler);
	}
}
