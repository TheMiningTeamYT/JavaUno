package com.loganius.Uno;

import java.net.*;
import java.io.*;

import java.util.Date;
import java.util.Vector;
import java.util.Random;

import java.lang.Thread;

public class UnoServer {
	private ServerSocket server = null;
	private Socket[] sockets = new Socket[] {null, null, null, null};
	private volatile Vector inQueue = new Vector();
	private volatile Vector[] outQueue = new Vector[] {
			new Vector(),
			new Vector(),
			new Vector(),
			new Vector(),
	};
	private Thread[][] workers = {
			new Thread[] {null, null},
			new Thread[] {null, null},
			new Thread[] {null, null},
			new Thread[] {null, null},
	};
	private String[] names = {
			"",
			"",
			"",
			"",
	};
	private Thread gameWorker = new GameWorker();
	private volatile boolean start = false;
	private volatile int players = 0;
	private volatile boolean ended = false;
	
	public UnoServer(int port) throws IOException {
		server = new ServerSocket(port, 4, InetAddress.getByName("0.0.0.0"));
		server.setSoTimeout(500);
		gameWorker.start();
	}
	
	public void stop() {
		ended = true;
		cleanup();
	}

	private void handleError(Exception e) {
		if (!ended) {
			System.out.println("error");
			ended = true;
			e.printStackTrace();
			cleanup();
		}
	}
	
	private void cleanup() {
		if (gameWorker != null) {
			gameWorker.interrupt();
			gameWorker = null;
		}
		for (int i = 0; i < players; i++) {
			if (workers[i][0] != null) {
				workers[i][0].interrupt();
				workers[i][0] = null;
			}
			if (workers[i][1] != null) {
				workers[i][1].interrupt();
				workers[i][1] = null;
			}
			if (sockets[i] != null) {
				try {
					sockets[i].close();
				} catch (IOException e2) {};
				sockets[i] = null;
			}
		}
		if (server != null) {
			try {
				server.close();
			} catch (IOException e2) {};
			server = null;
		}
	}
	
	private class GameWorker extends Thread implements GameHandler {
		private GameState state = new GameState();
		private ServerGameAdapter gameNet = new ServerGameAdapter();
		private ServerGame game = new ServerGame(this, gameNet, state);
		private Random[] RNGs;
		private Random starter = new Random();
		
		GameWorker() {
			RNGs = new Random[] {
					new Random(new Date().getTime() ^ starter.nextLong()),
					new Random(new Date().getTime() ^ starter.nextLong()),
					new Random(new Date().getTime() ^ starter.nextLong()),
					new Random(new Date().getTime() ^ starter.nextLong()),
			};
		}
		
		private void processQueue() {
			GameState oldState = (GameState)state.clone();
			boolean[] desynced = new boolean[players];
			synchronized(inQueue) {
				while (inQueue.size() > 0) {
					ClientActionSet actionSet = (ClientActionSet)inQueue.firstElement();
					int client = actionSet.getClient();
					inQueue.removeElementAt(0);
					
					if (start) {
						gameNet.drain();
						for (int i = 0; i < actionSet.size(); i++) {
							Action a = actionSet.getAction(i);
							if (a == null || !a.check()) {
								continue;
							}
							switch (a.getType()) {
								case Action.SEND_STATE: {
									desynced[client] = true;
									break;
								}
								default:
									gameNet.add(actionSet.getAction(i));
									break;
							}
						}

						if ((desynced[client] = !game.dispatch(client, RNGs[client]))) {
							for (int i = 0; i < inQueue.size(); i++) {
								if (((ClientActionSet)inQueue.elementAt(i)).getClient() == client) {
									inQueue.removeElementAt(i--);
								}
							}
							state = (GameState)oldState.clone();
							game.setState(state);
							System.out.println("Desynced on client " + client + "!");
						} else {
							Action[] acts = gameNet.getQueuedActions();
							for (int i = 0; i < players; i++) {
								if (i != client) {
									synchronized(outQueue[i]) {
										for (int j = 0; j < acts.length; j++) {
											outQueue[i].addElement(acts[j]);
										}
									}
								}
							}
							oldState = (GameState)state.clone();
						}

						gameNet.flush();
					} else {
						for (int i = 0; i < actionSet.size(); i++) {
							Action a = actionSet.getAction(i);

							if (a == null || !a.check()) {
								continue;
							}

							Object argument = a.getArgument();

							switch(a.getType()) {
								case Action.HELLO: {
									names[client] = (String)argument;
									String[] currentNames = new String[players];
									System.arraycopy(names, 0, currentNames, 0, players);
									for (int j = 0; j < players; j++) {
										synchronized(outQueue[j]) {
											outQueue[j].addElement(Action.welcome(currentNames, j, players));
										}
									}
									continue;
								}
								case Action.REQUEST_START:
									if (client == 0) {
										game.setRuleSet((RuleSet)argument);
										start = true;
									}
									continue;
								default:
									continue;
							}
						}
					}
				}
			}
			for (int i = 0; i < players; i++) {
				synchronized(outQueue[i]) {
					if (desynced[i]) {
						long seed = new Date().getTime() ^ starter.nextLong();
						Action.State newState = new Action.State(state, seed);
						RNGs[i].setSeed(seed);
						outQueue[i].removeAllElements();
						outQueue[i].addElement(Action.setState(newState));
					}
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
							sockets[players] = server.accept();
						} catch (InterruptedIOException e) {
							processQueue();
							continue;
						}
	
						workers[players][0] = new InputWorker(sockets[players], players);
						workers[players][1] = new OutputWorker(sockets[players], players);
						workers[players][0].start();
						workers[players][1].start();
						players++;
						state.setPlayers(players);
						
						System.out.println("Player " + players + " connected!");
					} else {
						try {
							synchronized(inQueue) {
								inQueue.wait();
							}
						} catch (InterruptedException e) {}
						processQueue();
					}
				}
			} catch (IOException e) {
				handleError(e);
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
			processQueue();

			game.setPlayers(players);
			game.start();
			
			for (int i = 0; i < players; i++) {
				long seed = new Date().getTime() ^ starter.nextLong();
				RNGs[i].setSeed(seed);
				synchronized(outQueue[i]) {
					outQueue[i].addElement(Action.start(new Action.State(state, seed)));
				}
			}

			while (!ended) {
				processQueue();
				synchronized(inQueue) {
					try {
						inQueue.wait();
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}

		public void gameOver() {
			// TODO Auto-generated method stub
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
				try {
					in.close();
				} catch (IOException e2) {
					handleError(e2);
				}
				handleError(e);
				return;
			}
			while (true) {
				try {
					Object received = in.readObject();

					if (received == null || received.getClass() != Action[].class) {
						continue;
					}
					
					synchronized(inQueue) {
						inQueue.addElement(new ClientActionSet((Action[]) received, id));
						inQueue.notify();
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
				try {
					out.close();
				} catch (IOException e2) {
					handleError(e2);
				}
				handleError(e);
				return;
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
							try {
								out.close();
							} catch (IOException e2) {
								handleError(e2);
							}
							handleError(e);
							return;
						}

						outQueue[id].removeAllElements();
					} else {
						try {
							outQueue[id].wait();
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
	
	private static class ServerGameAdapter implements ProtocolInterface {
		private Vector inQueue = new Vector();
		private Vector outQueue = new Vector();

		public void open(ErrorHandler errorHandler) {}

		public void close() throws Exception {}

		public Action next() {
			Action next = null;
			if (inQueue.size() > 0) {
				next = (Action)inQueue.firstElement();
				inQueue.removeElementAt(0);
			}
			return next;
		}

		public void queue(Action a) {
			outQueue.addElement(a);
		}
		
		void add(Action a) {
			inQueue.addElement(a);
		}

		public void send() {}
		
		public void flush() {
			inQueue.removeAllElements();
		}
		
		public void drain() {
			outQueue.removeAllElements();
		}
		
		Action[] getQueuedActions() {
			Action[] acts = new Action[outQueue.size()];
			outQueue.copyInto(acts);
			return acts;
		}
	}
	
	private static class ClientActionSet {
		private Action[] acts;
		private int client;
		
		ClientActionSet(Action[] acts, int client) {
			this.acts = acts;
			this.client = client;
		}
		
		int size() {
			return acts.length;
		}
		
		Action getAction(int index) {
			return acts[index];
		}
		
		int getClient() {
			return client;
		}
	}

	public static void main(String[] args) {
		try {
			int port = 23770;
			if (args.length > 0) {
				port = Integer.parseInt(args[0]);
			}
			new UnoServer(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
