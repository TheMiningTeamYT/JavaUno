package com.loganius.Uno;

import java.net.URL;

import com.loganius.Uno.CardType.*;

/* TODO: Add actually different images for the different change color results */
class Deck {
	static final int NUM_CARDS = 62;

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

	protected CardType[] cards;
	protected CardType[] originalDeck;
	protected int[] weightedCards;
	
	Deck(CardType[] cards) {
		originalDeck = cards;
		this.cards = (CardType[]) originalDeck.clone();
		updateWeights();
	}
	
	Deck() {
		this(createDefaultCards());
	}
	
	// ONLY FOR USE BY SERVERS!
	static Deck blankDeck() {
		return new Deck(createBlankCards());
	}
	
	int random() {
		return weightedCards[(int)(Math.random() * weightedCards.length)];
	}
	
	CardType getCard(int type) {
		return cards[type];
	}
	
	CardType[] getCards() {
		return (CardType[]) originalDeck.clone();
	}
	
	int addCard(CardType newCard) {
		CardType[] oldCards = cards;
		cards = new CardType[cards.length + 1];
		System.arraycopy(cards, 0, oldCards, 0, oldCards.length);
		cards[cards.length - 1] = newCard;
		updateWeights();
		
		return cards.length - 1;
	}
	
	void overrideCard(int index, CardType override) {
		if (index >= 0 && index < cards.length) {
			cards[index] = override;
		}
		updateWeights();
	}
	
	void resetCard(int index) {
		if (index >= 0 && index < originalDeck.length) {
			cards[index] = originalDeck[index];
		}
		updateWeights();
	}
	
	void reset() {
		cards = (CardType[]) originalDeck.clone();
		updateWeights();
	}
	
	int getLength() {
		return cards.length;
	}
	
	void updateWeights() {
		int totalWeight = 0;
		int currentWeight = 0;
		for (int i = 0; i < cards.length; i++) {
			totalWeight += cards[i].getWeight();
		}
		
		weightedCards = new int[totalWeight];
		for (int i = 0; i < cards.length; i++) {
			for (int j = 0; j < cards[i].getWeight(); j++) {
				weightedCards[currentWeight++] = i;
			}
		}
	}
	
	static CardType[] createDefaultCards() {
		URL[] fallback = {
			Util.getResource("Assets/UnoCorns/r0.gif"),
			Util.getResource("Assets/UnoCorns/r1.gif"),
			Util.getResource("Assets/UnoCorns/r2.gif"),
			Util.getResource("Assets/UnoCorns/r3.gif"),
			Util.getResource("Assets/UnoCorns/r4.gif"),
			Util.getResource("Assets/UnoCorns/r5.gif"),
			Util.getResource("Assets/UnoCorns/r6.gif"),
			Util.getResource("Assets/UnoCorns/r7.gif"),
			Util.getResource("Assets/UnoCorns/r8.gif"),
			Util.getResource("Assets/UnoCorns/r9.gif"),
			Util.getResource("Assets/UnoCorns/r+2.gif"),
			Util.getResource("Assets/UnoCorns/rs.gif"),
			Util.getResource("Assets/UnoCorns/rr.gif"),
			Util.getResource("Assets/UnoCorns/y0.gif"),
			Util.getResource("Assets/UnoCorns/y1.gif"),
			Util.getResource("Assets/UnoCorns/y2.gif"),
			Util.getResource("Assets/UnoCorns/y3.gif"),
			Util.getResource("Assets/UnoCorns/y4.gif"),
			Util.getResource("Assets/UnoCorns/y5.gif"),
			Util.getResource("Assets/UnoCorns/y6.gif"),
			Util.getResource("Assets/UnoCorns/y7.gif"),
			Util.getResource("Assets/UnoCorns/y8.gif"),
			Util.getResource("Assets/UnoCorns/y9.gif"),
			Util.getResource("Assets/UnoCorns/y+2.gif"),
			Util.getResource("Assets/UnoCorns/ys.gif"),
			Util.getResource("Assets/UnoCorns/yr.gif"),
			Util.getResource("Assets/UnoCorns/g0.gif"),
			Util.getResource("Assets/UnoCorns/g1.gif"),
			Util.getResource("Assets/UnoCorns/g2.gif"),
			Util.getResource("Assets/UnoCorns/g3.gif"),
			Util.getResource("Assets/UnoCorns/g4.gif"),
			Util.getResource("Assets/UnoCorns/g5.gif"),
			Util.getResource("Assets/UnoCorns/g6.gif"),
			Util.getResource("Assets/UnoCorns/g7.gif"),
			Util.getResource("Assets/UnoCorns/g8.gif"),
			Util.getResource("Assets/UnoCorns/g9.gif"),
			Util.getResource("Assets/UnoCorns/g+2.gif"),
			Util.getResource("Assets/UnoCorns/gs.gif"),
			Util.getResource("Assets/UnoCorns/gr.gif"),
			Util.getResource("Assets/UnoCorns/b0.gif"),
			Util.getResource("Assets/UnoCorns/b1.gif"),
			Util.getResource("Assets/UnoCorns/b2.gif"),
			Util.getResource("Assets/UnoCorns/b3.gif"),
			Util.getResource("Assets/UnoCorns/b4.gif"),
			Util.getResource("Assets/UnoCorns/b5.gif"),
			Util.getResource("Assets/UnoCorns/b6.gif"),
			Util.getResource("Assets/UnoCorns/b7.gif"),
			Util.getResource("Assets/UnoCorns/b8.gif"),
			Util.getResource("Assets/UnoCorns/b9.gif"),
			Util.getResource("Assets/UnoCorns/b+2.gif"),
			Util.getResource("Assets/UnoCorns/bs.gif"),
			Util.getResource("Assets/UnoCorns/br.gif"),
			Util.getResource("Assets/UnoCorns/wtc.gif"),
			Util.getResource("Assets/UnoCorns/rtc.gif"),
			Util.getResource("Assets/UnoCorns/ytc.gif"),
			Util.getResource("Assets/UnoCorns/gtc.gif"),
			Util.getResource("Assets/UnoCorns/btc.gif"),
			Util.getResource("Assets/UnoCorns/w+4.gif"),
			Util.getResource("Assets/UnoCorns/r+4.gif"),
			Util.getResource("Assets/UnoCorns/y+4.gif"),
			Util.getResource("Assets/UnoCorns/g+4.gif"),
			Util.getResource("Assets/UnoCorns/b+4.gif")
		};
		URL fallbackBack = Util.getResource("Assets/UnoCorns/0.gif");
		CachedImage[] back = CardSkin.generateImageList(Util.getImage(fallbackBack));
		return new CardType[] {
			new CardType(new CardSkin(Util.getImage(fallback[0]), back), Color.RED, 0, 1),
			new CardType(new CardSkin(Util.getImage(fallback[1]), back), Color.RED, 1, 2),
			new CardType(new CardSkin(Util.getImage(fallback[2]), back), Color.RED, 2, 2),
			new CardType(new CardSkin(Util.getImage(fallback[3]), back), Color.RED, 3, 2),
			new CardType(new CardSkin(Util.getImage(fallback[4]), back), Color.RED, 4, 2),
			new CardType(new CardSkin(Util.getImage(fallback[5]), back), Color.RED, 5, 2),
			new CardType(new CardSkin(Util.getImage(fallback[6]), back), Color.RED, 6, 2),
			new CardType(new CardSkin(Util.getImage(fallback[7]), back), Color.RED, 7, 2),
			new CardType(new CardSkin(Util.getImage(fallback[8]), back), Color.RED, 8, 2),
			new CardType(new CardSkin(Util.getImage(fallback[9]), back), Color.RED, 9, 2),
			new DrawTwo(new CardSkin(Util.getImage(fallback[10]), back), Color.RED),
			new Skip(new CardSkin(Util.getImage(fallback[11]), back), Color.RED),
			new Reverse(new CardSkin(Util.getImage(fallback[12]), back), Color.RED),
			new CardType(new CardSkin(Util.getImage(fallback[13]), back), Color.YELLOW, 0, 1),
			new CardType(new CardSkin(Util.getImage(fallback[14]), back), Color.YELLOW, 1, 2),
			new CardType(new CardSkin(Util.getImage(fallback[15]), back), Color.YELLOW, 2, 2),
			new CardType(new CardSkin(Util.getImage(fallback[16]), back), Color.YELLOW, 3, 2),
			new CardType(new CardSkin(Util.getImage(fallback[17]), back), Color.YELLOW, 4, 2),
			new CardType(new CardSkin(Util.getImage(fallback[18]), back), Color.YELLOW, 5, 2),
			new CardType(new CardSkin(Util.getImage(fallback[19]), back), Color.YELLOW, 6, 2),
			new CardType(new CardSkin(Util.getImage(fallback[20]), back), Color.YELLOW, 7, 2),
			new CardType(new CardSkin(Util.getImage(fallback[21]), back), Color.YELLOW, 8, 2),
			new CardType(new CardSkin(Util.getImage(fallback[22]), back), Color.YELLOW, 9, 2),
			new DrawTwo(new CardSkin(Util.getImage(fallback[23]), back), Color.YELLOW),
			new Skip(new CardSkin(Util.getImage(fallback[24]), back), Color.YELLOW),
			new Reverse(new CardSkin(Util.getImage(fallback[25]), back), Color.YELLOW),
			new CardType(new CardSkin(Util.getImage(fallback[26]), back), Color.GREEN, 0, 1),
			new CardType(new CardSkin(Util.getImage(fallback[27]), back), Color.GREEN, 1, 2),
			new CardType(new CardSkin(Util.getImage(fallback[28]), back), Color.GREEN, 2, 2),
			new CardType(new CardSkin(Util.getImage(fallback[29]), back), Color.GREEN, 3, 2),
			new CardType(new CardSkin(Util.getImage(fallback[30]), back), Color.GREEN, 4, 2),
			new CardType(new CardSkin(Util.getImage(fallback[31]), back), Color.GREEN, 5, 2),
			new CardType(new CardSkin(Util.getImage(fallback[32]), back), Color.GREEN, 6, 2),
			new CardType(new CardSkin(Util.getImage(fallback[33]), back), Color.GREEN, 7, 2),
			new CardType(new CardSkin(Util.getImage(fallback[34]), back), Color.GREEN, 8, 2),
			new CardType(new CardSkin(Util.getImage(fallback[35]), back), Color.GREEN, 9, 2),
			new DrawTwo(new CardSkin(Util.getImage(fallback[36]), back), Color.GREEN),
			new Skip(new CardSkin(Util.getImage(fallback[37]), back), Color.GREEN),
			new Reverse(new CardSkin(Util.getImage(fallback[38]), back), Color.GREEN),
			new CardType(new CardSkin(Util.getImage(fallback[39]), back), Color.BLUE, 0, 1),
			new CardType(new CardSkin(Util.getImage(fallback[40]), back), Color.BLUE, 1, 2),
			new CardType(new CardSkin(Util.getImage(fallback[41]), back), Color.BLUE, 2, 2),
			new CardType(new CardSkin(Util.getImage(fallback[42]), back), Color.BLUE, 3, 2),
			new CardType(new CardSkin(Util.getImage(fallback[43]), back), Color.BLUE, 4, 2),
			new CardType(new CardSkin(Util.getImage(fallback[44]), back), Color.BLUE, 5, 2),
			new CardType(new CardSkin(Util.getImage(fallback[45]), back), Color.BLUE, 6, 2),
			new CardType(new CardSkin(Util.getImage(fallback[46]), back), Color.BLUE, 7, 2),
			new CardType(new CardSkin(Util.getImage(fallback[47]), back), Color.BLUE, 8, 2),
			new CardType(new CardSkin(Util.getImage(fallback[48]), back), Color.BLUE, 9, 2),
			new DrawTwo(new CardSkin(Util.getImage(fallback[49]), back), Color.BLUE),
			new Skip(new CardSkin(Util.getImage(fallback[50]), back), Color.BLUE),
			new Reverse(new CardSkin(Util.getImage(fallback[51]), back), Color.BLUE),
			new WildChangeColorType(new CardSkin(Util.getImage(fallback[52]), back)),
			new CardType(new CardSkin(Util.getImage(fallback[53]), back), Color.RED, -1, 0),
			new CardType(new CardSkin(Util.getImage(fallback[54]), back), Color.YELLOW, -1, 0),
			new CardType(new CardSkin(Util.getImage(fallback[55]), back), Color.GREEN, -1, 0),
			new CardType(new CardSkin(Util.getImage(fallback[56]), back), Color.BLUE, -1, 0),
			new WildDraw4Type(new CardSkin(Util.getImage(fallback[57]), back)),
			new CardType(new CardSkin(Util.getImage(fallback[58]), back), Color.RED, -1, 0),
			new CardType(new CardSkin(Util.getImage(fallback[59]), back), Color.YELLOW, -1, 0),
			new CardType(new CardSkin(Util.getImage(fallback[60]), back), Color.GREEN, -1, 0),
			new CardType(new CardSkin(Util.getImage(fallback[61]), back), Color.BLUE, -1, 0),
		};
	}
	
	static CardType[] createBlankCards() {
		return new CardType[] {
			new CardType(null, Color.RED, 0, 1),
			new CardType(null, Color.RED, 1, 2),
			new CardType(null, Color.RED, 2, 2),
			new CardType(null, Color.RED, 3, 2),
			new CardType(null, Color.RED, 4, 2),
			new CardType(null, Color.RED, 5, 2),
			new CardType(null, Color.RED, 6, 2),
			new CardType(null, Color.RED, 7, 2),
			new CardType(null, Color.RED, 8, 2),
			new CardType(null, Color.RED, 9, 2),
			new DrawTwo(null, Color.RED),
			new Skip(null, Color.RED),
			new Reverse(null, Color.RED),
			new CardType(null, Color.YELLOW, 0, 1),
			new CardType(null, Color.YELLOW, 1, 2),
			new CardType(null, Color.YELLOW, 2, 2),
			new CardType(null, Color.YELLOW, 3, 2),
			new CardType(null, Color.YELLOW, 4, 2),
			new CardType(null, Color.YELLOW, 5, 2),
			new CardType(null, Color.YELLOW, 6, 2),
			new CardType(null, Color.YELLOW, 7, 2),
			new CardType(null, Color.YELLOW, 8, 2),
			new CardType(null, Color.YELLOW, 9, 2),
			new DrawTwo(null, Color.YELLOW),
			new Skip(null, Color.YELLOW),
			new Reverse(null, Color.YELLOW),
			new CardType(null, Color.GREEN, 0, 1),
			new CardType(null, Color.GREEN, 1, 2),
			new CardType(null, Color.GREEN, 2, 2),
			new CardType(null, Color.GREEN, 3, 2),
			new CardType(null, Color.GREEN, 4, 2),
			new CardType(null, Color.GREEN, 5, 2),
			new CardType(null, Color.GREEN, 6, 2),
			new CardType(null, Color.GREEN, 7, 2),
			new CardType(null, Color.GREEN, 8, 2),
			new CardType(null, Color.GREEN, 9, 2),
			new DrawTwo(null, Color.GREEN),
			new Skip(null, Color.GREEN),
			new Reverse(null, Color.GREEN),
			new CardType(null, Color.BLUE, 0, 1),
			new CardType(null, Color.BLUE, 1, 2),
			new CardType(null, Color.BLUE, 2, 2),
			new CardType(null, Color.BLUE, 3, 2),
			new CardType(null, Color.BLUE, 4, 2),
			new CardType(null, Color.BLUE, 5, 2),
			new CardType(null, Color.BLUE, 6, 2),
			new CardType(null, Color.BLUE, 7, 2),
			new CardType(null, Color.BLUE, 8, 2),
			new CardType(null, Color.BLUE, 9, 2),
			new DrawTwo(null, Color.BLUE),
			new Skip(null, Color.BLUE),
			new Reverse(null, Color.BLUE),
			new WildChangeColorType((CardSkin)null),
			new CardType(null, Color.RED, -1, 0),
			new CardType(null, Color.YELLOW, -1, 0),
			new CardType(null, Color.GREEN, -1, 0),
			new CardType(null, Color.BLUE, -1, 0),
			new WildDraw4Type(null),
			new CardType(null, Color.RED, -1, 0),
			new CardType(null, Color.YELLOW, -1, 0),
			new CardType(null, Color.GREEN, -1, 0),
			new CardType(null, Color.BLUE, -1, 0),
		};
	}
}
