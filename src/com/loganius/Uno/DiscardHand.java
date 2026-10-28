package com.loganius.Uno;

public class DiscardHand extends StandardHand {
	public DiscardHand(Game game) {
		super(game, false);
	}
	
	public void add(Card card) {
		cards.addElement(card);
		if (cards.size() > 2) {
			remove((UICard)cards.firstElement());
		}
	}

	public Card getLastCard() {
		if (cards.size() > 0) {
			return (UICard)cards.lastElement();
		} else {
			return null;
		}
	}
	
	public Card getSecondToLastCard() {
		if (cards.size() > 1) {
			return (UICard)cards.firstElement();
		} else {
			return null;
		}
	}
}
