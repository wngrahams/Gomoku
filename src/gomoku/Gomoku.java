package gomoku;

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
}
