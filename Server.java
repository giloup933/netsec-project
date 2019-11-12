/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gil
 */
public class Server {
    private Crypto c;
    public int port=3421;
    public int numClients=0;
    public int numSyn=0;
    public int maxClients=20;
    public int maxSyn=10;
    public String key="532436u7jhgt6u7ki";
    public int ctr=0;
    private String kill="Over";
    private Socket socket=null;
    private ServerSocket server=null;
    private DataInputStream in=null; 
    private DataOutputStream out=null;
    Map<String, File> files=new HashMap<>();
    public Server() {
        try {
            server=new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for clients...");
            socket=server.accept();
            numClients++;
            System.out.println("Client #"+numClients+" accepted.");
            in=new DataInputStream(socket.getInputStream());//new BufferedInputStream(socket.getInputStream()));?
            out=new DataOutputStream(socket.getOutputStream());
            String input="";
            while(!input.equals(kill))
            {
                try {
                    input = in.readUTF(); 
                    System.out.println(input); 
                }
                catch (IOException i)
                {
                    System.out.println(i);
                }
            }
        } catch (IOException i) {
            System.out.println(i);
        }
    }
    public void handleInput(String input) {
        String[] msg = input.split("\\s+");
        switch(msg[0]) {
            case "reqKey":
                output(key);
                break;
            case "reset":
                break;
            case "sync":
                break;
            case "init":
                break;
            case "upld":
                break;
            case "dwnl":
                //file name stored in msg[1]
                if (files.containsKey(msg[1]))
                {
                    try {
                        byte[] b = c.encryptFile(files.get(msg[1]), key, ctr); //this should be transmitted to the client
                    }
                    catch (NoSuchAlgorithmException | IOException e) {
                        System.out.println(e);
                    }
                }
                else
                    output("err file-does-not-exist");
                break;
            case "err":
                break;
            case "over":
                break;
            default:
                break;
        }
        closeCon();
    }
    public void output(String output) {
        //NOT IMPLEMENTED YET
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
        new Server();
    }
}
