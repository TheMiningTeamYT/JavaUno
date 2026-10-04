package com.loganius.Uno;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
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

	protected CardFace[] face;
	protected CardFace[] back;
	private int width = -1;
	private int height = -1;
	protected int color;
	protected int value;
	protected boolean drawable;

	CardType(URL front, CardFace[] back, int color, int value, boolean drawable) {
		face = generateImageList(Util.getImage(front));
		this.back = back;
		this.color = color;
		this.value = value;
		this.drawable = drawable;
	}
	
	CardType(CardType parent) {
		face = (CardFace[]) parent.face.clone();
		back = (CardFace[]) parent.back.clone();
		width = parent.width;
		height = parent.height;
		color = parent.color;
		value = parent.value;
		drawable = parent.drawable;
	}
	
	Image getFace(int orientation) {
		return face[orientation / 90].get();
	}
	
	Image getBack(int orientation) {
		return back[orientation / 90].get();
	}
	
	void useFace(int orientation) {
		face[orientation / 90].using();
	}
	
	void stopUsingFace(int orientation) {
		face[orientation / 90].stopUsing();
	}
	
	void useBack(int orientation) {
		back[orientation / 90].using();
	}
	
	void stopUsingBack(int orientation) {
		back[orientation / 90].stopUsing();
	}
	
	static CardFace[] generateImageList(Image src) {
		return new CardFace[] {
			new CardFace(src, 0),
			new CardFace(Util.rotate(src, 90), 90),
			new CardFace(Util.rotate(src, 180), 180),
			new CardFace(Util.rotate(src, 270), 270),
		};
	}
	
	int getColor() {
		return color;
	}
	
	int getValue() {
		return value;
	}
	
	boolean getDrawable() {
		return drawable;
	}

	/**
	 * Take an action when the card is played.
	 * @param parent
	 * @return
	 */
	void played(Card parent) {
		parent.getGame().onTurn();
	};
	
	static class CardFace {
		private Image original;
		private Image buffered = null;
		private int users = 0;
		private int width = -1;
		private int height = -1;
		private int orientation;

		CardFace(Image original, int orientation) {
			this.original = original;
			this.orientation = orientation;
		}
		
		Image get() {
			update();
			return buffered;
		}
		
		void update() {
			if (buffered == null || width != Card.getWidth(orientation) || height != Card.getHeight(orientation)) {
				width = Card.getWidth(orientation);
				height = Card.getHeight(orientation);
				buffered = Util.bufferScaledImage(original, width, height);
			}
		}
		
		void using() {
			users++;
			System.out.println("Users using " + getClass().getName() + ": " + users);
			update();
		}
		
		void stopUsing() {
			users--;
			if (users <= 0) {
				// Because cards often stop using and then immediately start using images,
				// we wait a bit before actually deleting the image for real.
				final Timer cleanupDelay = new Timer(1000, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (users <= 0) {
							buffered = null;
							users = 0;
						}
					}
				});
				cleanupDelay.setRepeats(false);
				cleanupDelay.start();
			}
		}
	}
	
	static final class SkipCardType extends CardType {
		SkipCardType(URL front, CardFace[] back, int color) {
			super(front, back, color, Value.SKIP, true);
		}
		
		void played(Card parent) {
			// TODO: Maybe play an animation for skipping?
			parent.getGame().onTurn();
			parent.getGame().onTurn();
		};
	}
	
	static final class ReverseCardType extends CardType {
		ReverseCardType(URL front, CardFace[] back, int color) {
			super(front, back, color, Value.REVERSE, true);
		}
		
		void played(Card parent) {
			// TODO: Maybe play an animation for reverse?
			parent.getGame().reverse();
			parent.getGame().onTurn();
		};
	}
	
	static final class DrawTwoCardType extends CardType {
		DrawTwoCardType(URL front, CardFace[] back, int color) {
			super(front, back, color, Value.PLUS2, true);
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
}
