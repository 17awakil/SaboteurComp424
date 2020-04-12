package student_player;

import boardgame.Move;
import java.util.*;
import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.*;
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

        public Node(int row, int col){
            this.pos = new int[] { row, col };
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

    // offset of the edges from the center of the tile path
    private int[][] edges = new int[][] { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };

    // priorities of the tiles to allow better selection
    private int[] tilePriorities = new int[] { 16, 100, 100, 100, 100, 16, 8, 16, 0, 8, 16, 100, 100, 100, 100, 100 };

    private SaboteurBoardState boardState;

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

    private int shortestManhattanDistanceOfTile(SaboteurTile tile, int[] posIntBoard) {
        int min = Integer.MAX_VALUE;
        int[][] path = tile.getPath();

        for (int[] edge : this.edges) {
            if (path[1 + edge[0]][1 + edge[1]] != 1)
                continue;

            int[] intBoardOffset = MyTools.mapPathOffsetToIntBoardOffset(edge);

            int dist = manhattanIntDistance(posIntBoard[0] + intBoardOffset[0], posIntBoard[1] + intBoardOffset[1]);
            min = min > dist ? dist : min;

        }

        return min;
    }
    public int getTilePriority(SaboteurTile tile){ 
        if(tile.getIdx().length() > 1 && Character.isDigit(tile.getIdx().charAt(0)) && Character.isDigit(tile.getIdx().charAt(1))){
            char c1 = tile.getIdx().charAt(0);
            char c2 = tile.getIdx().charAt(1);
            String s = "" + c1 + c2;
            int index = Integer.parseInt(s);
            return tilePriorities[index];
        }
        else if(Character.isDigit(tile.getIdx().charAt(0))){
            return tilePriorities[Character.getNumericValue(tile.getIdx().charAt(0))];
        }
        return -1;
    }
    public int calculateMoveGoodness(SaboteurMove m){
        return manhattanDistance(m.getPosPlayed()) + getTilePriority((SaboteurTile)m.getCardPlayed());
    }

    public int[] findTileToDestroy(SaboteurTile[][] board){
        int originPosition = SaboteurBoardState.originPos;

        // queue implemented using LinkedList & HashSet to keep track of visited nodes
        // (to prevent nodes from being visited more than once)
        LinkedList<Node> queue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();

        // if there exists a path from the current target to the starting point, then let's search for a
        // disconnected tile from the targetPos
        if(existsPathFromTargetToOrigin(board))
            queue.add(new Node(posCurrentTarget[0], posCurrentTarget[1]));
        //otherwise, let's search from the origin position
        else
            queue.add(new Node(originPosition, originPosition));

        int[] startingCoords = {queue.getFirst().pos[0], queue.getFirst().pos[1]};

        while(!queue.isEmpty()){
            // pop the best choice so far
            Node current = queue.removeFirst();

            // get the current tile
            SaboteurTile currentTile = board[current.pos[0]][current.pos[1]];

            // visit the node
            visited.add(Arrays.toString(current.pos));

            int tilePriority = getTilePriority(currentTile);
            if( current.pos[0] != startingCoords[0] && current.pos[1]!= startingCoords[1] && 
             tilePriority > 50 ){
                return new int[] {current.pos[0], current.pos[1]};
            }

            // pruned valid neighbours
            ArrayList<int[]> neighbours = MyTools.getNeighbours(current.pos);
            // count the number of closer neighbours to the target
            for (int[] neighbour : neighbours) {
                // stringify position
                String neighbourPos = Arrays.toString(neighbour);
            
                // validate that the position has not been visited before, is a valid
                // neighbour, and parent & child tiles are connected
                if (board[neighbour[0]][neighbour[1]] != null && !visited.contains(neighbourPos)) {
            
                // visit node
                visited.add(neighbourPos);
            
                // add the node to the queue
                Node neighbourNode = new Node(neighbour[0], neighbour[1], current.cost + 1);
                //add first (DFS)
                queue.addFirst(neighbourNode);
                }
            }
        }
        return new int[] {-1,-1};
    }
    
    public boolean existsPathFromTargetToOrigin(SaboteurTile[][] board){

        // queue implemented using LinkedList & HashSet to keep track of visited nodes
        // (to prevent nodes from being visited more than once)
        LinkedList<Node> queue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();

        queue.add(new Node(posCurrentTarget[0], posCurrentTarget[0]));

        while(!queue.isEmpty()){
            // pop the best choice so far
            Node current = queue.removeFirst();

            // get the current tile
            SaboteurTile currentTile = board[current.pos[0]][current.pos[1]];

            // visit the node
            visited.add(Arrays.toString(current.pos));

            if(current.pos[0] == SaboteurBoardState.originPos && current.pos[1] == SaboteurBoardState.originPos){
                return true;
            }
            
            // pruned valid neighbours
            ArrayList<int[]> neighbours = MyTools.getNeighbours(current.pos);
            // count the number of closer neighbours to the target
            for (int[] neighbour : neighbours) {
                // stringify position
                String neighbourPos = Arrays.toString(neighbour);
            
                // validate that the position has not been visited before, is a valid
                // neighbour, and parent & child tiles are connected
                if (board[neighbour[0]][neighbour[1]] != null && !visited.contains(neighbourPos)) {
            
                // visit node
                visited.add(neighbourPos);
            
                // add the node to the queue
                Node neighbourNode = new Node(neighbour[0], neighbour[1]);
                queue.addFirst(neighbourNode);
                }
            }
        }
        return false;
    }

    // determines whether or not the cards in the hand or worth playing or not
    // returns true if it is best to drop a card
    public SaboteurMove assessHand(ArrayList<SaboteurMove> hand){
        SaboteurMove worstMove = hand.get(0);
        SaboteurMove bestMove = null;
        for(SaboteurMove m : hand){
            if(m.getCardPlayed() instanceof SaboteurTile){

                if(getTilePriority((SaboteurTile)(m.getCardPlayed())) > getTilePriority((SaboteurTile)(worstMove.getCardPlayed()))){
                    worstMove = m;
                }
                if(getTilePriority((SaboteurTile)(m.getCardPlayed())) < 50){
                    bestMove = m;
                }
            }
        }
        //if there are no tiles with priority below 50, then all the tiles are bad
        if(bestMove == null){
            //drop the one with the highest tile priority
            int indexOf = hand.indexOf(worstMove);
            return new SaboteurMove(new SaboteurDrop(), indexOf, 0, boardState.getTurnPlayer());
        }
        return null;
    }
    /**
     * returns a list of k ascending coordinates by manhattan distance
     * 
     * @param board the board represented as a matrix of saboteur tiles
     * @return a sorted list of closest coordinates increasing in manhattan distance
     */
    public ArrayList<int[]> kClosest(SaboteurTile[][] board) {
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

                // validate that the position has not been visited before, is a valid
                // neighbour, and parent & child tiles are connected
                if (board[neighbour[0]][neighbour[1]] != null && !visited.contains(neighbourPos)) {

                    // visit node
                    visited.add(neighbourPos);

                    // add the node to the queue
                    Node neighbourNode = new Node(neighbour[0], neighbour[1], current.cost + 1);
                    queue.addLast(neighbourNode);

                    if (kClosest.size() < K_VALUE)
                        kClosest.add(neighbour);

                    // check if the neighbour is a closer neighbour, if so update closest node
                    else if (neighbourNode.heuristic < manhattanDistance(kClosest.peek())) {
                        kClosest.poll();
                        kClosest.add(neighbour);
                    }
                }
            }
        }

        ArrayList<int[]> toReturn = new ArrayList<>(kClosest);
        toReturn.sort((int[] a, int[] b) -> manhattanDistance(a) - manhattanDistance(b));

        return toReturn;
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(SaboteurBoardState boardState) {
        this.boardState = boardState;
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

        // TODO: if turnnumber < manhattan distance to goal, then drop useless cards
        // while opponent builds towards goal for us

        int playerNumber = boardState.getTurnPlayer();

        // used for debugging
        ArrayList<SaboteurCard> hand = boardState.getCurrentPlayerCards();
        // sort the hand by ascending priority (worst cards at the end)
        hand.sort((SaboteurCard a, SaboteurCard b) -> {
            if (!(a instanceof SaboteurTile))
                return -1;
            if (!(b instanceof SaboteurTile))
                return 1;

            int priorityA = getTilePriority((SaboteurTile) a);
            int priorityB = getTilePriority((SaboteurTile) b);;

            return priorityA - priorityB;
        });

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
        // if we have a malus card, play it
        if (malus != null)
            return new SaboteurMove(malus, 0, 0, playerNumber);

        // play the map card if we have it and there is no current win condition
        if (map != null && boardState.getTurnNumber() < manhattanDistance(SaboteurBoardState.originPos,
                SaboteurBoardState.originPos))
            return new SaboteurMove(map, this.posCurrentTarget[0], this.posCurrentTarget[1], this.player_id);

        int[] destroyCoords = findTileToDestroy(boardState.getHiddenBoard()); 
        if(destroy != null && !Arrays.equals(destroyCoords,new int[] {-1,-1})){
            return new SaboteurMove(new SaboteurDestroy(),destroyCoords[0], destroyCoords[1], playerNumber); 
        }

        // check if we are malused
        if (boardState.getNbMalus(playerNumber) > 0) {
            // check to see if we have a bonus card to play, if yes play
            if (bonus != null)
                return new SaboteurMove(bonus, 0, 0, playerNumber);

            // if we have a destroy card and a tile that should be destroyed, destroy it
            if(destroy != null && !Arrays.equals(destroyCoords,new int[] {-1,-1})){
                return new SaboteurMove(new SaboteurDestroy(),destroyCoords[0], destroyCoords[1], playerNumber); 
            }

            // check to see if we have a map card to play and we don't currently know where
            // the nugget is, if yes play
            if (map != null
                    && displayBoard[this.posCurrentTarget[0]][this.posCurrentTarget[1]].getName().contains("goalTile"))
                return new SaboteurMove(map, this.posCurrentTarget[0], this.posCurrentTarget[1], playerNumber);

            else if (map != null) {
                // if we know where the nugget is and we have a map card, the map card is
                // rendered obsolete
                return new SaboteurMove(new SaboteurDrop(), hand.indexOf(map), 0, playerNumber);
            }



            // Check that hand size is at least 1 
            if(hand.size() >= 1)          
                // process the move to drop a random card
                return (new SaboteurMove(new SaboteurDrop(), hand.size() - 1, 0, boardState.getTurnPlayer()));

        }

        // retrieve the list of legal moves
        ArrayList<SaboteurMove> legalMoves = boardState.getAllLegalMoves();

        // check if there are any worthwile cards to play
        // if there aren't, drop the worst card
        SaboteurMove dropMove = assessHand(legalMoves);
        if(dropMove != null && boardState.isLegal(dropMove)){
            return dropMove;
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

        // sort from the closest positions to the target
        ArrayList<int[]> kClosestSorted = this.kClosest(board);

        // store all of our potential plays
        ArrayList<SaboteurMove> potentialMoves = new ArrayList<>();

        // print all the k closest coordinates
        for (int[] parentCoord : kClosestSorted) {
            // neighbours of one of the k closest positions that get us closer to the goal
            ArrayList<int[]> potentialCandidates = MyTools.getNeighbours(parentCoord);

            int[] posParentIntBoard = new int[] { parentCoord[0] * 3 + 1, parentCoord[1] * 3 + 1 };

            // pruned for child coordinates (where tiles dont already exist)
            HashSet<String> prunedCandidates = new HashSet<>();

            // stringify the neighbours and insert into set (e.g. "[6, 5]", "[5, 5]", "[5,
            // 6]")
            for (int[] candidate : potentialCandidates) {
                if (boardState.getHiddenBoard()[candidate[0]][candidate[1]] == null)
                    prunedCandidates.add(Arrays.toString(candidate));
            }

            for (String childKey : prunedCandidates) {
                ArrayList<SaboteurMove> legalMovesAtCoord = coordToMoves.get(childKey);

                // if there are no legal moves at the coordinate, skip
                if (legalMovesAtCoord == null)
                    continue;

                for (SaboteurMove potentialMoveAtCoord : legalMovesAtCoord) {
                    // if the potential move is not a tile, skip
                    if (!(potentialMoveAtCoord.getCardPlayed() instanceof SaboteurTile))
                        continue;

                    // get the tile thats at coord
                    SaboteurTile tileAtCoord = (SaboteurTile) potentialMoveAtCoord.getCardPlayed();

                    // get integer positions/coordinates in both the tile_board and int_board
                    int[] posChild = potentialMoveAtCoord.getPosPlayed();
                    int[] posChildIntBoard = new int[] { posChild[0] * 3 + 1, posChild[1] * 3 + 1 };

                    // if the tile has a wall in the middle, skip the tile (we don't want to
                    // consider it as a positive long-term investment)
                    if (tileAtCoord.getPath()[1][1] == 0)
                        continue;

                    int[] childConnectPoint = MyTools.getConnectedPoint(parentCoord, posChild);

                    // child and parent must be connected at some point; assertion
                    assert childConnectPoint != null;

                    int[] intBoardConnectionOffset = MyTools.mapPathOffsetToIntBoardOffset(childConnectPoint);

                    for (int[] offset : edges) {
                        if (tileAtCoord.getPath()[1 + offset[0]][1 + offset[1]] == 0
                                || MyTools.posEqual(offset, childConnectPoint))
                            continue;

                        // get intboard offset
                        int[] intBoardEdgeOffset = MyTools.mapPathOffsetToIntBoardOffset(offset);

                        if (manhattanIntDistance(posParentIntBoard[0] + intBoardEdgeOffset[0],
                                posParentIntBoard[1] + intBoardEdgeOffset[1]) <= manhattanIntDistance(
                                        posParentIntBoard[0] + intBoardConnectionOffset[0],
                                        posParentIntBoard[1] + intBoardConnectionOffset[1])) {
                            potentialMoves.add(potentialMoveAtCoord);
                            break;
                        }
                    }

                }
            }
        }

        potentialMoves.sort((SaboteurMove a, SaboteurMove b) -> {
            if (!(a.getCardPlayed() instanceof SaboteurTile))
                return 1;
            if (!(b.getCardPlayed() instanceof SaboteurTile))
                return -1;

            SaboteurTile aTile = (SaboteurTile) a.getCardPlayed();
            SaboteurTile bTile = (SaboteurTile) b.getCardPlayed();

            // shortest manhattan distance of both tile paths
            int shortestManhattanDistanceA = this.shortestManhattanDistanceOfTile(aTile,
                    new int[] { a.getPosPlayed()[0] * 3 + 1, a.getPosPlayed()[1] * 3 + 1 });

            int shortestManhattanDistanceB = this.shortestManhattanDistanceOfTile(bTile,
                    new int[] { b.getPosPlayed()[0] * 3 + 1, b.getPosPlayed()[1] * 3 + 1 });

            // priorities of the cards based on custom rating
            int priorityA = getTilePriority((SaboteurTile) aTile);;
            int priorityB = getTilePriority((SaboteurTile) bTile);;

            return (shortestManhattanDistanceA + priorityA) - (shortestManhattanDistanceB + priorityB);
        });

        for (SaboteurMove m : potentialMoves)
            if (boardState.isLegal(m))
                return m;

        // Return your move to be processed by the server.
        return boardState.getRandomMove();
    }
}