package org.netty;

/**
 * socksserver启动类
 * @author zhaohui
 *
 */
public class Start {

	public static void main(String[] args) {
		new Thread(new SocksServer()).start();
	}
}
