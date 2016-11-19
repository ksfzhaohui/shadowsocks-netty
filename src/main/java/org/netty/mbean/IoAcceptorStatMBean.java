package org.netty.mbean;

public interface IoAcceptorStatMBean {

	/**
	 * netty写流量 bytes/s 对浏览器来说其实就是下载速度
	 * 
	 * @return
	 */
	public long getWrittenBytesThroughput();

	/**
	 * netty 读流量 bytes/s 对浏览器来说其实就是上传速度
	 * 
	 * @return
	 */
	public long getReadBytesThroughput();
}
