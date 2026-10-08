package com.loganius.Uno;

interface ActionHandler {
	void actionDryRun(int type, Object argument, Game game);
	void handleAction(int type, Object argument, Game game);
}
