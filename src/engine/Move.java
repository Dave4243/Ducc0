package engine;

public class Move {
	private byte fromSquare;
	private byte toSquare;
	private byte promotionPiece;
	private byte castlingFlag;
	
	private Piece capturedPiece;
	
	private boolean isEnPassant;
	
	public Move() {
		this.fromSquare = 0;
		this.toSquare = 0;
		this.promotionPiece = Piece.NULL;
	}

	public Move(int from, int to) {
		this.fromSquare     = (byte)from;
		this.toSquare       = (byte)to;
		this.promotionPiece = Piece.NULL;
	}
	
	public Move(int from, int to, int pp) {
		this.fromSquare     = (byte)from;
		this.toSquare       = (byte)to;
		this.promotionPiece = (byte)pp;
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
	
	public void setPromotionPiece(int p) {
		this.promotionPiece = (byte)p;
	}
	
	public void setCapturedPiece(Piece p) {
		this.capturedPiece = p;
	}
	
	public Piece getCapturedPiece() {
		return capturedPiece;
	}
	
	public void setCastlingFlag(int f) {
		this.castlingFlag = (byte)f;
	}
	
	public int getCastlingFlag() {
		return castlingFlag;
	}

	public boolean isEnPassant() {
		return isEnPassant;
	}

	public void setEnPassant(boolean isEnPassant) {
		this.isEnPassant = isEnPassant;
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
			|| (this.promotionPiece != m.promotionPiece)
			|| (this.isEnPassant != m.isEnPassant)
			|| (this.castlingFlag != m.castlingFlag)){
			return false;
		}
		
		if (this.capturedPiece == null || m.capturedPiece == null) {
			return this.capturedPiece == m.capturedPiece;
		}
		
		return (this.capturedPiece.getType() == m.capturedPiece.getType())
				&& (this.capturedPiece.getColor() == m.capturedPiece.getColor());
	}
}