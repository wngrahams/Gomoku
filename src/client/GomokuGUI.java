package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import gomoku.Gomoku;
import gomoku.GomokuMove;

@SuppressWarnings("serial")
public class GomokuGUI extends JFrame {
	
	private JButton sendButton;
	private JPanel chatPanel;
	private GameBoardPanel gamePanel;
	private JTextArea chatDisplay;
	private JTextField textEntry;
	
	private int userColor;

	public static void main(String args[]) {
		GomokuGUI gg = new GomokuGUI(Gomoku.BLACK);
	}
	
	public GomokuGUI(int color) {
		super();
		userColor = color;
		initializePanels();
		this.setBackground(Color.BLACK);
		
		setVisible(true);
	}
	
	public void makeMove(int x, int y) {
		GomokuMove move = new GomokuMove(x, y, userColor);
		// send move to client
	}
	
	private void initializeChatPanel() {
		chatPanel = new JPanel(new BorderLayout());
		
		chatDisplay = new JTextArea();
	    chatDisplay.setEditable(false);
	    chatDisplay.setPreferredSize(new Dimension(250, 300));
	    JScrollPane textScrollPane = new JScrollPane(chatDisplay);
	    
	    JPanel typingPanel = new JPanel(new BorderLayout());
	    textEntry = new JTextField();
	    typingPanel.add(textEntry, BorderLayout.CENTER);
	    sendButton = new JButton("Send");
	    typingPanel.add(sendButton, BorderLayout.EAST);
	    
	    chatPanel.add(textScrollPane, BorderLayout.CENTER);
	    chatPanel.add(typingPanel, BorderLayout.SOUTH);
	    
	    add(chatPanel, BorderLayout.EAST);
	}
	
	private void initializeGamePanel() {
		gamePanel = new GameBoardPanel(this);
		
		add(gamePanel, BorderLayout.CENTER);
	}

	private void initializePanels() {
		setTitle("Gomoku");
		setSize(750, 500);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);

	    setLayout(new BorderLayout());
		
	    initializeChatPanel();
	    initializeGamePanel();
	}
	
	public void placePieceOnBoard(int x, int y) {
		gamePanel.drawPiece(new GomokuMove(x, y, userColor));
	}
}
