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
        
        final public static byte[] readBytes (InputStream in, int len) throws IOException {
            byte[] b=new byte[8];
            in.read(b, 0, len);
            return b;
        }

	final public static void main(String[] args) {
		byte []x = new byte[256];
		new Random().nextBytes(x);
		System.out.println(hexPrint(x));
	}
        
}
