package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

/**
 * Represents the space in which a game of Uno is played.
 * TODO: Add safety checks
 */
public class UIGame extends JLayeredPane implements ActionListener, Game {
	private static final double arcRatio = 0.13962634;
	private static final Color arcColor = new Color(0, 220, 255);

	private Game game = this;
	private GameHandler handler;
	private ProtocolInterface io;
	private InputHandler inHandler = null;
	private RuleSet rules = new RuleSet.Standard();
	protected UIStandardHand[] hands = {
			new UIStandardHand(this, 0, 360, 640, 120, 0, false),
			new UIStandardHand(this, 0, 130, 120, 220, 270, false),
			new UIStandardHand(this, 0, 0, 400, 120, 180, false),
			new UIStandardHand(this, 520, 130, 120, 220, 90, false),
	};
	protected UIDiscardHand discardHand = new UIDiscardHand(this, 0, 360);
	private Theme theme;
	private Deck deck;
	private UICard lastDrawn = null;
	private QueueHandler queueHandler = new QueueHandler();
	private volatile GameState state;
	private Random RNG = new Random();
	private Hashtable actionHandlers = new Hashtable();
	private int turnOrder = GameUtils.CLOCKWISE;
	private int players = 1;
	private int player = 0;
	private int playerGoing = 0;
	private boolean started = false;
	private boolean drawing = false;
	private boolean drew = false;
	private boolean uno = false;
	private boolean ended = false;
	private boolean challengeActive = false;
	private int interruptQueue = 0;
	
	private DrawButton draw;
	private UnoButton unoButton;
	private JPanel customUISpace = new JPanel();
	private Image bgSource;
	private Image background;
	private PlayerOverlay overlay = new PlayerOverlay(this);
	private Rectangle backgroundBounds;
	private Rectangle arcBounds = new Rectangle(640 / 4 - 30, 480 / 4 - 30, 640 / 2 + 60, 480 / 2 + 60);
	private String[] names;
	private int[] arcX = new int[43];
	private int[] arcY = new int[43];
	private long lastFrame = (new Date()).getTime();
	private int frame = 0;
	
	UIGame(Theme theme, String name, GameHandler handler, ProtocolInterface io) {
		this.state = new GameState(hands);
		this.theme = theme;
		this.handler = handler;
		this.io = io;
		
		draw = new DrawButton(this);
		unoButton = new UnoButton(this);
		
		bgSource = theme.getGameBackground();
		backgroundBounds = Util.scaleAndCrop(new Rectangle(bgSource.getWidth(null), bgSource.getHeight(null)), new Rectangle(640, 480));
		background = Util.bufferScaledImage(bgSource, backgroundBounds.width, backgroundBounds.height);
		deck = theme.getDeck();
		preInit();

		deck.reset();
		rules.bind(this);

		customUISpace.setVisible(false);
		customUISpace.setOpaque(false);

		setLayout(null);
		add(customUISpace, MODAL_LAYER);
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				onResize();
			}
		});
		
		new Timer(33, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isInterrupted()) {
					long now = (new Date()).getTime();
					frame = (frame + (int)(Math.round(((double)(now - lastFrame))/33))) % 360;
					lastFrame = now;
					repaint(arcBounds);
				}
			}
		}).start();

		names = new String[] { name };
		
		io.open(this);
		io.queue(Action.hello(name));
		io.send();
		
		queueHandler.start();
	}
	
	void requestStart() {
		io.queue(Action.requestStart(rules));
		io.send();
	}
	
	protected void preInit() {}
	
	protected void start(Action.State state) {
		if (!started) {
			Card first;
			started = true;
			add(draw, 0);
			add(unoButton, 0);
			setState(state);
			first = (Card) getLastPlayed();
			if (isHandPlayer(0) && first.getCardType().getColor() == CardType.Color.WILD) {
				play(first);
			}
		}
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
		state.setTurnOrder(order);
	}
	
	public Deck getDeck() {
		return deck;
	}
	
	Theme getTheme() {
		return theme;
	}
	
	Color getTextColor() {
		return theme.getGameText();
	}
	
	Color getAccentColor() {
		return theme.getGameAccent();
	}
	
	JPanel getCustomUISpace() {
		return customUISpace;
	}
	
	void interrupt() {
		interruptQueue++;
	}
	
	void release() {
		if (isInterrupted()) {
			interruptQueue--;
		}
	}
	
	boolean isInterrupted() {
		return (interruptQueue > 0);
	}
	
	Rectangle getDiscardHandBounds() {
		return discardHand.getBounds();
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
	
	public void draw() {
		drawing = true;
		io.queue(Action.startDraw(state));
		rules.onDraw(this);
		drew = true;
	}
	
	public void finishDraw() {
		rules.finishDraw(this);
		drawing = false;
		io.queue(Action.endDraw(state));
		io.send();
	}
	
	protected int findPlayer(int turnPos) {
		return GameUtils.findPlayer(turnPos, turnOrder, players, playerGoing);
	}
	
	protected UIStandardHand findPlayerHand(int turnPos) {
		return hands[findPlayer(turnPos)];
	}
	
	protected int findTurnPos(int player) {
		return GameUtils.findTurnPos(player, turnOrder, players, playerGoing);
	}
	
	protected int findPlayer(Card card) {
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
	
	int screenToTurnPos(int screenPos) {
		return findTurnPos((screenPos + player) % players);
	}
	
	public boolean isHandPlayer(int hand) {
		return true;
	}

	protected void resizeHands() {
		int cardHeight = UICard.getHeight(0);
		Rectangle size = getBounds();

		hands[player].onResize(0, size.height - cardHeight, size.width, cardHeight);
		if (players == 2) {
			hands[(player + 1) % players].onResize(cardHeight, 0, size.width - cardHeight * 2, cardHeight);
		} else {
			if ((player + 1) % GameUtils.MAX_PLAYERS < players) {
				hands[(player + 1) % GameUtils.MAX_PLAYERS].onResize(0, cardHeight + cardHeight/8, cardHeight, size.height - cardHeight * 2 - cardHeight / 4);
			}
			if ((player + 2) % GameUtils.MAX_PLAYERS < players) {
				hands[(player + 2) % GameUtils.MAX_PLAYERS].onResize(cardHeight, 0, size.width - cardHeight * 2, cardHeight);
			}
			if ((player + 3) % GameUtils.MAX_PLAYERS < players) {
				hands[(player + 3) % GameUtils.MAX_PLAYERS].onResize(size.width - cardHeight, cardHeight + cardHeight/8, cardHeight, size.height - cardHeight * 2 - cardHeight / 4);
			}
		}
		repaint();
	}
	
	public int getPlayers() {
		return players;
	}
	
	public int getPlayer() {
		return player;
	}
	
	protected int getPlayerGoing() {
		return playerGoing;
	}
	
	protected void setPlayerGoing(int playerGoing) {
		if (playerGoing >= 0 && playerGoing < players) {
			this.playerGoing = playerGoing;
			state.setPlayerGoing(playerGoing);
		}
	}
	
	protected void setPlayer(int player, int players) {
		if (players <= 0 || players > GameUtils.MAX_PLAYERS || player < 0 || player >= players) {
			throw new IllegalArgumentException("Player count is out of bounds!");
		}
		this.player = player;
		this.players = players;
		if (players == 2) {
			hands[player].setOrientation(0);
			hands[(player + 1) % players].setOrientation(180);
		} else {
			for (int i = 0; i < players; i++) {
				hands[i].setOrientation((360 - 90*((i + GameUtils.MAX_PLAYERS - player) % GameUtils.MAX_PLAYERS)) % 360);
			}
		}
		resizeHands();
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
	
	protected void setUno(boolean uno) {
		this.uno = uno;
	}
	
	protected boolean getChallengeActive() {
		return challengeActive;
	}
	
	protected void setChallengeActive(boolean challengeActive) {
		if (challengeActive != this.challengeActive) {
			if (this.challengeActive) {
				endWildCardChallenge();
			} else {
				sendWildCardChallenge();
			}
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		gameOver();
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

	/* These functions here are intended to be intercepted so their work can be captured
	 * and sent over the network. */
	public Card drawToHand(int turnPos) {
		return drawToHand(turnPos, deck.random(RNG));
	}

	private Card drawToHand(int turnPos, int type) {
		lastDrawn = new UICard(type, findPlayerHand(turnPos));
		checkUno();
		if (drawing) {
			io.queue(Action.draw(state));
			io.send();
		}
		return lastDrawn;
	}
	
	public Card getLastDrawn() {
		return lastDrawn;
	}
	
	public void removeFromHand(int turnPos, Card card) {
		if (!(card instanceof UICard)) {
			throw new IllegalArgumentException("Card is not a UICard!");
		}
		findPlayerHand(turnPos).remove(card);
		remove((UICard)card);
	}
	
	private void discard(Card card) {
		card.transfer(discardHand);
		state.discard(card);
	}
	
	public void play(Card card) {
		if (!isInterrupted() && isLegal(card)) {
			int type = card.getType();
			discard(card);
			card.played();
			io.queue(Action.play(type, state));
			io.send();
		}
	}
	
	public void reverse() {
		turnOrder = GameUtils.reverse(turnOrder);
		state.reverse();
	}
	
	public void rotateHands() {
		boolean[] upBefore = new boolean[players];
		int[] orientationBefore = new int[players];

		for (int i = 0; i < players; i++) {
			upBefore[i] = hands[i].getUp();
			orientationBefore[i] = hands[i].getOrientation();
		}

		GameUtils.rotateHands(hands, players, turnOrder);
		
		for (int i = 0; i < players; i++) {
			hands[i].setUp(upBefore[i]);
			hands[i].setOrientation(orientationBefore[i]);
		}
		
		resizeHands();
		state.rotate();
	}
	
	public boolean checkUno() {
		if (findPlayerHand(0).numCards() == 2 && canBePlayed()) {
			uno = false;
			unoButton.setVisible(true);
			return true;
		}
		return false;
	}
	
	public void swapHands(int target1, int target2) {
		int hand1 = findPlayer(target1);
		int hand2 = findPlayer(target2);
		boolean[] upBefore = {hands[hand1].getUp(), hands[hand2].getUp()};
		int[] orientationBefore = {hands[hand1].getOrientation(), hands[hand2].getOrientation()};
		
		GameUtils.swapHands(target1, target2, turnOrder, players, playerGoing, hands);
		
		hands[hand1].setUp(upBefore[0]);
		hands[hand2].setUp(upBefore[1]);
		hands[hand1].setOrientation(orientationBefore[0]);
		hands[hand2].setOrientation(orientationBefore[1]);
		
		resizeHands();
	}
	
	public void onTurn() {
		if (!isInterrupted()) {
			hands[playerGoing].setPlayable(false);
			unoButton.setVisible(false);
			drew = false;
			uno = false;
			if (hands[playerGoing].numCards() == 0) {
				onWin();
			} else {
				playerGoing = GameUtils.nextPlayer(turnOrder, players, playerGoing);
				setHandPlayable(isHandPlayer(0));
				overlay.setActivePlayer(playerGoing);
				checkUno();
			}
			
			state.turn();
			state.setLastPlayed(getLastPlayed().getType());
		}
	}
	
	public void setRuleSet(RuleSet ruleset) {
		rules.unbind(this);
		deck.reset();
		rules = ruleset;
		rules.bind(this);
		state.setRuleSet(ruleset);
	}
	
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
	
	public void sendInput(int value) {
		if (inHandler != null) {
			inHandler.handleUserInput(this, value);
			io.queue(Action.input(value, state));
			io.send();
		}
	}
	
	public void listenForInput(InputHandler handler) {
		inHandler = handler;
	}
	
	public void sendWildCardChallenge() {
		challengeActive = true;
		if (isHandPlayer(1)) {
			JButton challenge = new JButton("Challenge");
			JButton decline = new JButton("Decline");
			JPanel buttons = new JPanel();

			Util.commonComponentInit(challenge, Util.BLACK);
			Util.commonComponentInit(decline, Util.BLACK);
			customUISpace.setLayout(new BoxLayout(customUISpace, BoxLayout.Y_AXIS));
			customUISpace.add(Box.createVerticalGlue());
			
			buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
			buttons.setOpaque(false);
			
			challenge.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					handleWildCardChallenge(true);
				}
			});
			
			decline.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					handleWildCardChallenge(false);
				}
			});
			
			buttons.add(challenge);
			buttons.add(decline);
			customUISpace.add(buttons);
			customUISpace.setVisible(true);
		} else {
			JLabel waiting = new JLabel("Waiting for player to challenge.", SwingConstants.CENTER);
			Util.commonComponentInit(waiting, getTextColor());
			customUISpace.setLayout(new GridLayout(1, 1));
			customUISpace.add(waiting);
			customUISpace.setVisible(true);
		}
		state.sendWildCardChallenge(getLastPlayed().getType());
	}

	// TODO: Add an animation for the outcome of the challenge.
	public void handleWildCardChallenge(final boolean challenged) {
		if (challengeActive) {
			endWildCardChallenge();
			if (challenged) {
				final boolean upBefore = findPlayerHand(0).getUp();
				findPlayerHand(0).setUp(true);
				
				Timer handleDelay = new Timer(2000, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int color = discardHand.getSecondToLastCard().getCardType().getColor();

						findPlayerHand(0).setUp(upBefore);
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
						io.queue(Action.resolveChallenge(challenged, state));
						io.send();
					}
				});

				handleDelay.setRepeats(false);
				handleDelay.start();
			} else {
				for (int i = 0; i < 4; i++) {
					drawToHand(1);
				}

				onTurn();
				onTurn();
				io.queue(Action.resolveChallenge(challenged, state));
				io.send();
			}
		}
	}
	
	public boolean isClient() {
		return true;
	}
	
	protected void endWildCardChallenge() {
		customUISpace.removeAll();
		customUISpace.setVisible(false);
		challengeActive = false;
		state.endWildCardChallenge();
	}

	protected boolean verify() {
		return GameUtils.verify(state, players, playerGoing, turnOrder,
				getLastPlayed(), rules, uno, challengeActive, hands);
		}
	
	void onEnd(String msg) {
		JButton endButton = new JButton("Leave");
		JLabel label = new JLabel();
		interrupt();
		for (int i = 0; i < players; i++) {
			hands[i].setUp(true);
		}

		customUISpace.removeAll();
		customUISpace.setLayout(new BoxLayout(customUISpace, BoxLayout.Y_AXIS));
		
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		label.setVerticalAlignment(SwingConstants.BOTTOM);
		label.setFont(Util.getScaledFont());
		label.setForeground(new Color(255, 255, 255));
		label.setText(msg);
		label.addComponentListener(Util.ResizeListener);
		
		endButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		endButton.setFont(Util.getScaledFont());
		endButton.addActionListener(this);
		endButton.addComponentListener(Util.ResizeListener);
		
		customUISpace.add(label);
		customUISpace.add(endButton);
		customUISpace.setVisible(true);
		ended = true;
		
		for (int i = 0; i < players; i++) {
			hands[i].setUp(true);
		}

		validate();
		repaint();
	}
	
	void onWin() {
		onEnd((isHandPlayer(0)) ? "You Win!" : "You Lose");
	}
	
	public void callUno(int player) {
		if (!uno) {
			if (player != 0 && findPlayerHand(0).numCards() == 2 && canBePlayed()) {
				drawToHand(0);
				drawToHand(0);
			}
			unoButton.setVisible(false);
			uno = true;
		}
		
		state.doUno();
	}
	
	private void removeFromHand(int turnPos, int cardType) {
		Card toBeRemoved = findPlayerHand(turnPos).getCardByType(cardType);
		if (toBeRemoved != null) {
			removeFromHand(turnPos, toBeRemoved);
		}
	}
	
	private void discard(int cardType, int turnPos) {
		Card toBeDiscarded = findPlayerHand(turnPos).getCardByType(cardType);
		if (toBeDiscarded != null) {
			discard(toBeDiscarded);
		}
	}
	
	void applyAction(Action a) {
		int type = a.getType();
		Object argument = a.getArgument();

		if (type >= 1024) {
			if (actionHandlers.containsKey(new Integer(type))) {
				((Plugin)actionHandlers.get(new Integer(type))).handleAction(type, argument, this);
			}
			return;
		}

		switch (type) {
			case Action.CALL_UNO:
				if (isHandPlayer(0)) {
					callUno(1);
				} else {
					callUno(0);
				}
				break;
			case Action.SEND_CHALLENGE: {
				int cardType = ((Integer)argument).intValue();
				getLastPlayed().setType(cardType);
				sendWildCardChallenge();
				break;
			}
			case Action.WELCOME: {
				if (a.getArgument().getClass() != Action.Welcome.class) {
					break;
				}
				Action.Welcome hello = (Action.Welcome) a.getArgument();
				int newPlayer = hello.getPlayer();
				int newNumPlayers = hello.getNumPlayers();
				String[] newNames = hello.getNames();
				if (newPlayer < 0 || newPlayer >= 4 || newNumPlayers <= 0 || newNumPlayers > 4) {
					break;
				}

				setPlayer(hello.getPlayer(), hello.getNumPlayers());
				state.setPlayers(newNumPlayers);
				if (getPlayer() == 0 && getPlayers() > 1) {
					overlay.showStartButton();
				}

				if (newNames == null || newNames.length > 4) {
					break;
				}
				names = newNames;
				overlay.setPlayers(names);
				break;
			}
			case Action.START: {
				Action.State state = (Action.State)argument;
				overlay.start(getPlayer(), getPlayers());
				start(state);
				break;
			}
			 case Action.SET_STATE: {
				Action.State state = (Action.State) a.getArgument();
				setState(state);
				break;
			}
			case Action.DRAW_TO_HAND: {
				int args[] = (int[]) argument;
				drawToHand(args[0], args[1]);
				break;
			} case Action.REMOVE_FROM_HAND: {
				int args[] = (int[]) argument;
				removeFromHand(args[0], args[1]);
				break;
			} case Action.DISCARD: {
				int args[] = (int[]) argument;
				discard(args[1], args[0]);
				break;
			} case Action.REVERSE:
				reverse();
				break;
			case Action.ROTATE_HANDS:
				rotateHands();
				break;
			case Action.TURN: {
				int cardType = ((Integer)argument).intValue();
				getLastPlayed().setType(cardType);
				onTurn();
				break;
			}
			case Action.SWAP: {
				int args[] = (int[])argument;
				swapHands(args[0], args[1]);
				break;
			}
			case Action.END_CHALLENGE:
				endWildCardChallenge();
				break;
			case Action.SET_RULES:
				setRuleSet((RuleSet)argument);
				break;
			default:
				break;
		}
	}
	
	private void setState(Action.State state) {
		GameState newState = state.getState();
		SimpleCard[][] newHands = newState.getHands();

		for (int i = 0; i < getPlayers(); i++) {
			hands[i].removeAll();
			hands[i].setPlayable(false);
			for (int j = 0; j < newHands[i].length; j++) {
				new UICard(newHands[i][j], hands[i]);
			}
		}

		RNG.setSeed(state.getSeed());
		setTurnOrder(newState.getTurnOrder());
		setRuleSet(newState.getRules());
		setUno(newState.getUno());
		setChallengeActive(newState.getChallengeActive());
		setPlayerGoing(newState.getPlayerGoing());
		setHandPlayable(isHandPlayer(0));
		overlay.setActivePlayer(playerGoing);
		
		// TODO: Put the last 2 cards played into the game state
		new UICard(newState.getLastPlayed(), discardHand);
		this.state = newState;
		resizeHands();
		
		io.flush();
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
			io.queue(new Action(type, arg, state));
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
	
	protected void gameOver() {
		handler.gameOver();
	}
	
	protected void onResize() {
		Rectangle size = getBounds();
		int cardWidth = (int)(Util.scale(80));
		int cardHeight = (int)(Util.scale(120));
		UICard.setWidth(cardWidth);
		UICard.setHeight(cardHeight);

		discardHand.onResize((size.width - cardWidth)/2, (size.height - cardHeight)/2);
		customUISpace.setBounds(0, cardHeight, size.width, size.height - cardHeight * 2);
		draw.setBounds(0, 0, cardHeight, cardHeight);
		unoButton.setBounds(size.width - cardHeight, 0, cardHeight, cardHeight);
		
		backgroundBounds = Util.scaleAndCrop(new Rectangle(bgSource.getWidth(null), bgSource.getHeight(null)), size);
		background = Util.bufferScaledImage(bgSource, backgroundBounds.width, backgroundBounds.height);

		arcBounds = new Rectangle((int)(size.width / 4 - Util.scale(30)), (int)(size.height / 4 - Util.scale(30)), (int)(size.width / 2 + Util.scale(60)), (int)(size.height / 2 + Util.scale(60)));
		overlay.setBounds(0, 0, size.width, size.height);
		resizeHands();
	}
	
	public void handleError(Exception e) {
		if (!getEnded()) {
			final String msg = (e.getMessage() == null) ? "A networking error occured." : e.getMessage();
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						onEnd(msg);
					}
				});
			} catch (Exception e2) {
				ended = true;
				e2.printStackTrace();
			};

			e.printStackTrace();

			try {
				io.close();
			} catch (Exception e2) {};
			
			overlay.setVisible(false);
			queueHandler.interrupt();
		}
	}

	public void paintComponent(Graphics g) {
		Rectangle bounds = getBounds();
		
		g.drawImage(background, backgroundBounds.x, backgroundBounds.y, backgroundBounds.width, backgroundBounds.height, this);
		if (arcBounds.intersects(g.getClipBounds())) {
			g.setColor(arcColor);
			
			if (turnOrder == GameUtils.CLOCKWISE) {
				double sin = 0;
				double cos = 0;
				for (int i = 0; i < 20; i++) {
					sin = Math.sin((i + frame)*arcRatio);
					cos = Math.cos((i + frame)*arcRatio);
					arcX[i] = (int)(cos*(bounds.height / 4)) + bounds.width / 2;
					arcY[i] = (int)(sin*(bounds.height / 4)) + bounds.height / 2;
					arcX[42 - i] = (int)(cos*((bounds.height / 4) - Util.scale(20))) + bounds.width / 2;
					arcY[42 - i] = (int)(sin*((bounds.height / 4) - Util.scale(20))) + bounds.height / 2;
				}
				
				// Draw a triangle.
				arcX[20] = arcX[19] - (int)(cos*Util.scale(40));
				arcY[20] = arcY[19] - (int)(sin*Util.scale(40));
				arcX[21] = arcX[19] + (int)((cos*Util.scale(-10)) - (sin*Util.scale(51.962)));
				arcY[21] = arcY[19] + (int)((sin*Util.scale(-10)) + (cos*Util.scale(51.962)));
				arcX[22] = arcX[19] + (int)(cos*Util.scale(20));
				arcY[22] = arcY[19] + (int)(sin*Util.scale(20));
			} else {
				double sin = 0;
				double cos = 0;
				for (int i = 0; i < 20; i++) {
					sin = Math.sin((i - frame)*arcRatio);
					cos = Math.cos((i - frame)*arcRatio);
					arcX[i] = (int)(cos*(bounds.height / 4)) + bounds.width / 2;
					arcY[i] = (int)(sin*(bounds.height / 4)) + bounds.height / 2;
					arcX[39 - i] = (int)(cos*((bounds.height / 4) - Util.scale(20))) + bounds.width / 2;
					arcY[39 - i] = (int)(sin*((bounds.height / 4) - Util.scale(20))) + bounds.height / 2;
				}
				
				// Draw a triangle.
				sin = Math.sin(frame*arcRatio);
				cos = Math.cos(frame*arcRatio);
				arcX[40] = arcX[0] - (int)(cos*Util.scale(40));
				arcY[40] = arcY[0] + (int)(sin*Util.scale(40));
				arcX[41] = arcX[0] + (int)((cos*Util.scale(-10)) - (sin*Util.scale(51.962)));
				arcY[41] = arcY[0] - (int)((sin*Util.scale(-10)) + (cos*Util.scale(51.962)));
				arcX[42] = arcX[0] + (int)(cos*Util.scale(20));
				arcY[42] = arcY[0] - (int)(sin*Util.scale(20));
			}
	
			g.fillPolygon(arcX, arcY, 43);
		}
	}
	
	// TODO: Implement ack and keepalive
	private class QueueHandler extends Thread {
		public void run() {
			while (true) {
				Vector acts = new Vector();
				Action a;

				synchronized(io) {
					try {
						io.wait();
					} catch (InterruptedException e) {
						return;
					}
				}
				
				while ((a = io.next()) != null) {
					if (!a.check()) {
						continue;
					}
					acts.addElement(a);
				}

				if (acts.size() == 0) {
					continue;
				}
				
				GameState oldState = state;
				state = (GameState)state.clone();
				for (int i = 0; i < acts.size(); i++) {
					a = (Action)acts.elementAt(i);
					if (a.getType() >= 1024) {
						if (actionHandlers.get(new Integer(a.getType())) != null) {
							((Plugin)actionHandlers.get(new Integer(a.getType()))).simulate(a.getType(), a.getArgument(), game);
						}
					} else {
						state.applyAction(a);
					}
				}
				
				if (a.verify(state)) {
					state = oldState;
					for (int i = 0; i < acts.size(); i++) {
						final Action act = (Action)acts.elementAt(i);
						try {
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									applyAction(act);
								}
							});
						} catch (Exception e) {};
					}
					if (!a.verify(state) || !verify()) {
						io.flush();
						io.queue(Action.getState());
						io.send();
					}
				} else {
					io.flush();
					io.queue(Action.getState());
					io.send();
				}
			}
		}
	}
}
