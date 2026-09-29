package com.loganius.Uno;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.lang.*;
import java.awt.event.*;
import javax.swing.*;

import com.loganius.Uno.Action;

import java.util.Vector;

/**
 * TODO: Add the ability for users to set usernames.
 * TODO: Handle network errors gracefully.
 * TODO: Some more GUI flair for joining a game.
 * note: Can get a font of a given size using the Font constructor
 */
class NetworkGameClient extends Game {
	private Socket socket = null;
	private volatile Vector inQueue = new Vector();
	private volatile Vector outQueue = new Vector();
	private Thread inThread;
	private Thread outThread;
	private GameState state = new GameState();
	private NetGameUI netGameUI = new NetGameUI(this);
	private int moves = 0;
	private int player = 0;
	private String[] names;
	private Timer queueHandler = new Timer(20, new TimerListener());

	NetworkGameClient(Deck deck, GameHandler handler, String name, String server, int port) {
		super(deck, handler);
		Rectangle bounds = getBounds();

		try {
			socket = new Socket(server, port);
		} catch (IOException e) {
			handleError(e);
			return;
		}
		
		netGameUI.setBounds(0, 0, bounds.width, bounds.height);
		names = new String[] {name};
		outQueue.addElement(Action.hello(name));

		inThread = new InputWorker();
		outThread = new OutputWorker();
		inThread.start();
		outThread.start();
		queueHandler.start();
	}
	
	void start() {
		super.start();
		if (player == 0) {
			deal();
		}
		netGameUI.setActivePlayer(findScreenHand(0));
		onResize();
	}
	
	void drawToHand(int hand, int type) {
		super.drawToHand(hand, type);
		state.drawToHand(hand, type);
		synchronized(outQueue) {
			outQueue.addElement(Action.drawToHand(hand, type, state));
		}
	}
	
	void removeFromHand(int hand, Card card) {
		super.removeFromHand(hand, card);
		state.removeFromHand(hand, card.getType());
		synchronized(outQueue) {
			outQueue.addElement(Action.removeFromHand(hand, card.getType(), state));
		}
	}
	
	void discard(Card card) {
		super.discard(card);
		state.discard(card.getType());
		synchronized(outQueue) {
			outQueue.addElement(Action.discard(card.getType(), state));
		}
	}
	
	void reverse() {
		super.reverse();
		state.reverse();
		synchronized(outQueue) {
			outQueue.addElement(Action.reverse(state));
		}
	}
	
	void rotateHands() {
		super.rotateHands();
		state.move();

		if (getTurnOrder() == 0) {
			moves++;
		} else {
			moves--;
		}

		synchronized(outQueue) {
			outQueue.addElement(Action.rotateHands(state));
		}
	}
	
	void onTurn() {
		super.onTurn();
		state.move();
		state.setLastPlayed(getLastPlayed().getType());

		if (getTurnOrder() == 0) {
			moves++;
		} else {
			moves--;
		}
		
		hands[0].setPlayable(isHand0Player());
		netGameUI.setActivePlayer(findScreenHand(0));

		synchronized(outQueue) {
			outQueue.addElement(Action.turn(state, getLastPlayed().getType()));
		}
	}
	
	void callUno(int player) {
		if (player == 0) {
			super.callUno(0);
			state.doUno();
			
			synchronized(outQueue) {
				outQueue.addElement(Action.uno(state));
			}
		}
	}
	
	void deal() {
		super.deal();
		state.setLastPlayed(getLastPlayed().getType());
		synchronized(outQueue) {
			outQueue.removeAllElements();
			outQueue.addElement(Action.setState(state));
		}
	}
	
	private void removeFromHand(int hand, int cardType) {
		Card toBeRemoved = hands[hand].getCardByType(cardType);
		if (toBeRemoved != null) {
			super.removeFromHand(hand, toBeRemoved);
		}
	}
	
	private void discard(int cardType) {
		Card toBeDiscarded = hands[0].getCardByType(cardType);
		if (toBeDiscarded != null) {
			super.discard(toBeDiscarded);
		}
	}
	
	private void setState(GameState newState) {
		Integer[][] newHands = newState.getHands();
		int adjustMoves = ((newState.getMoves()) - moves) % getPlayers();
		setTurnOrder(newState.getTurnOrder());

		for (int i = 0; i < getPlayers(); i++) {
			hands[i].removeAll();
		}

		if (adjustMoves < 0) {
			adjustMoves += getPlayers();
		}
		if (getTurnOrder() == 1) {
			adjustMoves = (getPlayers() - adjustMoves) % getPlayers();
		}

		for (int i = 0; i < adjustMoves; i++) {
			rotate();
		}
		hands[0].setPlayable(isHand0Player());
		moves = newState.getMoves();
		
		
		for (int i = 0; i < getPlayers(); i++) {
			for (int j = 0; j < newHands[i].length; j++) {
				new Card(newHands[i][j].intValue(), hands[i]);
			}
		}
		
		new Card(newState.getLastPlayed(), discardHand);
		resizeHands();
	}
	
	void applyAction(Action a) {
		int[] args;
		Integer arg;
		switch (a.getType()) {
			case Action.DRAW_TO_HAND:
				if (a.getArgument().getClass() != int[].class) {
					break;
				}
				args = (int[]) a.getArgument();
				super.drawToHand(args[0], args[1]);
				break;
			case Action.REMOVE_FROM_HAND:
				if (a.getArgument().getClass() != int[].class) {
					break;
				}
				args = (int[]) a.getArgument();
				removeFromHand(args[0], args[1]);
				break;
			case Action.DISCARD:
				if (a.getArgument().getClass() != Integer.class) {
					break;
				}
				arg = (Integer) a.getArgument();
				discard(arg.intValue());
				break;
			case Action.REVERSE:
				if (a.getArgument() != null) {
					break;
				}
				super.reverse();
				break;
			case Action.ROTATE_HANDS:
				if (a.getArgument() != null) {
					break;
				}
				super.rotateHands();
				break;
			case Action.TURN:
				if (a.getArgument().getClass() != Integer.class) {
					break;
				}
				arg = (Integer) a.getArgument();
				new Card(arg.intValue(), discardHand);
				super.onTurn();
				hands[0].setPlayable(isHand0Player());
				netGameUI.setActivePlayer(findScreenHand(0));
				break;
			case Action.SET_STATE:
				if (a.getArgument().getClass() != GameState.class) {
					break;
				}
				{
					GameState state = (GameState) a.getArgument();
					setState(state);
				}
				break;
			case Action.WELCOME: {
				if (a.getArgument().getClass() != Action.WelcomeAction.class) {
					break;
				}
				Action.WelcomeAction hello = (Action.WelcomeAction) a.getArgument();
				int newPlayer = hello.getPlayer();
				int newNumPlayers = hello.getNumPlayers();
				String[] newNames = hello.getNames();
				if (newPlayer < 0 || newPlayer >= 4 || newNumPlayers <= 0 || newNumPlayers > 4) {
					break;
				}

				player = hello.getPlayer();
				setPlayers(hello.getNumPlayers());
				state.setPlayers(newNumPlayers);
				if (player == 0 && getPlayers() > 1) {
					netGameUI.showStartButton();
				}

				if (newNames == null || newNames.length > 4) {
					break;
				}
				names = newNames;
				netGameUI.setPlayers(names);
				break;
			} case Action.START_GAME: {
				int adjustment = player;
				if (adjustment != 0) {
					for (int i = 0; i < adjustment; i++) {
						rotateScreenHands(1);
					}
					resizeHands();
				}
				screenOrderedHands[0].setUp(true);
				netGameUI.start(player, getPlayers());
				start();
				break;
			} 
			case Action.UNO:
				if (isHand0Player()) {
					super.callUno(1);
				} else {
					super.callUno(0);
				}
				break;
			default:
				break;
		}
	}


	void handleError(Exception err) {
		if (!getEnded()) {
			final String msg = (err.getMessage() == null) ? "A networking error occured." : err.getMessage();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					onEnd(msg);
				}
			});
			err.printStackTrace();

			if (inThread != null) {
				inThread.interrupt();
				inThread = null;
			}
			if (outThread != null) {
				outThread.interrupt();
				outThread = null;
			}
			try {
				if (socket != null) {
					socket.close();
					socket = null;
				}
			} catch (IOException e) {}
			
			netGameUI.setVisible(false);
			queueHandler.stop();
		}
	}
	
	protected void onResize() {
		if (netGameUI != null) {
			Rectangle bounds = getBounds();
			netGameUI.setBounds(0, 0, bounds.width, bounds.height);
		}
		super.onResize();
	}
	
	protected void gameOver() {
		if (inThread != null) {
			inThread.interrupt();
			inThread = null;
		}
		if (outThread != null) {
			outThread.interrupt();
			outThread = null;
		}
		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (IOException e) {}
		super.gameOver();
	}
	
	void requestStart() {
		outQueue.addElement(Action.requestStart());
	}
	
	private class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Vector queue = null;
			synchronized(inQueue) {
				if (inQueue.size() > 0) {
					queue = (Vector)inQueue.clone();
				}
			}

			if (queue != null) {
				Action a = null;
				do {
					Action[] acts = (Action[]) queue.firstElement();
					for (int i = 0; i < acts.length; i++) {
						a = acts[i];
						state.applyAction(a);
					}
					queue.removeElementAt(0);
				} while (queue.size() > 0);

				if (a != null && a.verify(state)) {
					synchronized(inQueue) {
						do {
							Action[] acts = (Action[]) inQueue.firstElement();
							for (int i = 0; i < acts.length; i++) {
								applyAction(acts[i]);
							}
							inQueue.removeElementAt(0);
						} while (inQueue.size() > 0);
					}
				} else {
					synchronized(outQueue) {
						outQueue.removeAllElements();
						outQueue.addElement(Action.getState());
					}
				}
			}

			synchronized(outQueue) {
				if (outQueue.size() > 0) {
					outQueue.notify();
				}
			}
		}
	}
	
	// TODO: Handle the errors that can occur with networking
	private class InputWorker extends Thread {
		private ObjectInputStream in;

		public void run() {
			try {
				in = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			while (true) {
				try {
					Object received = in.readObject();

					if (received == null || received.getClass() != Action[].class) {
						continue;
					}

					synchronized(inQueue) {
						inQueue.addElement(received);
					}
				} catch (IOException e) {
					try {
						in.close();
					} catch (IOException e2) {
						handleError(e2);
					}
					handleError(e);
					return;
				} catch (ClassNotFoundException e) {
					try {
						in.close();
					} catch (IOException e2) {
						handleError(e2);
					}
					handleError(e);
					return;
				}
			}
		}
	}
	
	private class OutputWorker extends Thread {
		private ObjectOutputStream out;

		public void run() {
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
				out = null;
			}
			while (true) {
				synchronized(outQueue) {
					if (outQueue.size() > 0) {
						Action[] queue = new Action[outQueue.size()];
						outQueue.copyInto(queue);

						try {
							out.writeObject(queue);
							out.reset();
							out.flush();
						} catch (IOException e) {
							try {
								out.close();
							} catch (IOException e2) {
								handleError(e2);
							}
							handleError(e);
							return;
						}

						outQueue.removeAllElements();
					} else {
						try {
							outQueue.wait();
						} catch (InterruptedException e) {
							try {
								out.close();
							} catch (IOException e2) {
								handleError(e2);
							}
							handleError(e);
							return;
						}
					}
				}
			}
		}
	}
}
