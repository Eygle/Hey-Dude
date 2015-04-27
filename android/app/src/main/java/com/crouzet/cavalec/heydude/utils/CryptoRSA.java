package crouzet.cryptotest;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by crouze_t on 4/27/2015.
 */
public class CryptoRSA {
    private Cipher cipher;
    private SecureRandom random;
    private Key pubKey;
    private Key privKey;
    private static CryptoRSA ourInstance = new CryptoRSA();

    public static CryptoRSA getInstance() {
        return ourInstance;
    }

    private CryptoRSA() {
        try {
            cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        random = new SecureRandom();
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (generator != null) {
            generator.initialize(2048, random);

            KeyPair pair = generator.generateKeyPair();
            pubKey = pair.getPublic();
            privKey = pair.getPrivate();
        }
    }

    public byte[] getPubKey() {
        return pubKey.getEncoded();
    }

    public byte[] encrypt(String text) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        cipher.init(Cipher.ENCRYPT_MODE, pubKey, random);

        byte[] input = text.getBytes();
        byte[] cipherText = cipher.doFinal(input);

        System.out.println("cipher: " + new String(cipherText));
        return cipherText;
    }

    public byte[] encrypt(String text, byte[] pubKeyByte) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyByte);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

        cipher.init(Cipher.ENCRYPT_MODE, pubKey, random);

        byte[] input = text.getBytes();
        byte[] cipherText = cipher.doFinal(input);

        System.out.println("cipher: " + new String(cipherText));
        return cipherText;
    }

    public byte[] decrypt(byte[] input) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.DECRYPT_MODE, privKey);

        byte[] plainText = cipher.doFinal(input);

        System.out.println("plain : " + new String(plainText));
        return plainText;
    }
}
