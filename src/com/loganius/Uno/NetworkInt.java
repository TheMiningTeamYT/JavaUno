package com.loganius.Uno;

import java.io.*;

public class NetworkInt implements Serializable {
	private static final long serialVersionUID = 1L;

	private int slot;
	
	public NetworkInt(Game game, int slot, int val) {
		this.slot = slot;
		game.requestTrackingSlot(slot, val);
	}
	
	public void set(Game game, int val) {
		game.updateSlot(slot, val);
	}
	
	public int get(Game game) {
		return game.getSlot(slot);
	}
	
	public void release(Game game) {
		game.releaseTrackingSlot(slot);
	}
}
