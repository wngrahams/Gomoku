package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GameBoardPanel extends JPanel implements MouseListener{
	
	private static final int EMPTY = 0b00;
	private static final int WHITE = 0b01;
	private static final int BLACK = 0b10;
	
	private boolean humanUser;
	private int userColor;
	
	private int[][] gameState = new int[15][15];
	
	public GameBoardPanel(int color) {
		this(true, color);
	}

	public GameBoardPanel(boolean human, int color) {
		setDoubleBuffered(true);
		humanUser = human;
		userColor = color;
		
		initializePanel();
		resetBoard();
	}
	
	private void initializePanel() {
		setBackground(Color.WHITE);
		
		if (humanUser)
			addMouseListener(this);
	}
	
	public void resetBoard() {
		for (int i=0; i<15; i++) {
			for (int j=0; j<15; j++) {
				gameState[i][j] = EMPTY;
			}
		}
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
			gameState[roundedX][roundedY] = userColor;
		
		repaint();
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
				if (gameState[i][j] != EMPTY) {
					if (gameState[i][j] == WHITE)
						g.setColor(Color.WHITE);
					else if (gameState[i][j] == BLACK)
						g.setColor(Color.BLACK);
					
					double diameter = Math.min(cubeWidth, cubeHeight);
					g.fillOval((int)(cubeWidth*(i+1) - diameter/2.0), (int)(cubeHeight*(j+1) - diameter/2.0), (int)diameter, (int)diameter);
				}
			}
		}
	}
}
