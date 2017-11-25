package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
	
	private InputStream fromServer;
    private BufferedReader bFrom;
    private OutputStream toServer;
    private BufferedWriter bTo;
	
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
		
		gui = new GomokuGUI(Gomoku.EMPTY);
		
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
			fromServer = socket.getInputStream();
			bFrom = new BufferedReader(new InputStreamReader(fromServer));
			toServer = socket.getOutputStream();
			bTo = new BufferedWriter(new OutputStreamWriter(toServer));
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
			if (fromServer != null) {
				fromServer.close();
				fromServer = null;
			}
			if (toServer != null) {
				toServer.close();
				toServer = null;
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
	
	private class ServerListener extends Thread {
		
		@Override
		public void run() {
			while (true) {
				try {
					String messageReceived = bFrom.readLine();
					if (isSetBlackColorMessage(messageReceived)) 
						gui = new GomokuGUI(Gomoku.BLACK);
					else if (isSetWhiteColorMessage(messageReceived)) 
						gui = new GomokuGUI(Gomoku.WHITE);
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
