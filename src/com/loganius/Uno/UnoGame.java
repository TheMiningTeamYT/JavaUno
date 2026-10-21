package com.loganius.Uno;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.*;

import java.net.URL;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

// TODO: Handle scaling the game from the get go rather than starting at 640x480
public class UnoGame implements GameHandler {
	private Vector ruleFactories = new Vector();

	private Game gameState = null;
	private UnoServer server = null;

	private Container mainScreen;
	private Container parentContainer;
	private UnoGame unoGame = this;

	private CardLayout layout = new CardLayout();
	private volatile Hashtable knownPacks = new Hashtable();
	private Vector loadedThemes = new Vector();
	private volatile Image bgSource;
	private volatile Image bgImage;
	private volatile Rectangle bgBounds = new Rectangle(0, -80, 640, 640);
	private volatile Theme theme;
	private volatile Color textColor;
	private volatile Color accentColor;
	private int frame = 0;
	
	UnoGame() {
		ResourcePack defaultPack = new DefaultResourcePack();
		ruleFactories.addElement(new DrawToMatch.Factory());
		ruleFactories.addElement(new ForcePlay.Factory());
		ruleFactories.addElement(new SevenZero.Factory());
		ruleFactories.addElement(new JumpIn.Factory());
		ruleFactories.addElement(new Stacking.Factory());
		ruleFactories.addElement(new NoBluffing.Factory());
		knownPacks.put(defaultPack.getName(), defaultPack);
		try {
			loadedThemes.addElement((theme = defaultPack.produce(null)));
		} catch (IOException e) {
			throw new RuntimeException("wat");
		}
		textColor = theme.getMainScreenText();
		accentColor = theme.getMainScreenAccent();
	}
	
	public void init(Container parent) {
		this.parentContainer = parent;
		bgSource = theme.getMainScreenBackground();
		bgImage = Util.bufferScaledImage(bgSource, 640, 640);

		mainScreen = new JPanel() {
			public void paintComponent(Graphics g) {
				g.drawImage(bgImage, bgBounds.x, bgBounds.y, this);
			}
		};

		mainScreen.setLayout(new GridLayout(1, 1));
		mainScreen.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				updateBackground();
			}
		});
		
		mainScreen.setLayout(layout);
		mainScreen.add(new MainMenu(), "main");
		mainScreen.add(new JoinMenu(), "join");
		mainScreen.add(new HostMenu(), "host");
		mainScreen.add(new ResourcePackSelectionMenu(), "packs");

		parent.removeAll();
		parent.setLayout(new GridLayout(1, 1));
		parent.add(mainScreen);
		parent.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				Rectangle bounds = e.getComponent().getBounds();
				Util.onResize(bounds.width, bounds.height);
			}
		});

		parent.validate();
	}
	
	private void updateBackground() {
		Rectangle bounds = mainScreen.getBounds();
		int width;
		int height;
		while ((width = bgSource.getWidth(null)) == -1);
		while ((height = bgSource.getHeight(null)) == -1);
		bgBounds = Util.scaleAndCrop(new Rectangle(width, height), bounds);
		bgImage = Util.bufferScaledImage(bgSource, bgBounds.width, bgBounds.height);
	}
	
	private synchronized void setTheme(Theme theme) {
		this.theme = theme;
		bgSource = theme.getMainScreenBackground();
		textColor = theme.getMainScreenText();
		accentColor = theme.getMainScreenAccent();
		updateBackground();
		mainScreen.repaint();
	}
	
	public void gameOver() {
		if (server != null) {
			server.stop();
			server = null;
		}
		parentContainer.remove(gameState);
		parentContainer.add(mainScreen);
		parentContainer.validate();
		layout.show(mainScreen, "main");
		gameState = null;
	}
	
	private int packInUse(ResourcePack pack) {
		String name = pack.getName();
		for (int i = 0; i < loadedThemes.size(); i++) {
			if (((Theme)loadedThemes.elementAt(i)).getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	private synchronized void reloadPacks(int startFrom) throws IOException {
		Theme currentTheme = (startFrom > 0) ? (Theme) loadedThemes.elementAt(startFrom - 1) : null;
		for (int i = startFrom; i < loadedThemes.size(); i++) {
			currentTheme = ((ResourcePack)knownPacks.get(((Theme)loadedThemes.elementAt(i)).getName())).produce(currentTheme);
			loadedThemes.setElementAt(currentTheme, i);
		}
		setTheme(currentTheme);
	}
	
	private class MainMenu extends JPanel {
		private Image UnoLogo = theme.getLogo();
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
			JLabel packs = new JLabel("Resource Packs", SwingConstants.CENTER);
			JLabel exit = new JLabel("Exit", SwingConstants.CENTER);
			JLabel copyright = new JLabel(
					"(C) Copyright 2026 Logan C. GPLv3 or later. " +
					"Not produced by, licensed by, or associated with Mattel, Inc.",
					SwingConstants.LEFT
			);
			
			Util.commonComponentInit(text, textColor);
			Util.commonComponentInit(join, accentColor);
			Util.commonComponentInit(host, accentColor);
			Util.commonComponentInit(packs, accentColor);
			Util.commonComponentInit(exit, accentColor);

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
			
			packs.setAlignmentX(CENTER_ALIGNMENT);
			packs.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					layout.show(mainScreen, "packs");
				}
			});
			innerMain.add(packs);

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
			copyright.setForeground(textColor);

			add(copyright, BorderLayout.SOUTH);
			
			setOpaque(false);
			addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					Rectangle bounds = getBounds();
					double scaleFactor = Util.getScaleFactor();
					Image previous = icon.getImage();

					if (previous != null) {
						previous.flush();
					}
					if (239*scaleFactor > bounds.height / 3) {
						icon.setImage(UnoLogo.getScaledInstance(-1, bounds.height / 3, frame));
					} else {
						icon.setImage(UnoLogo.getScaledInstance(bounds.width / 2, -1, frame));
					}
					repaint();
				}
			});
		}	
	}
	
	// TODO: Find a way to make it so that you can't enter a non number for the port.
	private class JoinMenu extends JPanel {
		JoinMenu() {
			JPanel inner = new JPanel(new GridBagLayout());
			JLabel playerLabel = new JLabel("Name", SwingConstants.CENTER);
			JLabel hostLabel = new JLabel("Server", SwingConstants.CENTER);
			JButton start = new JButton("Join");
			JButton back = new JButton("Back");
			final JTextField player = new JTextField(40);
			final JTextField host = new JTextField(40);
			final JLabel error = new JLabel("", SwingConstants.CENTER);
			GridBagConstraints c = new GridBagConstraints();

			Util.commonComponentInit(hostLabel, textColor);
			Util.commonComponentInit(playerLabel, textColor);
			Util.commonComponentInit(player, Util.BLACK);
			Util.commonComponentInit(host, Util.BLACK);
			Util.commonComponentInit(start, Util.BLACK);
			Util.commonComponentInit(back, Util.BLACK);
			Util.commonComponentInit(error, textColor);

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
						port = (splitHost.length > 1) ? Integer.parseInt(splitHost[1]) : 23770;
					} catch (NumberFormatException e2) {
						error.setText("Invalid port number!");
						return;
					}

					if (name != null && splitHost[0] != null && name.length() != 0 && splitHost[0].length() != 0) {
						gameState = new NetworkGameClient(theme, unoGame, name, splitHost[0], port);
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
		HostMenu() {
			JPanel inner = new JPanel(new GridBagLayout());
			JLabel[] ruleLabels = new JLabel[ruleFactories.size()];
			JLabel playerLabel = new JLabel("Name", SwingConstants.CENTER);
			JLabel portLabel = new JLabel("Port", SwingConstants.CENTER);
			JLabel rulesLabel = new JLabel("Rules", SwingConstants.CENTER);
			JButton start = new JButton("Host");
			JButton back = new JButton("Back");
			final JTextField player = new JTextField(40);
			final JTextField port = new JTextField("23770", 6);
			final JCheckBox[] ruleBoxes = new JCheckBox[ruleFactories.size()];
			final JTextArea error = new JTextArea("");
			GridBagConstraints c = new GridBagConstraints();

			Util.commonComponentInit(portLabel, textColor);
			Util.commonComponentInit(playerLabel, textColor);
			Util.commonComponentInit(rulesLabel, textColor);
			Util.commonComponentInit(player, Util.BLACK);
			Util.commonComponentInit(port, Util.BLACK);
			Util.commonComponentInit(start, Util.BLACK);
			Util.commonComponentInit(back, Util.BLACK);
			Util.commonComponentInit(error, textColor);
			error.setEditable(false);
			error.setOpaque(false);

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
					ruleLabels[i].setForeground(textColor);
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
						gameState = new NetworkGameClient(theme, unoGame, player.getText(), "127.0.0.1", portNum);
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
	
	private class ResourcePackSelectionMenu extends JPanel {
		private JPanel inner = new JPanel(new GridBagLayout());
		private JPanel packContainer = new JPanel() {
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				result.width = packScroll.getBounds().width;
				return result;
			}
		};
		private JScrollPane packScroll = new JScrollPane(packContainer);
		private JPanel finalUI = new JPanel();
		private ResourcePack defaultPack = new DefaultResourcePack();

		ResourcePackSelectionMenu() {
			JLabel title = new JLabel("Resource Pack Selection", SwingConstants.CENTER);
			JButton back = new JButton("Back");
			JLabel remoteURLLabel = new JLabel("Load remote resource pack", SwingConstants.LEFT);
			final JTextField remoteURL = new JTextField();
			JButton load = new JButton("Load");
			final JTextArea error = new JTextArea();
			
			GridBagConstraints c = new GridBagConstraints();
			
			Util.commonComponentInit(title, textColor);
			Util.commonComponentInit(back, Util.BLACK);
			Util.commonComponentInit(remoteURLLabel, textColor);
			Util.commonComponentInit(remoteURL, Util.BLACK);
			Util.commonComponentInit(load, Util.BLACK);
			error.setForeground(textColor);
			error.setFont(Util.getSmallFont());
			error.addComponentListener(Util.SmallResizeListener);
			
			setLayout(new GridLayout(1, 1));
			setOpaque(false);
			
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 2;
			c.gridheight = 1;
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			inner.add(title, c);
			
			c.gridy++;
			packContainer.setLayout(new GridBagLayout());
			packContainer.setOpaque(false);
			packScroll.getViewport().setOpaque(false);
			packScroll.setOpaque(false);
			packScroll.setBorder(null);
			c.fill = GridBagConstraints.BOTH;
			c.weighty = 1;
			inner.add(packScroll, c);
			
			c.gridy++;
			c.weighty = 0;
			inner.add(remoteURLLabel, c);
			
			c.gridy++;
			inner.add(remoteURL, c);

			c.gridy++;
			c.gridwidth = 1;
			load.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final String path = remoteURL.getText();

					if (path == null || path.length() == 0) {
						return;
					}
					new Thread(new Runnable() {
						public void run() {
							try {
								ZIPResourcePack pack = new ZIPResourcePack(new URLStreamAdapter(new URL(remoteURL.getText())));
								if (!pack.getName().equalsIgnoreCase("Default")) {
									synchronized(knownPacks) {
										knownPacks.put(pack.getName(), pack);
									}
								}
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										showPacks();
									}
								});
							} catch (final Exception err) {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										error.setText(err.getMessage());
									}
								});
							}
						}
					}).start();
				}
			});
			inner.add(load, c);

			c.gridx++;
			back.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					layout.show(mainScreen, "main");
				}
			});
			inner.add(back, c);
			inner.setOpaque(false);
			
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 2;
			error.setLineWrap(true);
			error.setEditable(false);
			error.setOpaque(false);
			inner.add(error, c);

			finalUI.setLayout(null);
			finalUI.setOpaque(false);
			finalUI.add(inner);
			finalUI.addComponentListener(new Util.CenteringColumn(inner));
			
			addComponentListener(new ComponentAdapter() {
				public void componentShown(ComponentEvent e) {
					scan();
				}
			});
		}
		
		void scan() {
			JLabel loading = new JLabel("Loading, please wait...");
			Util.commonComponentInit(loading, textColor);
			removeAll();
			add(loading, SwingConstants.CENTER);

			new Thread(new Runnable() {
				public void run() {
					String[] packPaths;
					File resourcePackDir = new File("resourcepacks");
						try {
							if (resourcePackDir.exists()) {
								packPaths = new File("resourcepacks").list(new FilenameFilter() {
									public boolean accept(File dir, String name) {
										String extension = name.substring(name.length() - 4);
										return extension.equalsIgnoreCase(".zip");
									}
								});

								for (int i = 0; i < packPaths.length; i++) {
									try {
										ResourcePack newPack = new ZIPResourcePack(new FileStreamAdapter(new File(resourcePackDir, packPaths[i])));
										synchronized(knownPacks) {
											if (!newPack.getName().equalsIgnoreCase("Default")) {
												knownPacks.put(newPack.getName(), newPack);
											}
										}
									} catch (Exception e) {};
								}
							} else {
								resourcePackDir.mkdir();
							}
						} catch (SecurityException e) {}
					
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							showPacks();
						}
					});
				}
			}).start();
		}
		
		private void showPacks() {
			GridBagConstraints c = new GridBagConstraints();
			
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			packContainer.removeAll();

			synchronized(knownPacks) {
				if (knownPacks.size() > 0) {
					for (Enumeration e = knownPacks.elements(); e.hasMoreElements();) {
						JPanel row = new JPanel(new GridLayout(1, 4));
						for (int j = 0; e.hasMoreElements() && j < 3; j++) {
							final ResourcePack pack = (ResourcePack) e.nextElement();
							final Image packIcon = pack.getIcon();
							final ImageIcon icon = new ImageIcon();
							JLabel name = new JLabel(pack.getName(), SwingConstants.CENTER);
							JLabel author = new JLabel(pack.getAuthor(), SwingConstants.CENTER);
							JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
							JPanel box = new JPanel();
							JButton inspect = new JButton("Inspect");
							
							Util.commonComponentInit(inspect, Util.BLACK);
							Util.commonComponentInit(name, textColor);
	
							author.setForeground(textColor);
							author.setFont(Util.getSmallFont());
							author.addComponentListener(Util.SmallResizeListener);
	
							box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
							
							iconLabel.setAlignmentX(CENTER_ALIGNMENT);
							box.add(iconLabel);
							
							inspect.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									mainScreen.add(new ResourcePackScreen(pack), "packscreen");
									layout.show(mainScreen, "packscreen");
								}
							});
							
							name.setAlignmentX(CENTER_ALIGNMENT);
							box.add(name);
							
							author.setAlignmentX(CENTER_ALIGNMENT);
							box.add(author);
							
							inspect.setAlignmentX(CENTER_ALIGNMENT);
							box.add(inspect);
							
							box.addComponentListener(new ComponentAdapter() {
								public void componentResized(ComponentEvent e) {
									updatePackIcon(packIcon, icon, e.getComponent());
								}
								
								public void componentMoved(ComponentEvent e) {
									updatePackIcon(packIcon, icon, e.getComponent());
								}
							});
							box.setOpaque(false);
							row.add(box);
						}
						row.setOpaque(false);
						packContainer.add(row, c);
						c.gridy++;
					}
				} else {
					JLabel noneFoundError = new JLabel("No resource packs were found.", SwingConstants.CENTER);
					Util.commonComponentInit(noneFoundError, textColor);
					packContainer.add(noneFoundError, c);
				}
			}
			removeAll();
			add(finalUI);
			validate();
			repaint();
		}
		
		private void updatePackIcon(Image packIcon, ImageIcon icon, Component component) {
			Rectangle bounds = packContainer.getBounds();
			Image previous = icon.getImage();
			if (previous != null) {
				previous.flush();
			}
			if (bounds.width > bounds.height) {
				icon.setImage(packIcon.getScaledInstance(-1, bounds.height/3 - (int)(5*Util.getScaleFactor()), Image.SCALE_FAST));
			} else {
				icon.setImage(packIcon.getScaledInstance(bounds.width/3 - (int)(5*Util.getScaleFactor()), -1, Image.SCALE_FAST));
			}
			component.invalidate();
			validate();
		}
	}
	
	private class ResourcePackScreen extends JPanel {
		private Image packIcon;
		private ImageIcon icon = new ImageIcon();

		ResourcePackScreen(final ResourcePack pack) {
			JPanel inner = new JPanel(new GridBagLayout());
			JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
			JLabel name = new JLabel(pack.getName(), SwingConstants.CENTER);
			JLabel author = new JLabel(pack.getAuthor(), SwingConstants.CENTER);
			JTextArea description = new JTextArea(pack.getDescription());
			final JButton use = new JButton(packInUse(pack) >= 0 ? "Unload" : "Load");
			final JButton back = new JButton("Back");
			final JLabel error = new JLabel("", SwingConstants.CENTER);
			GridBagConstraints c = new GridBagConstraints();

			packIcon = pack.getIcon();
			Util.commonComponentInit(name, textColor);
			Util.commonComponentInit(author, textColor);
			Util.commonComponentInit(use, Util.BLACK);
			Util.commonComponentInit(back, Util.BLACK);
			Util.commonComponentInit(error, textColor);
			
			description.setForeground(textColor);
			description.setFont(Util.getSmallFont());
			description.addComponentListener(Util.SmallResizeListener);

			setLayout(null);
			setOpaque(false);
			addComponentListener(new Util.CenteringColumn(inner));
			addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					Rectangle bounds = getBounds();
					Image previous = icon.getImage();
					if (previous != null) {
						previous.flush();
					}
					if (bounds.width > bounds.height) {
						icon.setImage(packIcon.getScaledInstance(-1, bounds.height / 3, Image.SCALE_FAST));
					} else {
						icon.setImage(packIcon.getScaledInstance(bounds.width / 3, -1, Image.SCALE_FAST));
					}
					validate();
				}
			});
			
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 2;
			c.gridheight = 1;
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			inner.add(iconLabel,c);
			
			c.gridy++;
			inner.add(name, c);
			
			c.gridy++;
			description.setLineWrap(true);
			description.setEditable(false);
			description.setOpaque(false);
			inner.add(description, c);
			
			c.gridy++;
			inner.add(author, c);
			
			c.gridy++;
			inner.add(error, c);
			
			c.gridy++;
			c.gridwidth = 1;
			use.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (pack.getName().equalsIgnoreCase("Default")) {
						return;
					}

					try {
						final int packIndex = packInUse(pack);
						if (packIndex == 0) {
							return;
						}
						if (packIndex > 0) {
							synchronized(loadedThemes) {
								loadedThemes.removeElementAt(packIndex);
								if (packIndex == loadedThemes.size()) {
									setTheme((Theme)loadedThemes.elementAt(packIndex - 1));
									use.setText("Load");
								} else {
									new Thread(new Runnable() {
										public void run() {
											try {
												reloadPacks(packIndex);
												SwingUtilities.invokeLater(new Runnable() {
													public void run() {
														use.setText("Load");
													}
												});
											} catch (final Exception err) {
												SwingUtilities.invokeLater(new Runnable() {
													public void run() {
														err.printStackTrace();
														error.setText(err.getMessage());
													}
												});
											}
										}
									}).start();
								}
							}
						} else {
							setTheme(pack.produce(theme));
							loadedThemes.addElement(theme);
							use.setText("Unload");
						}
					} catch (Exception err) {
						err.printStackTrace();
						error.setText(err.getMessage());
					}
				}
			});
			inner.add(use, c);
			
			c.gridx++;
			back.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cleanup();
				}
			});
			inner.add(back, c);
			
			inner.add(Box.createVerticalGlue());
			inner.setOpaque(false);
			add(inner);
			validate();
		}
		
		private void cleanup() {
			layout.show(mainScreen, "packs");
			mainScreen.remove(this);
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
