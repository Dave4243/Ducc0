package chess;

import java.util.*;

/**
 * @author Dave4243
 * The MoveGenerator.java class generates various arraylists of moves
 * that are legal.
 */
public class MoveGenerator {
	MoveChecker checker = new MoveChecker();
	
	public ArrayList<Move> generateAllPossibleMoves(int color, Board b)
	{
		ArrayList<Move> result = new ArrayList<Move>();
		
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				if (b.getPiece(i, j) != null && b.getPiece(i, j).getColor() == color)
				{
					ArrayList<Move> l = generatePossibleMovesFromSquare(color, b, i, j);
					for (Move m : l)
					{
						if (m != null)
						result.add(m);
					}
				}
			}
		}
		return result;
	}
	
	public ArrayList<Move> generatePossibleMovesFromSquare(int color, Board b, int row, int col)
	{
		Piece p = b.getPiece(row, col);
		ArrayList<Move> result = p.generateFakeMoves(row, col);
//		Board temp = b.cloneBoard();
		
		if (p instanceof Pawn)
		{
			if ((p.getColor() == Piece.WHITE && row == 1)
					|| (p.getColor() == Piece.BLACK && row == 6))
			{
				result = addPromotionMoves(result, p.getColor());
			}
		}
		
		for (int i = 0; i < result.size(); i++)
		{
			Move m = result.get(i);
			if (checker.checkMove(b, m) == false) {
				result.set(i, null);
				continue;
			}
			
			b.doMove(m);
			
			if (checker.canCheck(color, b.getKingPosition(color)[0], b.getKingPosition(color)[1], b))
			{
				result.set(i, null);
			}
			b.undoMove(m);
//			b.setPiece(b.getPiece(m.getDestinationRow(), m.getDestinationFile()), m.getDestinationRow(), m.getDestinationFile());
		}
		return result;
	}
	
	public ArrayList<Move> generateCaptureMovesFromSquare(int color, Board b, int row, int col) {
		Piece p = b.getPiece(row, col);
		ArrayList<Move> result = p.generateFakeMoves(row, col);
//		Board temp = b.cloneBoard();
		
		if (p instanceof Pawn)
		{
			if ((p.getColor() == Piece.WHITE && row == 1)
					|| (p.getColor() == Piece.BLACK && row == 6))
			{
				result = addPromotionMoves(result, p.getColor());
			}
		}
		
		// does not generate en passant captures cus i'm too lazy (irrelevant moves anyway)
		for (int i = 0; i < result.size(); i++)
		{
			Move m = result.get(i);
			if (b.getPiece(m.getDestinationRow(), m.getDestinationFile()) == null) {
				result.set(i, null);
				continue;
			}
			
			if (checker.checkMove(b, m) == false) {
				result.set(i, null);
				continue;
			}
			
			b.doMove(m);
			
			if (checker.canCheck(color, b.getKingPosition(color)[0], b.getKingPosition(color)[1], b))
			{
				result.set(i, null);
			}
			b.undoMove(m);
			b.setPiece(b.getPiece(m.getDestinationRow(), m.getDestinationFile()), m.getDestinationRow(), m.getDestinationFile());
		}
		return result;
	}
	
	public ArrayList<Move> generateAllCaptureMoves(int color, Board b)
	{
		ArrayList<Move> result = new ArrayList<Move>();
		
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				if (b.getPiece(i, j) != null && b.getPiece(i, j).getColor() == color)
				{
					ArrayList<Move> l = generateCaptureMovesFromSquare(color, b, i, j);
					for (Move m : l)
					{
						if (m != null)
						result.add(m);
					}
				}
			}
		}
		return result;
	}
	
	private ArrayList<Move> addPromotionMoves(ArrayList<Move> prev, int c)
	{
		ArrayList<Move> a = new ArrayList<Move>();
		for (Move m : prev)
		{
			Move m1 = m.clone();
			m1.setPromotionPiece(new Queen(c));
			
			Move m2 = m.clone();
			m2.setPromotionPiece(new Rook(c));
			
			Move m3 = m.clone();
			m3.setPromotionPiece(new Bishop(c));
		
			Move m4 = m.clone();
			m4.setPromotionPiece(new Knight(c));
			
			a.add(m1);
			a.add(m2);
			a.add(m3);
			a.add(m4);
		}
		return a;
	}
}
