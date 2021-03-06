package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import gomoku.Gomoku;
import gomoku.GomokuMove;
import gomoku.GomokuProtocol;

/** 
 * @author Graham Stubbs (wgs11@georgetown.edu)
 * @author Cooper Logerfo (cml264@georgetown.edu)
 */
public abstract class GomokuClient {
	
	protected GomokuGUI gui;
	
	private Socket socket; 
	private String serverIP;
	private int serverPort;
	
	private PrintWriter outStream;
    private BufferedReader inStream; 
    
    private String user;
    protected int userColor = Gomoku.EMPTY;
    protected boolean myTurn = false;
    protected int[][] gameState;
    
    protected static final int DEFAULT_PORT = 0xFFFF;
	
	public GomokuClient() {
		this("localhost", DEFAULT_PORT);
	}
	
	public GomokuClient(int port) {
		this("localhost", port);
	}
	
	public GomokuClient(String ip, int port) {
		serverIP = ip;
		serverPort = port;
		
		gameState = new int[15][15];
		for (int i=0; i<15; i++) {
			for (int j=0; j<15; j++)
				gameState[i][j] = Gomoku.EMPTY;
		}
		
		initializeUserName();
		initializeGUI();
		
		if(!connectToServer())
			disconnectFromServer();
	}
	
	private boolean connectToServer() {
		try {
			gui.displayMessage("Connecting to server: " + serverIP + " " + serverPort);
			socket = new Socket(serverIP, serverPort);
		} catch (UnknownHostException e) {
			gui.displayMessage("Error: IP " + serverIP + " could not be determined.");
			return false;
		} catch (IOException e) {
			gui.displayMessage("Error: Error creating socket with host: " + serverIP + " " + serverPort);
			return false;
		} 
		
		gui.displayMessage("Connection successful.");
		
		try {
			outStream = new PrintWriter(socket.getOutputStream(), true);
			inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			gui.displayMessage("Error: An error occurred while connecting to server input/output streams.");
			return false;
		}
		
		// start listen thread to constantly listen to server:
		new ServerListener().start(); 
		
		//no errors:
		return true;
	}

	private void disconnectFromServer() {
		// disconnect from the server:
		try {
			if (outStream != null) {
				outStream.close();
				outStream = null;
			}
			if (inStream != null) {
				inStream.close();
				inStream = null;
			}
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (IOException e) {
			gui.displayMessage("Error: An error occured while disconnecting from server.");
		}
		
		gui.displayMessage("Disonnected from server.");
		
	}
	
	public String getUserName() {
		return user;
	}
	
	protected abstract void initializeGUI();
	
	protected abstract void initializeUserName();
	
//	public boolean isMyTurn() {
//		return myTurn;
//	}
	
	private void resetGameState() {
		for (int i=0; i<15; i++) {
			for (int j=0; j<15; j++) {
				gameState[i][j] = Gomoku.EMPTY;
			}
		}
	}
	
	protected void sendChatMessage(String message) {
		String chatMessage = GomokuProtocol.generateChatMessage(user, message);
		sendMessage(chatMessage);
	}
	
	protected void sendChangeNameMessage(String newName) {
		String changeNameMessage = GomokuProtocol.generateChangeNameMessage(user, newName);
		sendMessage(changeNameMessage);
	}
	
	private void sendMessage(String message) {
		Thread messageThread = new Thread(new MessageSender(message));
		messageThread.start();
	}
	
	protected void sendGiveupMessage() {
		String giveupMessage = GomokuProtocol.generateGiveupMessage();
		sendMessage(giveupMessage);
	}
	
	protected boolean sendPlayMessage(GomokuMove move) {
		if (myTurn && gameState[move.getRow()][move.getColumn()] == Gomoku.EMPTY) {
			boolean black = (move.getColor() == Gomoku.BLACK ? true : false);
			String playMessage = GomokuProtocol.generatePlayMessage(black, move.getRow(), move.getColumn());
			sendMessage(playMessage);
			return true;
		}
		else {
			gui.displayMessage("Invalid move.");
			return false;
		}
	}
	
	protected void sendResetMessage() {
		String resetMessage = GomokuProtocol.generateResetMessage();
		sendMessage(resetMessage);
	}
	
	public void setUserName(String u) {
		user = u;
	}
	
	protected void updatePlayerTurn() {
		myTurn = !myTurn;
	}
	
	private class MessageSender implements Runnable {
		
		private String messageToSend;
		
		public MessageSender(String message) {
			messageToSend = message;
		}
		
		@Override
		public void run() {
			try {					
				if (messageToSend != null) {
			        outStream.println(messageToSend);
			    }
				else
					throw new IOException();
				
			} catch (IOException e) {
				gui.displayMessage("Error sending message: '" + messageToSend + "'");
			} catch (NullPointerException e) {
				gui.displayMessage("Not connected to any server.");
			}
		}
	}
	
	private class ServerListener extends Thread {
		
		@Override
		public void run() {
			while (true) {
				try {
					String messageReceived = inStream.readLine();
					
					if (messageReceived == null) 
						throw new IOException();				
					else if (GomokuProtocol.isSetBlackColorMessage(messageReceived)) {
						gui.setColor(Gomoku.BLACK);
						userColor = Gomoku.BLACK;
						gui.displayMessage("New game started. Your color is: BLACK");
						updatePlayerTurn();
					}
					else if (GomokuProtocol.isSetWhiteColorMessage(messageReceived)) {
						gui.setColor(Gomoku.WHITE);
						userColor = Gomoku.WHITE;
						gui.displayMessage("New game started. Your color is: WHITE");
					}
					else if (GomokuProtocol.isPlayMessage(messageReceived)) {
						if (!gui.isGameOver()) {
							int [] details = GomokuProtocol.getPlayDetail(messageReceived);
							gui.placePieceOnBoard(new GomokuMove(details[0], details[1], details[2]));
							gameState[details[1]][details[2]] = details[0];
							updatePlayerTurn();
						}
					}
					else if (GomokuProtocol.isWinMessage(messageReceived)) {
						if (!gui.isGameOver()) {
							gui.displayMessage("You Win!");
							gui.gameOver();
							myTurn = false;
						}
					}
					else if (GomokuProtocol.isLoseMessage(messageReceived)) {
						if (!gui.isGameOver()) {
							gui.displayMessage("You Lose!");
							gui.gameOver();
							myTurn = false;
						}
					}
					else if (GomokuProtocol.isResetMessage(messageReceived)) {
						gui.displayMessage("Game has been reset");
						myTurn = (userColor == Gomoku.BLACK) ? true : false;
						gui.newGame();
						resetGameState();
					}
					else if (GomokuProtocol.isGiveupMessage(messageReceived)) {
						if (!gui.isGameOver()) {
							gui.displayMessage("You Win By Forfeit!");
							gui.gameOver();
							myTurn = false;
						}
					}
					else if (GomokuProtocol.isChatMessage(messageReceived)) {
						String[] chatMessage = GomokuProtocol.getChatDetail(messageReceived);
						gui.displayMessage(chatMessage[0] + ": " + chatMessage[1]);
					}
					else if (GomokuProtocol.isChangeNameMessage(messageReceived)) {
						String[] tokens = GomokuProtocol.getChangeNameDetail(messageReceived);
						if(user.equals(tokens[0])) {
							setUserName(tokens[1]);
							gui.updateUserName(tokens[1]);
						}
					}
					else {
						throw new ClassNotFoundException();
					}
				} catch (IOException e) {
					gui.displayMessage("Server has shutdown.");
					disconnectFromServer();
					break;
				} catch (ClassNotFoundException e) {
					gui.displayMessage("Error: Unknown message received.");
				}
			}
		}
	}
}
