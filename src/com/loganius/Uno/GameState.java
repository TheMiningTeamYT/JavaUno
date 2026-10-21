package com.loganius.Uno;

import java.util.*;
import java.util.zip.CRC32;
import java.io.*;

/**
 * A lightweight representation of the Uno game state, fit to be transmitted over the network.
 * TODO: More safety checks
 * TODO: Add the ability for plugins to track custom values in the game state.
 */
class GameState implements Serializable {
	private static final long serialVersionUID = 9L;

	// Array of int vectors (representing types of cards)
	private Vector[] hands = {
			new Vector(),
			new Vector(),
			new Vector(),
			new Vector()
	};
	private Vector trackingCookies = new Vector();
	private Hashtable tracking = new Hashtable();
	private RuleSet rules = new RuleSet.Standard();
	private int lastPlayed;
	private int turnOrder = Game.CLOCKWISE;
	private int players = 0;
	private int playerGoing = 0;
	private boolean uno = false;
	private boolean challengeActive = false;

	GameState() {};

	GameState(GameState other) {
		clone(other);
	}
	
	private void clone(GameState other) {
		for (int i = 0; i < hands.length; i++) {
			hands[i] = (Vector) other.hands[i].clone();
		}
		trackingCookies = (Vector) other.trackingCookies.clone();
		tracking = (Hashtable) other.tracking.clone();
		rules = other.rules;
		lastPlayed = other.lastPlayed;
		turnOrder = other.turnOrder;
		players = other.players;
		playerGoing = other.playerGoing;
		uno = other.uno;
		challengeActive = other.challengeActive;
	}
	
	void setPlayers(int players) {
		this.players = players;
	}

	void drawToHand(int turnPos, int card) {
		findPlayerHand(turnPos).addElement(new Integer(card));
	}

	void removeFromHand(int turnPos, int card) {
		if (turnPos >= 0) {
			findPlayerHand(turnPos).removeElement(new Integer(card));
		}
	}
	
	void discard(int card, int hand) {
		removeFromHand(hand, card);
		lastPlayed = card;
	}

	void reverse() {
		if (turnOrder == Game.CLOCKWISE) {
			turnOrder = Game.COUNTER_CLOCKWISE;
		} else {
			turnOrder = Game.CLOCKWISE;
		}
	}

	void rotate() {
		Vector temp1;
		Vector temp2;
		if (turnOrder == Game.CLOCKWISE) {
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
	}
	
	void turn() {
		uno = false;
		if (hands[playerGoing].size() > 0) {
			if (turnOrder == Game.CLOCKWISE) {
				playerGoing = (playerGoing + 1) % players;
			} else {
				playerGoing = (playerGoing + players - 1) % players;
			}
		}
	}
	
	void doUno() {
		uno = true;
	}
	
	void startUno() {
		uno = false;
	}
	
	void setRuleSet(RuleSet rules) {
		this.rules = rules;
	}
	
	RuleSet getRules() {
		return rules;
	}
	
	void swapHands(int turnPos1, int turnPos2) {
		int hand1 = findPlayer(turnPos1);
		int hand2 = findPlayer(turnPos2);
		Vector temp = hands[hand1];
		hands[hand1] = hands[hand2];
		hands[hand2] = temp;
	}
	
	void sendWildCardChallenge(int lastPlayed) {
		this.lastPlayed = lastPlayed;
		challengeActive = true;
	}
	
	void endWildCardChallenge() {
		challengeActive = false;
	}
	
	void requestTrackingSlot(int cookie, int val) {
		boolean added = false;
		if (tracking.containsKey(new Integer(cookie))) {
			System.err.println("Warning! Cookie " + cookie + " already in use!");
			updateSlot(cookie, val);
			return;
		}
		if (trackingCookies.size() != 0) {
			for (int i = 0; i < trackingCookies.size(); i++) {
				if (cookie >= ((Integer)trackingCookies.elementAt(i)).intValue()) {
					trackingCookies.insertElementAt(new Integer(cookie), i);
					added = true;
					break;
				}
			}
		}
		if (!added) {
			trackingCookies.addElement(new Integer(cookie));
		}
		tracking.put(new Integer(cookie), new Integer(val));
	}
	
	void releaseTrackingSlot(int cookie) {
		if (!tracking.containsKey(new Integer(cookie))) {
			throw new IllegalArgumentException("Cookie " + cookie + " not in use!");
		}
		trackingCookies.removeElement(new Integer(cookie));
		tracking.remove(new Integer(cookie));
	}
	
	void updateSlot(int cookie, int val) {
		if (!tracking.containsKey(new Integer(cookie))) {
			throw new IllegalArgumentException("Cookie " + cookie + " already not found!");
		}
		tracking.put(new Integer(cookie), new Integer(val));
	}
	
	int getSlot(int cookie) {
		if (!tracking.containsKey(new Integer(cookie))) {
			throw new IllegalArgumentException("Cookie " + cookie + " already not found!");
		}
		return ((Integer)tracking.get(new Integer(cookie))).intValue();
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
				if (a.getArgument().getClass() != int[].class) {
					break;
				}
				args = (int[]) a.getArgument();
				discard(args[0], args[1]);
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
				rotate();
				break;
			case Action.TURN:
				if (a.getArgument().getClass() != Integer.class) {
					break;
				}
				arg = (Integer) a.getArgument();
				lastPlayed = arg.intValue();
				turn();
				break;
			case Action.SET_STATE:
				if (a.getArgument().getClass() != GameState.class) {
					break;
				}
				{
					GameState state = (GameState) a.getArgument();
					boolean valid = true;
					if (state.hands == null) {
						break;
					}
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
					if (state.tracking == null) {
						break;
					}
					if (state.trackingCookies == null) {
						break;
					}
					if (state.rules == null) {
						break;
					}
					if (valid) {
						clone(state);
					}
				}
				break;
			case Action.CALL_UNO:
				uno = true;
				break;
			case Action.SWAP:
				if (a.getArgument().getClass() != int[].class) {
					break;
				}
				args = (int[]) a.getArgument();
				swapHands(args[0], args[1]);
				break;
			case Action.CREATE_SLOT:
				if (a.getArgument().getClass() != int[].class) {
					break;
				}
				args = (int[])a.getArgument();
				requestTrackingSlot(args[0], args[1]);
				break;
			case Action.UPDATE_SLOT:
				if (a.getArgument().getClass() != int[].class) {
					break;
				}
				args = (int[]) a.getArgument();
				updateSlot(args[0], args[1]);
				break;
			case Action.SET_RULES:
				if (!(a.getArgument() instanceof RuleSet)) {
					break;
				}
				setRuleSet((RuleSet)a.getArgument());
				break;
			case Action.START_UNO:
				startUno();
				break;
			case Action.SEND_CHALLENGE:
				if (a.getArgument().getClass() != Integer.class) {
					break;
				}
				arg = (Integer) a.getArgument();
				sendWildCardChallenge(arg.intValue());
				break;
			case Action.END_CHALLENGE:
				endWildCardChallenge();
				break;
			default:
				break;
		}
	}
	
	private int findPlayer(int turnPos) {
		if (turnOrder == Game.CLOCKWISE) {
			return (playerGoing + turnPos) % players;
		} else {
			return (playerGoing + players - turnPos) % players;
		}
	}
	
	private Vector findPlayerHand(int turnPos) {
		return hands[findPlayer(turnPos)];
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
	
	int getPlayers() {
		return players;
	}
	
	int getPlayerGoing() {
		return playerGoing;
	}
	
	boolean getUno() {
		return uno;
	}
	
	boolean getChallengeActive() {
		return challengeActive;
	}
	
	long getCRC32() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CRC32 sum = new CRC32();
		DataOutputStream out = new DataOutputStream(bos);
		Integer[][] hands = getHands();
		byte[] raw;
		
		for (int i = 0; i < players; i++) {
			for (int j = 0; j < hands[i].length; j++) {
				out.writeInt(hands[i][j].intValue());
			}
			// Canary value to prevent 2 valid game states from producing the same serialized output.
			out.writeByte(-1);
		}
		
		out.writeInt(lastPlayed);
		out.writeByte(-1);
		out.writeInt(turnOrder);
		out.writeByte(-1);
		out.writeInt(players);
		out.writeByte(-1);
		out.writeInt(playerGoing);
		out.writeByte(-1);
		out.writeBoolean(uno);
		out.writeByte(-1);
		out.writeBoolean(challengeActive);
		out.writeByte(-1);
		
		for (int i = 0; i < trackingCookies.size(); i++) {
			int val = ((Integer)tracking.get(trackingCookies.elementAt(i))).intValue();
			out.writeInt(val);
			out.writeByte(-1);
		}

		raw = bos.toByteArray();

		out.close();
		bos.close();

		sum.update(raw);
		return sum.getValue();
	}
}
