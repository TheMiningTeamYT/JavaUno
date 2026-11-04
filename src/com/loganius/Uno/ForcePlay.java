package com.loganius.Uno;

class ForcePlay extends Rule {
	private static final long serialVersionUID = 1L;
	
	int finishDraw(Game game) {
		final Card drawn = game.getLastDrawn();
		if (drawn == null || !game.isLegal(drawn)) {
			return ALLOW;
		}
		game.setHandPlayable(false);
		if (game.checkUno()) {
			drawn.setPlayable(true);
		} else {
			drawn.played();
		}
		return PREVENT_FALLBACK;
	}
	
	static class Factory implements RuleFactory {
		public Rule create() {
			return new ForcePlay();
		}
		
		public String getName() {
			return "Force Play";
		}
	}
}
