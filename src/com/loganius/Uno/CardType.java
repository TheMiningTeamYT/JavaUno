package com.loganius.Uno;

// TODO: Figure out how to send a card type over the network in a way I'm happy with.
// TODO: Pre-scale images for better performance?
public class CardType {
	public static final class Color {
		static final int RED = 0;
		static final int YELLOW = 1;
		static final int GREEN = 2;
		static final int BLUE = 3;
		static final int WILD = 4;
	}
	
	public static final class Value {
		static final int PLUS2 = -2;
		static final int SKIP = -3;
		static final int REVERSE = -4;
		static final int CHANGE_COLOR = -5;
		static final int DRAW_4 = -6;
	}

	protected CardSkin skin;
	protected int color;
	protected int value;
	private int weight;

	public CardType(CardSkin skin, int color, int value, int weight) {
		this.skin = skin;
		this.color = color;
		this.value = value;
		this.weight = weight;
	}
	
	public CardType(CardType parent) {
		skin = parent.skin;
		color = parent.color;
		value = parent.value;
		weight = parent.weight;
	}

	public int getColor() {
		return color;
	}
	
	public int getValue() {
		return value;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public boolean canBeSkinned() {
		return true;
	}
	
	public CardSkin getSkin() {
		return skin;
	}

	/**
	 * Take an action when the card is played.
	 * (Executed on the client)
	 * @param parent
	 * @return
	 */
	void played(Card parent) {};
	
	/**
	 * Execute the logic that occurs when the card is played.
	 * (Executed on the server)
	 */
	void run(Card parent) {
		parent.getGame().onTurn();
	}
	
	public static final class Skip extends CardType {
		Skip(CardSkin skin, int color) {
			super(skin, color, Value.SKIP, 2);
		}
		
		void run(Card parent) {
			// TODO: Maybe play an animation for skipping?
			parent.getGame().onTurn();
			parent.getGame().onTurn();
		};
	}
	
	public static final class Reverse extends CardType {
		Reverse(CardSkin skin, int color) {
			super(skin, color, Value.REVERSE, 2);
		}
		
		void run(Card parent) {
			// TODO: Maybe play an animation for reverse?
			parent.getGame().reverse();
			parent.getGame().onTurn();
		};
	}
	
	public static final class DrawTwo extends CardType {
		DrawTwo(CardSkin skin, int color) {
			super(skin, color, Value.PLUS2, 2);
		}
		
		void run(Card parent) {
			// TODO: Maybe play an animation for drawing?
			Game game = parent.getGame();
			game.drawToHand(1);
			game.drawToHand(1);
			game.onTurn();
			game.onTurn();
		};
	}
	
	public static final class Skinned extends CardType {
		private CardType parent;

		Skinned(CardType parent, CachedImage[] face, CachedImage back[]) {
			super(parent);
			if (parent instanceof Skinned) {
				this.parent = ((Skinned) parent).parent;
			} else {
				this.parent = parent;
			}
			if (parent.canBeSkinned()) {
				skin = new CardSkin(parent.getSkin(), face, back);
			}
		}
		
		void played(Card parent) {
			this.parent.played(parent);
		}
		
		void run(Card parent) {
			this.parent.run(parent);
		}
	}
}
