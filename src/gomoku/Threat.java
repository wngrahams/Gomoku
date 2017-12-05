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
	
	public ArrayList<GomokuMove> getCostSquares(int[][] gameState) {
		ArrayList<GomokuMove> costSquares = new ArrayList<GomokuMove>();
		int otherColor = (components.get(0).getColor()-1)*(-1);
		
		if (threatType == HORIZONTAL) {
			int leftMost = Integer.MAX_VALUE;
			int rightMost = Integer.MIN_VALUE;
			for (int i=0; i<components.size(); i++) {
				if (components.get(i).getColumn() < leftMost)
					leftMost = components.get(i).getColumn();
				if (components.get(i).getColumn() > rightMost)
					rightMost = components.get(i).getColumn();
			}
			
			if (hasGaps(leftMost, rightMost, components.get(0).getRow(), components.get(0).getRow())) {
				for (int i=leftMost; i<=rightMost; i++) {
					if (gameState[components.get(0).getRow()][i] == Gomoku.EMPTY)
						costSquares.add(new GomokuMove(otherColor, components.get(0).getRow(), i));
				}
			}
			
			if (leftMost - 1 > -1) {
				if (gameState[components.get(0).getRow()][leftMost-1] == Gomoku.EMPTY)
					costSquares.add(new GomokuMove(otherColor, components.get(0).getRow(), leftMost-1));
				else if (gameState[components.get(0).getRow()][leftMost-1] == otherColor && threatSize < 4)
					threatSize--;
			}
			else if (threatSize < 4)
				threatSize--;
			
			if (rightMost + 1 < 15) {
				if (gameState[components.get(0).getRow()][rightMost+1] == Gomoku.EMPTY)
					costSquares.add(new GomokuMove(otherColor, components.get(0).getRow(), rightMost+1));
				else if (gameState[components.get(0).getRow()][rightMost+1] == otherColor && threatSize < 4)
					threatSize--;
			}
			else if (threatSize < 4)
				threatSize--;
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
			
			if (hasGaps(components.get(0).getColumn(), components.get(0).getColumn(), upMost, downMost)) {
				for (int i=upMost; i<=downMost; i++) {
					if (gameState[i][components.get(0).getColumn()] == Gomoku.EMPTY)
						costSquares.add(new GomokuMove(otherColor, i, components.get(0).getColumn()));
				}
			}
			
			if (upMost - 1 > -1) {  // need to consider a four that is touching another ?
				if (gameState[upMost-1][components.get(0).getColumn()] == Gomoku.EMPTY)
					costSquares.add(new GomokuMove(otherColor, upMost-1, components.get(0).getColumn()));
				else if (gameState[upMost-1][components.get(0).getColumn()] == otherColor && threatSize < 4)
					threatSize--;
			}
			else if (threatSize < 4)
				threatSize--;
			
			if (downMost + 1 < 15) {
				if (gameState[downMost+1][components.get(0).getColumn()] == Gomoku.EMPTY)
					costSquares.add(new GomokuMove(otherColor, downMost+1, components.get(0).getColumn()));
				else if (gameState[downMost+1][components.get(0).getColumn()] == otherColor && threatSize < 4)
					threatSize--;
			}
			else if (threatSize < 4)
				threatSize--;
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
			
			if (hasGaps(leftMost, rightMost, upMost, downMost)) {
				for (int i=upMost, j=leftMost; i<=downMost && j<=rightMost; i++, j++) {
					if (gameState[i][j] == Gomoku.EMPTY)
						costSquares.add(new GomokuMove(otherColor, i, j));
				}
			}
			
			if (threatType == UP_RIGHT) {
				if (rightMost + 1 < 15 && upMost - 1 > -1) { 
					if (gameState[upMost-1][rightMost+1] == Gomoku.EMPTY)
						costSquares.add(new GomokuMove(otherColor, upMost-1, rightMost+1));
					else if (gameState[upMost-1][rightMost+1] == otherColor && threatSize < 4)
						threatSize--;
				}
				else if (threatSize < 4)
					threatSize--;
				
				if (leftMost - 1 > -1 && downMost + 1 < 15) {
					if (gameState[downMost+1][leftMost-1] == Gomoku.EMPTY)
						costSquares.add(new GomokuMove(otherColor, downMost+1, leftMost-1));
					else if (gameState[downMost+1][leftMost-1] == otherColor && threatSize < 4)
						threatSize--;					
				}
				else if (threatSize < 4)
					threatSize--;
			}
			else if (threatType == DOWN_RIGHT) {
				if (rightMost + 1 < 15 && downMost + 1 < 15) {			
					if (gameState[downMost+1][rightMost+1] == Gomoku.EMPTY)
						costSquares.add(new GomokuMove(otherColor, downMost+1, rightMost+1));
					else if (gameState[downMost+1][rightMost+1] == otherColor && threatSize < 4)
						threatSize--;
				}
				else if (threatSize < 4)
					threatSize--;
				
				if (leftMost - 1 > -1 && upMost - 1 > -1) {
					costSquares.add(new GomokuMove((components.get(0).getColor()-1)*(-1), upMost-1, leftMost-1));
					
					if (gameState[upMost-1][leftMost-1] == Gomoku.EMPTY)
						costSquares.add(new GomokuMove(otherColor, upMost-1, leftMost-1));
					else if (gameState[upMost-1][leftMost-1] == otherColor && threatSize < 4)
						threatSize--;
				}
				else if (threatSize < 4)
					threatSize--;
			}
		}
			
		return costSquares;
	}
	
	private boolean hasGaps(int leftMost, int rightMost, int upMost, int downMost) {
		if (rightMost - leftMost >= threatSize)
			return true;
		else if (upMost - downMost >= threatSize)
			return true;
		else
			return false;
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
