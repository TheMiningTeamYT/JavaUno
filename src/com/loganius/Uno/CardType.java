package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// TODO: Figure out how to send a card type over the network in a way I'm happy with.
// TODO: Pre-scale images for better performance?
class CardType {
	static final class Color {
		static final int RED = 0;
		static final int YELLOW = 1;
		static final int GREEN = 2;
		static final int BLUE = 3;
		static final int WILD = 4;
	}
	
	static final class Value {
		static final int PLUS2 = -2;
		static final int SKIP = -3;
		static final int REVERSE = -4;
		static final int CHANGE_COLOR = -5;
		static final int DRAW_4 = -6;
	}

	protected CachedImage[] face;
	protected CachedImage[] back;
	private int width = -1;
	private int height = -1;
	protected int color;
	protected int value;
	private int weight;

	CardType(Image front, CachedImage[] back, int color, int value, int weight) {
		face = generateImageList(front);
		this.back = back;
		this.color = color;
		this.value = value;
		this.weight = weight;
	}
	
	CardType(CardType parent) {
		face = parent.face;
		back = parent.back;
		width = parent.width;
		height = parent.height;
		color = parent.color;
		value = parent.value;
		weight = parent.weight;
	}
	
	Image getFace(int orientation) {
		return face[orientation / 90].get();
	}
	
	Image getBack(int orientation) {
		return back[orientation / 90].get();
	}
	
	void useFace(int orientation) {
		face[orientation / 90].using();
		face[0].using();
		face[0].update();
	}
	
	void stopUsingFace(int orientation) {
		face[orientation / 90].stopUsing();
		face[0].stopUsing();
	}
	
	void useBack(int orientation) {
		back[orientation / 90].using();
		face[0].using();
		face[0].update();
	}
	
	void stopUsingBack(int orientation) {
		back[orientation / 90].stopUsing();
		face[0].stopUsing();
	}
	
	static CachedImage[] generateImageList(Image src) {
		return new CachedImage[] {
			new CachedImage(src, 0, 80, 120),
			new CachedImage(Util.rotate(src, 90), 90, 80, 120),
			new CachedImage(Util.rotate(src, 180), 180, 80, 120),
			new CachedImage(Util.rotate(src, 270), 270, 80, 120),
		};
	}
	
	int getColor() {
		return color;
	}
	
	int getValue() {
		return value;
	}
	
	int getWeight() {
		return weight;
	}
	
	boolean canBeSkinned() {
		return true;
	}

	/**
	 * Take an action when the card is played.
	 * @param parent
	 * @return
	 */
	void played(Card parent) {
		parent.getGame().onTurn();
	};
	
	static final class Skip extends CardType {
		Skip(Image front, CachedImage[] back, int color) {
			super(front, back, color, Value.SKIP, 2);
		}
		
		void played(Card parent) {
			// TODO: Maybe play an animation for skipping?
			parent.getGame().onTurn();
			parent.getGame().onTurn();
		};
	}
	
	static final class Reverse extends CardType {
		Reverse(Image front, CachedImage[] back, int color) {
			super(front, back, color, Value.REVERSE, 2);
		}
		
		void played(Card parent) {
			// TODO: Maybe play an animation for reverse?
			parent.getGame().reverse();
			parent.getGame().onTurn();
		};
	}
	
	static final class DrawTwo extends CardType {
		DrawTwo(Image front, CachedImage[] back, int color) {
			super(front, back, color, Value.PLUS2, 2);
		}
		
		void played(Card parent) {
			// TODO: Maybe play an animation for drawing?
			Game game = parent.getGame();
			game.drawToHand(1);
			game.drawToHand(1);
			game.onTurn();
			game.onTurn();
		};
	}
	
	static final class Skinned extends CardType {
		private CardType parent;

		Skinned(CardType parent, CachedImage[] face, CachedImage back[]) {
			super(parent);
			if (parent instanceof Skinned) {
				this.parent = ((Skinned) parent).parent;
			} else {
				this.parent = parent;
			}
			if (face != null) {
				this.face = face;
			}
			if (back != null) {
				this.back = back;
			}
		}
		
		void played(Card parent) {
			this.parent.played(parent);
		}
	}
}
