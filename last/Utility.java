package last;
import java.util.Random;

public class Utility {
	final public static String hexPrint(byte[] bytes) {
		String result = "";
		for (byte b : bytes) {
			result += String.format("%x",b);
		}
		return result;
	}

	final public static void main(String[] args) {
		byte []x = new byte[256];
		new Random().nextBytes(x);
		System.out.println(hexPrint(x));
	}
}
