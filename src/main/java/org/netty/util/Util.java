package org.netty.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.SecureRandom;

/**
 * Helper class
 */
public class Util {
	public static String dumpBytes(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a)
			sb.append(String.format("%x", b & 0xff));
		return sb.toString();
	}

	public static byte[] randomBytes(int size) {
		byte[] bytes = new byte[size];
		new SecureRandom().nextBytes(bytes);
		return bytes;
	}

	public static String getErrorMessage(Throwable e) {
		Writer writer = new StringWriter();
		PrintWriter pWriter = new PrintWriter(writer);
		e.printStackTrace(pWriter);
		return writer.toString();
	}

}
