package gomoku;

import java.util.PriorityQueue;

public class BoardEvaluator {
	
	private int[][] gameState;
	
	public BoardEvaluator(int[][] gameState) {
		for (int i=0; i<15; i++) {
			for (int j=0; j<15; j++) {
				this.gameState[i][j] = gameState[i][j];
			}
		}
	}
	
	public PriorityQueue<GomokuMove> evaluateBoard(){
		
	}
}
