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
