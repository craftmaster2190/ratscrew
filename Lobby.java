import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Main method for the game. The selection of the game, number of players, and hosting the game are portrayed here.
 * 
 * @author Bryce J. Fisher, Carlos Arredondo
 */

public class Lobby {

	private static final int LOBBY_CHOOSE_MODE = 0;
	private static final int LOBBY_HOST_MODE = 1;
	private static final int LOBBY_JOIN_MODE = 2;
	// private static final int NETWORK_PLAY_MODE = 3;
	// private static final int NETWORK_PAUSE_MODE = 4;
	private static final int NETWORK_SHUTDOWN_MODE = 5;
	private static final int NETWORK_SINGLE_PLAYER_MODE = 6;

	private static final int GAME_EGYPTIAN_RATCREW = 1;
	// private static final int GAME_POKER = 2;
	// private static final int GAME_HEARTS = 3;

	private static int mode = LOBBY_CHOOSE_MODE;
	protected static int game = LOBBY_CHOOSE_MODE;
	private static int numberOfTotalPlayers = 0;
	private static int numberOfUserPlayers = 0;
	private static int numberOfComputerAIs = 0;

	@SuppressWarnings("unused")
	public static void main(final String[] args) {
		final Scanner input = new Scanner(System.in);

		// prompt user to host or join game
		while (true) {
			System.out.print("(H)ost, (J)oin, (E)xit ? ");
			final String in = input.nextLine().trim();
			if (in.length() > 1 || in.length() == 0) {
				System.out
						.println("Invalid Input: should be only one character.");
				continue;
			}
			final char c = in.toLowerCase().charAt(0);
			if (c == 'h') {
				mode = LOBBY_HOST_MODE;
				break;
			} else if (c == 'j') {
				mode = LOBBY_JOIN_MODE;
				break;
			} else if (c == 'e') {
				mode = NETWORK_SHUTDOWN_MODE;
				break;
			} else {
				System.out.println("Invalid Input: should be h,j, or e.");
				continue;
			}
		}

		if (mode == LOBBY_HOST_MODE) {

			// prompt user to select game
			while (true) {
				System.out
						.print("Select a game: (E)gyptian Ratscrew, (P)oker, (H)earts ");
				final String in = input.nextLine().trim();
				if (in.length() > 1 || in.length() == 0) {
					System.out
							.println("Invalid Input: should be only one character.");
					continue;
				}
				final char c = in.toLowerCase().charAt(0);
				if (c == 'e') {
					game = GAME_EGYPTIAN_RATCREW;
					break;
				} else if (c == 'p') {
					System.out.println("Invalid Game: Poker is not ready yet.");
					continue;
				} else if (c == 'h') {
					System.out
							.println("Invalid Game: Hearts is not ready yet.");
					continue;
				} else {
					System.out.println("Invalid Input: should be h,j, or e.");
					continue;
				}
			}

			// prompt user to select max number of players
			while (true) {
				System.out.print("Select number of players (2 - 9) ");
				final String in = input.nextLine().trim();
				if (in.length() > 1 || in.length() == 0) {
					System.out
							.println("Invalid Input: should be only one character.");
					continue;
				}

				try {
					numberOfTotalPlayers = Integer.parseInt(in);
				} catch (final NumberFormatException e) {
					System.out.println("Invalid Input: should be a number.");
					continue;
				}

				if (numberOfTotalPlayers < 2 || numberOfTotalPlayers > 9) {
					System.out.println("Invalid Input: should within 2 - 9.");
					continue;
				}
				break;
			}

			// prompt user to select number of computer players
			final int numberOfComputerPlayerBounds = numberOfTotalPlayers - 1;
			if (numberOfComputerPlayerBounds != 1) {
				final String numberOfComputerPlayerBoundsString = 1 + " - "
						+ numberOfComputerPlayerBounds;
				while (true) {
					System.out.print("Select number of computer players ("
							+ numberOfComputerPlayerBoundsString + ") ");
					final String in = input.nextLine().trim();
					if (in.length() > 1 || in.length() == 0) {
						System.out
								.println("Invalid Input: should be only one character.");
						continue;
					}

					try {
						numberOfComputerAIs = Integer.parseInt(in);
					} catch (final NumberFormatException e) {
						System.out
								.println("Invalid Input: should be a number.");
						continue;
					}

					if (numberOfComputerAIs < 1
							|| numberOfComputerAIs > numberOfComputerPlayerBounds) {
						System.out.println("Invalid Input: should within "
								+ numberOfComputerPlayerBoundsString + ".");
						continue;
					}
					break;
				}
				if (numberOfComputerPlayerBounds == numberOfComputerAIs) {
					mode = NETWORK_SINGLE_PLAYER_MODE;
				}
			} else {
				// if there is only one other player, prompt the user for
				// computer opponent or human
				while (true) {
					System.out
							.print("Would you like a computer opponent? (Y/N) ");
					final String in = input.nextLine().trim();
					if (in.length() > 1 || in.length() == 0) {
						System.out
								.println("Invalid Input: should be only one character.");
						continue;
					}
					final char c = in.toLowerCase().charAt(0);
					if (c == 'y') {
						mode = NETWORK_SINGLE_PLAYER_MODE;
						break;
					} else if (c == 'n') {

						break;
					} else {
						System.out.println("Invalid Input: should be y or n.");
						continue;
					}
				}
			}

			numberOfUserPlayers = numberOfTotalPlayers - numberOfComputerAIs;
		}

		// decide what to do
		if (mode == NETWORK_SINGLE_PLAYER_MODE) {
			// pass npcs to game class with a blank network
			System.out.println("Starting single player game with "
					+ numberOfComputerAIs + " npc(s)");
			new Game(numberOfTotalPlayers);
		} else if (mode == LOBBY_HOST_MODE) {
			// display network address and start network threads
			System.out.println("Waiting on " + numberOfUserPlayers
					+ " players....");
			// create a host and join it
			final Network hostingNetwork = new Network(numberOfUserPlayers);
			// Network clientNetwork = new Network(hostingNetwork.getAddress());
		} else if (mode == LOBBY_JOIN_MODE) {
			// Get network address to join
			System.out.println("Enter a network address...");
			// new Network( inet address );
			try {
				final Network clientNetwork = new Network(
						InetAddress.getLocalHost());
			} catch (final UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("Exiting...");
		}
		input.close();
	}
}
