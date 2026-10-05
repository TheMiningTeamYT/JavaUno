package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Button that the user can use to draw a card (or more).
 * TODO: Rotate the button asset to be fancy.
 */
class DrawButton extends JComponent {
	private static final long serialVersionUID = 1L;

	private Game game;
	private Image buttonUp = Util.bufferImage(Util.getImage(getClass().getResource("Assets/draw.gif")));
	private Image buttonDown = Util.bufferImage(Util.getImage(getClass().getResource("Assets/draw_down.gif")));
	private Image button = buttonUp;

	DrawButton(Game game) {
		super();
		this.game = game;
		addMouseListener(new ClickListener());
	}
	
	public void paintComponent(Graphics g) {
		Rectangle bounds = getBounds();
		g.drawImage(button, 0, 0, bounds.width, bounds.height, this);
	}
	
	// TODO: Animate the draw button.
	private class ClickListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (!game.isInterrupted()) {
				game.onDraw();
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
