package engine;

public class SEE {
	private final int[] pieceValues = {100, 310, 320, 520, 950, 32000, 0};
	private final MoveGenerator movegen = new MoveGenerator();
	
	// with inspiration from Ethereal and Mess chess engines
	public boolean staticExchangeEvaluation(Board b, int move, int threshold) {
		int from         = Move.getFrom(move);
		int target       = Move.getTo(move);
		int side         = b.getSideToMove();
		int agressorType = b.getPiece(from).getType();
		
		// checks for en passant and direct capture
		int victimType = Move.getEnPassant(move) == 1 ? Piece.PAWN : b.getPiece(target).getType();

		int balance = pieceValues[victimType] - threshold;
		
		// winning the piece still can't overcome threshold
		if (balance < 0) {
			return false;
		}
		
		balance -= pieceValues[agressorType];
		
		// losing the capturing piece still beats threshold
		if (balance >= 0) {
			return true;
		}
	
		// calculate attackers to target square
		long attackers = movegen.attacksTo(b, target);
		
		// reveal sliding piece attackers for x ray attacks
		long diagonal = b.getPieceBitBoard(Piece.BISHOP) | b.getPieceBitBoard(Piece.QUEEN);
		long straight = b.getPieceBitBoard(Piece.ROOK)   | b.getPieceBitBoard(Piece.QUEEN);
		
		// simulate the capture by removing the attacker
		long occupied = b.getOccupiedSquares();
		occupied ^= 0x1L << from;
		side = 1 - side;
		
		while (true) {
			// calculate attackers from the current side
			long friendlyAttackers = attackers & b.getSideBitBoard(side);
			
			// no more attackers from current side = stop
			if (friendlyAttackers == 0) {
				break;
			}

			// find the least valuable piece to attack with
			int nextAttacker = 0;
			for (nextAttacker = Piece.PAWN; nextAttacker < Piece.KING; nextAttacker++) {
				// found it
				if ((friendlyAttackers & b.getPieceBitBoard(nextAttacker)) != 0) {
					break;
				}
			}
			
			// simulate the capture by removing the attacker
			occupied  ^= 0x1L << BitBoard.getLSB(friendlyAttackers & b.getPieceBitBoard(nextAttacker));

			// add attackers which were hidden by the capturing piece (x rays)
			if (nextAttacker == Piece.PAWN || nextAttacker == Piece.BISHOP || nextAttacker == Piece.QUEEN) {
				attackers |= movegen.getBishopAttacks(occupied, target) & diagonal;
			}
			
			if (nextAttacker == Piece.ROOK || nextAttacker == Piece.QUEEN) {
				attackers |= movegen.getRookAttacks(occupied, target) & straight;
			}
			
			// remove attackers which have already captured + remove capturer
			attackers &= occupied;
			side       = 1-side;
			balance    = -balance - 1 - pieceValues[nextAttacker];
			
			if (balance >= 0) {
				if ((nextAttacker == Piece.KING) && (attackers & ~friendlyAttackers) != 0) {
					// other side still has attackers, so the king capturing would be illegal and we lose
					side = 1-side;
				}
				break;
			}
		}

		// side that failed to capture back loses the exchange
		return side != b.getSideToMove();
	}
}