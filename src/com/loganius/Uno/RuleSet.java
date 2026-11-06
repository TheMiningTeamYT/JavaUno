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
	
	void unbind(Game game) {};
	
	void bind(Deck deck) {};
	
	abstract boolean wildcardChallenges(Game game);
	
	abstract protected void draw(Game game);

	void onDraw(Game game) {
		if (game.getHandPlayable(0) && !game.getDrew()) {
			game.setHandPlayable(false);
			draw(game);
		}
	}
	
	RuleSet getFallback() {
		return this.fallback;
	}
	
	void finishDraw(Game game) {
		Card card = game.getLastDrawn();
		if (game.isLegal(card)) {
			game.setPlayable(card, true);
		}
		if (game.canBePlayed(0)) {
			game.checkUno();
		} else {
			game.onTurn();
		}
	}
	
	boolean equals(RuleSet other) {
		if (other == null) {
			return this == null;
		}
		return getClass() == other.getClass();
	}
	
	boolean check() {
		return true;
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
			return (card.getColor() == CardType.Color.WILD ||
				card.getColor() == lastPlayed.getColor() ||
				(lastPlayed.getValue() != -1 &&
				card.getValue() == lastPlayed.getValue()));
		}

		protected void draw(Game game) {
			game.drawToHand(0);
			game.finishDraw();
		}
		
		boolean wildcardChallenges(Game game) {
			return true;
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
		
		Composite(Rule[] rules) {
			this(new Standard(), rules);
		}

		Composite(RuleSet fallback, Rule[] rules) {
			super(fallback);
			if (rules == null) {
				throw new IllegalArgumentException("No rules provided!");
			}
			this.rules = rules;
		}
		
		void bind(Game game) {
			for (int i = 0; i < rules.length; i++) {
				rules[i].bind(game);
			}
			bind(game.getDeck());
		}
		
		void unbind(Game game) {
			for (int i = 0; i < rules.length; i++) {
				rules[i].unbind(game);
			}
		}
		
		void bind(Deck deck) {
			for (int i = 0; i < rules.length; i++) {
				rules[i].bind(deck);
			}
		}

		boolean isLegal(Card card, Game game) {
			int decision = Rule.ALLOW;
			for (int i = 0; i < rules.length; i++) {
				switch (rules[i].isLegal(card, game)) {
					case Rule.ILLEGAL:
						decision = Rule.ILLEGAL;
					case Rule.LEGAL:
						if (decision != Rule.ILLEGAL) {
							decision = Rule.LEGAL;
						}
					case Rule.ALLOW:
					default:
						break;
				}
			}
			if (decision == Rule.ALLOW) {
				return fallback.isLegal(card, game);
			} else {
				return decision == Rule.LEGAL;
			}
		}
		
		protected void draw(final Game game) {
			new Thread() {
				public void run() {
					boolean runFallback = true;
					for (int i = 0; i < rules.length; i++) {
						switch (rules[i].onDraw(game)) {
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
								game.finishDraw();
							}
						});
					}
				}
			}.start();
		}
		
		void finishDraw(Game game) {
			boolean runFallback = true;
			for (int i = 0; i < rules.length; i++) {
				switch (rules[i].finishDraw(game)) {
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
				fallback.finishDraw(game);
			}
		}
		
		boolean wildcardChallenges(Game game) {
			for (int i = 0; i < rules.length; i++) {
				switch (rules[i].wildcardChallenges(game)) {
					case Rule.LEGAL:
						return true;
					case Rule.ILLEGAL:
						return false;
					case Rule.ALLOW:
					default:
						break;
				}
			}
			return fallback.wildcardChallenges(game);
		}
		
		boolean equals(RuleSet other) {
			if (other == null) {
				return this == null;
			}
			if (getClass() != other.getClass()) {
				return false;
			}
			if (((Composite)other).rules.length != rules.length) {
				return false;
			}
			for (int i = 0; i < rules.length; i++) {
				if (((Composite)other).rules[i].getClass() != rules[i].getClass()) {
					return false;
				}
			}
			return true;
		}
		
		boolean check() {
			if (rules == null) {
				return false;
			}
			
			for (int i = 0; i < rules.length; i++) {
				if (rules[i] == null) {
					return false;
				}
			}
			
			return true;
		}
	}
}
