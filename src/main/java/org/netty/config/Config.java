package org.netty.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置
 * 
 * @author zhaohui
 * 
 */
public class Config {

	private int _localPort;
	private List<RemoteServer> remoteList = new ArrayList<RemoteServer>(20);

	public Config() {

	}

	public void addRemoteConfig(RemoteServer remoteConfig) {
		remoteList.add(remoteConfig);
	}

	public List<RemoteServer> getRemoteList() {
		return remoteList;
	}

	public int get_localPort() {
		return _localPort;
	}

	public void set_localPort(int _localPort) {
		this._localPort = _localPort;
	}

}
