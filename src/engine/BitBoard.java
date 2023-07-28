package engine;
/**
 * @author Dave4243
 * @Description Utility class for BitBoards
 */
public final class BitBoard {
	private static final long[] fileMasks = {
		0x0101010101010101L, 0x0202020202020202L,
		0x0404040404040404L, 0x0808080808080808L,
		0x1010101010101010L, 0x2020202020202020L,
		0x4040404040404040L, 0x8080808080808080L
	};

	private static final long[] rowMasks = {
		0x00000000000000ffL, 0x000000000000ff00L,
		0x0000000000ff0000L, 0x00000000ff000000L,
		0x000000ff00000000L, 0x0000ff0000000000L, 
		0x00ff000000000000L, 0xff00000000000000L
	};
	
	private static final long[] diagonalMasks = {
		0x8040201008040201L, 0x4020100804020100L, 0x2010080402010000L, 0x1008040201000000L, 
		0x0804020100000000L, 0x0402010000000000L, 0x0201000000000000L, 0x0100000000000000L, 
		0x0000000000000000L, 0x0000000000000080L, 0x0000000000008040L, 0x0000000000804020L, 
		0x0000000080402010L, 0x0000008040201008L, 0x0000804020100804L, 0x0080402010080402L
	};
	
	private static final long[] antiDiagonalMasks = {
		0x0102040810204080L, 0x0001020408102040L, 0x0000010204081020L, 0x0000000102040810L,
		0x0000000001020408L, 0x0000000000010204L, 0x0000000000000102L, 0x0000000000000001L, 
		0x0000000000000000L, 0x8000000000000000L, 0x4080000000000000L, 0x2040800000000000L,
		0x1020408000000000L, 0x0810204080000000L, 0x0408102040800000L, 0x0204081020408000L
	};
	
	public static final long WKMASK = 0x60L;
	public static final long WQMASK = 0xeL;
	public static final long BKMASK = 0x6000000000000000L;
	public static final long BQMASK = 0xe00000000000000L;
	
	public static long getRowMask(int pos) {
		return rowMasks[pos >>> 3];
	}
	
	public static long getFileMask(int pos) {
		return fileMasks[pos & 7];
	}
	
	public static long getDiagonalMask(int pos) {
		int rankIndex = pos >>> 3;
		int fileIndex = pos & 7;
		return diagonalMasks[(rankIndex - fileIndex) & 15];
	}
	
	public static long getAntiDiagonalMask(int pos) {
		int rankIndex = pos >>> 3;
		int fileIndex = pos & 7;
		return antiDiagonalMasks[(rankIndex + fileIndex) ^ 7];
	}
	
	public static long getLeftFileMask(int pos) {
		return (getFileMask(pos) >>> 1) & ~getRowMask(7);
	}
	
	public static long getRightFileMask(int pos) {
		return (getFileMask(pos) << 1) & ~getRowMask(0);
	}
	
	public static int getLSB(long bitBoard) {
		return Long.numberOfTrailingZeros(bitBoard);
	}
	
	public static int getMSB(long bitBoard) {
		return Long.numberOfLeadingZeros(bitBoard);
	}
	
	public static int getPopCount(long bitBoard) {
		return Long.bitCount(bitBoard);
	}
	
	public static long removeLSB(long bitBoard) {
		return bitBoard & (bitBoard-1);
	}
	
	public static long northFill(long bitBoard) {
	   bitBoard |= (bitBoard <<  8);
	   bitBoard |= (bitBoard << 16);
	   bitBoard |= (bitBoard << 32);
	   return bitBoard;
	}
	
	public static long southFill(long bitBoard) {
		bitBoard |= (bitBoard >>>  8);
		bitBoard |= (bitBoard >>> 16);
		bitBoard |= (bitBoard >>> 32);
		return bitBoard;
	}
	
	public static long fileFill(long bitBoard) {
		return northFill(bitBoard) | southFill(bitBoard);
	}
	
	public static long frontSpan(long bitBoard, int color) {
		if (color == Piece.WHITE) {
			return northFill(bitBoard << 8);
		}
		return southFill(bitBoard >>> 8);
	}
	
	public static long interSpan(long wBB, long bBB) {
		return frontSpan(wBB, Piece.WHITE) | frontSpan(bBB, Piece.BLACK);
	}
	
	public static void print(long bb) {
		String result = Long.toBinaryString(bb);
		while (result.length() < 64) {
			result = "0" + result;
		}

		for (int i = 8; i <= 64; i += 8) {
			for (int j = 1; j <= 8; j++) {
				System.out.print(result.charAt(i - j) + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	private BitBoard() {};
}