package com.loganius.Uno;

public interface Plugin {
	void bind(Game game);
	void unbind(Game game);
	void simulate(int type, Object argument, Game game);
	void handleAction(int type, Object argument, Game game);
	// TODO: Add Set State
}
