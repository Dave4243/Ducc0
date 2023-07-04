package chess;

/**
 * @author Dave4243
 * Contains a method that converts a fen string to a Board.java
 */
public class FenConverter {

	// converts a board that is empty into specified board state from fen
	// should probably split this method up 
	public static Board convert(String fen)
	{
		Board b = new Board(new Board().createEmptyBoard());
		
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
		int rowIndex = 0;
		int colIndex = 0;
		for (int i = 0; i < placements.length(); i++)
		{
			if (Character.isDigit(placements.charAt(i)))
			{
				colIndex += Integer.valueOf(placements.substring(i,i+1));
			}
			else if (placements.charAt(i) == '/')
			{
				colIndex = 0;
				rowIndex++;
			}
			else
			{
				char piece = placements.charAt(i);
				if (piece == 'p')
					b.setPiece(new Pawn(Piece.BLACK), rowIndex, colIndex);
				if (piece == 'P')
					b.setPiece(new Pawn(Piece.WHITE), rowIndex, colIndex);
				
				if (piece == 'r')
					b.setPiece(new Rook(Piece.BLACK), rowIndex, colIndex);
				if (piece == 'R')
					b.setPiece(new Rook(Piece.WHITE), rowIndex, colIndex);
				
				if (piece == 'b')
					b.setPiece(new Bishop(Piece.BLACK), rowIndex, colIndex);
				if (piece == 'B')
					b.setPiece(new Bishop(Piece.WHITE), rowIndex, colIndex);
				
				if (piece == 'n')
					b.setPiece(new Knight(Piece.BLACK), rowIndex, colIndex);
				if (piece == 'N')
					b.setPiece(new Knight(Piece.WHITE), rowIndex, colIndex);
				
				if (piece == 'q')
					b.setPiece(new Queen(Piece.BLACK), rowIndex, colIndex);
				if (piece == 'Q')
					b.setPiece(new Queen(Piece.WHITE), rowIndex, colIndex);
				
				if (piece == 'k') {
					b.setPiece(new King(Piece.BLACK), rowIndex, colIndex);
					b.setBlackKingPosition(new int[] {rowIndex, colIndex});
				}
				if (piece == 'K') {
					b.setPiece(new King(Piece.WHITE), rowIndex, colIndex);
					b.setWhiteKingPosition(new int[] {rowIndex, colIndex});
				}
				
				colIndex++;
			}
			if (placements.charAt(i) == ' ')
			{
				break;
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
		if (castling.charAt(0) == '-') {
			b.setCastlingRights(0, false);
			b.setCastlingRights(1, false);
			b.setCastlingRights(2, false);
			b.setCastlingRights(3, false);
		}
		else if (castling.indexOf("K") == -1) 
			b.setCastlingRights(0, false);
		
		else if (castling.indexOf("Q") == -1) 
			b.setCastlingRights(1, false);
		
		else if (castling.indexOf("k") == -1) 
			b.setCastlingRights(2, false);
		
		else if (castling.indexOf("q") == -1) 
			b.setCastlingRights(3, false);
		/*************************************************************************/
		
		
		
		// en passant square
		/*************************************************************************/
		Move lastMove = null;
		if (enPassantSquare.charAt(0) != '-') {
			int col = enPassantSquare.charAt(0) - 'a';
			int row = 8 - enPassantSquare.charAt(1);
			// direction -1 means go up the board (white moved)
			int direction = enPassantSquare.charAt(1) == '3' ? -1 : 1;
			Piece p = direction == -1 ? new Pawn(Piece.WHITE) : new Pawn(Piece.BLACK);
			int fromRow = row - direction;
			int fromCol = col;
			
			int toRow = row + direction;
			int toCol = col;
			
			lastMove = new Move(p, fromRow, fromCol, toRow, toCol);
		}
		/*************************************************************************/
		
		
		
		// halfmove clock
		/*************************************************************************/
		int hmc = 0;
		if (halfmoves.indexOf("-") != -1) {
			b.setHalfMoveClock(Integer.valueOf(halfmoves));
		}
		b.addPastState(new BoardState(lastMove, null, hmc, 0));
		/*************************************************************************/
		
		
		
		// full moves
		/*************************************************************************/
		b.setMoveNumber(Integer.valueOf(fullmoves));
		/*************************************************************************/
		
		return b;
	}
}
