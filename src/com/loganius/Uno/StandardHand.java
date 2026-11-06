package com.loganius.Uno;

public class StandardHand extends SimpleHand implements Hand {
	protected Game game;
	
	public StandardHand(Game game) {
		this.game = game;
	}
	
	public void add(Card card) {
		super.add(card);
	}
	
	public void remove(Card card) {
		super.remove(card);
	}

	public Card getCardByType(int cardType) {
		return (Card)super.getSimpleCardByType(cardType);
	}
	
	public Card getCardByID(int ID) {
		return (Card)super.getSimpleCardByID(ID);
	}

	public boolean containsColor(int color) {
		for (int i = 0; i < cards.size(); i++) {
			Card card = (Card)cards.elementAt(i);
			if (card.getCardType().getColor() == color) {
				return true;
			}
		}
		return false;
	}
	
	public void setPlayable(boolean playable) {
		super.setPlayable(playable);
	}
	
	public void removeAll() {
		super.removeAll();
	}
	
	/**
	 * Tests if any cards in the current hand can be played right now.
	 * @return
	 */
	public boolean canBePlayed() {
		for (int i = 0; i < cards.size(); i++) {
			if (game.isLegal((Card)cards.elementAt(i))) {
				return true;
			}
		}
		return false;
	}

	public Game getGame() {
		return game;
	}
}
