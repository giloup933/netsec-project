/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import static java.lang.System.exit;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author gil
 */
public class Client {
    private Crypto c;
    public int port=3421;
    private String kill="Over";
    private Socket socket=null;
    private DataInputStream in=null, sin=null; 
    private DataOutputStream out=null;
    public Client(String addr) {
        initCon(addr);
    }
    public void initCon(String addr) {
        try {
            socket=new Socket(InetAddress.getByName(addr), port);
            sin=new DataInputStream(System.in);
            in=new DataInputStream(socket.getInputStream());//terminal? System.in
            out=new DataOutputStream(socket.getOutputStream()); 
        }
        catch (Exception e) {
            System.out.println(e);
            exit(-1);
        }
        Loop();
    }
    public void Loop() {
        String input="";
        while (!input.equals(kill)) {
            try {
                input=in.readUTF();
                out.writeUTF(input);
            }
            catch (Exception i) {
                System.out.println(in+"!!!");
                System.out.println(i+"...");
            }
        }
        closeCon();
    }
    public void closeCon() {
        try {
            out.close();
            in.close();
            socket.close();
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
