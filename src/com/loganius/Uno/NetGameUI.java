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
	private int activePlayer = 0;

	NetGameUI(NetworkGameClient parent) {
		this.parent = parent;
		setLayout(null);
		setOpaque(false);
		
		for (int i = 0; i < players.length; i++) {
			players[i].setForeground(inactivePlayerColor);
			players[i].addComponentListener(Util.getTextResizeListener());
			add(players[i]);
		}

		startButton.setVisible(false);
		startButton.addComponentListener(null);
		startButton.addActionListener(this);
		startButton.addComponentListener(Util.getTextResizeListener());
		add(startButton);

		addComponentListener(new ResizeListener());
		onResize();

		this.parent.add(this, JLayeredPane.MODAL_LAYER);
	}
	
	void setPlayers(String[] players) {
		for (int i = 0; i < players.length; i++) {
			this.players[i].setText(players[i]);
		}
		repaint();
	}
	
	void start(int player, int numPlayers) {
		int adjustment = player;
		JLabel temp1;
		JLabel temp2;

		if (adjustment != 0) {
			for (int i = 0; i < adjustment; i++) {
				temp1 = players[numPlayers - 1];
				for (int j = 0; j < numPlayers; j++) {
					temp2 = players[j];
					players[j] = temp1;
					temp1 = temp2;
				}
			}

			players[0].setHorizontalAlignment(SwingConstants.CENTER);
			players[1].setHorizontalAlignment(SwingConstants.LEFT);
			players[2].setHorizontalAlignment(SwingConstants.CENTER);
			players[3].setHorizontalAlignment(SwingConstants.RIGHT);
			onResize();
		}

		for (int i = numPlayers; i < players.length; i++) {
			players[i].setVisible(false);
		}
		startButton.setVisible(false);
	}
	
	void showStartButton() {
		startButton.setVisible(true);
	}
	
	void setActivePlayer(int player) {
		players[activePlayer].setForeground(inactivePlayerColor);
		players[player].setForeground(activePlayerColor);
		activePlayer = player;
		System.out.println(player);
		repaint();
	}
	
	private void onResize() {
		Rectangle bounds = getBounds();
		int cardHeight = Card.getHeight(0);

		players[0].setBounds(0, bounds.height - cardHeight - (int)(Util.scale(20)) - 50, bounds.width, 100);
		players[1].setBounds(cardHeight + (int)(Util.scale(10)), bounds.height / 2 - 50, bounds.width / 2, 100);
		players[2].setBounds(0, cardHeight + (int)(Util.scale(10)) - 50, bounds.width, 100);
		players[3].setBounds(bounds.width - cardHeight - (int)(Util.scale(10)) - bounds.width / 2, bounds.height / 2 - 50, bounds.width / 2, 100);
		startButton.setBounds(bounds.width / 2 - (bounds.height * 3) / 32, (bounds.height * 7) / 16, (bounds.height * 3) / 16, bounds.height / 8);
	}
	
	public void actionPerformed(ActionEvent e) {
		parent.requestStart();
	}
	
	private class ResizeListener extends ComponentAdapter {
		public void componentResized(ComponentEvent e) {
			onResize();
		}
	}
}
