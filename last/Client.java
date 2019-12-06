/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package last;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import static java.lang.System.exit;
import java.net.InetAddress;
import java.net.Socket;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author gil
 */
public class Client {
    static final int PORT=3421;
    private Socket socket=null;
    private BufferedReader in=null, sin=null; 
    private OutputStreamWriter out=null;
    private String state="WAITING";
    private byte[] key, ctr;
    private String dwnl="";
    public Key pubKey;
    protected String id;
    public String fileName="";
    protected boolean awaits_dwnl=false;
    public Client(String addr) {
        initCon(addr);
    }
    public void initCon(String addr) {
        try {
            socket=new Socket(InetAddress.getByName(addr), PORT);
            sin=new BufferedReader(new InputStreamReader(System.in));
            in=new BufferedReader(new InputStreamReader(socket.getInputStream()));//terminal? System.in
            out=new OutputStreamWriter(socket.getOutputStream());
            // out=new OutputStream(new OutputStreamWriter(socket.getOutputStream()), true); 
        }
        catch (Exception e) {
            System.out.println(e);
            exit(-1);
        }
        Loop();
    }
    public void Loop() {
        String input="";
        String input0="";
        while (true) {
            if (state.equals("WAITING"))
            {
                System.out.println("WAITIN'");
                try {
                    input0=sin.readLine();//terminal input
                    processUserInput(input0);
                    input0="";
                }
                catch (Exception e) {
                    System.out.println(e);
                    //closeCon();
                }
            }
            else {
                System.out.println("PROCESSIN'");
                try {
                    input=in.readLine();//from socket
                    processServerInput(input);
                    //System.out.println(input+" received.");
                    //state="WAITING";
                    input="";
                    //out.writeUTF(input);
                    state="WAITING";
                }
                catch (Exception e) {
                    System.out.println("connection abandonned..");
                    closeCon();
                }
            }
        }
    }
    public void processServerInput(String str) throws IOException, FileNotFoundException, NoSuchAlgorithmException {
        String[] ln=str.split("     ");
        for (String s: ln) {
            s=s.trim();
        }
        if (str.equals("QUIT")) {
            System.out.println("Closing time...");
            closeCon();
        }
        else {
            System.out.println(str+" received.");
            processServerProtocol(ln);
            //System.out.println("default.."+ln[0]);
            //System.out.println(ln[0].trim().equals("SKEY"));
        }
    }
    public void processServerProtocol(String[] str) throws IOException, FileNotFoundException, NoSuchAlgorithmException{
        if (str[0].equals("SKEY")) {
            for (int i=0;i<str.length;i++)
            {
                System.out.println(new String(str[i])+" ");
            }
            
        }
        else if (str[0].equals("ENCR")) {
            System.out.println("encr...");
            if (!str[1].equals(id)) {
                System.out.println("id error");
                //return;
            }
            if (!str[3].equals("CONF")) /*should be decrypted by RSA...*/{
                System.out.println("conf error");
                //return;
            }
            for (int i=0;i<str.length;i++)
            {
                System.out.println(i+": "+str[i]+".");
            }
            ctr=str[2].getBytes();
            System.out.println(str[2]+"is the string.");
            System.out.println("pre-counter: "+new String(ctr));
            System.out.println("pre-counter: "+ctr);
            ctr=Crypto.addOne(ctr);
            System.out.println("post-counter: "+new String(ctr));
            System.out.println("post-counter: "+ctr);
        }
        else if (str[0].equals("UPLD")) {
            if (str[1].equals("F")) {
                if (str[2].equals("NAME"))
                {
                    System.out.println("File by this name exists.");
                }
                else if (str[2].equals("BADNAME"))
                {
                    System.out.println("Bad file name: only letters, numbers and dots.");
                }
                else if (str[2].equals("CONG"))
                {
                    System.out.println("Network busy");
                }
            }
            else if (str[1].equals("T"))
            {
                //upload file here...
                String fileName="gitcat555.jpg";
                File f=new File(fileName);
                byte[] b=Crypto.encdecFile(key, ctr, fileName);
                if (b.length==0)
                {
                    System.out.println("file doesn't exist or is empty.");
                    return;
                }
                String toSend="DATA     "+fileName+"     "+new String(b)+"\n";
                System.out.println(toSend);
                out.write(toSend);
                out.flush();
            }
        }
        else if (str[0].equals("DWNL")) {
            if (str[1].equals("F")) {
                if (str[2].equals("NAME")) {
                    System.out.println("file "+str[3]+" doesn't exist.");
                }
                else if (str[2].equals("BADNAME"))
                {
                    System.out.println("Bad file name: only letters, numbers and dots.");
                }
                else if (str[2].equals("CONG"))
                {
                    System.out.println("Network busy.");
                }
                //maybe other reasons?
            }
            else {
                
            }
        }
        else if (str[0].equals("DATA")) {
            
        }
        //System.out.println(str[0]);
        //if (str[0].trim().equals("SKEY"))
        /*System.out.println("received: "+str);
        if (str[0].contains("SKEY"))
        {
            byte[] decodedKey=str[1].getBytes();//verify that it is 8 bytes...
            pubKey=new SecretKeySpec(decodedKey, 0, decodedKey.length, "RSA");
        }
        //else if (str[0].trim().equals("LIST"))
        else if (str[0].contains("LIST"))
        {
            if (str.length!=0)
            {
                //System.out.println("Server has:\n"+Arrays.copyOfRange(str, 1, str.length));
                System.out.println("Server has:");
                for (int i=1;i<str.length;i++)
                    System.out.print(str[i]);
                System.out.println();
            }
            else
                System.out.println("Server is empty");
        }
        //else if (str[0].trim().equals("ENCR"))
        else if (str[0].contains("ENCR"))
        {
        //session confirmed
            if (!str[1].equals(id)) {//problem, id's dont match. cancel everything and restart.
                return;
            }   
            String ctr=str[2];
            String encMsg=str[3];//k,ctr,"conf". decrypt msg with k and ctr, if getting conf its good
            if (!encMsg.equals("CONF")) {//problem
                return;
            }
        }
        //else if (str[0].equals("UPLD"))
        else if (str[0].contains("UPLD"))
        {
            if (str[1].equals("T"))
            {
                String toSend="DATA "+fileName+" ";
                toSend+=new String(Crypto.encdecFile(key, ctr, fileName));
                    //start file transfer here!!!
                    //##$%#^$&%$Q
                    //begin to send DATA messages...
            }
            else if (str[1].equals("F")) {//declined..
                if (str[2].equals("NAME"))
                    System.out.println("file name already exists.");
                    //add traffic cases
            }
            else {//error
            }  
        }
        //else if (str[0].equals("DWNL"))
        else if (str[0].contains("DWNL"))
        {
            if (str[1].equals("T")) {
                    //wait for file download here!!!
                    //##$%#^$&%$Q
            }
            else if (str[1].equals("F")) {//declined..
                awaits_dwnl=false;
                fileName="";
                if (str[2].equals("NOFILE"))
                    System.out.println("file not found.");
                    //add traffic cases
            }
            else {
                    //error
            }
        }
        //this is when downloading, so fetch the bytes and construct the files..
        //ADD FILE INTEGRITY CHECKS - LATER
        //else if (str[0].equals("DATA"))
        else if (str[0].contains("DATA"))
        {
            if (str[1].equals("DONE") && str[2].equals(fileName)) {
                System.out.println();
                File f=new File(fileName);
                FileOutputStream fos=new FileOutputStream(f);
                fos.write(Crypto.crypto(key, ctr, dwnl.getBytes()));
                fos.close();
                dwnl="";
                awaits_dwnl=false;
                fileName="";
                return;
            }
            else
            {
                ctr=str[1].getBytes();
                if (!str[2].equals(fileName))
                {
                    System.out.println("Warning: different file name");
                }
                String cipherF=str[3];
                for (int i=4;i<str.length;i++) {
                    cipherF+=" "+str[i];
                }
                dwnl+=cipherF;
            }
        }
        else
        {
            //state="WAITING";
            //System.out.println("bad input from server");
            //return;
            if (str.length>0)
                this.processServerProtocol(Arrays.copyOfRange(str, 1, str.length));
            else
            {
                state="WAITING";
                return;
            }
        }
        state="WAITING";*/
    }
    public void processUserInput(String str) throws IOException {
        state="PROCESSING";
        String toSend="";
        String[] spl=str.split(" ");
        if (str.equals("quit")) {
            toSend="QUIT";
        }
        else if (str.equals("key")) {
            toSend="RKEY";
        }
        else if (str.equals("init")) {
            id=new String(Crypto.randByteStr(4));
            key=Crypto.randByteStr(32);
            System.out.println(new String(key)+" is the key.");
            toSend="INIT     "+id+"     "+encryptRSA(key, Server.pubKey);//our code encrypted by RSA with server's public code
        }
        else if (spl[0].equals("upld")) {
            String fileName=spl[1];
            toSend="UPLD     "+fileName;
        }
        else if (spl[0].equals("dwnl")) {
            String fileName=spl[1];
            toSend="DWNL     "+fileName;
        }
        System.out.println("Sending: "+toSend+".");
        out.write(toSend+"\n");
        out.flush();
        /*String toSend="";
        String[] spl=str.split(" ");
        if (str.equals("quit")) {
            toSend="QUIT";
        }
        else if (str.equals("key")) {
            toSend="RKEY";
        }
        else if (str.equals("init")) {
            id=new String(Crypto.randByteStr(4));
            key=Crypto.randByteStr(32);
            toSend="INIT "+id+" "+encryptRSA(key, Server.pubKey);//our code encrypted by RSA with server's public code
        }
        else if (str.equals("list")) {
            toSend="LIST";
        }
        else if (spl[0].equals("upld")) {
            toSend="UPLD "+spl[1];//consider adding a flow ID to support several concurrent uploads/downloads
        }
        else if (spl[0].equals("dwnl")) {
            toSend="DWNL "+spl[1];
            fileName=spl[1];
            awaits_dwnl=true;
        }
        else {
            //invalid, ignore.
            state="WAITING";
            return;
        }
        out.write(toSend+"\n");
        out.flush();
        System.out.println("delivered.");*/
    }
    public String encryptRSA(byte[] plaintext, Key pubKey) {
        return new String(plaintext); // This is happening because I can't obtain the key from the socket yet.
        //byte[] bytes = Base64.getDecoder().decode(plaintext);
        /*Cipher encryptCipher = null;
        try {
            encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            encryptCipher.init(Cipher.ENCRYPT_MODE, pubKey);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            //return new String(encryptCipher.doFinal(bytes), UTF_8);
            return new String(encryptCipher.doFinal(plaintext), UTF_8);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";*/
    }
    public void closeCon() {
        try {
            out.close();
            in.close();
            socket.close();
            Server.numCon--;
            System.out.println("connection closed.");
            System.exit(0);
        }
        catch (IOException i) {
            System.out.println(i);
        }
    }
    public static void main(String[] args) {
        if (args.length<2)
            new Client("127.0.0.1");
        else
            new Client(args[0]);
    }
}
