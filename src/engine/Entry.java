package engine;

import engine.TranspositionTable.NodeType;

/**
 * @author Dave4243
 * The TranspositionEntry.java class represnts a transposition table entry.
 */
public class Entry {

	private long     zobristKey;
	private Move     bestMove;
	private int      evaluation;
	private int      depth;
	private int      age;
	private NodeType type;
	
	/**
	 * @param key The zobrist key of the position
	 * @param m   The best move found in this position
	 * @param eval The evaluation of the position, may be a upper or lower bound
	 * @param depth The depth the position was searched to
	 * @param age The move number that the position occured at
	 * @param t The type of node stored (EXACT= PV, LOWER = cut node, UPPER = all node)
	 */
	public Entry(long key, Move m, int eval, int depth, int age, NodeType t) {
		this.zobristKey = key;
		this.bestMove   = m;
		this.evaluation = eval;
		this.depth      = depth;
		this.age        = age;
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
	
	public int getAge() {
		return age;
	}
	
	public NodeType getNodeType() {
		return type;
	}
}
