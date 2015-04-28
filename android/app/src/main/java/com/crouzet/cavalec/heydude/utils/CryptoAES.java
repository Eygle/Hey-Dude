package com.crouzet.cavalec.heydude.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Johan on 27/04/2015.
 * Cryptographic tool that encrypt and decrypt
 * Use AES 256 bits CBC mode with PKCS7 padding
 */
public class CryptoAES {
    private Cipher cipher;
    private SecretKeySpec key;

    // Initialization vector
    private byte[] iv;

    private static SecureRandom sr = new SecureRandom();

    /**
     * Constructor
     * @param key symmetric AES key
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public CryptoAES(byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        this.key = new SecretKeySpec(key, "AES");
        cipher = Cipher.getInstance("AES/CBC/PKCS7Padding"); //, "BC");
    }

    /**
     * Encrypt text using AES
     * @param text plaintext
     * @return cipher text
     * @throws InvalidKeyException
     * @throws ShortBufferException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     */
    public byte[] encrypt(String text) throws InvalidKeyException, ShortBufferException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        iv = generateRandomBytes(16);

        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] input = text.getBytes();
        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        int ctLength = cipher.update(input, 0, input.length, cipherText, 0);

        ctLength += cipher.doFinal(cipherText, ctLength);

        return cipherText;
    }

    /**
     * Decrypt message using AES
     * @param input cipher text
     * @param iv Initialization vector
     * @return plaintext
     * @throws InvalidKeyException
     * @throws ShortBufferException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     */
    public String decrypt(byte[] input, byte[] iv) throws InvalidKeyException, ShortBufferException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] plainText = new byte[cipher.getOutputSize(input.length)];
        int ptLength = cipher.update(input, 0, input.length, plainText, 0);

        ptLength += cipher.doFinal(plainText, ptLength);

        return new String(plainText).substring(0, ptLength);
    }

    /**
     * @return Initialization vector
     */
    public byte[] getIV() {
        return iv;
    }

    /**
     * Used to generate key and iv
     * @param length length of generated array of bytes
     * @return random bites array
     */
    public static byte[] generateRandomBytes(int length) {
        byte[] b = new byte[length];
        sr.nextBytes(b);
        return b;
    }
}
