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
	
	private int[][] gameState = new int[15][15];

	public GameBoardPanel() {
		setDoubleBuffered(true);
		
		initializePanel();
		resetBoard();
	}
	
	private void initializePanel() {
		setBackground(Color.WHITE);
		
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
		// TODO Auto-generated method stub
		
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
		
		// draw lines
		g.setColor(Color.BLACK);
		for (int i=1; i<16; i++) {
			g.drawLine((int)(w/16.0)*i, 0, (int)(w/16.0)*i, h);
			g.drawLine(0, (int)(h/16.0)*i, w, (int)(h/16.0)*i);
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
					
					int diameter = Math.min((int)(w/16.0), (int)(h/16.0));
					g.fillOval((int)(w/16.0)*(i+1) - diameter/2, (int)(h/16.0)*(j+1) - diameter/2, diameter, diameter);
				}
			}
		}
	}
}
