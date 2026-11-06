package com.loganius.Uno;

import java.io.Serializable;

import java.util.Vector;
import java.util.Hashtable;

class SimpleHand implements Serializable {
	protected Vector cards = new Vector();
	private Hashtable cardsByID = new Hashtable();
	protected boolean playable = false;
	
	public Object clone() {
		SimpleHand result = new SimpleHand();
		result.cards = (Vector) cards.clone();
		result.cardsByID = (Hashtable) cardsByID.clone();
		result.playable = playable;
		return result;
	}
	
	/**
	 * Add a card to the hand.
	 * @param card
	 */
	public void add(SimpleCard card) {
		int size = cards.size();
		int type = card.getType();
		int index = 0;
		while (index < size) {
			if (((SimpleCard)cards.elementAt(index)).compareTo(type) >= 0) {
				break;
			}
			index++;
		}
		cards.insertElementAt(card, index);
		cardsByID.put(new Integer(card.getID()), card);
	}
	
	private int findCardIndex(int type) {
		int lowerBound = 0;
		int upperBound = cards.size() - 1;
		int guess;
		int compare;
		if (upperBound == -1) {
			return -1;
		}
		guess = upperBound/2;
		while ((compare = ((SimpleCard)cards.elementAt(guess)).compareTo(type)) != 0) {
			if (upperBound == lowerBound) {
				return -1;
			}
			if (upperBound - lowerBound == 1) {
				guess = upperBound;
				lowerBound = upperBound;
			} else  {
				if (compare > 0) {
					upperBound = guess;
				} else {
					lowerBound = guess;
				}
				guess = (lowerBound + upperBound)/2;
			}
		}
		return guess;
	}

	/**
	 * Remove a card from the hand.
	 * @param card
	 */
	public void remove(SimpleCard card) {
		int type = card.getType();
		int index = findCardIndex(type);
		int numCards = cards.size();
		if (index == -1) {
			return;
		}
		
		while (index > 0) {
			if (((SimpleCard)cards.elementAt(index)).getType() != type) {
				break;
			}
			index--;
		}

		while (index < numCards) {
			SimpleCard candidate = (SimpleCard)cards.elementAt(index);
			if (candidate.getType() != type) {
				break;
			}
			if (candidate == card) {
				cards.removeElementAt(index);
				cardsByID.remove(new Integer(card.getID()));
				break;
			}
			index++;
		}
	}

	public boolean getPlayable() {
		return playable;
	}

	public void setPlayable(boolean playable) {
		this.playable = playable;
		for (int i = 0; i < cards.size(); i++) {
			((SimpleCard)cards.elementAt(i)).setPlayable(playable);
		}
	}

	public SimpleCard getSimpleCardByType(int cardType) {
		int index = findCardIndex(cardType);
		if (index == -1) {
			return null;
		}
		return (SimpleCard)cards.elementAt(index);
	}
	
	public SimpleCard getSimpleCardByID(int ID) {
		return (SimpleCard)cardsByID.get(new Integer(ID));
	}

	public void setTypePlayable(int cardType, boolean playable) {
		for (int i = 0; i < cards.size(); i++) {
			SimpleCard card = (SimpleCard)cards.elementAt(i);
			if (card.getType() == cardType) {
				card.setPlayable(playable);
			}
		}
	}

	public boolean contains(int cardType) {
		return (findCardIndex(cardType) == -1) ? false : true;
	}

	public void removeAll() {
		cards.removeAllElements();
	}

	public int numCards() {
		return cards.size();
	}
	
	void copyInto(SimpleCard[] out) {
		cards.copyInto(out);
	}
	
	public boolean equals(Object other) {
		SimpleHand hand;
		SimpleHand thisCopy;
		int otherSize;

		if (other == null) {
			return this == null;
		}
		if (!(other instanceof SimpleHand)) {
			return false;
		}

		hand = (SimpleHand) other;

		if (this.playable != hand.playable) {
			return false;
		}
		otherSize = hand.numCards();
		
		if (numCards() != otherSize) {
			return false;
		}
		
		thisCopy = (SimpleHand) clone();
		
		for (int i = 0; i < otherSize; i++) {
			SimpleCard card = (SimpleCard) hand.cards.elementAt(i);
			int numCards = thisCopy.numCards();
			int cardType = card.getType();
			int cardIndex = thisCopy.findCardIndex(cardType);
			boolean playable = card.getPlayable();
			boolean found = false;

			if (cardIndex == -1) {
				return false;
			}

			while (cardIndex > 0) {
				if (((SimpleCard)thisCopy.cards.elementAt(cardIndex)).getType() != cardType) {
					break;
				}
				cardIndex--;
			}

			while (cardIndex < numCards) {
				SimpleCard candidate = (SimpleCard)thisCopy.cards.elementAt(cardIndex);
				if (candidate.getType() != cardType) {
					break;
				}
				if (candidate.getPlayable() == playable) {
					thisCopy.cards.removeElementAt(cardIndex);
					found = true;
					break;
				}
				cardIndex++;
			}

			if (!found) {
				return false;
			}
		}
		return true;
	}
	
	boolean check() {
		for (int i = 0; i < cards.size(); i++) {
			SimpleCard card = (SimpleCard)cards.elementAt(i);
			if (card == null) {
				return false;
			}
			if (card.getType() < 0) { 
				return false;
			}
		}
		return true;
	}
}
