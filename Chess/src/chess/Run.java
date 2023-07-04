package chess;

import java.util.Scanner;
public class Run {
	private static Board       board;
	private static ChessEngine engine;
	private static MoveChecker checker;

	private static void initialize() {
		board  = new Board();
		engine = new ChessEngine(board);
		checker = new MoveChecker();
	}

	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		String input = "";
		while (!input.equals("quit")) {
			input = s.nextLine();
			doCommand(input.split(" "));
		}
	}
	
	public static void doCommand(String[] args) {
		String command = args[0];
		switch(command ) {
		case "uci":	
			System.out.println("uciok");
			break;
		case "isready":
			initialize();
			System.out.println("readyok");
			break;
		case "position": 
			setupPosition(args);
			break;
		case "go":
			System.out.println("bestmove " + go(args));
			break;
		}
	}
	
	private static String go(String[] input) {
		int wtime = 60000;
		int btime = 60000;
		int winc  = 0;
		int binc  = 0;
		
		for (int i = 0; i < input.length; i++) {
			if (input[i].equals("wtime"))
				wtime = Integer.valueOf(input[i+1]);
			else if (input[i].equals("btime"))
				btime = Integer.valueOf(input[i+1]);
			else if (input[i].equals("winc"))
				winc = Integer.valueOf(input[i+1]);
			else if (input[i].equals("binc"))
				binc = Integer.valueOf(input[i+1]);
		}
		if (board.getSideToMove() == Piece.WHITE) {
			return engine.play(board, wtime, winc).toString();
		}
		return engine.play(board, btime, binc).toString();
		
	}
	
	private static void setupPosition(String[] input) {
		String fen = input[1];
		if (fen.equals("fen")) {
			int endIndex = 2;
			String f = "";
			while (endIndex < input.length && !input[endIndex].equals("moves")) {
				f += input[endIndex] + " ";
				endIndex++;
			}
			board = FenConverter.convert(f);
		}
		else if (fen.equals("startpos"))
			board = new Board();
		
		int index = 0;
		for (int i = 0; i < input.length; i++) {
			if (input[i].equals("moves")) {
				index = i+1;
				break;
			}
		}
		if (index != 0) {
			for (int i = index; i < input.length; i++) {
				Move m = convertToMove(board, input[i]);
				board.doMove(m);
			}
		}
	}
	
	private static Move convertToMove(Board b, String str) {
		int fromRow = 8 - Integer.valueOf(str.substring(1,2));
		int fromCol = str.charAt(0) - 'a';
		
		int toRow = 8 - Integer.valueOf(str.substring(3,4));
		int toCol = str.charAt(2) - 'a';
		
		Piece p = b.getPiece(fromRow, fromCol);
		
		Move result = new Move(p, fromRow, fromCol, toRow, toCol);
		checker.checkMove(b, result);
		
		int color = p.getColor();

		Piece promotionPiece = null;
		if (str.length() == 5) {
			char pp = str.charAt(4);
			switch (pp) {
				case 'q': promotionPiece = new Queen(color);
					      break;
				case 'r': promotionPiece = new Rook(color);
						  break;
				case 'b': promotionPiece = new Bishop(color);
						  break;
				case 'n': promotionPiece = new Knight(color);
						  break;
				default:  promotionPiece = null;
						  break;
			}
		}
		
		result.setPromotionPiece(promotionPiece);
		return result;
	}
}
