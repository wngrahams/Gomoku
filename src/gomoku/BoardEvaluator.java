package gomoku;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;

public class BoardEvaluator {
	
	private int[][] gameState = new int[15][15];
	private int myColor;
	private int otherColor;
	
	private static final int MAX_PRIORITY = 15*15;
	
	public BoardEvaluator(int[][] gameState, int color) {
		
		myColor = color;
		otherColor = (color - 1)*(-1);
		
		for (int i=0; i<15; i++) {
			for (int j=0; j<15; j++) {
				this.gameState[i][j] = gameState[i][j];
			}
		}
	}
	
	public PriorityQueue<GomokuMove> evaluateBoard() {
		PriorityQueue<GomokuMove> moveQueue = new PriorityQueue<GomokuMove>();
		
		int numPieces = getNumPiecesPlayed();
		GomokuMove moveToAdd;
		
		if(numPieces < 2){ 
			for (int i=0; i<15; i++) {
				for (int j=0; j<15; j++) {
					if (gameState[i][j] == Gomoku.EMPTY) {
						moveToAdd = new GomokuMove(myColor, i, j);
						moveToAdd.setPriority(2*49 - (int)(Math.pow((7-i), 2) + (int)(Math.pow((7-j), 2))));
						moveQueue.add(moveToAdd);
					}
				}
			}
		}
		else {
			
			ArrayList<GomokuMove> winningMoves = lookForWin();
			for(GomokuMove g : winningMoves)
				moveQueue.add(g);
			
			moveToAdd = playNecessaryDefense();
			if (moveToAdd != null) {
				moveQueue.add(moveToAdd);
			}
			
			moveToAdd = playOffense();
			if (moveToAdd != null) {
				moveQueue.add(moveToAdd);
			}
		}
		
		return moveQueue;
	}
	
	private int getNumPiecesPlayed() {
		int counter = 0;
		for (int i=0; i<15; i++) {
			for (int j=0; j<15; j++) {
				if (gameState[i][j] != Gomoku.EMPTY)
					counter++;
			}
		}
		
		return counter;
	}
	
	private ArrayList<GomokuMove> lookForWin() {
		ArrayList<GomokuMove> winningMoves = new ArrayList<GomokuMove>();
		
		int[][] boardCopy = new int[15][15];
		for (int i=0; i<15; i++)
			boardCopy[i] = gameState[i].clone();
		
		for (int i=0; i<15; i++) {
			for (int j=0; j<15; j++) {
				if (gameState[i][j] == Gomoku.EMPTY) {
					boardCopy[i][i] = myColor;
					if (Gomoku.isGameOver(boardCopy)[1] == myColor) {
						GomokuMove moveToAdd = new GomokuMove(myColor, i, j);
						moveToAdd.setPriority(MAX_PRIORITY);
						winningMoves.add(moveToAdd);
					}
					
					boardCopy[i][j] = Gomoku.EMPTY;
				}
			}
		}
		
		return winningMoves;
	}
	
	private GomokuMove pickFromMiddle() {
		if (gameState[7][7] == Gomoku.EMPTY)
			return new GomokuMove(myColor, 7, 7);
		else {
			GomokuMove chosenMove;
			int counter = 0;
			
			do {
				counter++;
				int randRow = ThreadLocalRandom.current().nextInt(6 - counter/8, 9 + counter/8);
				int randCol = ThreadLocalRandom.current().nextInt(6 - counter/8, 9 + counter/8);
				chosenMove = new GomokuMove(myColor, randRow, randCol);
			} while (gameState[chosenMove.getRow()][chosenMove.getColumn()] != Gomoku.EMPTY);
			
			return chosenMove;
		}
	}
	
	private GomokuMove playNecessaryDefense() {
		PriorityQueue<Threat> threats = Gomoku.findThreats(gameState, otherColor);
		Threat highestPriority = threats.poll();
		if (highestPriority == null) {
			return null;
		}
		else {
			if (highestPriority.threatSize < 3)
				return null;
			else {
				GomokuMove moveToPlay = Gomoku.respondToThreat(gameState, highestPriority, otherColor);
				while (moveToPlay == null && highestPriority.threatSize > 2) {
					highestPriority = threats.poll();
					moveToPlay = Gomoku.respondToThreat(gameState, highestPriority, otherColor);
				}
				if (moveToPlay == null) {
					return null;
				}
				else {
					GomokuMove defenseMove = new GomokuMove(myColor, moveToPlay.getRow(), moveToPlay.getColumn());
					defenseMove.setPriority(highestPriority.threatSize);
					return defenseMove;
//					return moveToPlay;
				}
			}
		}
	}
	
	private GomokuMove playOffense() {
		PriorityQueue<Threat> threats = Gomoku.findThreats(gameState, myColor);
		Threat highestPriority = threats.poll();
		if (highestPriority == null) {
//			throw new RuntimeException("No move found");
			return pickFromMiddle();
		}
		else {
			GomokuMove moveToPlay = Gomoku.respondToThreat(gameState, highestPriority, myColor);
			while (moveToPlay == null) {
				highestPriority = threats.poll();
				if (highestPriority == null) {
					throw new RuntimeException("Nothing found");
				}
				moveToPlay = Gomoku.respondToThreat(gameState, highestPriority, myColor);
			}
			
			GomokuMove offenseMove = new GomokuMove(myColor, moveToPlay.getRow(), moveToPlay.getColumn());
			offenseMove.setPriority(highestPriority.threatSize);
			return offenseMove;
//			return moveToPlay;
		}
	}
}
