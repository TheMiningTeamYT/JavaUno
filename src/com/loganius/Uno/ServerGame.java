package com.loganius.Uno;

import java.util.Hashtable;
import java.util.Random;

/**
 * This class will be used to handle actions on the server.
 * I need to figure out what is really needed for that.
 * Also try to share some functions between ServerGame and UIGame.
 * this is a nightmare to write
 * TODO: Decide if interrupting is needed for server games.
 */
public final class ServerGame implements Game {
	private GameHandler handler;
	private ProtocolInterface net;
	private InputHandler inHandler = null;
	private RuleSet rules = new RuleSet.Standard();
	private StandardHand[] hands = {
			new StandardHand(this),
			new StandardHand(this),
			new StandardHand(this),
			new StandardHand(this),
	};
	private DiscardHand discardHand = new DiscardHand(this);
	private Deck deck = Deck.blankDeck();
	private Card lastDrawn = null;
	private int turnOrder = GameUtils.CLOCKWISE;
	private int players = 1;
	private int player = 0;
	private int playerGoing = 0;
	private int nextCard = 0;
	private Random RNG = new Random();
	private boolean started = false;
	private boolean drew = false;
	private boolean drawing = false;
	private boolean uno = false;
	private boolean ended = false;
	private boolean challengeActive = false;
	private GameState state;
	private Hashtable actionHandlers = new Hashtable();
	
	ServerGame(GameHandler handler, ProtocolInterface net, GameState state) {
		this.handler = handler;
		this.net = net;
		this.state = state;
		
		net.open(this);
		deck.reset();
		rules.bind(this);
	}
	
	void start() {
		StandardCard first = new StandardCard(nextCard++, deck.random(RNG), discardHand);
		first.transfer(discardHand);

		for (int i = 0; i < players; i++) {
			for (int j = 0; j < 7; j++) {
				drawToHand(i);
			}
		}

		hands[0].setPlayable(true);
		state.setLastPlayed(first);
		net.flush();
	}
	
	public RuleSet getRuleSet() {
		return rules;
	}
	
	public int getTurnOrder() {
		return turnOrder;
	}
	
	boolean getStarted() {
		return started;
	}

	public void setTurnOrder(int order) {
		turnOrder = order;
	}
	
	public Deck getDeck() {
		return deck;
	}
	
	public Card getLastPlayed() {
		return discardHand.getLastCard();
	}
	
	public boolean canBePlayed() {
		return canBePlayed(0);
	}
	
	public boolean canBePlayed(int turnPos) {
		return findPlayerHand(turnPos).canBePlayed();
	}
	
	public boolean getHandPlayable(int turnPos) {
		return findPlayerHand(turnPos).getPlayable();
	}
	
	public void setHandPlayable(boolean playable) {
		setHandPlayable(playable, 0);
	}
	
	public void setHandPlayable(boolean playable, int turnPos) {
		findPlayerHand(turnPos).setPlayable(playable);
	}
	
	public boolean isLegal(Card card) {
		return rules.isLegal(card, this);
	}
	
	public void draw() {}
	
	public void finishDraw() {
		drawing = false;
		drew = true;
		rules.finishDraw(this);
	}
	
	private int findPlayer(int turnPos) {
		return GameUtils.findPlayer(turnPos, turnOrder, players, playerGoing);
	}
	
	private StandardHand findPlayerHand(int turnPos) {
		return hands[findPlayer(turnPos)];
	}
	
	private int findTurnPos(int player) {
		return GameUtils.findTurnPos(player, turnOrder, players, playerGoing);
	}
	
	private int findPlayer(Card card) {
		if (card.inHand(discardHand)) {
			for (int i = 0; i < players; i++) {
				if (card.wasInHand(hands[i])) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < players; i++) {
				if (card.inHand(hands[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public int findTurnPos(Card card) {
		return findTurnPos(findPlayer(card));
	}
	
	public boolean isHandPlayer(int hand) {
		return findPlayer(hand) == player;
	}
	
	public int getPlayers() {
		return players;
	}
	
	public int getPlayer() {
		return player;
	}
	
	void setPlayers(int players) {
		if (players <= 0 || players > GameUtils.MAX_PLAYERS) {
			throw new IllegalArgumentException("Player count is out of bounds!");
		}
		this.players = players;
	}
	
	public boolean getDrew() {
		return drew;
	}
	
	public boolean getEnded() {
		return ended;
	}
	
	public boolean getUno() {
		return uno;
	}
	
	public boolean handContains(int turnPos, int cardType) {
		return findPlayerHand(turnPos).contains(cardType);
	}
	
	public boolean handContainsColor(int turnPos, int color) {
		return findPlayerHand(turnPos).containsColor(color);
	}
	
	public void setTypePlayable(int type, boolean playable) {
		setTypePlayable(type, playable, 0);
	}
	
	public void setTypePlayable(int type, boolean playable, int turnPos) {
		findPlayerHand(turnPos).setTypePlayable(type, playable);
	}

	public Card drawToHand(int turnPos) {
		return drawToHand(turnPos, deck.random(RNG));
	}

	public Card drawToHand(int turnPos, int type) {
		uno = false;
		checkUno();
		net.queue(Action.drawToHand(turnPos, type, state));
		return (lastDrawn = new StandardCard(nextCard++, type, findPlayerHand(turnPos)));
	}
	
	public Card getLastDrawn() {
		return lastDrawn;
	}
	
	public void removeFromHand(int turnPos, Card card) {
		int type = card.getType();
		findPlayerHand(turnPos).remove(card);
		net.queue(Action.removeFromHand(turnPos, type, state));
	}
	
	public void play(Card card) {
		if (card.getPlayable() && isLegal(card)) {
			int turnPos = findTurnPos(card);
			int type = card.getType();
			card.transfer(discardHand);
			state.discard(card);
			net.queue(Action.discard(turnPos, type, state));
			card.played();
		}
	}
	
	public void reverse() {
		turnOrder = GameUtils.reverse(turnOrder);
		state.reverse();
		net.queue(Action.reverse(state));
	}
	
	public void rotateHands() {
		GameUtils.rotateHands(hands, players, turnOrder);
		state.rotate();
		net.queue(Action.rotateHands(state));
	}
	
	public boolean checkUno() {
		return findPlayerHand(0).numCards() == 2 && canBePlayed();
	}
	
	public void swapHands(int target1, int target2) {
		GameUtils.swapHands(target1, target2, turnOrder, players, playerGoing, hands);
		state.swapHands(target1, target2);
		net.queue(Action.swap(target1, target2, state));
	}
	
	public void onTurn() {
		hands[playerGoing].setPlayable(false);
		drew = false;
		drawing = false;
		uno = false;
		if (hands[playerGoing].numCards() == 0) {
			onWin();
		} else {
			playerGoing = GameUtils.nextPlayer(turnOrder, players, playerGoing);
			hands[playerGoing].setPlayable(true);
			checkUno();
		}
		state.turn();
		state.setLastPlayed(getLastPlayed().getType());
		net.queue(Action.turn(state, getLastPlayed().getType()));
	}
	
	public void setRuleSet(RuleSet ruleset) {
		rules.unbind(this);
		deck.reset();
		rules = ruleset;
		rules.bind(this);
		state.setRuleSet(ruleset);
		net.queue(Action.setRules(ruleset, state));
	}
	
	// TODO: These functions will need to be modified for use on the server.
	public void startWildCardChallenge() {
		if (rules.wildcardChallenges(this) && discardHand.getSecondToLastCard() != null) {
			sendWildCardChallenge();
		} else {
			for (int i = 0; i < 4; i++) {
				drawToHand(1);
			}
			onTurn();
			onTurn();
		} 
	}
	
	private void sendWildCardChallenge() {
		int type = getLastPlayed().getType();
		challengeActive = true;
		getLastPlayed().setType(type);
		state.sendWildCardChallenge(type);
		net.queue(Action.sendChallenge(type, state));
	}

	// TODO: Add an animation for the outcome of the challenge.
	public void resolveWildCardChallenge(boolean challenged) {
		if (challengeActive) {
			challengeActive = false;
			if (challenged) {
				int color = discardHand.getSecondToLastCard().getCardType().getColor();
				if (findPlayerHand(0).containsColor(color)) {
					for (int i = 0; i < 4; i++) {
						drawToHand(0);
					}
				} else {
					for (int i = 0; i < 6; i++) {
						drawToHand(1);
					}
					onTurn();
				}
				onTurn();
			} else {
				for (int i = 0; i < 4; i++) {
					drawToHand(1);
				}

				onTurn();
				onTurn();
			}
			state.endWildCardChallenge();
			net.queue(Action.endChallenge(state));
		}
	}
	
	void onEnd() {
		ended = true;
		handler.gameOver();
	}
	
	void onWin() {
		onEnd();
	}
	
	public void callUno(int player) {
		if (!uno) {
			if (player != 0 && findPlayerHand(0).numCards() == 2 && canBePlayed()) {
				drawToHand(0);
				drawToHand(0);
			}
			uno = true;
		}
		state.doUno();
	}
	
	public void registerAction(int type, Plugin handler) {
		if (actionHandlers.containsKey(new Integer(type))) {
			throw new IllegalArgumentException("!!! TWO PLUGINS ARE USING ACTION" + type + "!!!");
		}
		actionHandlers.put(new Integer(type), handler);
	}
	
	public void unregisterAction(int type) {
		if (!actionHandlers.containsKey(new Integer(type))) {
			throw new IllegalArgumentException("ActionType " + type + "not in use!");
		}
		actionHandlers.remove(new Integer(type));
	}
	
	public void sendAction(int type, Object arg) {
		if (actionHandlers.containsKey(new Integer(type))) {
			net.queue(new Action(type, arg, state));
		} else {
			throw new IllegalArgumentException("Action type " + type + " not registered");
		}
	}
	
	public void requestTrackingSlot(int cookie, int val) {
		state.requestTrackingSlot(cookie, val);
	}
	
	public void releaseTrackingSlot(int cookie) {
		state.releaseTrackingSlot(cookie);
	}
	
	public void updateSlot(int cookie, int val) {
		if (state.getSlot(cookie) != val) {
			state.updateSlot(cookie, val);
		}
	}
	
	public int getSlot(int cookie) {
		return state.getSlot(cookie);
	}
	
	public void sendInput(int value) {
		if (inHandler != null) {
			inHandler.handleUserInput(this, value);
		}
	}
	
	public void listenForInput(InputHandler handler) {
		inHandler = handler;
	}
	
	public boolean isClient() {
		return false;
	}
	
	private void applyAction(Action a) {
		int type = a.getType();
		Object argument = a.getArgument();
		
		if (type >= 1024) {
			if (actionHandlers.containsKey(new Integer(type))) {
				((Plugin)actionHandlers.get(new Integer(type))).handleAction(type, argument, this);
			}
			return;
		}
		
		switch (type) {
			case Action.PLAY: {
				int cardType = ((Integer)argument).intValue();
				Card toBePlayed = hands[player].getCardByType(cardType);
				if (toBePlayed != null) {
					play(toBePlayed);
				}
				break;
			}
			case Action.START_DRAW:
				if (!drew) {
					drawing = true;
				}
				break;
			case Action.DRAW:
				if (drawing) {
					drawToHand(0);
				}
				break;
			case Action.END_DRAW:
				finishDraw();
				break;
			case Action.RESOLVE_CHALLENGE: {
				boolean decision = ((Boolean)argument).booleanValue();
				resolveWildCardChallenge(decision);
				break;
			}
			case Action.CALL_UNO:
				if (isHandPlayer(0)) {
					callUno(1);
				} else {
					callUno(0);
				}
				net.queue(a);
				break;
			case Action.USER_INPUT: {
				int value = ((Integer)argument).intValue();
				sendInput(value);
				break;
			}
			default:
				break;
		}
	}
	
	boolean dispatch(int forPlayer) {
		Action a = null;
		player = forPlayer;

		while ((a = net.next()) != null) {
			if (!a.check()) {
				continue;
			}
			applyAction(a);
			if (!a.verify(state)) {
				return false;
			}
		}

		net.send();

		return verify();
	}
	
	private boolean verify() {
		return GameUtils.verify(state, players, playerGoing, turnOrder,
			getLastPlayed(), rules, uno, challengeActive, hands);
	}
	
	void setState(GameState state) {
		SimpleCard[][] newHands = state.getHands();

		turnOrder = state.getTurnOrder();
		rules = state.getRules();

		for (int i = 0; i < getPlayers(); i++) {
			hands[i].removeAll();
			hands[i].setPlayable(false);
			for (int j = 0; j < newHands[i].length; j++) {
				new StandardCard(newHands[i][j], hands[i]);
			}
		}

		hands[findTurnPos(0)].setPlayable(isHandPlayer(0));
		uno = state.getUno();
		challengeActive = state.getChallengeActive();
		playerGoing = state.getPlayerGoing();
		
		// TODO: Put the last 2 cards played into the game state
		new StandardCard(state.getLastPlayed(), discardHand);
		this.state = state;
		state.bind(hands);

		if (!verify()) {
			throw new RuntimeException("Failed to set state.");
		}
	}
	
	public void handleError(Exception e) {
		// TODO: write
		e.printStackTrace();
		handler.gameOver();
	}
}
