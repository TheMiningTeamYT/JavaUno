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
class Stacking extends Rule implements ActionHandler {
	private static final long serialVersionUID = 1L;
	private static final int STACK = 1024;
	private static final int FINISH_STACK = 1025;

	private transient JLabel cardsToDrawLabel = new JLabel("", SwingConstants.CENTER);
	private boolean stacking = false;
	private int cardsToDraw = 0;

	void bind(Game game) {
		Deck deck = game.getDeck();
		deck.overrideCard(Deck.RED_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.RED_PLUS2)));
		deck.overrideCard(Deck.YELLOW_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.YELLOW_PLUS2)));
		deck.overrideCard(Deck.GREEN_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.GREEN_PLUS2)));
		deck.overrideCard(Deck.BLUE_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.BLUE_PLUS2)));

		if (game instanceof NetworkGameClient) {
			((NetworkGameClient)game).registerAction(STACK, this);
			((NetworkGameClient)game).registerAction(FINISH_STACK, this);
		}
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
			cardsToDrawLabel.setForeground(Util.BLACK);
			cardsToDrawLabel.setFont(Util.getScaledFont());
			cardsToDrawLabel.addComponentListener(Util.getTextResizeListener());
		}

		void played(Card parent) {
			// TODO: Maybe play an animation for drawing?
			Game game = parent.getGame();
			if (game.handContains(1, Deck.RED_PLUS2) || game.handContains(1, Deck.YELLOW_PLUS2)
				|| game.handContains(1, Deck.GREEN_PLUS2) || game.handContains(1, Deck.BLUE_PLUS2)) {
				JPanel customUISpace = parent.getGame().getCustomUISpace();
				int[] args = new int[] {
						2,
						Deck.RED_PLUS2,
						Deck.YELLOW_PLUS2,
						Deck.GREEN_PLUS2,
						Deck.BLUE_PLUS2,
				};

				game.onTurn();
				if (game.getHandPlayable(0)) {
					game.setHandPlayable(false);
					game.setTypePlayable(Deck.RED_PLUS2, true);
					game.setTypePlayable(Deck.YELLOW_PLUS2, true);
					game.setTypePlayable(Deck.GREEN_PLUS2, true);
					game.setTypePlayable(Deck.BLUE_PLUS2, true);
				}

				stack(game, 2);

				if (game instanceof NetworkGameClient) {
					System.out.println("Sending stacking action!");
					((NetworkGameClient)game).sendAction(STACK, args);
				}
			} else {
				for (int i = 0; i < cardsToDraw + 2; i++) {
					game.drawToHand(1);
				}
				
				game.onTurn();
				game.onTurn();

				if (game instanceof NetworkGameClient) {
					((NetworkGameClient)game).sendAction(FINISH_STACK, null);
				}
				finish(game);
			}
		};
	}
	
	private void start(Game game) {
		JPanel customUISpace = game.getCustomUISpace();

		customUISpace.setLayout(new GridLayout(1, 1));
		customUISpace.add(cardsToDrawLabel);
		customUISpace.setVisible(true);

		stacking = true;
	}
	
	private void stack(Game game, int value) {
		cardsToDraw += value;
		cardsToDrawLabel.setText("+" + cardsToDraw);

		if (!stacking) {
			start(game);
		}
	}
	
	private void finish(Game game) {
		JPanel customUISpace = game.getCustomUISpace();
		customUISpace.removeAll();
		customUISpace.setVisible(false);
		cardsToDraw = 0;
		stacking = false;
	}

	public void handleAction(int type, Object argument, Game game) {
		switch (type) {
			case STACK: {
				int[] args;
				if (!(argument instanceof int[])) {
					return;
				}

				args = (int[]) argument;
				if (game.getHandPlayable(0)) {
					game.setHandPlayable(false);
					for (int i = 1; i < args.length; i++) {
						game.setTypePlayable(args[i], true);
					}
				}
				stack(game, args[0]);
				break;
			} case FINISH_STACK:
				finish(game);
			default:
				break;
		}
		
	}
}
