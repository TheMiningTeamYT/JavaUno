package com.loganius.Uno;

public interface Card {
	void transfer(Hand destination);
	int getType();
	CardType getCardType();
	void setType(int type);
	boolean getPlayable();
	void setPlayable(boolean playable);
	Game getGame();
	boolean inHand(Hand hand);
	boolean wasInHand(Hand hand);
	// TODO: Separate the UI and game logic functions
	void played();
	int compareTo(Card other);
}
