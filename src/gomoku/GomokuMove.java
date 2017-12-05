package gomoku;

/** 
 * @author Graham Stubbs (wgs11@georgetown.edu)
 * @author Cooper Logerfo (cml264@georgetown.edu)
 */
public class GomokuMove implements Comparable<GomokuMove>{
	
	private int color;
	private int row;
	private int column; 
	private int priority = 0;
	
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
	
	public int getPriority() {
		return priority;
	}
	
	public int getRow() {
		return row;
	}
	
	public void setColumn(int column) {
		this.column = column;
	}
	
	public void setPriority(int p) {
		priority = p;
	}

	public void setRow(int row) {
		this.row = row;
	}

	@Override
	public int compareTo(GomokuMove o) {
		if (this.priority > o.priority)
			return -1;
		else if (this.priority < o.priority)
			return 1;
		else
			return 0;
	}
}
