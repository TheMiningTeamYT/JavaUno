package com.loganius.Uno;

//Special hand type which holds the last played card.
class DiscardHand extends Hand {
	DiscardHand(Game game, int x, int y, int width, int height) {
		super(game, x, y, width, height, 0, false, true);
	}

	void add(Card card) {
		super.getGame().add(card, 0);
		cards.addElement(card);
		if (cards.size() > 2) {
			remove((Card)cards.firstElement());
		}
		onResize();
	}
	
	Card getLastCard() {
		if (cards.size() > 0) {
			return (Card)cards.lastElement();
		} else {
			return null;
		}
	}

	void onResize() {
		// TODO: Scale the cards with the screen size.
		int cardWidth = Card.getWidth(0);
		int cardHeight = Card.getHeight(0);

		// TODO: Handle animating cards, as this would reset the position of a card in the middle of its animation.
		if (cards.size() > 0) {
			((Card)cards.lastElement()).setBounds(x, y, cardWidth, cardHeight);
			for (int i = 0; i < cards.size() - 1; i++) {
				((Card)cards.elementAt(i)).setVisible(false);
			}
		}
	}
}
