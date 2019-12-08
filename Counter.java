public class Counter {
	long c;

	Counter(long l) {
		c = l;
	}
        
        Counter(byte[]b) {
                c = byte2long(b);
        }

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

	final public long get() { return c; }
	final public void set(long l) { c = l; }
	final public void inc() { c += 1; }
	final public byte[] getBytes() { return long2byteBE(c); }
        final public String getString() { return new String(getBytes()); };
}
