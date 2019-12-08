public class Counter {
	long c;

	Counter(long l) {
		c = l;
	}

	Counter(byte[]b) {
		c = Utility.byte2long(b);
	}

	final public long get() { return c; }
	final public void set(long l) { c = l; }
	final public void inc() { c += 1; }
	final public byte[] getBytes() { return Utility.long2byteBE(c); }
	final public String getString() { return new String(getBytes()); };
}
