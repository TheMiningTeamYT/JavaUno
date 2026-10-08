package com.loganius.Uno;

import java.io.*;

class NetworkInt implements Serializable {
	private static final long serialVersionUID = 1L;

	private int newValue;
	private int value;
	private int slot;
	private boolean delayedSet = false;
	
	NetworkInt(Game game, int slot, int val) {
		this.slot = slot;
		value = val;
		newValue = val;
		if (game instanceof NetworkGameClient) {
			((NetworkGameClient)game).requestTrackingSlot(slot, val);
		}
	}
	
	void set(Game game, int val) {
		set(game, val, false);
	}
	
	void set(Game game, int val, boolean delayed) {
		if (game instanceof NetworkGameClient) {
			((NetworkGameClient)game).updateSlot(slot, val);
		}
		if (!delayed || !(game instanceof NetworkGameClient)) {
			value = val;
		}
		newValue = val;
		delayedSet = delayed;
	}
	
	int get(Game game) {
		int slotValue;
		if (!(game instanceof NetworkGameClient)) {
			return value;
		}
		slotValue = ((NetworkGameClient)game).getSlot(slot);
		if (delayedSet && slotValue == newValue) {
			return value;
		}
		return slotValue;
	}
	
	void release(Game game) {
		if (game instanceof NetworkGameClient) {
			((NetworkGameClient)game).releaseTrackingSlot(slot);
		}
	}
}
