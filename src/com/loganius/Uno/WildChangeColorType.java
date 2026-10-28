package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class WildChangeColorType extends CardType {
	private JPanel buttonSpace = new JPanel();
	private ComponentAdapter listener = new Util.Centering(buttonSpace);

	WildChangeColorType(CardSkin skin) {
		super(skin, Color.WILD, Value.CHANGE_COLOR, 4);
		buttonSpace.setLayout(new GridBagLayout());
		buttonSpace.setOpaque(false);
	}
	
	WildChangeColorType(CardType parent) {
		super(parent);
		buttonSpace.setLayout(new GridBagLayout());
		buttonSpace.setOpaque(false);
	}
	
	void played(Card parent) {
		if (parent.getGame() instanceof UIGame) {
			UIGame game = (UIGame)parent.getGame();
			JPanel customUISpace = game.getCustomUISpace();
			Rectangle size = customUISpace.getBounds();
			JLabel instruction = new JLabel("Select a color", SwingConstants.CENTER);
			GridBagConstraints c = new GridBagConstraints();
			
			Util.commonComponentInit(instruction, game.getTextColor());
			
			buttonSpace.removeAll();
			buttonSpace.setBounds(size.width / 4, size.height / 4, size.width / 2, size.height / 2);
	
			c.gridx = 0;
			c.weightx = 1;
			c.weighty = 1;
			c.gridwidth = 2;
			c.gridheight = 1;
			c.fill = GridBagConstraints.BOTH;
			buttonSpace.add(instruction, c);
			c.gridy = 1;
			c.gridwidth = 1;
			c.gridheight = 1;
			buttonSpace.add(new ColorButton(Color.RED, "Red", parent), c);
			c.gridx++;
			buttonSpace.add(new ColorButton(Color.YELLOW, "Yellow", parent), c);
			c.gridx = 0;
			c.gridy++;
			buttonSpace.add(new ColorButton(Color.GREEN, "Green", parent), c);
			c.gridx++;
			buttonSpace.add(new ColorButton(Color.BLUE, "Blue", parent), c);
			
			customUISpace.setLayout(null);
			customUISpace.add(buttonSpace);
			customUISpace.setVisible(true);
			customUISpace.addComponentListener(listener);
			
			parent.getGame().setHandPlayable(false);
		}
	}

	protected void cardAction(Card parent, int color) {
		parent.setType(Deck.RED_CHANGE_COLOR + color);
		parent.getGame().onTurn();
	};
	
	private class ColorButton extends JButton implements ActionListener {
		private int color;
		private Card parent;

		ColorButton(int color, String text, Card parent) {
			super(text);
			this.color = color;
			this.parent = parent;
			
			setFont(Util.getScaledFont());
			addActionListener(this);
			addComponentListener(Util.ResizeListener);
		}
		
		public void actionPerformed(ActionEvent e) {
			if (parent.getGame() instanceof UIGame) {
				UIGame game = (UIGame)parent.getGame();
				JPanel customUISpace = game.getCustomUISpace();
				customUISpace.removeAll();
				customUISpace.removeComponentListener(listener);
				customUISpace.setVisible(false);
			}
			cardAction(parent, color);
		}
	}
}