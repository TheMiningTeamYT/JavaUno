package com.loganius.Uno;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Date;

/**
 * TODO: Add the ability for users to set usernames.
 * TODO: Handle network errors gracefully.
 * TODO: Some more GUI flair for joining a game.
 * TODO: Ensure the network representation is in sync the game representation
 * note: Can get a font of a given size using the Font constructor
 */
class NetworkGameClient extends Game {
	private Socket socket = null;
	private volatile Vector inQueue = new Vector();
	private volatile Vector outQueue;
	private Thread inThread;
	private Thread outThread;
	private GameState state;
	private NetGameUI netGameUI;
	private int moves = 0;
	private int player = 0;
	private String[] names;
	private Timer queueHandler = new Timer(20, new TimerListener());
	private Hashtable actionHandlers;
	private boolean handlingMessages = false;
	private Game game = this;
	private long lastMove = new Date().getTime();

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
	
	protected void preInit() {
		state = new GameState();
		outQueue = new Vector();
		actionHandlers = new Hashtable();
		netGameUI = new NetGameUI(this);
		super.preInit();
	}
	
	void start() {
		super.start();
		if (player == 0) {
			deal();
		}
		netGameUI.setActivePlayer(findScreenHand(0));
	}
	
	Card drawToHand(int hand, int type) {
		if (isHandPlayer(hand)) {
			lastMove = new Date().getTime();
		}

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
			state.removeFromHand(hand, card.getType());
			synchronized(outQueue) {
				outQueue.addElement(Action.removeFromHand(hand, card.getType(), state));
			}
		}
	}
	
	void discard(Card card) {
		int hand = findHand(card);
		super.discard(card);
		
		if (isHandPlayer(hand)) {
			lastMove = new Date().getTime();
		}

		if (!handlingMessages) {
			state.discard(card.getType(), hand);
			synchronized(outQueue) {
				outQueue.addElement(Action.discard(card.getType(), hand, state));
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
		
		if (isHandPlayer(0)) {
			lastMove = (new Date()).getTime();
		}
		
		if (!handlingMessages) {
			state.move();
			state.setLastPlayed(getLastPlayed().getType());
			synchronized(outQueue) {
				outQueue.addElement(Action.turn(state, getLastPlayed().getType()));
			}
		}
	}
	
	void setRuleSet(RuleSet rules) {
		super.setRuleSet(rules);
		
		if (!handlingMessages) {
			state.setRuleSet(rules);
			synchronized(outQueue) {
				outQueue.addElement(Action.setRules(rules));
			}
		}
	}
	
	void callUno(int player) {
		// Bit of a hack
		boolean handlingBefore = handlingMessages;

		handlingMessages = false;
		super.callUno(player);
		handlingMessages = handlingBefore;
		
		if (!handlingMessages) {
			state.doUno();
			synchronized(outQueue) {
				outQueue.addElement(Action.callUno(state));
			}
		}
	}
	
	boolean checkUno() {
		boolean unoActive = super.checkUno();
		if (unoActive && !handlingMessages) {
			state.startUno();
			synchronized(outQueue) {
				outQueue.addElement(Action.startUno(state));
			}
		}
		return unoActive;
	}
	
	void deal() {
		// Bit of a hack
		boolean handlingBefore = handlingMessages;

		handlingMessages = false;
		super.deal();
		handlingMessages = handlingBefore;

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
	
	void sendWildCardChallenge() {
		try {
			super.sendWildCardChallenge();
		} catch (RuntimeException e) {};
		
		if (!handlingMessages) {
			state.sendWildCardChallenge(getLastPlayed().getType());
			synchronized(outQueue) {
				outQueue.addElement(Action.sendChallenge(state, getLastPlayed().getType()));
			}
		}
	}
	
	protected void endWildCardChallenge() {
		super.endWildCardChallenge();
		
		if (!handlingMessages) {
			state.endWildCardChallenge();
			synchronized(outQueue) {
				outQueue.addElement(Action.endChallenge(state));
			}
		}
	}
	
	boolean isHandPlayer(int hand) {
		return (findHand(0) == hand);
	}
	
	private void move() {
		if (hands[0].numCards() > 0) {
			if (getTurnOrder() == 0) {
				moves++;
			} else {
				moves--;
			}
			hands[0].setPlayable(isHandPlayer(0));
			netGameUI.setActivePlayer(findScreenHand(0));
		}
	}
	
	private void removeFromHand(int hand, int cardType) {
		Card toBeRemoved = hands[hand].getCardByType(cardType);
		if (toBeRemoved != null) {
			super.removeFromHand(hand, toBeRemoved);
		}
	}
	
	private void discard(int cardType, int hand) {
		Card toBeDiscarded = hands[hand].getCardByType(cardType);
		if (toBeDiscarded != null) {
			super.discard(toBeDiscarded);
		}
	}
	
	private void setState(GameState newState) {
		Integer[][] newHands = newState.getHands();
		int adjustMoves = ((newState.getMoves()) - moves) % getPlayers();
		setTurnOrder(newState.getTurnOrder());
		setRuleSet(newState.getRules());

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
		hands[0].setPlayable(isHandPlayer(0));
		netGameUI.setActivePlayer(findScreenHand(0));
		setUno(newState.getUno());
		setChallengeActive(newState.getChallengeActive());
		
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
			throw new IllegalArgumentException("!!! TWO PLUGINS ARE USING ACTION" + type + "!!!");
		}
		new CustomAction(type, handler);
	}
	
	void unregisterAction(int type) {
		if (!actionHandlers.containsKey(new Integer(type))) {
			throw new IllegalArgumentException("ActionType " + type + "not in use!");
		}
		actionHandlers.remove(new Integer(type));
	}
	
	void sendAction(int type, Object arg) {
		if (actionHandlers.containsKey(new Integer(type))) {
			synchronized(outQueue) {
				outQueue.addElement(new Action(type, arg, state));
			}
		} else {
			throw new IllegalArgumentException("Action type " + type + " not registered");
		}
	}
	
	void requestTrackingSlot(int cookie, int val) {
		state.requestTrackingSlot(cookie, val);
		if (!handlingMessages) {
			synchronized(outQueue) {
				outQueue.addElement(Action.createSlot(state, cookie, val));
			}
		}
	}
	
	void releaseTrackingSlot(int cookie) {
		state.releaseTrackingSlot(cookie);
	}
	
	void updateSlot(int cookie, int val) {
		if (state.getSlot(cookie) != val) {
			state.updateSlot(cookie, val);
			if (!handlingMessages) {
				synchronized(outQueue) {
					outQueue.addElement(Action.updateSlot(state, cookie, val));
				}
			}
		}
	}
	
	int getSlot(int cookie) {
		return state.getSlot(cookie);
	}
	
	boolean verify() {
		Integer[][] stateHands;
		if (state.getPlayers() != getPlayers() || state.getTurnOrder() != getTurnOrder() ||
			(getLastPlayed() != null && state.getLastPlayed() != getLastPlayed().getType()) ||
			state.getMoves() != moves || !state.getRules().equals(getRuleSet()) ||
			state.getUno() != getUno() || state.getChallengeActive() != getChallengeActive()) {
			return false;
		}

		stateHands = state.getHands();
		for (int i = 0; i < getPlayers(); i++) {
			if (stateHands[i].length != hands[i].numCards()) {
				return false;
			}
			for (int j = 0; j < stateHands[i].length; j++) {
				if (!hands[i].contains(stateHands[i][j].intValue())) {
					return false;
				}
			}
		}
		return true;
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
				if (a.getArgument().getClass() != int[].class) {
					break;
				}
				args = (int[]) a.getArgument();
				drawToHand(args[0], args[1]);
				break;
			case Action.REMOVE_FROM_HAND:
				if (a.getArgument().getClass() != int[].class) {
					break;
				}
				args = (int[]) a.getArgument();
				removeFromHand(args[0], args[1]);
				break;
			case Action.DISCARD:
				if (a.getArgument().getClass() != int[].class) {
					break;
				}
				args = (int[]) a.getArgument();
				discard(args[0], args[1]);
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
				if (a.getArgument().getClass() != Integer.class) {
					break;
				}
				arg = (Integer) a.getArgument();
				getLastPlayed().setType(arg.intValue());
				onTurn();
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
						rotateScreenHands(0);
					}
					resizeHands();
				}
				screenHands[0].setUp(true);
				netGameUI.start(player, getPlayers());
				start();
				break;
			}
			case Action.CALL_UNO:
				if (isHandPlayer(0)) {
					callUno(1);
				} else {
					callUno(0);
				}
				break;
			case Action.SWAP:
				if (a.getArgument().getClass() != int[].class) {
					break;
				}
				args = (int[]) a.getArgument();
				swapHands(args[0], args[1]);
				break;
			case Action.SET_RULES:
				if (!(a.getArgument() instanceof RuleSet)) {
					break;
				}
				setRuleSet((RuleSet)a.getArgument());
				break;
			case Action.START_UNO:
				checkUno();
				break;
			case Action.SEND_CHALLENGE:
				if (a.getArgument().getClass() != Integer.class) {
					break;
				}
				arg = (Integer) a.getArgument();
				getLastPlayed().setType(arg.intValue());
				sendWildCardChallenge();
				break;
			case Action.END_CHALLENGE:
				endWildCardChallenge();
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
		Rectangle bounds = getBounds();
		netGameUI.setBounds(0, 0, bounds.width, bounds.height);
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
			boolean appliedSuccessfully = true;
			handlingMessages = true;

			synchronized(inQueue) {
				if (inQueue.size() > 0) {
					Vector queue = (Vector)inQueue.clone();
					Action a = null;
					while (queue.size() > 0) {
						Action[] acts = (Action[]) queue.firstElement();
						for (int i = 0; i < acts.length; i++) {
							a = acts[i];
							if (!actionHandlers.containsKey(new Integer(a.getType()))
								|| !((CustomAction)actionHandlers.get(new Integer(a.getType()))).dryRun(a)) {
								state.applyAction(a);
							}
						}
						queue.removeElementAt(0);
					};

					if (a != null && a.verify(state)) {
						while (inQueue.size() > 0) {
							Action[] acts = (Action[]) inQueue.firstElement();
							for (int i = 0; i < acts.length; i++) {
								applyAction(acts[i]);
							}
							inQueue.removeElementAt(0);
						};
						appliedSuccessfully = verify();
					} else {
						appliedSuccessfully = false;
					}
				}
			}
			
			handlingMessages = false;
			
			if (isHandPlayer(0)) {
				if (!getStarted() || isInterrupted()) {
					lastMove = new Date().getTime();
				} else if (new Date().getTime() - lastMove > 20000) {
					game.onTurn();
				} else if (new Date().getTime() - lastMove > 15000) {
					if (!game.getDrew()) {
						game.onDraw();
					}
				} 
			}

			synchronized(outQueue) {
				if (!appliedSuccessfully) {
					outQueue.removeAllElements();
					outQueue.addElement(Action.getState());
					System.out.println("Client desync!");
				}
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
		
		boolean dryRun(Action a) {
			if (a.getType() == type) {
				handler.actionDryRun(type, a.getArgument(), game);
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
