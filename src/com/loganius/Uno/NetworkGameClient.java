package com.loganius.Uno;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.Vector;
import java.util.Hashtable;

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
	private Hashtable actionHandlers;
	private boolean handlingMessages = false;
	private Game game = this;

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
	
	Card drawToHand(int hand, int type) {
		if (!handlingMessages) {
			state.drawToHand(hand, type);
			synchronized(outQueue) {
				outQueue.addElement(Action.drawToHand(hand, type, state));
			}
		}

		return super.drawToHand(hand, type);
	}
	
	void removeFromHand(int hand, Card card) {
		super.removeFromHand(hand, card);

		if (!handlingMessages) {
			synchronized(outQueue) {
				state.removeFromHand(hand, card.getType());
				outQueue.addElement(Action.removeFromHand(hand, card.getType(), state));
			}
		}
	}
	
	void discard(Card card) {
		super.discard(card);

		if (!handlingMessages) {
			synchronized(outQueue) {
				state.discard(card.getType());
				outQueue.addElement(Action.discard(card.getType(), state));
			}
		}
	}
	
	void reverse() {
		super.reverse();
		
		if (!handlingMessages) {
			state.reverse();
			synchronized(outQueue) {
				outQueue.addElement(Action.reverse(state));
			}
		}
	}
	
	void rotateHands() {
		super.rotateHands();
		move();
		
		if (!handlingMessages) {
			state.move();
			synchronized(outQueue) {
				outQueue.addElement(Action.rotateHands(state));
			}
		}
	}
	
	void onTurn() {
		super.onTurn();
		move();
		
		if (!handlingMessages) {
			state.move();
			state.setLastPlayed(getLastPlayed().getType());
			synchronized(outQueue) {
				outQueue.addElement(Action.turn(state, getLastPlayed().getType()));
			}
		}
	}
	
	void callUno(int player) {
		if (player == 0) {
			super.callUno(0);
			
			if (!handlingMessages) {
				state.doUno();
				synchronized(outQueue) {
					outQueue.addElement(Action.uno(state));
				}
			}
		}
	}
	
	void deal() {
		// Bit of a hack
		boolean handlingBefore = handlingMessages;

		handlingMessages = false;
		super.deal();
		handlingMessages = true;

		state.setLastPlayed(getLastPlayed().getType());
		synchronized(outQueue) {
			outQueue.removeAllElements();
			outQueue.addElement(Action.setState(state));
		}
	}
	
	void swapHands(int hand1, int hand2) {
		super.swapHands(hand1, hand2);
		
		if (!handlingMessages) {
			state.swapHands(hand1, hand2);
			synchronized(outQueue) {
				outQueue.addElement(Action.swap(state, hand1, hand2));
			}
		}
	}
	
	private void move() {
		if (getTurnOrder() == 0) {
			moves++;
		} else {
			moves--;
		}
		hands[0].setPlayable(isHand0Player());
		netGameUI.setActivePlayer(findScreenHand(0));
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
			hands[i].setPlayable(false);
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
		moves = newState.getMoves();
		hands[0].setPlayable(isHand0Player());
		netGameUI.setActivePlayer(findScreenHand(0));
		
		for (int i = 0; i < getPlayers(); i++) {
			for (int j = 0; j < newHands[i].length; j++) {
				new Card(newHands[i][j].intValue(), hands[i]);
			}
		}
		
		new Card(newState.getLastPlayed(), discardHand);
		resizeHands();
	}
	
	void registerAction(int type, ActionHandler handler) {
		// Hack for the order in which Java initializes objects.
		if (actionHandlers == null) {
			actionHandlers = new Hashtable();
		}
		if (actionHandlers.containsKey(new Integer(type))) {
			System.err.println("!!! TWO PLUGINS ARE USING ACTION" + type + "!!!");
			return;
		}
		new CustomAction(type, handler);
	}
	
	void sendAction(int type, Object arg) {
		if (actionHandlers.containsKey(new Integer(type))) {
			synchronized(outQueue) {
				outQueue.addElement(new Action(type, arg, state));
			}
		}
	}
	
	void applyAction(Action a) {
		int[] args;
		Integer arg;

		if (actionHandlers.containsKey(new Integer(a.getType()))
			&& ((CustomAction)actionHandlers.get(new Integer(a.getType()))).handleAction(a)) {
			return;
		}

		switch (a.getType()) {
			case Action.DRAW_TO_HAND:
				if (!(a.getArgument() instanceof int[])) {
					break;
				}
				args = (int[]) a.getArgument();
				drawToHand(args[0], args[1]);
				break;
			case Action.REMOVE_FROM_HAND:
				if (!(a.getArgument() instanceof int[])) {
					break;
				}
				args = (int[]) a.getArgument();
				removeFromHand(args[0], args[1]);
				break;
			case Action.DISCARD:
				if (!(a.getArgument() instanceof Integer)) {
					break;
				}
				arg = (Integer) a.getArgument();
				discard(arg.intValue());
				break;
			case Action.REVERSE:
				if (a.getArgument() != null) {
					break;
				}
				reverse();
				break;
			case Action.ROTATE_HANDS:
				if (a.getArgument() != null) {
					break;
				}
				rotateHands();
				break;
			case Action.TURN:
				if (!(a.getArgument() instanceof Integer)) {
					break;
				}
				arg = (Integer) a.getArgument();
				new Card(arg.intValue(), discardHand);
				onTurn();
				break;
			case Action.SET_STATE:
				if (!(a.getArgument() instanceof GameState)) {
					break;
				}
				{
					GameState state = (GameState) a.getArgument();
					setState(state);
				}
				break;
			case Action.WELCOME: {
				if (!(a.getArgument() instanceof Action.WelcomeAction)) {
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
				screenHands[0].setUp(true);
				netGameUI.start(player, getPlayers());
				start();
				break;
			} 
			case Action.UNO:
				if (isHand0Player()) {
					callUno(1);
				} else {
					callUno(0);
				}
				break;
			case Action.SWAP:
				if (!(a.getArgument() instanceof int[])) {
					break;
				}
				args = (int[]) a.getArgument();
				swapHands(args[0], args[1]);
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
						handlingMessages = true;
						do {
							Action[] acts = (Action[]) inQueue.firstElement();
							for (int i = 0; i < acts.length; i++) {
								applyAction(acts[i]);
							}
							inQueue.removeElementAt(0);
						} while (inQueue.size() > 0);
						handlingMessages = false;
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
	
	private class CustomAction {
		int type;
		ActionHandler handler;

		CustomAction(int type, ActionHandler handler) {
			this.type = type;
			this.handler = handler;
			actionHandlers.put(new Integer(type), this);
		}
		
		boolean handleAction(Action a) {
			if (a.getType() == type) {
				handler.handleAction(type, a.getArgument(), game);
				return true;
			}
			return false;
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

					if (received == null || !(received instanceof Action[])) {
						continue;
					}

					synchronized(inQueue) {
						inQueue.addElement(received);
					}
				} catch (Exception e) {
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
				handleError(e);
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
							return;
						}
					}
				}
			}
		}
	}
}
