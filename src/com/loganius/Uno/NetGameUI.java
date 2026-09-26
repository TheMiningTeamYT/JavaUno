package com.loganius.Uno;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// TODO: Add all the net game UI elements and methods.
class NetGameUI extends JPanel implements ActionListener {
	private NetworkGameClient parent;
	private JLabel[] players = {
			new JLabel("Player", SwingConstants.CENTER),
			new JLabel("Waiting...", SwingConstants.LEFT),
			new JLabel("Waiting...", SwingConstants.CENTER),
			new JLabel("Waiting...", SwingConstants.RIGHT),
	};
	private JButton startButton = new JButton("Start");

	NetGameUI(NetworkGameClient parent) {
		this.parent = parent;
		setLayout(null);
		setOpaque(false);
		
		for (int i = 0; i < players.length; i++) {
			players[i].setForeground(new Color(240, 240, 240));
			add(players[i]);
		}

		startButton.setVisible(false);
		startButton.addActionListener(this);
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
		int adjustment = -player;
		JLabel temp1;
		JLabel temp2;

		if (adjustment != 0) {
			for (int i = 0; i < adjustment; i++) {
				temp1 = players[0];
				for (int j = numPlayers - 1; j >= 0; j--) {
					temp2 = players[j];
					players[j] = temp1;
					temp1 = temp2;
				}
			}
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
	
	private void onResize() {
		Rectangle bounds = getBounds();
		double scaleFactor = parent.getScaleFactor();
		int cardHeight = Card.getHeight(0);
		
		for (int i = 0; i < players.length; i++) {
			players[i].setFont(parent.getDefaultFont());
		}
		startButton.setFont(parent.getDefaultFont());

		players[0].setBounds(0, bounds.height - cardHeight - (int)(20*scaleFactor) - 50, bounds.width, 100);
		players[1].setBounds(cardHeight + (int)(10*scaleFactor), bounds.height / 2 - 50, bounds.width / 2, 100);
		players[2].setBounds(0, cardHeight + (int)(20*scaleFactor) - 50, bounds.width, 100);
		players[3].setBounds(bounds.width - cardHeight - (int)(10*scaleFactor) - bounds.width / 2, bounds.height / 2 - 50, bounds.width / 2, 100);
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
