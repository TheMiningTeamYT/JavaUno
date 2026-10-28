package com.loganius.Uno;

import java.awt.Rectangle;

//Special hand type which holds the last played card.
class UIDiscardHand extends DiscardHand implements UIHand {
	private int x;
	private int y;

	UIDiscardHand(UIGame game, int x, int y) {
		super(game);
		this.x = x;
		this.y = y;
	}

	public void add(Card card) {
		UIGame game = (UIGame)this.game;
		UICard uiCard = (UICard)card;
		super.add(uiCard);

		game.add(uiCard, 0);
		if (cards.size() > 1) {
			((UICard)cards.firstElement()).setVisible(false);
		}
		onResize();
	}
	
	public int getOrientation() {
		return 0;
	}
	
	public boolean getUp() {
		return true;
	}
	
	Rectangle getBounds() {
		return new Rectangle(x, y, UICard.getWidth(0), UICard.getHeight(0));
	}
	
	void onResize(int x, int y) {
		this.x = x;
		this.y = y;
		onResize();
	}

	void onResize() {
		// TODO: Scale the cards with the screen size.
		int cardWidth = UICard.getWidth(0);
		int cardHeight = UICard.getHeight(0);

		// TODO: Handle animating cards, as this would reset the position of a card in the middle of its animation.
		if (cards.size() > 0) {
			((UICard)cards.lastElement()).setBounds(x, y, cardWidth, cardHeight);
		}
	}
}
