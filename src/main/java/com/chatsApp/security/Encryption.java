package com.chatsApp.security;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    private static final String ALG = "AES";
    private static final String MODE = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private PrivateKey PRIVATE_KEY;
    private PublicKey PUBLIC_KEY;
    private PublicKey PEER_KEY;
    private SecretKey SECRET_KEY;
    private int KEY_SIZE;
    private Cipher CIPHER;

    public Encryption(int keySize) {
        this.KEY_SIZE = keySize;
        try {
            CIPHER = Cipher.getInstance(MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Encryption generateKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("X25519");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        this.PRIVATE_KEY = keyPair.getPrivate();
        this.PUBLIC_KEY = keyPair.getPublic();
        return this;
    }

    public Encryption sendPublicKey(ObjectOutputStream OUT) throws Exception {
        OUT.writeObject(PUBLIC_KEY.getEncoded());
        OUT.flush();
        return this;
    }

    public Encryption getPeerPublicKey(ObjectInputStream IN) throws Exception {
        byte[] peerKeyBytes = (byte[]) IN.readObject();
        KeyFactory keyFactory = KeyFactory.getInstance("X25519");
        PEER_KEY = keyFactory.generatePublic(new X509EncodedKeySpec(peerKeyBytes));

        return this;
    }

    public Encryption getSharedSecret() throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("X25519");
        keyAgreement.init(this.PRIVATE_KEY);
        keyAgreement.doPhase(PEER_KEY, true);
        byte[] sharedSecret = keyAgreement.generateSecret();

        byte[] aesKey = Arrays.copyOf(sharedSecret, KEY_SIZE / 8);
        SECRET_KEY = new SecretKeySpec(aesKey, ALG);

        return this;
    }

    public byte[] encrypt(byte[] data) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(iv);

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        CIPHER.init(Cipher.ENCRYPT_MODE, SECRET_KEY, gcmParameterSpec);
        byte[] encryptedData = CIPHER.doFinal(data);

        byte[] result = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);

        return result;
    }

    public byte[] decrypt(byte[] data) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(data, 0, iv, 0, GCM_IV_LENGTH);

        byte[] encryptedData = new byte[data.length - GCM_IV_LENGTH];
        System.arraycopy(data, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        CIPHER.init(Cipher.DECRYPT_MODE, SECRET_KEY, gcmParameterSpec);

        return CIPHER.doFinal(encryptedData);
    }
}
