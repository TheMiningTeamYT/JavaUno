package com.loganius.Uno;
import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;
import javax.swing.*;

public class UnoGame extends JApplet implements GameHandler, ActionListener {
	private static Game gameState;
	private static JLabel loading = new JLabel("Loading assets, please wait...", SwingConstants.CENTER);
	private int frame = 0;
	
	public static void startGame() {
		gameState = new NetworkGameClient(new Deck.UnoCorns(), new UnoGame(), "Player4", "100.68.9.96", 23770);
		gameState.setBounds(0, 0, 640, 480);
	}
	
	// TODO: Work on the Applet part, make sure it works properly
	public void init() {
		setBounds(0, 0, 640, 480);
		setLayout(null);
		startGame();
		add(gameState);
	}
	
	public void gameOver() {
		System.exit(0);
	}
	
	public void actionPerformed(ActionEvent e) {
		String text = "Loading assets, please wait.";
		for (int i = 0; i < frame; i++) {
			text += ".";
		}
		loading.setText(text);
		frame = (frame + 1) % 3;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Uno!");
		Timer timer = new Timer(1000, new UnoGame());

		frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.setSize(640, 480);

		loading.setFont(Util.getScaledFont());
		loading.addComponentListener(Util.getTextResizeListener());

		frame.getContentPane().add(loading);
		frame.addWindowListener(new WindowListener());
		frame.addComponentListener(new ResizeListener());
		frame.setVisible(true);
		timer.start();
		
		startGame();
		
		timer.stop();
		frame.getContentPane().remove(loading);
		frame.getContentPane().add(gameState);
		frame.repaint();
	}
	
	private static class WindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	
	private static class ResizeListener extends ComponentAdapter {
		public void componentResized(ComponentEvent e) {
			Rectangle bounds = e.getComponent().getBounds();
			Util.onResize(bounds.width, bounds.height);
		}
	}
}
