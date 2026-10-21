package com.loganius.Uno;

import java.awt.Image;
import java.io.IOException;

class DefaultResourcePack extends ResourcePack {
	private Theme defaultTheme = new Theme();

	public String getName() {
		return "Default";
	}

	public String getAuthor() {
		return "Loganius";
	}

	public String getDescription() {
		return "The default look of Java Uno.";
	}

	public Image getIcon() {
		return defaultIcon;
	}

	public Theme produce(Theme current) throws IOException {
		return defaultTheme;
	}

}
