package com.loganius.Uno;

/**
 * The rules by which a particular game of Uno is played.
 * TODO: Figure out a way to allow multiple rulesets to coexist.
 * In real Uno, you can have multiple special rules active simultaneously.
 * Idea: Implement individual rules which return a boolean if they activated on any given condition.
 */

abstract class RuleSet {
	protected RuleSet fallback;

	RuleSet(RuleSet fallback) {
		this.fallback = fallback;
	}
	// Determine if it's legal to play a particular card under the given ruleset.
	abstract boolean isLegal(Card card, Game game);

	// TODO: Figure out A: if overriding the turn action is ever necessary and B: if so, how to do that.
	void onTurn(Game game) {};

	abstract void onDraw(Game game);
	
	RuleSet getFallback() {
		return this.fallback;
	}
	
	/**
	 * An implementation of the standard ruleset for Uno.
	 */
	static class Standard extends RuleSet {
		Standard() {
			super(null);
		}

		boolean isLegal(Card card, Game game) {
			Card lastPlayed = game.getLastPlayed();
			return (card.getCardType().color == CardType.Color.WILD ||
				card.getCardType().color == lastPlayed.getCardType().color ||
				(lastPlayed.getCardType().value != -1 &&
				card.getCardType().value == lastPlayed.getCardType().value));
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
}
