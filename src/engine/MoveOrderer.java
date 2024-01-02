package engine;

/**
 * @author Dave4243
 * The MoveOrderer.java class orders moves 
 */
public class MoveOrderer {
	
	private int[] pieceValues = {0, 100, 315, 315, 520, 950, 32000};
	private int[] promotionValues = {-100, -100, 900, -100, -100, 950, 0};
	public static int[][][] butterflyHistory = new int[2][64][64];
	private int goodCaptureBonus = 10000000;
	private int badCaptureBonus = -10000000;
	
	private SEE see = new SEE();
	
	/**
	 * 1. Hash/PV Moves (Moves deemed best from the transposition table)
	 * 2. Winning captures/promotions
	 * 3. Equal captures/promotions
	 * 4. Killer Moves
	 * 5. Quiet moves ordered based on history heuristic
	 * 6. Losing captures/promotions
	 */
	public void scoreMoves(MoveList moveList, Board b, int hashMove, int ply) {
		for (int i = 0; i < moveList.size(); i++) {
			if (moveList.moves[i] == hashMove) {
				moveList.scores[i] = Integer.MAX_VALUE;
			} else {
				moveList.scores[i]= computeValue(b, moveList.moves[i], ply);
			}
		}
	}
	
	public void sortNext(MoveList moveList, int moveNumber) {
		int bestScore = Integer.MIN_VALUE;
		int bestIndex = moveNumber;
		for (int i = moveNumber; i < moveList.size(); i++) {
			if (moveList.scores[i] > bestScore) {
				bestScore = moveList.scores[i];
				bestIndex = i;
			}
		}
		int temp = moveList.moves[moveNumber];
		moveList.moves[moveNumber] = moveList.moves[bestIndex];
		moveList.moves[bestIndex] = temp;
		
		int t = moveList.scores[moveNumber];
		moveList.scores[moveNumber] = moveList.scores[bestIndex];
		moveList.scores[bestIndex] = t;
	}
	
	private int computeValue(Board b, int m, int ply) {
		// quiet moves are searched after equal captures and killers
		if (Move.getCaptured(m) == Piece.NULL && Move.getPromotion(m) == Piece.NULL) {
			return butterflyHistory[b.getSideToMove()][Move.getFrom(m)][Move.getTo(m)];
		}
		
		int promotion = pieceValues[Move.getPromotion(m) + 1];

		int aggressor = pieceValues[b.getPiece(Move.getFrom(m)).getType()];

		int victim = pieceValues[Move.getCaptured(m) + 1];
		
		return (victim + promotion) * 1000 - aggressor;
	}
	
	public void clearHistory() {
		butterflyHistory = new int[2][64][64];
	}
	
}