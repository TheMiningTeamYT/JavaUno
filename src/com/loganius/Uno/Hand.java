package com.loganius.Uno;
import java.awt.*;
import java.util.Vector;

/**
 * A player's hand in a game of Uno.
 * TODO: Figure out how to sort a hand by color in 1.1
 */
class Hand {
	private int orientation;
	protected Vector cards = new Vector();
	protected int x;
	protected int y;
	private int width;
	private int height;
	private boolean playable;
	private boolean up;
	private Game game;
	
	/**
	 * Basic constructor for a new hand.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param parent
	 * @param orientation
	 * @param playable
	 */
	Hand(Game game, int x, int y, int width, int height, int orientation, boolean playable, boolean up) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.orientation = orientation;
		this.playable = playable;
		this.up = up;
		this.game = game;
	}
	
	/**
	 * Add a card to the hand.
	 * @param card
	 */
	void add(Card card) {
		boolean added = false;

		if (cards.size() != 0) {
			for (int i = 0; i < cards.size(); i++) {
				if (card.compareTo((Card)cards.elementAt(i)) >= 0) {
					cards.insertElementAt(card, i);
					added = true;
					break;
				}
			}
		}

		if (!added) {
			cards.addElement(card);
		}
		
		for (int i = 0; i < cards.size(); i++) {
			game.setLayer((Card)cards.elementAt(i), i);
		}

		game.add(card);
		onResize();
	}
	
	/**
	 * Remove a card from the hand.
	 * @param card
	 */
	void remove(Card card) {
		game.remove(card);
		cards.removeElement(card);
		onResize();
	}
	
	Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}
	
	int getOrientation() {
		return orientation;
	}
	
	void setOrientation(int orientation) {
		this.orientation = orientation;
		for (int i = 0; i < cards.size(); i++) {
			((Card) cards.elementAt(i)).setOrientation(orientation);
		}
	}
	
	boolean getPlayable() {
		return playable;
	}
	
	void setPlayable(boolean playable) {
		this.playable = playable;
		for (int i = 0; i < cards.size(); i++) {
			((Card) cards.elementAt(i)).setPlayable(playable);
		}
	}
	
	boolean getUp() {
		return up;
	}

	void setUp(boolean up) {
		this.up = up;
		for (int i = 0; i < cards.size(); i++) {
			((Card) cards.elementAt(i)).setUp(up);
		}
	}
	
	Card getCardByType(int cardType) {
		for (int i = 0; i < cards.size(); i++) {
			Card card = (Card) cards.elementAt(i);
			if (card.getType() == cardType) {
				return card;
			}
		}
		return null;
	}
	
	void setTypePlayable(int cardType, boolean playable) {
		for (int i = 0; i < cards.size(); i++) {
			Card card = (Card) cards.elementAt(i);
			if (card.getType() == cardType) {
				card.setPlayable(playable);
			}
		}
	}
	
	boolean contains(int cardType) {
		for (int i = 0; i < cards.size(); i++) {
			Card card = (Card) cards.elementAt(i);
			if (card.getType() == cardType) {
				return true;
			}
		}
		return false;
	}
	
	void removeAll() {
		for (int i = 0; i < cards.size(); i++) {
			game.remove((Card) cards.elementAt(i));
		}
		cards.removeAllElements();
	}
	
	/**
	 * Tests if any cards in the current hand can be played right now.
	 * @return
	 */
	boolean canBePlayed() {
		for (int i = 0; i < cards.size(); i++) {
			if (game.isLegal((Card)cards.elementAt(i))) {
				return true;
			}
		}
		return false;
	}
	
	Game getGame() {
		return game;
	}
	
	int numCards() {
		return cards.size();
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	void onResize(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		onResize();
	}
	
	/*
	 * Callback function for when the hand gets resized on the screen,
	 * or when the contents of the hand changes.
	 * This handles repositioning all of the cards.
	 */
	void onResize() {
		int cardWidth = Card.getWidth(orientation);
		int cardHeight = Card.getHeight(orientation);
		
		if (orientation == 0 || orientation == 180) {
			// If the hand is oriented vertically.
			int totalWidth = cardWidth * cards.size();
			if (totalWidth > width) {
				for (int i = 0; i < cards.size(); i++) {
					((Card)cards.elementAt(i)).setBounds(x + ((width - cardWidth)*i)/(cards.size() - 1), y, cardWidth, cardHeight);
				}
			} else {
				int startX = x + (width - totalWidth)/2;
				for (int i = 0; i < cards.size(); i++) {
					((Card)cards.elementAt(i)).setBounds(cardWidth * i + startX, y, cardWidth, cardHeight);
				}
			}
		} else {
			// If the hand is oriented horizontally.
			int totalHeight = cardHeight * cards.size();
			if (totalHeight > height) {
				for (int i = 0; i < cards.size(); i++) {
					((Card)cards.elementAt(i)).setBounds(x, y + ((height - cardHeight) * i) / (cards.size() - 1), cardWidth, cardHeight);
				}
			} else {
				int startY = y + (height - totalHeight)/2;
				for (int i = 0; i < cards.size(); i++) {
					((Card)cards.elementAt(i)).setBounds(x, cardHeight*i + startY, cardWidth, cardHeight);
				}
			}
		}
	}
}
