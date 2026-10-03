package com.loganius.Uno;
import java.awt.*;
import java.net.URL;

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

	protected Image[] originalFace;
	protected Image[] originalBack;
	protected Image[] face;
	protected Image[] back;
	private int width = -1;
	private int height = -1;
	protected int color;
	protected int value;
	protected boolean drawable;

	CardType(URL front, Image[] back, int color, int value, boolean drawable) {
		originalFace = generateImageList(Util.getImage(front));
		face = (Image[]) originalFace.clone();
		originalBack = back;
		this.back = (Image[])back.clone();
		this.color = color;
		this.value = value;
		this.drawable = drawable;
	}
	
	CardType(CardType parent) {
		originalFace = parent.originalFace;
		originalBack = parent.originalBack;
		face = (Image[]) parent.face.clone();
		back = (Image[]) parent.back.clone();
		width = parent.width;
		height = parent.height;
		color = parent.color;
		value = parent.value;
		drawable = parent.drawable;
	}
	
	Image[] getFace() {
		if (Card.getWidth(0) == width && Card.getHeight(0) == height) {
			return face;
		}
		width = Card.getWidth(0);
		height = Card.getHeight(0);
		updateImages();
		return face;
	}
	
	Image[] getBack() {
		if (Card.getWidth(0) == width && Card.getHeight(0) == height) {
			return back;
		}
		width = Card.getWidth(0);
		height = Card.getHeight(0);
		updateImages();
		return back;
	}
	
	static Image[] generateImageList(Image src) {
		return new Image[] {
			src,
			Util.rotate(src, 90),
			Util.rotate(src, 180),
			Util.rotate(src, 270),
			
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
	
	private void updateImages() {
		for (int i = 0; i < 4; i++) {
			face[i] = Util.bufferScaledImage(originalFace[i], width, height);
			back[i] = Util.bufferScaledImage(originalBack[i], width, height);
		}
	}

	/**
	 * Take an action when the card is played.
	 * @param parent
	 * @return
	 */
	void played(Card parent) {
		parent.getGame().onTurn();
	};
	
	static final class SkipCardType extends CardType {
		SkipCardType(URL front, Image[] back, int color) {
			super(front, back, color, Value.SKIP, true);
		}
		
		void played(Card parent) {
			// TODO: Maybe play an animation for skipping?
			parent.getGame().onTurn();
			parent.getGame().onTurn();
		};
	}
	
	static final class ReverseCardType extends CardType {
		ReverseCardType(URL front, Image[] back, int color) {
			super(front, back, color, Value.REVERSE, true);
		}
		
		void played(Card parent) {
			// TODO: Maybe play an animation for reverse?
			parent.getGame().reverse();
			parent.getGame().onTurn();
		};
	}
	
	static final class DrawTwoCardType extends CardType {
		DrawTwoCardType(URL front, Image[] back, int color) {
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
