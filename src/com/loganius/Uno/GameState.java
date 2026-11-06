package com.loganius.Uno;

import java.util.*;
import java.util.zip.CRC32;
import java.io.*;

/**
 * A lightweight representation of the Uno game state, fit to be transmitted over the network.
 * TODO: Track playability
 */
final class GameState implements Serializable {
	private static final long serialVersionUID = 11L;

	// Array of int vectors (representing types of cards)
	private SimpleHand[] hands = {
			new SimpleHand(),
			new SimpleHand(),
			new SimpleHand(),
			new SimpleHand(),
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
			hands[i] = (SimpleHand) other.hands[i].clone();
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
	
	GameState drawToHand(int turnPos, SimpleCard card) {
		GameState result = new GameState(this);
		result.doDrawToHand(turnPos, new BasicCard(card));
		return result;
	}
	
	private void doDrawToHand(int turnPos, int ID, int type) {
		SimpleHand hand = findPlayerHand(turnPos);
		BasicCard card = new BasicCard(ID, type, hand.getPlayable());
		doDrawToHand(turnPos, card);
	}

	private void doDrawToHand(int turnPos, BasicCard card) {
		uno = false;
		SimpleHand hand = findPlayerHand(turnPos);
		hand.add(card);
	}
	
	GameState removeFromHand(int turnPos, SimpleCard card) {
		GameState result = new GameState(this);
		result.doRemoveFromHand(turnPos, card);
		return result;
	}
	
	private void doRemoveFromHand(int turnPos, int ID) {
		SimpleHand hand = findPlayerHand(turnPos);
		SimpleCard cardToRemove = hand.getSimpleCardByID(ID);
		if (cardToRemove != null) {
			hand.remove(cardToRemove);
		}
	}

	private void doRemoveFromHand(int turnPos, SimpleCard card) {
		doRemoveFromHand(turnPos, card.getID());
	}
	
	GameState discard(SimpleCard card, int hand) {
		GameState result = new GameState(this);
		result.doDiscard(card, hand);
		return result;
	}
	
	private void doDiscard(SimpleCard card, int hand) {
		doDiscard(card.getType(), card.getID(), hand);
	}
	
	private void doDiscard(int type, int ID, int hand) {
		doRemoveFromHand(hand, ID);
		lastPlayed = type;
	}
	
	GameState reverse() {
		GameState result = new GameState(this);
		result.doReverse();
		return result;
	}

	private void doReverse() {
		if (turnOrder == GameUtils.CLOCKWISE) {
			turnOrder = GameUtils.COUNTER_CLOCKWISE;
		} else {
			turnOrder = GameUtils.CLOCKWISE;
		}
	}
	
	GameState rotate() {
		GameState result = new GameState(this);
		result.doRotate();
		return result;
	}

	private void doRotate() {
		SimpleHand temp1;
		SimpleHand temp2;
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
	
	GameState turn() {
		GameState result = new GameState(this);
		result.doTurn();
		return result;
	}
	
	private void doTurn() {
		uno = false;
		if (hands[playerGoing].numCards() > 0) {
			playerGoing = GameUtils.nextPlayer(turnOrder, players, playerGoing);
		}
	}
	
	GameState uno() { 
		GameState result = new GameState(this);
		result.doUno();
		return result;
	}
	
	private void doUno() {
		uno = true;
	}
	
	GameState setRuleSet(RuleSet rules) {
		GameState result = new GameState(this);
		result.doSetRuleSet(rules);
		return result;
	}
	
	private void doSetRuleSet(RuleSet rules) {
		this.rules = rules;
	}
	
	RuleSet getRules() {
		return rules;
	}
	
	GameState swapHands(int turnPos1, int turnPos2) {
		GameState result = new GameState(this);
		result.doSwapHands(turnPos1, turnPos2);
		return result;
	}
	
	private void doSwapHands(int turnPos1, int turnPos2) {
		int hand1 = findPlayer(turnPos1);
		int hand2 = findPlayer(turnPos2);
		SimpleHand temp = hands[hand1];
		hands[hand1] = hands[hand2];
		hands[hand2] = temp;
	}
	
	GameState sendWildCardChallenge(int lastPlayed) {
		GameState result = new GameState(this);
		result.doSendChallenge(lastPlayed);
		return result;
	}
	
	private void doSendChallenge(int lastPlayed) {
		this.lastPlayed = lastPlayed;
		challengeActive = true;
	}
	
	GameState endWildCardChallenge() {
		GameState result = new GameState(this);
		result.doEndChallenge();
		return result;
	}
	
	private void doEndChallenge() {
		challengeActive = false;
	}
	
	GameState requestTrackingSlot(int cookie, int val) {
		GameState result = new GameState(this);
		result.doRequestSlot(cookie, val);
		return result;
	}
	
	private void doRequestSlot(int cookie, int val) {
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
	
	GameState releaseTrackingSlot(int cookie) {
		GameState result = new GameState(this);
		result.doReleaseSlot(cookie);
		return result;
	}
	
	private void doReleaseSlot(int cookie) {
		if (!tracking.containsKey(new Integer(cookie))) {
			throw new IllegalArgumentException("Cookie " + cookie + " not in use!");
		}
		trackingCookies.removeElement(new Integer(cookie));
		tracking.remove(new Integer(cookie));
	}
	
	GameState updateSlot(int cookie, int val) {
		GameState result = new GameState(this);
		result.doUpdateSlot(cookie, val);
		return result;
	}
	
	private void doUpdateSlot(int cookie, int val) {
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
	
	boolean getHandPlayable(int turnPos) {
		return hands[findPlayer(turnPos)].getPlayable();
	}
	
	boolean compareHands(StandardHand hands[]) {
		for (int i = 0; i < players; i++) {
			if (!hands[i].equals(this.hands[i])) {
				return false;
			}
		}
		
		return true;
	}
	
	GameState applyAction(Action a) {
		if (!a.check()) {
			return this;
		}
		
		GameState result = new GameState(this);

		Object argument = a.getArgument();
		// TODO: Fix card-based action types.
		switch (a.getType()) {
			case Action.DRAW_TO_HAND: {
				int args[] = (int[])argument;
				result.doDrawToHand(args[0], args[1], args[2]);
				break;
			}
			case Action.REMOVE_FROM_HAND: {
				int args[] = (int[]) argument;
				result.doRemoveFromHand(args[0], args[1]);
				break;
			}
			case Action.DISCARD: {
				int args[] = (int[]) argument;
				result.doDiscard(args[1], args[2], args[0]);
				break;
			}
			case Action.REVERSE:
				result.doReverse();
				break;
			case Action.ROTATE_HANDS:
				result.doRotate();
				break;
			case Action.TURN: {
				int val = ((Integer)argument).intValue();
				lastPlayed = val;
				result.doTurn();
				break;
			}
			case Action.SET_STATE: 
			case Action.START: {
				Action.State state = (Action.State)argument;
				if (state.check()) {
					return state.getState();
				}
				break;
			}
			case Action.CALL_UNO:
				result.doUno();
				break;
			case Action.SWAP: {
				int args[] = (int[]) argument;
				result.doSwapHands(args[0], args[1]);
				break;
			}
			case Action.SET_RULES:
				result.doSetRuleSet((RuleSet)argument);
				break;
			case Action.SEND_CHALLENGE: {
				int val = ((Integer) argument).intValue();
				result.sendWildCardChallenge(val);
				break;
			}
			case Action.END_CHALLENGE:
				result.endWildCardChallenge();
				break;
			default:
				break;
		}
		return result;
	}
	
	private int findPlayer(int turnPos) {
		if (turnOrder == GameUtils.CLOCKWISE) {
			return (playerGoing + turnPos) % players;
		} else {
			return (playerGoing + players - turnPos) % players;
		}
	}
	
	private SimpleHand findPlayerHand(int turnPos) {
		return hands[findPlayer(turnPos)];
	}
	
	SimpleCard[][] getHands() {
		SimpleCard[][] rawHands = new SimpleCard[][] {
			new SimpleCard[hands[0].numCards()],
			new SimpleCard[hands[1].numCards()],
			new SimpleCard[hands[2].numCards()],
			new SimpleCard[hands[3].numCards()],
		};
		for (int i = 0; i < hands.length; i++) {
			hands[i].copyInto(rawHands[i]);
		}
		return rawHands;
	}
	
	int getLastPlayed() {
		return lastPlayed;
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
			hands[i].check();
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
		SimpleCard[][] hands = getHands();
		byte[] raw;
		
		for (int i = 0; i < hands.length; i++) {
			out.writeBoolean(this.hands[i].getPlayable());
			for (int j = 0; j < hands[i].length; j++) {
				out.writeInt(hands[i][j].getType());
				out.writeBoolean(hands[i][j].getPlayable());
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
