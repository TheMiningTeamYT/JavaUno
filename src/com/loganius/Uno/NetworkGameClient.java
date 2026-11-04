package com.loganius.Uno;

public class NetworkGameClient extends UIGame {
	NetworkGameClient(Theme theme, String name, GameHandler handler, ProtocolInterface io) {
		super(theme, name, handler, io);
	}
	
	public boolean isHandPlayer(int turnPos) {
		return findPlayer(turnPos) == getPlayer();
	}
	
	protected void start(Action.State state) {
		hands[getPlayer()].setUp(true);
		super.start(state);
	}
}
