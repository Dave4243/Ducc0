package engine;

/**
 * @author Dave4243
 * The TranspositionEntry.java class represnts a transposition table entry.
 */
public class Entry {
	
	public static byte UPPER = 2;
	public static byte LOWER = 1;
	public static byte EXACT = 0;
	
	private long     zobristKey;
	private int      bestMove;
	private int      evaluation;
	private byte     depth;
	private byte     type;
	
	/**
	 * @param key The zobrist key of the position
	 * @param m   The best move found in this position
	 * @param eval The evaluation of the position, may be a upper or lower bound
	 * @param depth The depth the position was searched to
	 * @param age The move number that the position occured at
	 * @param t The type of node stored (EXACT= PV, LOWER = cut node, UPPER = all node)
	 */
	public Entry(long key, int move, int eval, int depth, byte t) {
		this.zobristKey = key;
		this.bestMove   = move;
		this.evaluation = eval;
		this.depth      = (byte) depth;
		this.type       = t;
	}
	
	public long getHash() {
		return zobristKey;
	}
    
    public void setHash(long h){
        this.zobristKey = h;
    }
	
	public int getBestMove() {
		return bestMove;
	}
    
    public void setBestMove(int move){
        this.bestMove = move;
    }
	
	public int getEvaluation() {
		return evaluation;
	}
    
    public void setEvaluation(int eval){
        this.evaluation = eval;
    }
	
	public int getDepth() {
		return depth;
	}
    
    public void setDepth(int newDepth) {
        this.depth = (byte)newDepth;
    }
	
	public byte getNodeType() {
		return type;
	}
    
    public void setNodeType (byte nodeType) {
        this.type = nodeType;
    }
	
	public String toString() {
		return "Key: " + zobristKey + '\n' + "Move: " + Move.toString(bestMove)
		+ '\n' + "Evaluation: " + evaluation + '\n' + "Depth: " + depth
		+ '\n' + "Type: " + type;
	}
	
	public boolean equals(Entry e) {
		if (e == null) {
			return false;
		}
		if (e.depth == depth && e.evaluation == evaluation && e.type == type && e.zobristKey == zobristKey) {
			boolean valid = bestMove == e.bestMove;
			if (!valid) {
				System.out.println("move issue");
				System.out.println("Object array: " + e.getBestMove());
				System.out.println("En Passant: " + Move.getEnPassant(e.getBestMove()));
				System.out.println();
				System.out.println("Double array: " + bestMove);
				System.out.println("En Passant: " + Move.getEnPassant(bestMove));
				System.out.println("======================================================");
			}
			return valid;
		}
		System.out.println("Double array entry: " + toString());
		System.out.println();
		System.out.println("Entry object array: " + e.toString());
		System.out.println("================================================");
		return false;
	}
}
