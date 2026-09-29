package com.loganius.Uno;
import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;
import javax.swing.*;
import java.io.*;

// TODO: Handle scaling the game from the get go rather than starting at 640x480
public class UnoGame extends JApplet implements GameHandler, ActionListener {
	private Game gameState = null;
	private UnoServer server = null;

	private Container mainScreen;
	private Container parentContainer;
	private CardLayout layout = new CardLayout();
	private JLabel loading = new JLabel("Loading assets, please wait...", SwingConstants.CENTER);
	private Image bgSource = Util.getImage(Util.getResource("Assets/background.jpg"));
	private Image bgImage = bgSource;
	private Rectangle bgBounds = new Rectangle(0, -80, 640, 640);
	private Deck deck;
	private UnoGame unoGame = this;
	private int frame = 0;
	
	private void init(Container parent) {
		this.parentContainer = parent;

		Timer loadingAnimationTimer = new Timer(1000, this);
		mainScreen = new JPanel() {
			public void paintComponent(Graphics g) {
				g.drawImage(bgImage, bgBounds.x, bgBounds.y, bgBounds.width, bgBounds.height, null);
			}
		};

		parent.setLayout(new GridLayout(1, 1));
		
		mainScreen.setLayout(new GridLayout(1, 1));

		loading.setFont(Util.getScaledFont());
		loading.setForeground(Util.WHITE);
		loading.addComponentListener(Util.getTextResizeListener());
		mainScreen.add(loading);

		mainScreen.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				Rectangle bounds = mainScreen.getBounds();
				bgBounds = Util.scaleAndCrop(new Rectangle(1024, 1024), bounds);
				bgImage = Util.bufferScaledImage(bgSource, bgBounds.width, bgBounds.height);
				mainScreen.repaint();
			}
		});

		parent.add(mainScreen);
		parent.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				Rectangle bounds = e.getComponent().getBounds();
				Util.onResize(bounds.width, bounds.height);
			}
		});
		loadingAnimationTimer.start();
		
		deck = new Deck.UnoCorns();
		
		loadingAnimationTimer.stop();
		
		mainScreen.remove(loading);
		loading = null;
		mainScreen.setLayout(layout);
		mainScreen.add(new MainMenu(), "main");
		mainScreen.add(new JoinMenu(), "join");
		mainScreen.add(new HostMenu(), "host");
		parent.validate();
		parent.repaint();
	}
	
	public void gameOver() {
		if (server != null) {
			server.stop();
		}
		parentContainer.remove(gameState);
		parentContainer.add(mainScreen);
		parentContainer.validate();
		layout.show(mainScreen, "main");
		gameState = null;
	}
	
	public void actionPerformed(ActionEvent e) {
		String text = "Loading assets, please wait.";
		for (int i = 0; i < frame; i++) {
			text += ".";
		}
		loading.setText(text);
		frame = (frame + 1) % 3;
	}
	
	// TODO: Work on the Applet part, make sure it works properly
	public void init() {
		setSize(640, 480);
		setVisible(true);
		init(this);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Uno!");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setSize(640, 480);
		frame.setVisible(true);
		new UnoGame().init(frame.getContentPane());
	}
	
	private class MainMenu extends JPanel {
		private Image UnoLogo = Util.getImage(Util.getResource("Assets/UnoLogo.gif"));
		private ImageIcon icon = new ImageIcon(UnoLogo.getScaledInstance(-1, 160, Image.SCALE_FAST));
		

		MainMenu() {
			JPanel start = new JPanel();
			JPanel main = new JPanel();
			JLabel logo = new JLabel(icon);
			JLabel text = new JLabel(
					"for Java",
					SwingConstants.CENTER
			);
			JLabel join = new JLabel("Join a Game", SwingConstants.CENTER);
			JLabel host = new JLabel("Host a Game", SwingConstants.CENTER);
			JLabel exit = new JLabel("Exit", SwingConstants.CENTER);
			JLabel copyright = new JLabel(
					"<html>(C) Copyright 2026 Logan C. GPLv3 or later.<br>" +
					"Not produced by, licensed by, or associated with Mattel, Inc.</html>",
					SwingConstants.LEFT
			);
			Component filler = Box.createVerticalStrut(20);
			final Color blue = new Color(104, 137, 255);

			setLayout(new BorderLayout());
			start.setLayout(new BoxLayout(start, BoxLayout.Y_AXIS));
			main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

			logo.setAlignmentX(CENTER_ALIGNMENT);
			start.add(logo);

			text.setForeground(Util.WHITE);
			text.setAlignmentX(CENTER_ALIGNMENT);
			text.setFont(Util.getScaledFont());
			text.addComponentListener(Util.getTextResizeListener());
			start.add(text);
			
			start.setOpaque(false);
			add(start, BorderLayout.NORTH);

			main.add(filler);
			
			join.setForeground(blue);
			join.setAlignmentX(CENTER_ALIGNMENT);
			join.setFont(Util.getScaledFont());
			join.addComponentListener(Util.getTextResizeListener());
			join.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					layout.show(mainScreen, "join");
				}
			});
			main.add(join);
			
			host.setForeground(blue);
			host.setAlignmentX(CENTER_ALIGNMENT);
			host.setFont(Util.getScaledFont());
			host.addComponentListener(Util.getTextResizeListener());
			host.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					layout.show(mainScreen, "host");
				}
			});
			main.add(host);
			
			exit.setForeground(blue);
			exit.setAlignmentX(CENTER_ALIGNMENT);
			exit.setFont(Util.getScaledFont());
			exit.addComponentListener(Util.getTextResizeListener());
			exit.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					System.exit(0);
				}
			});
			main.add(exit);

			main.setOpaque(false);
			main.doLayout();
			add(main, BorderLayout.CENTER);
			
			copyright.setFont(Util.getSmallFont());
			copyright.addComponentListener(Util.getSmallTextResizeListener());
			copyright.setForeground(Util.WHITE);

			add(copyright, BorderLayout.SOUTH);
			
			setOpaque(false);
			addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					Rectangle bounds = getBounds();
					double scaleFactor = Util.getScaleFactor();
					
					if (239*scaleFactor > bounds.height / 3) {
						icon.setImage(UnoLogo.getScaledInstance(-1, bounds.height / 3, frame));
					} else {
						icon.setImage(UnoLogo.getScaledInstance(bounds.width / 2, -1, frame));
					}
				}
			});
		}	
	}
	
	// TODO: Find a way to make it so that you can't enter a non number for the port.
	private class JoinMenu extends JPanel {
		JTextField player = new JTextField(40);
		JTextField host = new JTextField(40);
		JTextField port = new JTextField("23770", 6);

		JoinMenu() {
			JLabel playerLabel = new JLabel("Name", SwingConstants.CENTER);
			JLabel hostLabel = new JLabel("Server", SwingConstants.CENTER);
			JLabel portLabel = new JLabel("Port", SwingConstants.CENTER);
			JButton start = new JButton("Join");
			JButton back = new JButton("Back");
			GridBagConstraints c = new GridBagConstraints();

			setLayout(new GridBagLayout());

			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 3;
			c.gridheight = 1;
			c.weightx = 1;
			c.fill = GridBagConstraints.BOTH;
			playerLabel.setForeground(Util.WHITE);
			playerLabel.setFont(Util.getScaledFont());
			playerLabel.addComponentListener(Util.getTextResizeListener());
			add(playerLabel, c);

			c.gridy++;
			player.setFont(Util.getScaledFont());
			player.addComponentListener(Util.getTextResizeListener());
			add(player, c);

			c.gridy++;
			c.gridwidth = 2;
			hostLabel.setForeground(Util.WHITE);
			hostLabel.setFont(Util.getScaledFont());
			hostLabel.addComponentListener(Util.getTextResizeListener());
			add(hostLabel, c);
			
			c.gridx += 2;
			c.gridwidth = 1;
			portLabel.setForeground(Util.WHITE);
			portLabel.setFont(Util.getScaledFont());
			portLabel.addComponentListener(Util.getTextResizeListener());
			add(portLabel, c);
			
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 2;
			host.setFont(Util.getScaledFont());
			host.addComponentListener(Util.getTextResizeListener());
			add(host, c);
			
			c.gridx += 2;
			c.gridwidth = 1;
			port.setFont(Util.getScaledFont());
			port.addComponentListener(Util.getTextResizeListener());
			add(port, c);
			
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 2;
			start.setFont(Util.getScaledFont());
			start.addComponentListener(Util.getTextResizeListener());
			start.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String name = player.getText();
					String hostname = host.getText();
					int portNum = Integer.parseInt(port.getText());
					if (name != null && hostname != null && name.length() != 0 && hostname.length() != 0) {
						gameState = new NetworkGameClient(deck, unoGame, player.getText(), host.getText(), portNum);
						parentContainer.remove(mainScreen);
						parentContainer.add(gameState);
						parentContainer.validate();
					}
				}
			});
			add(start, c);
			
			c.gridx += 2;
			c.gridwidth = 1;
			back.setFont(Util.getScaledFont());
			back.addComponentListener(Util.getTextResizeListener());
			back.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					layout.show(mainScreen, "main");
				}
			});
			add(back, c);
			
			setOpaque(false);
		}
	}
	
	private class HostMenu extends JPanel {
		JTextField player = new JTextField(40);
		JTextField port = new JTextField("23770", 6);

		HostMenu() {
			JLabel playerLabel = new JLabel("Name", SwingConstants.CENTER);
			JLabel portLabel = new JLabel("Port", SwingConstants.CENTER);
			JButton start = new JButton("Host");
			JButton back = new JButton("Back");
			GridBagConstraints c = new GridBagConstraints();

			setLayout(new GridBagLayout());

			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 2;
			c.gridheight = 1;
			c.weightx = 1;
			c.fill = GridBagConstraints.BOTH;
			playerLabel.setForeground(Util.WHITE);
			playerLabel.setFont(Util.getScaledFont());
			playerLabel.addComponentListener(Util.getTextResizeListener());
			add(playerLabel, c);

			c.gridy++;
			player.setFont(Util.getScaledFont());
			player.addComponentListener(Util.getTextResizeListener());
			add(player, c);

			c.gridy++;
			portLabel.setForeground(Util.WHITE);
			portLabel.setFont(Util.getScaledFont());
			portLabel.addComponentListener(Util.getTextResizeListener());
			add(portLabel, c);

			c.gridy++;
			port.setFont(Util.getScaledFont());
			port.addComponentListener(Util.getTextResizeListener());
			add(port, c);

			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 1;
			start.setFont(Util.getScaledFont());
			start.addComponentListener(Util.getTextResizeListener());
			start.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String name = player.getText();
					int portNum = Integer.parseInt(port.getText());
					if (name != null && name.length() != 0) {
						try {
							server = new UnoServer(portNum);
						} catch (IOException e2) {
							e2.printStackTrace();
						}
						gameState = new NetworkGameClient(deck, unoGame, player.getText(), "127.0.0.1", portNum);
						parentContainer.remove(mainScreen);
						parentContainer.add(gameState);
						parentContainer.validate();
					}
				}
			});
			add(start, c);
			
			c.gridx++;
			back.setFont(Util.getScaledFont());
			back.addComponentListener(Util.getTextResizeListener());
			back.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					layout.show(mainScreen, "main");
				}
			});
			add(back, c);
			
			setOpaque(false);
		}
	}
}
