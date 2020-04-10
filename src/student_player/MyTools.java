package student_player;

import java.util.ArrayList;
import Saboteur.SaboteurBoardState;
import Saboteur.cardClasses.SaboteurTile;

public class MyTools {
    public static double getSomething() {
        return Math.random();
    }

    private static boolean inbounds(int[] pos) {
        int row = pos[0], col = pos[1];

        return (row >= 0 && row < SaboteurBoardState.BOARD_SIZE && col >= 0 && col < SaboteurBoardState.BOARD_SIZE);
    }

    // get all inbound neighbours of a given position
    public static ArrayList<int[]> getNeighbours(int[] pos) {
        int row = pos[0];
        int col = pos[1];

        int[][] neighbours = new int[][] { { row + 1, col }, { row - 1, col }, { row, col - 1 }, { row, col + 1 } };

        ArrayList<int[]> inbounds = new ArrayList<>(4);
        for (int[] neighbour : neighbours)
            if (inbounds(neighbour))
                inbounds.add(neighbour);
        return inbounds;
    }

    public static boolean isConnected(SaboteurTile a, SaboteurTile b) {
        int[][] aPath = a.getPath();
        int[][] bPath = b.getPath();

        int aLeft = aPath[0][1], aRight = aPath[2][1], aBot = aPath[1][0], aTop = aPath[1][2], aCenter = aPath[1][1];

        int bLeft = bPath[0][1], bRight = bPath[2][1], bBot = bPath[1][0], bTop = bPath[1][2], bCenter = bPath[1][1];

        return (aTop == bBot && aTop == 1) || (aBot == bTop && aBot == 1) || (aLeft == bRight && aLeft == 1)
                || (aRight == bLeft && aRight == 1) && (aCenter == bCenter && aCenter == 1);
    }

}