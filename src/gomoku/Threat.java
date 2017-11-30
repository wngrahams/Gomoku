package gomoku;

import java.util.ArrayList;

public class Threat implements Comparable<Threat> {
	
	public static final int HORIZONTAL = 0b00;
	public static final int VERTICAL = 0b01;
	public static final int DOWN_RIGHT = 0b10;
	public static final int UP_RIGHT = 0b11;
	
	public ArrayList<GomokuMove> components;
	public int threatSize;
	public int threatType;
	
	public Threat(ArrayList<GomokuMove> components, int direction) {
		this.components = components;
		threatSize = components.size();
		threatType = direction;
	}
	
	public ArrayList<GomokuMove> getCostSquares() {
		ArrayList<GomokuMove> costSquares = new ArrayList<GomokuMove>();
//		if (threatSize == 4){
			if (threatType == HORIZONTAL) {
				int leftMost = Integer.MAX_VALUE;
				int rightMost = Integer.MIN_VALUE;
				for (int i=0; i<components.size(); i++) {
					if (components.get(i).getColumn() < leftMost)
						leftMost = components.get(i).getColumn();
					if (components.get(i).getColumn() > rightMost)
						rightMost = components.get(i).getColumn();
				}
				
				if (leftMost - 1 > -1) {  // need to consider a four that is touching another ?
					costSquares.add(new GomokuMove((components.get(0).getColor()-1)*(-1), components.get(0).getRow(), leftMost-1));
				}
				if (rightMost + 1 < 15) {
					costSquares.add(new GomokuMove((components.get(0).getColor()-1)*(-1), components.get(0).getRow(), rightMost+1));
				}
			}
			else if (threatType == VERTICAL) {
				int upMost = Integer.MAX_VALUE;
				int downMost = Integer.MIN_VALUE;
				for (int i=0; i<components.size(); i++) {
					if (components.get(i).getRow() < upMost)
						upMost = components.get(i).getRow();
					if (components.get(i).getRow() > downMost)
						downMost = components.get(i).getRow();
				}
				
				if (upMost - 1 > -1) {  // need to consider a four that is touching another ?
					costSquares.add(new GomokuMove((components.get(0).getColor()-1)*(-1), upMost-1, components.get(0).getRow()));
				}
				if (downMost + 1 < 15) {
					costSquares.add(new GomokuMove((components.get(0).getColor()-1)*(-1), downMost+1, components.get(0).getRow()));
				}
			}
			else if (threatType == UP_RIGHT || threatType == DOWN_RIGHT) {
				int leftMost = Integer.MAX_VALUE;
				int rightMost = Integer.MIN_VALUE;
				int upMost = Integer.MAX_VALUE;
				int downMost = Integer.MIN_VALUE;
				for (int i=0; i<components.size(); i++) {
					if (components.get(i).getColumn() < leftMost)
						leftMost = components.get(i).getColumn();
					if (components.get(i).getColumn() > rightMost)
						rightMost = components.get(i).getColumn();
					if (components.get(i).getRow() < upMost)
						upMost = components.get(i).getRow();
					if (components.get(i).getRow() > downMost)
						downMost = components.get(i).getRow();
				}
				
				if (threatType == UP_RIGHT) {
					if (rightMost + 1 < 15 && upMost - 1 > -1) {  // need to consider a four that is touching another ?
						costSquares.add(new GomokuMove((components.get(0).getColor()-1)*(-1), upMost-1, rightMost+1));
					}
					if (leftMost - 1 > -1 && downMost + 1 < 15) {
						costSquares.add(new GomokuMove((components.get(0).getColor()-1)*(-1), downMost+1, leftMost-1));
					}
				}
				else if (threatType == DOWN_RIGHT) {
					if (rightMost + 1 < 15 && downMost + 1 < 15) {  // need to consider a four that is touching another ?
						costSquares.add(new GomokuMove((components.get(0).getColor()-1)*(-1), downMost+1, rightMost+1));
					}
					if (leftMost - 1 > -1 && upMost - 1 > -1) {
						costSquares.add(new GomokuMove((components.get(0).getColor()-1)*(-1), upMost-1, leftMost-1));
					}
				}
			}
//		}
		
//		else if (threatSize == 3)
			
		return costSquares;
	}

	@Override
	public int compareTo(Threat o) {
		if (this.threatSize > o.threatSize)
			return -1;
		else if (this.threatSize < o.threatSize)
			return 1;
		else
			return 0;
	}
}
