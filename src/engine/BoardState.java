package engine;

public class BoardState {
	private int  halfmove;
	private long zobristKey;
	private int  castlingRights;
	private long enPassantBB;
	
	public BoardState(int halfmove, long zobristKey, int castlingRights, long enPassant) {
		this.halfmove = halfmove;
		this.zobristKey = zobristKey;
		this.castlingRights = castlingRights;
		this.enPassantBB = enPassant;
	}
	
	public int getHalfmoves() {
		return halfmove;
	}
	
	public long getZobrist() {
		return zobristKey;
	}
	
	public int getCastlingRights() {
		return castlingRights;
	}
	
	public long getEnPassantBB() {
		return enPassantBB;
	}
	
}
