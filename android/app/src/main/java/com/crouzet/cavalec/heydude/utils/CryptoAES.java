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
 */
public class CryptoAES {
    private Cipher cipher;
    private SecretKeySpec key;

    private byte[] iv;

    private static SecureRandom sr = new SecureRandom();

    public CryptoAES(byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        // si tu veux utiliser bouncy castle faut set la lib avec la ligne du dessus et rajouter le BC au Cipher.getInstance()
        this.key = new SecretKeySpec(key, "AES");
        cipher = Cipher.getInstance("AES/CBC/PKCS7Padding"); //, "BC");
    }

    public byte[] encrypt(String text) throws InvalidKeyException, ShortBufferException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        iv = generateRandomBytes(16);

        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] input = text.getBytes();
        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        int ctLength = cipher.update(input, 0, input.length, cipherText, 0);

        ctLength += cipher.doFinal(cipherText, ctLength);
        System.out.println(new String(cipherText));
        System.out.println(ctLength);
        return cipherText;
    }

    public String decrypt(byte[] input, byte[] iv) throws InvalidKeyException, ShortBufferException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] plainText = new byte[cipher.getOutputSize(input.length)];
        int ptLength = cipher.update(input, 0, input.length, plainText, 0);

        ptLength += cipher.doFinal(plainText, ptLength);

        System.out.println(new String(plainText).substring(0, ptLength));
        System.out.println(ptLength);

        return new String(plainText).substring(0, ptLength);
    }

    public byte[] getIV() {
        return iv;
    }

    public static byte[] generateRandomBytes(int length) {
        byte[] b = new byte[length];
        sr.nextBytes(b);
        return b;
    }
}
