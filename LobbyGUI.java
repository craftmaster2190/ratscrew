import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * 
 * @author Bryce J. Fisher & Carlos
 * 
 */
public class LobbyGUI extends JFrame {

	private static final int HOSTING = JOptionPane.YES_OPTION;
	private static final int JOINING = JOptionPane.NO_OPTION;
	private static final long serialVersionUID = 1L;
	static Network networkForServer;

	/**
	 * If an exception occurs in the program, handle it with this method.
	 * 
	 * @param exception
	 */
	public static void handleExceptionWithGUI(final Exception exception) {
		// print exception to console
		exception.printStackTrace();

		// collect information about the exception
		String exceptionString = exception.getCause() + exception.getMessage()
				+ "\n\n" + exception.getClass().toString() + " : "
				+ exception.getMessage() + "\n\n";
		for (final StackTraceElement stackTraceElement : exception
				.getStackTrace()) {
			exceptionString += stackTraceElement.getMethodName() + " ( "
					+ stackTraceElement.getClassName() + ":"
					+ stackTraceElement.getLineNumber() + " )\n";
		}
		// show exception information in GUI dialog box
		JOptionPane.showMessageDialog(null, exceptionString,
				exception.getCause() + exception.getMessage(),
				JOptionPane.ERROR_MESSAGE, null);

		// rethrow the exception thereby terminating the thread
		throw new RuntimeException(exception);
	}

	public static void main(final String[] args) {
		final int mode = JOptionPane.showOptionDialog(null,
				"Message - Would you like to host/join?", "Title - Cards",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, new Object[] { "Host", "Join", "Quit" }, null);
		// hosting
		if (mode == HOSTING) {
			// String input = (String) JOptionPane.showInputDialog(null,
			// "Enter the number of players (includes host)", "Join Game",
			// JOptionPane.QUESTION_MESSAGE, null, new Object[] { "2",
			// "3", "4", "5", "6", "7", "8" }, "4");
			final Network networkHost = new Network(0);
			final Network networkForMe = new Network(Network.getAddress());
			new LobbyGUI(networkForMe, networkHost).setVisible(true);
		} else
		// join game
		if (mode == JOINING) {
			final String input = (String) JOptionPane.showInputDialog(null,
					"Enter the hosts IP address", "Join Game",
					JOptionPane.QUESTION_MESSAGE, null, null, "127.0.0.1");
			try {
				new LobbyGUI(new Network(InetAddress.getByName(input)))
						.setVisible(true);
			} catch (final Exception exception) {
				handleExceptionWithGUI(exception);

			}
		}

	}

	JPanel centerPanel;

	boolean inLobby = true;
	final boolean isHost;

	Runnable listeningMessagingRunnable = new Runnable() {

		private Player me;
		private int myID;
		private CardsGUI cardsGUI;

		@Override
		public void run() {
			Network.snooze(3333L);
			while (true) {

				final String messageFromServer = networkToUse.getMessage();

				// if server has disconnected or initiated an end game
				if (!networkToUse.isConnected()) {
					setTitle("Disconnected");
					playersTextPane.setText("<html><body><center><br><h1><u>Disconnected</u></h1></center></body></html>");
					addChatMessageToMessageOutputTextArea("---Game over!--- Reason: "
							+ (inLobby ? "Host canceled game."
									: "A user disconnected"));
					messageInputTextField.setEnabled(false);
					messageOutputTextArea.setEnabled(false);
					nameTextField.setEnabled(false);
					sendMessageButton.setEnabled(false);
					return;
				}

				// check that the message is not null, might be disconnected
				if (messageFromServer == null) {

					Network.snooze(100L);
					continue;
				}
				// if players are using chat function
				if (messageFromServer.startsWith(Network.CMD_MESSAGE)) {
					addChatMessageToMessageOutputTextArea(messageFromServer
							.substring(Network.CMD_MESSAGE.length()));
				} else if (messageFromServer.startsWith(Network.CMD_YOUR_ID)) {
					myID = Integer.parseInt(messageFromServer
							.substring(Network.CMD_YOUR_ID.length()));
				} else
				// if server is sending list of players
				if (messageFromServer
						.startsWith(Network.CMD_SEND_PLAYERS_TO_CLIENTS)) {
					// if not in lobby anymore, ignore these
					if (inLobby) {
						// prevent writer from asking for players for another
						// second, not needed
						waitLongerWeGotIt = true;
						final String substring = messageFromServer
								.substring(Network.CMD_SEND_PLAYERS_TO_CLIENTS
										.length());
						final String[] playersStrings = StringParser
								.splitPlayers(substring);

						int count = 0;

						// prepare html representation of players
						String htmlString = "<html>\n<body>\n<center>\n<br><h2><u>Players in Lobby</u></h2>\n";

						// reset players and reorder them
						playersIDsAndNames.clear();

						// complete html
						for (final String currentPlayer : playersStrings) {
							count++;
							final String name = StringParser
									.splitAndDecodeName(currentPlayer);
							final int id = StringParser
									.splitAndGetID(currentPlayer);
							htmlString += name + "&nbsp;&nbsp;&nbsp;<b>ID: "
									+ id + "</b><br>\n";

							// re add players
							playersIDsAndNames.put(id, name);
						}

						htmlString += "<br>\n<b>" + "My ID: " + myID
								+ "</b><br>\n";

						// close html string

						// add instructions
						htmlString += "<h2><b>Egyptian Ratscrew</b> - Instructions</h2>\n";

						htmlString += "<b>Objective:</b> Take all of the cards from all other players.<br>";
						htmlString += "A player has unofficially* lost when that player has zero cards.<br>";
						htmlString += "<br>";

						htmlString += "<b>Important!</b> If the Play button lights up, it is your turn.<br>";
						htmlString += "<br>";

						htmlString += "If you see two cards of the same value in sequence. ie. Ace - Ace, Two - Two<br>";
						htmlString += "or if you see two cards of the same value in sequence with another card in between them. ie. Ace - Two - Ace<br>";
						htmlString += "or if you see a king and queen in sequence, double-click those cards.<br>";
						htmlString += "If the cards you click are the top cards of the pile, you take the pile.<br>";
						htmlString += "<b>*Note:</b> A player that has no cards may re-enter the game this way.<br>";
						htmlString += "<br>";
						
						htmlString += "If you play a royal card, (Ace, King, Queen, Jack)<br>";
						htmlString += "The next player with cards must play a royal with x amount of cards or else you take the pile.<br>";
						htmlString += "Ace is 4, King is 3, Queen is 2, Jack is 1. Example, if you place a Queen,<br>";
						htmlString += "the next player has two chances to place a royal card.<br>";
						htmlString += "<br>";
						
						htmlString += "</center>\n";
						htmlString += "</body>\n</html>";

						// add html string to visual layout
						if(!(playersTextPane.getText().equals(htmlString)))
							playersTextPane.setText(htmlString);
						numberOfPlayers = count;

					}
				}
				// when the host clicks start game, change everything
				else if (messageFromServer.startsWith(Network.CMD_START_GAME)) {

					startGame();

				} else if (messageFromServer
						.startsWith(Network.CMD_SYSTEM_MESSAGE)) {
					// Display system messages
					final String sysMessage = messageFromServer
							.substring(Network.CMD_SYSTEM_MESSAGE.length());
					addChatMessageToMessageOutputTextArea(sysMessage);
				} else if (messageFromServer.startsWith(Network.CMD_VICTORY)) {
					// remove(cardsGUI);
					validate();
					repaint();
				} else if (messageFromServer
						.startsWith(Network.CMD_PLAYED_CARD)
						|| messageFromServer.startsWith(Network.CMD_SLAP)
						|| messageFromServer.startsWith(Network.CMD_TURN)
						|| messageFromServer
								.startsWith(Network.CMD_REQUEST_PLAY_A_CARD)
						|| messageFromServer
								.startsWith(Network.CMD_CARD_COUNTS)
						|| messageFromServer
								.startsWith(Network.CMD_CLEAR_CARDS)) {
					// TODO Do nothing
				} else {
					System.out.println("LobbyGUI - R -- " + messageFromServer);
				}
			}

		}

		private void startGame() {
			// inform user in chat panel
			addChatMessageToMessageOutputTextArea("---Game Start!---");

			// remove unnecessary panels
			remove(centerPanel);
			remove(nameTextField.getParent());

			// get players for the game panel
			final Player[] playersToUseInGame = new Player[numberOfPlayers];
			int i = 0;
			for (final int indexID : playersIDsAndNames.keySet()) {
				playersToUseInGame[i] = new Player(
						playersIDsAndNames.get(indexID), indexID);
				if (indexID == myID) {
					me = playersToUseInGame[i];
				}
				i++;
			}

			cardsGUI = new CardsGUI(1, me, playersToUseInGame,
					networkToUse.getNetworkQueue(isHost));
			add(cardsGUI, BorderLayout.CENTER);

			// aggressively force repaint
			cardsGUI.invalidate();
			cardsGUI.validate();
			cardsGUI.repaint();
			validate();
			repaint();

			// prevent lobby functions from running
			inLobby = false;
		}
	};

	private final int MARGIN_SIZE_DIVISOR = 2;
	JTextField messageInputTextField;
	JTextArea messageOutputTextArea;
	JTextField nameTextField;
	final Network networkToUse;

	int numberOfPlayers = 1;

	Runnable playerCounterRunnable = new Runnable() {
		@Override
		public void run() {
			Network.snooze(1000L);
			while (inLobby) {
				if (!networkToUse.isConnected()) {
					break;
				}

				Network.snooze(1000L);
				if (waitLongerWeGotIt) {
					waitLongerWeGotIt = false;
					Network.snooze(1000L);
					continue;
				}
				networkToUse.sendMessage(Network.CMD_ASK_SERVER_FOR_PLAYERS);
			}

		}
	};

	HashMap<Integer, String> playersIDsAndNames = new HashMap<Integer, String>();

	JEditorPane playersTextPane;
	JButton sendMessageButton;

	boolean waitLongerWeGotIt = false;

	/**
	 * Use this constructor if guest
	 * 
	 * @param networkToUse
	 */
	public LobbyGUI(final Network networkToUse) {
		this.networkToUse = networkToUse;
		isHost = false;
		setTitle("Client + " + networkToUse.getName());
		init();
	}

	/**
	 * Use this constructor if host
	 * 
	 * @param networkToUse
	 * @param networkHost
	 */
	public LobbyGUI(final Network networkToUse, final Network networkHost) {
		LobbyGUI.networkForServer = networkHost;
		isHost = true;
		this.networkToUse = networkToUse;
		setTitle("Host + " + networkForServer.getName());
		init();
	}

	void addChatMessageToMessageOutputTextArea(final String message) {

		messageOutputTextArea.setText(message + '\n'
				+ messageOutputTextArea.getText());
		messageOutputTextArea.setCaretPosition(0);
	}

	/**
	 * Perform all shared construction work
	 */
	private void init() {
		// request focus each time displayed
		setAutoRequestFocus(true);

		// add main panel
		setLayout(new BorderLayout());

		// get screen size
		// size of the screen
		final Dimension screenSize = Toolkit.getDefaultToolkit()
				.getScreenSize();
		setPreferredSize(new Dimension(screenSize.width - screenSize.width
				/ MARGIN_SIZE_DIVISOR, screenSize.height - screenSize.height
				/ (MARGIN_SIZE_DIVISOR + 1)));
		pack();
		System.out.println("Windows size is ( " + getPreferredSize().getWidth()
				+ " x " + getPreferredSize().getHeight() + " )");

		// center window
		setLocationRelativeTo(null);

		this.add(initNamePanel(networkForServer != null), BorderLayout.NORTH);
		this.add(initMessagePanel(), BorderLayout.SOUTH);

		// set up player text field in the center
		centerPanel = new JPanel(new GridLayout());
		playersTextPane = new JEditorPane();
		centerPanel.add(playersTextPane);

		playersTextPane.setContentType("text/html; charset=EUC-JP");
		playersTextPane.setEditable(false);

		// prepare default view
		String htmlString = "<html>\n<body>\n<center>\n<br><h2><u>Players in Lobby</u></h2>\n";
		htmlString += "Waiting for server data...<br>";
		htmlString += "</center>\n</body>\n</html>";
		playersTextPane.setText(htmlString);

		add(centerPanel, BorderLayout.CENTER);

		// set close operations
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent e) {

				if (networkForServer != null || !inLobby) {
					networkToUse.sendMessage(Network.CMD_END_GAME);
				} else {
					networkToUse.disconnect();
				}
				Network.snooze(3000L);
				if (networkForServer != null) {
					networkForServer.disconnect();
				}

			}

			@Override
			public void windowLostFocus(final WindowEvent e) {
				repaint();
				validate();
			}
		});

		// init Chat Thread
		final Thread listeningMessagingThread = new Thread(
				listeningMessagingRunnable);
		listeningMessagingThread.setDaemon(true);
		listeningMessagingThread.setName("Lobby - Client Listening Thread");
		listeningMessagingThread.start();

		final Thread playerCounterThread = new Thread(playerCounterRunnable);
		playerCounterThread.setDaemon(true);
		playerCounterThread.setName("Lobby - Client Player Manager Thread");
		playerCounterThread.start();
	}

	private JPanel initMessagePanel() {
		final JPanel basePanel = new JPanel();
		basePanel.setLayout(new BorderLayout());

		final JLabel messageInfo = new JLabel("Message: ");
		messageInputTextField = new JTextField();
		sendMessageButton = new JButton("Send");

		messageOutputTextArea = new JTextArea();
		messageOutputTextArea.setEditable(false);

		final JScrollPane jsp = new JScrollPane(messageOutputTextArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		jsp.setPreferredSize(new Dimension(50, 50));

		basePanel.add(messageInfo, BorderLayout.WEST);
		basePanel.add(messageInputTextField, BorderLayout.CENTER);
		basePanel.add(sendMessageButton, BorderLayout.EAST);
		basePanel.add(jsp, BorderLayout.NORTH);

		messageInputTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendChatMessage();
				}
			}
		});

		sendMessageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				sendChatMessage();
			}
		});

		return basePanel;
	}

	private JPanel initNamePanel(final boolean hosting) {
		final JPanel namePanel = new JPanel();
		final int height = 27;
		namePanel.setPreferredSize(new Dimension(1000, height + 10));

		final JLabel infoLabel = new JLabel("Your Username: ");
		final String name = networkToUse.getName();
		nameTextField = new JTextField(name);

		nameTextField.setPreferredSize(new Dimension(200, height));
		nameTextField.setHorizontalAlignment(SwingConstants.CENTER);
		namePanel.add(infoLabel);
		namePanel.add(nameTextField);

		nameTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					setNewName();
				}
			}
		});

		nameTextField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent e) {
				setNewName();
			}

			@Override
			public void focusGained(final FocusEvent e) {
				nameTextField.setSelectionStart(0);
				nameTextField.setSelectionEnd(nameTextField.getText().length());
			}
		});

		nameTextField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() >= 2) {
					nameTextField.setSelectionStart(0);
					nameTextField.setSelectionEnd(nameTextField.getText()
							.length());
				}
			}
		});

		nameTextField
				.setToolTipText("Press Enter to set name when done typing.");

		if (hosting) {
			final JButton startGameButton = new JButton("Start Game");
			namePanel.add(startGameButton);
			startGameButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					networkToUse.sendMessage(Network.CMD_START_GAME);
					startGameButton.setEnabled(false);
				}
			});
		}

		return namePanel;
	}

	void sendChatMessage() {
		final String toSend = messageInputTextField.getText();
		messageInputTextField.setText("");
		messageInputTextField.requestFocus();
		if (toSend.trim().equals("")) {
			return;
		}
		networkToUse.sendMessage(Network.CMD_MESSAGE + toSend);
		// addChatMessageToMessageOutputTextArea(getName() + " - " + toSend);

	}

	public void setNewName() {
		final String name = nameTextField.getText().trim();
		if (!name.equals("")) {
			if (!getName().equals(name)) {
				networkToUse.setName(name);
			}
			nameTextField.setText(name);
			messageInputTextField.requestFocus();
			setName(name);
		} else {
			setName("null");
			nameTextField.setText("null");
		}

	}
}
