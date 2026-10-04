package com.loganius.Uno;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// TODO: Implement animating the card from the hand of the person who played it to the played pile.
/**
 * A card in the game of Uno.
 */
class Card extends JComponent {
	private static final long serialVersionUID = 1L;

	private int type;
	private Hand parent;
	private static int width = 80;
	private static int height = 120;
	private boolean playable;
	private Card card = this;

	Card(int type, Hand parent) {
		super();
		this.type = type;
		this.parent = parent;
		playable = parent.getPlayable();
		
		DragListener listener = new DragListener();
		addMouseListener(listener);
		addMouseMotionListener(listener);
		
		parent.getGame().add(this);
		parent.add(this);
	}

	void transfer(Hand destination) {
		parent.remove(this);
		destination.add(this);
		playable = destination.getPlayable();
		this.parent = destination;
	}
	
	int getType() {
		return type;
	}
	
	CardType getCardType() {
		return parent.getGame().getDeck().getCard(type);
	}
	
	void setType(int type) {
		this.type = type;
	}

	static int getWidth(int orientation) {
		if (orientation == 0 || orientation == 180) {
			return width;
		} else {
			return height;
		}
	}
	
	static void setWidth(int width) {
		Card.width = width;
	}
	
	static int getHeight(int orientation) {
		if (orientation == 0 || orientation == 180) {
			return height;
		} else {
			return width;
		}
	}
	
	static void setHeight(int height) {
		Card.height = height;
	}
	
	void setPlayable(boolean playable) {
		this.playable = playable;
	}
	
	boolean getPlayable() {
		return playable;
	}
	
	Game getGame() {
		return parent.getGame();
	}
	
	// TODO: Implement animations for played cards.
	void played() {
		parent.getGame().discard(this);
		getCardType().played(this);
	}
	
	int compareTo(Card other) {
		if (other.getCardType().getColor() != getCardType().getColor()) {
			return other.getCardType().getColor() - getCardType().getColor();
		}
		return other.getCardType().getValue() - getCardType().getValue();
	}
	
	public void paintComponent(Graphics g) {
		CardType cardType = parent.getGame().getDeck().getCards()[type];
		if (parent.getUp()) {
			Image front = cardType.getFace()[parent.getOrientation() / 90];
			g.drawImage(front, 0, 0, getWidth(), getHeight(), this);
		} else {
			Image back = cardType.getBack()[parent.getOrientation() / 90];
			g.drawImage(back, 0, 0, getWidth(), getHeight(), this);
		}
	}
	
	class DragListener extends MouseAdapter implements MouseMotionListener {
		private boolean active = false;
		private int startX;
		private int startY;
		private int startLayer;
		private Rectangle startBounds = null;

		public void mouseDragged(MouseEvent e) {
			if (playable) {
				if (active) {
					Rectangle bounds = getBounds();
					bounds.x += e.getX() - startX;
					bounds.y += e.getY() - startY;
					setBounds(bounds);
				} else {
					active = true;
					startX = e.getX();
					startY = e.getY();
					startBounds = getBounds();
					startLayer = parent.getGame().getLayer((Component)card);
					parent.getGame().setLayer(card, JLayeredPane.DRAG_LAYER.intValue());
				}
			}
		}
		
		public void mouseReleased(MouseEvent e) {
			if (playable) {
				active = false;
				Rectangle bounds = getBounds();
				Rectangle discardBounds = parent.getGame().getDiscardHandBounds();
				parent.getGame().setLayer(card, startLayer);
				if (e.getX() + bounds.x >= discardBounds.x - 10 && e.getX() + bounds.x <= discardBounds.x + discardBounds.width + 10 &&
					e.getY() + bounds.y >= discardBounds.y - 10 && e.getY() + bounds.y <= discardBounds.y + discardBounds.height + 10) {
					if (parent.getGame().isLegal(card)) {
						card.played();
						return;
					}
				}

				if (startBounds != null) {
					setBounds(startBounds);
					repaint();
				}
			}
		}
		
		public void mouseMoved(MouseEvent e) {}
	}
}
