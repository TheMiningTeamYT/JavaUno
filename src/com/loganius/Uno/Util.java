package com.loganius.Uno;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

public class Util {
	private static byte[] buf = new byte[1000000];
	public static Image rotate(Image img, int deg) {
		RotateFilter rotate = new RotateFilter(deg * Math.PI / 180);
		ImageProducer producer = new FilteredImageSource(img.getSource(), rotate);
		return bufferImage(Toolkit.getDefaultToolkit().createImage(producer));
	}
	
	public static Image getImage(URL path) {
		try {
			InputStream in = path.openStream();
			int index = 0;
			int read;
			byte[] buf2;

			synchronized(buf) {
				while ((read = in.read(buf, index, buf.length - index)) != -1) {
					index += read;
				}
				buf2 = new byte[index];
				System.arraycopy(buf, 0, buf2, 0, index);
			}

			return bufferImage(Toolkit.getDefaultToolkit().createImage(buf2));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
}
