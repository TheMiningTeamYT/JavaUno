package com.loganius.Uno;

public class DiscardHand extends StandardHand {
	public DiscardHand(Game game) {
		super(game);
	}
	
	public void add(Card card) {
		super.add(card);
		if (cards.size() > 2) {
			remove((Card)cards.firstElement());
		}
	}

	public Card getLastCard() {
		if (cards.size() > 0) {
			return (Card)cards.lastElement();
		} else {
			return null;
		}
	}
	
	public Card getSecondToLastCard() {
		if (cards.size() > 1) {
			return (Card)cards.firstElement();
		} else {
			return null;
		}
	}
}
