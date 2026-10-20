package com.loganius.Uno;

import java.awt.Image;

final class WildDraw4Type extends WildChangeColorType {
	WildDraw4Type(Image front, CachedImage[] back) {
		super(front, back);
		value = Value.DRAW_4;
	}
	
	protected void cardAction(Card parent, int color) {
		parent.setType(Deck.RED_DRAW4 + color);
		parent.getGame().startWildCardChallenge();
	}
}
