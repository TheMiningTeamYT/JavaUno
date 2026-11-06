package com.loganius.Uno;

interface SimpleCard {
	int getID();
	int getType();
	boolean getPlayable();
	int compareTo(int otherType);
}
