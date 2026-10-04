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
	
	private static final int NOT_STACKING = 0;
	private static final int STACKING_2 = 2;
	private static final int STACKING_4 = 4;

	private transient JLabel cardsToDrawLabel = new JLabel("", SwingConstants.CENTER);
	private int stacking = 0;
	private int cardsToDraw = 0;
	
	Stacking() {
		cardsToDrawLabel.setForeground(new Color(105, 0, 204));
		cardsToDrawLabel.setFont(Util.getLargeFont());
		cardsToDrawLabel.addComponentListener(Util.LargeResizeListener);
	}

	void bind(Game game) {
		Deck deck = game.getDeck();
		deck.overrideCard(Deck.RED_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.RED_PLUS2)));
		deck.overrideCard(Deck.YELLOW_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.YELLOW_PLUS2)));
		deck.overrideCard(Deck.GREEN_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.GREEN_PLUS2)));
		deck.overrideCard(Deck.BLUE_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.BLUE_PLUS2)));
		deck.overrideCard(Deck.WILD_DRAW4, new StackingDrawFourType(deck.getCard(Deck.WILD_DRAW4)));

		if (game instanceof NetworkGameClient) {
			((NetworkGameClient)game).registerAction(STACK, this);
			((NetworkGameClient)game).registerAction(FINISH_STACK, this);
		}
	}
	
	int isLegal(Card card, Game game) {
		switch (stacking) {
			default:
				return ALLOW;
			case STACKING_2:
				switch (card.getType()) {
					case Deck.RED_PLUS2:
					case Deck.YELLOW_PLUS2:
					case Deck.GREEN_PLUS2:
					case Deck.BLUE_PLUS2:
						return ALLOW;
					default:
						return ILLEGAL;
				}
			case STACKING_4:
				switch (card.getType()) {
					case Deck.WILD_DRAW4:
						return ALLOW;
					default:
						return ILLEGAL;
				}
		}
	}
	
	private class StackingDrawTwoType extends CardType {
		private final int[] args = {
				STACKING_2,
				Deck.RED_PLUS2,
				Deck.YELLOW_PLUS2,
				Deck.GREEN_PLUS2,
				Deck.BLUE_PLUS2,
		};
		
		StackingDrawTwoType(CardType parent) {
			super(parent);
		}

		void played(Card parent) {
			// TODO: Maybe play an animation for drawing?
			Game game = parent.getGame();
			stack(game, STACKING_2);

			if (game.handContains(1, Deck.RED_PLUS2) || game.handContains(1, Deck.YELLOW_PLUS2)
				|| game.handContains(1, Deck.GREEN_PLUS2) || game.handContains(1, Deck.BLUE_PLUS2)) {
				game.onTurn();
				setTypesPlayable(game, args);

				if (game instanceof NetworkGameClient) {
					System.out.println("Sending stacking action!");
					((NetworkGameClient)game).sendAction(STACK, args);
				}
			} else {
				draw(game);
				if (game instanceof NetworkGameClient) {
					((NetworkGameClient)game).sendAction(FINISH_STACK, null);
				}
				finish(game);
			}
		};
	}
	
	private class StackingDrawFourType extends WildChangeColorType {
		private final int[] args = {
				STACKING_4,
				Deck.WILD_DRAW4,
		};

		StackingDrawFourType(CardType parent) {
			super(parent);
		}
		
		void played(Card parent) {
			parent.getGame().getCustomUISpace().removeAll();
			super.played(parent);
		}
		
		protected void cardAction(Card parent, int color) {
			Game game = parent.getGame();
			parent.setType(Deck.RED_DRAW4 + color);

			stack(game, STACKING_4);
			start(game, STACKING_4);

			if (game.handContains(1, Deck.WILD_DRAW4)) {
				game.onTurn();
				setTypesPlayable(game, args);

				if (game instanceof NetworkGameClient) {
					System.out.println("Sending stacking action!");
					((NetworkGameClient)game).sendAction(STACK, args);
				}
			} else {
				draw(game);
				if (game instanceof NetworkGameClient) {
					((NetworkGameClient)game).sendAction(FINISH_STACK, null);
				}
				finish(game);
			}
		};
	}
	
	private void start(Game game, int value) {
		JPanel customUISpace = game.getCustomUISpace();

		customUISpace.setLayout(new GridLayout(1, 1));
		customUISpace.add(cardsToDrawLabel);
		customUISpace.setVisible(true);

		stacking = value;
	}
	
	private void stack(Game game, int value) {
		cardsToDraw += value;
		cardsToDrawLabel.setText("+" + cardsToDraw);

		if (stacking == NOT_STACKING) {
			start(game, value);
		}
	}
	
	private void draw(Game game) {
		for (int i = 0; i < cardsToDraw; i++) {
			game.drawToHand(1);
		}
		
		game.onTurn();
		game.onTurn();
	}
	
	private void finish(Game game) {
		JPanel customUISpace = game.getCustomUISpace();
		customUISpace.removeAll();
		customUISpace.setVisible(false);
		cardsToDraw = 0;
		stacking = NOT_STACKING;
	}
	
	private void setTypesPlayable(Game game, int[] args) {
		if (game.getHandPlayable(0)) {
			game.setHandPlayable(false);
			for (int i = 1; i < args.length; i++) {
				game.setTypePlayable(args[i], true);
			}
		}
	}

	public void handleAction(int type, Object argument, Game game) {
		switch (type) {
			case STACK: {
				int[] args;
				if (!(argument instanceof int[])) {
					return;
				}

				args = (int[]) argument;
				stack(game, args[0]);
				setTypesPlayable(game, args);
				break;
			} case FINISH_STACK:
				finish(game);
			default:
				break;
		}
		
	}
}
