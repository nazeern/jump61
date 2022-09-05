# Jump61
A command line strategy game inspired by the Jumpin’ Cubes game on the Commodore 64. Features an alpha-beta pruning AI opponent that finds and executes forced wins at a depth of four.

You can play a browser version of this game at [this site](http://www.playonlinedicegames.com/jumpingcube) (external link).

## Summary of gameplay:
- The game is a turn-based game between two players, red and green, with an N x N board. 
- Squares are either colored green, red, or white. Green squares belong to player green, Red squares belong to player red, and white squares are unclaimed.

- Initially, each square is white and holds one pellet.
- When a player clicks a square that either belongs to them or is unclaimed (white), it adds a pellet and becomes claimed.
- Squares can only hold as many pellets as the number of neighboring squares they have: 
  - corner squares have two neighbors and can only hold two pellets.
  - edge squares have three neighbor and can hold three pellets.
  - all other squares have four neighbors and can hold four pellets.
  
- If a player adds a pellet to a square and exceeds the square's capacity, the square gives one pellet to each of its neighbors and claims the neighbor. 
The original square is left with just one pellet.
  - This process continues if the neighbors exceed capacity, causing a "wave" of recursion
  
- A player wins if they claim all the squares on the board.
  
# I hope you enjoy playing this game as much as I enjoyed making it!
