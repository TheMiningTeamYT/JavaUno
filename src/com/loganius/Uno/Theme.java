package com.loganius.Uno;

import java.awt.Color;
import java.awt.Image;

public class Theme {
	private String name;
	private CachedImage[] unoButton;
	private CachedImage[] drawButton;
	private Image logo;

	private Image mainScreenBackground;
	private Image gameBackground;

	private Deck deck;

	private Color mainScreenText;
	private Color mainScreenAccent;
	private Color gameText;
	private Color gameAccent;
	
	Theme() {
		this.name = "Default";
		this.unoButton = new CachedImage[] {
				new CachedImage(Util.getImage(Util.getResource("Assets/uno.gif")), 0, 120, 120),
				new CachedImage(Util.getImage(Util.getResource("Assets/uno_down.gif")), 0, 120, 120),
		};
		this.drawButton = new CachedImage[] {
				new CachedImage(Util.getImage(Util.getResource("Assets/draw.gif")), 0, 120, 120),
				new CachedImage(Util.getImage(Util.getResource("Assets/draw_down.gif")), 0, 120, 120),
		};
		this.logo = Util.getImage(Util.getResource("Assets/UnoLogo.gif"));
		this.mainScreenBackground = Util.getImage(Util.getResource("Assets/background.jpg"));
		this.gameBackground = this.mainScreenBackground;
		this.deck = new Deck();
		this.mainScreenText = Util.WHITE;
		this.mainScreenAccent = new Color(151, 213, 252);
		this.gameText = Util.WHITE;
		this.gameAccent = new Color(105, 0, 204);
	}
	
	Theme(Theme fallback, String name, Image[] unoButton, Image[] drawButton, Image logo,
			Image mainScreenBackground, Image gameBackground, Deck deck,
			Color mainScreenText, Color mainScreenAccent, Color gameText,
			Color gameAccent) {
		this.name = name;
		this.unoButton = (unoButton == null) ? fallback.unoButton : new CachedImage[] {
				new CachedImage(unoButton[0], 0, 120, 120),
				new CachedImage(unoButton[1], 0, 120, 120),
		};
		this.drawButton = (drawButton == null) ? fallback.drawButton : new CachedImage[] {
				new CachedImage(drawButton[0], 0, 120, 120),
				new CachedImage(drawButton[1], 0, 120, 120),
		};
		this.logo = (logo == null) ? fallback.logo : logo;
		this.mainScreenBackground = (mainScreenBackground == null) ? fallback.mainScreenBackground : mainScreenBackground;
		this.gameBackground = (gameBackground == null) ? fallback.gameBackground: gameBackground;
		this.deck = (deck == null) ? fallback.deck : deck;
		this.mainScreenText = (mainScreenText == null) ? fallback.mainScreenText : mainScreenText;
		this.mainScreenAccent = (mainScreenAccent == null) ? fallback.mainScreenAccent : mainScreenAccent;
		this.gameText = (gameText == null) ? fallback.gameText : gameText;
		this.gameAccent = (gameAccent == null) ? fallback.gameAccent : gameAccent;
	}
	
	CachedImage[] getUnoButton() {
		return (CachedImage[]) unoButton.clone();
	}
	
	CachedImage[] getDrawButton() {
		return (CachedImage[]) drawButton.clone();
	}
	
	Image getLogo() {
		return logo;
	}
	
	Image getMainScreenBackground() {
		return mainScreenBackground;
	}
	
	Image getGameBackground() {
		return gameBackground;
	}
	
	Deck getDeck() {
		return deck;
	}
	
	Color getMainScreenText() {
		return mainScreenText;
	}
	
	Color getMainScreenAccent() {
		return mainScreenAccent;
	}
	
	Color getGameText() {
		return gameText;
	}
	
	Color getGameAccent() {
		return gameAccent;
	}
	
	String getName() {
		return name;
	}
}
