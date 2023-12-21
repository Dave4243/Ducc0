package engine;

public class BoardState {
	private int  halfmove;
	private long zobristKey;
	private int  castlingRights;
	private long enPassantBB;
    private Piece capturedPiece;
	
	public BoardState(int halfMove, long zobristKey, int castlingRights, long enPassant) {
		this.halfmove = halfMove;
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
	
	public Piece getCaptured() {
		return capturedPiece;
	}
	
	public void setCaptured(Piece p) {
		this.capturedPiece = p;
	}
	
	// "constructor" for already present BoardState
	// this is to save allocation of new BoardStates by using existing ones
	public void set(int halfMove, long zobristKey, int castlingRights, long enPassant) {
		this.halfmove = halfMove;
		this.zobristKey = zobristKey;
		this.castlingRights = castlingRights;
		this.enPassantBB = enPassant;
	}
}
