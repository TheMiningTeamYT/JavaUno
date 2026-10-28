package com.loganius.Uno;

import java.io.IOException;
import java.awt.Image;

abstract class ResourcePack {
	protected static Image defaultIcon = Util.getImage(Util.getResource("Assets/pack.jpg"));

	abstract String getName();
	abstract String getAuthor();
	abstract String getDescription();
	abstract Image getIcon();
	abstract Theme produce(Theme current) throws IOException;
	
	Deck createDeck(Image[] cardSources, Image backSource, Theme fallback) {
		CardType[] deck = fallback.getDeck().getCards();
		CachedImage[] back = null;
		boolean useFaces = cardSources != null;
		boolean useBack = backSource != null;

		if (!useFaces && !useBack) {
			throw new IllegalArgumentException("Both image sources are null!");
		}

		if (useFaces && cardSources.length != Deck.NUM_CARDS) {
			throw new IllegalArgumentException("Length of cards is wrong!");
		}
		
		if (deck.length != Deck.NUM_CARDS && cardSources.length != deck.length) {
			System.err.println("Warning! Deck length is non-standard, but loading standard deck over it!");
		}
		
		if (useBack) {
			back = CardSkin.generateImageList(backSource);
		}
		
		for (int i = 0; i < Deck.NUM_CARDS; i++) {
			if (deck[i].canBeSkinned()) {
				CachedImage[] front = null;
				
				if (useFaces && cardSources[i] != null) {
					front = CardSkin.generateImageList(cardSources[i]);
				}
				
				if (front != null || back != null) {
					deck[i] = new CardType.Skinned(deck[i], front, back);
				}
			}
		}
		
		return new Deck(deck);
	}
}
