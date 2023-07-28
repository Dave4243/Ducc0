package engine;

public class Piece {
	public static final int WHITE  = 0;
	public static final int BLACK  = 1;
	
	public static final int NULL   = -1;
	public static final int PAWN   = 0;
	public static final int KNIGHT = 1;
	public static final int BISHOP = 2;
	public static final int ROOK   = 3;
	public static final int QUEEN  = 4;
	public static final int KING   = 5;
	
	private int color;
	private int type;
	
	public Piece(int color, int type) {
		this.color = color;
		this.type  = type;
	}
	
	public int getColor() {
		return color;
	}
	
	public int getType() {
		return type;
	}
}
