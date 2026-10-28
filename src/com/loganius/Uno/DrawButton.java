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

	private UIGame game;
	private CachedImage buttonUp;
	private CachedImage buttonDown;
	private CachedImage button;

	DrawButton(UIGame game) {
		super();

		CachedImage[] button = game.getTheme().getDrawButton();
		this.game = game;
		buttonUp = button[0];
		buttonDown = button[1];
		this.button = buttonUp;

		addMouseListener(new ClickListener());
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(button.get(), 0, 0, this);
	}
	
	public void addNotify() {
		super.addNotify();
		buttonUp.using();
		buttonDown.using();
	}
	
	public void removeNotify() {
		super.removeNotify();
		buttonUp.stopUsing();
		buttonDown.stopUsing();
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
