package com.loganius.Uno;

import java.awt.Image;
import java.net.URL;

import com.loganius.Uno.CardType.*;

/* TODO: Add actually different images for the different change color results */
class Deck {
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
	
	private static final URL[] fallback = {
		Util.getResource("Assets/UnoCorns/0.gif"),
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

	protected CardType[] cards;
	protected CardType[] originalDeck;
	protected int[] weightedCards;
	
	Deck(CardType[] cards) {
		originalDeck = cards;
		this.cards = (CardType[]) originalDeck.clone();
		updateWeights();
	}
	
	Deck(Image[] cards) {
		this(createCompositeCards(cards));
	}
	
	Deck() {
		this(createDefaultCards());
	}
	
	int random() {
		return weightedCards[(int)(Math.random() * weightedCards.length)];
	}
	
	CardType getCard(int type) {
		return cards[type];
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
		return createCompositeCards(null);
	}
	
	static CardType[] createCompositeCards(Image[] overrideCards) {
		Image[] cards = new Image[fallback.length];
		
		for (int i = 0; i < cards.length; i++) {
			try {
				cards[i] = overrideCards[i];
			} catch (IndexOutOfBoundsException e) {
				
			} catch (NullPointerException e) {}
			
			if (cards[i] == null) {
				cards[i] = Util.getImage(fallback[i]);
			}
		}
		
		return createCards(cards);
	}
	
	static CardType[] createCards(Image[] cards) {
		CachedImage[] back = CardType.generateImageList(cards[0]);
		
		return new CardType[] {
			new CardType(cards[1], back, Color.RED, 0, 1),
			new CardType(cards[2], back, Color.RED, 1, 2),
			new CardType(cards[3], back, Color.RED, 2, 2),
			new CardType(cards[4], back, Color.RED, 3, 2),
			new CardType(cards[5], back, Color.RED, 4, 2),
			new CardType(cards[6], back, Color.RED, 5, 2),
			new CardType(cards[7], back, Color.RED, 6, 2),
			new CardType(cards[8], back, Color.RED, 7, 2),
			new CardType(cards[9], back, Color.RED, 8, 2),
			new CardType(cards[10], back, Color.RED, 9, 2),
			new DrawTwoCardType(cards[11], back, Color.RED),
			new SkipCardType(cards[12], back, Color.RED),
			new ReverseCardType(cards[13], back, Color.RED),
			new CardType(cards[14], back, Color.YELLOW, 0, 1),
			new CardType(cards[15], back, Color.YELLOW, 1, 2),
			new CardType(cards[16], back, Color.YELLOW, 2, 2),
			new CardType(cards[17], back, Color.YELLOW, 3, 2),
			new CardType(cards[18], back, Color.YELLOW, 4, 2),
			new CardType(cards[19], back, Color.YELLOW, 5, 2),
			new CardType(cards[20], back, Color.YELLOW, 6, 2),
			new CardType(cards[21], back, Color.YELLOW, 7, 2),
			new CardType(cards[22], back, Color.YELLOW, 8, 2),
			new CardType(cards[23], back, Color.YELLOW, 9, 2),
			new DrawTwoCardType(cards[24], back, Color.YELLOW),
			new SkipCardType(cards[25], back, Color.YELLOW),
			new ReverseCardType(cards[26], back, Color.YELLOW),
			new CardType(cards[27], back, Color.GREEN, 0, 1),
			new CardType(cards[28], back, Color.GREEN, 1, 2),
			new CardType(cards[29], back, Color.GREEN, 2, 2),
			new CardType(cards[30], back, Color.GREEN, 3, 2),
			new CardType(cards[31], back, Color.GREEN, 4, 2),
			new CardType(cards[32], back, Color.GREEN, 5, 2),
			new CardType(cards[33], back, Color.GREEN, 6, 2),
			new CardType(cards[34], back, Color.GREEN, 7, 2),
			new CardType(cards[35], back, Color.GREEN, 8, 2),
			new CardType(cards[36], back, Color.GREEN, 9, 2),
			new DrawTwoCardType(cards[37], back, Color.GREEN),
			new SkipCardType(cards[38], back, Color.GREEN),
			new ReverseCardType(cards[39], back, Color.GREEN),
			new CardType(cards[40], back, Color.BLUE, 0, 1),
			new CardType(cards[41], back, Color.BLUE, 1, 2),
			new CardType(cards[42], back, Color.BLUE, 2, 2),
			new CardType(cards[43], back, Color.BLUE, 3, 2),
			new CardType(cards[44], back, Color.BLUE, 4, 2),
			new CardType(cards[45], back, Color.BLUE, 5, 2),
			new CardType(cards[46], back, Color.BLUE, 6, 2),
			new CardType(cards[47], back, Color.BLUE, 7, 2),
			new CardType(cards[48], back, Color.BLUE, 8, 2),
			new CardType(cards[49], back, Color.BLUE, 9, 2),
			new DrawTwoCardType(cards[50], back, Color.BLUE),
			new SkipCardType(cards[51], back, Color.BLUE),
			new ReverseCardType(cards[52], back, Color.BLUE),
			new WildChangeColorType(cards[53], back),
			new CardType(cards[54], back, Color.RED, -1, 0),
			new CardType(cards[55], back, Color.YELLOW, -1, 0),
			new CardType(cards[56], back, Color.GREEN, -1, 0),
			new CardType(cards[57], back, Color.BLUE, -1, 0),
			new WildDraw4Type(cards[58], back),
			new CardType(cards[59], back, Color.RED, -1, 0),
			new CardType(cards[60], back, Color.YELLOW, -1, 0),
			new CardType(cards[61], back, Color.GREEN, -1, 0),
			new CardType(cards[62], back, Color.BLUE, -1, 0),
		};
	}
}
