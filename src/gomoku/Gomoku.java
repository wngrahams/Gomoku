package gomoku;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class Gomoku {
	public static final int EMPTY = 0b11111111111111111111111111111111;
	public static final int WHITE = 0b00;
	public static final int BLACK = 0b01;
	
	public static final int GAME_NOT_OVER = 0b00;
	public static final int GAME_OVER = 0b01;
	
	public static String colorAsString(int color) {
		if (color == WHITE)
			return "White";
		else if (color == BLACK)
			return "Black";
		else
			return "";
	}
	
	public static int[] isGameOver(int[][] gameState) {
		if (gameState.length != 15 || gameState[0].length != 15)
			throw new RuntimeException("Game must be 15x15");
		
		for (int i=0; i<15; i++) {
			for (int j=0; j<15; j++) {
				if (gameState[i][j] != EMPTY) {
					int potentialWinner = gameState[i][j];

					// check horizontal right
					if(checkRight(gameState, 5, potentialWinner, i, j))
						return new int[] {GAME_OVER, potentialWinner};
					
					// check diagonal down and right
					if(checkDownAndRight(gameState, 5, potentialWinner, i, j))
						return new int[] {GAME_OVER, potentialWinner};
					
					// check vertical down
					if(checkDown(gameState, 5, potentialWinner, i, j))
						return new int[] {GAME_OVER, potentialWinner};
					
					// check diagonal down and left
					if(checkDownAndLeft(gameState, 5, potentialWinner, i, j))
						return new int[] {GAME_OVER, potentialWinner};
				}
			}
		}
		
		return new int[] {GAME_NOT_OVER, EMPTY};
	}
	
	private static boolean checkDown(int[][] gameState, int amtToWin, int color, int startCol, int startRow) {
		amtToWin--;
		if (amtToWin <= 0)
			return true;
		if ((startRow+1) < 15) {
			if (gameState[startCol][startRow+1] == color)
				return checkDown(gameState, amtToWin, color, startCol, startRow+1);
			else
				return false;
		}
		else
			return false;
	}
	
	private static boolean checkDownAndLeft(int[][] gameState, int amtToWin, int color, int startCol, int startRow) {
		amtToWin--;
		if (amtToWin <= 0)
			return true;
		if ((startCol-1) >= 0 && (startRow+1) < 15) {
			if (gameState[startCol-1][startRow+1] == color)
				return checkDownAndLeft(gameState, amtToWin, color, startCol-1, startRow+1);
			else
				return false;
		}
		else
			return false;
	}
	
	private static boolean checkDownAndRight(int[][] gameState, int amtToWin, int color, int startCol, int startRow) {
		amtToWin--;
		if (amtToWin <= 0)
			return true;
		if ((startCol+1) < 15 && (startRow+1) < 15) {
			if (gameState[startCol+1][startRow+1] == color)
				return checkDownAndRight(gameState, amtToWin, color, startCol+1, startRow+1);
			else
				return false;
		}
		else
			return false;
	}
	
	private static boolean checkRight(int[][] gameState, int amtToWin, int color, int startCol, int startRow) {
		amtToWin--;
		if (amtToWin <= 0)
			return true;
		if ((startCol+1) < 15) {
			if (gameState[startCol+1][startRow] == color)
				return checkRight(gameState, amtToWin, color, startCol+1, startRow);
			else
				return false;
		}
		else
			return false;
	}

//	public static ArrayList<GomokuMove> findFours(int[][] gameState, int otherColor) {
//		ArrayList<GomokuMove> fours = new ArrayList<GomokuMove>();
//		ArrayList<GomokuMove> potentialFours = new ArrayList<GomokuMove>();
//		for (int i=0; i<15)
//		return null;
//	}
	
	public static PriorityQueue<Threat> findThreats(int[][] gameState, int otherColor) {
		PriorityQueue<Threat> threats = new PriorityQueue<Threat>();
		for (int i=0; i<15; i++) {
			for (int j=0; j<15; j++) {
				if (gameState[i][j] == otherColor) {
					Threat downThreat = getThreatDown(gameState, otherColor, new GomokuMove(otherColor, i, j));
					Threat downLeftThreat = getThreatDownLeft(gameState, otherColor, new GomokuMove(otherColor, i, j));
					Threat downRightThreat = getThreatDownRight(gameState, otherColor, new GomokuMove(otherColor, i, j));
					Threat rightThreat = getThreatRight(gameState, otherColor, new GomokuMove(otherColor, i, j));

					threats.add(downThreat);
					threats.add(downLeftThreat);
					threats.add(downRightThreat);
					threats.add(rightThreat);
				}
			}
		}
		
		return threats;
	}
	
	private static Threat getThreatDown(int[][] gameState, int color, GomokuMove startingPos) {
		
		ArrayList<GomokuMove> down = new ArrayList<GomokuMove>();
		int threatSize = getAmtDown(gameState, color, startingPos.getRow(), startingPos.getColumn()) + 1;
		
		down.add(startingPos);
		for (int i=1; i<threatSize; i++) {
			down.add(new GomokuMove(color, startingPos.getRow()+i, startingPos.getColumn()));
		}
		
		Threat threat = new Threat(down, Threat.VERTICAL);
		return threat;
	}
	
	private static int getAmtDown(int[][] gameState, int color, int startRow, int startCol) {
		try {
			if (gameState[startRow + 1][startCol] == color)
				return 1 + getAmtDown(gameState, color, startRow+1, startCol);
			else
				return 0;
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}
	}
	
	private static Threat getThreatDownLeft(int[][] gameState, int color, GomokuMove startingPos) {
		
		ArrayList<GomokuMove> down = new ArrayList<GomokuMove>();
		int threatSize = getAmtDownLeft(gameState, color, startingPos.getRow(), startingPos.getColumn()) + 1;
		
		down.add(startingPos);
		for (int i=1; i<threatSize; i++) {
			down.add(new GomokuMove(color, startingPos.getRow()+i, startingPos.getColumn()-i));
		}
		
		Threat threat = new Threat(down, Threat.UP_RIGHT);
		return threat;
	}
	
	private static int getAmtDownLeft(int[][] gameState, int color, int startRow, int startCol) {
		try {
			if (gameState[startRow + 1][startCol - 1] == color)
				return 1 + getAmtDownLeft(gameState, color, startRow+1, startCol-1);
			else
				return 0;
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}
	}
	
	private static Threat getThreatDownRight(int[][] gameState, int color, GomokuMove startingPos) {
		
		ArrayList<GomokuMove> down = new ArrayList<GomokuMove>();
		int threatSize = getAmtDownRight(gameState, color, startingPos.getRow(), startingPos.getColumn()) + 1;
		
		down.add(startingPos);
		for (int i=1; i<threatSize; i++) {
			down.add(new GomokuMove(color, startingPos.getRow()+i, startingPos.getColumn()+i));
		}
		
		Threat threat = new Threat(down, Threat.DOWN_RIGHT);
		return threat;
	}
	
	private static int getAmtDownRight(int[][] gameState, int color, int startRow, int startCol) {
		try {
			if (gameState[startRow + 1][startCol + 1] == color)
				return 1 + getAmtDownRight(gameState, color, startRow+1, startCol+1);
			else
				return 0;
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}
	}
	
	private static Threat getThreatRight(int[][] gameState, int color, GomokuMove startingPos) {
		
		ArrayList<GomokuMove> down = new ArrayList<GomokuMove>();
		int threatSize = getAmtRight(gameState, color, startingPos.getRow(), startingPos.getColumn()) + 1;
		
		down.add(startingPos);
		for (int i=1; i<threatSize; i++) {
			down.add(new GomokuMove(color, startingPos.getRow(), startingPos.getColumn()+i));
		}
		
		Threat threat = new Threat(down, Threat.HORIZONTAL);
		return threat;
	}
	
	private static int getAmtRight(int[][] gameState, int color, int startRow, int startCol) {
		try {
			if (gameState[startRow][startCol + 1] == color)
				return 1 + getAmtRight(gameState, color, startRow, startCol+1);
			else
				return 0;
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}
	}
	
	public static GomokuMove respondToThreat(int[][] gameState, Threat t, int otherColor) {
		ArrayList<GomokuMove> costSquares = t.getCostSquares();
		for (int i=0; i<costSquares.size(); i++) {
			// TODO: inefficient: why return a cost square if it's already filled
			if (gameState[costSquares.get(i).getRow()][costSquares.get(i).getColumn()] == EMPTY)
				return costSquares.get(i);
		}
		
		return null;
	}
}
