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

public class GomokuServer {
	
	private int port;
	private boolean keepRunning;
	
	private static final int DEFAULT_PORT = 0xFFFF;
	private static final int GAMEOVER_GIVEUP = 0;
	private static final int GAMEOVER_COMPLETE = 1;
	private static final int GAMEOVER_DISCONNECT = 2;
	
	// An ArrayList to keep track of all connected clients
	private ArrayList<ClientThread> threadList = new ArrayList<ClientThread>();
	
	private GameQueue<ClientThread> waitingQueue = new GameQueue<ClientThread>();
	private ArrayList<GameManager> activeGames = new ArrayList<GameManager>();
	
	
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
				if (pair.get(0).isConnected() && pair.get(1).isConnected()) {
					GameManager newGame = new GameManager(pair.get(0), pair.get(1));
					activeGames.add(newGame);
				}
				else {
					for (ClientThread i : pair) {
						if (i.isConnected())
							waitingQueue.putBack(i);
					}
				}
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
		
		private GameManager currentGame;
		
		private boolean connected = false;
				
		public ClientThread(Socket s) {
			super();
			clientSocket = s;
			
			try {
				out = new PrintWriter(clientSocket.getOutputStream(), true);
		        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		        //System.out.println("inside new clientthread(constructor), stream = " + in.readLine());
			} catch (IOException e) {
				System.err.println("Error connecting client input/output stream");
				return;
			} 
			
			connected = true;
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
			
			connected = false;
		}
		
		public String getUser() {
			return clientUser;
		}
		
		public boolean isConnected() {
			return connected;
		}
		
		@Override
		public void run() {
			boolean clientRun = true;
			while (clientRun) {
				try {
					clientMessage = in.readLine();
					if (clientMessage == null) 
						throw new IOException();					
					else if (GomokuProtocol.isPlayMessage(clientMessage)) {
						if (currentGame != null)
							currentGame.processPlayMessage(clientMessage);
						else {
							String error = GomokuProtocol.generateChatMessage("Server", "Please wait to be connected to a game.");
							sendMessage(error);
						}
					}
					else if (GomokuProtocol.isChatMessage(clientMessage)) {
						if (currentGame != null)
							currentGame.processChatMessage(clientMessage);
						else
							sendMessage(clientMessage);
					}
					else if (GomokuProtocol.isChangeNameMessage(clientMessage)) {
						if(currentGame != null)
							currentGame.processNameChangeMessage(clientMessage);
					}
					else if (GomokuProtocol.isGiveupMessage(clientMessage)) {
						if(currentGame != null)
							currentGame.processGiveupMessage(clientMessage, this);
					}
					else if(GomokuProtocol.isResetMessage(clientMessage)) {
						if(currentGame != null)
							currentGame.processResetMessage(clientMessage);
					}
					
				} catch (IOException e) {
					// user has disconnected
					System.err.println("User has disconnected");
					if (currentGame != null)
						currentGame.setLoser(this, GAMEOVER_DISCONNECT);
					break;
				}
			}
			
			removeClientFromList(this);
			close();
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
		
		public void setCurrentGame(GameManager game) {
			currentGame = game;
		}
	}
	
	private class GameManager {
		
		private ClientThread black;
		private ClientThread white;
		
		private int[][] gameState = new int[15][15];
		
		public GameManager(ClientThread p1, ClientThread p2) {
			
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
			
			black.sendMessage(GomokuProtocol.generateSetBlackColorMessage());
			white.sendMessage(GomokuProtocol.generateSetWhiteColorMessage());
		}

		public ClientThread getBlack() {
			return black;
		}

		public ClientThread getWhite() {
			return white;
		}
		
		public void processChatMessage(String chatMessage) {
			black.sendMessage(chatMessage);
			white.sendMessage(chatMessage);
		}
		
		public void processPlayMessage(String playMessage) {
			int[] info = GomokuProtocol.getPlayDetail(playMessage);
			if (gameState[info[1]][info[2]] == Gomoku.EMPTY) {
				gameState[info[1]][info[2]] = info[0];
				black.sendMessage(playMessage);
				white.sendMessage(playMessage);
//				int[] results = Gomoku.isGameOver(gameState);
//				if (results[0] == Gomoku.GAME_OVER) {
//					setWinner(results[1], GAMEOVER_COMPLETE);
//				}
				int[]latestMove = new int[3];
				latestMove = info;
				int[] results2 = Gomoku.isGameOver(gameState, latestMove);
				if(results2[0] == Gomoku.GAME_OVER) {
					setWinner(results2[1], GAMEOVER_COMPLETE);
				}
			}
			else {
				if (info[0] == Gomoku.BLACK)
					black.sendMessage(GomokuProtocol.generateChatMessage("Server", "That is an invalid move."));
				else if (info[0] == Gomoku.WHITE)
					white.sendMessage(GomokuProtocol.generateChatMessage("Server", "That is an invalid move."));
			}
		}
		
		public void processNameChangeMessage(String nameMessage) {
			String[] tokens = GomokuProtocol.getChangeNameDetail(nameMessage);
			if(checkNameAvailable(tokens[1])) {
					black.sendMessage(nameMessage);
					white.sendMessage(nameMessage);
			}
			
		}
		
		public void processGiveupMessage(String giveupMessage, ClientThread loser) {
			setLoser(loser, GAMEOVER_GIVEUP);
		}
		
		public void processResetMessage(String resetMessage) {
			black.sendMessage(resetMessage);
			white.sendMessage(resetMessage);
			for (int i=0; i<15; i++) {
				for (int j=0; j<15; j++)
					gameState[i][j] = Gomoku.EMPTY;
			}
		}
		
		public void setBlack(ClientThread black) {
			this.black = black;
		}
		
		public void setLoser(ClientThread loser, int type) {
			if (loser == black) {
				setResults(white, black, type);
			}
			else if (loser == white) {
				setResults(black, white, type);
			}
		}
		

		private void setResults(ClientThread winner, ClientThread loser, int type) {
			if(type == GAMEOVER_GIVEUP)
			{
				winner.sendMessage(GomokuProtocol.generateGiveupMessage());
				loser.sendMessage(GomokuProtocol.generateLoseMessage());
			}
			else if(type == GAMEOVER_COMPLETE)
			{
				winner.sendMessage(GomokuProtocol.generateWinMessage());
				loser.sendMessage(GomokuProtocol.generateLoseMessage());
			}
			else if(type == GAMEOVER_DISCONNECT)
			{
				//TODO: add "win by disconnect" messages if we want them
				winner.sendMessage(GomokuProtocol.generateWinMessage());
				loser.sendMessage(GomokuProtocol.generateLoseMessage());
			}
		}

		public void setWhite(ClientThread white) {
			this.white = white;
		}
		
		public void setWinner(ClientThread winner, int type) {
			if (winner == black) {
				setResults(black, white, type);
			}
			else if (winner == white) {
				setResults(white, black,type);
			}
		}
		
		public void setWinner(int winner, int type) {
			if (winner == Gomoku.BLACK) {
				setWinner(black, type);
			}
			else if (winner == Gomoku.WHITE) {
				setWinner(white, type);
			}
		}
	}
}