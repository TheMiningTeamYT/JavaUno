package com.loganius.Uno;

import java.util.Vector;
import java.util.zip.CRC32;
import java.io.*;

/**
 * A lightweight representation of the Uno game state, fit to be transmitted over the network.
 */
class GameState implements Serializable {
	private static final long serialVersionUID = 4L;

	// Array of int vectors (representing types of cards)
	private Vector[] hands = {
			new Vector(),
			new Vector(),
			new Vector(),
			new Vector()
	};
	private int lastPlayed;
	private int turnOrder = 0;
	private int moves = 0;
	private int players = 0;
	private boolean uno = false;

	GameState() {};

	GameState(GameState other) {
		for (int i = 0; i < hands.length; i++) {
			hands[i] = (Vector) other.hands[i].clone();
		}
		lastPlayed = other.lastPlayed;
		turnOrder = other.turnOrder;
		moves = other.moves;
		players = other.players;
	}
	
	void setPlayers(int players) {
		this.players = players;
	}

	void drawToHand(int hand, int card) {
		hands[hand].addElement(new Integer(card));
	}

	void removeFromHand(int hand, int card) {
		hands[hand].removeElement(new Integer(card));
	}
	
	void discard(int card) {
		removeFromHand(0, card);
		lastPlayed = card;
	}

	void reverse() {
		rotate();

		for (int i = 0; i < hands.length / 2; i++) {
            Vector temp = hands[i];
            hands[i] = hands[hands.length - 1 - i];
            hands[hands.length - 1 - i] = temp;
        }

		if (turnOrder == 0) {
			turnOrder = 1;
		} else {
			turnOrder = 0;
		}
	}

	void rotate() {
		Vector temp1 = hands[0];
		Vector temp2;
		for (int i = hands.length - 1; i >= 0; i--) {
			temp2 = hands[i];
			hands[i] = temp1;
			temp1 = temp2;
		}
	}
	
	void move() {
		uno = false;
		rotate();
		if (turnOrder == 0) {
			moves++;
		} else {
			moves--;
		}
	}
	
	void doUno() {
		uno = true;
	}
	
	void applyAction(Action a) {
		int[] args;
		Integer arg;
		switch (a.getType()) {
			case Action.DRAW_TO_HAND:
				if (a.getArgument().getClass() != int[].class) {
					break;
				}
				args = (int[]) a.getArgument();
				drawToHand(args[0], args[1]);
				break;
			case Action.REMOVE_FROM_HAND:
				if (a.getArgument().getClass() != int[].class) {
					break;
				}
				args = (int[]) a.getArgument();
				removeFromHand(args[0], args[1]);
				break;
			case Action.DISCARD:
				if (a.getArgument().getClass() != Integer.class) {
					break;
				}
				arg = (Integer) a.getArgument();
				discard(arg.intValue());
				break;
			case Action.REVERSE:
				if (a.getArgument() != null) {
					break;
				}
				reverse();
				break;
			case Action.ROTATE_HANDS:
				if (a.getArgument() != null) {
					break;
				}
				move();
				break;
			case Action.TURN:
				if (a.getArgument().getClass() != Integer.class) {
					break;
				}
				arg = (Integer) a.getArgument();
				move();
				lastPlayed = arg.intValue();
				break;
			case Action.SET_STATE:
				if (a.getArgument().getClass() != GameState.class) {
					break;
				}
				{
					GameState state = (GameState) a.getArgument();
					if (state.hands != null) {
						boolean valid = true;
						for (int i = 0; i < state.hands.length; i++) {
							if (state.hands[i] == null) {
								valid = false;
								break;
							}
							for (int j = 0; j < state.hands[i].size(); j++) {
								if (state.hands[i].elementAt(j) == null) {
									valid = false;
									break;
								}
							}
						}
						if (valid) {
							hands = state.hands;
							lastPlayed = state.lastPlayed;
							turnOrder = state.turnOrder;
						}
					}
				}
				break;
			case Action.UNO:
				uno = true;
				break;
			default:
				break;
		}
	}
	
	Integer[][] getHands() {
		Integer[][] rawHands = new Integer[][] {
			new Integer[hands[0].size()],
			new Integer[hands[1].size()],
			new Integer[hands[2].size()],
			new Integer[hands[3].size()],
		};
		for (int i = 0; i < hands.length; i++) {
			hands[i].copyInto(rawHands[i]);
		}
		return rawHands;
	}
	
	int getLastPlayed() {
		return lastPlayed;
	}
	
	void setLastPlayed(int card) {
		lastPlayed = card;
	}
	
	int getTurnOrder() { 
		return turnOrder;
	}
	
	int getMoves() {
		return moves;
	}
	
	int getPlayers() {
		return players;
	}
	
	long getCRC32() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CRC32 sum = new CRC32();
		DataOutputStream out = new DataOutputStream(bos);
		Integer[][] hands = getHands();
		
		for (int i = 0; i < players; i++) {
			for (int j = 0; j < hands[i].length; j++) {
				out.writeInt(hands[i][j].intValue());
			}
			// Canary value to prevent 2 valid game states from producing the same serialized output.
			out.writeInt(-1);
		}
		
		out.writeInt(lastPlayed);
		out.writeInt(-1);
		out.writeInt(turnOrder);
		out.writeInt(-1);
		out.writeInt(moves);
		out.writeInt(-1);
		out.writeInt(players);
		out.writeInt(-1);
		
		byte[] raw = bos.toByteArray();

		out.close();
		bos.close();

		sum.update(raw);
		return sum.getValue();
	}
}
