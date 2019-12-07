package last;

public class Counter {
	long c;

	Counter(long l) {
		c = l;
	}
        
        Counter(byte[]b) {
                c = byte2long(b);
        }

	final public static byte []long2byteBE(long l) {
		byte []out = new byte[Long.BYTES];
		for (int i = 0; i < Long.BYTES; i++) {
			out[i] = (byte)(l >> (((Long.BYTES - 1) - i) << 3) & 0xFF);
		}
		return out;
	}

	final public static long byte2long(byte[] bytes) {
		long result = 0;
		for (byte b : bytes) {
			result = result << 8 | b;
		}
		return result;
	}

	final public static int byte2int(byte[] bytes) {return (int)byte2long(bytes); }

	final public long get() { return c; }
	final public void set(long l) { c = l; }
	final public void inc() { c += 1; }
	final public byte[] getBytes() { return long2byteBE(c); }
	final public String getString() { return new String(getBytes()); }
	final private boolean lt(long l) {
		return (c > ~0xFFFF && l <= 0xFFFF) ||
			(!(l > ~0xFFFF && c <= 0xFFFF) && (Long.compareUnsigned(l,c) < 0));
	}
	final public void setMax(long l) { if (lt(l)) c = l; }
	final public void setMax(byte[] b) { setMax(byte2long(b)); }
}
