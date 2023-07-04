package chess;

import java.util.ArrayList;
import javax.swing.ImageIcon;

/**
 * @author Dave4243
 */
public class Pawn extends Piece{

	public Pawn(int color) {
		super(color, icon(color));
	}
	
	private static ImageIcon icon(int c)
	{
		if (c == Piece.WHITE)
		{
			return new ImageIcon("src/chess/WhitePawn.png");
		}
		else if (c == Piece.BLACK)
		{
			return new ImageIcon("src/chess/BlackPawn.png");
		}
		return null;
	}

	@Override
	public boolean canMove(Move m)
	{
		// for white
		if (getColor() == Piece.WHITE)
		{
			// at starting position
			if (m.getOriginalRow() == 6)
			{
	
				if (m.getDestinationRow() + 2 == m.getOriginalRow() && m.getOriginalFile() == m.getDestinationFile())	
				{
					return true;
				}
			}
			// 1 move up OR capture
			if ((m.getDestinationRow() + 1 == m.getOriginalRow()  
				&& m.getOriginalFile() == m.getDestinationFile()) 
					||
				(m.getDestinationRow() + 1 == m.getOriginalRow()
				&& Math.abs(m.getOriginalFile() - m.getDestinationFile()) == 1))
			{
				return true;
			}
			return false;
		}
		
		// for black
		else if (getColor() == Piece.BLACK)
		{
			// at starting position
			if (m.getOriginalRow() == 1)
			{
				if (m.getDestinationRow() - 2 == m.getOriginalRow()  && m.getOriginalFile() == m.getDestinationFile())
				{
					return true;
				}
			}
			// 1 move OR capture
			if ((m.getDestinationRow() - 1 == m.getOriginalRow() 
				&& m.getOriginalFile() == m.getDestinationFile())
					||
				(m.getDestinationRow() - 1 == m.getOriginalRow()
				&& Math.abs(m.getOriginalFile() - m.getDestinationFile()) == 1))
			{
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<Move> generateFakeMoves(int row, int col)
	{
		ArrayList<Move> result = new ArrayList<Move>();
		
		if (super.getColor() == Piece.WHITE)
		{
			if (row == 6)
			{
				result.add(new Move(this, row, col, row-2, col));
			}
			if (col - 1 >= 0)
				result.add(new Move(this, row, col, row-1, col-1));
			
			result.add(new Move(this, row, col, row-1, col));
			
			if (col + 1 < 8)
				result.add(new Move(this, row, col, row-1, col+1));
		}
		if (super.getColor() == Piece.BLACK)
		{
			if (row == 1)
			{
				result.add(new Move(this, row, col, row+2, col));
			}
			if (col - 1 >= 0)
				result.add(new Move(this, row, col, row+1, col-1));
			
			result.add(new Move(this, row, col, row+1, col));
			
			if (col + 1 < 8)
				result.add(new Move(this, row, col, row+1, col+1));
		}
		return result;
	}
	public String toString()
	{
		return "P";
	}
	
	public char toChar()
	{
		if (super.getColor() == Piece.WHITE)
			return 'P';
		
		return 'p';
	}
	
	public int toInt() {
		return super.getColor() == Piece.WHITE ? 0 : 1;
	}
}
