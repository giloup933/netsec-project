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
                System.out.println(command+" RECEIVED");
                if (command.equals("QUIT"))
                {
					out.write("QUIT".getBytes());
                    System.out.println("Thread closed.");
                    socket.close();
					return;
                    // System.exit(0);
                }
                else if (command.equals("RKEY"))
                {
                    long len=Server.pubKey.getEncoded().length;
                    System.out.println(Utility.hexPrint(Utility.long2byteBE(len)));
                    System.out.println("key: "+Utility.hexPrint(Server.pubKey.getEncoded()));
                    System.out.println(Server.pubKey.getEncoded().length);
                    out.write(("SKEY").getBytes());
                    out.write(Utility.long2byteBE(len));
                    out.write(Server.pubKey.getEncoded());
                    out.flush();
                }
                else if (command.equals("INIT"))
                {
                    b=new byte[8];
                    in.read(b, 0, 8);
                    int len=(int)Utility.byte2long(b);
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
                    ByteArrayInputStream inp = cr.decMsgStream(in);
                    // System.out.println(Utility.hexPrint(msg));
                    // ByteArrayInputStream inp=new ByteArrayInputStream(msg);
                    // byte[] aux=Utility.readBytes(inp,8);
                    // inp.read(aux);
                    // String cmd=new String(aux);
                    // System.out.println("Encrypted "+cmd+" received");
					String cmd = Utility.readCommand(inp);
                    if (cmd.equals("UPLD")) {
                        int len=(int)Utility.byte2long(Utility.readBytes(inp,8));
                        System.out.println("file name length: "+len);
                        String fileName=new String(Utility.readBytes(inp,len));
						System.out.println("filename is: " + fileName);
                        len=(int)Utility.byte2long(Utility.readBytes(inp,8));
                        System.out.println("file length: "+len);
                        File f=new File("server-files/"+fileName);
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
                    } else if (cmd.equals("CHAL")) {
						long offset = Utility.readLong(inp);
						long len = Utility.readLong(inp);
						byte []salt = Utility.readBytes(inp,32);
						String filename = new String(Utility.readBytes(inp,inp.available()));
                        File f=new File("server-files/"+filename);
						System.out.println("Got a challenge for " + filename);
						System.out.println(" at offset " + offset);
						System.out.println(" of length " + len);
						System.out.println(" with salt " + Utility.hexPrint(salt));

						byte []answer = Crypto.challenge(f,(int)offset,(int)len,salt);
						cr.encMsg(out, new byte[][]{"ANSR".getBytes() ,answer});
                    } else if (cmd.equals("DWNL")) {
                        byte []filenameBuf = new byte[inp.available()];
                        inp.read(filenameBuf);
                        String fileName="server-files/"+new String(filenameBuf);
                        if (!fileExists(fileName)) {
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
                        // stream.write(Utility.long2byteBE(fileName.getBytes().length));
                        // stream.write(fileName.getBytes());
                        // stream.write(Utility.long2byteBE(file.length));
                        // stream.write(file);
                        // cr.encMsg(out,file);
						cr.encMsg(out, new byte[][] {"DATA".getBytes() ,file});
						// cr.encMsg(out,
						// 		  new byte[][]
						// 	{"DATA".getBytes()
						// 	 ,Utility.long2byteBE(fileName.length())
						// 	 ,fileName.getBytes()
						// 	 ,Utility.long2byteBE(file.length)
						// 	 ,file});
                    } else if (cmd.equals("STAT")) {
                        byte []filenameBuf = new byte[inp.available()];
						String fileName = "server-files/" + new String(Utility.readBytes(inp,inp.available()));
                        File file = new File(fileName);
                        if (!fileExists(fileName)) {
                            // TODO?
                        }
                        if (file==null)
                        {
                            return;
                        }
                        // len=fileName.getBytes().length+file.length+8+"UPLD".getBytes().length;
                        // ByteArrayOutputStream stream=new ByteArrayOutputStream(len);
                        // stream.write("DATA".getBytes());
                        // stream.write(Utility.long2byteBE(fileName.getBytes().length));
                        // stream.write(fileName.getBytes());
                        // stream.write(Utility.long2byteBE(file.length));
                        // stream.write(file);
                        // cr.encMsg(out,file);
						cr.encMsg(out, new byte[][] {
								"STTR".getBytes()
								,Utility.long2byteBE(file.length())
								,Crypto.digest(Utility.getFile(fileName))});
						// cr.encMsg(out,
						// 		  new byte[][]
						// 	{"DATA".getBytes()
						// 	 ,Utility.long2byteBE(fileName.length())
						// 	 ,fileName.getBytes()
						// 	 ,Utility.long2byteBE(file.length)
						// 	 ,file});
                    } else if (cmd.equals("LIST")) {
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
                try {
                    Thread.sleep(10);
                    while (in.available()<4)
                    {
                        Thread.sleep(100);
                        //System.out.println(in.available());
                    }
                } catch (InterruptedException ex) {
                    //Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        catch (Exception e) {
            System.out.println(e+"...");
        }
        System.out.println("left");
    }
    private boolean fileExists(String fileName) {
        return true;
    }
}
