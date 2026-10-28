package com.loganius.Uno;

public interface Hand {
	void add(Card card);
	void remove(Card card);
	boolean getPlayable();
	void setPlayable(boolean playable);
	Card getCardByType(int cardType);
	void setTypePlayable(int cardType, boolean playable);
	boolean contains(int cardType);
	boolean containsColor(int color);
	void removeAll();
	boolean canBePlayed();
	Game getGame();
	int numCards();
}
