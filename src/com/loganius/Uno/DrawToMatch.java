package com.loganius.Uno;

/**
 * TODO: Figure out a way to slow this down so the player can see what's happening
 * without breaking everything.
 */
class DrawToMatch extends Rule {
	private static final long serialVersionUID = 1L;

	int onDraw(Game game, RuleSet.Composite ruleset) {
		do {
			ruleset.drawToHand(0, game);
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (!game.isLegal(ruleset.getLastDrawn()));
		return PREVENT_FALLBACK;
	}
}
