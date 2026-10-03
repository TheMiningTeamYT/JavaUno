package com.loganius.Uno;

import javax.swing.*;

class ForcePlay extends Rule {
	private static final long serialVersionUID = 1L;

	int onDraw(Game game, RuleSet.Composite ruleset) {
		final Card drawn = ruleset.getLastDrawn();
		if (drawn == null || !game.isLegal(drawn)) {
			return ALLOW;
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				drawn.played();
			}
		});
		return STOP;
	}
}
