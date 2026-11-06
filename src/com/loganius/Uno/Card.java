package com.loganius.Uno;

public interface Card extends SimpleCard {
	void transfer(Hand destination);
	Game getGame();
	boolean inHand(Hand hand);
	boolean wasInHand(Hand hand);
	int getColor();
	int getValue();
	// TODO: Separate the UI and game logic functions
	int compareTo(Card other);
}
