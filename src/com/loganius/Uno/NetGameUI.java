package com.loganius.Uno;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// TODO: Add all the net game UI elements and methods.
class NetGameUI extends JPanel implements ActionListener {
	private static Color inactivePlayerColor = new Color(240, 240, 240);
	private static Color activePlayerColor = new Color(0, 208, 1);
	private NetworkGameClient parent;
	private JLabel[] players = {
			new JLabel("Player", SwingConstants.CENTER),
			new JLabel("Waiting...", SwingConstants.LEFT),
			new JLabel("Waiting...", SwingConstants.CENTER),
			new JLabel("Waiting...", SwingConstants.RIGHT),
	};
	private JButton startButton = new JButton("Start");
	private int player = 0;
	private int numPlayers = 4;
	private int activePlayer = 0;

	NetGameUI(NetworkGameClient parent) {
		this.parent = parent;
		setLayout(null);
		setOpaque(false);
		
		for (int i = 0; i < players.length; i++) {
			players[i].setForeground(inactivePlayerColor);
			players[i].addComponentListener(Util.ResizeListener);
			add(players[i]);
		}

		startButton.setVisible(false);
		startButton.addComponentListener(null);
		startButton.addActionListener(this);
		startButton.addComponentListener(Util.ResizeListener);
		add(startButton);

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				onResize();
			}
		});

		this.parent.add(this, JLayeredPane.MODAL_LAYER);
	}
	
	void setPlayers(String[] players) {
		for (int i = 0; i < players.length; i++) {
			this.players[i].setText(players[i]);
		}
		repaint();
	}
	
	void start(int player, int numPlayers) {
		this.player = player;
		this.numPlayers = numPlayers;

		for (int i = numPlayers; i < players.length; i++) {
			players[i].setVisible(false);
		}
		
		players[player].setHorizontalAlignment(SwingConstants.CENTER);
		if (numPlayers == 2) {
			players[(player + 1) % numPlayers].setHorizontalAlignment(SwingConstants.CENTER);
		} else {
			if ((player + 1) % Game.MAX_PLAYERS < numPlayers) {
				players[(player + 1) % Game.MAX_PLAYERS].setHorizontalAlignment(SwingConstants.LEFT);
			}
			if ((player + 2) % Game.MAX_PLAYERS < numPlayers) {
				players[(player + 2) % Game.MAX_PLAYERS].setHorizontalAlignment(SwingConstants.CENTER);
			}
			if ((player + 3) % Game.MAX_PLAYERS < numPlayers) {
				players[(player + 3) % Game.MAX_PLAYERS].setHorizontalAlignment(SwingConstants.RIGHT);
			}
		}
		startButton.setVisible(false);
		onResize();
	}
	
	void showStartButton() {
		Rectangle bounds = getBounds();
		startButton.setVisible(true);
		startButton.setBounds(bounds.width / 2 - (bounds.height * 3) / 32, (bounds.height * 7) / 16, (bounds.height * 3) / 16, bounds.height / 8);
	}
	
	void setActivePlayer(int player) {
		players[activePlayer].setForeground(inactivePlayerColor);
		players[player].setForeground(activePlayerColor);
		activePlayer = player;
		repaint();
	}
	
	private void onResize() {
		Rectangle bounds = getBounds();
		int cardHeight = Card.getHeight(0);

		players[player].setBounds(0, bounds.height - cardHeight - (int)(Util.scale(20)) - 50, bounds.width, 100);
		if (numPlayers == 2) {
			players[(player + 1) % numPlayers].setBounds(0, cardHeight + (int)(Util.scale(10)) - 50, bounds.width, 100);
		} else {
			if ((player + 1) % Game.MAX_PLAYERS < numPlayers) {
				players[(player + 1) % Game.MAX_PLAYERS].setBounds(cardHeight + (int)(Util.scale(10)), bounds.height / 2 - 50, bounds.width / 2, 100);
			}
			if ((player + 2) % Game.MAX_PLAYERS < numPlayers) {
				players[(player + 2) % Game.MAX_PLAYERS].setBounds(0, cardHeight + (int)(Util.scale(10)) - 50, bounds.width, 100);
			}
			if ((player + 3) % Game.MAX_PLAYERS < numPlayers) {
				players[(player + 3) % Game.MAX_PLAYERS].setBounds(bounds.width - cardHeight - (int)(Util.scale(10)) - bounds.width / 2, bounds.height / 2 - 50, bounds.width / 2, 100);
			}
		}
		if (startButton.isVisible()) {
			startButton.setBounds(bounds.width / 2 - (bounds.height * 3) / 32, (bounds.height * 7) / 16, (bounds.height * 3) / 16, bounds.height / 8);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		parent.requestStart();
	}
}
