package com.loganius.Uno;

public interface Hand {
	void add(Card card);
	void remove(Card card);
	boolean getPlayable();
	void setPlayable(boolean playable);
	void setTypePlayable(int cardType, boolean playable);
	boolean contains(int cardType);
	void removeAll();
	int numCards();
	Card getCardByType(int cardType);
	Card getCardByID(int ID);
	boolean containsColor(int color);
	boolean canBePlayed();
	Game getGame();
}
