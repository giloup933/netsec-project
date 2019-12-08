import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
        boolean ct=true;
        try {
            while (true) {
                byte[] b=new byte[4];
                in.read(b, 0, 4);
                String command=new String(b);
                System.out.println(command+"^#*%&U^YU#$$U");
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
                    System.out.println(Utility.hexPrint(key)+" is the key");
                    Random r=new Random();
                    long cntr=r.nextLong();
                    ctr=new Counter(cntr);
                    cr=new Crypto(key, ctr);
                    cr.encMsg(out, "CONF".getBytes());
                }
                else if (command.equals("ENCR"))
                {
                    byte[] msg=cr.decMsg(in);
                    System.out.println(Utility.hexPrint(msg));
                    ByteArrayInputStream inp=new ByteArrayInputStream(msg);
                    byte[] aux=new byte[4];
                    inp.read(aux);
                    String cmd=new String(aux);
                    System.out.println(cmd+"!@!!!!");
                    if (cmd.equals("UPLD")) {
                        int len=(int)Counter.byte2long(Utility.readBytes(inp,8));
                        String fileName=new String(Utility.readBytes(inp,len));
                        len=(int)Counter.byte2long(Utility.readBytes(inp,8));
                        File f=new File("server-files/"+fileName);
                        System.out.println("file name length: "+len);
						System.out.println("filename is: " + fileName);
                        System.out.println("file length: "+len);
						System.out.println("file is: "+f);

                        FileOutputStream fos=new FileOutputStream(f);
                        fos.write(Utility.readBytes(inp,len));
                        fos.close();
						cr.encMsg(out, "SUCC".getBytes());
						// aux = new byte[8];
                        // inp.read(aux);
                        // aux=new byte[len];
                        // inp.read(aux);
                        // aux=new byte[8];
                        // inp.read(aux);
                        // aux=new byte[len];
                        // inp.read(aux);
                    }
                    else if (cmd.equals("DWNL")) {
                        byte []filenameBuf = new byte[inp.available()];
                        inp.read(filenameBuf);
                        String fileName=new String(filenameBuf);
                        if (!fileExists("server-files/"+fileName)) {
                            // TODO?
                        }
                        byte[] file=Utility.getFile(fileName);
                        if (file==null)
                        {
                            return;
                        }
                        // len=fileName.getBytes().length+file.length+8+"UPLD".getBytes().length;
                        // ByteArrayOutputStream stream=new ByteArrayOutputStream(len);
                        // stream.write("DATA".getBytes());
                        // stream.write(Counter.long2byteBE(fileName.getBytes().length));
                        // stream.write(fileName.getBytes());
                        // stream.write(Counter.long2byteBE(file.length));
                        // stream.write(file);
                        // cr.encMsg(out,file);
						cr.encMsg(out,
								  new byte[][]
							{"DATA".getBytes()
							 ,Counter.long2byteBE(fileName.length())
							 ,fileName.getBytes()
							 ,Counter.long2byteBE(file.length)
							 ,file});
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
					else if (cmd.equals("STTR")) {
						//STAT
					}
					else if (cmd.equals("CHAL")) {
						//ANSR
					}
                }
                try {
                    Thread.sleep(10);
                    while (in.available()<4)
                    {
                        Thread.sleep(1000);
                        //System.out.println(in.available());
                    }
                } catch (InterruptedException ex) {
                    //Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
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
