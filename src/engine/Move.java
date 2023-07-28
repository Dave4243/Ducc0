package engine;

public class Move {
	private int fromSquare;
	private int toSquare;
	private int promotionPiece;
	private Piece capturedPiece;
	
	// 0000 = none
	// 0001 = white kingside
	// 0010 = white queenside
	// 0100 = black kingside
	// 1000 = black queenside
	private int castlingFlag;
	// In Board.java, castling rights are represented as a bitwise or of the flags:
	// 1111 = all castling rights
	// 1011 = all castling rights EXCEPT for black kingside
	// etc.
	
	public Move() {
		this.fromSquare = 0;
		this.toSquare = 0;
		this.promotionPiece = Piece.NULL;
	}

	public Move(int from, int to) {
		this.fromSquare     = from;
		this.toSquare       = to;
		this.promotionPiece = Piece.NULL;
	}
	
	public Move(int from, int to, int pp) {
		this.fromSquare     = from;
		this.toSquare       = to;
		this.promotionPiece = pp;
	}
	
	public int getFromSquare() {
		return fromSquare;
	}
	
	public int getToSquare() {
		return toSquare;
	}
	
	public int getPromotionPiece() {
		return promotionPiece;
	}
	
	public void setCapturedPiece(Piece p) {
		this.capturedPiece = p;
	}
	
	public Piece getCapturedPiece() {
		return capturedPiece;
	}
	
	public void setCastlingFlag(int f) {
		this.castlingFlag = f;
	}
	
	public int getCastlingFlag() {
		return castlingFlag;
	}

	public String toString() {
		int fromRank = fromSquare >>> 3;
		int fromFile = fromSquare & 7;
		
		int toRank = toSquare >>> 3;
		int toFile = toSquare & 7;
		
		String promotion = "";
		
		switch (promotionPiece) {
		case 1:
			promotion = "n";
			break;
		case 2:
			promotion = "b";
			break;
		case 3:
			promotion = "r";
			break;
		case 4:
			promotion = "q";
			break;
		}
		return "" + (char)('a' + fromFile) + (fromRank +1)
				  + (char)('a' + toFile) + (toRank + 1) + promotion;
	}
	
	public boolean equals(Move m) {
		if (m == null) {
			return false;
		}
		
		if ((this.fromSquare != m.fromSquare)
			|| (this.toSquare != m.toSquare)
			|| (this.promotionPiece != m.promotionPiece)){
			return false;
		}
		
		if (this.capturedPiece == null || m.capturedPiece == null) {
			return this.capturedPiece == m.capturedPiece;
		}
		
		return (this.capturedPiece.getType() == m.capturedPiece.getType())
				&& (this.capturedPiece.getColor() == m.capturedPiece.getColor());
	}
}