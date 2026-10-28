package com.loganius.Uno;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// TODO: Implement animating the card from the hand of the person who played it to the played pile.
/**
 * A card in the game of Uno.
 */
class UICard extends JComponent implements Card {
	private static final long serialVersionUID = 1L;

	private int type;
	private UIHand parent;
	private UIHand oldParent = null;
	private static int width = 80;
	private static int height = 120;
	private boolean playable;
	private boolean up;
	private int orientation;
	private boolean dirty = true;

	public static int getWidth(int orientation) {
		if (orientation == 0 || orientation == 180) {
			return width;
		} else {
			return height;
		}
	}
	
	public static synchronized void setWidth(int width) {
		UICard.width = width;
	}
	
	public static int getHeight(int orientation) {
		if (orientation == 0 || orientation == 180) {
			return height;
		} else {
			return width;
		}
	}
	
	public static synchronized void setHeight(int height) {
		UICard.height = height;
	}

	public UICard(int type, UIHand parent) {
		super();
		this.type = type;
		this.parent = parent;
		playable = parent.getPlayable();
		orientation = parent.getOrientation();
		up = parent.getUp();
		
		DragListener listener = new DragListener(this);
		addMouseListener(listener);
		addMouseMotionListener(listener);

		parent.add(this);
	}

	public void transfer(Hand destination) {
		UIHand dest = (UIHand)destination;
		oldParent = parent;
		parent.remove(this);
		playable = dest.getPlayable();
		orientation = dest.getOrientation();
		up = dest.getUp();
		dest.add(this);
		parent = dest;
	}
	
	public int getType() {
		return type;
	}
	
	public CardType getCardType() {
		UIGame game = (UIGame)getGame();
		return game.getDeck().getCard(type);
	}
	
	public void setType(int type) {
		stopUsingImages();
		this.type = type;
	}
	
	public boolean getPlayable() {
		return playable;
	}

	public void setPlayable(boolean playable) {
		this.playable = playable;
	}

	public void setUp(boolean up) {
		stopUsingImages();
		this.up = up;
	}
	
	public void setOrientation(int orientation) {
		stopUsingImages();
		this.orientation = orientation;
	}
	
	public Game getGame() {
		return parent.getGame();
	}
	
	public boolean inHand(Hand hand) {
		return hand == parent;
	}
	
	public boolean wasInHand(Hand hand) {
		return hand == oldParent;
	}
	
	// TODO: Implement animations for played cards.
	public void played() {
		getGame().discard(this);
		if (oldParent.numCards() == 0) {
			getGame().onTurn();
		} else {
			getCardType().played(this);
		}
	}
	
	public int compareTo(Card other) {
		if (other.getCardType().getColor() != getCardType().getColor()) {
			return other.getCardType().getColor() - getCardType().getColor();
		}
		return other.getCardType().getValue() - getCardType().getValue();
	}
	
	private void usingImages() {
		if (dirty) {
			CardSkin skin = getCardType().getSkin();
			if (up) {
				skin.useFace(orientation);
			} else {
				skin.useBack(orientation);
			}
			dirty = false;
		}
	}
	
	private void stopUsingImages() {
		if (!dirty) {
			CardSkin skin = getCardType().getSkin();
			if (up) {
				skin.stopUsingFace(orientation);
			} else {
				skin.stopUsingBack(orientation);
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
		CardSkin skin = getCardType().getSkin();
		usingImages();
		if (up) {
			Image front = skin.getFace(orientation);
			g.drawImage(front, 0, 0, this);
		} else {
			Image back = skin.getBack(orientation);
			g.drawImage(back, 0, 0, this);
		}
	}
	
	private class DragListener extends MouseAdapter implements MouseMotionListener {
		private boolean active = false;
		private int startX;
		private int startY;
		private int startLayer = 0;
		private Rectangle startBounds = null;
		private UICard card;
		
		DragListener(UICard parent) {
			this.card = parent;
		}
		
		public void mouseClicked(MouseEvent e) {
			if (playable && !((UIGame)getGame()).isInterrupted() && getGame().isLegal(card)) {
				played();
			}
		}

		public void mouseDragged(MouseEvent e) {
			UIGame game = (UIGame)getGame();
			if (playable && !game.isInterrupted()) {
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
					game.setLayer(card, JLayeredPane.DRAG_LAYER.intValue());
				}
			}
		}
		
		public void mouseReleased(MouseEvent e) {
			UIGame game = (UIGame)getGame();
			if (playable && !game.isInterrupted()) {
				active = false;
				Rectangle bounds = getBounds();
				Rectangle discardBounds = game.getDiscardHandBounds();
				if (startBounds != null) {
					game.setLayer(card, startLayer);
					setBounds(startBounds);
					repaint();
				}
				if (e.getX() + bounds.x >= discardBounds.x - 10 && e.getX() + bounds.x <= discardBounds.x + discardBounds.width + 10 &&
					e.getY() + bounds.y >= discardBounds.y - 10 && e.getY() + bounds.y <= discardBounds.y + discardBounds.height + 10 &&
					getGame().isLegal(card)) {
					played();
				}
			}
		}
		
		public void mouseMoved(MouseEvent e) {}
	}
}
