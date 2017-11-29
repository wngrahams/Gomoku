package client;

import java.util.concurrent.ThreadLocalRandom;

public class AIClient extends GomokuClient {
	
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
		System.out.println("calculating...");
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
	protected void updatePlayerTurn() {
		super.updatePlayerTurn();
		
		calculateNextMove();
	}
}
