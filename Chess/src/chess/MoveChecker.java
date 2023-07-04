package chess;

/**
 * @author Dave4243
 * The MoveChecker.java class verifies moves
 */
public class MoveChecker {
	// returns true if m can be made regardless of checks
	// returns false if m can not be made regardless of checks
	public boolean checkMove(Board b, Move m) {
		Piece pieceToMove = m.getPiece();
		Piece pieceToCapture = b.getPiece(m.getDestinationRow(), m.getDestinationFile());
		m.setCapturedPiece(pieceToCapture);

		if (pieceToMove.canMove(m) == false) 
			return false;
		
		if (pieceToCapture != null 
				&& pieceToMove.getColor() == b.getPiece(m.getDestinationRow(), m.getDestinationFile()).getColor())
			return false;
		
		if (hasBlockingPiece(b, m))
			return false;

		if (pieceToMove instanceof King && Math.abs(m.getDestinationFile()-m.getOriginalFile()) > 1)
		{
			return canCastle(pieceToMove.getColor(), m, b);
		}
		if (pieceToMove instanceof Pawn == false)
			return true;
		
		// everything after here is if pieceToMove is a pawn
		
		if (pieceToCapture != null)
		{
			// capture has to be diagonal
			if (m.getDestinationFile() == m.getOriginalFile())
				return false;
		}
		// pawn does not land on enemy piece
		else
		{
			// if it is en passant, then it is legal
			if (isEnPassant(m, b.getLastMove()))
			{
				m.setEnPassant(true);
				return true;
			}
			// otherwise, it has to be the same file
			return m.getDestinationFile() == m.getOriginalFile();
		}
		return true;
	}
	
	private boolean isEnPassant(Move currentMove, Move lastMove)
	{
		if (lastMove == null)
		{
			return false;
		}
		if (lastMove.getPiece() instanceof Pawn
				&& Math.abs(lastMove.getDestinationRow() - lastMove.getOriginalRow()) == 2)
		{
			if (currentMove.getPiece().getColor() == Piece.WHITE 
					&& currentMove.getDestinationRow() == 2
					&& currentMove.getDestinationFile() == lastMove.getDestinationFile())		
				return true;
			if (currentMove.getPiece().getColor() == Piece.BLACK 
					&& currentMove.getDestinationRow() == 5
					&& currentMove.getDestinationFile() == lastMove.getDestinationFile())
				return true;
			
		}
		return false;
	}
	
	/**
	 * Assumes that the Move is a move that the piece can make
	 */
	private boolean hasBlockingPiece(Board b, Move m)
	{
		Piece pieceToMove = m.getPiece();
		if (pieceToMove instanceof Knight)
			return false;
		
		int horizontalDistance = m.getDestinationFile() - m.getOriginalFile();
		int verticalDistance = m.getDestinationRow() - m.getOriginalRow();
		
		int rowDirection;
		int fileDirection;
		int distance;
		
		if (verticalDistance == 0) {
			rowDirection = 0;
		}
		else {
			rowDirection = verticalDistance / Math.abs(verticalDistance);
		}
		
		if (horizontalDistance == 0) {
			fileDirection = 0;
		}
		else {
			fileDirection = horizontalDistance / Math.abs(horizontalDistance);
		}
		
		if (Math.abs(horizontalDistance) > Math.abs(verticalDistance)) {
			distance = Math.abs(horizontalDistance);
		}
		else {
			distance = Math.abs(verticalDistance);
		}
		
		if (distance == 1)
		{
			return false;
		}
		for (int i = 1; i < distance; i++)
		{
			if (b.getPiece(m.getOriginalRow() + i*rowDirection, m.getOriginalFile() + i*fileDirection) != null)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean canCheck(int kingColor, int kingRow, int kingFile, Board b)
	{
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				if (b.getPiece(i, j) != null && b.getPiece(i, j).getColor() != kingColor)
				{
					Move checkingMove = new Move(b.getPiece(i, j), i, j, kingRow, kingFile);

					if (b.getPiece(i, j).canMove(checkingMove) && hasBlockingPiece(b, checkingMove) == false)
					{
						if (checkingMove.getPiece() instanceof Pawn)
						{
							if (checkingMove.getOriginalFile() == checkingMove.getDestinationFile())
							{
								continue;
							}
						}
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean canCastle(int color, Move m, Board b)
	{
		if (m.getPiece() instanceof King && canCheck(color, m.getOriginalRow(), m.getOriginalFile(), b))
			return false;
		if (m.equals(new Move(new King(Piece.BLACK), 0, 4, 0, 2)))
		{
			if (b.getPiece(0, 1) == null && b.getPiece(0, 2) == null && b.getPiece(0, 3) == null
					&& b.getPiece(0, 0) != null && b.getPiece(0, 0) instanceof Rook && b.getPiece(0, 0).getColor() == color)
			{
				if (!canCheck(color, 0, 2, b) && !canCheck(color, 0, 3, b))
				{
					if (b.getCastlingRights(3))
					{
						m.setCastleQueenside(true);
						return true;
					}
				}
			}
		}
		
		else if (m.equals(new Move(new King(Piece.BLACK), 0, 4, 0, 6)))
		{
			if (b.getPiece(0, 5) == null && b.getPiece(0, 6) == null
					&& b.getPiece(0, 7) != null && b.getPiece(0, 7) instanceof Rook && b.getPiece(0, 7).getColor() == color)
			{
				if (!canCheck(color, 0, 5, b) && !canCheck(color, 0, 6, b))
				{
					if (b.getCastlingRights(2))
					{
						m.setCastleKingside(true);
						return true;
					}
				}
			}
		}
		
		else if (m.equals(new Move(new King(Piece.WHITE), 7, 4, 7, 2)))
		{
			if (b.getPiece(7, 1) == null && b.getPiece(7, 2) == null && b.getPiece(7, 3) == null
					&& b.getPiece(7, 0) != null && b.getPiece(7, 0) instanceof Rook && b.getPiece(7, 0).getColor() == color)
			{
				if (!canCheck(color, 7, 2, b) && !canCheck(color, 7, 3, b))
				{
					if (b.getCastlingRights(1))
					{
						m.setCastleQueenside(true);
						return true;
					}
				}
			}
		}
		else if (m.equals(new Move(new King(Piece.WHITE), 7, 4, 7, 6)))
		{
			if (b.getPiece(7, 5) == null && b.getPiece(7, 6) == null
					&& b.getPiece(7, 7) != null && b.getPiece(7, 7) instanceof Rook && b.getPiece(7, 7).getColor() == color)
			{
				if (!canCheck(color, 7, 5, b) && !canCheck(color, 7, 6, b))
				{
					if (b.getCastlingRights(0))
					{
						m.setCastleKingside(true);
						return true;
					}
				}
			}
		}
		return false;
	}
}
