package engine;

public class TranspositionTable {
	
	public enum NodeType {
		EXACT, UPPER, LOWER;
	}
	
	private static int TABLE_SIZE = 262144; // 16 mb hash
	
	private Entry[] table;
	
	public TranspositionTable(int size) {
		this.setSize(size);
		this.table = new Entry[TABLE_SIZE];
	}
	
	/**
	 * @param zobristKey
	 * @param eval
	 * @param depth
	 * @param age
	 * @param type
	 * @return True if the new entry was stored in the table, false if it wasn't
	 */
	public void store(long zobristKey, Move bestMove, int eval, int depth, NodeType type) {
		int index = getIndex(zobristKey);
		Entry newEntry = new Entry(zobristKey, bestMove, eval, depth, type);
		table[index] = newEntry;
	}
	
	public Entry lookup(long zobristKey) {
		int index = getIndex(zobristKey);
		Entry entry = table[index];
		if (entry == null)
			return null;
		if (entry.getHash() != zobristKey)
			return null;
		return entry;
	}
	
	public boolean contains(long zobristKey) {
		if (table[getIndex(zobristKey)] == null)
			return false;
		if (table[getIndex(zobristKey)].getHash() == zobristKey) 
			return true;
		return false;
	}
	
	public void clear() {
		table = new Entry[TABLE_SIZE];
	}
	
	public int getSize() {
		return TABLE_SIZE;
	}
	
	public void setSize(int hashMB) {
		TABLE_SIZE = (1048576*hashMB)/64; 
	}
	
	private int getIndex(long key) {
		return (int) (Math.abs(key) % TABLE_SIZE);
	}
}
