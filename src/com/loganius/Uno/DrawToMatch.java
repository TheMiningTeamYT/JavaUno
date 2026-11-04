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
		game.checkUno();
		return PREVENT_FALLBACK;
	}
	
	int finishDraw(Game game) {
		if (!game.isLegal(game.getLastDrawn())) {
			System.err.println("CHEATER!!!");
		}
		game.getLastDrawn().setPlayable(true);
		return ALLOW;
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
