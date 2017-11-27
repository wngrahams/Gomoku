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

public class GomokuClient extends GomokuProtocol {
	
	private GomokuGUI gui;
	
	private Socket socket; 
	private String serverIP;
	private int serverPort;
	
	private PrintWriter outStream;
    private BufferedReader inStream;
    
    private String user;
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		GomokuClient cc;
		if (args.length < 1)
			cc = new GomokuClient();
		else if (args.length < 2 || args.length > 2)
			System.out.println("Usage: java GomokuClient <hostname> <port_number>");
		else {
			try {
				int portInt = Integer.parseInt(args[1]);
				cc = new GomokuClient(args[0], portInt);
			} catch (NumberFormatException e) {
				System.out.println("Usage: java GomokuClient <hostname> <port_number>");
				System.out.println("Port number should be an integer less than " + 0xFFFF);
			}
		}
	}
	
	public GomokuClient() {
		this("localhost", 0xFFFF);
	}
	
	public GomokuClient(int port) {
		this("localhost", port);
	}
	
	public GomokuClient(String ip, int port) {
		serverIP = ip;
		serverPort = port;
		
		int rand = (int) (Math.random() * 100000 + 1);
		user = "user" + Integer.toString(rand);
		
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
	
	public void sendChatMessage(String message) {
		String chatMessage = generateChatMessage(user, message);
		sendMessage(chatMessage);
	}
	
	public void sendChangeNameMessage(String newName) {
		String changeNameMessage = generateChangeNameMessage(user, newName);
		sendMessage(changeNameMessage);
	}
	
	private void sendMessage(String message) {
		Thread messageThread = new Thread(new MessageSender(message));
		messageThread.start();
	}
	
	public void sendGiveupMessage() {
		String giveupMessage = generateGiveupMessage();
		sendMessage(giveupMessage);
	}
	
	public void sendPlayMessage(GomokuMove move) {
		boolean black = (move.getColor() == Gomoku.BLACK ? true : false);
		String playMessage = generatePlayMessage(black, move.getX(), move.getY());
		sendMessage(playMessage);
	}
	
	public void sendResetMessage() {
		String resetMessage = generateResetMessage();
		sendMessage(resetMessage);
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
			        System.out.println("Client: " + messageToSend);
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
					else if (isSetBlackColorMessage(messageReceived)) 
						gui.setColor(Gomoku.BLACK);
					else if (isSetWhiteColorMessage(messageReceived)) 
						gui.setColor(Gomoku.WHITE);
					else if (isPlayMessage(messageReceived)) {
						int [] details = getPlayDetail(messageReceived);
						gui.placePieceOnBoard(new GomokuMove(details[0], details[1], details[2]));
					}
					else if (isWinMessage(messageReceived)) {
						// TODO
					}
					else if (isLoseMessage(messageReceived)) {
						// TODO
					}
					else if (isResetMessage(messageReceived)) {
						// TODO
					}
					else if (isGiveupMessage(messageReceived)) {
						// TODO
					}
					else if (isChatMessage(messageReceived)) {
						String[] chatMessage = getChatDetail(messageReceived);
						gui.displayMessage(chatMessage[0] + ": " + chatMessage[1]);
					}
					else if (isChangeNameMessage(messageReceived)) {
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
