package com.unirc.tesi.marcoventura.contacttracing.cipher;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MakeHash {

    private static final String CHARSET = "UTF-8";
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Generates SHA256 hash of the password which is used as key
     *
     * @param password used to generated key
     * @return SHA256 of the password
     */
    public static String generateSHA(final String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        String res = null;

        final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] bytes = password.getBytes(CHARSET);
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();

        res = bytesToHex(key);

        return res;
    }

    /**
     * Converts byte array to hexidecimal useful for logging and fault finding
     * @param bytes
     * @return Hex String of the digest
     */
    private static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}