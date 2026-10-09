package com.loganius.Uno;

import java.awt.GridLayout;

import javax.swing.*;

public class UnoApplet extends JApplet {
	// TODO: Work on the Applet part, make sure it works properly
	public void init() {
		getContentPane().setLayout(new GridLayout(1, 1));
		getContentPane().add(new JLabel("Loading assets, please wait...", SwingConstants.CENTER));
		setVisible(true);
		new UnoGame().init(getContentPane());
	}
}
