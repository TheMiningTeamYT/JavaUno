package com.loganius.Uno;

import java.io.*;
import java.util.zip.CRC32;

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

	private int type;
	// Meaning is defined by the handler code.
	private Object argument;
	// The CRC32 of the game state after the action is completed. 
	// (For ensuring all parties have a consistent state.)
	private long finalState;
	
	static Action drawToHand(int hand, int type, GameState finalState) {
		try {
			return new Action(DRAW_TO_HAND, new int[] {hand, type}, finalState);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static Action removeFromHand(int hand, int type, GameState finalState) {
		try {
			return new Action(REMOVE_FROM_HAND, new int[] {hand, type}, finalState);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	static Action discard(int type, GameState finalState) {
		try {
			return new Action(DISCARD, new Integer(type), finalState);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static Action reverse(GameState finalState) {
		try {
			return new Action(REVERSE, null, finalState);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static Action rotateHands(GameState finalState) {
		try {
			return new Action(ROTATE_HANDS, null, finalState);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static Action turn(GameState finalState, int lastPlayed) {
		try {
			return new Action(TURN, new Integer(lastPlayed), finalState);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static Action setState(GameState finalState) {
		try {
			return new Action(SET_STATE, finalState, finalState);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static Action getState() {
		try {
			return new Action(SEND_STATE, null, null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static Action welcome(String[] names, int player, int numPlayers) {
		try {
			return new Action(WELCOME, new WelcomeAction(names, player, numPlayers), null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static Action requestStart() {
		try {
			return new Action(REQUEST_START, null, null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static Action startGame() {
		try {
			return new Action(START_GAME, null, null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static Action hello(String name) {
		try {
			return new Action(HELLO, name, null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static Action uno(GameState finalState) {
		try {
			return new Action(UNO, null, finalState);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	Action(int type, Object argument, GameState finalState) throws IOException {
		this.type = type;
		this.argument = argument;

		if (finalState == null) {
			this.finalState = 0;
		} else {
			this.finalState = finalState.getCRC32();
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
