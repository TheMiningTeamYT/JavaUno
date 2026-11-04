package com.loganius.Uno;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

public class NetworkClientAdapter implements ProtocolInterface {
	private Socket socket = null;
	private volatile Vector inQueue = new Vector();
	private volatile Vector outQueue = new Vector();
	private Thread inThread = null;
	private Thread outThread = null;
	private String server;
	private ErrorHandler handler = null;
	private int port;
	
	NetworkClientAdapter(String server, int port) {
		this.server = server;
		this.port = port;
	}

	public void open(ErrorHandler errorHandler) {
		handler = errorHandler;
		try {
			socket = new Socket(server, port);
		} catch (Exception e) {
			handler.handleError(e);
			return;
		}
		
		inThread = new InputWorker();
		outThread = new OutputWorker();
		
		inThread.start();
		outThread.start();
	}

	public void close() throws Exception {
		if (inThread != null) {
			inThread.interrupt();
			inThread = null;
		}
		if (outThread != null) {
			outThread.interrupt();
			outThread = null;
		}
		if (socket != null) {
			socket.close();
			socket = null;
		}
	}

	public Action next() {
		Action a;
		synchronized(inQueue) {
			if (inQueue.size() == 0) {
				return null;
			}
			a = (Action)inQueue.firstElement();
			inQueue.removeElementAt(0);
		}
		return a;
	}

	public void queue(Action a) {
		synchronized(outQueue) {
			outQueue.addElement(a);
		}
	}

	public void send() {
		synchronized(outQueue) {
			if (outQueue.size() > 0) {
				outQueue.notify();
			}
		}
	}

	public void flush() {
		synchronized(outQueue) {
			outQueue.removeAllElements();
		}
	}

	public void drain() {
		synchronized(inQueue) {
			inQueue.removeAllElements();
		}
	}
	
	private void handleError(Exception e) {
		try {
			close();
			handler.handleError(e);
		} catch (Exception e2) {
			handler.handleError(e2);
		}
	}
	
	private synchronized void notifyListeners() {
		this.notify();
	}
	
	private class InputWorker extends Thread {
		public void run() {
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				handleError(e);
				return;
			}
			while (true) {
				try {
					Object received = in.readObject();
					if (received == null || !(received instanceof Action[])) {
						continue;
					}
					synchronized(inQueue) {
						Action[] acts = (Action[])received;
						for (int i = 0; i < acts.length; i++) {
							if (acts[i] != null) {
								inQueue.addElement(acts[i]);
							}
						}
					}
					notifyListeners();
				} catch (Exception e) {
					try {
						in.close();
						handleError(e);
					} catch (IOException e2) {
						handleError(e2);
					}
					return;
				}
			}
		}
	}
	
	private class OutputWorker extends Thread {
		public void run() {
			ObjectOutputStream out;
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				handleError(e);
				return;
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
								handleError(e);
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
