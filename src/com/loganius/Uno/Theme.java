package com.loganius.Uno;

import java.awt.Color;
import java.awt.Image;

public class Theme {
	private CachedImage unoButtonUp;
	private CachedImage unoButtonDown;
	private CachedImage drawButtonUp;
	private CachedImage drawButtonDown;
	private Image logo;

	private Image mainScreenBackground;
	private Image gameBackground;

	private Deck deck;

	private Color mainScreenText;
	private Color mainScreenAccent;
	private Color gameText;
	private Color gameAccent;
	
	Theme() {
		this(null, null, null, null, null, null, null, new Deck(), null, null, null, null);
	}
	
	Theme(Image unoButtonUp, Image unoButtonDown, Image drawButtonUp, Image drawButtonDown, 
			Image logo, Image mainScreenBackground, Image gameBackground, Deck deck, 
			Color mainScreenText, Color mainScreenAccent, Color gameText, Color gameAccent) {
		this.unoButtonUp = (unoButtonUp == null) ? new CachedImage(Util.getImage(Util.getResource("Assets/uno.gif")), 0, 120, 120) :
			new CachedImage(unoButtonUp, 0, 120, 120);
		this.unoButtonDown = (unoButtonDown == null) ? new CachedImage(Util.getImage(Util.getResource("Assets/uno_down.gif")), 0, 120, 120) :
			new CachedImage(unoButtonDown, 0, 120, 120);
		this.drawButtonUp = (drawButtonUp == null) ? new CachedImage(Util.getImage(Util.getResource("Assets/draw.gif")), 0, 120, 120) :
			new CachedImage(drawButtonUp, 0, 120, 120);
		this.drawButtonDown = (drawButtonDown == null) ? new CachedImage(Util.getImage(Util.getResource("Assets/draw_down.gif")), 0, 120, 120) :
			new CachedImage(drawButtonDown, 0, 120, 120);
		this.logo = (logo == null) ? Util.getImage(Util.getResource("Assets/UnoLogo.gif")) : logo;
		this.mainScreenBackground = (mainScreenBackground == null) ? Util.getImage(Util.getResource("Assets/background.jpg")) : mainScreenBackground;
		this.gameBackground = (gameBackground == null || mainScreenBackground == gameBackground) ? this.mainScreenBackground : gameBackground;
		this.deck = deck;
		this.mainScreenText = (mainScreenText == null) ? Util.WHITE : mainScreenText;
		this.mainScreenAccent = (mainScreenAccent == null) ? new Color(151, 213, 252) : mainScreenAccent;
		this.gameText = (gameText == null) ? Util.WHITE : gameText;
		this.gameAccent = (gameAccent == null) ? new Color(105, 0, 204) : gameAccent;
	}
	
	CachedImage[] getUnoButton() {
		return new CachedImage[] {unoButtonUp, unoButtonDown};
	}
	
	CachedImage[] getDrawButton() {
		return new CachedImage[] {drawButtonUp, drawButtonDown};
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
}
