package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Button that the user can use to draw a card (or more).
 * TODO: Rotate the button asset to be fancy.
 */
class UnoButton extends JComponent {
	private static final long serialVersionUID = 1L;
	private static int width = 240;
	private static int height = 240;
	
	private Game game;
	private Image buttonUp = Util.bufferImage(Util.getImage(getClass().getResource("Assets/uno.gif")));
	private Image buttonDown = Util.bufferImage(Util.getImage(getClass().getResource("Assets/uno_down.gif")));
	private Image button = buttonUp;

	UnoButton(Game game) {
		super();
		this.game = game;
		setVisible(false);
		addMouseListener(new ClickListener());
	}
	
	public void paintComponent(Graphics g) {
		Rectangle bounds = getBounds();
		g.drawImage(button, 0, 0, bounds.width, bounds.height, this);
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}
	
	// TODO: Animate the draw button.
	private class ClickListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (!game.isInterrupted()) {
				game.callUno(0);
			}
		}
		
		public void mousePressed(MouseEvent e) {
			if (!game.isInterrupted()) {
				button = buttonDown;
				repaint();
			}
		}
		
		public void mouseReleased(MouseEvent e) {
			if (!game.isInterrupted()) {
				button = buttonUp;
				repaint();
			}
		}
	}
}
