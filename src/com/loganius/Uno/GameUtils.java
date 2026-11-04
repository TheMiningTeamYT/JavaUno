package com.loganius.Uno;

public class GameUtils {
	public static final int MAX_PLAYERS = 4;
	public static final int CLOCKWISE = 0;
	public static final int COUNTER_CLOCKWISE = 1;

	static int findPlayer(int turnPos, int turnOrder, int players, int playerGoing) {
		if (turnOrder == CLOCKWISE) {
			return (playerGoing + turnPos) % players;
		} else {
			return (playerGoing + players - turnPos) % players;
		}
	}
	
	static int findTurnPos(int player, int turnOrder, int players, int playerGoing) {
		if (turnOrder == CLOCKWISE) {
			return (player + players - playerGoing) % players;
		} else {
			return (playerGoing + players - player) % players;
		}
	}
	
	static int screenToTurnPos(int screenPos, int turnOrder, int players, int player, int playerGoing) {
		return findTurnPos((screenPos + player) % players, turnOrder, players, playerGoing);
	}
	
	static void rotateHands(Hand[] hands, int players, int turnOrder) {
		Hand temp1;
		Hand temp2;
		boolean[] playableBefore = new boolean[players];

		for (int i = 0; i < players; i++) {
			playableBefore[i] = hands[i].getPlayable();
		}

		if (turnOrder == CLOCKWISE) {
			temp1 = hands[players - 1];
			for (int i = 0; i < players; i++) {
				temp2 = hands[i];
				hands[i] = temp1;
				temp1 = temp2;
			}
		} else {
			temp1 = hands[0];
			for (int i = players - 1; i >= 0; i--) {
				temp2 = hands[i];
				hands[i] = temp1;
				temp1 = temp2;
			}
		}
		
		for (int i = 0; i < players; i++) {
			hands[i].setPlayable(playableBefore[i]);
		}
	}
	
	static int nextPlayer(int turnOrder, int players, int playerGoing) {
		if (turnOrder == CLOCKWISE) {
			return (playerGoing + 1) % players;
		} else {
			return (playerGoing + players - 1) % players;
		}
	}
	
	static void swapHands(int target1, int target2, int turnOrder, int players, int playerGoing, Hand[] hands) {
		int hand1 = findPlayer(target1, turnOrder, players, playerGoing);
		int hand2 = findPlayer(target2, turnOrder, players, playerGoing);
		boolean[] playableBefore = {hands[hand1].getPlayable(), hands[hand2].getPlayable()};
		Hand temp = hands[hand1];
		
		hands[hand1] = hands[hand2];
		hands[hand2] = temp;

		hands[hand1].setPlayable(playableBefore[0]);
		hands[hand2].setPlayable(playableBefore[1]);
	}
	
	static int reverse(int turnOrder) {
		if (turnOrder == GameUtils.CLOCKWISE) {
			return COUNTER_CLOCKWISE;
		} else {
			return CLOCKWISE;
		}
	}
	
	static boolean verify(GameState state, int players, int playerGoing, 
		int turnOrder, Card lastPlayed, RuleSet ruleSet, boolean uno,
		boolean challengeActive, Hand[] hands) {

		if (state.getPlayers() != players || state.getPlayerGoing() != playerGoing || 
			state.getTurnOrder() != turnOrder ||
			(lastPlayed != null && state.getLastPlayed() != lastPlayed.getType()) ||
			!state.getRules().equals(ruleSet) || state.getUno() != uno || 
			state.getChallengeActive() != challengeActive) {
			return false;
		}

		Integer[][] stateHands = state.getHands();
		for (int i = 0; i < players; i++) {
			if (stateHands[i].length != hands[i].numCards()) {
				return false;
			}
			for (int j = 0; j < stateHands[i].length; j++) {
				if (!hands[i].contains(stateHands[i][j].intValue())) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static void setLastPlayedColor(Card lastPlayed, int color, GameState state) {
		try {
			int type = Deck.toColor(lastPlayed.getType(), color);
			lastPlayed.setType(type);
			state.setLastPlayed(type);
		} catch (IllegalArgumentException e) {}
	}
	
	public static boolean isServer(Game game) {
		return (game instanceof ServerGame);
	}
}
