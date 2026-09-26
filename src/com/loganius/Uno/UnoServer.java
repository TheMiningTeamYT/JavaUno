package com.loganius.Uno;

import java.net.*;
import java.io.*;
import java.util.Vector;
import java.lang.Thread;

// TODO: Make this a reusable class
// TODO: Use threads for the different clients to avoid blocking.
public class UnoServer {
	private ServerSocket server = null;
	private Socket[] socket = new Socket[4];
	private volatile Vector[] inQueue = new Vector[] {
			new Vector(),
			new Vector(),
			new Vector(),
			new Vector(),
	};
	private volatile Vector[] outQueue = new Vector[] {
			new Vector(),
			new Vector(),
			new Vector(),
			new Vector(),
	};
	private Thread[][] workers = {
			new Thread[2],
			new Thread[2],
			new Thread[2],
			new Thread[2],
	};
	private String[] names = {
			"",
			"",
			"",
			"",
	};
	private Thread gameWorker;
	private volatile boolean start = false;
	private volatile int players = 0;
	private volatile int playersReady = 0;
	
	UnoServer() throws IOException {
		server = new ServerSocket(23770, 4, InetAddress.getByName("0.0.0.0"));
		server.setSoTimeout(500);
		gameWorker = new GameWorker();
		gameWorker.start();
	}
	
	private void handleError(Exception e) {
		e.printStackTrace();
		System.exit(1);
	}
	
	private class GameWorker extends Thread {
		GameState state = new GameState();
		public void processQueue() {
			GameState newState = new GameState(state);
			for (int client = 0; client < players; client++) {
				Action a = null;
				Vector acts = new Vector();
				synchronized(inQueue[client]) {
					if (inQueue[client].size() == 0) {
						continue;
					}
					while (inQueue[client].size() > 0) {
						Action[] actionSet = (Action[])inQueue[client].firstElement();
						for (int action = 0; action < actionSet.length; action++) {
							Action priorAction = a;
							a = actionSet[action];
							System.out.println("Action type " + a.getType() + " sent by " + client);
							switch (a.getType()) {
								case Action.SEND_STATE:
									synchronized(outQueue[client]) {
										outQueue[client].addElement(Action.setState(state));
									}
									a = priorAction;
									break;
								case Action.HELLO: {
									System.out.println("Hello " + client + "!");
									Object received = a.getArgument();
									if (received.getClass() != String.class) {
										break;
									}
									names[client] = (String)received;
									String[] currentNames = null;
									if (players > 0) {
										currentNames = new String[players];
										synchronized(names) {
											System.arraycopy(names, 0, currentNames, 0, players);
										}
									}
									for (int i = 0; i < players; i++) {
										synchronized(outQueue[i]) {
											outQueue[i].addElement(Action.welcome(currentNames, i, players));
										}
									}
									playersReady++;
									a = priorAction;
									break;
								}
								case Action.REQUEST_START:
									if (client == 0) {
										start = true;
									}
									a = priorAction;
									break;
								default:
									acts.addElement(a);
									newState.applyAction(a);
									break;
							}
						}
						inQueue[client].removeElementAt(0);
					}
				}

				if (a == null) {
					continue;
				}

				if (a.verify(newState)) {
					for (int i = 0; i < players; i++) {
						if (client != i) {
							synchronized(outQueue[i]) {
								for (int act = 0; act < acts.size(); act++) {
									outQueue[i].addElement(acts.elementAt(act));
								}
							}
						}
					}
					state = newState;
				} else {
					for (int j = 0; j < players; j++) {
						synchronized(outQueue[j]) {
							outQueue[j].addElement(Action.setState(state));
						}
						synchronized(inQueue[j]) {
							inQueue[j].removeAllElements();
						}
					}
					System.out.println("Desynced!");
					break;
				}
			}
			for (int i = 0; i < players; i++) {
				synchronized(outQueue[i]) {
					if (outQueue[i].size() > 0) {
						outQueue[i].notify();
					}
				}
			}
		}
		public void run() {
			try {
				System.out.println("Waiting for connections...");
				while (players == 0 || !start) {
					if (players < 4) {
						try {
							socket[players] = server.accept();
						} catch (InterruptedIOException e) {
							processQueue();
							continue;
						}
	
						workers[players][0] = new InputWorker(socket[players], players);
						workers[players][1] = new OutputWorker(socket[players], players);
						workers[players][0].start();
						workers[players][1].start();
						players++;
						state.setPlayers(players);
						
						System.out.println("Player " + players + " connected!");
					} else {
						processQueue();
					}
				}
			} catch (IOException e) {
				handleError(e);
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			processQueue();

			for (int i = 0; i < players; i++) {
				synchronized(outQueue[i]) {
					outQueue[i].addElement(Action.startGame());
				}
			}

			while (true) {
				processQueue();
				synchronized(inQueue) {
					try {
						inQueue.wait();
					} catch (InterruptedException e) {
						handleError(e);
					}
				}
			}
		}
	}
	
	// TODO: Handle the errors that can occur with networking
	private class InputWorker extends Thread {
		private ObjectInputStream in;
		private Socket socket;
		private int id;
		
		InputWorker(Socket socket, int id) {
			this.socket = socket;
			this.id = id;
		}

		public void run() {
			try {
				in = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				handleError(e);
			}
			while (true) {
				try {
					Object received = in.readObject();

					if (received == null || received.getClass() != Action[].class) {
						continue;
					}
					
					synchronized(inQueue[id]) {
						inQueue[id].addElement(received);
					}

					synchronized(inQueue) {
						inQueue.notify();
					}
				} catch (IOException e) {
					handleError(e);
				} catch (ClassNotFoundException e) {
					handleError(e);
				}
			}
		}
	}
	
	private class OutputWorker extends Thread {
		private ObjectOutputStream out;
		private Socket socket;
		private int id;
		
		OutputWorker(Socket socket, int id) {
			this.socket = socket;
			this.id = id;
		}

		public void run() {
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
				out.flush();
			} catch (IOException e) {
				handleError(e);
			}
			while (true) {
				synchronized(outQueue[id]) {
					if (outQueue[id].size() > 0) {
						Action[] queue = new Action[outQueue[id].size()];
						outQueue[id].copyInto(queue);

						try {
							out.writeObject(queue);
							out.reset();
							out.flush();
						} catch (IOException e) {
							handleError(e);
						}

						outQueue[id].removeAllElements();
						System.out.println("Sent action type " + queue[0].getType() + " to " + id);
					} else {
						try {
							outQueue[id].wait();
						} catch (InterruptedException e) {
							handleError(e);
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		try {
			new UnoServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
