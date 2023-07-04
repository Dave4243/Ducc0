package chess;

import javax.swing.ImageIcon;
import java.util.ArrayList;

/**
 * @author Dave4243
 */
public abstract class Piece {
	private int pieceColor;
	private ImageIcon pieceIcon;
	
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	
	public Piece(int color, ImageIcon i) {
		pieceColor = color;
		pieceIcon  = i;
	}
	
	public int getColor() 
	{
		return pieceColor;
	}
	
	public ImageIcon getIcon()
	{
		return pieceIcon;
	}
	public abstract char toChar();
	public abstract int toInt();
	public abstract boolean canMove(Move m);
	
	// generates all moves for the piece treating the board as if it was empty
	public abstract ArrayList<Move> generateFakeMoves(int row, int col);
	
	protected void addDiagonalMoves(ArrayList<Move> result, int row, int col) {
		int x = row;
		int y = col;
		while (x < 7 && y < 7) {
			x++;
			y++;
			result.add(new Move(this, row, col, x, y));
		}
		
		x = row;
		y = col;
		while (x < 7 && y > 0) {
			x++;
			y--;
			result.add(new Move(this, row, col, x, y));
		}
		
		x = row;
		y = col;
		while (x > 0 && y > 0) {
			x--;
			y--;
			result.add(new Move(this, row, col, x, y));
		}
		
		x = row;
		y = col;
		while (x > 0 && y < 7) {
			x--;
			y++;
			result.add(new Move(this, row, col, x, y));
		}
	}
	
	protected void addHorizontalMoves(ArrayList<Move> result, int xPos, int yPos) {
		int x = xPos;
		int y = yPos;
		while (x < 7) {
			x++;
			result.add(new Move(this, xPos, yPos, x, y));
		}
		
		x = xPos;
		while (x > 0) {
			x--;
			result.add(new Move(this, xPos, yPos, x, y));
		}
		x = xPos;
		y = yPos;
		while (y > 0) {
			y--;
			result.add(new Move(this, xPos, yPos, x, y));
		}
		
		y = yPos;
		while (y < 7) {
			y++;
			result.add(new Move(this, xPos, yPos, x, y));
		}
	}
	
	public boolean equals(Piece p)
	{
		if (p != null && p.getColor() == this.pieceColor)
		{
			if (p.getClass() == this.getClass())
			{
				return true;
			}
		}
		return false;
	}
}
