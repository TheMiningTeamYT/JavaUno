package com.loganius.Uno;

class NoBluffing extends Rule {
	int isLegal(Card card, Game game) {
		if (card.getType() == Deck.WILD_DRAW4 && 
			game.handContainsColor(0, game.getLastPlayed().getCardType().getColor())) {
			return ILLEGAL;
		}

		return ALLOW;
	}
	
	int wildcardChallenges(Game game) {
		return ILLEGAL;
	}
	
	static class Factory implements RuleFactory {
		public Rule create() {
			return new NoBluffing();
		}
		
		public String getName() {
			return "No Bluffing";
		}
	}
}
