package com.loganius.Uno;

import javax.swing.*;

class ForcePlay extends Rule {
	private static final long serialVersionUID = 1L;

	int onDraw(final Game game) {
		final Card drawn = game.getLastDrawn();
		if (drawn == null || !game.isLegal(drawn)) {
			return ALLOW;
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				game.setHandPlayable(false);
				drawn.played();
			}
		});
		return STOP;
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
