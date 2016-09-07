package org.netty.encryption;

import java.io.ByteArrayOutputStream;

/**
 * Interface of crypt
 */
public interface ICrypt {
	void encrypt(byte[] data, ByteArrayOutputStream stream);

	void encrypt(byte[] data, int length, ByteArrayOutputStream stream);

	void decrypt(byte[] data, ByteArrayOutputStream stream);

	void decrypt(byte[] data, int length, ByteArrayOutputStream stream);

	int getIVLength();

	int getKeyLength();
}
