package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GameBoardPanel extends JPanel implements MouseListener{

	public GameBoardPanel() {
		setDoubleBuffered(true);
		
		initializePanel();
	}

	private void initializePanel() {
		setBackground(Color.WHITE);
		
		addMouseListener(this);
	}
	
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
}
