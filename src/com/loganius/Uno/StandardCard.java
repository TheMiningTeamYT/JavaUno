package com.loganius.Uno;

class StandardCard implements Card {
	private int type;
	private Hand parent;
	private Hand oldParent = null;
	private boolean playable;
	
	public StandardCard(int type, Hand parent) {
		this.type = type;
		this.parent = parent;
		playable = parent.getPlayable();

		parent.add(this);
	}
	
	public void transfer(Hand destination) {
		oldParent = parent;
		parent.remove(this);
		playable = destination.getPlayable();
		destination.add(this);
		parent = destination;
	}
	
	public int getType() {
		return type;
	}
	
	public CardType getCardType() {
		return getGame().getDeck().getCard(type);
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public boolean getPlayable() {
		return playable;
	}

	public void setPlayable(boolean playable) {
		this.playable = playable;
	}
	
	public Game getGame() {
		return parent.getGame();
	}
	
	public boolean inHand(Hand hand) {
		return hand == parent;
	}
	
	public boolean wasInHand(Hand hand) {
		return hand == oldParent;
	}
	
	// TODO: Implement animations for played cards.
	public void played() {
		if (oldParent.numCards() == 0) {
			getGame().onTurn();
		} else {
			getCardType().played(this);
		}
	}
	
	public int compareTo(Card other) {
		if (other.getCardType().getColor() != getCardType().getColor()) {
			return other.getCardType().getColor() - getCardType().getColor();
		}
		return other.getCardType().getValue() - getCardType().getValue();
	}
}
