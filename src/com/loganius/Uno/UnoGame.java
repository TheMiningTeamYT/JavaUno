package com.loganius.Uno;
import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;
import javax.swing.*;

public class UnoGame extends JApplet implements ItemListener {
	private static Game gameState = new NetworkGameClient(new Deck.UnoCorns(), "Player", "127.0.0.1", 23770);
	
	public static void startGame() {
		gameState.setBounds(0, 0, 640, 480);
	}
	
	// TODO: Work on the Applet part, make sure it works properly
	public void init() {
		setBounds(0, 0, 640, 480);
		setLayout(null);
		startGame();
		add(gameState);
	}
	
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			System.out.println("Selected");
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Uno!");
		
		startGame();
		
		frame.getContentPane().add(gameState);
		frame.setSize(640, 480);
		frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.setVisible(true);
		frame.addWindowListener(new WindowListener());
	}
	
	private static class WindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
}
