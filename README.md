# Ducc0
Chess Engine written in Java

Play Ducc0 at https://lichess.org/@/Ducc0

Limited UCI Support

Supports basic UCI commands needed to run on lichess

(go wtime btime winc binc)

Features:
  -
  - Move Generation with Kindergarden BitBoards
  		- Maybe switch to magic bitboards in the future, KBB improved movegen well enough for now
  		
  - Evaluation:
  - Tapered Evaluation
  - Mobility
  - Insufficient material
  - Open files
  - Passed Pawn
  - Bishop Pair
  
  - Search:
  - Iterative deepening with fail soft search
  - Null Move Pruning
  - Check Extensions
  - Principal Variation Search
  - Late Move Reductions with log formula
  - Move Ordering with Hash Move + MVV-LVA + History Heuristic
  - Transposition Table
  - Quiescence Search with delta pruning


More updates in the future
  - Better evaluation
	  - Pawn Structure
	  - King Safety
  - More Search Techniques
  	  - Static null move pruning
  	  - Late move pruning
  	  - History pruning
  	  - Counter moves history
  	  - Continuation history
  	  - Capture history
  	  - Aspiration Windows
  	  - Static exchange evaluation
  	  - Transposition table in quiescence search
  - NNUE in far future

RESOURCES: 
  -
  - Chess Programming Wiki -> https://www.chessprogramming.org/Main_Page
  - CUTECHESS -> https://github.com/cutechess/cutechess
  - Chess Programming YT -> https://www.youtube.com/@chessprogramming591
  
ACKNOWLEDGEMENTS:
  - Stockfish 
  - Ethereal (for its highly readable code)
  - Altair (based my mobility values off of)
