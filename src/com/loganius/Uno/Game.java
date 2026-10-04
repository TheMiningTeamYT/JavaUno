package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;

/**
 * Represents the space in which a game of Uno is played.
 * TODO: Add safety checks
 */
class Game extends JLayeredPane implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final double arcRatio = 0.13962634;
	private static final Color arcColor = new Color(0, 220, 255);
	static final int clockwise = 0;
	static final int counterClockwise = 1;

	private GameHandler handler;
	private RuleSet rules = new RuleSet.Composite(new Rule[] {new Stacking(), new DrawToMatch(), new ForcePlay(), new SevenZero(),});
	protected Hand[] hands = {
			new Hand(this, 0, 360, 640, 120, 0, true, false),
			new Hand(this, 0, 130, 120, 220, 270, false, false),
			new Hand(this, 0, 0, 400, 120, 180, false, false),
			new Hand(this, 520, 130, 120, 220, 90, false, false),
	};
	protected DiscardHand discardHand = new DiscardHand(this, 0, 360, 640, 120);
	protected Hand[] screenHands;
	private Deck deck;
	private int turnOrder = clockwise;
	private int players = 1;
	private boolean drew = false;
	private boolean uno = false;
	private boolean ended = false;
	
	private DrawButton draw = new DrawButton(this);
	private UnoButton unoButton = new UnoButton(this);
	private JPanel customUISpace = new JPanel();
	private Vector interruptQueue = new Vector();
	private Image bgSource = Util.getImage(Util.getResource("Assets/background.jpg"));
	private Image background = Util.bufferImage(bgSource);
	private Rectangle backgroundBounds = new Rectangle(0, -80, 640, 640);
	private Rectangle arcBounds = new Rectangle(640 / 4 - 30, 480 / 4 - 30, 640 / 2 + 60, 480 / 2 + 60);
	private int[] arcX = new int[43];
	private int[] arcY = new int[43];
	private int frame = 0;
	
	Game(Deck deck, GameHandler handler) {
		super();
		this.deck = deck;
		this.handler = handler;
		screenHands = (Hand[])hands.clone();
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
					frame = (frame + 1) % 360;
					repaint(arcBounds);
				}
			}
		}).start();
	}
	
	void start() {
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
	
	void setRuleSet(RuleSet ruleset) {
		rules = ruleset;
		deck.reset();
		rules.bind(this);
	}
	
	int getTurnOrder() {
		return turnOrder;
	}

	void setTurnOrder(int order) {
		if (order == turnOrder) {
			return;
		}
		doReverse();
	}
	
	private void doReverse() {
		rotate();

		for (int i = 0; i < players / 2; i++) {
            Hand temp = hands[i];
            hands[i] = hands[players - 1 - i];
            hands[players - 1 - i] = temp;
        }

		if (turnOrder == clockwise) {
			turnOrder = counterClockwise;
		} else {
			turnOrder = clockwise;
		}
		
		repaint(arcBounds);
	}
	
	Deck getDeck() {
		return deck;
	}
	
	protected void rotate() {
		Hand temp1 = hands[0];
		Hand temp2;
		for (int i = players - 1; i >= 0; i--) {
			temp2 = hands[i];
			hands[i] = temp1;
			temp1 = temp2;
		}

	}
	
	JPanel getCustomUISpace() {
		return customUISpace;
	}
	
	void interrupt() {
		interruptQueue.addElement(new Boolean(true));
	}
	
	void release() {
		if (isInterrupted()) {
			interruptQueue.removeElementAt(interruptQueue.size() - 1);
		}
	}
	
	boolean isInterrupted() {
		return (interruptQueue.size() > 0);
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
	
	boolean canBePlayed(int hand) {
		return hands[hand].canBePlayed();
	}
	
	boolean getHandPlayable(int hand) {
		return hands[hand].getPlayable();
	}
	
	void setHandPlayable(boolean playable) {
		setHandPlayable(playable, 0);
	}
	
	void setHandPlayable(boolean playable, int hand) {
		hands[hand].setPlayable(playable);
	}
	
	boolean isLegal(Card card) {
		return rules.isLegal(card, this);
	}
	
	void onDraw() {
		rules.onDraw(this);
	}
	
	protected int findScreenHand(int hand) {
		for (int i = 0; i < screenHands.length; i++) {
			if (hands[hand] == screenHands[i]) {
				return i;
			}
		}
		
		// Should be unreachable.
		return -1;
	}
	
	protected int findHand(int hand) {
		for (int i = 0; i < screenHands.length; i++) {
			if (screenHands[hand] == hands[i]) {
				return i;
			}
		}
		
		// Should be unreachable.
		return -1;
	}
	
	protected boolean isHand0Player() {
		return (findScreenHand(0) == 0);
	}
	
	protected void resizeHands() {
		int cardHeight = Card.getHeight(0);
		Rectangle size = getBounds();

		screenHands[0].onResize(0, size.height - cardHeight, size.width, cardHeight);
		screenHands[1].onResize(0, cardHeight + cardHeight/8, cardHeight, size.height - cardHeight * 2 - cardHeight / 4);
		screenHands[2].onResize(cardHeight, 0, size.width - cardHeight * 2, cardHeight);
		screenHands[3].onResize(size.width - cardHeight, cardHeight + cardHeight/8, cardHeight, size.height - cardHeight * 2 - cardHeight / 4);
	}
	
	protected void rotateScreenHands(int direction) {
		Hand temp1;
		Hand temp2;
		boolean[] playableBefore = {screenHands[0].getPlayable(), screenHands[players - 1].getPlayable()};
		boolean[] upBefore = {screenHands[0].getUp(), screenHands[players - 1].getUp()};

		if (direction == clockwise) {
			int orientation = 90 * (players - 1);
			temp1 = screenHands[0];
			for (int i = players - 1; i >= 0; i--) {
				temp2 = screenHands[i];
				screenHands[i] = temp1;
				screenHands[i].setOrientation(orientation);
				orientation -= 90;
				temp1 = temp2;
			}
		} else {
			int orientation = 0;
			temp1 = screenHands[players - 1];
			for (int i = 0; i < players; i++) {
				temp2 = screenHands[i];
				screenHands[i] = temp1;
				screenHands[i].setOrientation(orientation);
				orientation += 90;
				temp1 = temp2;
			}
		}

		screenHands[0].setPlayable(playableBefore[0]);
		screenHands[0].setUp(upBefore[0]);

		screenHands[players - 1].setPlayable(playableBefore[1]);
		screenHands[players - 1].setUp(upBefore[1]);
	}
	
	int getPlayers() {
		return players;
	}
	
	void setPlayers(int players) {
		if (players > 0 && players <= 4) {
			this.players = players;
		}
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
	
	public void actionPerformed(ActionEvent e) {
		gameOver();
	}
	
	boolean handContains(int hand, int cardType) {
		return hands[hand].contains(cardType);
	}
	
	void setTypePlayable(int type, boolean playable) {
		hands[0].setTypePlayable(type, playable);
	}

	/* These functions here are intended to be intercepted so their work can be captured
	 * and sent over the network. */
	Card drawToHand(int hand) {
		return drawToHand(hand, deck.random());
	}

	Card drawToHand(int hand, int type) {
		return new Card(type, hands[hand]);
	}
	
	void removeFromHand(int hand, Card card) {
		hands[hand].remove(card);
		remove(card);
	}
	
	void discard(Card card) {
		card.transfer(discardHand);
	}
	
	void reverse() {
		doReverse();
	}
	
	void rotateHands() {
		rotate();
		rotateScreenHands(turnOrder);
		resizeHands();
	}
	
	void checkUno() {
		if (hands[0].numCards() == 2 && canBePlayed()) {
			unoButton.setVisible(true);
		}
	}
	
	void swapHands(int hand1, int hand2) {
		boolean[] playableBefore = {hands[hand1].getPlayable(), hands[hand2].getPlayable()};
		boolean[] upBefore = {hands[hand1].getUp(), hands[hand2].getUp()};
		int[] orientationBefore = {hands[hand1].getOrientation(), hands[hand2].getOrientation()};
		int screenHand1 = findScreenHand(hand1);
		int screenHand2 = findScreenHand(hand2);
		Hand temp = hands[hand1];
		
		hands[hand1] = hands[hand2];
		screenHands[screenHand1] = hands[hand2];
		hands[hand2] = temp;
		screenHands[screenHand2] = temp;
		
		hands[hand1].setPlayable(playableBefore[0]);
		hands[hand1].setUp(upBefore[0]);
		hands[hand1].setOrientation(orientationBefore[0]);
		hands[hand2].setPlayable(playableBefore[1]);
		hands[hand2].setUp(upBefore[1]);
		hands[hand2].setOrientation(orientationBefore[1]);

		onResize();
	}
	
	void onTurn() {
		if (!isInterrupted()) {
			hands[0].setPlayable(false);
			unoButton.setVisible(false);
			drew = false;
			uno = false;
			if (hands[0].numCards() == 0) {
				onWin();
			} else {
				rotate();
				checkUno();
			}
		}
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
		onEnd((isHand0Player()) ? "You Win!" : "You Lose");
	}
	
	void callUno(int player) {
		if (!uno) {
			if (player != 0 && hands[0].numCards() == 2 && canBePlayed()) {
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
		
		resizeHands();
		discardHand.onResize((size.width - cardWidth)/2, (size.height - cardHeight)/2, cardWidth, cardHeight);
		customUISpace.setBounds(0, cardHeight, size.width, size.height - cardHeight * 2);
		draw.setBounds(0, 0, cardHeight, cardHeight);
		unoButton.setBounds(size.width - cardHeight, 0, cardHeight, cardHeight);
		
		backgroundBounds = Util.scaleAndCrop(new Rectangle(1024, 1024), size);
		background = Util.bufferScaledImage(bgSource, backgroundBounds.width, backgroundBounds.height);
		arcBounds = new Rectangle((int)(size.width / 4 - Util.scale(30)), (int)(size.height / 4 - Util.scale(30)), (int)(size.width / 2 + Util.scale(60)), (int)(size.height / 2 + Util.scale(60)));
		repaint();
	}

	public void paintComponent(Graphics g) {
		Rectangle bounds = getBounds();
		
		g.drawImage(background, backgroundBounds.x, backgroundBounds.y, backgroundBounds.width, backgroundBounds.height, this);
		if (arcBounds.intersects(g.getClipBounds())) {
			g.setColor(arcColor);
			
			if (turnOrder == clockwise) {
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
