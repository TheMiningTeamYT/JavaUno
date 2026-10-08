package com.loganius.Uno;

class DrawToMatch extends Rule {
	private static final long serialVersionUID = 1L;

	int onDraw(Game game) {
		Card card;
		game.setHandPlayable(false);
		do {
			drawToHand(0, game);
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (!game.isLegal((card = game.getLastDrawn())));
		card.setPlayable(true);
		game.checkUno();
		return PREVENT_FALLBACK;
	}
	
	static class Factory implements RuleFactory {
		public Rule create() {
			return new DrawToMatch();
		}
		
		public String getName() {
			return "Draw to Match";
		}
	}
}
