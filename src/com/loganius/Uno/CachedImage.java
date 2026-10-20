package com.loganius.Uno;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Rectangle;

import javax.swing.Timer;

class CachedImage {
	private Image original;
	private Image buffered = null;
	private int users = 0;
	private int width = -1;
	private int height = -1;
	private int scaleWidth;
	private int scaleHeight;

	CachedImage(Image original, int orientation, int width, int height) {
		Rectangle bounds = Util.transformBBox(new Rectangle(width, height), orientation*Math.PI/180);
		this.original = original;
		scaleWidth = bounds.width;
		scaleHeight = bounds.height;
	}
	
	Image get() {
		update();
		return buffered;
	}
	
	int getWidth() {
		return (int)(scaleWidth*Util.getScaleFactor());
	}
	
	int getHeight() {
		return (int)(scaleHeight*Util.getScaleFactor());
	}

	void update() {
		if (buffered == null || width != getWidth() || height != getHeight()) {
			width = getWidth();
			height = getHeight();
			buffered = Util.bufferScaledImage(original, width, height);
		}
	}
	
	void using() {
		users++;
	}
	
	void stopUsing() {
		users--;
		if (users <= 0) {
			// Because cards often stop using and then immediately start using images,
			// we wait a bit before actually deleting the image for real.
			Timer cleanupDelay = new Timer(1000, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (users <= 0) {
						buffered = null;
						users = 0;
					}
				}
			});
			cleanupDelay.setRepeats(false);
			cleanupDelay.start();
		}
	}
}