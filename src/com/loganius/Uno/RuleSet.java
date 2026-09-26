package com.loganius.Uno;

/**
 * The rules by which a particular game of Uno is played.
 * TODO: Figure out a way to allow multiple rulesets to coexist.
 * In real Uno, you can have multiple special rules active simultaneously.
 * Idea: Implement individual rules which return a boolean if they activated on any given condition.
 */

abstract class RuleSet {
	protected RuleSet fallback;
	protected Game game;

	RuleSet(RuleSet fallback, Game game) {
		this.fallback = fallback;
		this.game = game;
	}
	// Determine if it's legal to play a particular card under the given ruleset.
	abstract boolean isLegal(Card card);

	// TODO: Figure out A: if overriding the turn action is ever necessary and B: if so, how to do that.
	void onTurn() {};

	// TODO: Checks to make sure that the user is allowed to draw.
	abstract void onDraw();
	
	RuleSet getFallback() {
		return this.fallback;
	}
	
	void fallback() {
		game.setRuleSet(getFallback());
	}
	
	/**
	 * An implementation of the standard ruleset for Uno.
	 */
	static class Standard extends RuleSet {
		Standard(Game game) {
			super(null, game);
		}

		boolean isLegal(Card card) {
			Card lastPlayed = game.getLastPlayed();
			return (card.getCardType().color == CardType.Color.WILD ||
				card.getCardType().color == lastPlayed.getCardType().color ||
				(lastPlayed.getCardType().value != -1 &&
				card.getCardType().value == lastPlayed.getCardType().value));
		}

		void onDraw() {
			if (game.getHandPlayable(0) && !game.canBePlayed()) {
				int deckLength = game.getDeck().getLength();
				game.drawToHand(0);
				game.onTurn();
			}
		}
		
		RuleSet getFallback() {
			return this;
		}
	}
}
