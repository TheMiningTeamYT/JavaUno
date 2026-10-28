package com.loganius.Uno;

import java.awt.Image;

class CardSkin {
	private CachedImage[] face;
	private CachedImage[] back;
	
	CardSkin(Image face, CachedImage[] back) {
		this.face = generateImageList(face);
		this.back = back;
	}
	
	CardSkin(CardSkin parent, CachedImage[] face, CachedImage[] back) {
		this.face = (face == null) ? parent.face : face;
		this.back = (back == null) ? parent.back : back;
	}
	
	Image getFace(int orientation) {
		return face[orientation / 90].get();
	}
	
	Image getBack(int orientation) {
		return back[orientation / 90].get();
	}
	
	void useFace(int orientation) {
		face[orientation / 90].using();
		face[0].using();
	}
	
	void stopUsingFace(int orientation) {
		face[orientation / 90].stopUsing();
		face[0].stopUsing();
	}
	
	void useBack(int orientation) {
		back[orientation / 90].using();
		face[0].using();
	}
	
	void stopUsingBack(int orientation) {
		back[orientation / 90].stopUsing();
		face[0].stopUsing();
	}
	
	static CachedImage[] generateImageList(Image src) {
		return new CachedImage[] {
			new CachedImage(src, 0, 80, 120),
			new CachedImage(Util.rotate(src, 90), 90, 80, 120),
			new CachedImage(Util.rotate(src, 180), 180, 80, 120),
			new CachedImage(Util.rotate(src, 270), 270, 80, 120),
		};
	}
}
