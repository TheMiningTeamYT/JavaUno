package com.loganius.Uno;

import java.awt.Image;
import java.net.URL;

final class WildDraw4Type extends WildChangeColorType {
	private static final long serialVersionUID = 1L;

	WildDraw4Type(URL front, Image[] back) {
		super(front, back);
		value = Value.DRAW_4;
	}
	
	protected void cardAction(Card parentCard, int color) {
		parentCard.setType(Deck.RED_DRAW4 + color);
		for (int i = 0; i < 4; i++) {
			parentCard.getGame().drawToHand(1);
		}
		parentCard.getGame().onTurn();
	}
}
