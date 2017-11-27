package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class GomokuServer {
	
	private int port;
	private boolean keepRunning;
	
	private static final int DEFAULT_PORT = 0xFFFF;
	
	// An ArrayList to keep track of all connected clients
	private ArrayList<ClientThread> threadList;
	
	
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
		threadList = new ArrayList<ClientThread>();
		
		startServer();
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
	
	private void removeClientFromList(ClientThread client) {
		synchronized(threadList) {
			threadList.remove(this);
		}
	}
	
	/** 
	 * Creates a new <code>ServerSocket</code> at the port given on class
	 * creation. Then creates a new <code>Socket</code> to continually
	 * listen for new client connections. Terminates on <code>ServerFrame</code>
	 * close or if an error is encountered when creating the <code>ServerSocket</code>
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
					
					out.println(clientMessage);
					
					System.out.println(clientMessage);
					
				} catch (IOException e) {
					// user has disconnected off
					System.err.println("User has disconnected");
					break;
				}

				// first check if it's a name change, if the new name is available
//				if (clientMessage.getType() == Message.USER_NAME_MESSAGE && !checkNameAvailable(clientMessage.getMessage()))
//						sendMessageToClient(new Message(clientUser, User.SERVER, "Name '" + clientMessage.getMessage() + "' is not available."));
//				else
//					sendMessageToClient(clientMessage);
			}
			
			removeClientFromList(this);
//			sendMessage(clientMessage);
			close();
		}
		
		public String getUser() {
			return clientUser;
		}
		
		/** 
		 * Uses the client's <code>ObjectOutputStream</code> to write the 
		 * given <code>Message</code> over the network to the client
		 * 
		 * @param m The <code>Message</code> to be written
		 * @return <code>true</code> if the message is successfully sent, <code>false</code> otherwise
		 */
		public boolean sendMessage(String message) {
			if (!clientSocket.isConnected() || clientSocket.isClosed()) {
				close();
				return false;
			}
			
//			try {
				
//				pSend.println(clientMessage);
//				pSend.flush();
//				if (m.getType() == Message.TEXT_MESSAGE) {
//					send.writeUnshared(m);
//				}
//				else if (m.getType() == Message.USER_NAME_MESSAGE) {
//					send.writeUnshared(m);
//					
//					// Change name only for user that sent this name change request
//					if (clientUser.equals(m.getSender())) {
//						User oldUser = new User(clientUser);
//						clientUser = new User(m.getMessage(), clientUser.getIP());
//						serverFrame.displayMessage(new Message(clientUser, oldUser, clientUser.getNickname(), Message.USER_NAME_MESSAGE));
//						send.writeUnshared(new Message(clientUser, User.SERVER, "Successfully changed name from '" + oldUser + "' to '" + clientUser + "'"));
//					}
//				}	
//				else if (m.getType() == Message.USER_LOGON_MESSAGE ||  m.getType() == Message.USER_LOGOFF_MESSAGE){
//					// No need to send to the user that logged on/off
//					if (m.getSender() != clientUser) 
//						send.writeUnshared(m);
//				}
				
//			} catch (IOException e) {
//				System.err.println("Error sending message to " + clientUser);
//				return false;
//			}
			
			return true;
		}
	}
}