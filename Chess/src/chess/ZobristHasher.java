package chess;

import java.security.SecureRandom;

/**
 * @author Dave4243
 * The ZobristHasher.java class turns a position into a zobrist key
 */
public class ZobristHasher {
    private static final long[][][] zobristKeys;
    private static final long[][]   enPassantKeys;
    private static final long[]     castlingKeys;
    private static final long       sideToMoveKey;

    static {
        zobristKeys = new long[8][8][12];
        enPassantKeys = new long[8][2];
        castlingKeys = new long[4];

        SecureRandom random = new SecureRandom();

        // set up keys for each square
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 12; k++) {
                    zobristKeys[i][j][k] = random.nextLong();
                }
            }
        }
        
        // i represents file number of enpassant square
        // j = 0 means last move was white (black en passant possiblity)
        // j = 1 means last move was black (white en passant possiblity)
        for (int i = 0; i < 8; i++) {
        	for (int j = 0; j < 2; j++) {
        		enPassantKeys[i][j] = random.nextLong();
        	}
        }
        
        for (int i = 0; i < 4; i++) {
        	castlingKeys[i] = random.nextLong();
        }
        
        sideToMoveKey = random.nextLong();
    }

    public static long calculateZobristKey(Board b) {
        long zobristKey = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = b.getPiece(i,j);
                if (piece != null) {
                    int index = piece.toInt();
                    zobristKey ^= zobristKeys[i][j][index];
                }
            }
        }
        
        Move  lastMove   = b.getLastMove();
        Piece lastPiece  = null;
        int lastColor = 0;
        int moveDistance = 0;
        if (lastMove != null) {
        	lastPiece = lastMove.getPiece();
        	lastColor  = lastPiece.getColor();
        	moveDistance = Math.abs(lastMove.getDestinationRow() - lastMove.getOriginalRow());
        }
        
        if (lastMove != null && lastPiece instanceof Pawn && moveDistance == 2) {
        	if (lastColor == Piece.WHITE)
        		zobristKey ^= enPassantKeys[lastMove.getDestinationFile()][0];
        	else
        		zobristKey ^= enPassantKeys[lastMove.getDestinationFile()][1];
        }
        
        for (int i = 0; i < 4; i++) {
        	boolean bool = b.getCastlingRights(i);
        	if (bool)
        		zobristKey ^= castlingKeys[i];
        	
        }
        
        if (b.getSideToMove() == Piece.WHITE) {
        	zobristKey ^= sideToMoveKey;
        }
        return zobristKey;
    }
}
