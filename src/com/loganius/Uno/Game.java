package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;

/**
 * Represents the space in which a game of Uno is played.
 * TODO: Add a background
 * TODO: Add turn direction indicators.
 * TODO: Add indicators for whose turn it is currently.
 * TODO: Add a way for the owner of the game object to know when the game's over.
 */
class Game extends JLayeredPane implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final double arcRatio = 0.13962634;
	private static final Color arcColor = new Color(0, 220, 255);
	static final int clockwise = 0;
	static final int counterClockwise = 1;

	private GameHandler handler;
	private RuleSet rules = new RuleSet.Standard(this);
	protected Hand[] hands = {
			new Hand(this, 0, 360, 640, 120, 0, true, false),
			new Hand(this, 0, 130, 120, 220, 270, false, false),
			new Hand(this, 0, 0, 400, 120, 180, false, false),
			new Hand(this, 520, 130, 120, 220, 90, false, false),
	};
	protected DiscardHand discardHand = new DiscardHand(this, 0, 360, 640, 120);
	protected Hand[] screenOrderedHands;
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
	private Image background;
	private Rectangle backgroundBounds;
	private Rectangle arcBounds = new Rectangle((int)(640 / 4 - 30), (int)(480 / 4 - 30), (int)(640 / 2 + 60), (int)(480 / 2 + 60));
	private int[] arcX = new int[43];
	private int[] arcY = new int[43];
	private int frame = 0;
	
	Game(Deck deck, GameHandler handler) {
		super();
		this.deck = deck;
		this.handler = handler;
		screenOrderedHands = (Hand[])hands.clone();

		customUISpace.setVisible(false);
		customUISpace.setBounds(0, 120, 640, 240);
		customUISpace.setOpaque(false);

		setLayout(null);
		setOpaque(false);
		add(customUISpace, MODAL_LAYER);
		addComponentListener(new ResizeListener());
		setBounds(0, 0, 640, 480);
		
		new Timer(33, new TimerListener()).start();
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
		Rectangle bounds = getBounds();
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
	
	protected boolean isHand0Player() {
		return (hands[0] == screenOrderedHands[0]);
	}
	
	protected void resizeHands() {
		int cardHeight = Card.getHeight(0);
		Rectangle size = getBounds();

		screenOrderedHands[0].onResize(0, size.height - cardHeight, size.width, cardHeight);
		screenOrderedHands[1].onResize(0, cardHeight + cardHeight/8, cardHeight, size.height - cardHeight * 2 - cardHeight / 4);
		screenOrderedHands[2].onResize(cardHeight, 0, size.width - cardHeight * 2, cardHeight);
		screenOrderedHands[3].onResize(size.width - cardHeight, cardHeight + cardHeight/8, cardHeight, size.height - cardHeight * 2 - cardHeight / 4);
	}
	
	protected void rotateScreenHands(int direction) {
		Hand temp1;
		Hand temp2;
		boolean playableBefore0 = hands[0].getPlayable();
		boolean upBefore0 = hands[0].getUp();
		boolean playableBefore3 = hands[3].getPlayable();
		boolean upBefore3 = hands[3].getUp();

		if (direction == clockwise) {
			int orientation = 90 * (players - 1);
			temp1 = screenOrderedHands[0];
			for (int i = players - 1; i >= 0; i--) {
				temp2 = screenOrderedHands[i];
				screenOrderedHands[i] = temp1;
				screenOrderedHands[i].setOrientation(orientation);
				orientation -= 90;
				temp1 = temp2;
			}
		} else {
			int orientation = 0;
			temp1 = screenOrderedHands[players - 1];
			for (int i = 0; i < players; i++) {
				temp2 = screenOrderedHands[i];
				screenOrderedHands[i] = temp1;
				screenOrderedHands[i].setOrientation(orientation);
				orientation += 90;
				temp1 = temp2;
			}
		}

		hands[0].setPlayable(playableBefore0);
		hands[0].setUp(upBefore0);

		hands[players - 1].setPlayable(playableBefore3);
		hands[players - 1].setUp(upBefore3);
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
	
	/* These functions here are intended to be intercepted so their work can be captured
	 * and sent over the network. */
	void drawToHand(int hand) {
		drawToHand(hand, deck.random());
	}

	void drawToHand(int hand, int type) {
		new Card(type, hands[hand]);
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
	
	void onTurn() {
		hands[0].setPlayable(false);
		unoButton.setVisible(false);
		drew = false;
		uno = false;
		if (hands[0].numCards() == 0) {
			onWin();
		} else {
			rotate();
			if (hands[0].numCards() == 1) {
				unoButton.setVisible(true);
			}
		}
	}
	
	void onEnd(String msg) {
		JButton endButton = new JButton("Exit");
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
		label.addComponentListener(Util.getTextResizeListener());
		
		endButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		endButton.setFont(Util.getScaledFont());
		endButton.addActionListener(this);
		endButton.addComponentListener(Util.getTextResizeListener());
		
		customUISpace.add(label);
		customUISpace.add(endButton);
		customUISpace.setVisible(true);
		ended = true;
	}
	
	void onWin() {
		onEnd((isHand0Player()) ? "You Win!" : "You Lose");
	}
	
	void callUno(int player) {
		if (!uno) {
			if (player != 0 && hands[0].numCards() == 1) {
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
		
		backgroundBounds = scaleAndCrop(new Rectangle(0, 0, 1024, 1024));
		background = Util.bufferScaledImage(bgSource, backgroundBounds.width, backgroundBounds.height);
		arcBounds = new Rectangle((int)(size.width / 4 - Util.scale(30)), (int)(size.height / 4 - Util.scale(30)), (int)(size.width / 2 + Util.scale(60)), (int)(size.height / 2 + Util.scale(60)));

		validate();
	}
	
	private Rectangle scaleAndCrop(Rectangle original) {
		Rectangle bounds = getBounds();
		double widthRatio = (double)bounds.width / original.width;
		double heightRatio = (double)bounds.height / original.height;
		if (widthRatio > heightRatio) {
			original.width *= widthRatio;
			original.height *= widthRatio;
			original.y = (bounds.height - original.height) / 2;
		} else {
			original.width *= heightRatio;
			original.height *= heightRatio;
			original.x = (bounds.width - original.width) / 2;
		}
		return original;
	}
	
	public void paintComponent(Graphics g) {
		Rectangle bounds = getBounds();
		g.drawImage(background, backgroundBounds.x, backgroundBounds.y, backgroundBounds.width, backgroundBounds.height, this);
		if (arcBounds.intersects(g.getClipBounds())) {
			g.setColor(arcColor);
			
			if (turnOrder == clockwise) {
				for (int i = 0; i < 20; i++) {
					arcX[i] = (int)(Math.cos((i + frame)*arcRatio)*(bounds.height / 4)) + bounds.width / 2;
					arcY[i] = (int)(Math.sin((i + frame)*arcRatio)*(bounds.height / 4)) + bounds.height / 2;
					arcX[42 - i] = (int)(Math.cos((i + frame)*arcRatio)*((bounds.height / 4) - Util.scale(20))) + bounds.width / 2;
					arcY[42 - i] = (int)(Math.sin((i + frame)*arcRatio)*((bounds.height / 4) - Util.scale(20))) + bounds.height / 2;
				}
				
				// Draw a triangle.
				arcX[20] = arcX[19] - (int)(Math.cos((19 + frame)*arcRatio)*Util.scale(40));
				arcY[20] = arcY[19] - (int)(Math.sin((19 + frame)*arcRatio)*Util.scale(40));
				arcX[21] = arcX[19] + (int)((Math.cos((19 + frame)*arcRatio)*Util.scale(-10)) - (Math.sin((19 + frame)*arcRatio)*Util.scale(51.962)));
				arcY[21] = arcY[19] + (int)((Math.sin((19 + frame)*arcRatio)*Util.scale(-10)) + (Math.cos((19 + frame)*arcRatio)*Util.scale(51.962)));
				arcX[22] = arcX[19] + (int)(Math.cos((19 + frame)*arcRatio)*Util.scale(20));
				arcY[22] = arcY[19] + (int)(Math.sin((19 + frame)*arcRatio)*Util.scale(20));
			} else {
				for (int i = 0; i < 20; i++) {
					arcX[i] = (int)(Math.cos((i - frame)*arcRatio)*(bounds.height / 4)) + bounds.width / 2;
					arcY[i] = (int)(Math.sin((i - frame)*arcRatio)*(bounds.height / 4)) + bounds.height / 2;
					arcX[39 - i] = (int)(Math.cos((i - frame)*arcRatio)*((bounds.height / 4) - Util.scale(20))) + bounds.width / 2;
					arcY[39 - i] = (int)(Math.sin((i - frame)*arcRatio)*((bounds.height / 4) - Util.scale(20))) + bounds.height / 2;
				}
				
				// Draw a triangle.
				arcX[40] = arcX[0] - (int)(Math.cos((frame)*arcRatio)*Util.scale(40));
				arcY[40] = arcY[0] + (int)(Math.sin((frame)*arcRatio)*Util.scale(40));
				arcX[41] = arcX[0] + (int)((Math.cos((frame)*arcRatio)*Util.scale(-10)) - (Math.sin((frame)*arcRatio)*Util.scale(51.962)));
				arcY[41] = arcY[0] - (int)((Math.sin((frame)*arcRatio)*Util.scale(-10)) + (Math.cos((frame)*arcRatio)*Util.scale(51.962)));
				arcX[42] = arcX[0] + (int)(Math.cos((frame)*arcRatio)*Util.scale(20));
				arcY[42] = arcY[0] - (int)(Math.sin((frame)*arcRatio)*Util.scale(20));
			}
	
			g.fillPolygon(arcX, arcY, 43);
		}
		super.paintComponent(g);
	}
	
	private class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (!isInterrupted()) {
				frame = (frame + 1) % 360;
				repaint(arcBounds);
			}
		}
	}
	private class ResizeListener extends ComponentAdapter {
		public void componentResized(ComponentEvent e) {
			onResize();
		}
	}
}
