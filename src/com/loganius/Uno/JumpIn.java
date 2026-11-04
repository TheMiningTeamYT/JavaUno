package com.loganius.Uno;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// TODO: Proper Game vs UIGame distinction
public class JumpIn extends Rule implements Plugin {
	private static final long serialVersionUID = 1L;

	private static final int START_JUMP_IN = 1027;
	private static final int END_JUMP_IN = 1028;
	private static final int SLOT = 268482188;

	private transient JLabel instruction;
	private transient boolean[] playableBefore;
	private NetworkInt active;

	// Defacto constructor.
	public void bind(Game game) {
		if (!GameUtils.isServer(game)) {
			UIGame uiGame = (UIGame)game;
			instruction = new JLabel("Jump In!", SwingConstants.CENTER);
			instruction.setForeground(uiGame.getAccentColor());
			instruction.setFont(Util.getLargeFont());
			instruction.addComponentListener(Util.LargeResizeListener);
		}
		game.registerAction(START_JUMP_IN, this);
		game.registerAction(END_JUMP_IN, this);
		playableBefore = new boolean[GameUtils.MAX_PLAYERS];
		active = new NetworkInt(game, SLOT, -1);
	}
	
	public void unbind(Game game) {
		game.unregisterAction(START_JUMP_IN);
		game.unregisterAction(END_JUMP_IN);
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
		boolean playerHasCard = false;
		for (int i = 0; i < game.getPlayers(); i++) {
			playableBefore[i] = game.getHandPlayable(i);
			if (game.isHandPlayer(i) || GameUtils.isServer(game)) {
				game.setHandPlayable(false, i);
				if (game.handContains(i, lastPlayed)) {
					game.setTypePlayable(lastPlayed, true, i);
					playerHasCard = true;
				}
			}
		}

		if (!GameUtils.isServer(game)) {
			JPanel customUISpace = ((UIGame)game).getCustomUISpace();
	
			instruction.setText((playerHasCard) ? "Jump In!" : "Waiting...");
			
			customUISpace.setLayout(new GridLayout(1, 1));
			customUISpace.add(instruction);
			customUISpace.setVisible(true);
		}
		
		active.set(game, lastPlayed);
	}
	
	private void finish(Game game) {
		if (!GameUtils.isServer(game)) {
			JPanel customUISpace = ((UIGame)game).getCustomUISpace();
	
			customUISpace.removeAll();
			customUISpace.setVisible(false);
		}

		for (int i = 0; i < game.getPlayers(); i++) {
			game.setHandPlayable(playableBefore[i]);
		}
		
		active.set(game, -1);
	}

	public void handleAction(int type, Object argument, Game game) {
		switch (type) {
			case START_JUMP_IN:
				if (argument.getClass() != Integer.class) {
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
	
	public void simulate(int type, Object argument, Game game) {
		switch (type) {
			case START_JUMP_IN:
				if (argument.getClass() != Integer.class) {
					break;
				}
				active.set(game, ((Integer)argument).intValue());
				break;
			case END_JUMP_IN:
				active.set(game, -1);
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
				int turnPos = game.findTurnPos(parent);
				finish(game);
				
				if (GameUtils.isServer(game)) {
					game.sendAction(END_JUMP_IN, null);
				}

				for (int i = 0; i < turnPos; i++) {
					game.onTurn();
				}
			} else {
				for (int i = 0; i < game.getPlayers(); i++) {
					if (game.handContains(i, type)) {
						Timer jumpInDelay = new Timer(2000, new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								if (active.get(game) >= 0 && !game.getEnded()) {
									finish(game);

									if (GameUtils.isServer(game)) {
										game.sendAction(END_JUMP_IN, null);
									}

									parentType.played(parent);
								}
							}
						});
						jumpInDelay.setRepeats(false);
						//jumpInDelay.start();

						activate(game, type);
						if (GameUtils.isServer(game)) {
							game.sendAction(START_JUMP_IN, new Integer(type));
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
