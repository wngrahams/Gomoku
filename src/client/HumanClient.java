package client;

import java.util.concurrent.ThreadLocalRandom;

public class HumanClient extends GomokuClient {
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		GomokuClient cc;
		if (args.length < 1)
			cc = new HumanClient();
		else if (args.length < 2 || args.length > 2)
			System.out.println("Usage: java GomokuClient <hostname> <port_number>");
		else {
			try {
				int portInt = Integer.parseInt(args[1]);
				cc = new HumanClient(args[0], portInt);
			} catch (NumberFormatException e) {
				System.out.println("Usage: java GomokuClient <hostname> <port_number>");
				System.out.println("Port number should be an integer less than " + DEFAULT_PORT);
			}
		}
	}

	public HumanClient() {
		super(true);
	}
	
	public HumanClient(int port) {
		super("localhost", port, true);
	}
	
	public HumanClient(String ip, int port) {
		super(ip, port, true);
	}

	@Override
	protected void initializeUserName() {
		int rand = ThreadLocalRandom.current().nextInt(1, 100000);
		setUserName("user" + Integer.toString(rand));
	}
}
