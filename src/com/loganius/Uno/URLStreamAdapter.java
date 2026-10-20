package com.loganius.Uno;

import java.net.URL;
import java.io.InputStream;
import java.io.IOException;

public class URLStreamAdapter implements StreamAdapter {
	private URL url;
	
	public URLStreamAdapter(URL url) {
		this.url = url;
	}
	
	public InputStream openStream() throws IOException {
		return url.openStream();
	}
}
