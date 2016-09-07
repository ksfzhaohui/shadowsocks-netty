package org.netty.proxy;

import org.netty.config.Config;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socks.SocksInitRequestDecoder;
import io.netty.handler.codec.socks.SocksMessageEncoder;

public final class SocksServerInitializer extends
		ChannelInitializer<SocketChannel> {

	private SocksMessageEncoder socksMessageEncoder;
	private SocksServerHandler socksServerHandler;

	public SocksServerInitializer(Config config) {
		socksMessageEncoder = new SocksMessageEncoder();
		socksServerHandler = new SocksServerHandler(config);
	}

	@Override
	public void initChannel(SocketChannel socketChannel) throws Exception {
		ChannelPipeline p = socketChannel.pipeline();
		p.addLast(new SocksInitRequestDecoder());
		p.addLast(socksMessageEncoder);
		p.addLast(socksServerHandler);
	}
}
