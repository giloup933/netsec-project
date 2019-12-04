package last;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import static java.lang.Integer.max;
import java.net.Socket;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gil
 */
public class ServerThread extends Thread{
    static final int MAX_STR_SIZE=50000;
    static int num=0;
    protected Socket socket;
    private byte[]key, ctr;
    protected String id;
    public Key pubKey, privKey;
    public String[] files;
    private File folder=new File("./server-files");
    public ServerThread(Socket clientSocket) {
        this.socket=clientSocket;
    }
    
    public void run() {
        System.out.println("Client #"+(num++)+" accepted...");
        BufferedReader brinp = null;
        PrintWriter out = null;
        try {
            brinp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true); 
        } catch (IOException e) {
            return;
        }
        String line;
        while (true) {
            try {
                if (brinp.ready()) {
                    line=brinp.readLine();
                    System.out.println(line+" is the line.");
                    if (line.equals("QUIT"))
                    {
                        System.out.println("Thread closed.");
                        socket.close();
                        System.exit(0);
                    }
                    else if (line.equals("RKEY"))
                    {
                        out.write("SKEY     "+new String(Server.pubKey.getEncoded())+"\n");
                        System.out.println(new String(Server.pubKey.getEncoded())+" is the RSA pubKey.");
                        out.flush();
                    }
                    else
                    {
                        String[] slp=line.split("     ");
                        for (int i=0; i<slp.length; i++)
                        {
                            //System.out.println(slp[i]);
                        }
                        if (slp[0].equals("INIT")) {
                            id=slp[1];
                            key=Server.decryptRSA(slp[2], Server.privKey);
                            System.out.println(new String(key)+" is the key.");
                            ctr=Crypto.randByteStr(8);
                            System.out.println(new String(ctr)+" is the ctr.");
                            String encMsg="CONF";//encrypt that
                            out.write("ENCR     "+id+"     "+new String(ctr)+"     "+encMsg+"\n");
                            out.flush();
                        }
                        else if (slp[0].equals("UPLD")) {
                            String fileName=slp[1];
                            if (fileExists(fileName)) {
                                out.write("UPLD     F     "+fileName+"     NAME\n");
                                out.flush();
                            }
                            else if (!isSanitized(fileName)) {
                                out.write("UPLD     F     "+fileName+"     BADNAME\n");
                                out.flush();
                            }
                            out.write("UPLD     T\n");
                            out.flush();
                        }
                        else if (slp[0].equals("DWNL")) {
                            String fileName=slp[1];
                            if (!fileExists(fileName))
                            {
                                out.write("DWNL     F     "+fileName+"     NAME\n");
                                out.flush();
                            }
                            else if (!isSanitized(fileName)) {
                                out.write("DWNL     F     "+fileName+"     BADNAME\n");
                                out.flush();
                            }
                            else
                            {
                                File f=new File("server-files/"+fileName);
                                if (f.exists()) {
                                    byte[] b=Crypto.encdecFile(key, ctr, fileName);
                                    String toSend="DATA     "+fileName+"     "+new String(b)+"\n";
                                    out.write(toSend+"\n");
                                }
                                else {
                                    out.write("DWNL     F     "+fileName+"     NAME\n");
                                }
                                out.flush();
                            }
                        }
                        else if (slp[0].equals("DATA")) {
                            String fileName=slp[1];
                            String data=slp[2];
                            FileOutputStream fos=new FileOutputStream(new File("server-files/"+fileName));
                            byte[] b=Crypto.crypto(key, ctr, data.getBytes());
                            fos.write(b);
                            fos.close();
                        }
                    }
                    //out.write(line+"\n");
                    //out.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /*BufferedReader brinp = null;
        DataOutputStream out = null;
        try {
            brinp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        String line;
        if (!folder.exists())
        {
            try {
                folder.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        while (true) {
            try {
                if (brinp.ready()) {
                    line = brinp.readLine();
                    if ((line == null) || line.equalsIgnoreCase("QUIT")) {
                        System.out.println("Thread closed.");
                        socket.close();
                        return;
                    }
                    else {
                        String[] ln=line.split(" ");
                        if (ln[0].equals("RKEY")) {
                            System.out.println(Server.pubKey.toString());
                            out.writeUTF("SKEY "+new String(Server.pubKey.getEncoded())+"\n");
                            out.flush();
                        }
                        if (ln[0].equals("INIT")) {
                            if (ln.length>=3) {
                                String id = ln[1];
                                String encKey="";
                                //encKey = ln[2];
                                for (int i=2;i<ln.length;i++) {
                                    encKey += ln[i];
                                }
                                key=encKey.getBytes();
                                ctr=Crypto.randByteStr(8);
                                String encMsg="CONF";
                                out.writeUTF("ENCR "+id+" "+new String(ctr)+" "+encMsg+"\n");
                                out.flush();
                            }
                            else
                            {
                                out.writeUTF("ERR INIT");
                            }
                        }
                        if (ln[0].equals("LIST")) {
                            if (!contents().equals(""))
                            {
                                out.writeUTF("LIST "+contents()+"\n");
                                out.flush();
                            }
                            else
                            {
                                out.writeUTF("LIST\n");
                                out.flush();
                            }
                        }
                        if (ln[0].equals("UPLD")) {
                            String fileName=ln[1];
                            if (fileExists(fileName)) {
                                out.writeUTF("UPLD F NAME\n");
                                out.flush();
                            }
                            out.writeUTF("UPLD T\n");
                            out.flush();
                        }
                        if (ln[0].equals("DWNL")) {
                            String fileName=ln[1];
                            if (!fileExists(fileName)) {
                                out.writeUTF("DWNL F NOFILE\n");
                            }
                            //out.writeUTF("DWNL T"\n);
                            //out.flush();
                            try {
                                String toSend="DATA "+ctr+" "+fileName+" ";
                                String file = new String(Crypto.encdecFile(key, ctr, "server-files/"+fileName));
                                if (file.length()+toSend.length()<MAX_STR_SIZE)
                                {
                                    out.writeUTF(toSend+file);
                                    out.flush();
                                }
                                else
                                {
                                    int size = toSend.length() + file.length();
                                    int ind=0;
                                    while (ind*MAX_STR_SIZE>=size)
                                    {
                                        out.writeUTF(toSend+Integer.toString(ind)+" "+file.substring(ind*MAX_STR_SIZE, 
                                                max((ind+1)*MAX_STR_SIZE, file.length())));
                                        out.flush();
                                    }
                                }
                                //toSend+=new String(file);
                                //out.writeUTF(toSend.substring(0, 50000));
                                toSend="DATA DONE "+fileName;
                                out.writeUTF(toSend);
                                out.flush();
                            } catch (FileNotFoundException ex) {
                                out.writeUTF("file not found");
                                out.flush();
                                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (NoSuchAlgorithmException ex) {
                                out.writeUTF("algorithm error");//shouldn't happen...
                                out.flush();
                                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        if (ln[0].equals("DATA")) {
                            String ctr=ln[1];
                            //String fileName=ln[1];
                            //add CID
                            out.writeUTF("ACKD");//CID...and maybe ack n
                            out.flush();
                        }
                        //ADD FILE INTEGRITY CHECKS - LATER
                        System.out.println("Hello "+line+" .");
                        //out.writeBytes("933!!!");
                        //out.writeBytes(line + "\n\r");
                        //out.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }*/
    }
    public boolean isSanitized(String fileName) {
        //for the file name, we are allowing letters, numbers, and dots only.
        Pattern p= Pattern.compile("[a-z0-9.]", Pattern.CASE_INSENSITIVE);
        Matcher m= p.matcher(fileName);
        return m.find();
    }
    public String contents() {
        String str="";
        File[] f = folder.listFiles();
        //System.out.println(f);
        //System.out.println("hi!!");
        if (f.length==0)
        {
            //System.out.println("zero");
            return "";
        }
        for (File file : f)
        {
            System.out.println("name: "+file.getName());
            str+=file.getName()+" ";
        }
        //System.out.println(str);
        return str;
    }
    public boolean fileExists(String fileName) {
        //for now...
        return false;
        /*File[] f = folder.listFiles();
        if (f.length==0)
            return false;
        for (File file : f)
        {
            if (fileName.equals(file.getName()))
                return true;
        }
        return false;*/
        /*if (files.length==0)
            return false;
        for (String fileName : files) {
            if (fileName.equals(str))
                return true;
        }
        return false;*/
    }
}
