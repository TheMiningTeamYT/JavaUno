package com.loganius.Uno;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SevenZero extends Rule {
	private JLabel instruction = new JLabel("Choose player to swap with", SwingConstants.CENTER);

	SevenZero() {
		instruction.setForeground(Util.WHITE);
		instruction.setFont(Util.getScaledFont());
		instruction.addComponentListener(Util.ResizeListener);
	}
	
	void bind(Game game) {
		Deck deck = game.getDeck();

		deck.overrideCard(Deck.RED_0, new Zero(deck.getCard(Deck.RED_0)));
		deck.overrideCard(Deck.YELLOW_0, new Zero(deck.getCard(Deck.YELLOW_0)));
		deck.overrideCard(Deck.GREEN_0, new Zero(deck.getCard(Deck.GREEN_0)));
		deck.overrideCard(Deck.BLUE_0, new Zero(deck.getCard(Deck.BLUE_0)));
		deck.overrideCard(Deck.RED_7, new Seven(deck.getCard(Deck.RED_7), game));
		deck.overrideCard(Deck.YELLOW_7, new Seven(deck.getCard(Deck.YELLOW_7), game));
		deck.overrideCard(Deck.GREEN_7, new Seven(deck.getCard(Deck.GREEN_7), game));
		deck.overrideCard(Deck.BLUE_7, new Seven(deck.getCard(Deck.BLUE_7), game));
	}
	
	private class Seven extends CardType {
		SelectorTriangle[] triangles = {null, null, null,};

		Seven(CardType parent, Game game) {
			super(parent);
			for (int i = 0; i < 3; i++) {
				triangles[i] = new SelectorTriangle(1.570796327 * (i + 1), 60, i + 1, game);
			}
		}
		
		void played(Card parent) {
			final Game game = parent.getGame();
			JPanel customUISpace = game.getCustomUISpace();
			GridBagConstraints c = new GridBagConstraints();

			customUISpace.setLayout(new GridBagLayout());
			
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 0;
			c.gridheight = 0;
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0;
			c.weighty = 0;
			c.insets = new Insets(10, 10, 10, 10);

			if (game.getPlayers() > 2) {
				c.gridwidth = 3;
				customUISpace.add(triangles[1], c);
				c.gridy++;
			}

			c.gridwidth = 1;
			customUISpace.add(triangles[0], c);

			c.gridx++;
			customUISpace.add(instruction, c);

			if (game.getPlayers() > 3) {
				c.gridx++;
				customUISpace.add(triangles[2], c);
			}
			
			for (int i = 0; i < game.getPlayers() - 1; i++) {
				triangles[i].calculatePoints();
			}
			customUISpace.setVisible(true);
		}
	}
	
	private class Zero extends CardType {
		Zero(CardType parent) {
			super(parent);
		}
		
		void played(Card parent) {
			parent.getGame().rotateHands();
			parent.getGame().onTurn();
		}
	}
	
	private static class SelectorTriangle extends JComponent {
		private static Color green = new Color(0, 208, 1);
		private static double[][] points = {
				{-0.5, -0.5},
				{0, 0.366025403784},
				{0.5, -0.5},
		};
		
		private int[] x = new int[3];
		private int[] y = new int[3];
		private double sin;
		private double cos;
		private double size;
		private int hand;
		private Game game;

		SelectorTriangle(double angle, double size, final int hand, final Game game) {
			this.hand = hand;
			this.game = game;
			this.size = size;
			sin = Math.sin(angle);
			cos = Math.cos(angle);
			
			calculatePoints();
			
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					JPanel customUISpace = game.getCustomUISpace();
					customUISpace.removeAll();
					customUISpace.setVisible(false);
					game.swapHands(0, game.findHand(hand));
					game.onTurn();
				}
			});
			
			addComponentListener(new ComponentAdapter() {
				public void componentMoved(ComponentEvent e) {
					calculatePoints();
				}
				public void componentResized(ComponentEvent e) {
					calculatePoints();
				}
			});

			setOpaque(false);
		}
		
		void calculatePoints() {
			Rectangle bounds = getBounds();
			for (int i = 0; i < 3; i++) {
				x[i] = (int)((points[i][0]*cos + 0.5 - points[i][1]*sin)*size*Util.getScaleFactor());
				y[i] = (int)((points[i][0]*sin + 0.5 + points[i][1]*cos)*size*Util.getScaleFactor());
			}
		}
		
		public void paintComponent(Graphics g) {
			g.setColor(green);
			g.fillPolygon(x, y, 3);
		}
		
		public Dimension getPreferredSize() {
			return new Dimension((int)(size*Util.getScaleFactor()), (int)(size*Util.getScaleFactor()));
		}
	}
}
