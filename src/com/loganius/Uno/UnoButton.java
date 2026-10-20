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
	private CachedImage buttonUp;
	private CachedImage buttonDown;
	private CachedImage button;

	UnoButton(Game game) {
		super();
		
		CachedImage[] button = game.getTheme().getUnoButton();
		this.game = game;
		buttonUp = button[0];
		buttonDown = button[1];
		this.button = buttonUp;

		setVisible(false);
		addMouseListener(new ClickListener());
	}
	
	public void paintComponent(Graphics g) {
		Rectangle bounds = getBounds();
		g.drawImage(button.get(), 0, 0, this);
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
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
