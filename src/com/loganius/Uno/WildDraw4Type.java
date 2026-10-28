package com.loganius.Uno;

final class WildDraw4Type extends WildChangeColorType {
	WildDraw4Type(CardSkin skin) {
		super(skin);
		value = Value.DRAW_4;
	}
	
	protected void cardAction(Card parent, int color) {
		parent.setType(Deck.RED_DRAW4 + color);
		parent.getGame().startWildCardChallenge();
	}
}
