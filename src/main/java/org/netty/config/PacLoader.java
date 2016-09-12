package org.netty.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * 加载pac配置xml
 * 
 * @author zhaohui
 * 
 */
public class PacLoader {

	private static Logger log = Logger.getLogger(PacLoader.class);

	private static List<String> domainList = new ArrayList<String>();
	private static List<String> tempList = new ArrayList<String>();

	/** 重加载的间隔时间 **/
	private static int reloadTime = 5;

	private static long lastModify;

	public static void load(final String filePath) throws Exception {
		File file = new File(filePath);
		if (!file.exists()) {
			throw new RuntimeException("file = " + filePath + " is not exist!");
		}
		if (file.lastModified() == lastModify) {
			return;
		}
		lastModify = file.lastModified();

		loadFile(filePath);

		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(
				new Runnable() {

					@Override
					public void run() {
						try {
							load(filePath);
						} catch (Exception e) {
							log.error(e);
						}
					}
				}, reloadTime, reloadTime, TimeUnit.SECONDS);
	}

	private synchronized static void loadFile(String file) throws Exception {
		tempList.clear();
		InputStream in = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			in = new FileInputStream(file);
			Document doc = builder.parse(in);
			NodeList list = doc.getElementsByTagName("domain");

			if (list.getLength() > 0) {
				for (int j = 0; j < list.getLength(); j++) {
					tempList.add(list.item(j).getTextContent());
				}
			}
			setDomainList(tempList);
			log.info("load pac !");
		} catch (Exception e) {
			throw e;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private synchronized static void setDomainList(List<String> tempList) {
		domainList.clear();
		domainList.addAll(tempList);
	}

	/**
	 * 指定的host是否需要代理
	 * 
	 * @param host
	 * @return
	 */
	public synchronized static boolean isProxy(String host) {
		for (String domain : domainList) {
			if (host.contains(domain)) {
				return true;
			}
		}
		return false;
	}
}
