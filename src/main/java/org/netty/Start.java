package org.netty;

/**
 * socksserver启动类
 * 
 * @author zhaohui
 * 
 */
public class Start {

	public static void main(String[] args) {
		SocksServer ss = new SocksServer();
		ss.start();
	}
}
