package com.loganius.Uno;

import java.awt.Image;
import java.net.URL;

final class WildDraw4Type extends WildChangeColorType {
	WildDraw4Type(URL front, CardFace[] back) {
		super(front, back);
		value = Value.DRAW_4;
	}
	
	protected void cardAction(Card parent, int color) {
		parent.setType(Deck.RED_DRAW4 + color);
		for (int i = 0; i < 4; i++) {
			parent.getGame().drawToHand(1);
		}
		parent.getGame().onTurn();
		parent.getGame().onTurn();
	}
}
