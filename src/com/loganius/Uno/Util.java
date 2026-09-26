package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

public class Util {
	private static Font scaledFont = new Font("Arial", Font.PLAIN, 20);
	private static double scaleFactor = 1;
	private static Util.Resizer ResizeListener = new Util.Resizer(); 

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
	
	public static ComponentListener getTextResizeListener() {
		return ResizeListener;
	}
	
	private static class Resizer extends ComponentAdapter {
		public void componentResized(ComponentEvent e) {
			e.getComponent().setFont(scaledFont);
		}

		public void componentMoved(ComponentEvent e) {
			e.getComponent().setFont(scaledFont);
		}

		public void componentShown(ComponentEvent e) {
			e.getComponent().setFont(scaledFont);
		}
	}
}
