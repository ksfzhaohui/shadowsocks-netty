package org.netty.config;

/**
 * 远程服务器配置信息
 * 
 * @author hui.zhao.cfs
 *
 */
public class RemoteServer {

	/** 远程服务器地址和端口 **/
	private String _ipAddr;
	private int _port;

	/** 加密方法和密码 **/
	private String _method;
	private String _password;

	/** 状态 1:可用 0:不可用 **/
	private int status;

	public String toString() {
		return "_ipAddr = " + get_ipAddr() + ",_port = " + get_port();
	}

	public String get_ipAddr() {
		return _ipAddr;
	}

	public void set_ipAddr(String _ipAddr) {
		this._ipAddr = _ipAddr;
	}

	public int get_port() {
		return _port;
	}

	public void set_port(int _port) {
		this._port = _port;
	}

	public String get_method() {
		return _method;
	}

	public void set_method(String _method) {
		this._method = _method;
	}

	public String get_password() {
		return _password;
	}

	public void set_password(String _password) {
		this._password = _password;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
