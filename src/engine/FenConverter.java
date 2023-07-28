package engine;

/**
 * @author Dave4243
 */
public class FenConverter {

	public static void convert(Board b, String fen) {
		
		String[] f = fen.split(" ");
		
		String placements = f[0];
		String color = f[1];
		String castling = f[2];
		String enPassantSquare = f[3];
		String halfmoves = "0";
		String fullmoves = "0";
		if (f.length >= 5)
			halfmoves = f[4];
		if (f.length >= 6)
			fullmoves = f[5];
		
		// set up board
		/*************************************************************************/
		int pos = 56;
		for (int i = 0; i < placements.length(); i++) {
			if (Character.isDigit(placements.charAt(i))) {
				pos += Integer.valueOf(placements.substring(i, i+1));
			}
			else if (placements.charAt(i) == '/') {
				pos -= 16;
			}
			else {
				int c = Character.isLowerCase(placements.charAt(i)) ? Piece.BLACK : Piece.WHITE;
				switch (Character.toLowerCase(placements.charAt(i))) {
				case 'p':
					b.bitBoards[c][Piece.PAWN]   |= (0x1L << pos);
					b.pieces[pos] = new Piece(c, Piece.PAWN);
					break;
				case 'n':
					b.bitBoards[c][Piece.KNIGHT] |= (0x1L << pos);
					b.pieces[pos] = new Piece(c, Piece.KNIGHT);
					break;
				case 'b':
					b.bitBoards[c][Piece.BISHOP] |= (0x1L << pos);
					b.pieces[pos] = new Piece(c, Piece.BISHOP);
					break;
				case 'r':
					b.bitBoards[c][Piece.ROOK]   |= (0x1L << pos);
					b.pieces[pos] = new Piece(c, Piece.ROOK);
					break;
				case 'q':
					b.bitBoards[c][Piece.QUEEN]  |= (0x1L << pos);
					b.pieces[pos] = new Piece(c, Piece.QUEEN);
					break;
				case 'k':
					b.bitBoards[c][Piece.KING]   |= (0x1L << pos);
					b.pieces[pos] = new Piece(c, Piece.KING);
					break;
				}
				pos++;
			}
		}
		/*************************************************************************/

		
		
		// side to move
		/*************************************************************************/
		if (color.charAt(0) == 'w')
			b.setSideToMove(Piece.WHITE);
		else
			b.setSideToMove(Piece.BLACK);
		
		/*************************************************************************/
		
		
		
		// castling
		/*************************************************************************/
		int castlingRights = 0b1111;
		if (castling.charAt(0) == '-') {
			b.setCastlingRights(0);
		}
		else {
			if (castling.indexOf("K") == -1) {
				castlingRights &= 0b1110;
			}
		
			if (castling.indexOf("Q") == -1) {
				castlingRights &= 0b1101;
			}
			
			if (castling.indexOf("k") == -1) {
				castlingRights &= 0b1011;
			}
			
			if (castling.indexOf("q") == -1) {
				castlingRights &= 0b0111;
			}
				
			b.setCastlingRights(castlingRights);
		}
		
		/*************************************************************************/
		
		
		
		// en passant square
		/*************************************************************************/
		if (!enPassantSquare.equals("-")){
			int col = enPassantSquare.charAt(0) - 'a';
			int row = Integer.valueOf(enPassantSquare.substring(1,2))-1;
			
			int index = col + row * 8;
			
			long enPassant = 0x1L << index;
			b.setEnPassantTarget(enPassant);
		}
		
		/*************************************************************************/
		
		
		
		// halfmove clock
		/*************************************************************************/
		b.setHalfMoveClock(Integer.valueOf(halfmoves));
		
		/*************************************************************************/
		
		b.setZobristKey(Zobrist.getKey(b));
	}
}
