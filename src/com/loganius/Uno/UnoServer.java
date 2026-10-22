package com.loganius.Uno;

import java.net.*;
import java.io.*;
import java.util.Vector;
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
	private Thread gameWorker = null;
	private volatile boolean start = false;
	private volatile int players = 0;
	private volatile boolean ended = false;
	
	public UnoServer(int port) throws IOException {
		server = new ServerSocket(port, 4, InetAddress.getByName("0.0.0.0"));
		server.setSoTimeout(500);
		gameWorker = new GameWorker();
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
	
	private class GameWorker extends Thread {
		GameState state = new GameState();
		public void processQueue() {
			GameState newState = new GameState(state);
			synchronized(inQueue) {
				while (inQueue.size() > 0) {
					ClientActionSet actionSet = (ClientActionSet)inQueue.firstElement();
					int client = actionSet.getClient();
					for (int i = 0; i < actionSet.size(); i++) {
						Action a = actionSet.getAction(i);
						boolean send = true;
						
						// System.out.println("Received type " + a.getType() + " from " + client);
						switch (a.getType()) {
							case Action.SEND_STATE:
								synchronized(outQueue[client]) {
									outQueue[client].addElement(Action.setState(state));
								}
								continue;
							case Action.HELLO: {
								Object received = a.getArgument();
								if (received.getClass() != String.class) {
									continue;
								}
								names[client] = (String)received;
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
									start = true;
								}
								continue;
							case Action.CREATE_SLOT:
							case Action.UPDATE_SLOT:
								send = false;
							default:
								newState.applyAction(a);
								break;
						}
						if (a.verify(newState)) {
							state = newState;
							newState = new GameState(state);
							if (send) {
								for (int j = 0; j < players; j++) {
									if (client != j) {
										synchronized(outQueue[j]) {
											outQueue[j].addElement(a);
										}
									}
								}
							}
						} else {
							synchronized(outQueue[client]) {
								outQueue[client].addElement(Action.setState(state));
							}
							for (int j = 1; j < inQueue.size(); j++) {
								if (((ClientActionSet)inQueue.elementAt(j)).getClient() == client) {
									inQueue.removeElementAt(j--);
								}
							}
							newState = new GameState(state);
							System.out.println("Desynced!");
							System.out.println("Last action received: " + a.getType());
						}
					}
					inQueue.removeElementAt(0);
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

			for (int i = 0; i < players; i++) {
				synchronized(outQueue[i]) {
					outQueue[i].addElement(Action.startGame());
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
	
	private static class ClientActionSet {
		private Action[] acts;
		private int client;
		
		ClientActionSet(Action[] acts, int client) {
			this.acts = acts;
			this.client = client;
		}
		
		Action[] getActs() {
			return acts;
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
