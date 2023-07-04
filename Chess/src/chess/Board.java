package chess;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @author Dave4243
 * The Board.java class represents a chess board. Containing all the pieces
 * on squares, along with all the necessary information to simulate a real
 * chess board, such as castling rights and past move (for en passant)
 */
public class Board {

	private Square[][] squares = new Square[8][8];
	
	// castling rights: white kingside, white queenside, black kingside, black queenside
	private Stack<BoardState> pastStates = new Stack<BoardState>();
	private ArrayList<Long>   pastKeys   = new ArrayList<Long>();
	
	private boolean[] castlingRights = new boolean[] {true, true, true, true};
	
	private int[] whiteKingPosition = new int[] {7, 4};
	private int[] blackKingPosition = new int[] {0, 4};
	
	private int   sideToMove;
	private int   moveNumber;
	
	private int   halfmoveClock;
	private long  hash;
	
	/**
	 * Makes a default Chess board with the starting position
	 */
	public Board()
	{
		squares = createEmptyBoard();

		// sets black starting position on board
		squares[0][0].setPiece(new Rook(Piece.BLACK));
		squares[0][1].setPiece(new Knight(Piece.BLACK));
		squares[0][2].setPiece(new Bishop(Piece.BLACK));
		squares[0][3].setPiece(new Queen(Piece.BLACK));
		squares[0][4].setPiece(new King(Piece.BLACK));
		squares[0][5].setPiece(new Bishop(Piece.BLACK));
		squares[0][6].setPiece(new Knight(Piece.BLACK));
		squares[0][7].setPiece(new Rook(Piece.BLACK));
		
		for (int i = 0; i < 8; i++)
		{
			squares[1][i].setPiece(new Pawn(Piece.BLACK));
		}
		
		// sets white starting position on board
		squares[7][0].setPiece(new Rook(Piece.WHITE));
		squares[7][1].setPiece(new Knight(Piece.WHITE));
		squares[7][2].setPiece(new Bishop(Piece.WHITE));
		squares[7][3].setPiece(new Queen(Piece.WHITE));
		squares[7][4].setPiece(new King(Piece.WHITE));
		squares[7][5].setPiece(new Bishop(Piece.WHITE));
		squares[7][6].setPiece(new Knight(Piece.WHITE));
		squares[7][7].setPiece(new Rook(Piece.WHITE));
		
		for (int i = 0; i < 8; i++)
		{
			squares[6][i].setPiece(new Pawn(Piece.WHITE));
		}
		
		sideToMove = Piece.WHITE;
		hash = ZobristHasher.calculateZobristKey(this);
	}
	
	/**
	 * Makes a board with a custom position
	 * @param s The array of squares to set the custom position of the board
	 */
	public Board(Square[][] s)
	{
		squares = createEmptyBoard();
		for (int i = 0; i < 8; i++)
		{
			for (int x = 0; x < 8; x++)
			{
				Piece temp = s[i][x].getPiece();
				squares[i][x].setPiece(temp);
			}
		}
	}
	
	public Piece getPiece(int row, int col) {
		return squares[row][col].getPiece();
	}
	
	public void setPiece(Piece p, int row, int col) {
		squares[row][col].setPiece(p);
	}
	
	private void movePiece(int row, int col, int dRow, int dCol) {
		setPiece(getPiece(row, col), dRow, dCol);
		setPiece(null, row, col);
	}
	
	public void removePiece(int row, int col) {
		squares[row][col].setPiece(null);
	}
	
	// precondition: the Move m is a valid move
	// postcondition: the Move m is executed on the board
	public void doMove(Move m)
	{
		pastKeys.add(hash);
		BoardState entry = new BoardState(
				m, 
				new boolean[] {castlingRights[0], castlingRights[1], castlingRights[2],castlingRights[3]}, 
				halfmoveClock, 
				hash
				);
		pastStates.push(entry);
		
		Piece p = m.getPiece();

		m.setCapturedPiece(this.getPiece(m.getDestinationRow(), m.getDestinationFile()));
		
		if (m.getEnPassant()) {
			if (p.getColor() == Piece.WHITE) {
				removePiece(m.getDestinationRow()+1, m.getDestinationFile());
			}
			else if (p.getColor() == Piece.BLACK) {
				removePiece(m.getDestinationRow()-1, m.getDestinationFile());
			}
		}
		
		if (!castle(m))
			movePiece(m.getOriginalRow(), m.getOriginalFile(), m.getDestinationRow(), m.getDestinationFile());
		
		if (m.getPromotionPiece() != null) {
			setPiece(m.getPromotionPiece(), m.getDestinationRow(), m.getDestinationFile());
		}
		
		if (p instanceof King) {
			if (p.getColor() == Piece.WHITE) {
				setWhiteKingPosition(new int[] {m.getDestinationRow(), m.getDestinationFile()});
				castlingRights[0] = false;
				castlingRights[1] = false;
			}
			else {
				setBlackKingPosition(new int[] {m.getDestinationRow(), m.getDestinationFile()});
				castlingRights[2] = false;
				castlingRights[3] = false;
			}
		}
		
		if (p instanceof Rook) {
			if (p.getColor() == Piece.WHITE && m.getOriginalRow() == 7 && m.getOriginalFile() == 7) {
				castlingRights[0] = false;
			}
			else if (p.getColor() == Piece.WHITE && m.getOriginalRow() == 7 && m.getOriginalFile() == 0) {
				castlingRights[1] = false;
			}
			else if (p.getColor() == Piece.BLACK && m.getOriginalRow() == 0 && m.getOriginalFile() == 7) {
				castlingRights[2] = false;
			}
			else if (p.getColor() == Piece.BLACK && m.getOriginalRow() == 0 && m.getOriginalFile() == 0) {
				castlingRights[3] = false;
			}
		}

		if (m.getCapturedPiece() != null || p instanceof Pawn) 
			halfmoveClock = 0;
		else
			halfmoveClock++;
		
		if (sideToMove == Piece.BLACK) 
			moveNumber++;
		
		hash = ZobristHasher.calculateZobristKey(this);
		sideToMove = 1-sideToMove;
	}
	
	public void undoMove(Move m)
	{
		Piece p = m.getPiece();
		pastKeys.remove(pastKeys.size()-1);

		BoardState pastState = pastStates.pop();
		castlingRights = pastState.getCastlingRights();
		halfmoveClock = pastState.getHalfmoveClock();
		hash = pastState.getPreviousPosition();
		
		if (m.getEnPassant()) {
			if (p.getColor() == Piece.WHITE) {
				setPiece(new Pawn(Piece.BLACK), m.getDestinationRow()+1, m.getDestinationFile());
			}
			
			else if (p.getColor() == Piece.BLACK) {
				setPiece(new Pawn(Piece.WHITE), m.getDestinationRow()-1, m.getDestinationFile());
			}
		}
		
		if (!unCastle(m)) {
			this.setPiece(m.getPiece(), m.getOriginalRow(), m.getOriginalFile());
			this.setPiece(m.getCapturedPiece(), m.getDestinationRow(), m.getDestinationFile());
		}
		
		if (p instanceof King) {
			if (p.getColor() == Piece.WHITE){
				setWhiteKingPosition(new int[] {m.getOriginalRow(), m.getOriginalFile()});
			}
			else {
				setBlackKingPosition(new int[] {m.getOriginalRow(), m.getOriginalFile()});
			}
		}
		if (sideToMove == Piece.WHITE) 
			moveNumber--;
		sideToMove = 1-sideToMove;
	}
	
	private boolean castle(Move m) {
		if (m.getCastleKingside()) {
			if (m.getPiece().getColor() == Piece.WHITE && castlingRights[0]) {
				movePiece(7, 4, 7, 6);
				movePiece(7, 7, 7, 5);
				castlingRights[0] = false;
				castlingRights[1] = false;
				return true;
			}
			else if (m.getPiece().getColor() == Piece.BLACK && castlingRights[2]) {
				movePiece(0, 4, 0, 6);
				movePiece(0, 7, 0, 5);
				castlingRights[2] = false;
				castlingRights[3] = false;
				return true;
			}
		}
		
		else if (m.getCastleQueenside()) {
			if (m.getPiece().getColor() == Piece.WHITE && castlingRights[1]) {
				movePiece(7, 4, 7, 2);
				movePiece(7, 0, 7, 3);
				castlingRights[0] = false;
				castlingRights[1] = false;
				return true;
			}
			else if (m.getPiece().getColor() == Piece.BLACK && castlingRights[3]) {
				movePiece(0, 4, 0, 2);
				movePiece(0, 0, 0, 3);
				castlingRights[2] = false;
				castlingRights[3] = false;
				return true;
			}
		}
		return false;
	}
	
	private boolean unCastle(Move m) {
		if (m.getCastleKingside()) {
			if (m.getPiece().getColor() == Piece.WHITE) {
				movePiece(7, 6, 7, 4);
				movePiece(7, 5, 7, 7);
				return true;
			}
			else if (m.getPiece().getColor() == Piece.BLACK) {
				movePiece(0, 6, 0, 4);
				movePiece(0, 5, 0, 7);
				return true;
			}
		}
		
		else if (m.getCastleQueenside()) {
			if (m.getPiece().getColor() == Piece.WHITE) {
				movePiece(7, 2, 7, 4);
				movePiece(7, 3, 7, 0);
				return true;
			}
			else if (m.getPiece().getColor() == Piece.BLACK) {
				movePiece(0, 2, 0, 4);
				movePiece(0, 3, 0, 0);
				return true;
			}
		}
		return false;
	}
	
	// creates a new board with all editable references removed
	// idk this method is actually ever used (previously used for move
	// generation but scrapped due to inefficiency)
	public Board cloneBoard()
	{
		Square[][] result = createEmptyBoard();
		Square[][] reference = squares;
		
		// pieces are not edited, only used, so its ok to use reference
		for (int i = 0; i < 8; i++) {
			for (int x = 0; x < 8; x++) {
				Piece temp = reference[i][x].getPiece();
				result[i][x].setPiece(temp);
			}
		}
		
		Board clone = new Board(result);
		
		Stack<BoardState> statesCopy = new Stack<BoardState>();
		
		BoardState currentState;

		Move m = null;
		boolean[] copy = new boolean[] {true, true, true, true};
		int moveClock = halfmoveClock;
		long key      = hash;
		// copies previous board state (removes reference to this)
		if (!pastStates.isEmpty()) {
			boolean[] temp = pastStates.peek().getCastlingRights();
			for (int i = 0; i < 4; i++) {
				copy[i] = temp[i];
			}
			
			m = pastStates.peek().getMove();
		}
		
		currentState = new BoardState(m, copy, moveClock, key);
		statesCopy.push(currentState);
		clone.pastStates = statesCopy;
		
		for (int i = 0; i < 4; i++) {
			clone.castlingRights[i] = this.castlingRights[i];
		}
		
		clone.whiteKingPosition = new int[] {this.whiteKingPosition[0], this.whiteKingPosition[1]};
		clone.blackKingPosition = new int[] {this.blackKingPosition[0], this.blackKingPosition[1]};
		
		return clone;
	}
	
	public Square[][] createEmptyBoard()
	{
		Square[][] result = new Square[8][8];
		
		for (int i = 0; i < 8; i++)
		{
			for (int x = 0; x < 8; x++)
			{
				result[i][x] = new Square(null, i, x);
			}
		}
		return result;
	}

	public String toString()
	{
		for (int i = 0; i < 8; i++)
		{
			for (int x = 0; x < 8; x++)
			{
				Piece temp = squares[i][x].getPiece();
				if (temp != null)
					System.out.print(squares[i][x].getPiece().toChar() + " ");
				else
					System.out.print("- ");
			
				if (x == 7)
					System.out.print("\n");
			}
		}
		return "";
	}
	
	public boolean equals(Board b)
	{
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				if (b.getPiece(i, j) != null && b.getPiece(i, j).equals(this.getPiece(i, j)) == false)
					return false;
			}
		}
		return true;
	}
	
	public void setWhiteKingPosition(int[] pos) {
		whiteKingPosition = pos;
	}
	
	public void setBlackKingPosition(int[] pos) {
		blackKingPosition = pos;
	}
	
	public Move getLastMove() {
		if (!pastStates.isEmpty())
			return pastStates.peek().getMove();
		return null;
	}
	
	public boolean getCastlingRights(int index) {
		return castlingRights[index];
	}
	
	public void setCastlingRights(int index, boolean newRight) {
		castlingRights[index] = newRight;
	}
	
	public int getSideToMove() {
		return sideToMove;
	}
	
	public int[] getKingPosition(int color) {
		if (color == Piece.BLACK) {
			return blackKingPosition;
		}
		return whiteKingPosition;
	}
	
	public int getAge() {
		return pastStates.size();
	}
	
	public int getMoveNumber() {
		return moveNumber;
	}
	
	public void setMoveNumber(int num) {
		moveNumber = num;
	}
	
	public long getZobristKey() {
		return hash;
	}
	
	public boolean isRepeat(long zobristKey) {
		int iterations = pastKeys.size();
		if (iterations > 10) {
			iterations = 10;
		}
		for (int i = 0; i < iterations; i++) {
			if (pastKeys.get(pastKeys.size()-1-i) == zobristKey)
				return true;
		}
		return false;
	}
	
	public void setSideToMove(int side) {
		sideToMove = side;
	}
	
	public int getHalfMoveClock() {
		return halfmoveClock;
	}
	
	public void setHalfMoveClock(int h) {
		halfmoveClock = h;
	}
	
	public void addPastState(BoardState state) {
		pastStates.add(state);
	}
}