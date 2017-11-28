package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import gomoku.Gomoku;
import gomoku.GomokuMove;

@SuppressWarnings("serial")
public class GameBoardPanel extends JPanel implements MouseListener {
	
	private boolean humanUser;
	private GomokuGUI connectedFrame;
	
	private int[][] gameState = new int[15][15];
	
	public GameBoardPanel(GomokuGUI g) {
		this(g, false);
	}

	public GameBoardPanel(GomokuGUI g, boolean human) {
		setDoubleBuffered(true);
		humanUser = human;
		connectedFrame = g;
		
		initializePanel();
		resetBoard();
	}
	
	public void drawPiece(GomokuMove move) {
		gameState[move.getRow()][move.getColumn()] = move.getColor();
		
		repaint();
	}
	
	private void initializePanel() {
		setBackground(Color.WHITE);
		
		if (humanUser)
			addMouseListener(this);
	}
	
	public int[][] getGameState() {
		return gameState;
	}
	
	public void resetBoard() {
		for (int i=0; i<15; i++) {
			for (int j=0; j<15; j++) {
				gameState[i][j] = Gomoku.EMPTY;
			}
		}
		
		repaint();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int h = getHeight();
		int w = getWidth();
		
		double cubeWidth = w/(16.0);
		double cubeHeight = h/(16.0);
		
		double xGrid = (x/(cubeWidth + 0.0) - 0.5);
		double yGrid = (y/(cubeHeight + 0.0) - 0.5);
		int roundedX;
		int roundedY;
		
		if (xGrid >=0)
			roundedX = (int)xGrid;
		else
			roundedX = -1;
		if (yGrid >=0)
			roundedY = (int)yGrid;
		else
			roundedY = -1;
		
		if (xGrid >= 0 && xGrid < 15 && yGrid >=0 && yGrid < 15)
			connectedFrame.makeMove(roundedY, roundedX);
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
	
	@Override
	public void paint(Graphics g){
		// draw background
		Color backgroundColor = new Color(0.9f, 0.7f, 0.2f, 0.35f);
		g.setColor(backgroundColor); 
		int w = getWidth();  
		int h = getHeight();
		g.fillRect( 0, 0, w, h ); 
		
		double cubeWidth = w/(16.0);
		double cubeHeight = h/(16.0);
		
		// draw lines
		g.setColor(Color.BLACK);
		for (int i=1; i<16; i++) {
			g.drawLine((int)(cubeWidth*i), 0, (int)(cubeWidth*i), h);
			g.drawLine(0, (int)(cubeHeight*i), w, (int)(cubeHeight*i));
		}
		
		// draw pieces played
		// TODO: make this more efficient by just checking for updates, not searching whole board every time
		for (int i=0; i<15; i++) {
			for (int j=0; j<15; j++) {
				if (gameState[i][j] != Gomoku.EMPTY) {
					if (gameState[i][j] == Gomoku.WHITE)
						g.setColor(Color.WHITE);
					else if (gameState[i][j] == Gomoku.BLACK)
						g.setColor(Color.BLACK);
					
					double diameter = Math.min(cubeWidth, cubeHeight);
					g.fillOval((int)(cubeWidth*(j+1) - diameter/2.0), (int)(cubeHeight*(i+1) - diameter/2.0), (int)diameter, (int)diameter);
				}
			}
		}
	}
}
