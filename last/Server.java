/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author gil
 */
public class Server {
    public static Key pubKey;
    protected static Key privKey;
    static final int PORT = 3421;
    static int numCon=0;

    public static void main(String args[]) throws NoSuchAlgorithmException {
        makeKeys();
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client
            numCon++;
            new ServerThread(socket).start();
        }
    }
    public static void makeKeys() throws NoSuchAlgorithmException {
        System.out.println("making keys...");
        KeyPairGenerator kpg=KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp=kpg.generateKeyPair();
        pubKey=kp.getPublic();
        privKey=kp.getPrivate();
        //System.out.println(new String(pubKey.getEncoded())+" is the real public key!");
        //System.out.println(new String(pubKey.getEncoded())+" is the real public key!");
    }
    public static byte[] decryptRSA(String ciphertext, Key privKey) /*throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException*/ {
        return ciphertext.getBytes();
    }
    public static byte[] decryptRSA(byte[] ciphertext, Key privKey) /*throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException*/ {
        return ciphertext;
        //byte[] bytes = Base64.getDecoder().decode(ciphertext);
        //Cipher decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        //decryptCipher.init(Cipher.DECRYPT_MODE, privKey);
        //return new String(decryptCipher.doFinal(ciphertext), UTF_8);
        //return decryptCipher.doFinal(ciphertext);
    }
}
