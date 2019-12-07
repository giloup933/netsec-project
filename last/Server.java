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
import java.security.SecureRandom;
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
        kpg.initialize(1024, new SecureRandom());
        //308227721030d692a864886f7d11150482261308225d210281810f44d1b1bcc1da8
        //308227821030d692a864886f7d11150482262308225e210281810b73d37e0529353
        //308227621030d692a864886f7d11150482260308225c210281810b2a2a99874637c
        KeyPair kp=kpg.generateKeyPair();
        pubKey=kp.getPublic();
        privKey=kp.getPrivate();
        //System.out.println(new String(pubKey.getEncoded())+" is the real public key!");
        //System.out.println(new String(pubKey.getEncoded())+" is the real public key!");
    }
    public static byte[] decryptRSA(byte[] ciphertext, Key privKey) /*throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException*/ {
        try {
            Cipher decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            decryptCipher.init(Cipher.DECRYPT_MODE, privKey);
            return decryptCipher.doFinal(ciphertext);
        }
        catch (Exception e) {
            System.out.println(e);
            System.exit(-1);
        }
        return null;
    }
}
