package chess;

/**
 * @author Dave4243
 * The TranspositionTable.java class represents a hash table
 * for chess position lookup so that the program does not need to
 * reevaluate certain positions.
 */
public class TranspositionTable {
	
	public enum NodeType {
		EXACT, UPPER, LOWER;
	}
	
	private static final int TABLE_SIZE = 1000000;
	private TranspositionEntry[] table;
	
	public TranspositionTable() {
		this.table = new TranspositionEntry[TABLE_SIZE];
	}
	
	/**
	 * @param zobristKey
	 * @param eval
	 * @param depth
	 * @param age
	 * @param type
	 * @return True if the new entry was stored in the table, false if it wasn't
	 */
	public boolean store(long zobristKey, Move bestMove, int eval, int depth, int age, NodeType type) {
		int index = getIndex(zobristKey);
		TranspositionEntry e = table[index];
		TranspositionEntry newEntry = new TranspositionEntry(zobristKey, bestMove, eval, depth, age, type);
		if (e == null) {
			table[index] = newEntry;
			return true;
		}
		
		// if same position (keys are the same)
		// then compare depths to decide re-store
		if (e.getHash() == zobristKey) {
			if (e.getDepth() < depth) {
				// never replace a EXACT eval with a bound
				if (type != NodeType.EXACT && e.getNodeType() == NodeType.EXACT)
					return false;
				table[index] = newEntry;
				return true;
			}
			return false;
		}
		
		// if NOT same position
		else {
			// if age is close (within 2 moves) then compare depths
			if (age - e.getAge() <= 2) {
				if (e.getDepth() < depth) {
					// never replace a EXACT eval with a bound
					if (type != NodeType.EXACT && e.getNodeType() == NodeType.EXACT)
						return false;
					table[index] = newEntry;
					return true;
				}
				return false;
			}
			// if age is not close then just replace
			table[index] = newEntry;
		}
		return true;
	}
	
	public TranspositionEntry lookup(long zobristKey) {
		int index = getIndex(zobristKey);
		TranspositionEntry entry = table[index];
		if (entry == null)
			return null;
		if (entry.getHash() != zobristKey)
			return null;
		return entry;
	}
	
	public boolean contains(long zobristKey) {
		if (table[getIndex(zobristKey)] == null)
			return false;
		if (table[getIndex(zobristKey)].getHash() != zobristKey) 
			return false;
		return true;
	}
	
	public static int getSize() {
		return TABLE_SIZE;
	}
	
	private int getIndex(long key) {
		return (int) (Math.abs(key) % TABLE_SIZE);
	}
}