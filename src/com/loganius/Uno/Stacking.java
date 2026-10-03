package com.loganius.Uno;

import javax.swing.*;
import java.awt.*;

/**
 *  TODO: I hate this code for relying on bespoke code in the Game class
 *  I need support for, like, a side channel so that we don't have to rely
 *  on bespoke code, which will be important in the eventual scripted rule future.
 *  Idea: Implement side channel communications using the action type to
 *  determine which listener to dispatch the action to.
 *  Do this when implementing scripted rules.
 */
class Stacking extends Rule {
	private static final long serialVersionUID = 1L;
	private transient JLabel cardsToDraw = new JLabel("", SwingConstants.CENTER);
	private boolean stacking = false;

	void bind(Game game) {
		Deck deck = game.getDeck();
		deck.overrideCard(Deck.RED_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.RED_PLUS2)));
		deck.overrideCard(Deck.YELLOW_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.YELLOW_PLUS2)));
		deck.overrideCard(Deck.GREEN_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.GREEN_PLUS2)));
		deck.overrideCard(Deck.BLUE_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.BLUE_PLUS2)));
	}
	
	int isLegal(Card card, Game game) {
		if (stacking) {
			switch (card.getType()) {
				case Deck.RED_PLUS2:
				case Deck.YELLOW_PLUS2:
				case Deck.GREEN_PLUS2:
				case Deck.BLUE_PLUS2:
					return ALLOW;
				default:
					return ILLEGAL;
			}
		} else {
			return ALLOW;
		}
	}
	
	void onTurn() {
		stacking = false;
	}
	
	private class StackingDrawTwoType extends CardType {
		StackingDrawTwoType(CardType parent) {
			super(parent);
			cardsToDraw.setForeground(Util.BLACK);
			cardsToDraw.setFont(Util.getScaledFont());
			cardsToDraw.addComponentListener(Util.getTextResizeListener());
		}

		void played(Card parent) {
			// TODO: Maybe play an animation for drawing?
			Game game = parent.getGame();
			if (game.handContains(1, Deck.RED_PLUS2) || game.handContains(1, Deck.YELLOW_PLUS2)
				|| game.handContains(1, Deck.GREEN_PLUS2) || game.handContains(1, Deck.BLUE_PLUS2)) {
				JPanel customUISpace = parent.getGame().getCustomUISpace();

				game.stackingCardPlayed(new int[] {Deck.RED_PLUS2, Deck.YELLOW_PLUS2, Deck.GREEN_PLUS2, Deck.BLUE_PLUS2}, 2);

				cardsToDraw.setText("+" + game.getCardsToDraw());
				customUISpace.setLayout(new GridLayout(1, 1));
				customUISpace.add(cardsToDraw);
				customUISpace.setVisible(true);

				stacking = true;
			} else {
				JPanel customUISpace = parent.getGame().getCustomUISpace();
				int cardsToDraw = game.getCardsToDraw() + 2;

				for (int i = 0; i < cardsToDraw; i++) {
					game.drawToHand(1);
				}

				game.onTurn();
				game.onTurn();
				customUISpace.removeAll();
				customUISpace.setVisible(false);

				stacking = false;
			}
		};
	}
}
