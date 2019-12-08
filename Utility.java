import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utility {
	final public static byte [] long2byteBE(long l) {
		byte []out = new byte[Long.BYTES];
		for (int i = 0; i < Long.BYTES; i++) {
			out[i] = (byte)(l >>> ((7 - i) << 3) & 0xFF);
		}
		return out;
	}

	final public static long byte2long(byte[] bytes) {
		long result = 0;
		for (byte b : bytes) {
			result = result << 8 | (b & 0xFF);
		}
		return result;
	}

	final public static String hexPrint(byte[] bytes) {
		String result = "";
		for (byte b : bytes) {
			result += String.format("%02x",b);
		}
		return result;
	}
        
        public static byte[] getFile(String fileName) {
            File f=new File(fileName);
            FileInputStream fis;
            try {
                fis = new FileInputStream(f);
                byte[] b=new byte[(int)f.length()];
                fis.read(b);
                fis.close();
                return b;
            } catch (Exception ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
        
        public static boolean saveFile(String fileName, byte[] file) {
            File f=new File(fileName);
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(f);
                fos.write(file);
                fos.close();
                return true;
            } catch (Exception ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        
	// final public static byte[] readBytes (InputStream in, int len) throws IOException {
	// 	byte[] b=new byte[len];
	// 	in.read(b, 0, len);
	// 	return b;
	// }

	// A more aggressive function
	final public static byte[] readBytes (InputStream in, int len) throws Exception {
		int off = 0;
		byte[] b=new byte[len];
		while (off < len) {
			Thread.sleep(10);
			off += in.read(b, off, len-off);
		}
		return b;
	}

	final public static String readCommand(InputStream in) throws Exception {
		return new String(readBytes(in,4));
	}

	final public static long readLong(InputStream in) throws Exception {
		return byte2long(readBytes(in,8));
	}

	final public static byte[] combBytes(byte [][]bytes) {
		// int len = 0;
		// for (byte[] b : bytes) {
		// 	len += b.length;
		// }
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		try {
			for (byte[] b : bytes) o.write(b);
		} catch (IOException e) {
			System.err.println("Impossible IO exception in combBytes" + e);
			System.exit(-1);
		}
		return o.toByteArray();
	}

	final public static void main(String[] args) {
		byte []x = new byte[256];
		new Random().nextBytes(x);
		System.out.println(hexPrint(x));
	}
}
