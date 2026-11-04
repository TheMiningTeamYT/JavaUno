package com.loganius.Uno;

public interface ProtocolInterface {
	void open(ErrorHandler errorHandler);
	void close() throws Exception;
	Action next();
	void queue(Action a);
	void send();
	void flush();
	void drain();
}
