# Ducc0
Chess Engine written in Java

Play Ducc0 at https://lichess.org/@/Ducc0

Limited UCI Support

Supports basic UCI commands needed to run on lichess

(go wtime btime winc binc)

Features:
  -
  - BitBoards
  - Move Generation with Kindergarden BitBoards
      - Maybe switch to magic bitboards in the future, KBB improved movegen well enough for now
  - Tapered Evaluation
  - Principal Variation Search
  - Move Ordering with Hash Move + LVV-MVA + Killer Move
  - Transposition Table
  - Killer Move Heuristic
  - Check Extensions (110 elo gain)

More updates in the future
  - Null move pruning (Hopefully big elo gain)
  - Better evaluation
      - Mobility
      - Pawn Structure
      - King Safety

Notes:
  - 
  - Very high branching factor (hopefully pruning implementations can lower this)
  - Doesn't seem to improve much on lichess despite cutechess sprt testing indicating significant elo gains
  - (May just be because of challenging better opponents)

RESOURCES: 
  -
  - Chess Programming Wiki -> https://www.chessprogramming.org/Main_Page
  - Good Explanations -> https://web.archive.org/web/20071026090003/www.brucemo.com/compchess/programming/index.htm
  - CUTECHESS -> https://github.com/cutechess/cutechess
