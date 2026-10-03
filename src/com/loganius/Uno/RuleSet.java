package com.loganius.Uno;

import javax.swing.SwingUtilities;
import java.io.Serializable;

/**
 * The rules by which a particular game of Uno is played.
 * TODO: Figure out a way to allow multiple rulesets to coexist.
 * In real Uno, you can have multiple special rules active simultaneously.
 * Idea: Implement individual rules which return a boolean if they activated on any given condition.
 * TODO: Figure out A: if overriding the turn action is ever necessary and B: if so, how to do that.
 */

abstract class RuleSet implements Serializable {
	private static final long serialVersionUID = 1L;
	protected RuleSet fallback;

	RuleSet(RuleSet fallback) {
		this.fallback = fallback;
	}
	// Determine if it's legal to play a particular card under the given ruleset.
	abstract boolean isLegal(Card card, Game game);
	
	void bind(Game game) {};

	abstract void onDraw(Game game);
	
	RuleSet getFallback() {
		return this.fallback;
	}
	
	void onTurn(Game game) {};
	
	static void finalizeDraw(Game game) {
		game.drew();
		if (game.canBePlayed(0)) {
			game.checkUno();
		} else {
			game.onTurn();
		}
	}
	
	/**
	 * An implementation of the standard ruleset for Uno.
	 */
	static class Standard extends RuleSet {
		private static final long serialVersionUID = 1L;

		Standard() {
			super(null);
		}

		boolean isLegal(Card card, Game game) {
			Card lastPlayed = game.getLastPlayed();
			return (card.getCardType().getColor() == CardType.Color.WILD ||
				card.getCardType().getColor() == lastPlayed.getCardType().getColor() ||
				(lastPlayed.getCardType().getValue() != -1 &&
				card.getCardType().getValue() == lastPlayed.getCardType().getValue()));
		}

		void onDraw(Game game) {
			if (game.getHandPlayable(0) && !game.getDrew()) {
				Card card;
				game.setHandPlayable(false);
				card = game.drawToHand(0);
				game.drew();
				if (game.isLegal(card)) {
					card.setPlayable(true);
					game.checkUno();
				} else {
					game.onTurn();
				}
			}
		}
		
		RuleSet getFallback() {
			return this;
		}
	}
	
	/**
	 * A composite ruleset comprised of individual rules.
	 * 
	 */
	static class Composite extends RuleSet {
		private static final long serialVersionUID = 1L;
		private Rule[] rules;
		private transient Card lastDrawn = null;
		
		Composite(Rule[] rules) {
			this(new Standard(), rules);
		}

		Composite(RuleSet fallback, Rule[] rules) {
			super(fallback);
			this.rules = rules;
		}
		
		void bind(Game game) {
			for (int i = 0; i < rules.length; i++) {
				rules[i].bind(game);
			}
		}

		boolean isLegal(Card card, Game game) {
			for (int i = 0; i < rules.length; i++) {
				switch (rules[i].isLegal(card, game)) {
					case Rule.ILLEGAL:
						return false;
					case Rule.LEGAL:
						return true;
					case Rule.ALLOW:
						break;
					default:
						break;
				}
			}
			return fallback.isLegal(card, game);
		}
		
		void drawToHand(final int hand, final Game game) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					lastDrawn = game.drawToHand(hand);
				}
			});
		}
		
		Card getLastDrawn() {
			return lastDrawn;
		}
		
		void onTurn(Game game) {
			for (int i = 0; i < rules.length; i++) {
				rules[i].onTurn(game);
			}
		};
		
		void onDraw(final Game game) {
			if (game.getHandPlayable(0) && !game.getDrew()) {
				game.setHandPlayable(false);
				final Composite ruleset = this;
				new Thread() {
					public void run() {
						boolean runFallback = true;
						for (int i = 0; i < rules.length; i++) {
							switch (rules[i].onDraw(game, ruleset)) {
								case Rule.STOP:
									return;
								case Rule.PREVENT_FALLBACK:
									runFallback = false;
									break;
								case Rule.ALLOW:
									break;
								default:
									break;
							}
						}
						if (runFallback) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									fallback.onDraw(game);
								}
							});
						} else {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									RuleSet.finalizeDraw(game);
								}
							});
						}
					}
				}.start();
			}
		}
	}
}
