package engine;

public class Tables {
    public static final long[][] pawnAttacks = {{
        0x0000000000000200L, 0x0000000000000500L, 0x0000000000000a00L, 0x0000000000001400L, 
        0x0000000000002800L, 0x0000000000005000L, 0x000000000000a000L, 0x0000000000004000L, 
        0x0000000000020000L, 0x0000000000050000L, 0x00000000000a0000L, 0x0000000000140000L, 
        0x0000000000280000L, 0x0000000000500000L, 0x0000000000a00000L, 0x0000000000400000L, 
        0x0000000002000000L, 0x0000000005000000L, 0x000000000a000000L, 0x0000000014000000L, 
        0x0000000028000000L, 0x0000000050000000L, 0x00000000a0000000L, 0x0000000040000000L, 
        0x0000000200000000L, 0x0000000500000000L, 0x0000000a00000000L, 0x0000001400000000L, 
        0x0000002800000000L, 0x0000005000000000L, 0x000000a000000000L, 0x0000004000000000L,
        0x0000020000000000L, 0x0000050000000000L, 0x00000a0000000000L, 0x0000140000000000L, 
        0x0000280000000000L, 0x0000500000000000L, 0x0000a00000000000L, 0x0000400000000000L, 
        0x0002000000000000L, 0x0005000000000000L, 0x000a000000000000L, 0x0014000000000000L, 
        0x0028000000000000L, 0x0050000000000000L, 0x00a0000000000000L, 0x0040000000000000L, 
        0x0200000000000000L, 0x0500000000000000L, 0x0a00000000000000L, 0x1400000000000000L, 
        0x2800000000000000L, 0x5000000000000000L, 0xa000000000000000L, 0x4000000000000000L, 
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L
    },
    
    {
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000002L, 0x0000000000000005L, 0x000000000000000aL, 0x0000000000000014L, 
        0x0000000000000028L, 0x0000000000000050L, 0x00000000000000a0L, 0x0000000000000040L, 
        0x0000000000000200L, 0x0000000000000500L, 0x0000000000000a00L, 0x0000000000001400L, 
        0x0000000000002800L, 0x0000000000005000L, 0x000000000000a000L, 0x0000000000004000L, 
        0x0000000000020000L, 0x0000000000050000L, 0x00000000000a0000L, 0x0000000000140000L, 
        0x0000000000280000L, 0x0000000000500000L, 0x0000000000a00000L, 0x0000000000400000L, 
        0x0000000002000000L, 0x0000000005000000L, 0x000000000a000000L, 0x0000000014000000L, 
        0x0000000028000000L, 0x0000000050000000L, 0x00000000a0000000L, 0x0000000040000000L, 
        0x0000000200000000L, 0x0000000500000000L, 0x0000000a00000000L, 0x0000001400000000L, 
        0x0000002800000000L, 0x0000005000000000L, 0x000000a000000000L, 0x0000004000000000L, 
        0x0000020000000000L, 0x0000050000000000L, 0x00000a0000000000L, 0x0000140000000000L, 
        0x0000280000000000L, 0x0000500000000000L, 0x0000a00000000000L, 0x0000400000000000L, 
        0x0002000000000000L, 0x0005000000000000L, 0x000a000000000000L, 0x0014000000000000L, 
        0x0028000000000000L, 0x0050000000000000L, 0x00a0000000000000L, 0x0040000000000000L
    }
    };
    
    public static final long[] knightMoves = {
        0x0000000000020400L, 0x0000000000050800L, 0x00000000000A1100L, 0x0000000000142200L,
        0x0000000000284400L, 0x0000000000508800L, 0x0000000000A01000L, 0x0000000000402000L,
        0x0000000002040004L, 0x0000000005080008L, 0x000000000A110011L, 0x0000000014220022L,
        0x0000000028440044L, 0x0000000050880088L, 0x00000000A0100010L, 0x0000000040200020L,
        0x0000000204000402L, 0x0000000508000805L, 0x0000000A1100110AL, 0x0000001422002214L,
        0x0000002844004428L, 0x0000005088008850L, 0x000000A0100010A0L, 0x0000004020002040L,
        0x0000020400040200L, 0x0000050800080500L, 0x00000A1100110A00L, 0x0000142200221400L,
        0x0000284400442800L, 0x0000508800885000L, 0x0000A0100010A000L, 0x0000402000204000L,
        0x0002040004020000L, 0x0005080008050000L, 0x000A1100110A0000L, 0x0014220022140000L,
        0x0028440044280000L, 0x0050880088500000L, 0x00A0100010A00000L, 0x0040200020400000L,
        0x0204000402000000L, 0x0508000805000000L, 0x0A1100110A000000L, 0x1422002214000000L,
        0x2844004428000000L, 0x5088008850000000L, 0xA0100010A0000000L, 0x4020002040000000L,
        0x0400040200000000L, 0x0800080500000000L, 0x1100110A00000000L, 0x2200221400000000L,
        0x4400442800000000L, 0x8800885000000000L, 0x100010A000000000L, 0x2000204000000000L,
        0x0004020000000000L, 0x0008050000000000L, 0x00110A0000000000L, 0x0022140000000000L,
        0x0044280000000000L, 0x0088500000000000L, 0x0010A00000000000L, 0x0020400000000000L
    };
    
    public static final long[] kingMoves = {
        0x0000000000000302L, 0x0000000000000705L, 0x0000000000000E0AL, 0x0000000000001C14L,
        0x0000000000003828L, 0x0000000000007050L, 0x000000000000E0A0L, 0x000000000000C040L,
        0x0000000000030203L, 0x0000000000070507L, 0x00000000000E0A0EL, 0x00000000001C141CL,
        0x0000000000382838L, 0x0000000000705070L, 0x0000000000E0A0E0L, 0x0000000000C040C0L,
        0x0000000003020300L, 0x0000000007050700L, 0x000000000E0A0E00L, 0x000000001C141C00L,
        0x0000000038283800L, 0x0000000070507000L, 0x00000000E0A0E000L, 0x00000000C040C000L,
        0x0000000302030000L, 0x0000000705070000L, 0x0000000E0A0E0000L, 0x0000001C141C0000L,
        0x0000003828380000L, 0x0000007050700000L, 0x000000E0A0E00000L, 0x000000C040C00000L,
        0x0000030203000000L, 0x0000070507000000L, 0x00000E0A0E000000L, 0x00001C141C000000L,
        0x0000382838000000L, 0x0000705070000000L, 0x0000E0A0E0000000L, 0x0000C040C0000000L,
        0x0003020300000000L, 0x0007050700000000L, 0x000E0A0E00000000L, 0x001C141C00000000L,
        0x0038283800000000L, 0x0070507000000000L, 0x00E0A0E000000000L, 0x00C040C000000000L,
        0x0302030000000000L, 0x0705070000000000L, 0x0E0A0E0000000000L, 0x1C141C0000000000L,
        0x3828380000000000L, 0x7050700000000000L, 0xE0A0E00000000000L, 0xC040C00000000000L,
        0x0203000000000000L, 0x0507000000000000L, 0x0A0E000000000000L, 0x141C000000000000L,
        0x2838000000000000L, 0x5070000000000000L, 0xA0E0000000000000L, 0x40C0000000000000L
    };
    
    // 8 = one of the 8 from files
    // 64 = 2^6 different arrangements of bits for 6 squares on the first rank
    public static long[][] slidingAttackLookup = new long[8][64];
  
    /**
     * Does some stuff (scuffed sliding piece attack calculation)
     * Efficiency literally does not matter since this is for initialization
     * Method is called once at the start of the program
     */
    public static void computeFirstRankAttacks() {
    	// i represents the file the piece is on
    	for (int i = 0; i < 8; i++) {
    		long mask = ~(0x1L << i); // masks for the current piece
    		// j represents the pieces
    		for (int j = 0; j < 64; j++) {
    			// makes j 8 bits instead of 6 AND masks the piece
    			long bb = ((long)j << 1) & mask; 
    			int left  = 0; // closest bit to the left of piece
    			int right = 7; // closes bit to the right of piece
    			while (BitBoard.getLSB(bb) < i){
    				left = BitBoard.getLSB(bb);
    				bb &= bb-1; // clears last bit
    			}
    			right = BitBoard.getLSB(bb);
    			if (right == 64)
    				right = 7;
    			// constructs the sliding piece attack bitboard (on first rank)
    			long result = 0x0L;
    			for (int x = left; x <= right; x++) {
    				if (x != i) result |= 0x1 << x;
    			}
    			slidingAttackLookup[i][j] = result;
    		}
    	}
    }
    
    public static int[][] mgMobilityValues = {
    	{-30, -15, -8, -4, 0, 8, 16, 32, 32},
    	{-30, -20, -10, 0, 5, 10, 15, 20, 25, 25, 30, 30, 30, 35},
    	{-32, -24, -16, -8, 0, 4, 16, 17, 18, 19, 20, 28, 36, 45, 50},
    	{-24, -22, -20, -18, -15, -10, -8, -4, 0, 4, 8, 12, 16, 18,
    	 18, 20, 20, 22, 22, 24, 24, 26, 26, 32, 32, 32, 32, 32} 
    };
    
    // stuck pieces in the endgame are MUCH worse
    // too much mobility might be bad in the case of stopping/protecting pawns and such
    public static int[][] egMobilityValues = {
    	{-80, -35, 0, 4, 16, 34, 24, 24, 18},
    	{-90, -50, -10, -5, 0, 5, 10, 15, 20, 25, 35, 30, 25, 22},
    	{-150, -75, -32, -12, -4, 0, 8, 17, 22, 24, 24, 22, 20, 20, 14},
    	{-300, -200, -100, -50, -30, -30, -20, -15, -8, 0, 10, 12, 16, 18,
    	 18, 20, 20, 22, 22, 24, 24, 26, 32, 30, 24, 24, 24, 22} 
    };
    
    private Tables() {}
}