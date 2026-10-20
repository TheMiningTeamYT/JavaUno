package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.io.InputStream;
import java.io.IOException;
import java.util.Vector;

public class Util {
	private static Font largeFont = new Font("Arial", Font.PLAIN, 40);
	private static Font scaledFont = new Font("Arial", Font.PLAIN, 20);
	private static Font smallFont = new Font("Arial", Font.PLAIN, 10);
	private static double scaleFactor = 1;
	public static final Color WHITE = new Color(255, 255, 255);
	public static final Color BLACK = new Color(0, 0, 0);

	public static final ComponentAdapter LargeResizeListener = new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
			e.getComponent().setFont(largeFont);
		}

		public void componentMoved(ComponentEvent e) {
			e.getComponent().setFont(largeFont);
		}

		public void componentShown(ComponentEvent e) {
			e.getComponent().setFont(largeFont);
		}
	};

	public static final ComponentAdapter ResizeListener = new ComponentAdapter() {
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

	public static final ComponentAdapter SmallResizeListener = new ComponentAdapter() {
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
		return Toolkit.getDefaultToolkit().createImage(producer);
	}
	
	public static Image getImage(URL path) {
		return Toolkit.getDefaultToolkit().getImage(path);
	}
	
	public static Image getImage(InputStream in, int expectedSize) throws IOException {
		return Toolkit.getDefaultToolkit().createImage(getFile(in, expectedSize));
	}
	
	public static Image bufferScaledImage(Image img, int width, int height) {
		Image scaled = img.getScaledInstance(width, height, Image.SCALE_FAST);
		Image result = bufferImage(scaled);
		scaled.flush();
		return result;
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
		largeFont = new Font("Arial", Font.BOLD, (int)(40 * scaleFactor));
		scaledFont = new Font("Arial", Font.PLAIN, (int)(20 * scaleFactor));
		smallFont = new Font("Arial", Font.PLAIN, (int)(10 * scaleFactor));
	}
	
	public static double getScaleFactor() {
		return scaleFactor;
	}
	
	public static double scale(double val) {
		return val*scaleFactor;
	}
	
	public static Font getLargeFont() {
		return largeFont;
	}
	
	public static Font getScaledFont() {
		return scaledFont;
	}
	
	public static Font getSmallFont() {
		return smallFont;
	}
	
	public static void commonComponentInit(Component comp, Color color) {
		comp.setForeground(color);
		comp.setFont(Util.getScaledFont());
		comp.addComponentListener(Util.ResizeListener);
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
		Vector subStrings = new Vector();
		String[] result;
		int index;

		while ((index = src.indexOf(split)) != -1) {
			if (index != 0) {
				subStrings.addElement(src.substring(0, index));
			}
			src = src.substring(index + split.length());
		}
		subStrings.addElement(src);

		result = new String[subStrings.size()];
		subStrings.copyInto(result);
		return result;
	}
	
	public static String replace(String src, String find, String replacement) {
		Vector subStrings = new Vector();
		String result = "";
		int index;
		
		while ((index = src.indexOf(find)) != -1) {
			subStrings.addElement(src.substring(0, index));
			subStrings.addElement(replacement);
			src = src.substring(index + find.length());
		}
		subStrings.addElement(src);
		
		for (int i = 0; i < subStrings.size(); i++) {
			result = result.concat((String) subStrings.elementAt(i));
		}
		
		return result;
	}
	
	 public static Rectangle transformBBox(Rectangle rect, double angle) {
        double minx = Double.POSITIVE_INFINITY;
        double miny = Double.POSITIVE_INFINITY;
        double maxx = Double.NEGATIVE_INFINITY;
        double maxy = Double.NEGATIVE_INFINITY;
        double[] coord = new double[2];

        for (int y = 0; y <= 1; y++) {
            for (int x = 0; x <= 1; x++) {
            	coord[0] = rect.x + Math.cos(angle)*x*rect.width + Math.sin(angle)*y*rect.height;
            	coord[1] = rect.y + Math.cos(angle)*y*rect.height - Math.sin(angle)*x*rect.width;

                minx = Math.min(minx, coord[0]);
                miny = Math.min(miny, coord[1]);
                maxx = Math.max(maxx, coord[0]);
                maxy = Math.max(maxy, coord[1]);
            }
        }

        return new Rectangle((int)Math.round(minx), (int)Math.round(miny), 
           (int)Math.round(maxx - minx), 
           (int)Math.round(maxy - miny));
    }
	 
	public static byte[] getFile(InputStream in, int expectedSize) throws IOException {
		byte[] buffer = new byte[(expectedSize > 0) ? expectedSize : 262144];
		byte[] newBuf;
		int index = 0;
		int read;
		
		while ((read = in.read(buffer, index, buffer.length - index)) != -1) {
			index += read;
			if (index == buffer.length) {
				newBuf = new byte[buffer.length * 2];
				System.arraycopy(buffer, 0, newBuf, 0, index);
				buffer = newBuf;
			}
		}
		
		newBuf = new byte[index];
		System.arraycopy(buffer, 0, newBuf, 0, index);
		return newBuf;
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
	
	// TODO: Make this a layout manager?
	public static class CenteringColumn extends ComponentAdapter {
		private Component child;
		
		CenteringColumn(Component child) {
			this.child = child;
		}
		
		public void componentResized(ComponentEvent e) {
			Rectangle bounds = e.getComponent().getBounds();
			child.setBounds(bounds.width / 4, 0, bounds.width / 2, bounds.height);
			e.getComponent().validate();
		}
	}
}
