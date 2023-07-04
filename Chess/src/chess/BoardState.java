package chess;

import java.util.Arrays;

/**
 * @author Dave4243
 * The BoardState.java class contains the extra information
 * necessary to recreate a previous board state.
 */
public class BoardState {
	private Move      move;
	private boolean[] castlingRights;
	private int       halfmoveClock;
	private long      previousPosition;
	
	public BoardState(Move m, boolean[] cr, int hmc, long pp) {
		this.move             = m;
		this.castlingRights   = cr;
		this.halfmoveClock    = hmc;
		this.previousPosition = pp;
	}
	
	public Move getMove() {
		return move;
	}
	
	public boolean[] getCastlingRights() {
		return castlingRights;
	}
	
	public int getHalfmoveClock() {
		return halfmoveClock;
	}
	
	public long getPreviousPosition() {
		return previousPosition;
	}
	
	public String toString() {
		return "Move: " + move + '\n'
			+ "Castilng Rights: " + Arrays.toString(castlingRights) + '\n' 
			+ "Halfmove clock: " + halfmoveClock + '\n' 
			+ "Previous Hash: "  + previousPosition;
	}
}
