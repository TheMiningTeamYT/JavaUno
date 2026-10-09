package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.Vector;

// TODO: Handle scaling the game from the get go rather than starting at 640x480
public class UnoGame implements GameHandler, ActionListener {
	private Vector ruleFactories = new Vector();

	private Game gameState = null;
	private UnoServer server = null;

	private Container mainScreen;
	private Container parentContainer;
	private CardLayout layout = new CardLayout();
	private JLabel loading = new JLabel("Loading assets, please wait...", SwingConstants.CENTER);
	private Image bgSource = Util.getImage(Util.getResource("Assets/background.jpg"));
	private Image bgImage = Util.bufferImage(bgSource);
	private Rectangle bgBounds = new Rectangle(0, -80, 640, 640);
	private Deck deck;
	private UnoGame unoGame = this;
	private int frame = 0;
	
	UnoGame() {
		ruleFactories.addElement(new DrawToMatch.Factory());
		ruleFactories.addElement(new ForcePlay.Factory());
		ruleFactories.addElement(new SevenZero.Factory());
		ruleFactories.addElement(new JumpIn.Factory());
		ruleFactories.addElement(new Stacking.Factory());
	}
	
	public void init(Container parent) {
		this.parentContainer = parent;

		Timer loadingAnimationTimer = new Timer(1000, this);
		mainScreen = new JPanel() {
			public void paintComponent(Graphics g) {
				g.drawImage(bgImage, bgBounds.x, bgBounds.y, null);
			}
		};

		parent.setLayout(new GridLayout(1, 1));
		
		mainScreen.setLayout(new GridLayout(1, 1));

		loading.setFont(Util.getScaledFont());
		loading.setForeground(Util.WHITE);
		loading.addComponentListener(Util.ResizeListener);
		mainScreen.add(loading);

		mainScreen.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				Rectangle bounds = mainScreen.getBounds();
				bgBounds = Util.scaleAndCrop(new Rectangle(1024, 1024), bounds);
				bgImage = Util.bufferScaledImage(bgSource, bgBounds.width, bgBounds.height);
				mainScreen.repaint();
			}
		});

		parent.removeAll();
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
	
	private class MainMenu extends JPanel {
		private Image UnoLogo = Util.getImage(Util.getResource("Assets/UnoLogo.gif"));
		private ImageIcon icon = new ImageIcon(UnoLogo.getScaledInstance(-1, 160, Image.SCALE_FAST));
		private JPanel main = new JPanel();
		private JPanel innerMain = new JPanel();

		MainMenu() {
			JPanel start = new JPanel();
			JLabel logo = new JLabel(icon);
			JLabel text = new JLabel(
					"for Java",
					SwingConstants.CENTER
			);
			JLabel join = new JLabel("Join a Game", SwingConstants.CENTER);
			JLabel host = new JLabel("Host a Game", SwingConstants.CENTER);
			JLabel exit = new JLabel("Exit", SwingConstants.CENTER);
			JLabel copyright = new JLabel(
					"(C) Copyright 2026 Logan C. GPLv3 or later." +
					"Not produced by, licensed by, or associated with Mattel, Inc.",
					SwingConstants.LEFT
			);
			final Color blue = new Color(151, 213, 252);
			
			Util.commonComponentInit(text, Util.WHITE);
			Util.commonComponentInit(join, blue);
			Util.commonComponentInit(host, blue);
			Util.commonComponentInit(exit, blue);

			setLayout(new BorderLayout());
			start.setLayout(new BoxLayout(start, BoxLayout.Y_AXIS));
			innerMain.setLayout(new BoxLayout(innerMain, BoxLayout.Y_AXIS));

			logo.setAlignmentX(CENTER_ALIGNMENT);
			start.add(logo);

			text.setAlignmentX(CENTER_ALIGNMENT);
			start.add(text);
			
			start.setOpaque(false);
			add(start, BorderLayout.NORTH);

			innerMain.add(Box.createVerticalGlue());

			join.setAlignmentX(CENTER_ALIGNMENT);
			join.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					layout.show(mainScreen, "join");
				}
			});
			innerMain.add(join);

			host.setAlignmentX(CENTER_ALIGNMENT);
			host.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					layout.show(mainScreen, "host");
				}
			});
			innerMain.add(host);

			exit.setAlignmentX(CENTER_ALIGNMENT);
			exit.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					System.exit(0);
				}
			});
			innerMain.add(exit);
			
			innerMain.add(Box.createVerticalGlue());

			innerMain.setOpaque(false);
			innerMain.doLayout();
			
			main.setOpaque(false);
			main.setLayout(null);
			main.add(innerMain);
			main.addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					Rectangle outerBounds = getBounds();
					Rectangle innerBounds = main.getBounds();
					innerMain.setBounds(outerBounds.width / 4 - innerBounds.x, (outerBounds.height / 4) - innerBounds.y, outerBounds.width / 2, outerBounds.height / 2);
					main.validate();
				}
			});
			add(main, BorderLayout.CENTER);
			
			copyright.setFont(Util.getSmallFont());
			copyright.addComponentListener(Util.SmallResizeListener);
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
		private JTextField player = new JTextField(40);
		private JTextField host = new JTextField(40);
		private JLabel error = new JLabel("", SwingConstants.CENTER);

		JoinMenu() {
			JPanel inner = new JPanel(new GridBagLayout());
			JLabel playerLabel = new JLabel("Name", SwingConstants.CENTER);
			JLabel hostLabel = new JLabel("Server", SwingConstants.CENTER);
			JButton start = new JButton("Join");
			JButton back = new JButton("Back");
			GridBagConstraints c = new GridBagConstraints();

			Util.commonComponentInit(hostLabel, Util.WHITE);
			Util.commonComponentInit(playerLabel, Util.WHITE);
			Util.commonComponentInit(player, Util.BLACK);
			Util.commonComponentInit(host, Util.BLACK);
			Util.commonComponentInit(start, Util.BLACK);
			Util.commonComponentInit(back, Util.BLACK);
			Util.commonComponentInit(error, Util.WHITE);

			setLayout(null);
			setOpaque(false);
			addComponentListener(new Util.CenteringColumn(inner));

			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 2;
			c.gridheight = 1;
			c.weightx = 1;
			c.fill = GridBagConstraints.BOTH;
			inner.add(playerLabel, c);

			c.gridy++;
			inner.add(player, c);

			c.gridy++;
			inner.add(hostLabel, c);

			c.gridy++;
			inner.add(host, c);

			c.gridy++;
			c.gridwidth = 1;
			start.setMnemonic(KeyEvent.VK_ENTER);
			start.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (host.getText() == null) {
						error.setText("Missing server hostname!");
						return;
					}
					String name = player.getText();
					String[] splitHost = Util.splitBy(host.getText(), ":");
					int port;

					try {
						port = (splitHost[1] == null) ? 23770 : Integer.parseInt(splitHost[1]);
					} catch (NumberFormatException e2) {
						error.setText("Invalid port number!");
						return;
					}

					if (name != null && splitHost[0] != null && name.length() != 0 && splitHost[0].length() != 0) {
						gameState = new NetworkGameClient(deck, unoGame, name, splitHost[0], port);
						parentContainer.remove(mainScreen);
						parentContainer.add(gameState);
						parentContainer.validate();
					}
				}
			});
			inner.add(start, c);
			
			c.gridx++;
			back.setMnemonic(KeyEvent.VK_BACK_SPACE);
			back.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					layout.show(mainScreen, "main");
				}
			});
			inner.add(back, c);
			
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 2;
			inner.add(error, c);

			inner.setOpaque(false);
			add(inner);
		}
	}
	
	private class HostMenu extends JPanel {
		private JTextField player = new JTextField(40);
		private JTextField port = new JTextField("23770", 6);
		private JCheckBox[] ruleBoxes = new JCheckBox[ruleFactories.size()];
		private JLabel error = new JLabel("", SwingConstants.CENTER);
		
		HostMenu() {
			JPanel inner = new JPanel(new GridBagLayout());
			JLabel[] ruleLabels = new JLabel[ruleFactories.size()];
			JLabel playerLabel = new JLabel("Name", SwingConstants.CENTER);
			JLabel portLabel = new JLabel("Port", SwingConstants.CENTER);
			JLabel rulesLabel = new JLabel("Rules", SwingConstants.CENTER);
			JButton start = new JButton("Host");
			JButton back = new JButton("Back");
			GridBagConstraints c = new GridBagConstraints();

			Util.commonComponentInit(portLabel, Util.WHITE);
			Util.commonComponentInit(playerLabel, Util.WHITE);
			Util.commonComponentInit(rulesLabel, Util.WHITE);
			Util.commonComponentInit(player, Util.BLACK);
			Util.commonComponentInit(port, Util.BLACK);
			Util.commonComponentInit(start, Util.BLACK);
			Util.commonComponentInit(back, Util.BLACK);
			Util.commonComponentInit(error, Util.WHITE);

			setLayout(null);
			setOpaque(false);
			addComponentListener(new Util.CenteringColumn(inner));

			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 2;
			c.gridheight = 1;
			c.weightx = 1;
			c.fill = GridBagConstraints.BOTH;
			inner.add(playerLabel, c);

			c.gridy++;
			inner.add(player, c);

			c.gridy++;
			inner.add(portLabel, c);

			c.gridy++;
			inner.add(port, c);
			
			c.gridy++;
			inner.add(rulesLabel, c);
			
			for (int i = 0; i < ruleFactories.size();) {
				JPanel ruleBox = new JPanel(new GridLayout(1, 4));
				c.gridy++;

				for (int j = 0; i < ruleFactories.size() && j < 4; i++, j++) {
					RuleFactory factory = (RuleFactory)ruleFactories.elementAt(i);
					JPanel box = new JPanel();
					box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
	
					ruleLabels[i] = new JLabel(factory.getName(), SwingConstants.CENTER);
					ruleLabels[i].setForeground(Util.WHITE);
					ruleLabels[i].setFont(Util.getSmallFont());
					ruleLabels[i].addComponentListener(Util.SmallResizeListener);
					ruleLabels[i].setAlignmentX(CENTER_ALIGNMENT);
					
					ruleBoxes[i] = new JCheckBox();
					ruleBoxes[i].setOpaque(false);
					ruleBoxes[i].setAlignmentX(CENTER_ALIGNMENT);
	
					box.add(ruleLabels[i]);
					box.add(ruleBoxes[i]);
	
					box.setOpaque(false);
					ruleBox.add(box);
				}
				ruleBox.setOpaque(false);
				inner.add(ruleBox, c);
			}

			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 1;
			start.setMnemonic(KeyEvent.VK_ENTER);
			start.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String name = player.getText();
					int portNum = Integer.parseInt(port.getText());
					if (name != null && name.length() != 0) {
						Vector rules = new Vector();
						try {
							server = new UnoServer(portNum);
						} catch (IOException e2) {
							e2.printStackTrace();
						}
						gameState = new NetworkGameClient(deck, unoGame, player.getText(), "127.0.0.1", portNum);
						for (int i = 0; i < ruleFactories.size(); i++) {
							if (ruleBoxes[i].isSelected()) {
								rules.addElement(((RuleFactory)ruleFactories.elementAt(i)).create());
							}
						}
						if (rules.size() > 0) {
							Rule[] ruleArray;
							ruleArray = new Rule[rules.size()];
							rules.copyInto(ruleArray);
							gameState.setRuleSet(new RuleSet.Composite(ruleArray));
						}
						parentContainer.remove(mainScreen);
						parentContainer.add(gameState);
						parentContainer.validate();
					}
				}
			});
			inner.add(start, c);
			
			c.gridx++;
			back.setMnemonic(KeyEvent.VK_BACK_SPACE);
			back.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					layout.show(mainScreen, "main");
				}
			});
			inner.add(back, c);
			
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 2;
			inner.add(error);
			
			inner.setOpaque(false);
			add(inner);
		}
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Uno!");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setSize(640, 480);
		frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.getContentPane().add(new JLabel("Loading assets, please wait...", SwingConstants.CENTER));
		frame.setVisible(true);
		new UnoGame().init(frame.getContentPane());
	}
}
