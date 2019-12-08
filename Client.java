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
import java.security.SecureRandom;
import java.util.Arrays;
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
    private Socket socket;
    private BufferedReader sin;
    private InputStream in;
    private OutputStream out;
    private String state;
    private byte[] key = null;
    private Key pubKey;
    private Crypto cr;
	private Counter ctr;
	SecureRandom random;
    public Client(String addr) throws Exception {
		random = new SecureRandom();
		socket = new Socket(InetAddress.getByName(addr), PORT);
		sin = new BufferedReader(new InputStreamReader(System.in));
		in = socket.getInputStream();
		out = socket.getOutputStream();
		state = "WAITING";
    }

    public void Loop() throws Exception{
        String input="";
        String input0="";
        while (true) {
			System.out.println("Enter command: ");
			input0=sin.readLine();//terminal input
			processUserInput(input0);
			input0="";
		}
	}

	public void challengeServer(String fileName, long offset, long len) throws Exception {
		File file = new File("client-files/"+fileName);
		// long offset = random.nextLong(file.length());
		// long len = random.nextLong(file.length()-offset);
		byte []salt = new byte[32];
		random.nextBytes(salt);
		byte []chal = Crypto.challenge(file,(int)offset,(int)len,salt);
		if (file==null) {
			return;
		}
		System.out.println("Challenging " + fileName);
		System.out.println(" at offset " + offset);
		System.out.println(" of length " + len);
		System.out.println(" with salt " + Utility.hexPrint(salt));
		cr.encMsg(out, new byte[][]{
				"CHAL".getBytes()
				,Utility.long2byteBE(offset)
				,Utility.long2byteBE(len)
				,salt
				,fileName.getBytes()
			});

		ByteArrayInputStream dec = cr.decResponse(in);
		final String decmd = Utility.readCommand(dec);
		if (decmd.equals("ANSR")) {
			byte []response = Utility.readBytes(dec,dec.available());
			System.out.println("Challenge answer is: " + Utility.hexPrint(chal));
			System.out.println("Challenge response is: " + Utility.hexPrint(response));
			if (Arrays.equals(chal,response)) {
				System.out.println("Correct response");
			} else {
				System.out.println("Incorrect response");
			}
			System.out.println("Upload successful");
		} else if (decmd.equals("FAIL")) {
			System.out.println("CHAL FAILURE");
		} else {
			System.err.println("CHAL unexpected response " + decmd);
		}
	}

    public void processUserInput(String str) throws Exception {
        // state="PROCESSING";
        // String toSend="";
		if (str == null) {
			str = "quit";
		}
        String[] spl=str.split(" ");
        if (str.equals("quit")) {
            out.write("QUIT".getBytes());
			final String command = Utility.readCommand(in);
			if (!command.equals("QUIT")) {
				System.err.println("Unexpected response to QUIT");
			}
			closeCon();
        }
        else if (spl[0].equals("list")) {
            cr.encMsg(out, "LIST".getBytes());
			System.out.println(cr.decResponse(in));
        }
        else if (str.equals("key")) {
            out.write("RKEY".getBytes());
			out.flush();
			final String command = Utility.readCommand(in);
			if (command.equals("SKEY")) {
				int len=(int)Utility.readLong(in);
				byte[] pubKeyBuf = Utility.readBytes(in,len);
				System.out.println("key length: " + len);
				System.out.println("key (hex): " + Utility.hexPrint(pubKeyBuf));
				pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKeyBuf));
			}
        }
        else if (str.equals("init")) {
            // byte []key = Crypto.genkey(random);
            key = Crypto.genkey(random);
			cr = new Crypto(key, null);
            System.out.println(Utility.hexPrint(key)+" is the key.");
            byte[] ek=encryptRSA(key, pubKey);
            long len=ek.length;
            System.out.println(len);
            // toSend="INIT"+ek.length;//our code encrypted by RSA with server's public code
            out.write(("INIT").getBytes());
            out.write(Utility.long2byteBE(len));
            out.write(ek);
            out.flush();
            //System.out.print(encryptRSA(key, Server.pubKey).length);
            System.out.println("init sent");
			ByteArrayInputStream dec = cr.decResponse(in,true);
			final String decmd = Utility.readCommand(dec);
			if (decmd.equals("CONF")) {
				System.out.println("Session initialized");
			} else {
				System.err.println("INIT unexpected response " + decmd);
			}
		} else if (spl[0].equals("upld")) {
            String fileName=spl[1];
            byte[] file=Utility.getFile("client-files/"+fileName);
            if (file==null) {
                return;
            }
			System.out.println("Uploading " + spl[1]);
            cr.encMsg(out, new byte[][]{
					"UPLD".getBytes()
					,Utility.long2byteBE(fileName.getBytes().length)
					,fileName.getBytes()
					,Utility.long2byteBE(file.length)
					,file
				});

			ByteArrayInputStream dec = cr.decResponse(in);
			final String decmd = Utility.readCommand(dec);
			if (decmd.equals("SUCC")) {
				System.out.println("Upload successful");
			} else if (decmd.equals("FAIL")) {
				System.out.println("UPLD FAILURE");
			} else {
				System.err.println("UPLD unexpected response " + decmd);
			}
        } else if (spl[0].equals("dwnl")) {
            String fileName=spl[1];
			cr.encMsg(out, new byte[][]{"DWNL".getBytes(),fileName.getBytes()});
			// cr=new Crypto(key, new Counter(0));

			ByteArrayInputStream dec = cr.decResponse(in);
			final String decmd = Utility.readCommand(dec);
			if (decmd.equals("DATA")) {
				FileOutputStream fos=new FileOutputStream("client-files/"+fileName);
				fos.write(Utility.readBytes(dec,dec.available()));
				fos.close();
				System.out.println("Download successful (now verify the file)");
			} else if (decmd.equals("FAIL")) {
				System.out.println("DWNL FAILURE");
			} else {
				System.err.println("DWNL unexpected response " + decmd);
			}
		} else if (spl[0].equals("chalpart")) {
			String fileName=spl[1];
			File file = new File("client-files/"+fileName);
			long offset = random.nextInt((int)file.length());
			long len = random.nextInt((int)(file.length()-offset));
			challengeServer(fileName,offset,len);
		} else if (spl[0].equals("chal")) {
			String fileName=spl[1];
			File file = new File("client-files/"+fileName);
			challengeServer(fileName,0,file.length());
        } else if (spl[0].equals("stat")) {
            String fileName=spl[1];
			cr.encMsg(out, new byte[][]{"STAT".getBytes(),fileName.getBytes()});
			// cr=new Crypto(key, new Counter(0));

			ByteArrayInputStream dec = cr.decResponse(in);
			final String decmd = Utility.readCommand(dec);
			if (decmd.equals("STTR")) {
				System.out.println("File length: " + Utility.readLong(dec));
				final byte []hash = Utility.readBytes(dec,dec.available());
				System.out.println("File hash: " + Utility.hexPrint(hash));
				// FileOutputStream fos=new FileOutputStream("client-files/"+fileName);
				// fos.write(Utility.readBytes(dec,dec.available()));
				// fos.close();
				// System.out.println("Download successful (now verify the file)");
			} else if (decmd.equals("FAIL")) {
				System.out.println("STAT FAILURE");
			} else {
				System.err.println("STAT unexpected response " + decmd);
			}
        } else {
            //invalid, ignore.
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
    public static void main(String[] args) throws Exception {
		new Client(args.length<2 ? "127.0.0.1" : args[0]).Loop();
    }
}
