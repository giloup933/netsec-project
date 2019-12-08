import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import static java.lang.Integer.max;
import java.net.Socket;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ServerThread extends Thread{
    static final int MAX_STR_SIZE=50000;
    static int num=0;
    protected Socket socket;
    private byte[]key;
    public String[] files;
    private File folder=new File("./server-files");
    private Crypto cr;
    private Counter ctr;
    private InputStream in=null;
    private OutputStream out=null;
    public ServerThread(Socket clientSocket) {
        this.socket=clientSocket;
        Random r=new Random();
        ctr=new Counter(r.nextLong());
        key=new byte[32];
        r.nextBytes(key);
        cr=new Crypto(key, ctr);
    }
    
    public void run() {
        System.out.println("Client #"+(num++)+" accepted...");
        try {
            in=socket.getInputStream();
            out=socket.getOutputStream();
        } catch (IOException e) {
            return;
        }
        String line;
        byte[] b=new byte[4];
        boolean ct=true;
        try {
            while (true) {
                in.read(b, 0, 4);
                String command=new String(b);
                System.out.println(command);
                if (command.equals("QUIT"))
                {
                    System.out.println("Thread closed.");
                    socket.close();
                    System.exit(0);
                }
                else if (command.equals("RKEY"))
                {
                    long len=Server.pubKey.getEncoded().length;
                    System.out.println(Utility.hexPrint(Counter.long2byteBE(len)));
                    System.out.println("key: "+Utility.hexPrint(Server.pubKey.getEncoded()));
                    System.out.println(Server.pubKey.getEncoded().length);
                    out.write(("SKEY").getBytes());
                    out.write(Counter.long2byteBE(len));
                    out.write(Server.pubKey.getEncoded());
                    out.flush();
                }
                else if (command.equals("INIT"))
                {
                    b=new byte[8];
                    in.read(b, 0, 8);
                    int len=(int)Counter.byte2long(b);
                    System.out.println(len);
                    b=new byte[len];
                    in.read(b, 0, len);
                    key=Server.decryptRSA(b, Server.privKey);
                    Random r=new Random();
                    long cntr=r.nextLong();
                    ctr=new Counter(cntr);
                    cr=new Crypto(key, ctr);
                    cr.encMsg(out, "CONF".getBytes());
                }
                else if (command.equals("ENCR"))
                {
                    byte[] msg=cr.decMsg(in);
                    byte[] aux=new byte[4];
                    in.read(aux, 0, 4);
                    String cmd=new String(aux);
                    if (cmd.equals("UPLD")) {
                        in.read(aux, 0, 4);
                        int len=(int)Counter.byte2long(aux);
                        aux=new byte[len];
                        in.read(aux, 0, len);
                        String fileName=new String(aux);
                        in.read(aux, 0, 4);
                        len=(int)Counter.byte2long(aux);
                        aux=new byte[len];
                        in.read(aux, 0, len);//this is the file
                        File f=new File("server-files/"+fileName);
                        FileOutputStream fos=new FileOutputStream(f);
                        fos.close();
                        fos.write(aux);
                    }
                    else if (cmd.equals("DWNL")) {
                        in.read(aux, 0, 4);
                        int len=(int)Counter.byte2long(aux);
                        aux=new byte[len];
                        in.read(aux, 0, len);
                        String fileName=new String(aux);
                        if (!fileExists("server-files/"+fileName)) {
                            
                        }
                        byte[] file=Utility.getFile(fileName);
                        if (file==null)
                        {
                            return;
                        }
                        len=fileName.getBytes().length+file.length+8+"UPLD".getBytes().length;
                        ByteArrayOutputStream stream=new ByteArrayOutputStream(len);
                        stream.write("DATA".getBytes());
                        stream.write(fileName.getBytes().length);
                        stream.write(fileName.getBytes());
                        stream.write(file.length);
                        stream.write(file);
                        cr.encMsg(out, stream.toByteArray());
                    }
                    else if (cmd.equals("LIST")) {
                        File dir=new File("server-files");
                        String[] children=dir.list();
                        if (children==null)
                        {
                            System.out.println("empty");
                            cr.encMsg(out, "LISTNONE".getBytes());
                        }
                        else
                        {
                            String answer="";
                            for (int i=0;i<children.length;i++) {
                                answer+=children[i];
                            }
                            int len=4+answer.getBytes().length;
                            ByteArrayOutputStream stream=new ByteArrayOutputStream(len);
                            stream.write("LIST".getBytes());
                            stream.write(answer.getBytes());
                            cr.encMsg(out, stream.toByteArray());
                        }
                    }
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
                try {
                    Thread.sleep(10);
                    while (in.available()<4)
                    {
                        //System.out.println(in.available());
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        catch (IOException e) {
            System.out.println(e+"...");
        }
        System.out.println("left");
    }
    private boolean fileExists(String fileName) {
        return true;
    }
}