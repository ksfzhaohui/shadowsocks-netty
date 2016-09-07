package org.netty.encryption;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netty.encryption.impl.AesCrypt;
import org.netty.encryption.impl.BlowFishCrypt;
import org.netty.encryption.impl.CamelliaCrypt;
import org.netty.encryption.impl.SeedCrypt;

/**
 * Crypt factory
 */
public class CryptFactory {

	private static Log logger = LogFactory.getLog(CryptFactory.class);

	private static Map<String, String> crypts = new HashMap<String, String>();

	static {
		crypts.putAll(AesCrypt.getCiphers());
		crypts.putAll(CamelliaCrypt.getCiphers());
		crypts.putAll(BlowFishCrypt.getCiphers());
		crypts.putAll(SeedCrypt.getCiphers());
	}

	public static ICrypt get(String name, String password) {
		String className = crypts.get(name);
		if (className == null) {
			return null;
		}

		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getConstructor(String.class,
					String.class);
			return (ICrypt) constructor.newInstance(name, password);
		} catch (Exception e) {
			logger.error("get crypt error", e);
		}

		return null;
	}
}
