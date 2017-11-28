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

public abstract class GomokuClient implements GomokuProtocol {
	
	private GomokuGUI gui;
	
	private Socket socket; 
	private String serverIP;
	private int serverPort;
	
	private PrintWriter outStream;
    private BufferedReader inStream;
    
    private String user;
    private boolean humanUser = false;
    
    protected static final int DEFAULT_PORT = 0xFFFF;
	
	public GomokuClient() {
		this("localhost", DEFAULT_PORT, false);
	}
	
	public GomokuClient(int port) {
		this(port, false);
	}
	
	public GomokuClient(boolean human) {
		this(DEFAULT_PORT, human);
	}
	
	public GomokuClient(int port, boolean human) {
		this("localhost", port, false);
	}
	
	public GomokuClient(String ip, int port, boolean human) {
		serverIP = ip;
		serverPort = port;
		humanUser = human;
		
		initializeUserName();
		
		gui = new GomokuGUI(this);
		
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
	
	protected abstract void initializeUserName();
	
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
	
	protected void sendPlayMessage(GomokuMove move) {
		boolean black = (move.getColor() == Gomoku.BLACK ? true : false);
		String playMessage = GomokuProtocol.generatePlayMessage(black, move.getX(), move.getY());
		sendMessage(playMessage);
	}
	
	protected void sendResetMessage() {
		String resetMessage = GomokuProtocol.generateResetMessage();
		sendMessage(resetMessage);
	}
	
	public void setUserName(String u) {
		user = u;
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
						gui.displayMessage("New game started. Your color is: BLACK");
					}
					else if (GomokuProtocol.isSetWhiteColorMessage(messageReceived)) {
						gui.setColor(Gomoku.WHITE);
						gui.displayMessage("New game started. Your color is: WHITE");
					}
					else if (GomokuProtocol.isPlayMessage(messageReceived)) {
						int [] details = GomokuProtocol.getPlayDetail(messageReceived);
						gui.placePieceOnBoard(new GomokuMove(details[0], details[1], details[2]));
					}
					else if (GomokuProtocol.isWinMessage(messageReceived)) {
						gui.displayMessage("You Win!");
					}
					else if (GomokuProtocol.isLoseMessage(messageReceived)) {
						gui.displayMessage("You Lose!");
					}
					else if (GomokuProtocol.isResetMessage(messageReceived)) {
						// TODO
					}
					else if (GomokuProtocol.isGiveupMessage(messageReceived)) {
						// TODO
					}
					else if (GomokuProtocol.isChatMessage(messageReceived)) {
						String[] chatMessage = GomokuProtocol.getChatDetail(messageReceived);
						gui.displayMessage(chatMessage[0] + ": " + chatMessage[1]);
					}
					else if (GomokuProtocol.isChangeNameMessage(messageReceived)) {
						// TODO
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
