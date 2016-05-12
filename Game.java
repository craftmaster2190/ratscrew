/**
 * Class for the operations of the game
 * @author Bryce J. Fisher, Carlos Arredondo
 */
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class Game {
	final Scanner input = new Scanner(System.in);
	private Pile middlePile;
	private Player[] players;
	private boolean hasSlapped;
	private int currentPlayerNumber;
	private Player currentPlayer;
	private Player nextPlayer;

	public Game(final int numberOfPlayers) {
		// int numberOfPlayers=2;

		// create cards
		final Card[] deck = Card.createDeckOf52Cards();
		Card.shuffle(deck);

		players = new Player[numberOfPlayers];
		for (int i = 0; i < numberOfPlayers; i++) {
			players[i] = new Player("Player " + i, 0, i != 0);

		}
		currentPlayer = players[0];
		nextPlayer = players[1];

		middlePile = new Pile(deck);
		middlePile.dealPileToPlayers(players);

		// Begin playing game
		new SlappButton();
		System.out.println("Press Enter to throw a card");
		System.out.println("Press the button");

		currentPlayerNumber = 1;
		boolean royalPlay = false;
		while (true) {
			// rotate turns if not royal play
			if (!royalPlay) {
				rotateTurns();

			} else
				royalPlay = false;

			// handle a player losing all his/her cards
			if (!currentPlayer.hasCards()) {
				snoozeforOneSecond();
				System.out.println(currentPlayer.getName()
						+ " is out of cards!!!");
				// skip current player if out of cards
				continue;
			}

			// handle royal cards
			final Card topCardOnThePile = middlePile.viewTopCard();
			final boolean topCardIsRoyal = topCardOnThePile != null
					&& topCardOnThePile.isRoyal();
			if (topCardIsRoyal) {
				while (topCardIsRoyal) {
					System.out.println("\tThis is royal!");
					final boolean nextPlayerDroppedARoyal = dealRoyal(
							topCardOnThePile, nextPlayer, middlePile);
					if (hasSlapped)
						break;
					// if the next player does not drop a royal card, give the
					// pile
					// to the current player
					if (!nextPlayerDroppedARoyal) {
						snoozeforOneSecond();
						System.out.println("\tThe pile is given to "
								+ currentPlayer.getName());
						System.out.println("\t" + currentPlayer.getName()
								+ " now has " + currentPlayer.viewCards().length
								+ " cards.");
						middlePile.shuffleCards();
						middlePile.givePileToPlayer(currentPlayer);
						royalPlay = true;
						break;
					}
					// if the next player drops a royal, rotate players and keep
					// it
					// going
					snoozeforOneSecond();
					break;
				}
			} else {
				// if human player, wait for return key
				if (currentPlayer == players[0]) {
					System.out.println("\tYour turn! Press Enter.");
					input.nextLine();
				} else {
					// wait one second
					snoozeforOneSecond();
				}

				// place the current player's card on the top of the pile
				middlePile.addCardToPile(currentPlayer.getBottomCard());
				System.out.println(currentPlayer.getName() + " played "
						+ middlePile.viewTopCard());
				royalPlay = middlePile.viewTopCard().isRoyal();
			}
			// check if there is a winner
			Player winningPlayer = checkVictory(players);
			if (winningPlayer != null) {
				System.out.println(winningPlayer.getName()
						+ " won! All other players are out of cards.");
				break;
			}

		}

		input.close();
	}

	private void rotateTurns() {
		// printNumberOfCards(players);
		currentPlayer = nextPlayer;
		currentPlayerNumber++;
		nextPlayer = currentPlayerNumber == players.length ? players[0]
				: players[currentPlayerNumber];
		if (currentPlayerNumber == players.length)
			currentPlayerNumber = 0;
	}

	@SuppressWarnings("unused")
	private void rotateTurns(Player newCurrentPlayer) {
		currentPlayer = newCurrentPlayer;
		boolean setNextPlayer = false;
		for (int i = 0; i < players.length; i++) {
			if (setNextPlayer) {
				nextPlayer = players[i];
			}
			if (players[i] == newCurrentPlayer) {
				setNextPlayer = true;
				currentPlayerNumber = i;
			}

		}
		if (setNextPlayer) {
			nextPlayer = players[0];
		}
		currentPlayer = nextPlayer;
		currentPlayerNumber++;
		nextPlayer = currentPlayerNumber == players.length ? players[0]
				: players[currentPlayerNumber];
		if (currentPlayerNumber == players.length)
			currentPlayerNumber = 0;
	}

	public void printNumberOfCards(Player[] players) {
		int totalNumberOfCardsInPlay = 0;
		for (Player currentPlayer : players) {
			int playerCardCount = currentPlayer.viewCards().length;
			totalNumberOfCardsInPlay += playerCardCount;
			System.out.print(currentPlayer.getName() + "(" + playerCardCount
					+ ")");
			System.out.print(" - ");
		}
		int middlePileSize = middlePile.getCards().length;
		totalNumberOfCardsInPlay += middlePileSize;
		System.out.println("Pile:(" + middlePileSize + ") - Total("
				+ totalNumberOfCardsInPlay + ")");
	}

	/**
	 * If only one player has cards, return that player, otherwise null.
	 * 
	 * @param players
	 * @return
	 */
	public Player checkVictory(Player[] players) {
		int count = 0;
		Player winningPlayer = null;
		for (Player currentPlayer : players) {
			if (currentPlayer.hasCards()) {
				count++;
				winningPlayer = currentPlayer;
			}
		}
		if (count != 1)
			winningPlayer = null;
		return winningPlayer;
	}

	public boolean dealRoyal(final Card startingCard, final Player nextPlayer,
			final Pile middlePile) {

		if (hasSlapped)
			return false;

		// if nextPlayer is out of Cards, give the pile to the next player

		if (!nextPlayer.hasCards())
			return false;
		if (startingCard.getValue() >= Card.VALUE_JACK
				|| startingCard.getValue() == Card.VALUE_ACE) {
			if (!nextPlayer.isComputer()) {
				System.out.println("\tYour turn! Press Enter.");
				input.nextLine();
			} else
				snoozeforOneSecond();
			// Jack = 1 more
			final Card currentCard = nextPlayer.getBottomCard();
			System.out.println("This is " + nextPlayer.getName()
					+ "'s first card: " + currentCard);
			middlePile.addCardToPile(currentCard);
			if (currentCard.isRoyal()) {
				return true;
			}
		}

		if (hasSlapped)
			return false;

		if (!nextPlayer.hasCards())
			return false;
		if (startingCard.getValue() >= Card.VALUE_QUEEN
				|| startingCard.getValue() == Card.VALUE_ACE) {
			if (!nextPlayer.isComputer()) {
				System.out.println("\tYour turn! Press Enter.");
				input.nextLine();
			} else
				snoozeforOneSecond();
			// Queen = 2 more
			final Card currentCard = nextPlayer.getBottomCard();
			System.out.println("This is " + nextPlayer.getName()
					+ "'s second card: " + currentCard);
			middlePile.addCardToPile(currentCard);
			if (currentCard.isRoyal()) {
				return true;
			}
		}

		if (hasSlapped)
			return false;

		if (!nextPlayer.hasCards())
			return false;
		if (startingCard.getValue() >= Card.VALUE_KING
				|| startingCard.getValue() == Card.VALUE_ACE) {
			if (!nextPlayer.isComputer()) {
				System.out.println("\tYour turn! Press Enter.");
				input.nextLine();
			} else
				snoozeforOneSecond();
			// King = 3 more
			final Card currentCard = nextPlayer.getBottomCard();
			System.out.println("This is " + nextPlayer.getName()
					+ "'s third card: " + currentCard);
			middlePile.addCardToPile(currentCard);
			if (currentCard.isRoyal()) {
				return true;
			}
		}

		if (hasSlapped)
			return false;

		if (!nextPlayer.hasCards())
			return false;
		if (startingCard.getValue() == Card.VALUE_ACE) {
			if (!nextPlayer.isComputer()) {
				System.out.println("\tYour turn! Press Enter.");
				input.nextLine();
			} else
				snoozeforOneSecond();
			// Ace = 4 more
			final Card currentCard = nextPlayer.getBottomCard();
			System.out.println("This is " + nextPlayer.getName()
					+ "'s fourth card: " + currentCard);
			middlePile.addCardToPile(currentCard);
			if (currentCard.isRoyal()) {
				return true;
			}
		}
		return false;
	}

	public static void snoozeforOneSecond() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	// method for slapping.
	public String slapp(Pile middlePile, Player slappingPlayer) {

		if (middlePile.getCards().length == 2) {
			if (middlePile.doble()) {
				middlePile.givePileToPlayer(slappingPlayer);
				hasSlapped = true;
				return "It's double!";
			} else if (middlePile.marriage()) {
				middlePile.givePileToPlayer(slappingPlayer);
				hasSlapped = true;
				return "It's Marriage!";
			} else
				middlePile.addCardToPile(slappingPlayer.getTopCard());
			middlePile.addCardToPile(slappingPlayer.getTopCard());
			return "No rule applies. 2 cards have been drawn from "
					+ slappingPlayer.getName() + " to the middle pile";
		} else if (middlePile.getCards().length >= 3) {
			if (middlePile.doble()) {
				middlePile.givePileToPlayer(slappingPlayer);
				return "It's double!";
			} else if (middlePile.marriage()) {
				middlePile.givePileToPlayer(slappingPlayer);
				return "It's Marriage!";
			} else if (middlePile.sandwich()) {
				middlePile.givePileToPlayer(slappingPlayer);
				return "It's Sandwich!";
			} else
				middlePile.addCardToPile(slappingPlayer.getTopCard());
			middlePile.addCardToPile(slappingPlayer.getTopCard());
			return "No rule applies. 2 cards have been drawn from "
					+ slappingPlayer.getName() + " to the middle pile";
		} /*
		 * else if (middlePile.getCards().length >= 4) { if (middlePile.doble())
		 * { middlePile.givePileToPlayer(slappingPlayer); return "It's double!";
		 * } else if (middlePile.marriage()) {
		 * middlePile.givePileToPlayer(slappingPlayer); return "It's Marriage!";
		 * } else if (middlePile.sandwich()) {
		 * middlePile.givePileToPlayer(slappingPlayer); return "It's Sandwich!";
		 * } else if (middlePile.four()) {
		 * middlePile.givePileToPlayer(slappingPlayer); return "It's Four"; }
		 * else middlePile.addCardToPile(slappingPlayer.getTopCard());
		 * middlePile.addCardToPile(slappingPlayer.getTopCard()); return
		 * "No rule applies. 2 cards have been drawn from " +
		 * slappingPlayer.getName() + " to the middle pile"; }
		 */
		return "Not Enough Cards";
	}

	// button for slapping
	class SlappButton extends JFrame {

		private static final long serialVersionUID = 1L;

		public SlappButton() {
			Point centerPoint = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getCenterPoint();
			this.setBounds(centerPoint.x + centerPoint.x / 2, centerPoint.y
					+ centerPoint.y / 2, 150, 150);
			JPanel centerPanel = new JPanel(new GridLayout());
			JButton slapper = new JButton("Slapp!");
			slapper.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println(slapp(middlePile, players[0]));
					// rotateTurns(players[0]);
				}
			});
			centerPanel.add(slapper);
			this.add(centerPanel);
			this.setAlwaysOnTop(true);
			this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			this.setVisible(true);
		}
	}
}