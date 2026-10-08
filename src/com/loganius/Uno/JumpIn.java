package com.loganius.Uno;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JumpIn extends Rule implements ActionHandler {
	private static final long serialVersionUID = 1L;

	private static final int START_JUMP_IN = 1026;
	private static final int END_JUMP_IN = 1027;
	private static final int SLOT = 268482188;

	private transient JLabel instruction;
	private transient boolean[] playableBefore;
	private NetworkInt active;

	// Defacto constructor.
	void bind(Game game) {
		if (game instanceof NetworkGameClient) {
			((NetworkGameClient)game).registerAction(START_JUMP_IN, this);
			((NetworkGameClient)game).registerAction(END_JUMP_IN, this);
		}
		playableBefore = new boolean[Game.MAX_PLAYERS];
		active = new NetworkInt(game, SLOT, -1);

		instruction = new JLabel("Jump In!", SwingConstants.CENTER);
		instruction.setForeground(new Color(105, 0, 204));
		instruction.setFont(Util.getLargeFont());
		instruction.addComponentListener(Util.LargeResizeListener);
	}
	
	void unbind(Game game) {
		if (game instanceof NetworkGameClient) {
			((NetworkGameClient)game).unregisterAction(START_JUMP_IN);
			((NetworkGameClient)game).unregisterAction(END_JUMP_IN);
		}
		active.release(game);
		active = null;
	}
	
	void bind(Deck deck) {
		for (int i = 0; i < deck.getLength(); i++) {
			deck.overrideCard(i, new JumpInCard(deck.getCard(i)));
		}
	}
	
	int isLegal(Card card, Game game) {
		if (active.get(game) < 0) {
			return ALLOW;
		}
		if (card.getType() == game.getLastPlayed().getType()) {
			return LEGAL;
		} else {
			return ILLEGAL;
		}
	}
	
	private void activate(Game game, int lastPlayed) {
		JPanel customUISpace = game.getCustomUISpace();

		customUISpace.setLayout(new GridLayout(1, 1));
		customUISpace.add(instruction);
		customUISpace.setVisible(true);

		for (int i = 0; i < game.getPlayers(); i++) {
			playableBefore[i] = game.getHandPlayable(i);
			if (game.isHandPlayer(i)) {
				game.setHandPlayable(false, i);
				game.setTypePlayable(lastPlayed, true, i);
			}
		}
		
		active.set(game, lastPlayed);
	}
	
	private void finish(Game game) {
		JPanel customUISpace = game.getCustomUISpace();

		customUISpace.removeAll();
		customUISpace.setVisible(false);

		for (int i = 0; i < game.getPlayers(); i++) {
			game.setHandPlayable(playableBefore[i]);
		}
		
		active.set(game, -1);
	}

	public void handleAction(int type, Object argument, Game game) {
		switch (type) {
			case START_JUMP_IN:
				if (!(argument instanceof Integer)) {
					break;
				}
				activate(game, ((Integer)argument).intValue());
				break;
			case END_JUMP_IN:
				finish(game);
				break;
			default:
				break;
		}
	}
	
	public void actionDryRun(int type, Object argument, Game game) {
		switch (type) {
			case START_JUMP_IN:
				if (!(argument instanceof Integer)) {
					break;
				}
				active.set(game, ((Integer)argument).intValue(), true);
				break;
			case END_JUMP_IN:
				active.set(game, -1, true);
				break;
			default:
				break;
		}
	}
	
	private class JumpInCard extends CardType {
		private CardType parentType;

		JumpInCard(CardType parent) {
			super(parent);
			this.parentType = parent;
		}
		
		void played(final Card parent) {
			int type = parent.getType();
			final Game game = parent.getGame();
			if (active.get(game) >= 0) {
				int hand = game.findHand(parent);
				finish(game);
				
				if (game instanceof NetworkGameClient) {
					((NetworkGameClient)game).sendAction(END_JUMP_IN, null);
				}

				for (int i = 0; i < hand; i++) {
					game.onTurn();
				}
			} else {
				for (int i = 0; i < game.getPlayers(); i++) {
					if (game.handContains(i, type)) {
						Timer jumpInDelay = new Timer(2000, new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								if (active.get(game) >= 0) {
									finish(game);

									if (game instanceof NetworkGameClient) {
										((NetworkGameClient)game).sendAction(END_JUMP_IN, null);
									}

									parentType.played(parent);
								}
							}
						});
						jumpInDelay.setRepeats(false);
						jumpInDelay.start();

						game.checkUno();
						activate(game, type);
						if (game instanceof NetworkGameClient) {
							((NetworkGameClient)game).sendAction(START_JUMP_IN, new Integer(type));
						}
						return;
					}
				}
			}
			parentType.played(parent);
		}
	}
	
	static class Factory implements RuleFactory {
		public Rule create() {
			return new JumpIn();
		}
		
		public String getName() {
			return "Jump In";
		}
	}
}
