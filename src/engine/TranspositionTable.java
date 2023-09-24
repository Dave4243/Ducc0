package engine;

public class TranspositionTable {
	
	public enum NodeType {
		EXACT, UPPER, LOWER;
	}
	
	private static final int TABLE_SIZE = 1000000;
	
	private Entry[] table;
	
	public TranspositionTable() {
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
	public void store(long zobristKey, Move bestMove, int eval, int depth, int age, NodeType type) {
		int index = getIndex(zobristKey);
		Entry newEntry = new Entry(zobristKey, bestMove, eval, depth, age, type);
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
	
	private int getIndex(long key) {
		return (int) (Math.abs(key) % TABLE_SIZE);
	}
}
