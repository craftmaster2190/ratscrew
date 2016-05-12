import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * This class will maintain a pile of cards. Should be used for Pile of Cards in the middle of table, but can be used to
 * maintain piles of cards owner by each player.
 * 
 * @author Bryce J. Fisher, Carlos Arredondo
 * 
 */
public class Pile {

	private final ArrayList<Card> cards = new ArrayList<Card>();
	private Player pileOwner = null;
	int x;
	int i2;

	/**
	 * create an empty pile with no owner
	 */
	public Pile() {
	}

	/**
	 * Create an un-owned pile with no owner
	 * 
	 * @param cards
	 */
	public Pile(final Card[] cards) {
		this.cards.addAll(Arrays.asList(cards));
	}

	public Pile(final ArrayList<Card> cards) {
		this.cards.addAll(cards);
	}

	/**
	 * Create a pile with a specific owner
	 * 
	 * @param cards
	 * @param owner
	 */
	public Pile(final Card[] cards, final Player owner) {
		this.cards.addAll(Arrays.asList(cards));
		setPileOwner(owner);
	}

	/**
	 * Get a copy of the cards in this pile. Does not empty the pile
	 * 
	 * @return
	 */
	public Card[] getCards() {
		return cards.toArray(new Card[cards.size()]);
	}

	public int size() {
		return cards.size();
	}

	/**
	 * View the card on the bottom of the pile without removing it.
	 * 
	 * @return
	 */
	public Card viewBottomCard() {
		if (cards.isEmpty()) {
			return null;
		}
		return cards.get(0);
	}

	/**
	 * View the card on the top of the pile without removing it.
	 * 
	 * @return
	 */
	public Card viewTopCard() {
		if (cards.isEmpty()) {
			return null;
		}
		return cards.get(cards.size() - 1);
	}

	/**
	 * View the card n times before the top card in the pile without removing it.
	 * 
	 * @return
	 */
	public Card viewCardAtNFromTop(final int n) {
		if (cards.isEmpty()) {
			return null;
		}
		return cards.get(cards.size() - n);
	}

	/**
	 * This decrements the pile one card.
	 * 
	 * @return card from the bottom of the pile
	 */
	public Card getBottomCard() {
		if (cards.isEmpty()) {
			return null;
		}
		return cards.remove(0);
	}

	/**
	 * This decrements the pile one card.
	 * 
	 * @return card from the top of the pile
	 */
	public Card getTopCard() {
		if (cards.isEmpty()) {
			return null;
		}
		return cards.remove(cards.size() - 1);
	}

	/**
	 * 
	 * @return true if there are cards in the pile.
	 */
	public boolean hasCards() {
		return !cards.isEmpty();
	}

	/**
	 * 
	 * @return true if there are no cards in the pile.
	 */
	public boolean isEmpty() {
		return cards.isEmpty();
	}

	/**
	 * Add a card to the top of the pile
	 * 
	 * @param card
	 */
	public void addCardToPile(final Card card) {
		cards.add(card);
		card.setOwner(getPileOwner());
	}

	/**
	 * Give all the cards in this pile to a player
	 * 
	 * @param player
	 */
	public void givePileToPlayer(final Player player) {
		player.giveCardPile(this);
	}

	/**
	 * Deal the cards from this pile to players.
	 * 
	 * @param players
	 */
	public void dealPileToPlayers(final Player... players) {
		shuffleCards();
		while (hasCards()) {
			for (final Player p : players) {
				if (hasCards()) {
					p.giveCard(getTopCard());
				}
			}
		}

	}

	/**
	 * Shuffle the cards in this pile.
	 */
	public void shuffleCards() {
		Collections.shuffle(cards);
	}

	/**
	 * @return the pileOwner
	 */
	public Player getPileOwner() {
		return pileOwner;
	}

	/**
	 * @param pileOwner
	 *            the pileOwner to set
	 */
	public void setPileOwner(final Player pileOwner) {
		this.pileOwner = pileOwner;
	}

	/**
	 * Check the double rule. Top 2 cards are the same number.
	 * 
	 * @return
	 */
	public boolean doble() {
		if (cards.size() < 2) {
			return false;
		}
		if (viewTopCard().getValue() == viewCardAtNFromTop(2).getValue()) {
			return true;
		}
		return false;
	}

	/**
	 * Check the sandwich rule. Top and bottom cards of the top 3 cards must be the same number.
	 * 
	 * @return
	 */
	public boolean sandwich() {
		if (cards.size() < 3) {
			return false;
		}
		if (viewTopCard().getValue() == viewCardAtNFromTop(3).getValue()) {
			return true;
		}
		return false;
	}

	/**
	 * Check four rule. Top four cards must be in consecutive ascending order.
	 * 
	 * @return
	 */
	public boolean four() {
		if (cards.size() < 4) {
			return false;
		}
		for (int i = 4; i > 0; i--) {
			for (int j = 3; j > 0; j--) {
				if (viewCardAtNFromTop(i).getValue() - 1 == viewCardAtNFromTop(
						j).getValue()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check Marriage rule. Top card must be King
	 * 
	 * @return
	 */
	public boolean marriage() {
		if (cards.size() < 2) {
			return false;
		}
		if (viewTopCard().getValue() == Card.VALUE_KING
				&& viewCardAtNFromTop(2).getValue() == Card.VALUE_QUEEN) {
			return true;
		} else if (viewTopCard().getValue() == Card.VALUE_QUEEN
				&& viewCardAtNFromTop(2).getValue() == Card.VALUE_KING) {
			return true;
		}
		return false;
	}

}
