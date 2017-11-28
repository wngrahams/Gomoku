package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import gomoku.Gomoku;
import gomoku.GomokuProtocol;

public class GomokuServer extends GomokuProtocol {
	
	private int port;
	private boolean keepRunning;
	
	private static final int DEFAULT_PORT = 0xFFFF;
	
	// An ArrayList to keep track of all connected clients
	private ArrayList<ClientThread> threadList = new ArrayList<ClientThread>();
	
	private GameQueue<ClientThread> waitingQueue = new GameQueue<ClientThread>();
	private ArrayList<GameThread> activeGames = new ArrayList<GameThread>();
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		GomokuServer gs;
		if (args.length < 1)
			gs = new GomokuServer();
		else if (args.length == 1) {
			try {
				int portInt = Integer.parseInt(args[0]);
				gs = new GomokuServer(portInt);
			} catch (NumberFormatException e) {
				System.out.println("Usage: java ChatterServer <port_number>");
				System.out.println("Port number should be an integer less than " + DEFAULT_PORT);
			}
		}
		else {
			System.out.println("Usage: java ChatterServer <port_number>");
		}
	}
	
	public GomokuServer() {
		this(DEFAULT_PORT);
	}
	
	public GomokuServer(int port) {
		this.port = port;
		
		startServer();
	}
	
	private void addToWaitingQueue(ClientThread c) {
		synchronized (waitingQueue) {
			waitingQueue.enqueue(c);
		}
	}
	
	private boolean checkNameAvailable(String name) {
		synchronized (threadList) {
			for (int i=0; i<threadList.size(); i++) {
				String userName = threadList.get(i).getUser();
				if (name.equals(userName))
					return false;
			}
			
			return true;
		}
	}
	
	private void makeGamePair() {
		synchronized (waitingQueue) {
			ArrayList<ClientThread> pair = waitingQueue.dequeuePair();
			if (pair != null) {
				GameThread newGame = new GameThread(pair.get(0), pair.get(1));
				newGame.start();
				
				activeGames.add(newGame);
			}
		}
	}
	
	private void removeClientFromList(ClientThread client) {
		synchronized(threadList) {
			threadList.remove(this);
		}
	}
	
	/** 
	 * Creates a new <code>ServerSocket</code> at the port given on class
	 * creation. Then creates a new <code>Socket</code> to continually
	 * listen for new client connections. Terminates 
	 * if an error is encountered when creating the <code>ServerSocket</code>.
	 */
	private void startServer() {
		keepRunning = true;
				
		try {
			// Create the ServerSocket 
			ServerSocket serverSocket = new ServerSocket(port);
			
			// Do this until ServerFrame close, won't start if ServerSocket creation fails
			while (keepRunning) {
				
				try {
					// Create Socket to continually listen for new client connections
					Socket clientSocket = serverSocket.accept();
					
					if (!keepRunning)
						break;
					
					ClientThread newClient = new ClientThread(clientSocket);
					threadList.add(newClient);
					newClient.start();
					
					addToWaitingQueue(newClient);
			        makeGamePair();
				} catch (IOException e) {
					System.err.println("Error connecting to client.");
				} 
			}
			
			try {
				serverSocket.close();
				for (int i=0; i<threadList.size(); i++) {
					ClientThread toClose = threadList.get(i);
					toClose.close();
				}
			} catch (IOException e) {
				System.err.println("Error closing server socket.");
			}
		} catch (IOException e) {
			System.err.println("Error creating server socket at port: " + port);
			keepRunning = false;
		} catch (IllegalArgumentException e) {
			System.err.println("Port value out of range: " + port);
		}
	}

	private class ClientThread extends Thread {
		
		private Socket clientSocket;
		private String clientUser;
		
		private PrintWriter out;
		private BufferedReader in;
		
		private String clientMessage;
		
		private GameThread currentGame;
				
		public ClientThread(Socket s) {
			super();
			clientSocket = s;
			
			try {
				out = new PrintWriter(clientSocket.getOutputStream(), true);
		        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e) {
				System.err.println("Error connecting client input/output stream");
				return;
			} 
		}
		
		public void setCurrentGame(GameThread game) {
			currentGame = game;
		}
		
		protected void close() {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					System.err.println("Error disconnecting from client input stream.");
				}
			}
			if (clientSocket != null) {
				try {
					clientSocket.close();
				} catch (IOException e) {
					System.err.println("Error disconnecting from client socket.");
				}
			}
		}
		
		@Override
		public void run() {
			boolean clientRun = true;
			while (clientRun) {
				try {
					clientMessage = in.readLine();
					if (clientMessage == null) 
						throw new IOException();
					
					System.out.println(clientMessage);
					
					if (isPlayMessage(clientMessage)) {
						if (currentGame != null)
							currentGame.processPlayMessage(clientMessage);
						else {
							String error = generateChatMessage("Server", "Please wait to be connected to a game.");
							sendMessage(error);
						}
					}
					
				} catch (IOException e) {
					// user has disconnected
					System.err.println("User has disconnected");
					currentGame.setLoser(this);
					break;
				}
			}
			
			removeClientFromList(this);
			close();
		}
		
		public String getUser() {
			return clientUser;
		}
		
		public boolean sendMessage(String message) {
			if (!clientSocket.isConnected() || clientSocket.isClosed()) {
				close();
				return false;
			}
			
			try {
				
				if (message == null) 
					throw new IOException();	
					
				out.println(message);
				
			} catch (IOException e) {
				System.err.println("Error sending message to " + clientUser);
				return false;
			}
			
			return true;
		}
	}
	
	private class GameThread extends Thread {
		
		private ClientThread black;
		private ClientThread white;
		
		private int[][] gameState = new int[15][15];
		
		public GameThread(ClientThread p1, ClientThread p2) {
			
			for (int i=0; i<15; i++) {
				for (int j=0; j<15; j++)
					gameState[i][j] = Gomoku.EMPTY;
			}
			
			int rand = ThreadLocalRandom.current().nextInt(0, 2);
			if (rand%2 == 0) {
				setBlack(p1);
				setWhite(p2);
			}
			else {
				setBlack(p2);
				setWhite(p1);
			}
			
			black.setCurrentGame(this);
			white.setCurrentGame(this);
			
			black.sendMessage(generateSetBlackColorMessage());
			white.sendMessage(generateSetWhiteColorMessage());
		}

		public ClientThread getBlack() {
			return black;
		}

		public ClientThread getWhite() {
			return white;
		}
		
		public void processPlayMessage(String playMessage) {
			int[] info = getPlayDetail(playMessage);
			if (gameState[info[1]][info[2]] == Gomoku.EMPTY) {
				gameState[info[1]][info[2]] = info[0];
				black.sendMessage(playMessage);
				white.sendMessage(playMessage);
			}
			else {
				if (info[0] == Gomoku.BLACK)
					black.sendMessage(generateChatMessage("Server", "That is an invalid move."));
				else if (info[0] == Gomoku.WHITE)
					white.sendMessage(generateChatMessage("Server", "That is an invalid move."));
			}
		}
		
		public void setBlack(ClientThread black) {
			this.black = black;
		}
		
		public void setLoser(ClientThread loser) {
			if (loser == black) {
				setResults(white, black);
			}
			else if (loser == white) {
				setResults(black, white);
			}
		}
		
		private void setResults(ClientThread winner, ClientThread loser) {
			winner.sendMessage(generateWinMessage());
			loser.sendMessage(generateLoseMessage());
		}

		public void setWhite(ClientThread white) {
			this.white = white;
		}
		
		public void setWinner(ClientThread winner) {
			if (winner == black) {
				setResults(black, white);
			}
			else if (winner == white) {
				setResults(white, black);
			}
		}
		
		
	}
}