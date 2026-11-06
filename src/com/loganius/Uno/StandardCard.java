package com.loganius.Uno;

class StandardCard extends BasicCard implements Card {
	private Hand parent;
	private Hand oldParent = null;
	
	public StandardCard(int ID, int type, Hand parent) {
		super(ID, type, parent.getPlayable());
		this.parent = parent;
		parent.add(this);
	}
	
	public StandardCard(SimpleCard card, Hand parent) {
		super(card);
		parent.add(this);
	}
	
	public void transfer(Hand destination) {
		oldParent = parent;
		parent.remove(this);
		setPlayable(destination.getPlayable());
		destination.add(this);
		parent = destination;
	}
	
	public void setType(int type) {
		super.setType(type);
	}
	
	CardType getCardType() {
		return getGame().getDeck().getCard(getType());
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
	
	public void setPlayable(boolean playable) {
		super.setPlayable(playable);
	}
	
	public int getColor() {
		return getCardType().getColor();
	}
	
	public int getValue() {
		return getCardType().getValue();
	}

	public int compareTo(Card other) {
		if (other.getColor() != getColor()) {
			return other.getColor() - getColor();
		}
		return other.getValue() - getValue();
	}
}
