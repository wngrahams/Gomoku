package gomoku;

public class GomokuMove {
	
	private int color;
	private int row;
	private int column; 
	
	public GomokuMove(int color, int row, int col) {
		setColor(color);
		setRow(row);
		setColumn(col);
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColumn() {
		return column;
	}
	
	public int getRow() {
		return row;
	}
	
	public void setColumn(int column) {
		this.column = column;
	}

	public void setRow(int row) {
		this.row = row;
	}
}
