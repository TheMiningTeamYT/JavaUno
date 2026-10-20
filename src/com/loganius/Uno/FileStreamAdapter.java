package com.loganius.Uno;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

public class FileStreamAdapter implements StreamAdapter {
	private File file;
	
	public FileStreamAdapter(File file) {
		this.file = file;
	}
	
	public InputStream openStream() throws IOException {
		return new FileInputStream(file);
	}
}
