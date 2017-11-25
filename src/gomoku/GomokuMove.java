package gomoku;

public class GomokuMove {
	
	private int color;
	private int xPos;
	private int yPos;
	
	public GomokuMove(int color, int x, int y) {
		setColor(color);
		setX(x);
		setY(y);
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getX() {
		return xPos;
	}

	public void setX(int xPos) {
		this.xPos = xPos;
	}

	public int getY() {
		return yPos;
	}

	public void setY(int yPos) {
		this.yPos = yPos;
	}
}
