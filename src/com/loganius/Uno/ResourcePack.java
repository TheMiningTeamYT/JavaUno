package com.loganius.Uno;

import java.io.IOException;
import java.awt.Image;

abstract class ResourcePack {
	protected static Image defaultIcon = Util.getImage(Util.getResource("Assets/pack.jpg"));

	abstract String getName();
	abstract String getAuthor();
	abstract String getDescription();
	abstract Image getIcon();
	abstract Theme produce() throws IOException;
}
