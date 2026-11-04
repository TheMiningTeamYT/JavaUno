package com.loganius.Uno;

import java.io.*;

/**
 * A game action (such as drawing a card or reversing the turn order
 * encapsulated in a lightweight, network friendly format.
 * Think of this like an API call.
 * TODO: Handle when more players join.
 */
public final class Action implements Serializable {
	private static final long serialVersionUID = 2L;
	public static final int CLIENT_ONLY = 0x20;
	public static final int SERVER_ONLY = 0x40;
	
	/**
	 * Client <-> Server Types
	 */
	static final int ACK = 0;
	static final int KEEPALIVE = 1;
	static final int CALL_UNO = 2;
	
	/**
	 * Client -> Server Types
	 */
	static final int HELLO = CLIENT_ONLY + 0;
	static final int PLAY = CLIENT_ONLY + 1;
	static final int START_DRAW = CLIENT_ONLY + 2;
	static final int DRAW = CLIENT_ONLY + 3;
	static final int END_DRAW = CLIENT_ONLY + 4;
	static final int RESOLVE_CHALLENGE = CLIENT_ONLY + 5;
	static final int REQUEST_START = CLIENT_ONLY + 6;
	static final int SEND_STATE = CLIENT_ONLY + 7;
	static final int USER_INPUT = CLIENT_ONLY + 9;
	
	/**
	 * Server -> Client Types
	 */
	static final int WELCOME = SERVER_ONLY + 0;
	static final int LEAVE = SERVER_ONLY + 1;
	static final int START = SERVER_ONLY + 2;
	static final int SET_STATE = SERVER_ONLY + 3;
	static final int DRAW_TO_HAND = SERVER_ONLY + 4;
	static final int REMOVE_FROM_HAND = SERVER_ONLY + 5;
	static final int DISCARD = SERVER_ONLY + 6;
	static final int REVERSE = SERVER_ONLY + 7;
	static final int ROTATE_HANDS = SERVER_ONLY + 8;
	static final int TURN = SERVER_ONLY + 9;
	static final int SWAP = SERVER_ONLY + 10;
	static final int END_CHALLENGE = SERVER_ONLY + 11;
	static final int SET_RULES = SERVER_ONLY + 12;
	static final int SEND_CHALLENGE = SERVER_ONLY + 13;

	private int type;
	// Meaning is defined by the handler code.
	private Object argument;
	// The CRC32 of the game state after the action is completed. 
	// (For ensuring all parties have a consistent state.)
	private long finalState;

	// Client <-> Server types
	static Action ack(GameState finalState) {
		return new Action(ACK, null, finalState);
	}
	
	static Action keepalive(GameState finalState) {
		return new Action(KEEPALIVE, null, finalState);
	}

	static Action callUno(GameState finalState) {
		return new Action(CALL_UNO, null, finalState);
	}
	
	// TODO: Update all uses of sendChallenge
	static Action sendChallenge(int color, GameState finalState) {
		return new Action(SEND_CHALLENGE, new Integer(color), finalState);
	}
	
	// Client -> Server types
	static Action hello(String name) {
		return new Action(HELLO, name, null);
	}
	
	static Action play(int type, GameState finalState) {
		return new Action(PLAY, new Integer(type), finalState);
	}

	static Action startDraw(GameState finalState) {
		return new Action(START_DRAW, null, finalState);
	}

	static Action draw(GameState finalState) {
		return new Action(DRAW, null, finalState);
	}

	static Action endDraw(GameState finalState) {
		return new Action(END_DRAW, null, finalState);
	}

	static Action resolveChallenge(boolean decision, GameState finalState) {
		return new Action(RESOLVE_CHALLENGE, new Boolean(decision), finalState);
	}

	static Action requestStart(RuleSet rules) {
		return new Action(REQUEST_START, rules, null);
	}

	static Action getState() {
		return new Action(SEND_STATE, null, null);
	}

	static Action input(int value, GameState finalState) {
		return new Action(USER_INPUT, new Integer(value), finalState);
	}

	// Server -> Client types
	static Action welcome(String[] names, int player, int numPlayers) {
		return new Action(WELCOME, new Welcome(names, player, numPlayers), null);
	}

	static Action leave() {
		return new Action(LEAVE, null, null);
	}

	static Action start(State state) {
		return new Action(START, state, state.getState());
	}

	static Action setState(State state) {
		return new Action(SET_STATE, state, state.getState());
	}

	static Action drawToHand(int hand, int type, GameState finalState) {
		return new Action(DRAW_TO_HAND, new int[] {hand, type}, finalState);
	}

	static Action removeFromHand(int hand, int type, GameState finalState) {
		return new Action(REMOVE_FROM_HAND, new int[] {hand, type}, finalState);
	}

	static Action discard(int hand, int type, GameState finalState) {
		int[] args = new int[] {hand, type};
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

	static Action swap(int hand1, int hand2, GameState finalState) {
		int[] args = new int[] {hand1, hand2};
		return new Action(SWAP, args, finalState);
	}

	static Action endChallenge(GameState finalState) {
		return new Action(END_CHALLENGE, null, finalState);
	}
	
	static Action setRules(RuleSet rules, GameState finalState) {
		return new Action(SET_RULES, rules, finalState);
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
		switch (type) {
			case HELLO:
			case REQUEST_START:
			case SEND_STATE:
			case WELCOME:
			case LEAVE:
				return true;
			default:
				try {
					return state.getCRC32() == finalState;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
		}
	}
	
	boolean check() {
		if (type >= 1024) {
			return true;
		}

		if (argument == null) {
			switch (type) {
				case ACK:
				case KEEPALIVE:
				case CALL_UNO:
				case START_DRAW:
				case DRAW:
				case END_DRAW:
				case SEND_STATE:
				case LEAVE:
				case REVERSE:
				case ROTATE_HANDS:
				case END_CHALLENGE:
					return true;
				default:
					return false;
			}
		}

		switch (type) {
			case HELLO:
				if (argument.getClass() == String.class) {
					return true;
				}
				break;
			case PLAY:
				if (argument.getClass() == Integer.class) {
					int type = ((Integer)argument).intValue();
					if (type >= 0) {
						return true;
					}
				}
				break;
			case RESOLVE_CHALLENGE:
				if (argument.getClass() == Boolean.class) {
					return true;
				}
				break;
			case REQUEST_START:
				if (argument instanceof RuleSet) {
					// If you want to exploit Uno... RuleSets are probably
					// the way to go.
					return ((RuleSet)argument).check();
				}
				break;
			case USER_INPUT:
				if (argument.getClass() == Integer.class) {
					return true;
				}
				break;
			case WELCOME:
				if (argument.getClass() == Welcome.class) {
					return ((Welcome)argument).check();
				}
				break;
			case START:
			case SET_STATE:
				if (argument.getClass() == State.class) {
					return ((State)argument).check();
				}
				break;
			case DRAW_TO_HAND:
			case REMOVE_FROM_HAND:
			case DISCARD:
				if (argument.getClass() == int[].class) {
					int[] args = (int[])argument;
					if (args.length != 2) {
						break;
					}
					// The action has no way to know how many players there
					// are in the game or how many cards there are in the dec.
					if (args[0] < 0 || args[0] >= GameUtils.MAX_PLAYERS) {
						break;
					}
					if (args[1] < 0) {
						break;
					}
					return true;
				}
				break;
			case TURN:
				if (argument.getClass() == Integer.class) {
					// The action has no way to know how many cards there are
					// in the deck.
					if (((Integer)argument).intValue() < 0) {
						break;
					}
					return true;
				}
				break;
			case SEND_CHALLENGE:
				if (argument.getClass() == Integer.class) {
					if (((Integer)argument).intValue() < 0) {
						break;
					}
					return true;
				}
				break;
			case SWAP:
				if (argument.getClass() == int[].class) {
					int[] args = (int[])argument;
					if (args.length != 2) {
						break;
					}
					if (args[0] < 0 || args[0] >= GameUtils.MAX_PLAYERS) {
						break;
					}
					if (args[1] < 0 || args[1] >= GameUtils.MAX_PLAYERS) {
						break;
					}
					if (args[0] == args[1]) {
						break;
					}
					return true;
				}
				break;
			default:
				break;
		}

		return false;
	}
	
	public static final class Welcome implements Serializable {
		private static final long serialVersionUID = 1L;

		private String[] names;
		private int player;
		private int players;

		Welcome(String[] names, int player, int players) {
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
		
		boolean check() {
			if (names == null) {
				return false;
			}

			if (players <= 0 || players > GameUtils.MAX_PLAYERS) {
				return false;
			}

			if (player < 0 || player >= players) {
				return false;
			}
			
			if (names.length != players) {
				return false;
			}
			
			for (int i = 0; i < names.length; i++) {
				if (names[i] == null) {
					return false;
				}
			}

			return true;
		}
	}
	
	public static final class State implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private GameState state;
		private long seed;
		
		State(GameState state, long seed) {
			this.state = state;
			this.seed = seed;
		}
		
		GameState getState() {
			return state;
		}
		
		long getSeed() {
			return seed;
		}
		
		boolean check() {
			if (state == null) {
				return false;
			}

			return state.check();
		}
	}
}
