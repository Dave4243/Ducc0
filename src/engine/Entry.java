package engine;

/**
 * @author Dave4243
 * The TranspositionEntry.java class represnts a transposition table entry.
 */
public class Entry {

	private long     zobristKey;
	private Move     bestMove;
	private int      evaluation;
	private byte     depth;
	public static byte UPPER = 2;
	public static byte LOWER = 1;
	public static byte EXACT = 0;
	private byte     type;
	
	/**
	 * @param key The zobrist key of the position
	 * @param m   The best move found in this position
	 * @param eval The evaluation of the position, may be a upper or lower bound
	 * @param depth The depth the position was searched to
	 * @param age The move number that the position occured at
	 * @param t The type of node stored (EXACT= PV, LOWER = cut node, UPPER = all node)
	 */
	public Entry(long key, Move m, int eval, int depth, byte t) {
		this.zobristKey = key;
		this.bestMove   = m;
		this.evaluation = eval;
		this.depth      = (byte) depth;
		this.type       = t;
	}
	
	public long getHash() {
		return zobristKey;
	}
	
	public Move getBestMove() {
		return bestMove;
	}
	
	public int getEvaluation() {
		return evaluation;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public byte getNodeType() {
		return type;
	}
	
	public String toString() {
		return "Key: " + zobristKey + '\n' + "Move: " + bestMove.toString()
		+ '\n' + "Evaluation: " + evaluation + '\n' + "Depth: " + depth
		+ '\n' + "Type: " + type;
	}
	
	public boolean equals(Entry e) {
		if (e == null) {
			return false;
		}
		if (e.depth == depth && e.evaluation == evaluation && e.type == type && e.zobristKey == zobristKey) {
			if (e.bestMove == null && bestMove == null)
				return true;
			if (bestMove != null) {
				boolean valid = bestMove.equals(e.bestMove);
				if (!valid) {
					System.out.println("move issue");
					System.out.println("Object array: " + e.getBestMove());
					System.out.println("En Passant: " + e.getBestMove().isEnPassant());
					System.out.println();
					System.out.println("Double array: " + bestMove);
					System.out.println("En Passant: " + bestMove.isEnPassant());
					System.out.println("======================================================");
				}
				return valid;
			}
		}
		System.out.println("Double array entry: " + toString());
		System.out.println();
		System.out.println("Entry object array: " + e.toString());
		System.out.println("================================================");
		return false;
	}
}
