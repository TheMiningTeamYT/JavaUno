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
	private boolean up;
	private int orientation;
	private boolean dirty = true;

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

	Card(int type, Hand parent) {
		super();
		this.type = type;
		this.parent = parent;
		playable = parent.getPlayable();
		orientation = parent.getOrientation();
		up = parent.getUp();
		
		Game game = parent.getGame();
		
		DragListener listener = new DragListener(this);
		addMouseListener(listener);
		addMouseMotionListener(listener);

		parent.add(this);
	}

	void transfer(Hand destination) {
		parent.remove(this);
		playable = destination.getPlayable();
		orientation = destination.getOrientation();
		up = destination.getUp();
		destination.add(this);
		this.parent = destination;
	}
	
	int getType() {
		return type;
	}
	
	CardType getCardType() {
		return getGame().getDeck().getCard(type);
	}
	
	void setType(int type) {
		stopUsingImages();
		this.type = type;
	}
	
	void setPlayable(boolean playable) {
		this.playable = playable;
	}

	void setUp(boolean up) {
		stopUsingImages();
		this.up = up;
	}
	
	void setOrientation(int orientation) {
		stopUsingImages();
		this.orientation = orientation;
	}
	
	Game getGame() {
		return parent.getGame();
	}
	
	// TODO: Implement animations for played cards.
	void played() {
		getGame().discard(this);
		getCardType().played(this);
	}
	
	int compareTo(Card other) {
		if (other.getCardType().getColor() != getCardType().getColor()) {
			return other.getCardType().getColor() - getCardType().getColor();
		}
		return other.getCardType().getValue() - getCardType().getValue();
	}
	
	void usingImages() {
		if (dirty) {
			if (up) {
				getCardType().useFace(orientation);
			} else {
				getCardType().useBack(orientation);
			}
			dirty = false;
		}
	}
	
	void stopUsingImages() {
		if (!dirty) {
			if (up) {
				getCardType().stopUsingFace(orientation);
			} else {
				getCardType().stopUsingBack(orientation);
			}
			dirty = true;
		}
	}
	
	public void addNotify() {
		super.addNotify();
		usingImages();
	}
	
	public void removeNotify() {
		super.removeNotify();
		stopUsingImages();
	}
	
	public void paintComponent(Graphics g) {
		CardType cardType = getCardType();
		usingImages();
		if (up) {
			Image front = cardType.getFace(orientation);
			g.drawImage(front, 0, 0, this);
		} else {
			Image back = cardType.getBack(orientation);
			g.drawImage(back, 0, 0, this);
		}
	}
	
	private class DragListener extends MouseAdapter implements MouseMotionListener {
		private boolean active = false;
		private int startX;
		private int startY;
		private int startLayer = 0;
		private Rectangle startBounds = null;
		private Card card;
		
		DragListener(Card parent) {
			this.card = parent;
		}

		public void mouseDragged(MouseEvent e) {
			if (playable && !getGame().isInterrupted()) {
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
					startLayer = JLayeredPane.getLayer(card);
					getGame().setLayer(card, JLayeredPane.DRAG_LAYER.intValue());
				}
			}
		}
		
		public void mouseReleased(MouseEvent e) {
			if (playable && !getGame().isInterrupted()) {
				active = false;
				Rectangle bounds = getBounds();
				Rectangle discardBounds = getGame().getDiscardHandBounds();
				if (e.getX() + bounds.x >= discardBounds.x - 10 && e.getX() + bounds.x <= discardBounds.x + discardBounds.width + 10 &&
					e.getY() + bounds.y >= discardBounds.y - 10 && e.getY() + bounds.y <= discardBounds.y + discardBounds.height + 10) {
					if (getGame().isLegal(card)) {
						played();
						return;
					}
				}
			}
			if (startBounds != null) {
				getGame().setLayer(card, startLayer);
				setBounds(startBounds);
				repaint();
			}
		}
		
		public void mouseMoved(MouseEvent e) {}
	}
}
