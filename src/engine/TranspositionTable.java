package engine;

public class TranspositionTable {
	
	private static int TABLE_SIZE = 1048576;
	
	private long[] keys;
	private long[] table;
	
//	private Entry[] entries;
	
	public TranspositionTable() {
		this.setSize(UCI.ttSize);
		keys = new long[TABLE_SIZE];
		table = new long[TABLE_SIZE];
		
//		entries = new Entry[TABLE_SIZE];
	}
	
	/**
	 * @param zobristKey
	 * @param eval
	 * @param depth
	 * @param type
	 * @return True if the new entry was stored in the table, false if it wasn't
	 */
	public void store(long zobristKey, Move bestMove, int eval, int depth, byte type) {
		int index = getIndex(zobristKey);
		
		int move = bestMove.toInt();
		move |= (depth << 24) & 0x3f000000L;
		move |= ((int)type << 30);
		long entry = (move & 0xffffffffL) | ((long)eval << 32);
		
		table[index] = entry;
		keys[index] = zobristKey;
		
//		entries[index] = new Entry(zobristKey, bestMove, eval, depth, type);
	}
	
	public Entry lookup(long zobristKey) {
		int index = getIndex(zobristKey);
		Entry entry = null;
		if (keys[index] == zobristKey) {
			long data = table[index];
			int eval = (int)(data >>> 32);
			int depth = (int)(data & 0x3f000000L) >>> 24;
			byte type = (byte)((data & 0xc0000000L) >>> 30);
			Move m = Move.convert((int)(data & 0xffffffffL));
			entry = new Entry(keys[index], m, eval, depth, type);
			
//			if (!entry.equals(entries[index])) System.out.println("TRANSPOSITION ERROR");
		}
		return entry;
	}
	
	public boolean contains(long zobristKey) {
		if (keys[getIndex(zobristKey)] == zobristKey) 
			return true;
		return false;
	}
	
	public void clear() {
		keys = new long[TABLE_SIZE];
		table = new long[TABLE_SIZE];
	}
	
	public int getSize() {
		return TABLE_SIZE;
	}
	
	public void setSize(int hashMB) {
		TABLE_SIZE = (1048576*hashMB)/16; 
	}
	
	private int getIndex(long key) {
		return (int) (Math.abs(key) % TABLE_SIZE);
	}
}
