package com.loganius.Uno;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *  TODO: Add a delay between showing the final cards to draw
 *  and actually drawing the cards
 */
class Stacking extends Rule implements ActionHandler {
	private static final long serialVersionUID = 1L;
	private static final int STACK = 1024;
	private static final int FINISH_STACK = 1025;
	
	private static final int NOT_STACKING = 0;
	private static final int STACKING_2 = 2;
	private static final int STACKING_4 = 4;
	private static final int STACKING_SLOT = 858029013;
	private static final int CARDS_SLOT = 200481172;

	private transient JLabel cardsToDrawLabel;
	private NetworkInt stacking;
	private NetworkInt cardsToDraw;

	// Defacto constructor.
	void bind(Game game) {
		if (game instanceof NetworkGameClient) {
			((NetworkGameClient)game).registerAction(STACK, this);
			((NetworkGameClient)game).registerAction(FINISH_STACK, this);
		}
		stacking = new NetworkInt(game, STACKING_SLOT, NOT_STACKING);
		cardsToDraw = new NetworkInt(game, CARDS_SLOT, 0);

		cardsToDrawLabel = new JLabel("", SwingConstants.CENTER);
		cardsToDrawLabel.setForeground(new Color(105, 0, 204));
		cardsToDrawLabel.setFont(Util.getLargeFont());
		cardsToDrawLabel.addComponentListener(Util.LargeResizeListener);
	}
	
	void unbind(Game game) {
		if (game instanceof NetworkGameClient) {
			((NetworkGameClient)game).unregisterAction(STACK);
			((NetworkGameClient)game).unregisterAction(FINISH_STACK);
			stacking.release(game);
			cardsToDraw.release(game);
			stacking = null;
			cardsToDraw = null;
		}
	}
	
	void bind(Deck deck) {
		deck.overrideCard(Deck.RED_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.RED_PLUS2)));
		deck.overrideCard(Deck.YELLOW_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.YELLOW_PLUS2)));
		deck.overrideCard(Deck.GREEN_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.GREEN_PLUS2)));
		deck.overrideCard(Deck.BLUE_PLUS2, new StackingDrawTwoType(deck.getCard(Deck.BLUE_PLUS2)));
		deck.overrideCard(Deck.WILD_DRAW4, new StackingDrawFourType(deck.getCard(Deck.WILD_DRAW4)));
	}
	
	int isLegal(Card card, Game game) {
		switch (stacking.get(game)) {
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
	
	private boolean handContains(Game game, int[] args) {
		for (int i = 1; i < args.length; i++) {
			if (game.handContains(0, args[i])) {
				return true;
			}
		}
		return false;
	}
	
	private void pass(Game game, int[] args) {
		stack(game, args[0]);
		if (game instanceof NetworkGameClient) {
			((NetworkGameClient)game).sendAction(STACK, args);
		}
		setTypesPlayable(game, args);
	}
	
	private void stackNext(final Game game, int[] args) {
		game.onTurn();
		if (game.getEnded()) {
			return;
		}
		if (stacking.get(game) == NOT_STACKING) {
			if (handContains(game, args)) {
				pass(game, args);
			} else {
				draw(game, 2);
			}
		} else {
			pass(game, args);
			if (!handContains(game, args)) {
				game.setHandPlayable(false);
				Timer drawDelayTimer = new Timer(1000, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						draw(game, cardsToDraw.get(game));
					}
				});
				drawDelayTimer.setRepeats(false);
				drawDelayTimer.start();
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
			stackNext(parent.getGame(), args);
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
			parent.setType(Deck.RED_DRAW4 + color);
			stackNext(parent.getGame(), args);
		};
	}
	
	private void start(Game game, int value) {
		JPanel customUISpace = game.getCustomUISpace();

		customUISpace.setLayout(new GridLayout(1, 1));
		customUISpace.add(cardsToDrawLabel);
		customUISpace.setVisible(true);

		stacking.set(game, value);
	}
	
	private void stack(Game game, int value) {
		cardsToDraw.set(game, cardsToDraw.get(game) + value);
		cardsToDrawLabel.setText("+" + cardsToDraw.get(game));

		if (stacking.get(game) == NOT_STACKING) {
			start(game, value);
		}
	}
	
	private void draw(Game game, int numCards) {
		if (stacking.get(game) != NOT_STACKING) {
			finish(game);
			if (game instanceof NetworkGameClient) {
				((NetworkGameClient)game).sendAction(FINISH_STACK, null);
			}
		}
		for (int i = 0; i < numCards; i++) {
			game.drawToHand(0);
		}
		game.onTurn();
	}
	
	private void finish(Game game) {
		JPanel customUISpace = game.getCustomUISpace();
		customUISpace.removeAll();
		customUISpace.setVisible(false);
		cardsToDraw.set(game, 0);
		stacking.set(game, NOT_STACKING);
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
				break;
			default:
				break;
		}
		
	}
	
	public void actionDryRun(int type, Object argument, Game game) {
		switch (type) {
			case STACK: {
				int[] args;
				if (!(argument instanceof int[])) {
					return;
				}

				args = (int[]) argument;
				cardsToDraw.set(game, cardsToDraw.get(game) + args[0], true);
				stacking.set(game, args[0], true);
				break;
			} case FINISH_STACK:
				cardsToDraw.set(game, 0, true);
				stacking.set(game, NOT_STACKING, true);
				break;
			default:
				break;
		}
		
	}
	
	static class Factory implements RuleFactory {
		public Rule create() {
			return new Stacking();
		}
		
		public String getName() {
			return "Stack";
		}
	}
}
