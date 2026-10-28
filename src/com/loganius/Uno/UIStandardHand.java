package com.loganius.Uno;

/**
 * A player's hand in a game of Uno.
 * TODO: Figure out how to sort a hand by color in 1.1
 */
class UIStandardHand extends StandardHand implements UIHand {
	private int orientation;
	private int x;
	private int y;
	private int width;
	private int height;
	private boolean up;
	
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
	UIStandardHand(UIGame game, int x, int y, int width, int height, int orientation, boolean playable, boolean up) {
		super(game, playable);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.orientation = orientation;
		this.up = up;
	}
	
	/**
	 * Add a card to the hand.
	 * @param card
	 */
	public void add(Card card) {
		UIGame game = (UIGame)this.game;
		super.add(card);		
		for (int i = 0; i < cards.size(); i++) {
			game.setLayer((UICard)cards.elementAt(i), i);
		}
		game.add((UICard)card);
		onResize();
	}
	
	/**
	 * Remove a card from the hand.
	 * @param card
	 */
	public void remove(Card card) {
		UIGame game = (UIGame)this.game;
		super.remove(card);
		game.remove((UICard)card);
		onResize();
	}
	
	public boolean getUp() {
		return up;
	}

	public void setUp(boolean up) {
		this.up = up;
		for (int i = 0; i < cards.size(); i++) {
			((UICard)cards.elementAt(i)).setUp(up);
		}
	}
	
	public void removeAll() {
		UIGame game = (UIGame)this.game;
		for (int i = 0; i < cards.size(); i++) {
			game.remove((UICard)cards.elementAt(i));
		}
		super.removeAll();
	}
	
	public int getOrientation() {
		return orientation;
	}
	
	public void setOrientation(int orientation) {
		this.orientation = orientation;
		for (int i = 0; i < cards.size(); i++) {
			((UICard)cards.elementAt(i)).setOrientation(orientation);
		}
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
		int cardWidth = UICard.getWidth(orientation);
		int cardHeight = UICard.getHeight(orientation);
		
		if (orientation == 0 || orientation == 180) {
			// If the hand is oriented vertically.
			int totalWidth = cardWidth * cards.size();
			if (totalWidth > width) {
				for (int i = 0; i < cards.size(); i++) {
					((UICard)cards.elementAt(i)).setBounds(x + ((width - cardWidth)*i)/(cards.size() - 1), y, cardWidth, cardHeight);
				}
			} else {
				int startX = x + (width - totalWidth)/2;
				for (int i = 0; i < cards.size(); i++) {
					((UICard)cards.elementAt(i)).setBounds(cardWidth * i + startX, y, cardWidth, cardHeight);
				}
			}
		} else {
			// If the hand is oriented horizontally.
			int totalHeight = cardHeight * cards.size();
			if (totalHeight > height) {
				for (int i = 0; i < cards.size(); i++) {
					((UICard)cards.elementAt(i)).setBounds(x, y + ((height - cardHeight) * i) / (cards.size() - 1), cardWidth, cardHeight);
				}
			} else {
				int startY = y + (height - totalHeight)/2;
				for (int i = 0; i < cards.size(); i++) {
					((UICard)cards.elementAt(i)).setBounds(x, cardHeight*i + startY, cardWidth, cardHeight);
				}
			}
		}
	}
}
