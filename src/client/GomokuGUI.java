package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import gomoku.Gomoku;
import gomoku.GomokuMove;

/** 
 * @author Graham Stubbs (wgs11@georgetown.edu)
 * @author Cooper Logerfo (cml264@georgetown.edu)
 */
@SuppressWarnings("serial")
public class GomokuGUI extends JFrame implements ActionListener {
	
	private GomokuClient connectedClient;
	
	private GameBoardPanel gamePanel;
	private JButton giveUpButton;
	private JButton resetButton;
	private JButton sendButton;
	private JPanel chatPanel;
	private JPanel optionsPanel;
	private JTextArea chatDisplay;
	private JTextField textEntry;
	private JTextField nameChangeField;
	
	private int userColor;
	private String userName;
	private boolean humanUser;
	
	private boolean gameOver = false;
	
	private static final String TITLE = "Gomoku - ";
	
	public GomokuGUI(GomokuClient gc) {
		this(gc, false);
	}
	
	public GomokuGUI(GomokuClient gc, boolean human) {
		super();
		
		humanUser = human;
		
		userColor = Gomoku.EMPTY;
		connectedClient = gc;
		
		initializePanels();
		this.setBackground(Color.BLACK);

		setVisible(true);
	}
	
	public void displayMessage(String message) {
		chatDisplay.append(message + "\n");
		chatDisplay.setCaretPosition(chatDisplay.getDocument().getLength());
	}
	
	public void gameOver() {
		gameOver = true;
		if(humanUser) {
			gamePanel.removeMouseListener(gamePanel);
		}
	}
	
	private void initializeChatPanel() {
		chatPanel = new JPanel(new BorderLayout());
		
		chatDisplay = new JTextArea();
	    chatDisplay.setEditable(false);
	    JScrollPane textScrollPane = new JScrollPane(chatDisplay);
	    
	    JPanel typingPanel = new JPanel(new BorderLayout());
	    textEntry = new JTextField(15);
	    textEntry.addActionListener(this);
	    typingPanel.add(textEntry, BorderLayout.CENTER);
	    sendButton = new JButton("Send");
	    sendButton.addActionListener(this);
	    typingPanel.add(sendButton, BorderLayout.EAST);
	    
	    chatPanel.add(textScrollPane, BorderLayout.CENTER);
	    chatPanel.add(typingPanel, BorderLayout.SOUTH);
	    
	    add(chatPanel, BorderLayout.EAST);
	}
	
	private void initializeGamePanel() {
		gamePanel = new GameBoardPanel(this, humanUser);
		
		add(gamePanel, BorderLayout.CENTER);
	}
	
	private void initializeOptionsPanel() {
		optionsPanel = new JPanel(new BorderLayout());
		JPanel left = new JPanel(new BorderLayout());
		JPanel right = new JPanel(new FlowLayout());
		
		JLabel changeNameLabel = new JLabel("Change name:");
		left.add(changeNameLabel, BorderLayout.CENTER);
		
		nameChangeField = new JTextField(20);
		nameChangeField.addActionListener(this);
		left.add(nameChangeField, BorderLayout.EAST);
		
		giveUpButton = new JButton("Give Up");
		giveUpButton.addActionListener(this);
		right.add(giveUpButton);
		
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		right.add(resetButton);
		
		optionsPanel.add(left, BorderLayout.WEST);
		optionsPanel.add(right, BorderLayout.CENTER);
		
		add(optionsPanel, BorderLayout.SOUTH);
	}

	private void initializePanels() {
		userName = connectedClient.getUserName();
		updateTitle();
		setSize(750, 500);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);

	    setLayout(new BorderLayout());
		
	    initializeChatPanel();
	    initializeGamePanel();
	    initializeOptionsPanel();
	}
	
	public boolean isGameOver() {
		return gameOver;
	}

	public void makeMove(int row, int col) {
		if (userColor != Gomoku.EMPTY) {
			GomokuMove move = new GomokuMove(userColor, row, col);
			connectedClient.sendPlayMessage(move);
		}
	}
	
	public void newGame() {
		gameOver = false;
		gamePanel.resetBoard();
		if (userColor == Gomoku.BLACK) 
			connectedClient.updatePlayerTurn();
	}
	
	public void placePieceOnBoard(GomokuMove move) {
		gamePanel.drawPiece(move);
	}
	
	public void setColor(int color) {
		userColor = color;
		updateTitle();
	}
	
	private void updateTitle() {
		setTitle(TITLE + userName + " - " + Gomoku.colorAsString(userColor));
	}
	
	public void updateUserName(String user) {
		userName = user;
		updateTitle();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == sendButton || e.getSource() == textEntry) {
			String text = textEntry.getText();
			if (text != null && !text.isEmpty()) {				
				connectedClient.sendChatMessage(text);
				textEntry.setText(null);
			}
		}
		else if (e.getSource() == nameChangeField) {
			String newName = nameChangeField.getText();
			if (newName != null && !newName.isEmpty()) {				
				connectedClient.sendChangeNameMessage(newName);
				nameChangeField.setText(null);
			}
		}
		else if (e.getSource() == resetButton) {
			connectedClient.sendResetMessage();
		}
		else if (e.getSource() == giveUpButton) {
			connectedClient.sendGiveupMessage();
		}
	}
}
