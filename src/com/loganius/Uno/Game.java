package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import java.util.Date;

/**
 * Represents the space in which a game of Uno is played.
 * TODO: Add safety checks
 */
class Game extends JLayeredPane implements ActionListener {
	public static final int MAX_PLAYERS = 4;
	private static final long serialVersionUID = 1L;
	private static final double arcRatio = 0.13962634;
	private static final Color arcColor = new Color(0, 220, 255);
	static final int CLOCKWISE = 0;
	static final int COUNTER_CLOCKWISE = 1;

	private GameHandler handler;
	private RuleSet rules = new RuleSet.Standard();
	protected Hand[] hands = {
			new Hand(this, 0, 360, 640, 120, 0, false, false),
			new Hand(this, 0, 130, 120, 220, 270, false, false),
			new Hand(this, 0, 0, 400, 120, 180, false, false),
			new Hand(this, 520, 130, 120, 220, 90, false, false),
	};
	protected DiscardHand discardHand = new DiscardHand(this, 0, 360, 640, 120);
	private Theme theme;
	private Deck deck;
	private Card lastDrawn = null;
	private int turnOrder = CLOCKWISE;
	private int players = 1;
	private int player = 0;
	private int playerGoing = 0;
	private boolean started = false;
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
	private Rectangle backgroundBounds;
	private Rectangle arcBounds = new Rectangle(640 / 4 - 30, 480 / 4 - 30, 640 / 2 + 60, 480 / 2 + 60);
	private int[] arcX = new int[43];
	private int[] arcY = new int[43];
	private long lastFrame = (new Date()).getTime();
	private int frame = 0;
	
	Game(Theme theme, GameHandler handler) {
		super();
		this.theme = theme;
		this.handler = handler;
		
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
	}
	
	protected void preInit() {}
	
	void start() {
		started = true;
		add(draw, 0);
		add(unoButton, 0);
	}
	
	void deal() {
		for (int i = 0; i < players; i++) {
			for (int j = 0; j < 7; j++) {
				drawToHand(i);
			}
		}
		
		Card first = new Card(deck.random(), discardHand);
		if (first.getCardType().getColor() == CardType.Color.WILD) {
			first.played();
		}

		first.transfer(discardHand);
	}
	
	RuleSet getRuleSet() {
		return rules;
	}
	
	int getTurnOrder() {
		return turnOrder;
	}
	
	boolean getStarted() {
		return started;
	}

	void setTurnOrder(int order) {
		turnOrder = order;
	}
	
	private void doReverse() {
		if (turnOrder == CLOCKWISE) {
			turnOrder = COUNTER_CLOCKWISE;
		} else {
			turnOrder = CLOCKWISE;
		}
	}
	
	Deck getDeck() {
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
	
	Card getLastPlayed() {
		return discardHand.getLastCard();
	}
	
	boolean canBePlayed() {
		return canBePlayed(0);
	}
	
	boolean canBePlayed(int turnPos) {
		return findPlayerHand(turnPos).canBePlayed();
	}
	
	boolean getHandPlayable(int turnPos) {
		return findPlayerHand(turnPos).getPlayable();
	}
	
	void setHandPlayable(boolean playable) {
		setHandPlayable(playable, 0);
	}
	
	void setHandPlayable(boolean playable, int turnPos) {
		findPlayerHand(turnPos).setPlayable(playable);
	}
	
	boolean isLegal(Card card) {
		return rules.isLegal(card, this);
	}
	
	void onDraw() {
		rules.onDraw(this);
	}
	
	protected int findPlayer(int turnPos) {
		if (turnOrder == CLOCKWISE) {
			return (playerGoing + turnPos) % players;
		} else {
			return (playerGoing + players - turnPos) % players;
		}
	}
	
	protected Hand findPlayerHand(int turnPos) {
		return hands[findPlayer(turnPos)];
	}
	
	protected int findTurnPos(int player) {
		if (turnOrder == CLOCKWISE) {
			return (player + players - playerGoing) % players;
		} else {
			return (playerGoing + players - player) % players;
		}
	}
	
	protected int findPlayer(Card card) {
		if (card.isInHand(discardHand)) {
			for (int i = 0; i < players; i++) {
				if (card.wasInHand(hands[i])) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < players; i++) {
				if (card.isInHand(hands[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	int findTurnPos(Card card) {
		return findTurnPos(findPlayer(card));
	}
	
	int screenToTurnPos(int screenPos) {
		return findTurnPos((screenPos + player) % players);
	}
	
	boolean isHandPlayer(int hand) {
		return true;
	}

	protected void resizeHands() {
		int cardHeight = Card.getHeight(0);
		Rectangle size = getBounds();

		hands[player].onResize(0, size.height - cardHeight, size.width, cardHeight);
		if (players == 2) {
			hands[(player + 1) % players].onResize(cardHeight, 0, size.width - cardHeight * 2, cardHeight);
		} else {
			if ((player + 1) % MAX_PLAYERS < players) {
				hands[(player + 1) % MAX_PLAYERS].onResize(0, cardHeight + cardHeight/8, cardHeight, size.height - cardHeight * 2 - cardHeight / 4);
			}
			if ((player + 2) % MAX_PLAYERS < players) {
				hands[(player + 2) % MAX_PLAYERS].onResize(cardHeight, 0, size.width - cardHeight * 2, cardHeight);
			}
			if ((player + 3) % MAX_PLAYERS < players) {
				hands[(player + 3) % MAX_PLAYERS].onResize(size.width - cardHeight, cardHeight + cardHeight/8, cardHeight, size.height - cardHeight * 2 - cardHeight / 4);
			}
		}
		repaint();
	}
	
	int getPlayers() {
		return players;
	}
	
	protected int getPlayer() {
		return player;
	}
	
	protected int getPlayerGoing() {
		return playerGoing;
	}
	
	protected void setPlayerGoing(int playerGoing) {
		if (playerGoing >= 0 && playerGoing < players) {
			this.playerGoing = playerGoing;
		}
	}
	
	protected void setPlayer(int player, int players) {
		if (players <= 0 || players > MAX_PLAYERS || player < 0 || player >= players) {
			throw new IllegalArgumentException("Player count is out of bounds!");
		}
		this.player = player;
		this.players = players;
		if (players == 2) {
			hands[player].setOrientation(0);
			hands[(player + 1) % players].setOrientation(180);
		} else {
			for (int i = 0; i < players; i++) {
				hands[i].setOrientation((360 - 90*((i + MAX_PLAYERS - player) % MAX_PLAYERS)) % 360);
			}
		}
		resizeHands();
	}
	
	void drew() {
		drew = true;
	}
	
	boolean getDrew() {
		return drew;
	}
	
	boolean getEnded() {
		return ended;
	}
	
	boolean getUno() {
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
	
	boolean handContains(int turnPos, int cardType) {
		return findPlayerHand(turnPos).contains(cardType);
	}
	
	boolean handContainsColor(int turnPos, int color) {
		return findPlayerHand(turnPos).containsColor(color);
	}
	
	void setTypePlayable(int type, boolean playable) {
		setTypePlayable(type, playable, 0);
	}
	
	void setTypePlayable(int type, boolean playable, int turnPos) {
		findPlayerHand(turnPos).setTypePlayable(type, playable);
	}

	/* These functions here are intended to be intercepted so their work can be captured
	 * and sent over the network. */
	Card drawToHand(int turnPos) {
		return drawToHand(turnPos, deck.random());
	}

	Card drawToHand(int turnPos, int type) {
		lastDrawn = new Card(type, findPlayerHand(turnPos));
		return (lastDrawn);
	}
	
	Card getLastDrawn() {
		return lastDrawn;
	}
	
	void removeFromHand(int turnPos, Card card) {
		findPlayerHand(turnPos).remove(card);
		remove(card);
	}
	
	void discard(Card card) {
		card.transfer(discardHand);
	}
	
	void reverse() {
		doReverse();
	}
	
	void rotateHands() {
		Hand temp1;
		Hand temp2;
		boolean[] playableBefore = new boolean[players];
		boolean[] upBefore = new boolean[players];
		int[] orientationBefore = new int[players];

		for (int i = 0; i < players; i++) {
			playableBefore[i] = hands[i].getPlayable();
			upBefore[i] = hands[i].getUp();
			orientationBefore[i] = hands[i].getOrientation();
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
			hands[i].setUp(upBefore[i]);
			hands[i].setPlayable(playableBefore[i]);
			hands[i].setOrientation(orientationBefore[i]);
		}
		
		resizeHands();
	}
	
	boolean checkUno() {
		if (findPlayerHand(0).numCards() == 2 && canBePlayed()) {
			uno = false;
			unoButton.setVisible(true);
			return true;
		}
		return false;
	}
	
	void swapHands(int turnPos1, int turnPos2) {
		int hand1 = findPlayer(turnPos1);
		int hand2 = findPlayer(turnPos2);
		boolean[] playableBefore = {hands[hand1].getPlayable(), hands[hand2].getPlayable()};
		boolean[] upBefore = {hands[hand1].getUp(), hands[hand2].getUp()};
		int[] orientationBefore = {hands[hand1].getOrientation(), hands[hand2].getOrientation()};
		Hand temp = hands[hand1];

		if (hands[hand1].numCards() <= 0 && hands[hand2].numCards() <= 0) {
			return;
		}
		
		hands[hand1] = hands[hand2];
		hands[hand2] = temp;
		
		hands[hand1].setUp(upBefore[0]);
		hands[hand1].setPlayable(playableBefore[0]);
		hands[hand1].setOrientation(orientationBefore[0]);

		hands[hand2].setUp(upBefore[1]);
		hands[hand2].setPlayable(playableBefore[1]);
		hands[hand2].setOrientation(orientationBefore[1]);
		
		resizeHands();
	}
	
	void onTurn() {
		if (!isInterrupted()) {
			hands[playerGoing].setPlayable(false);
			unoButton.setVisible(false);
			drew = false;
			uno = false;
			if (hands[playerGoing].numCards() == 0) {
				onWin();
			} else {
				if (turnOrder == CLOCKWISE) {
					playerGoing = (playerGoing + 1) % players;
				} else {
					playerGoing = (playerGoing + players - 1) % players;
				}
				checkUno();
			}
		}
	}
	
	void setRuleSet(RuleSet ruleset) {
		rules.unbind(this);
		deck.reset();
		rules = ruleset;
		rules.bind(this);
	}
	
	void startWildCardChallenge() {
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
	
	void sendWildCardChallenge() {
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
			
			/** 
			 * Subclasses of Game which have non-local-player hands must handle
			 * wildcard challenges on their own, or else the game would
			 * softlock.
			 * This exception is here to make sure they do.
			 * If you wrap this in a try-catch and do nothing, that's your
			 * fault.
			 */
			throw new RuntimeException(
					"Dear Developer, you appear not to have implemented your" +
					"subclass of Game properly. Please fix that." + 
					"-Developer");
		}
	}

	// TODO: Add an animation for the outcome of the challenge.
	void handleWildCardChallenge(boolean challenged) {
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
			}
		}
	}
	
	protected void endWildCardChallenge() {
		customUISpace.removeAll();
		customUISpace.setVisible(false);
		challengeActive = false;
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
	
	void callUno(int player) {
		if (!uno) {
			if (player != 0 && findPlayerHand(0).numCards() == 2 && canBePlayed()) {
				drawToHand(0);
				drawToHand(0);
			}
			unoButton.setVisible(false);
			uno = true;
		}
	}
	
	protected void gameOver() {
		handler.gameOver();
	}
	
	protected void onResize() {
		Rectangle size = getBounds();
		int cardWidth = (int)(Util.scale(80));
		int cardHeight = (int)(Util.scale(120));
		Card.setWidth(cardWidth);
		Card.setHeight(cardHeight);

		discardHand.onResize((size.width - cardWidth)/2, (size.height - cardHeight)/2, cardWidth, cardHeight);
		customUISpace.setBounds(0, cardHeight, size.width, size.height - cardHeight * 2);
		draw.setBounds(0, 0, cardHeight, cardHeight);
		unoButton.setBounds(size.width - cardHeight, 0, cardHeight, cardHeight);
		
		backgroundBounds = Util.scaleAndCrop(new Rectangle(bgSource.getWidth(null), bgSource.getHeight(null)), size);
		background = Util.bufferScaledImage(bgSource, backgroundBounds.width, backgroundBounds.height);

		arcBounds = new Rectangle((int)(size.width / 4 - Util.scale(30)), (int)(size.height / 4 - Util.scale(30)), (int)(size.width / 2 + Util.scale(60)), (int)(size.height / 2 + Util.scale(60)));
		resizeHands();
	}

	public void paintComponent(Graphics g) {
		Rectangle bounds = getBounds();
		
		g.drawImage(background, backgroundBounds.x, backgroundBounds.y, backgroundBounds.width, backgroundBounds.height, this);
		if (arcBounds.intersects(g.getClipBounds())) {
			g.setColor(arcColor);
			
			if (turnOrder == CLOCKWISE) {
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
}
