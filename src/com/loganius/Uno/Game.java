package com.loganius.Uno;

// TODO: Add the plugin handling functions to this interface.
public interface Game extends ErrorHandler {
	void draw();
	void onTurn();
	void reverse();
	void finishDraw();
	void rotateHands();
	boolean checkUno();
	void play(Card card);
	void callUno(int player);
	Card drawToHand(int turnPos);
	void startWildCardChallenge();
	void swapHands(int target1, int target2);
	void removeFromHand(int turnPos, Card card);

	int getSlot(int cookie);
	void sendInput(int value);
	void unregisterAction(int type);
	void releaseTrackingSlot(int cookie);
	void updateSlot(int cookie, int val);
	void sendAction(int type, Object arg);
	void listenForInput(InputHandler handler);
	void requestTrackingSlot(int cookie, int val);
	void registerAction(int type, Plugin handler);

	Deck getDeck();
	int getPlayer();
	boolean getUno();
	int getPlayers();
	boolean getDrew();
	boolean getEnded();
	int getTurnOrder();
	Card getLastDrawn();
	Card getLastPlayed();
	RuleSet getRuleSet();
	boolean getHandPlayable(int turnPos);
	
	int findTurnPos(Card card);
	boolean isLegal(Card card);
	boolean canBePlayed();
	boolean canBePlayed(int turnPos);
	boolean isHandPlayer(int hand);
	boolean handContains(int turnPos, int cardType);
	boolean handContainsColor(int turnPos, int color);
	
	void setTurnOrder(int order);
	void setRuleSet(RuleSet ruleset);
	void setHandPlayable(boolean playable);
	void setHandPlayable(boolean playable, int turnPos);
	void setTypePlayable(int type, boolean playable);
	void setTypePlayable(int type, boolean playable, int turnPos);
}
