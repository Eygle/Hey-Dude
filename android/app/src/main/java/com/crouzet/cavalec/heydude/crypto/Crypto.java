package com.crouzet.cavalec.heydude.crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Johan on 01/03/2015.
 */
public class Crypto {
    private static Cipher cipher;
    private static SecureRandom random = new SecureRandom();

    public static void initCrypto() {
        try {
            //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            //TLS_DHE_RSA_WITH_AES_256_CBC_SHA256
            cipher = Cipher.getInstance("RSA/CBC/OAEPWithSHA-256AndMGF1Padding", "BC");
        } catch (NoSuchAlgorithmException|NoSuchProviderException|NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public static KeyPair generateKeys() {
        try {

            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
            generator.initialize(386, random);

            KeyPair pair = generator.generateKeyPair();

            return pair;
        } catch (NoSuchAlgorithmException|NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(String p, Key pubKey) {
        try {
            byte[] input = p.getBytes();

            cipher.init(Cipher.ENCRYPT_MODE, pubKey, random);
            byte[] cipherText = cipher.doFinal(input);

            return new String(cipherText);
        } catch (InvalidKeyException|IllegalBlockSizeException|BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String c, Key privKey) {
        try {
            byte[] cipherText = c.getBytes();

            cipher.init(Cipher.DECRYPT_MODE, privKey);
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText);
        } catch (InvalidKeyException|IllegalBlockSizeException|BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
