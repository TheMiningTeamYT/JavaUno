package com.loganius.Uno;

import java.awt.Image;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class WildChangeColorType extends CardType {
	private static final long serialVersionUID = 1L;
	private Card parentCard;
	private JPanel buttonSpace = new JPanel();
	private ComponentListener listener = new ResizeListener();

	WildChangeColorType(URL front, Image[] back) {
		super(front, back, Color.WILD, Value.CHANGE_COLOR, true);
		buttonSpace.setLayout(new GridBagLayout());
		buttonSpace.setOpaque(false);
	}
	
	void played(Card parent) {
		JPanel customUISpace = parent.getGame().getCustomUISpace();
		Rectangle size = customUISpace.getBounds();
		JLabel instruction = new JLabel("Select a color", SwingConstants.CENTER);
		GridBagConstraints c = new GridBagConstraints();
		this.parentCard = parent;
		
		buttonSpace.removeAll();
		buttonSpace.setBounds(size.width / 4, size.height / 4, size.width / 2, size.height / 2);
		
		instruction.setForeground(new java.awt.Color(0, 0, 0));
		instruction.setFont(Util.getScaledFont());
		instruction.addComponentListener(Util.getTextResizeListener());

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		buttonSpace.add(instruction, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		buttonSpace.add(new ColorButton(Color.RED, "Red"), c);
		c.gridx++;
		buttonSpace.add(new ColorButton(Color.YELLOW, "Yellow"), c);
		c.gridx++;
		buttonSpace.add(new ColorButton(Color.GREEN, "Green"), c);
		c.gridx++;
		buttonSpace.add(new ColorButton(Color.BLUE, "Blue"), c);
		
		customUISpace.setLayout(null);
		customUISpace.add(buttonSpace);
		customUISpace.setVisible(true);
		customUISpace.addComponentListener(listener);
		
		parent.getGame().setHandPlayable(false);
	}
	
	// TODO: Add colored wild cards to the deck and set this properly.
	protected void cardAction(Card parent, int color) {
		parent.setType(Deck.RED_CHANGE_COLOR + color);
	};
	
	private class ColorButton extends JButton implements ActionListener {
		private int color;
		ColorButton(int color, String text) {
			super(text);
			this.color = color;
			
			setFont(Util.getScaledFont());
			addActionListener(this);
			addComponentListener(Util.getTextResizeListener());
		}
		
		public void actionPerformed(ActionEvent e) {
			JPanel customUISpace = parentCard.getGame().getCustomUISpace();
			customUISpace.removeAll();
			customUISpace.removeComponentListener(listener);
			customUISpace.setVisible(false);

			cardAction(parentCard, color);
			parentCard.getGame().onTurn();
		}
	}
	
	private class ResizeListener extends ComponentAdapter {
		public void componentResized(ComponentEvent e) {
			Rectangle size = e.getComponent().getBounds();
			buttonSpace.setBounds(size.width / 4, size.height / 4, size.width / 2, size.height / 2);
		}
	}
}