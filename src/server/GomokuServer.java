package server;

import java.util.ArrayList;
import java.util.Queue;

import gomoku.GomokuMove;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class GomokuServer {

	
    private static final String SEPARATOR = "\0";
    private static final String MESSAGE_PLAY = SEPARATOR + "/play";
    private static final String MESSAGE_SET_BLACK = SEPARATOR + "/black";
    private static final String MESSAGE_SET_WHITE = SEPARATOR + "/white";
    private static final String MESSAGE_WIN= SEPARATOR + "/win";
    private static final String MESSAGE_LOSE = SEPARATOR + "/lose";
    private static final String MESSAGE_RESET = SEPARATOR + "/reset";
    private static final String MESSAGE_GIVEUP = SEPARATOR + "/giveup";
    private static final String MESSAGE_CHAT = SEPARATOR + "/chat";
    private static final String MESSAGE_CHANGE_NAME = SEPARATOR + "/nick";
    
    private static final int BOARD_MAXIMUM = 15;
    private static final int BOARD_MINIMUM = 0;
    
    
	private int port = 0xFFFF;
	private boolean keepGoing;

    
	private ArrayList<ClientThread> threadList;
	private Queue<ClientThread> waitingClients;
	private ArrayList<GameThread> currentGames;

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	public GomokuServer() {
		this(0xFFFF);
	}
	
	public GomokuServer(int port)
	{
		this.port = port;
		threadList = new ArrayList<ClientThread>();
		startServer();
	}
	
	private void startServer() {
		keepGoing = true;
		
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			
			while(keepGoing)
			{
				try {
					Socket clientSocket = serverSocket.accept();
					
					if (!keepGoing)
						break;
					
					ClientThread newClient = new ClientThread(clientSocket);
					threadList.add(newClient);
					newClient.start();
					
					if(threadList.size()%2 != 0)
					{
						/*
						 * odd # of players, waiting message
						 */
					}
					else
					{
						/*
						 * even number of players, start new gamethread
						 * clean this up
						 */
						Socket player2 = threadList.get(threadList.size() - 1).getSocket();
						GameThread newGame = new GameThread(clientSocket, player2);
						currentGames.add(newGame);
						newGame.start();
					}
				}
				catch(Exception e) {
					
				}
			}
		}
		catch(Exception e) {
			
		}
	}
	
	
    public static int[] getPlayDetail(String msg) {
        if (isPlayMessage(msg)) {
            String[] tokens = msg.split(SEPARATOR);
            if (tokens.length >= 5) {
                return new int[]{
                        Integer.parseInt(tokens[2]), 
                        Integer.parseInt(tokens[3]), 
                        Integer.parseInt(tokens[4])};
            }
        }
        return null;
    }
    
    public static boolean isPlayMessage(String msg) {
        return msg.startsWith(MESSAGE_PLAY);
    }
    
	
	
	private class ClientThread extends Thread{
		
		private Socket clientSocket;
		
		BufferedReader buffReader;
		BufferedWriter buffWriter;
		
		OutputStream send;
		InputStream receive; 
		
		String message;
		
		boolean isPlayMessage = false;
		
		public ClientThread(Socket s) {
			clientSocket = s;
			try {
				
				receive = s.getInputStream();
				buffReader = new BufferedReader(new InputStreamReader(receive));
				
				send = s.getOutputStream();
				buffWriter = new BufferedWriter( new OutputStreamWriter(send));
				
				message = buffReader.readLine();

			} catch(Exception e) {
				return;
			}
			
			if(isPlayMessage(message)) {
				
			}else {
				
			}
			
		}
		
		protected void close() {
			if (send != null) {
				try {
					send.close();
				} catch (IOException e) {	
				}
			}
			if (receive != null) {
				try {
					receive.close();
				} catch (IOException e) {
				}
			}
			if (clientSocket != null) {
				try {
					clientSocket.close();
				} catch (IOException e) {
				}
			}
		}
		
		@Override
		public void run() {
			boolean clientRun = false;
			while(clientRun) {
				try {
					
					message = buffReader.readLine();					
					
				}
				catch(Exception e) {
					
				}
				
				if(message.startsWith(MESSAGE_PLAY))
				{
					int[] coordinates = getPlayDetail(message);
					//updateBoard(coordinates);
					int xMove = coordinates[0];
					int yMove = coordinates[1];
					int color = coordinates[3];
//					if(color == 0)
//					{
//						
//					}
					GomokuMove latestMove = new GomokuMove(xMove, yMove, color);
					
					//need to get this move to GameThread class
				}
				else if(message.startsWith(MESSAGE_CHAT))
				{
					//sendMessage 
				}
			}
		}
		
		
		private Socket getSocket() {
			return clientSocket;
		}
		
		
		
		public boolean updateBoard(int[] coords) {
			boolean boardUpdated = false;
			//coordinate 1 : isblack, 2 : row, 3 : col
			return boardUpdated;
		}
		
		
		
		public boolean sendMessage(String m) {
			boolean messageSent = false;
			
			return messageSent;
		}
		
		
	}

	
	private class GameThread extends Thread{
		
		private Socket player1;
		private Socket player2;
		
		BufferedReader buffReader;
		BufferedWriter buffWriter;
		
		OutputStream send;
		InputStream receive; 
		
		private int[] gameBoard;
		
//		private ClientThread player1Thread;
//		private ClientThread player2Thread;
		
		private ArrayList<GomokuMove> currentBoard;
		
		private GameThread(Socket p1, Socket p2) {
			player1 = p1;
			player2 = p2;
			
		}
		
//		private GameThread(ClientThread p1, ClientThread p2)
//		{
//			player1Thread = p1;
//			player2Thread = p2;
//		}
		
		public boolean checkStatus() {
			boolean stillGoing = false;
			
			return stillGoing;
		}
		
		/*
		 * Used a counter instead of break, can change
		 */
		private boolean checkIfValidMove(int x, int y) {
			boolean validMove = false;
			int counter = 0;
			
			//check if coords are within board
			if(x > BOARD_MAXIMUM || x < BOARD_MINIMUM || y > BOARD_MAXIMUM || y < BOARD_MINIMUM) {
				//invalid move
				counter++;
			}
			//compare point to rest of board
			for(int i = 0; i < currentBoard.size(); i++){
				GomokuMove tempMove = currentBoard.get(i);
				int tempX = tempMove.getX();
				int tempY = tempMove.getY();
				
				if(x == tempX || y == tempY) {
					//invalid move
					counter++;
				}
			}
			if(counter == 0) {
				validMove = true;
			}
			return validMove;
		}
		
		public void addNewMove(int x, int y, int color) {
			if( checkIfValidMove(x,y) ){
				//move is valid, can be added
				GomokuMove newMove = new GomokuMove(x,y, color);
				currentBoard.add(newMove);
			}
		}
		
		
		private Socket getP1Sock() {
			return player1;
		}
		
		private Socket getP2Sock() {
			return player2;
		}
	}
	


}
