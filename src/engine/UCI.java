package engine;

import java.util.Scanner;

public class UCI {
	private static Board         board;
	private static Search        search;
	
	public static int ttSize = 16;

	private static void initialize() {
		board  = new Board();
		search = new Search(board);
		MoveOrderer.clearHistory();
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
			System.out.println("option name Hash type spin default 16 min 1 max 1000");
			System.out.println("uciok");
			break;
		case "setoption":
			setOptions(args);
			break;
		case "ucinewgame":
			initialize();
			break;
		case "isready":
			System.out.println("readyok");
			break;
		case "position": 
			setupPosition(args);
			break;
		case "go":
			System.out.println("bestmove " + go(args));
			break;
		case "display":
			System.out.println(board);
			break;
		}
	}
	
	private static void setOptions(String[] input) {
		if ("Hash".equals(input[2])) {
			ttSize = Integer.valueOf(input[4]);
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
			int bestMove = search.getBestMove(board, wtime, winc);
			board.doMove(bestMove);
			return Move.toString(bestMove);
		}
		int bestMove = search.getBestMove(board, btime, binc);
		board.doMove(bestMove);
		return Move.toString(bestMove);
		
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
			board = new Board(f);
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
				int m = convertToMove(board, input[i]);
				board.doMove(m);
			}
		}
	}
	
	private static int convertToMove(Board b, String str) {
		int fromPos = (Integer.valueOf(str.substring(1,2))-1) * 8 + str.charAt(0) - 'a';
		int toPos   = (Integer.valueOf(str.substring(3,4))-1) * 8 + str.charAt(2) - 'a';
		
		int promotionPiece = Piece.NULL;
		if (str.length() == 5) {
			char pp = str.charAt(4);
			switch (pp) {
				case 'q': promotionPiece = Piece.QUEEN;
					      break;
				case 'r': promotionPiece = Piece.ROOK;
						  break;
				case 'b': promotionPiece = Piece.BISHOP;
						  break;
				case 'n': promotionPiece = Piece.KNIGHT;
						  break;
			}
		}
		
		Piece p = b.getPiece(fromPos);
		
		int result = Move.make(fromPos, toPos, promotionPiece);
		result = Move.setCap(result, b.getPiece(toPos));

		if (p.getType() == Piece.PAWN 
				&& (fromPos & 7) != (toPos & 7) 
				&& b.getPiece(toPos) == null
				&& toPos == BitBoard.getLSB(b.getEnPassantTarget())) {
			result = Move.setEnPassant(result, 1);
			int[] push = {-8, 8};
			result = Move.setCap(result, b.getPiece(toPos + push[b.getSideToMove()]));
		}
		
		if (p.getType() == Piece.KING
				&& Math.abs((fromPos & 7) - (toPos & 7)) == 2) {
			if (toPos == 2) result = Move.setCastle(result, 0b0010);
			if (toPos == 6) result = Move.setCastle(result, 0b0001);
			
			if (toPos == 58) result = Move.setCastle(result, 0b1000);
			if (toPos == 62) result = Move.setCastle(result, 0b0100);
		}

		return result;
	}
}