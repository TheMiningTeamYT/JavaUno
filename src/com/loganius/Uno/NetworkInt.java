package com.loganius.Uno;

import java.io.*;

class NetworkInt implements Serializable {
	private static final long serialVersionUID = 1L;

	private int slot;
	
	NetworkInt(Game game, int slot, int val) {
		this.slot = slot;
		game.requestTrackingSlot(slot, val);
	}
	
	void set(Game game, int val) {
		game.updateSlot(slot, val);
	}
	
	int get(Game game) {
		return game.getSlot(slot);
	}
	
	void release(Game game) {
		game.releaseTrackingSlot(slot);
	}
}
