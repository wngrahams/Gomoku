package client;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;

import javax.management.RuntimeErrorException;

import gomoku.BoardEvaluator;
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
		
		BoardEvaluator eval = new BoardEvaluator(gameState, userColor);
		GomokuMove moveToPlay = eval.evaluateBoard().poll();
		if (moveToPlay != null) {
			sendPlayMessage(moveToPlay);
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
