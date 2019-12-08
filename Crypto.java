
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gil
 */
public class Crypto {
    final public static int KEYLEN = 32;
    final public static int OTPLEN = 32;
    final public static String ALGORITHM = "SHA-256";

	final private byte []key;
	private Counter ctr;

	// Crypto(long k, Counter c) {
	// 	key = Counter.long2byteBE(k);
	// 	ctr = c;
	// }

	Crypto(byte []k, Counter c) {
		key = k;
		ctr = c;
	}

	final public static MessageDigest digestInstance() {
		MessageDigest d = null;
		try {
			d = MessageDigest.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("SHA encryption not supported by Java? " + e);
			System.exit(-1);
		}
		return d;
	}

	final public static byte[] genkey(Random r) {
		final byte[] k = new byte[KEYLEN];
		r.nextBytes(k);
		return k;
	}

	final public static byte[] digest(byte []x) {
		return digestInstance().digest(x);
	}

	final private byte[] consumeOTP() {
		final MessageDigest d = digestInstance();
		d.update(key);
		d.update(ctr.getBytes());
		ctr.inc();
		return d.digest();
	}
        
        final public void encMsg(OutputStream out, byte[] msg) {
            try {
                byte[] c=ctr.getBytes();
                byte[] b=crypt(msg);
                long len=b.length;
                out.write("ENCR".getBytes());
                out.write(c);
                out.write(Counter.long2byteBE(len));
                out.write(b);
                out.flush();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

	final public void encMsg(OutputStream out, byte[][] msg) {
		encMsg(out,Utility.combBytes(msg));
	}

        final public byte[] decMsg(InputStream in) {
            long msgCtr, len;
            try {
                msgCtr = Counter.byte2long(Utility.readBytes(in, 8));
                ctr=new Counter(msgCtr);
                len = Counter.byte2long(Utility.readBytes(in, 8));
                System.out.println((int)len);
                return crypt(Utility.readBytes(in, (int)len));
            } catch (IOException ex) {
                System.out.println("decMsg failure");
            }
            return null;
        }

	final public byte[] crypt(byte []in) {
		byte []pad = null;
		int padoff = 0;
		final byte []out = new byte[in.length];
		for (int i = 0; i < in.length; i++) {
			if (padoff == 0) {
				pad = consumeOTP();
			}
			out[i] = (byte)(in[i] ^ pad[padoff]);
			padoff = (padoff + 1) % pad.length;
		}
		return out;
	}

    // final byte[] encryptFile(File file) throws IOException {
    //     return crypt(Files.readAllBytes(file.toPath()));
    // }
    // final File decryptFile(byte[] cipher, String path, String filename) {
    //     return bytes2File(crypt(cipher), path, filename);
    // }
    // final void bytes2File(byte[] plain, String path, String filename) {
    //     // File file=new File(path+filename);
	// 	Files.write(Paths.get(path,filename),plain);
    //     //NOT IMPLEMENTED
    //     // return file;
    // }
    // final public byte[] encdecFile(String path) throws FileNotFoundException, IOException
    // {
    //     File f=new File(path);
    //     FileInputStream fis;
    //     byte[] b;
    //     if (f.exists())
    //     {
    //         fis=new FileInputStream(f);
    //         b=new byte[(int)f.length()];
    //         fis.read(b);
    //         fis.close();
    //         return crypt(b);
    //     }
    //     return new byte[0];
    // }

        final public static byte[] challenge(File file, int offset, int len, byte []salt) throws IOException {
		byte []b = new byte[len];
		MessageDigest i = digestInstance();
		i.update(salt);
		RandomAccessFile s = new RandomAccessFile(file,"r");
		s.seek(offset);
		s.read(b);
		s.close();
		return i.digest(b);
	}

	public static void main(String[] args) {
		Random r = new Random();
		byte []k = genkey(r);
		long initct = r.nextLong();
		Crypto enc = new Crypto(k,new Counter(initct));
		Crypto dec = new Crypto(k,new Counter(initct));

		// if (args.length == 0) {
		// 	System.out.println("Crypto debug test program: try ");
		// }

		int len = r.nextInt(5000);
		System.out.println("Crypto test");
		System.out.println("Testing with " + len + " random bytes");
		byte []randFile = new byte[len];
		r.nextBytes(randFile);
		System.out.println("Generated file is: " + new String(randFile));
		byte []encrypted = enc.crypt(randFile);
		System.out.println("Encrypted file is: " + new String(encrypted));
		byte []decrypted = dec.crypt(encrypted);
		System.out.println("Decrypted file is: " + new String(decrypted));

		if (decrypted.length != randFile.length) {
			System.out.println("Files are not the same length");
		}

		for (int i = 0; i < randFile.length; i++) {
			if (decrypted[i] != randFile[i]) {
				System.out.println("Byte " + i + " is different");
			}
		}
	}
}
