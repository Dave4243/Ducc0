package chess;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

/**
 * @description The Chess.java application provides a interactive Chess GUI that allows 2
 * players to play the game of chess against each other
 */
public class Chess extends JFrame implements ActionListener {
	private static JFrame f;
	
	private JPanel        mainPanel;
	private FenConverter  converter;
	
	private JPanel        boardPanel;
	private JButton[][]   display;
	
	private Board         board;
	private int           selectedX = -1, selectedY = -1;
	private boolean       hasSelectedSquare = false;
	
	private Color         selected = new Color(0,0,0), possibleMoveColor = Color.DARK_GRAY;
	
	private int 		  moveColor;
	
	private ArrayList<Move> possibleMoves;
	
	private MoveGenerator   generator = new MoveGenerator();
	
	private ChessEngine neo;
	/**
	 * main()
	 */
//	public static void main(String args[]) {
//		Chess app = new Chess();
//		try
//		{
//		    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//		}
//		catch (Exception e) {
//			
//		}
//
//		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		f.pack();
//		f.setResizable(true);
//		f.pack();
//		f.setVisible(true);
//	}
	
	/**
	 * Constructor
	 */
	public Chess()
	{
		f          = new JFrame("Chess");
		boardPanel = new JPanel(new GridLayout(8,8));
	
		mainPanel  = new JPanel(new GridBagLayout());

		display = new JButton[8][8];

		board   = new Board();
		
//		board = FenConverter.convert("8/8/3K4/8/8/8/5k2/q6r w - - ", board);
		
		neo = new ChessEngine(board);
		
//		board = FenConverter.convert("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ");
		
        for (int row = 0; row < 8; row++)
        {
            for (int file = 0; file < 8; file++)
            {
            	display[row][file] = new JButton();
            	display[row][file].setPreferredSize(new Dimension(80, 80));
				display[row][file].setBorder(new LineBorder(Color.BLACK));
                display[row][file].addActionListener(this);
                if ((row + file) % 2 == 0)
                {
                	display[row][file].setBackground(new Color(238,238,210));
                }
                else
                {
                	display[row][file].setBackground(new Color(118,150,86));
                }
                boardPanel.add(display[row][file]);
            }
        }
        moveColor = Piece.WHITE;
        refreshBoard();
        
//        showMoves(Color.WHITE);
		f.setContentPane(boardPanel);
	}
	
	public void showMoves(int color)
	{
		MoveGenerator generator = new MoveGenerator();
		
		long d = System.nanoTime();
		ArrayList<Move> allMoves = generator.generateAllPossibleMoves(color, board);
		Long dd = System.nanoTime();
		
		System.out.println("Time to generate all moves: " + (dd - d)/1000000000.0);
		
		for (Move m : allMoves)
		{
			display[m.getDestinationRow()][m.getDestinationFile()].setBackground(Color.BLACK);
		}
		//System.out.println(allMoves);
	}
	
	/**
	 * Responsible to update GUI in accordance to the internal board
	 */
	public void refreshBoard()
	{
		for (int i = 0; i < 8; i++)
		{
			for (int x = 0; x < 8; x++)
			{
				Piece p = board.getPiece(i, x);
				if (p != null)
				{
					display[i][x].setIcon(p.getIcon());
				}
				if (p == null)
				{
					display[i][x].setIcon(null);
					
				}
			}
		}
	}
	
	public void resetColors()
	{
        for (int row = 0; row < 8; row++)
        {
            for (int file = 0; file < 8; file++)
            {
                if ((row + file) % 2 == 0)
                {
                	display[row][file].setBackground(new Color(238,238,210));
                }
                else
                {
                	display[row][file].setBackground(new Color(118,150,86));
                }
            }
        }
	}
	
	/**
	 * Restores the ability to click the pieces
	 */
	public void addActionListeners()
	{
		for (JButton[] buttons : display)
		{
			for (JButton button : buttons)
			{
				if (button.getActionListeners().length == 0)
				{
					button.addActionListener(this);
				}
			}
		}
	}
	
	/**
	 *
	 */
	public int setSelectedSquare(int i, int x)
	{
		// unselect selected square
		if (i == selectedX && x == selectedY) {
			hasSelectedSquare = false;
			selectedX = -1;
			selectedY = -1;
			resetColors();
			return -1;
		}
		
		// if click is on no piece or enemy piece
		if (board.getPiece(i, x) == null || ((board.getPiece(i, x) != null && board.getPiece(i, x).getColor() != moveColor))) {
			// has selected square means move attempted
			if (selectedX != -1 && selectedY != -1)
			{
				return 0;
			}
			// no selected square means invalid click
			return 999;
		}

		resetColors();

		display[i][x].setBackground(selected);

		possibleMoves = generator.generatePossibleMovesFromSquare(moveColor, board, i, x);
		for (Move mv : possibleMoves)
		{
			if (mv != null)
			display[mv.getDestinationRow()][mv.getDestinationFile()].setBackground(possibleMoveColor);
		}
		
		hasSelectedSquare = true;
		selectedX = i;
		selectedY = x;
		
		return 1;
	}

	/**
	 * actionPerformed() handles user inputs
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		// loops through buttons 
		for (int i = 0; i < 8; i++) 
		{
			for (int x = 0; x < 8; x++)
			{
				// clicked on a square
				if (e.getSource() == display[i][x])
				{
					// attempted move!
					if (setSelectedSquare(i,x) == 0)
					{
						Move m = new Move(board.getPiece(selectedX, selectedY), selectedX, selectedY, i, x);
						if (i == 0 && board.getPiece(selectedX, selectedY) instanceof Pawn)
						{
							m.setPromotionPiece(new Queen(Piece.WHITE));
						}
						for (Move move : possibleMoves)
						{
							if (move != null && move.equals(m))
							{
								board.doMove(move);
								refreshBoard();
								resetColors();
								repaint();
								revalidate();
								neo.play(board, 600000, 0);
								refreshBoard();
								possibleMoves = generator.generateAllPossibleMoves(moveColor, board);
								selectedX = -1;
								selectedY = -1;
								hasSelectedSquare = false;
								System.out.println("Halfmove Clock: " + board.getHalfMoveClock());
							}
						}
					}
				}
			}
		}
	}
}