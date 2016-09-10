package org.netty.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

	public static void load(String file) throws Exception {
		InputStream in = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			in = new FileInputStream(file);
			Document doc = builder.parse(in);
			NodeList list = doc.getElementsByTagName("domain");

			if (list.getLength() > 0) {
				for (int j = 0; j < list.getLength(); j++) {
					domainList.add(list.item(j).getTextContent());
				}
			}
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

	/**
	 * 指定的host是否需要代理
	 * 
	 * @param host
	 * @return
	 */
	public static boolean isProxy(String host) {
		for (String domain : domainList) {
			if (host.contains(domain)) {
				return true;
			}
		}
		return false;
	}
}
