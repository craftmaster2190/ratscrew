import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class CardsGUI extends JPanel {

	private static final int MARGIN_SIZE_DIVISOR = 3;
	private static final long serialVersionUID = 1L;

	/**
	 * Main method only for testing
	 * 
	 * @param args
	 */
	public static void main1(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final NetworkQueue testDummyNetworkQueue = new Network()
						.getNetworkQueue(true);
				final Player testFirstPlayer = new Player("Me", 0);
				// instantiate new GUI
				final CardsGUI gui = new CardsGUI(1, testFirstPlayer,
						new Player[] { testFirstPlayer, new Player("One", 0),
								new Player("Two", 0), new Player("Three", 0) },
						testDummyNetworkQueue);
				final JFrame frame = new JFrame();
				frame.setLayout(new GridLayout());
				frame.add(gui);

				// get screen size
				// size of the screen
				final Dimension screenSize = Toolkit.getDefaultToolkit()
						.getScreenSize();
				final Dimension dimension = new Dimension(screenSize.width
						- screenSize.width / MARGIN_SIZE_DIVISOR,
						screenSize.height - screenSize.height
								/ MARGIN_SIZE_DIVISOR);
				frame.setPreferredSize(dimension);

				// debug string
				System.out.println("Windows size is ( "
						+ frame.getPreferredSize().getWidth() + " x "
						+ frame.getPreferredSize().getHeight() + " )");
				frame.pack();

				// request focus each time displayed
				frame.setAutoRequestFocus(true);
				// center window
				frame.setLocationRelativeTo(null);

				// display window
				frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				frame.setVisible(true);

			}
		});

	}

	private final ArrayList<Card> cards = new ArrayList<Card>();

	private Pile middlePile;

	final NetworkQueue networkQueue;

	private int numberOfDecksOnTable;
	private final int numberOfPlayers;
	private final JPanel myPilePanel;
	final Player playerOne;
	final Player[] players;

	final SpecialCardsPanel mainCardsSpecialPanel;
	volatile Player currentPlayerTurn;
	final JButton playButton;

	volatile boolean noWinner = true;

	volatile int royalCount = -1;
	private Player royalPlayer;
	boolean stealSlap = false;

	/**
	 * Constructor
	 * 
	 * @param numberOfDecks
	 * @param numberOfPlayers
	 * @param networkQueue
	 *            should be passed from the LobbyGUI
	 */
	public CardsGUI(final int numberOfDecks, final Player playerOne,
			final Player[] players, final NetworkQueue networkQueue) {

		// handle players
		this.playerOne = playerOne;
		this.players = players;
		numberOfPlayers = players.length;

		// set host as first player
		currentPlayerTurn = playerOne;

		// handle network
		this.networkQueue = networkQueue;

		// handle decks/cards
		numberOfDecksOnTable = numberOfDecks;

		// init layout
		setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER, 50, 50);

		// init panels
		final JPanel otherPlayersTopPanel = new JPanel(flowLayout);
		myPilePanel = new JPanel(flowLayout);
		flowLayout = new FlowLayout(FlowLayout.LEADING, 50, 50);
		mainCardsSpecialPanel = new SpecialCardsPanel();
		mainCardsSpecialPanel.setLayout(flowLayout);

		// init playButton
		final Font buttonFont = new Font("", Font.BOLD, 25);
		playButton = new JButton("Play!");
		playButton.setFont(buttonFont);
		playButton.setEnabled(false);

		// add player panels to top panel
		for (int index = 0; index < numberOfPlayers; index++) {
			// init card for back image
			final Card backOfCard = new Card(Card.VALUE_ACE, Card.SUIT_SPADES);
			backOfCard.setFaceUp(false);

			// create a panel to hold current player
			final JPanel currentPlayerPanel = new JPanel(
					new BorderLayout(0, 12));
			final JLabel currentCardsJLabel = backOfCard.getjLabel();

			// if playerOne, handle the pile and playerOne's stuff
			if (players[index] == playerOne) {
				final JPanel innerMyPilePanel = new JPanel(new BorderLayout(0,
						12));

				innerMyPilePanel.add(playButton, BorderLayout.NORTH);
				innerMyPilePanel.add(playerOne, BorderLayout.SOUTH);
				innerMyPilePanel.add(currentCardsJLabel, BorderLayout.CENTER);

				myPilePanel.add(innerMyPilePanel);
			}
			// add player to other players in north panel
			else {

				currentPlayerPanel.add(currentCardsJLabel, BorderLayout.CENTER);

				// add player's name
				final JLabel playerNameLabel = new JLabel(
						players[index].getName());

				playerNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
				currentPlayerPanel.add(playerNameLabel, BorderLayout.NORTH);
				currentPlayerPanel.add(players[index], BorderLayout.SOUTH);

				otherPlayersTopPanel.add(currentPlayerPanel);
			}
			players[index].setHorizontalAlignment(SwingConstants.CENTER);
		}

		// add play button click listener
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				ratScrew_clickPlayCard();
			}
		});

		// add top and bottom panels
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 0.8;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 5;
		add(otherPlayersTopPanel, c);

		// add bottom pile panel
		c.weightx = 0.2;
		c.weighty = 0.2;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		add(myPilePanel, c);

		// add bottom player's control panel
		c.weightx = 0.8;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 4;
		add(mainCardsSpecialPanel, c);

		if (networkQueue.isHost()) {
			initHost();
		} else {
			initGuest();
		}

	}

	private void initGuest() {
		final Thread guestThread = new Thread(new Runnable() {

			@Override
			public void run() {
				int count = 0;
				while (true) {
					Network.snooze(10L);
					String messageFromServer = networkQueue
							.getMessageFromQueue();
					if (messageFromServer == null) {
						count++;
						if (networkQueue.isHost() && count > 500) {
							networkQueue.sendMessageToQueue(Network.CMD_TURN
									+ currentPlayerTurn.getID());
							count = 0;
						}
						continue;
					}

					if (messageFromServer.startsWith(Network.CMD_PLAYED_CARD)) {
						messageFromServer = messageFromServer
								.substring(Network.CMD_PLAYED_CARD.length());
						final int cardValue = StringParser
								.splitAndGetID(messageFromServer);
						final int cardSuit = Integer.parseInt(StringParser
								.splitAndDecodeName(messageFromServer));
						ratScrew_clientUpdatePlayedCards(cardValue, cardSuit);
					} else if (messageFromServer
							.startsWith(Network.CMD_REQUEST_PLAY_A_CARD)
							&& noWinner) {
						if (networkQueue.isHost()) {
							messageFromServer = messageFromServer
									.substring(Network.CMD_REQUEST_PLAY_A_CARD
											.length());
							ratScrew_setCurrentPlayerByID(Integer
									.parseInt(messageFromServer));
							ratScrew_hostDeterminePlayCard();
							sendCardCounts();
						}

					} else if (messageFromServer
							.startsWith(Network.CMD_CARD_COUNTS)) {
						messageFromServer = messageFromServer
								.substring(Network.CMD_CARD_COUNTS.length());
						final String[] strings = StringParser
								.splitPlayers(messageFromServer);
						final int[] ids = new int[strings.length];
						final int[] vals = new int[strings.length];
						int i = 0;
						for (final String s : strings) {
							ids[i] = StringParser.splitAndGetID(s);
							vals[i] = Integer.parseInt(StringParser
									.splitAndDecodeName(s));
							i++;
						}

						for (final Player p : players) {
							for (i = 0; i < ids.length; i++) {
								if (p.getID() == ids[i]) {
									if (ids[i] == playerOne.getID()) {
										if (vals[i] == 0) {
											// if network says I am out of
											// cards, disable play button
											playButton.setEnabled(false);
										}
									}
									p.updateText(vals[i]);
									break;
								}
							}
						}

					} else if (messageFromServer.startsWith(Network.CMD_TURN)
							&& noWinner) {
						messageFromServer = messageFromServer
								.substring(Network.CMD_TURN.length());
						final int currentPlayerTurnIndex = Integer
								.parseInt(messageFromServer);
						for (final Player p : players) {
							if (p.getID() == currentPlayerTurnIndex) {
								currentPlayerTurn = p;
								break;
							}
						}
						// if turn belongs to current user, enable play button
						playButton.setEnabled(currentPlayerTurn == playerOne);
					} else if (messageFromServer.startsWith(Network.CMD_SLAP)
							&& networkQueue.isHost()) {
						messageFromServer = messageFromServer
								.substring(Network.CMD_SLAP.length());
						final int idOfSlapper = Integer
								.parseInt(messageFromServer);
						Player slappingPlayer = null;
						for (final Player p : players) {
							if (p.getID() == idOfSlapper) {
								slappingPlayer = p;
								break;
							}
						}
						ratScrew_hostCalcSlap(slappingPlayer);
					} else if (messageFromServer
							.startsWith(Network.CMD_CLEAR_CARDS)) {
						Network.snooze(1000L);
						mainCardsSpecialPanel.emptyAllCards();
					} else if (messageFromServer
							.startsWith(Network.CMD_VICTORY)) {
						playButton.setEnabled(false);
						String winnerString = messageFromServer.substring(Network.CMD_VICTORY.length());
						int winnerID = Integer.parseInt(winnerString); 
						if(winnerID == playerOne.getID())
							playButton.setText("WINNER!");
						else
							playButton.setText("LOSER!");
						Network.snooze(3000L);
						break;
					}

					else if (messageFromServer.startsWith(Network.CMD_MESSAGE)
							|| messageFromServer
									.startsWith(Network.CMD_SYSTEM_MESSAGE)
							|| messageFromServer.startsWith(Network.CMD_SLAP)) {
						// Do nothing
					} else {
						System.err.println("CardsGUI - R --- "
								+ messageFromServer);
					}
				}
			}

		});
		guestThread.setDaemon(true);
		guestThread.setName("CardsGUI - Guest Thread");
		guestThread.start();

	}

	private void initHost() {

		// add decks of cards
		initCardDecks(numberOfDecksOnTable);

		initGuest();

		sendCardCounts();

		playButton.setEnabled(true);
	}

	void sendCardCounts() {
		String cardCounts = "";

		boolean first = true;
		for (final Player p : players) {
			final String nextCardCount = StringParser.concat(p.getID(),
					p.getCardCount());
			if (first) {
				cardCounts = nextCardCount;
				first = false;
				continue;
			}
			cardCounts = StringParser.stringPairsTogether(nextCardCount,
					cardCounts);
		}
		networkQueue.sendMessageToQueue(Network.CMD_CARD_COUNTS + cardCounts);
	}

	/**
	 * This method is called when the player clicks a card
	 */
	void ratScrew_slap() {
		networkQueue.sendMessageToQueue(Network.CMD_SLAP + playerOne.getID());
	}

	/**
	 * This method is called when the host must calculate the who gets the slapped card
	 * 
	 * @param slapper
	 */
	void ratScrew_hostCalcSlap(final Player slapper) {
		if (middlePile == null) {
			return;
		}
//		for (int i = 1; i <= 3; i++) {
//			System.out.print(middlePile.viewCardAtNFromTop(i) + "-");
//		}
//		System.out.println();
		if (middlePile.doble() || middlePile.sandwich()
				|| middlePile.marriage()) {
			middlePile.givePileToPlayer(slapper);
			rotateTurns(slapper);
			sendSystemMessage(slapper.getName()
					+ " slapped and stole the pile.");
			royalCount = -1;
			stealSlap = true;
			networkQueue.sendMessageToQueue(Network.CMD_CLEAR_CARDS);
			sendCardCounts();
		} else {
			// TODO take two cards from slapper and put them in the pile
			sendSystemMessage(slapper.getName() + " slapped falsely.");
		}
		ratScrew_determineVictory();
	}

	void ratScrew_clickPlayCard() {
		if (!ratScrew_checkIfMyTurn()) {
			return;
		}
		networkQueue.sendMessageToQueue(Network.CMD_REQUEST_PLAY_A_CARD
				+ playerOne.getID());
		playButton.setEnabled(false);
	}

	boolean ratScrew_determineVictory() {
		// if at the end of a royal spree, a player with no cards may reenter the game, make note of it.
//		if (royalCount > 0) {
//			return false;
//		}
		if (calcNextPlayer() == null) {
			sendCardCounts();
			sendSystemMessage(currentPlayerTurn.getName() + " has won!!!");
			networkQueue.sendMessageToQueue(Network.CMD_VICTORY
					+ currentPlayerTurn.getID());
			noWinner = false;
			playButton.setEnabled(false);
			return true;
		}
		return false;
	}

	void ratScrew_hostDeterminePlayCard() {

		final Card topCard = currentPlayerTurn.getTopCard();

		// if the the host does not have cards, disable the play button
		if (!playerOne.hasCards()) {
			playButton.setEnabled(false);
		}

		networkQueue.sendMessageToQueue(Network.CMD_PLAYED_CARD
				+ StringParser.concat(topCard.getValue(), topCard.getSuit()));

		// determine if royal whose turn it is
		if (topCard.isRoyal()) {
			royalPlayer = currentPlayerTurn;
		}
		if (royalCount == -1 || topCard.isRoyal()) {
			currentPlayerTurn = calcNextPlayer();
		} else {
			royalCount--;
			if (!currentPlayerTurn.hasCards()) {
				currentPlayerTurn = calcNextPlayer();
			}
		}

		if (royalCount == 0 && !topCard.isRoyal()) {

			// give pile to previous player
			currentPlayerTurn = royalPlayer;

		}
		// else leave current player just the way they are

		if (topCard.isRoyal()) {
			switch (topCard.getValue()) {
			case Card.VALUE_ACE:
				royalCount = 4;
				break;
			case Card.VALUE_KING:
				royalCount = 3;
				break;
			case Card.VALUE_QUEEN:
				royalCount = 2;
				break;
			default: // Jack
				royalCount = 1;
			}
		}
		if (!ratScrew_determineVictory()) {
			networkQueue.sendMessageToQueue(Network.CMD_TURN
					+ currentPlayerTurn.getID());
		}
	}

	void ratScrew_setCurrentPlayerByID(final int id) {
		for (final Player p : players) {
			if (p.getID() == id) {
				currentPlayerTurn = p;
			}
		}
	}

	void ratScrew_clientUpdatePlayedCards(final int value, final int suit) {
		final Card nextCard = new Card(value, suit);
		nextCard.setFaceUp(true);
		if (middlePile != null) {
			// Fix bug that causes two of the same card to appear
			if (!(middlePile.viewTopCard() == null)
					&& middlePile.viewTopCard().equals(nextCard)) {
				System.out.println("Problem!");
				return;
			}
			middlePile.addCardToPile(nextCard);
			System.out.println("\t" + nextCard);
			if (royalCount == 0) {
				if (!(middlePile.doble() || middlePile.sandwich() || middlePile
						.marriage())) {
					middlePile.givePileToPlayer(royalPlayer);

					rotateTurns(royalPlayer);
					sendSystemMessage(royalPlayer.getName()
							+ " completed the royal spree and stole the pile.");
					networkQueue.sendMessageToQueue(Network.CMD_CLEAR_CARDS);
					sendCardCounts();
				} else {
					sendSystemMessage("Opportunity to steal!");
				}
				royalCount--;
			}
		}
		final JLabel currentCardJLabel = nextCard.getjLabel();

		mainCardsSpecialPanel.addSpecial(currentCardJLabel);

		getTopLevelAncestor().repaint();
		getTopLevelAncestor().validate();
	}

	boolean ratScrew_checkIfMyTurn() {
		final boolean isMyTurn = currentPlayerTurn == playerOne;
		if (!isMyTurn) {
			playButton.setEnabled(false);
		}
		return isMyTurn;
	}

	void sendSystemMessage(final String message) {
		final String s = " --- " + message + " ---";
		networkQueue.sendMessageToQueue(Network.CMD_SYSTEM_MESSAGE + s);
	}

	void rotateTurns() {
		currentPlayerTurn = calcNextPlayer();
		networkQueue.sendMessageToQueue(Network.CMD_TURN
				+ currentPlayerTurn.getID());
	}

	void rotateTurns(final Player nextTurnPlayer) {
		currentPlayerTurn = nextTurnPlayer;
		networkQueue.sendMessageToQueue(Network.CMD_TURN
				+ currentPlayerTurn.getID());
	}

	/**
	 * Figures out the next player, returns null if everyone else is out of cards
	 * 
	 * @return
	 */
	Player calcNextPlayer() {

		int currentPlayerTurnIndex = 0;
		for (int i = 0; i < players.length; i++) {
			if (players[i] == currentPlayerTurn) {
				currentPlayerTurnIndex = i;
				break;
			}
		}

		int i = currentPlayerTurnIndex;
		while (true) {
			i++;
			if (i > players.length - 1) {
				i = 0;
			}
			if (players[i].hasCards()) {
				if (players[i] == currentPlayerTurn) {
					return null;
				}
				return players[i];
			}

		}
	}

	/**
	 * Figures out the previous player, returns null if everyone else is out of cards
	 * 
	 * @return
	 */
	Player calcPreviousPlayer() {
		int currentPlayerTurnIndex = 0;
		for (int i = 0; i < players.length; i++) {
			if (players[i] == currentPlayerTurn) {
				currentPlayerTurnIndex = i;
				break;
			}
		}

		int i = currentPlayerTurnIndex;
		while (true) {
			i--;
			if (i < 0) {
				i = players.length - 1;
			}
			if (players[i].hasCards()) {
				if (players[i] == currentPlayerTurn) {
					return null;
				}
				return players[i];
			}

		}
	}

	/**
	 * Get cards in the GUI
	 * 
	 * @return
	 */
	public ArrayList<Card> getCards() {
		return cards;
	}

	/**
	 * 
	 * @return the number of decks on the table
	 */
	public int getNumberOfDecksOnTable() {
		return numberOfDecksOnTable;
	}

	/**
	 * Get the number of players at the table
	 * 
	 * @return
	 */
	public int getNumberOfPlayers() {
		return numberOfPlayers;
	}

	/**
	 * Add decks of cards to the table
	 * 
	 * @param numberOfDecksOnTable1
	 */
	protected void initCardDecks(final int numberOfDecksOnTable1) {
		numberOfDecksOnTable = numberOfDecksOnTable1;
		// create decks
		for (int i = 0; i < numberOfDecksOnTable1; i++) {
			cards.addAll(Arrays.asList(Card.createDeckOf52Cards()));
		}
		
		

		// shuffle cards
		Card.shuffle(cards);

		// set face down
		for (final Card c : cards) {
			c.setFaceUp(false);
		}

		// add decks to table
		middlePile = new Pile(cards);

		// deal pile to all players
		middlePile.dealPileToPlayers(players);

		// debug string
		System.out.println("There are " + getNumberOfDecksOnTable()
				+ " decks of cards on the table.");
	}

	class SpecialCardsPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private final MouseAdapter slapListener = new MouseAdapter() {
			public void mouseClicked(final java.awt.event.MouseEvent e) {
				// if a user double clicks a card, ie. slap it
				if (e.getClickCount() >= 2) {
					ratScrew_slap();
				}
			}
		};
		ArrayList<JLabel> labels = new ArrayList<JLabel>();
		static final int MAX_SHOW = 5;

		/**
		 * Add a card label to this panel
		 * 
		 * @param label
		 */
		void addSpecial(final JLabel label) {
			// if there are too many labels, get rid of the oldest one
			if (labels.size() >= MAX_SHOW) {
				labels.remove(0);
			}
			// add the new label to the list
			labels.add(label);

			// clear out the old cards
			removeAll();

			// add the labels back in to the panel
			for (int i = labels.size() - 1, j = 0; i >= 0; i--, j++) {
				add(labels.get(i));
				// only add the click listener to the first three cards
				if (j < 3) {
					labels.get(i).addMouseListener(slapListener);
				}
			}
		}

		/**
		 * remove all cards and remove mouse listeners
		 */
		@Override
		public void removeAll() {
			// remove listeners
			for (final Component c : getComponents()) {
				c.removeMouseListener(slapListener);
			}
			// remove labels
			super.removeAll();
		}

		/**
		 * remove all cards and remove mouse listeners
		 */
		public void emptyAllCards() {
			removeAll();
			labels.clear();
			getTopLevelAncestor().repaint();
			getTopLevelAncestor().validate();
		}
	}

}
