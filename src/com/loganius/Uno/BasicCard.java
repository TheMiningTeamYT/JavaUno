package com.loganius.Uno;

import java.io.Serializable;

class BasicCard implements Serializable, SimpleCard {
	private static final long serialVersionUID = 1L;
	private int ID;
	private int type;
	private boolean playable;
	
	BasicCard(int ID, int type, boolean playable) {
		this.ID = ID;
		this.type = type;
		this.playable = playable;
	}
	
	BasicCard(SimpleCard card) {
		this(card.getID(), card.getType(), card.getPlayable());
	}
	
	public int getType() {
		return type;
	}
	
	void setType(int type) {
		this.type = type;
	}
	
	public boolean getPlayable() {
		return playable;
	}

	void setPlayable(boolean playable) {
		this.playable = playable;
	}
	
	public int compareTo(int otherType) {
		return type - otherType;
	}
	
	public int getID() {
		return ID;
	}
}
