package com.loganius.Uno;

import java.awt.Image;
import com.loganius.Uno.CardType.*;

/* TODO: Add actually different images for the different change color results */
abstract class Deck {
	protected CardType[] cards;
	protected CardType[] originalDeck;
	static final int RED_0 = 0;
	static final int RED_1 = 1;
	static final int RED_2 = 2;
	static final int RED_3 = 3;
	static final int RED_4 = 4;
	static final int RED_5 = 5;
	static final int RED_6 = 6;
	static final int RED_7 = 7;
	static final int RED_8 = 8;
	static final int RED_9 = 9;
	static final int RED_PLUS2 = 10;
	static final int RED_SKIP = 11;
	static final int RED_REVERSE = 12;
	static final int YELLOW_0 = 13;
	static final int YELLOW_1 = 14;
	static final int YELLOW_2 = 15;
	static final int YELLOW_3 = 16;
	static final int YELLOW_4 = 17;
	static final int YELLOW_5 = 18;
	static final int YELLOW_6 = 19;
	static final int YELLOW_7 = 20;
	static final int YELLOW_8 = 21;
	static final int YELLOW_9 = 22;
	static final int YELLOW_PLUS2 = 23;
	static final int YELLOW_SKIP = 24;
	static final int YELLOW_REVERSE = 25;
	static final int GREEN_0 = 26;
	static final int GREEN_1 = 27;
	static final int GREEN_2 = 28;
	static final int GREEN_3 = 29;
	static final int GREEN_4 = 30;
	static final int GREEN_5 = 31;
	static final int GREEN_6 = 32;
	static final int GREEN_7 = 33;
	static final int GREEN_8 = 34;
	static final int GREEN_9 = 35;
	static final int GREEN_PLUS2 = 36;
	static final int GREEN_SKIP = 37;
	static final int GREEN_REVERSE = 38;
	static final int BLUE_0 = 39;
	static final int BLUE_1 = 40;
	static final int BLUE_2 = 41;
	static final int BLUE_3 = 42;
	static final int BLUE_4 = 43;
	static final int BLUE_5 = 44;
	static final int BLUE_6 = 45;
	static final int BLUE_7 = 46;
	static final int BLUE_8 = 47;
	static final int BLUE_9 = 48;
	static final int BLUE_PLUS2 = 49;
	static final int BLUE_SKIP = 50;
	static final int BLUE_REVERSE = 51;
	static final int WILD_CHANGE_COLOR = 52;
	static final int RED_CHANGE_COLOR = 53;
	static final int YELLOW_CHANGE_COLOR = 54;
	static final int GREEN_CHANGE_COLOR = 55;
	static final int BLUE_CHANGE_COLOR = 56;
	static final int WILD_DRAW4 = 57;
	static final int RED_DRAW4 = 58;
	static final int YELLOW_DRAW4 = 59;
	static final int GREEN_DRAW4 = 60;
	static final int BLUE_DRAW4 = 61;
	
	int random() {
		int card;
		do {
			card = (int)(Math.random() * cards.length);
		} while (!getCard(card).drawable);
		return card;
	}
	
	CardType getCard(int type) {
		return cards[type];
	}
	
	void addCard(CardType newCard) {
		CardType[] oldCards = cards;
		cards = new CardType[cards.length + 1];
		System.arraycopy(cards, 0, oldCards, 0, oldCards.length);
		cards[cards.length - 1] = newCard;
	}
	
	void overrideCard(int index, CardType override) {
		if (index >= 0 && index < cards.length) {
			cards[index] = override;
		}
	}
	
	void resetCard(int index) {
		if (index >= 0 && index < originalDeck.length) {
			cards[index] = originalDeck[index];
		}
	}
	
	void reset() {
		cards = (CardType[]) originalDeck.clone();
	}
	
	int getLength() {
		return cards.length;
	}

	static class UnoCorns extends Deck {
		UnoCorns() {
			originalDeck = deck;
			cards = (CardType[])deck.clone();
		}

		private final CardFace[] back = CardType.generateImageList(Util.getImage(Util.getResource("Assets/UnoCorns/0.gif")));
		
		private final CardType[] deck = {
			new CardType(Util.getResource("Assets/UnoCorns/r0.gif"), back, Color.RED, 0, true),
			new CardType(Util.getResource("Assets/UnoCorns/r1.gif"), back, Color.RED, 1, true),
			new CardType(Util.getResource("Assets/UnoCorns/r2.gif"), back, Color.RED, 2, true),
			new CardType(Util.getResource("Assets/UnoCorns/r3.gif"), back, Color.RED, 3, true),
			new CardType(Util.getResource("Assets/UnoCorns/r4.gif"), back, Color.RED, 4, true),
			new CardType(Util.getResource("Assets/UnoCorns/r5.gif"), back, Color.RED, 5, true),
			new CardType(Util.getResource("Assets/UnoCorns/r6.gif"), back, Color.RED, 6, true),
			new CardType(Util.getResource("Assets/UnoCorns/r7.gif"), back, Color.RED, 7, true),
			new CardType(Util.getResource("Assets/UnoCorns/r8.gif"), back, Color.RED, 8, true),
			new CardType(Util.getResource("Assets/UnoCorns/r9.gif"), back, Color.RED, 9, true),
			new DrawTwoCardType(Util.getResource("Assets/UnoCorns/r+2.gif"), back, Color.RED),
			new SkipCardType(Util.getResource("Assets/UnoCorns/rs.gif"), back, Color.RED),
			new ReverseCardType(Util.getResource("Assets/UnoCorns/rr.gif"), back, Color.RED),
			new CardType(Util.getResource("Assets/UnoCorns/y0.gif"), back, Color.YELLOW, 0, true),
			new CardType(Util.getResource("Assets/UnoCorns/y1.gif"), back, Color.YELLOW, 1, true),
			new CardType(Util.getResource("Assets/UnoCorns/y2.gif"), back, Color.YELLOW, 2, true),
			new CardType(Util.getResource("Assets/UnoCorns/y3.gif"), back, Color.YELLOW, 3, true),
			new CardType(Util.getResource("Assets/UnoCorns/y4.gif"), back, Color.YELLOW, 4, true),
			new CardType(Util.getResource("Assets/UnoCorns/y5.gif"), back, Color.YELLOW, 5, true),
			new CardType(Util.getResource("Assets/UnoCorns/y6.gif"), back, Color.YELLOW, 6, true),
			new CardType(Util.getResource("Assets/UnoCorns/y7.gif"), back, Color.YELLOW, 7, true),
			new CardType(Util.getResource("Assets/UnoCorns/y8.gif"), back, Color.YELLOW, 8, true),
			new CardType(Util.getResource("Assets/UnoCorns/y9.gif"), back, Color.YELLOW, 9, true),
			new DrawTwoCardType(Util.getResource("Assets/UnoCorns/y+2.gif"), back, Color.YELLOW),
			new SkipCardType(Util.getResource("Assets/UnoCorns/ys.gif"), back, Color.YELLOW),
			new ReverseCardType(Util.getResource("Assets/UnoCorns/yr.gif"), back, Color.YELLOW),
			new CardType(Util.getResource("Assets/UnoCorns/g0.gif"), back, Color.GREEN, 0, true),
			new CardType(Util.getResource("Assets/UnoCorns/g1.gif"), back, Color.GREEN, 1, true),
			new CardType(Util.getResource("Assets/UnoCorns/g2.gif"), back, Color.GREEN, 2, true),
			new CardType(Util.getResource("Assets/UnoCorns/g3.gif"), back, Color.GREEN, 3, true),
			new CardType(Util.getResource("Assets/UnoCorns/g4.gif"), back, Color.GREEN, 4, true),
			new CardType(Util.getResource("Assets/UnoCorns/g5.gif"), back, Color.GREEN, 5, true),
			new CardType(Util.getResource("Assets/UnoCorns/g6.gif"), back, Color.GREEN, 6, true),
			new CardType(Util.getResource("Assets/UnoCorns/g7.gif"), back, Color.GREEN, 7, true),
			new CardType(Util.getResource("Assets/UnoCorns/g8.gif"), back, Color.GREEN, 8, true),
			new CardType(Util.getResource("Assets/UnoCorns/g9.gif"), back, Color.GREEN, 9, true),
			new DrawTwoCardType(Util.getResource("Assets/UnoCorns/g+2.gif"), back, Color.GREEN),
			new SkipCardType(Util.getResource("Assets/UnoCorns/gs.gif"), back, Color.GREEN),
			new ReverseCardType(Util.getResource("Assets/UnoCorns/gr.gif"), back, Color.GREEN),
			new CardType(Util.getResource("Assets/UnoCorns/b0.gif"), back, Color.BLUE, 0, true),
			new CardType(Util.getResource("Assets/UnoCorns/b1.gif"), back, Color.BLUE, 1, true),
			new CardType(Util.getResource("Assets/UnoCorns/b2.gif"), back, Color.BLUE, 2, true),
			new CardType(Util.getResource("Assets/UnoCorns/b3.gif"), back, Color.BLUE, 3, true),
			new CardType(Util.getResource("Assets/UnoCorns/b4.gif"), back, Color.BLUE, 4, true),
			new CardType(Util.getResource("Assets/UnoCorns/b5.gif"), back, Color.BLUE, 5, true),
			new CardType(Util.getResource("Assets/UnoCorns/b6.gif"), back, Color.BLUE, 6, true),
			new CardType(Util.getResource("Assets/UnoCorns/b7.gif"), back, Color.BLUE, 7, true),
			new CardType(Util.getResource("Assets/UnoCorns/b8.gif"), back, Color.BLUE, 8, true),
			new CardType(Util.getResource("Assets/UnoCorns/b9.gif"), back, Color.BLUE, 9, true),
			new DrawTwoCardType(Util.getResource("Assets/UnoCorns/b+2.gif"), back, Color.BLUE),
			new SkipCardType(Util.getResource("Assets/UnoCorns/bs.gif"), back, Color.BLUE),
			new ReverseCardType(Util.getResource("Assets/UnoCorns/br.gif"), back, Color.BLUE),
			new WildChangeColorType(Util.getResource("Assets/UnoCorns/wtc.gif"), back),
			new CardType(Util.getResource("Assets/UnoCorns/rtc.gif"), back, Color.RED, -1, false),
			new CardType(Util.getResource("Assets/UnoCorns/ytc.gif"), back, Color.YELLOW, -1, false),
			new CardType(Util.getResource("Assets/UnoCorns/gtc.gif"), back, Color.GREEN, -1, false),
			new CardType(Util.getResource("Assets/UnoCorns/btc.gif"), back, Color.BLUE, -1, false),
			new WildDraw4Type(Util.getResource("Assets/UnoCorns/w+4.gif"), back),
			new CardType(Util.getResource("Assets/UnoCorns/r+4.gif"), back, Color.RED, -1, false),
			new CardType(Util.getResource("Assets/UnoCorns/y+4.gif"), back, Color.YELLOW, -1, false),
			new CardType(Util.getResource("Assets/UnoCorns/g+4.gif"), back, Color.GREEN, -1, false),
			new CardType(Util.getResource("Assets/UnoCorns/b+4.gif"), back, Color.BLUE, -1, false),
		};
	}
}
