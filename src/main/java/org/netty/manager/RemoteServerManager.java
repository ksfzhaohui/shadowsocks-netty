package org.netty.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.telnet.TelnetClient;
import org.netty.config.Config;
import org.netty.config.RemoteServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 远程服务器管理器
 * 
 * @author hui.zhao.cfs
 *
 */
public class RemoteServerManager {

	private static Logger logger = LoggerFactory.getLogger(RemoteServerManager.class);

	/** 可用 **/
	public static final int AVAILABLE = 1;
	/** 不可用 **/
	public static final int UNAVAILABLE = 0;

	private static Config config;
	private static Random random = new Random();

	public static void init(Config config) {
		RemoteServerManager.config = config;
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				try {
					checkStatus();
				} catch (Exception e) {
					logger.error("checkStatus error", e);
				}
			}
		}, 0, 30, TimeUnit.SECONDS);
	}

	/**
	 * 获取一台可用的远程服务器
	 * 
	 * @return
	 */
	public static RemoteServer getRemoteServer() {
		List<RemoteServer> availableList = new ArrayList<RemoteServer>();
		List<RemoteServer> remoteList = config.getRemoteList();
		for (RemoteServer remoteServer : remoteList) {
			if (remoteServer.getStatus() == AVAILABLE) {
				availableList.add(remoteServer);
			}
		}
		logger.info("available remoteServer size = " + availableList.size());
		if (availableList.size() > 0) {
			return availableList.get(random.nextInt(availableList.size()));
		}
		return remoteList.get(random.nextInt(remoteList.size()));
	}

	/**
	 * 检测远程服务器是否可以连接
	 */
	private static void checkStatus() {
		List<RemoteServer> remoteList = config.getRemoteList();
		for (RemoteServer remoteServer : remoteList) {
			if (isConnected(remoteServer)) {
				remoteServer.setStatus(AVAILABLE);
			} else {
				remoteServer.setStatus(UNAVAILABLE);
			}
		}
	}

	/**
	 * telnet 检测是否能连通
	 * 
	 * @param remoteServer
	 * @return
	 */
	private static boolean isConnected(RemoteServer remoteServer) {
		try {
			TelnetClient client = new TelnetClient();
			client.setDefaultTimeout(3000);
			client.connect(remoteServer.get_ipAddr(), remoteServer.get_port());
			return true;
		} catch (Exception e) {
			logger.warn("remote server: " + remoteServer.toString() + " telnet failed");
		}
		return false;
	}

}
