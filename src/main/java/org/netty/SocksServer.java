package org.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netty.config.Config;
import org.netty.config.ConfigXmlLoader;
import org.netty.proxy.SocksServerInitializer;

public class SocksServer implements Runnable {

	private static Log logger = LogFactory.getLog(SocksServer.class);

	private static final String CONFIG = "conf/config.xml";

	@Override
	public void run() {
		EventLoopGroup bossGroup = null;
		EventLoopGroup workerGroup = null;
		try {
			Config config = ConfigXmlLoader.load(CONFIG);
			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();

			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new SocksServerInitializer(config));
			
			logger.info("Start At Port " + config.get_localPort());
			b.bind(config.get_localPort()).sync().channel().closeFuture()
					.sync();
		} catch (Exception e) {
			logger.error("start error", e);
		} finally {
			if (bossGroup != null) {
				bossGroup.shutdownGracefully();
			}
			if (workerGroup != null) {
				workerGroup.shutdownGracefully();
			}
		}
	}
}
