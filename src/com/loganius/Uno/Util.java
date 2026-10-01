package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;

public class Util {
	private static Font scaledFont = new Font("Arial", Font.PLAIN, 20);
	private static Font smallFont = new Font("Arial", Font.PLAIN, 10);
	private static double scaleFactor = 1;
	public static final Color WHITE = new Color(255, 255, 255);
	public static final Color BLACK = new Color(0, 0, 0);

	private static ComponentAdapter ResizeListener = new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
			e.getComponent().setFont(scaledFont);
		}

		public void componentMoved(ComponentEvent e) {
			e.getComponent().setFont(scaledFont);
		}

		public void componentShown(ComponentEvent e) {
			e.getComponent().setFont(scaledFont);
		}
	};

	private static ComponentAdapter SmallResizeListener = new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
			e.getComponent().setFont(smallFont);
		}

		public void componentMoved(ComponentEvent e) {
			e.getComponent().setFont(smallFont);
		}

		public void componentShown(ComponentEvent e) {
			e.getComponent().setFont(smallFont);
		}
	}; 

	public static Image rotate(Image img, int deg) {
		RotateFilter rotate = new RotateFilter(deg * Math.PI / 180);
		ImageProducer producer = new FilteredImageSource(img.getSource(), rotate);
		return bufferImage(Toolkit.getDefaultToolkit().createImage(producer));
	}
	
	public static Image getImage(URL path) {
		return bufferImage(Toolkit.getDefaultToolkit().getImage(path));
	}
	
	public static Image bufferScaledImage(Image img, int width, int height) {
		return bufferImage(img.getScaledInstance(width, height, Image.SCALE_FAST));
	}
	
	public static Image bufferImage(Image img) {
		PixelGrabber grabber;
		MemoryImageSource bufferedImage;
		int[] buf;
		int width;
		int height;
		while ((width = img.getWidth(null)) == -1 || (height = img.getHeight(null)) == -1);
		buf = new int[width * height];
		grabber = new PixelGrabber(img, 0, 0, width, height, buf, 0, width);
		try {
			grabber.grabPixels();
		} catch (InterruptedException e) {
			return null;
		}
		bufferedImage = new MemoryImageSource(width, height, buf, 0, width);
		return Toolkit.getDefaultToolkit().createImage(bufferedImage);
	}
	
	public static URL getResource(String path) {
		return Util.class.getResource(path);
	}
	
	public static void onResize(int width, int height) {
		scaleFactor = (double)height / 480;
		scaledFont = new Font("Arial", Font.PLAIN, (int)(20 * scaleFactor));
		smallFont = new Font("Arial", Font.PLAIN, (int)(10 * scaleFactor));
	}
	
	public static double getScaleFactor() {
		return scaleFactor;
	}
	
	public static double scale(double val) {
		return val*scaleFactor;
	}
	
	public static Font getScaledFont() {
		return scaledFont;
	}
	
	public static Font getSmallFont() {
		return smallFont;
	}
	
	public static ComponentListener getTextResizeListener() {
		return ResizeListener;
	}
	
	public static ComponentListener getSmallTextResizeListener() {
		return SmallResizeListener;
	}
	
	public static void commonComponentInit(Component comp, Color color) {
		comp.setForeground(color);
		comp.setFont(Util.getScaledFont());
		comp.addComponentListener(Util.getTextResizeListener());
	}
	
	public static Rectangle scaleAndCrop(Rectangle original, Rectangle bounds) {
		double widthRatio = (double)bounds.width / original.width;
		double heightRatio = (double)bounds.height / original.height;
		if (widthRatio > heightRatio) {
			original.width *= widthRatio;
			original.height *= widthRatio;
			original.y = (bounds.height - original.height) / 2;
		} else {
			original.width *= heightRatio;
			original.height *= heightRatio;
			original.x = (bounds.width - original.width) / 2;
		}
		return original;
	}
	
	public static String[] splitBy(String src, String split) {
		String[] result = new String[2];
		int index;
		if ((index = src.indexOf(split)) != -1) {
			result[0] = src.substring(0, index);
			result[1] = src.substring(index + 1);
		} else {
			result[0] = src;
			result[1] = null;
		}
		return result;
	}
	
	// TODO: Make this a layout manager?
	public static class Centering extends ComponentAdapter {
		private Component child;
		
		Centering(Component child) {
			this.child = child;
		}
		
		public void componentResized(ComponentEvent e) {
			Rectangle bounds = e.getComponent().getBounds();
			child.setBounds(bounds.width / 4, bounds.height / 4, bounds.width / 2, bounds.height / 2);
			e.getComponent().validate();
		}
	}
}
