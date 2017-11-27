package gomoku;

public class Gomoku {
	public static final int EMPTY = 0b11111111111111111111111111111111;
	public static final int WHITE = 0b00;
	public static final int BLACK = 0b01;
	
	public static String colorAsString(int color) {
		if (color == WHITE)
			return "White";
		else if (color == BLACK)
			return "Black";
		else
			return "";
	}
}
