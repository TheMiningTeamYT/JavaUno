package com.loganius.Uno;

import java.util.zip.*;

import java.io.IOException;
import java.io.InputStream;

import java.util.Hashtable;
import java.util.Properties;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Color;

class ZIPResourcePack extends ResourcePack {
	private static final String[] cardNames = {
		"back",
		"red_0",
		"red_1",
		"red_2",
		"red_3",
		"red_4",
		"red_5",
		"red_6",
		"red_7",
		"red_8",
		"red_9",
		"red_plus2",
		"red_skip",
		"red_reverse",
		"yellow_0",
		"yellow_1",
		"yellow_2",
		"yellow_3",
		"yellow_4",
		"yellow_5",
		"yellow_6",
		"yellow_7",
		"yellow_8",
		"yellow_9",
		"yellow_plus2",
		"yellow_skip",
		"yellow_reverse",
		"green_0",
		"green_1",
		"green_2",
		"green_3",
		"green_4",
		"green_5",
		"green_6",
		"green_7",
		"green_8",
		"green_9",
		"green_plus2",
		"green_skip",
		"green_reverse",
		"blue_0",
		"blue_1",
		"blue_2",
		"blue_3",
		"blue_4",
		"blue_5",
		"blue_6",
		"blue_7",
		"blue_8",
		"blue_9",
		"blue_plus2",
		"blue_skip",
		"blue_reverse",
		"wild_change_color",
		"red_change_color",
		"yellow_change_color",
		"green_change_color",
		"blue_change_color",
		"wild_draw4",
		"red_draw4",
		"yellow_draw4",
		"green_draw4",
		"blue_draw4"
	};
	
	private static int DECORATIVE = 0;
	private static int SCRIPTED = 1;
	private static int JAVA = 2;

	private StreamAdapter source;
	private int type;
	private Image icon;
	private Properties properties = new Properties();
	
	ZIPResourcePack(StreamAdapter source) throws IOException {
		Hashtable entries = new Hashtable();
		Hashtable files = new Hashtable();
		InputStream in = source.openStream();
		ZipInputStream zip = new ZipInputStream(in);
		ZipEntry entry;

		this.source = source;
		while ((entry = zip.getNextEntry()) != null) {
			entries.put(entry.getName(), entry);
			if (entry.getName().equalsIgnoreCase("pack.properties")) {
				properties.load(zip);
			} else {
				files.put(entry.getName(), Util.getFile(zip, (int)entry.getSize()));
			}
			zip.closeEntry();
		}
		
		zip.close();
		in.close();
		
		if (entries.get("pack.properties") == null) {
			throw new IllegalArgumentException("ZIP file did not contain a resource pack descriptor pack.properties!");
		}
		
		if (!verify(entries)) {
			throw new IllegalArgumentException("ZIP file is not a valid resource pack!");
		}
		
		if (properties.getProperty("icon") != null && files.get(properties.getProperty("icon")) != null) {
			icon = Toolkit.getDefaultToolkit().createImage((byte[]) files.get(properties.getProperty("icon")));
		} else {
			icon = defaultIcon;
		}
	}
	
	private boolean verify(Hashtable files) {
		try {
			String type = properties.getProperty("type");
			String name = properties.getProperty("name");
			
			if (name == null) {
				return false;
			}
			
			if (type.equalsIgnoreCase("decorative")) {
				this.type = DECORATIVE;
				for (int i = 0; i < cardNames.length; i++) {
					String path = properties.getProperty(cardNames[i]);
					ZipEntry file;

					if (path == null) {
						continue;
					}
					
					file = (ZipEntry) files.get(path);

					if (file == null || file.isDirectory() || file.getSize() == 0) {
						return false;
					}
				}
				return true;
			}

			if (type.equalsIgnoreCase("scripted")) {
				throw new RuntimeException("Scripted type is Unimplemented!");
			}
			
			if (type.equalsIgnoreCase("java")) {
				throw new RuntimeException("Java type is Unimplemented!");
			}
			
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public String getName() {
		return properties.getProperty("name");
	}
	
	public String getDescription() {
		String description = properties.getProperty("description");
		return (description == null) ? "A resource pack for Uno." : description;
	}
	
	public String getAuthor() {
		String author = properties.getProperty("author");
		return (author == null) ? "Robert Walpole" : author;
	}
	
	public Image getIcon() {
		return icon;
	}
	
	private Color getAsColor(String property) {
		try {
			String[] components = Util.splitBy(properties.getProperty(property), " ");
			return new Color(Integer.parseInt(components[0]), Integer.parseInt(components[1]), 
				Integer.parseInt(components[2]));
		} catch (Exception e) {
			return null;
		}
	}
	
	public Theme produce() throws IOException {
		InputStream in = source.openStream();
		ZipInputStream zip = new ZipInputStream(in);
		ZipEntry entry;
		Hashtable files = new Hashtable();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		byte[] file;
		String path;

		Deck deck;
		Image unoButtonUp = null;
		Image unoButtonDown = null;
		Image drawButtonUp = null;
		Image drawButtonDown = null;
		Image logo = null;
		Image mainScreenBackground = null;
		Image gameBackground = null;
		Color mainScreenText = getAsColor("main_text");
		Color mainScreenAccent = getAsColor("main_accent");
		Color gameText = getAsColor("game_text");
		Color gameAccent = getAsColor("game_accent");
		
		try {
			while ((entry = zip.getNextEntry()) != null) {
				files.put(entry.getName(), Util.getFile(zip, (int)entry.getSize()));
				zip.closeEntry();
			}
		} catch (IOException e) {
			try {
				zip.close();
				in.close();
			} catch (IOException e2) {
				throw e2;
			}
			throw e;
		}
		
		try {
			zip.close();
			in.close();
		} catch (IOException e) {
			throw e;
		}
		
		if (type == DECORATIVE) {
			Image[] cards = new Image[cardNames.length];
			for (int i = 0; i < cardNames.length; i++) {
				if ((path = properties.getProperty(cardNames[i])) != null && (file = (byte[])files.get(path)) != null) {
					cards[i] = toolkit.createImage(file);
				}
			}
			deck = new Deck(cards);
		} else {
			throw new RuntimeException("Unimplemented!");
		}
		
		if ((path = properties.getProperty("uno_up")) != null && (file = (byte[])files.get(path)) != null) {
			unoButtonUp = toolkit.createImage(file);
		}
		
		if ((path = properties.getProperty("uno_down")) != null && (file = (byte[])files.get(path)) != null) {
			unoButtonDown = toolkit.createImage(file);
		}
		
		if ((path = properties.getProperty("draw_up")) != null && (file = (byte[])files.get(path)) != null) {
			drawButtonUp = toolkit.createImage(file);
		}
		
		if ((path = properties.getProperty("draw_down")) != null && (file = (byte[])files.get(path)) != null) {
			drawButtonDown = toolkit.createImage(file);
		}
		
		if ((path = properties.getProperty("logo")) != null && (file = (byte[])files.get(path)) != null) {
			logo = toolkit.createImage(file);
		}
		
		if ((path = properties.getProperty("main_background")) != null && (file = (byte[])files.get(path)) != null) {
			mainScreenBackground = toolkit.createImage(file);
		}
		
		if ((path = properties.getProperty("game_background")) != null && (file = (byte[])files.get(path)) != null) {
			gameBackground = toolkit.createImage(file);
		}

		return new Theme(unoButtonUp, unoButtonDown, drawButtonUp, drawButtonDown, 
			logo, mainScreenBackground, gameBackground, deck, mainScreenText,
			mainScreenAccent, gameText, gameAccent); 
	}
}
