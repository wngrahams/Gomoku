package client;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;

import javax.management.RuntimeErrorException;

import gomoku.Gomoku;
import gomoku.GomokuMove;
import gomoku.Threat;

public class AIClient extends GomokuClient {
	
	private int piecesPlayed = -1;
	private int otherColor;
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		GomokuClient cc;
		if (args.length < 1)
			cc = new AIClient();
		else if (args.length < 2 || args.length > 2)
			System.out.println("Usage: java GomokuClient <hostname> <port_number>");
		else {
			try {
				int portInt = Integer.parseInt(args[1]);
				cc = new AIClient(args[0], portInt);
			} catch (NumberFormatException e) {
				System.out.println("Usage: java GomokuClient <hostname> <port_number>");
				System.out.println("Port number should be an integer less than " + DEFAULT_PORT);
			}
		}
	}

	public AIClient() {
		super();
	}
	
	public AIClient(int port) {
		super(port);
	}
	
	public AIClient(String ip, int port) {
		super(ip, port);
	}
	
	private void calculateNextMove() {
		boolean sendSuccess = false;
		GomokuMove move;
		
		int randRow;
		int randCol;
		
		if (piecesPlayed < 2 && gameState[7][7] == Gomoku.EMPTY) {
			move = new GomokuMove(userColor, 7, 7);
			sendSuccess = sendPlayMessage(move);
		}
		else {

			move = playNecessaryDefense();  // gonna be real slow
			if (move != null) {
				sendPlayMessage(move);
				return;
			}
			else {
				move = playOffense();
				sendPlayMessage(move);
				return;
			}
			
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
					GomokuMove defenseMove = new GomokuMove(userColor, moveToPlay.getRow(), moveToPlay.getColumn());
					return defenseMove;
//					return moveToPlay;
				}
			}
		}
	}
	
	private GomokuMove playOffense() {
		PriorityQueue<Threat> threats = Gomoku.findThreats(gameState, userColor);
		Threat highestPriority = threats.poll();
		if (highestPriority == null) {
//			throw new RuntimeException("No move found");
			return pickFromMiddle();
		}
		else {
			GomokuMove moveToPlay = Gomoku.respondToThreat(gameState, highestPriority, userColor);
			while (moveToPlay == null) {
				highestPriority = threats.poll();
				if (highestPriority == null) {
					throw new RuntimeException("Nothing found");
				}
				moveToPlay = Gomoku.respondToThreat(gameState, highestPriority, userColor);
			}
			
			GomokuMove offenseMove = new GomokuMove(userColor, moveToPlay.getRow(), moveToPlay.getColumn());
			return offenseMove;
//			return moveToPlay;
		}
	}
	
	private GomokuMove pickFromMiddle() {
		if (gameState[7][7] == Gomoku.EMPTY)
			return new GomokuMove(userColor, 7, 7);
		else {
			GomokuMove chosenMove;
			int counter = 0;
			
			do {
				counter++;
				int randRow = ThreadLocalRandom.current().nextInt(6 - counter/8, 9 + counter/8);
				int randCol = ThreadLocalRandom.current().nextInt(6 - counter/8, 9 + counter/8);
				chosenMove = new GomokuMove(userColor, randRow, randCol);
			} while (gameState[chosenMove.getRow()][chosenMove.getColumn()] != Gomoku.EMPTY);
			
			return chosenMove;
		}
	}
	
	@Override
	protected void initializeGUI() {
		gui = new GomokuGUI(this, false);
	}

	@Override
	protected void initializeUserName() {
		int rand = ThreadLocalRandom.current().nextInt(1, 100000);
		setUserName("AI" + Integer.toString(rand));
	}
	
	@Override
	protected boolean sendPlayMessage(GomokuMove move) {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.sendPlayMessage(move);
	}
	
	@Override
	protected void updatePlayerTurn() {
		super.updatePlayerTurn();
		
		if(myTurn)
			calculateNextMove();
		
		piecesPlayed++;
		
		otherColor = (userColor - 1)*(-1);
	}
}
