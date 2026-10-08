package com.loganius.Uno;

import java.io.*;

/**
 * A game action (such as drawing a card or reversing the turn order
 * encapsulated in a lightweight, network friendly format.
 * Think of this like an API call.
 * TODO: Handle when more players join.
 */
class Action implements Serializable {
	private static final long serialVersionUID = 1L;
	
	static final int DRAW_TO_HAND = 0;
	static final int REMOVE_FROM_HAND = 1;
	static final int DISCARD = 2;
	static final int REVERSE = 3;
	static final int ROTATE_HANDS = 4;
	static final int TURN = 5;
	static final int SET_STATE = 6;
	static final int SEND_STATE = 7;
	static final int WELCOME = 8;
	static final int REQUEST_START = 9;
	static final int START_GAME = 10;
	static final int HELLO = 11;
	static final int UNO = 12;
	static final int SWAP = 13;
	static final int CREATE_SLOT = 14;
	static final int UPDATE_SLOT = 15;
	static final int SET_RULES = 16;

	private int type;
	// Meaning is defined by the handler code.
	private Object argument;
	// The CRC32 of the game state after the action is completed. 
	// (For ensuring all parties have a consistent state.)
	private long finalState;
	
	static Action drawToHand(int hand, int type, GameState finalState) {
		return new Action(DRAW_TO_HAND, new int[] {hand, type}, finalState);
	}
	
	static Action removeFromHand(int hand, int type, GameState finalState) {
		return new Action(REMOVE_FROM_HAND, new int[] {hand, type}, finalState);
	}

	static Action discard(int type, int hand, GameState finalState) {
		int[] args = new int[] {type, hand};
		return new Action(DISCARD, args, finalState);
	}
	
	static Action reverse(GameState finalState) {
		return new Action(REVERSE, null, finalState);
	}
	
	static Action rotateHands(GameState finalState) {
		return new Action(ROTATE_HANDS, null, finalState);
	}
	
	static Action turn(GameState finalState, int lastPlayed) {
		return new Action(TURN, new Integer(lastPlayed), finalState);
	}
	
	static Action setState(GameState finalState) {
		return new Action(SET_STATE, finalState, finalState);
	}
	
	static Action getState() {
		return new Action(SEND_STATE, null, null);
	}
	
	static Action welcome(String[] names, int player, int numPlayers) {
		return new Action(WELCOME, new WelcomeAction(names, player, numPlayers), null);
	}
	
	static Action requestStart() {
		return new Action(REQUEST_START, null, null);
	}
	
	static Action startGame() {
		return new Action(START_GAME, null, null);
	}
	
	static Action hello(String name) {
		return new Action(HELLO, name, null);
	}
	
	static Action uno(GameState finalState) {
		return new Action(UNO, null, finalState);
	}
	
	static Action swap(GameState finalState, int hand1, int hand2) {
		int[] args = new int[] {hand1, hand2};
		return new Action(SWAP, args, finalState);
	}
	
	static Action createSlot(GameState finalState, int slot, int val) {
		int[] args = new int[] {slot, val};
		return new Action(CREATE_SLOT, args, finalState);
	}
	
	static Action updateSlot(GameState finalState, int slot, int val) {
		int[] args = new int[] {slot, val};
		return new Action(UPDATE_SLOT, args, finalState);
	}
	
	static Action setRules(RuleSet rules) {
		return new Action(SET_RULES, rules, null);
	}
	
	Action(int type, Object argument, GameState finalState) {
		try {
			this.type = type;
			this.argument = argument;
	
			if (finalState == null) {
				this.finalState = 0;
			} else {
				this.finalState = finalState.getCRC32();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	int getType() {
		return type;
	}
	
	Object getArgument() {
		return argument;
	}
	
	boolean verify(GameState state) {
		if (finalState == 0) {
			return true;
		}
		try {
			return state.getCRC32() == finalState;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	static class WelcomeAction implements Serializable {
		private static final long serialVersionUID = 1L;

		private String[] names;
		private int player;
		private int players;

		WelcomeAction(String[] names, int player, int players) {
			this.names = names;
			this.player = player;
			this.players = players;
		}
		
		String[] getNames() {
			return names;
		}
		
		int getPlayer() {
			return player;
		}
		
		int getNumPlayers() {
			return players;
		}
	}
}
