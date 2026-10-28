package com.loganius.Uno;

import java.util.Vector;

public class StandardHand implements Hand {
	protected Vector cards = new Vector();
	protected boolean playable;
	protected Game game;
	
	public StandardHand(Game game, boolean playable) {
		this.playable = playable;
		this.game = game;
	}
	
	/**
	 * Add a card to the hand.
	 * @param card
	 */
	public void add(Card card) {
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
	}

	/**
	 * Remove a card from the hand.
	 * @param card
	 */
	public void remove(Card card) {
		cards.removeElement(card);
	}

	public boolean getPlayable() {
		return playable;
	}

	public void setPlayable(boolean playable) {
		this.playable = playable;
		for (int i = 0; i < cards.size(); i++) {
			((Card)cards.elementAt(i)).setPlayable(playable);
		}
	}

	public Card getCardByType(int cardType) {
		for (int i = 0; i < cards.size(); i++) {
			Card card = (Card)cards.elementAt(i);
			if (card.getType() == cardType) {
				return card;
			}
		}
		return null;
	}

	public void setTypePlayable(int cardType, boolean playable) {
		for (int i = 0; i < cards.size(); i++) {
			Card card = (Card)cards.elementAt(i);
			if (card.getType() == cardType) {
				card.setPlayable(playable);
			}
		}
	}

	public boolean contains(int cardType) {
		for (int i = 0; i < cards.size(); i++) {
			Card card = (Card)cards.elementAt(i);
			if (card.getType() == cardType) {
				return true;
			}
		}
		return false;
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

	public void removeAll() {
		cards.removeAllElements();
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

	public int numCards() {
		return cards.size();
	}
}
