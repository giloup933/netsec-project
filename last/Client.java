import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import static java.lang.System.exit;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
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
    private BufferedReader sin=null;
    private InputStream in=null; 
    private OutputStream out=null;
    private String state="WAITING";
    private byte[] key=new byte[32];
    private Counter ctr;
    private String dwnl="";
    private Key pubKey;
    private Crypto cr;
    public String fileName="";
    protected boolean awaits_dwnl=false;
    public Client(String addr) {
        initCon(addr);
    }
    public void initCon(String addr) {
        try {
            socket=new Socket(InetAddress.getByName(addr), PORT);
            sin=new BufferedReader(new InputStreamReader(System.in));
            in=socket.getInputStream();
            out=socket.getOutputStream();
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
                    Thread.sleep(1000);
                    input="";
                    byte[] b=new byte[4];
                    in.read(b, 0, 4);
                    String command=new String(b);
                    if (command.equals("QUIT"))
                    {
                        closeCon();
                    }
                    else if (command.equals("SKEY"))
                    {
                        b=new byte[8];
                        in.read(b, 0, 8);
                        System.out.println(Utility.hexPrint(b));
                        int len=(int)Counter.byte2long(b);
                        System.out.println(len);
                        byte[] pubKey1=new byte[len];
                        in.read(pubKey1, 0, len);
                        System.out.println("key: "+Utility.hexPrint(pubKey1));
                        pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKey1));
                        state="WAITING";
                    }
                    else if (command.equals("ENCR"))
                    {
                        byte[] msg=cr.decMsg(in);
                        if (msg.equals("CONF")) {
                            
                        }
                        else if (msg.equals("UPLD")) {
                            
                        }
                        else if (msg.equals("DWNL")) {
                            
                        }
                        else if (msg.equals("DATA")) {
                            byte[] aux=new byte[4];
                            in.read(aux, 0, 4);
                            int len=(int)Counter.byte2long(aux);
                            aux=new byte[len];
                            in.read(aux, 0, len);
                            String fileName=new String(aux);
                            in.read(aux, 0, 4);
                            len=(int)Counter.byte2long(aux);
                            aux=new byte[len];
                            in.read(aux, 0, len);//this is the file
                            File f=new File("client-files/"+fileName);
                            FileOutputStream fos=new FileOutputStream(f);
                            fos.close();
                            fos.write(aux);
                        }
                        state="WAITING";
                    }
                    else if (command.equals("DATA"))
                    {
                        b=new byte[4];
                        in.read(b, 1, 4);
                        System.out.println("length="+new String(b));
                        int length=Integer.decode(new String(b));
                        b=new byte[length];
                        in.read(b, 1, length);
                        //the message is in b, decrypt it
                    }
                }
                catch (Exception e) {
                    System.out.println("connection abandonned..");
                    System.out.println(e);
                    closeCon();
                }
            }
        }
    }
    public void processUserInput(String str) throws IOException {
        state="PROCESSING";
        String toSend="";
        String[] spl=str.split(" ");
        if (str.equals("quit")) {
            out.write("QUIT".getBytes());
        }
        else if (str.equals("key")) {
            out.write("RKEY".getBytes());
        }
        else if (str.equals("init")) {
            Random r=new Random();
            key=new byte[32];
            r.nextBytes(key);
            System.out.println(Utility.hexPrint(key)+" is the key.");
            byte[] ek=encryptRSA(key, pubKey);
            long len=ek.length;
            System.out.println(len);
            toSend="INIT"+ek.length;//our code encrypted by RSA with server's public code
            out.write(("INIT").getBytes());
            out.write(Counter.long2byteBE(len));
            out.write(ek);
            //System.out.print(encryptRSA(key, Server.pubKey).length);
        }
        else if (spl[0].equals("upld")) {
            String fileName=spl[1];
            byte[] file=Utility.getFile(fileName);
            if (file==null)
            {
                return;
            }
            int len=fileName.getBytes().length+file.length+8+"UPLD".getBytes().length;
            ByteArrayOutputStream stream=new ByteArrayOutputStream(len);
            stream.write("UPLD".getBytes());
            stream.write(fileName.getBytes().length);
            stream.write(fileName.getBytes());
            stream.write(file.length);
            stream.write(file);
            cr.encMsg(out, stream.toByteArray());
        }
        else if (spl[0].equals("dwnl")) {
            String fileName=spl[1];
            int len=fileName.getBytes().length+"DWNL".getBytes().length+4;
            ByteArrayOutputStream stream=new ByteArrayOutputStream(len);
            stream.write("DWNL".getBytes());
            stream.write(fileName.getBytes().length);
            stream.write(fileName.getBytes());
            cr.encMsg(out, stream.toByteArray());
        }
        else {
            //invalid, ignore.
            state="WAITING";
            return;
        }
    }
    
    public byte[] encryptRSA(byte[] plaintext, Key pubKey) {
        Cipher encryptCipher = null;
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
            return encryptCipher.doFinal(plaintext);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new byte[0];
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
