package org.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netty.config.Config;
import org.netty.config.ConfigXmlLoader;
import org.netty.config.PacLoader;
import org.netty.mbean.IoAcceptorStat;
import org.netty.proxy.SocksServerInitializer;

public class SocksServer {

	private static Log logger = LogFactory.getLog(SocksServer.class);

	private static final String CONFIG = "conf/config.xml";

	private static final String PAC = "conf/pac.xml";

	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
	private ServerBootstrap bootstrap = null;

	public void start() {
		try {
			Config config = ConfigXmlLoader.load(CONFIG);
			PacLoader.load(PAC);

			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();
			bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new SocksServerInitializer(config));

			logger.info("Start At Port " + config.get_localPort());
			bootstrap.bind(config.get_localPort()).sync().channel()
					.closeFuture().sync();

			startMBean();
		} catch (Exception e) {
			logger.error("start error", e);
		} finally {
			stop();
		}
	}

	public void stop() {
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
		logger.info("Stop Server!");
	}

	/**
	 * java MBean 进行流量统计
	 */
	private void startMBean() {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		IoAcceptorStat mbean = new IoAcceptorStat();

		try {
			ObjectName acceptorName = new ObjectName(mbean.getClass()
					.getPackage().getName()
					+ ":type=IoAcceptorStat");
			mBeanServer.registerMBean(mbean, acceptorName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
