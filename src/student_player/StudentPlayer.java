package student_player;

import boardgame.Move;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.SaboteurDrop;
import Saboteur.cardClasses.SaboteurTile;
import Saboteur.cardClasses.SaboteurCard;

import java.util.Random;
import java.util.stream.Collectors;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;

/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {
    private class Node {
        public int[] pos;
        public int cost;
        public int heuristic;

        public Node(int row, int col, int cost) {
            this.pos = new int[] { row, col };
            this.cost = cost;
            this.heuristic = StudentPlayer.this.manhattanDistance(row, col);
        }

        public int calc() {
            return cost + heuristic;
        }

        public boolean equals(Node other) {
            return this.pos[0] == other.pos[0] && this.pos[1] == other.pos[1];
        }
    }

    private boolean isFirstTime;
    private final int K_VALUE = 8;
    private SaboteurMove move;

    // the position of our nugget
    private int[] posCurrentTarget;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260733630");
        this.isFirstTime = true;
        this.move = null;
    }

    /**
     * calculate the manhattan distance from a location specified in params
     * 
     * @param row the row index
     * @param col the col index
     * @return the manhattan distance to our current target (presumed nugget
     *         location)
     */
    private int manhattanDistance(int row, int col) {
        return Math.abs(row - posCurrentTarget[0]) + Math.abs(col - posCurrentTarget[1]);
    }

    private int manhattanDistance(int[] coordinates) {
        return manhattanDistance(coordinates[0], coordinates[1]);
    }

    private int manhattanIntDistance(int row, int col) {
        return Math.abs(row - (posCurrentTarget[0] * 3 + 1)) + Math.abs(col - (posCurrentTarget[1] * 3 + 1));
    }

    private int manhattanIntDistance(int[] coordinates) {
        return manhattanIntDistance(coordinates[0], coordinates[1]);
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(SaboteurBoardState boardState) {
        SaboteurTile[][] displayBoard = boardState.getBoardForDisplay();

        // during first execution, do some extra math to locate the most optimal target
        if (this.isFirstTime) {
            this.isFirstTime = false;
            this.posCurrentTarget = SaboteurBoardState.hiddenPos[1];
        } else {
            // check if we have new information about hidden tiles

            // retrieve the tiles displayed from our persective

            // do we need to find a new target (because our current target has been
            // revealed)
            boolean needNewTarget = !displayBoard[posCurrentTarget[0]][posCurrentTarget[1]].getName()
                    .contains("goalTile");

            // check if we know where the nugget is
            for (int[] pos : SaboteurBoardState.hiddenPos) {
                String tileName = displayBoard[pos[0]][pos[1]].getName();
                if (tileName.contains("nugget")) {
                    this.posCurrentTarget = pos;
                    break;
                }
                if (needNewTarget && tileName.contains("goalTile"))
                    this.posCurrentTarget = pos;
            }
        }

        int playerNumber = boardState.getTurnNumber();

        // used for debugging
        ArrayList<SaboteurCard> hand = boardState.getCurrentPlayerCards();

        // keep track of the cards we can possibly play during malus
        SaboteurCard bonus = null, map = null, destroy = null, malus = null;

        // check for unique cards in the deck (non-tile cards)
        for (SaboteurCard card : hand) {
            // retrieve the card name
            String cardName = card.getName();
            // if the card in our hand is a bonus card, play it immediately
            if (cardName.equals("Bonus"))
                bonus = card;
            else if (cardName.equals("Map"))
                map = card;
            else if (cardName.equals("Destroy"))
                destroy = card;
            else if (cardName.equals("Malus"))
                malus = card;
        }

        // play the map card if we have it and there is no current win condition
        if (map != null && boardState.getTurnNumber() < manhattanDistance(SaboteurBoardState.originPos,
                SaboteurBoardState.originPos))
            return new SaboteurMove(map, this.posCurrentTarget[0], this.posCurrentTarget[1], this.player_id);

        // if we have a malus card, play it
        if (malus != null)
            return new SaboteurMove(malus, 0, 0, playerNumber);

        // check if we are malused
        if (boardState.getNbMalus(playerNumber) > 0) {
            // check to see if we have a bonus card to play, if yes play
            if (bonus != null)
                return new SaboteurMove(bonus, 0, 0, playerNumber);

            // check to see if we have a map card to play and we don't currently know where
            // the nugget is, if yes play
            if (map != null
                    && displayBoard[this.posCurrentTarget[0]][this.posCurrentTarget[1]].getName().contains("goalTile"))
                return new SaboteurMove(map, this.posCurrentTarget[0], this.posCurrentTarget[1], playerNumber);

            else if (map != null) {
                // if we know where the nugget is and we have a map card, the map card is
                // rendered obsolete
                // TODO: drop the map card in the deck
            }

            // TODO: remove the least useful cards from the deck vs dropping random card

            // otherwise we have no bonus card and must drop a random card
            Random rand = new Random();

            // generate the random index we'll be dropping from our deck
            int randomIndex = rand.nextInt(boardState.getCurrentPlayerCards().size());

            // process the move to drop a random card
            return (new SaboteurMove(new SaboteurDrop(), randomIndex, 0, boardState.getTurnPlayer()));
        }

        // retrieve the list of legal moves
        ArrayList<SaboteurMove> legalMoves = boardState.getAllLegalMoves();
        int numLegalMoves = legalMoves.size();

        // if there are no legal moves, drop a random card
        if (numLegalMoves == 0) {
            // TODO: check if dropping a card counts as a legal move
            Random rand = new Random();

            // generate the random index we'll be dropping from our deck
            int randomIndex = rand.nextInt(boardState.getCurrentPlayerCards().size());

            // process the move to drop a random card
            return (new SaboteurMove(new SaboteurDrop(), randomIndex, 0, boardState.getTurnPlayer()));
        }

        // map all coordinates to their legal moves
        // e.g. ("[2, 5]" -> [move1, move2, ...])
        HashMap<String, ArrayList<SaboteurMove>> coordToMoves = new HashMap<>();

        for (SaboteurMove move : legalMoves) {
            // construct the key
            String key = Arrays.toString(move.getPosPlayed());

            // get the value associate with key, defaulted to new array reference
            ArrayList<SaboteurMove> val = coordToMoves.getOrDefault(key, new ArrayList<>());

            // add move to the associated value
            val.add(move);

            // update key
            coordToMoves.put(key, val);
        }

        // there is at least one legal move to do, our goal is to pick the best one
        SaboteurTile[][] board = boardState.getHiddenBoard();

        // get the original position
        int originPosition = SaboteurBoardState.originPos;

        // queue implemented using LinkedList & HashSet to keep track of visited nodes
        // (to prevent nodes from being visited more than once)
        LinkedList<Node> queue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();

        // add the origin to the queue
        queue.add(new Node(originPosition, originPosition, 0));

        // find the k closest points to the target/destination (presumed nugget)
        PriorityQueue<int[]> kClosest = new PriorityQueue<>(
                (int[] a, int[] b) -> manhattanDistance(b) - manhattanDistance(a));

        // add root to the closest points
        kClosest.add(new int[] { originPosition, originPosition });

        // while the queue is not empty
        while (!queue.isEmpty()) {
            // pop the best choice so far
            Node current = queue.removeFirst();

            // get the current tile
            SaboteurTile currentTile = board[current.pos[0]][current.pos[1]];

            // visit the node
            visited.add(Arrays.toString(current.pos));

            // pruned valid neighbours
            ArrayList<int[]> neighbours = MyTools.getNeighbours(current.pos);

            // count the number of closer neighbours to the target
            for (int[] neighbour : neighbours) {
                // stringify position
                String neighbourPos = Arrays.toString(neighbour);
                SaboteurTile neighbourTile = board[neighbour[0]][neighbour[1]];

                // validate that the position has not been visited before, is a valid
                // neighbour, and parent & child tiles are connected
                if (board[neighbour[0]][neighbour[1]] != null && !visited.contains(neighbourPos)
                        && MyTools.isConnected(currentTile, neighbourTile)) {

                    // visit node
                    visited.add(neighbourPos);

                    // add the node to the queue
                    Node neighbourNode = new Node(neighbour[0], neighbour[1], current.cost + 1);
                    queue.addLast(neighbourNode);

                    if (kClosest.size() < K_VALUE)
                        kClosest.add(neighbour);

                    // check if the neighbour is a closer neighbour, if so update closest node
                    else if (neighbourNode.heuristic <= manhattanDistance(kClosest.peek())) {
                        kClosest.poll();
                        kClosest.add(neighbour);
                    }
                }
            }
        }

        // offset of the edges from the center
        int[][] edges = new int[][] { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };

        // sort from the closest positions to the target
        ArrayList<int[]> kClosestSorted = new ArrayList<>(kClosest);
        kClosestSorted.sort((int[] a, int[] b) -> manhattanDistance(a) - manhattanDistance(b));

        // store all of our potential plays
        ArrayList<SaboteurMove> potentialMoves = new ArrayList<>();

        // print all the k closest coordinates
        for (int[] coord : kClosestSorted) {
            // neighbours of one of the k closest positions that get us closer to the goal
            ArrayList<int[]> potentialCandidates = MyTools.getNeighbours(coord);

            // pruned for coordinates where tiles dont already exist)
            HashSet<String> prunedCandidates = new HashSet<>();

            // stringify the neighbours and insert into set (e.g. "[6, 5]", "[5, 5]", "[5,
            // 6]")
            for (int[] candidate : potentialCandidates) {
                if (boardState.getHiddenBoard()[candidate[0]][candidate[1]] == null)
                    prunedCandidates.add(Arrays.toString(candidate));
            }

            for (String coordKey : prunedCandidates) {
                ArrayList<SaboteurMove> legalMovesAtCoord = coordToMoves.get(coordKey);

                // if there are no legal moves at the coordinate, skip
                if (legalMovesAtCoord == null)
                    continue;

                for (SaboteurMove moveAtCoord : legalMovesAtCoord) {
                    // if the potential move is not a tile, skip
                    if (!(moveAtCoord.getCardPlayed() instanceof SaboteurTile))
                        continue;

                    // get the tile thats at coord
                    SaboteurTile tileAtCoord = (SaboteurTile) moveAtCoord.getCardPlayed();

                    // get integer positions/coordinates in both the tile_board and int_board
                    int[] pos = moveAtCoord.getPosPlayed();
                    int[] posIntBoard = new int[] { pos[0] * 3 + 1, pos[1] * 3 + 1 };

                    // if the tile has a wall in the middle, skip the tile (we don't want to
                    // consider it as a positive long-term investment)
                    if (tileAtCoord.getPath()[1][1] == 0)
                        continue;

                    // the position/coordinate of the square within the intboard that corresponds to
                    // the connection to the parent tile
                    int[] connection = new int[2];

                    if (moveAtCoord.getPosPlayed()[0] > coord[0])
                        // if the new card is below the parent card, the connection must be the left
                        // edge
                        connection = edges[1];

                    if (moveAtCoord.getPosPlayed()[0] < coord[0])
                        // if the new card is above the parent card, the connection must be the bottom
                        // edge
                        connection = edges[3];

                    if (moveAtCoord.getPosPlayed()[1] > coord[1])
                        // if the new card is to the right of the parent card, the connection must be
                        // the left edge
                        connection = edges[0];

                    if (moveAtCoord.getPosPlayed()[1] < coord[1])
                        // if the new card is to the left of the parent card, the connection must be the
                        // right edge
                        connection = edges[2];

                    for (int[] offset : edges) {
                        if (boardState.getHiddenIntBoard()[posIntBoard[0] + offset[0]][posIntBoard[1] + offset[1]] == 0
                                || connection == offset)
                            continue;

                        if (manhattanIntDistance(posIntBoard[0] + offset[0],
                                posIntBoard[1] + offset[1]) < manhattanIntDistance(posIntBoard[0] + connection[0],
                                        posIntBoard[1] + connection[1])) {
                            potentialMoves.add(moveAtCoord);
                            break;
                        }
                    }

                }
            }
        }

        potentialMoves.sort((SaboteurMove a, SaboteurMove b) -> manhattanDistance(a.getPosPlayed())
                - manhattanDistance(b.getPosPlayed()));

        for (SaboteurMove m : potentialMoves)
            if (boardState.isLegal(m))
                return m;

        // Return your move to be processed by the server.
        return boardState.getRandomMove();
    }
}