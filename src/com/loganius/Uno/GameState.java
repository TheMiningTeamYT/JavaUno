package com.loganius.Uno;

import java.util.*;
import java.util.zip.CRC32;
import java.io.*;

/**
 * A lightweight representation of the Uno game state, fit to be transmitted over the network.
 * TODO: Track playability
 */
final class GameState implements Serializable {
	private static final long serialVersionUID = 10L;

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
	private int turnOrder = GameUtils.CLOCKWISE;
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
	
	public Object clone() {
		return new GameState(this);
	}
	
	void setPlayers(int players) {
		this.players = players;
	}

	void drawToHand(int turnPos, int card) {
		uno = false;
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
		if (turnOrder == GameUtils.CLOCKWISE) {
			turnOrder = GameUtils.COUNTER_CLOCKWISE;
		} else {
			turnOrder = GameUtils.CLOCKWISE;
		}
	}

	void rotate() {
		Vector temp1;
		Vector temp2;
		if (turnOrder == GameUtils.CLOCKWISE) {
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
			playerGoing = GameUtils.nextPlayer(turnOrder, players, playerGoing);
		}
	}
	
	void doUno() {
		uno = true;
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
		if (!a.check()) {
			return;
		}

		Object argument = a.getArgument();
		switch (a.getType()) {
			case Action.DRAW_TO_HAND: {
				int args[] = (int[])argument;
				drawToHand(args[0], args[1]);
				break;
			}
			case Action.REMOVE_FROM_HAND: {
				int args[] = (int[]) argument;
				removeFromHand(args[0], args[1]);
				break;
			}
			case Action.DISCARD: {
				int args[] = (int[]) argument;
				discard(args[1], args[0]);
				break;
			}
			case Action.REVERSE:
				reverse();
				break;
			case Action.ROTATE_HANDS:
				rotate();
				break;
			case Action.TURN: {
				int val = ((Integer)argument).intValue();
				lastPlayed = val;
				turn();
				break;
			}
			case Action.SET_STATE: 
			case Action.START: {
				Action.State state = (Action.State)argument;
				if (state.check()) {
					clone(state.getState());
				}
				break;
			}
			case Action.CALL_UNO:
				uno = true;
				break;
			case Action.SWAP: {
				int args[] = (int[]) argument;
				swapHands(args[0], args[1]);
				break;
			}
			case Action.SET_RULES:
				setRuleSet((RuleSet)argument);
				break;
			case Action.SEND_CHALLENGE: {
				int val = ((Integer) argument).intValue();
				sendWildCardChallenge(val);
				break;
			}
			case Action.END_CHALLENGE:
				endWildCardChallenge();
				break;
			default:
				break;
		}
	}
	
	private int findPlayer(int turnPos) {
		if (turnOrder == GameUtils.CLOCKWISE) {
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
	
	void setTurnOrder(int order) {
		turnOrder = order;
	}
	
	int getPlayers() {
		return players;
	}
	
	int getPlayerGoing() {
		return playerGoing;
	}
	
	void setPlayerGoing(int player) {
		playerGoing = player;
	}
	
	boolean getUno() {
		return uno;
	}
	
	void setUno(boolean uno) {
		this.uno = uno;
	}
	
	boolean getChallengeActive() {
		return challengeActive;
	}
	
	void setChallengeActive(boolean active) {
		challengeActive = active;
	}
	
	boolean check() {
		int numCookies = 0;
		int numSlots = 0;

		if (hands == null) {
			return false;
		}
		if (hands.length != GameUtils.MAX_PLAYERS) {
			return false;
		}
		if (players <= 0 || players > GameUtils.MAX_PLAYERS) {
			return false;
		}
		if (playerGoing < 0 || playerGoing >= players) {
			return false;
		}
		if (turnOrder != GameUtils.CLOCKWISE && turnOrder != GameUtils.COUNTER_CLOCKWISE) {
			return false;
		}
		
		if (lastPlayed < 0) {
			return false;
		}

		for (int i = 0; i < players; i++) {
			if (hands[i] == null) {
				return false;
			}
			for (int j = 0; j < hands[i].size(); j++) {
				int card;
				if (hands[i].elementAt(j) == null || hands[i].elementAt(j).getClass() != Integer.class) {
					return false;
				}
				card = ((Integer)hands[i].elementAt(j)).intValue();
				
				// TODO: find a way to figure out how many cards should be in
				// the deck.
				if (card < 0) {
					return false;
				}
			}
		}
		
		if (rules == null) {
			return false;
		}
		
		if (!rules.check()) {
			return false;
		}

		if (tracking == null) {
			return false;
		}
		
		if (trackingCookies == null) {
			return false;
		}
		
		for (int i = 0; i < trackingCookies.size(); i++) {
			Integer cookie;
			if (trackingCookies.elementAt(i).getClass() != Integer.class) {
				return false;
			}
			cookie = ((Integer)trackingCookies.elementAt(i));
			if (!(tracking.containsKey(cookie))) {
				return false;
			}
		}
		
		for (Enumeration e = tracking.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			if (key.getClass() != Integer.class) {
				return false;
			}
			numCookies++;
		}
		
		if (numCookies != trackingCookies.size()) {
			return false;
		}
		
		for (Enumeration e = tracking.elements(); e.hasMoreElements();) {
			Object slot = e.nextElement();
			if (slot.getClass() != Integer.class) {
				return false;
			}
			numSlots++;
		}
		
		if (numSlots != numCookies) {
			return false;
		}
		
		return true;
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
