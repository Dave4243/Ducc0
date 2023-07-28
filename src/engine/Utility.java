package engine;

public class Utility {
	public static int getZobristIndex(Piece p, int index) {
		if (p == null)
			return 0;
		return index * 12 + p.getType() * 2 + p.getColor();
	}
	
	public static char convertPiece(Piece p) {
		int type = p.getType();
		boolean uppercase = p.getColor() == Piece.WHITE ? true : false;
		char result = ' ';
		switch (type) {
		case 0:
			result = 'p';
			break;
		case 1:
			result = 'n';
			break;
		case 2:
			result = 'b';
			break;
		case 3:
			result = 'r';
			break;
		case 4:
			result = 'q';
			break;
		case 5:
			result = 'k';
			break;
		}
		
		if (uppercase)
			result = Character.toUpperCase(result);
		return result;
	}
	
	private Utility() {};
}
