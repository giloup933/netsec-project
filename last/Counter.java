package last;

public class Counter {
	long c;

	Counter(long l) {
		c = l;
	}

	final public static byte [] long2byteBE(long l) {
		byte []out = new byte[Long.BYTES];
		for (int i = 0; i < Long.BYTES; i++) {
			out[i] = (byte)(l >> (i << 3) & 0xFF);
		}
		return out;
	}

	final public long get() { return c; }
	final public void set(long l) { c = l; }
	final public void inc() { c += 1; }
	final public byte[] getBytes() { return long2byteBE(c); }
}
