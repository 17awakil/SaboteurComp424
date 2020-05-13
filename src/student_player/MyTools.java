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

    public static int[] getConnectedPoint(int[] posParent, int[] posChild) {

        int parentX = posParent[0], parentY = posParent[1];
        int childX = posChild[0], childY = posChild[1];

        if (parentX == childX) {
            // if child & parent share the same row and parent column is greater, then
            // connection is to the right of the child
            if (parentY > childY)
                return new int[] { 1, 0 };
            // if child & parent share the same row and parent column is less, then
            // connection is to the left of the child
            else
                return new int[] { -1, 0 };
        }
        if (parentY == childY) {
            // if child & parent share the same column and parent row is greater, then
            // connection is to the bottom of the child
            if (parentX > childX)
                return new int[] { 0, -1 };

            // if child & parent share the same column and parent row is less, then
            // connection is to the top of the child
            else
                return new int[] { 0, 1 };
        }

        return null;
    }

    public static int[] mapPathOffsetToIntBoardOffset(int[] offset) {
        int[] newOffset = new int[] { offset[0], offset[1] };

        int temp = newOffset[0];
        newOffset[0] = newOffset[1];
        newOffset[1] = temp;
        newOffset[0] = -newOffset[0];

        return newOffset;
    }

    public static boolean posEqual(int[] a, int[] b) {
        return a[0] == b[0] && a[1] == b[1];
    }

}