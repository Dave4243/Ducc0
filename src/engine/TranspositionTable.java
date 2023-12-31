package engine;

public class TranspositionTable {
	
	private static int TABLE_SIZE = 1048576;
	
	private long[] keys;
	private long[] table;
	
	public TranspositionTable() {
		this.setSize(UCI.ttSize);
		keys = new long[TABLE_SIZE];
		table = new long[TABLE_SIZE];
	}

	public void store(long zobristKey, int bestMove, int eval, int depth, byte type) {
		int index = getIndex(zobristKey);
		
		bestMove |= (depth << 24) & 0x3f000000L;
		bestMove |= ((int)type << 30);
		long entry = (bestMove & 0xffffffffL) | ((long)eval << 32);
		
		table[index] = entry;
		keys[index] = zobristKey;
	}
	
	public boolean lookup(Entry e, long zobristKey) {
		int index = getIndex(zobristKey);
		if (keys[index] == zobristKey) {
			long data = table[index];
            e.setHash(keys[index]);
			e.setEvaluation((int)(data >>> 32));
			e.setDepth((int)(data & 0x3f000000L) >>> 24);
			e.setNodeType((byte)((data & 0xc0000000L) >>> 30));
			e.setBestMove((int)(data & 0xffffffL));
            return true;
		}
		return false;
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
