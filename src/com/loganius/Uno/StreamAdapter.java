package com.loganius.Uno;

import java.io.InputStream;
import java.io.IOException;

public interface StreamAdapter {
	InputStream openStream() throws IOException;
}
