import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JLabel;

/**
 * This class will maintain a Player and his Cards.
 * 
 * @author Bryce J. Fisher
 * 
 */
public class Player extends JLabel {
	private static final long serialVersionUID = 1L;
	ArrayList<Card> cards = new ArrayList<Card>();
	private String name;
	private int money;
	private int points;
	private final boolean isComputer;
	private final int ID;

	public boolean isComputer() {
		return isComputer;
	}

	/**
	 * Create a new computer player
	 * 
	 * @param name
	 */
	public Player(final String name, final int id) {
		super();
		ID = id;
		this.name = name;
		isComputer = true;
		updateText();
	}

	/**
	 * Create a new player
	 * 
	 * @param name
	 */
	public Player(final String name, final int id, final boolean isComputer) {
		super();
		ID = id;
		this.name = name;
		this.isComputer = isComputer;
		updateText();
	}

	public void setName(final String n) {
		name = n;
	}

	/**
	 * Get's player's name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public int getID() {
		return ID;
	}

	/**
	 * Get String about Player
	 */
	public String toString() {
		return getName() + ":($" + getMoney() + ", " + getPoints() + " points)";
	}

	/**
	 * Give a card to this player
	 * 
	 * @param card
	 */
	public void giveCard(final Card card) {
		cards.add(0, card);
		card.setOwner(this);
		updateText();
	}

	/**
	 * Give this player a pile of cards
	 * 
	 * @param pile
	 */
	public void giveCardPile(final Pile pile) {
		while (pile.hasCards()) {
			giveCard(pile.getBottomCard());
		}
		updateText();
	}

	/**
	 * Gets this players cards without removing any
	 * 
	 * @return
	 */
	public Card[] viewCards() {
		return cards.toArray(new Card[cards.size()]);
	}

	/**
	 * View the card on the bottom of this player's cards without removing it.
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
	 * View the card on the top of this player's cards without removing it.
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
	 * This decrements this player's cards one card.
	 * 
	 * @return card from the bottom of this player's cards
	 */
	public Card getBottomCard() {
		if (cards.isEmpty()) {
			return null;
		}
		updateText(cards.size() - 1);
		return cards.remove(0);
	}

	/**
	 * This decrements this player's cards one card.
	 * 
	 * @return card from the top of this player's cards
	 */
	public Card getTopCard() {
		if (cards.isEmpty()) {
			return null;
		}
		updateText(cards.size() - 1);
		return cards.remove(cards.size() - 1);
	}

	/**
	 * 
	 * @return true if player has cards
	 */
	public boolean hasCards() {
		return !cards.isEmpty();
	}

	/**
	 * 
	 * @return number of cards
	 */
	public int getCardCount() {
		return cards.size();
	}

	/**
	 * Get's user's money
	 * 
	 * @return
	 */
	public int getMoney() {
		return money;
	}

	/**
	 * Add money to user's money
	 * 
	 * @param addition
	 */
	public void addMoney(final int addition) {
		money += addition;
	}

	/**
	 * Subtract money, returns a negative nonzero number if the user is in debt.
	 * 
	 * @param subtraction
	 * @return
	 */
	public int lostMoney(final int subtraction) {
		money -= subtraction;
		if (money < 0) {
			return money;
		}
		return 0;
	}

	/**
	 * Get users points
	 * 
	 * @return
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * Set points indiscriminately
	 * 
	 * @param points
	 */
	public void setPoints(final int points) {
		this.points = points;
	}

	/**
	 * Add points to player's points
	 * 
	 * @param points
	 */
	public void addPoints(final int pointsToAdd) {
		points += pointsToAdd;
	}

	/**
	 * Shuffles this player's cards
	 */
	public void shuffleCards() {
		Collections.shuffle(cards);
	}

	/**
	 * Update this Player's JLabel text
	 */
	public void updateText() {
		updateText(cards.size());
	}

	/**
	 * Update this Player's JLabel text with the specified number of cards
	 * 
	 * @param size
	 */
	public void updateText(final int size) {
		setText(size + " Cards");
	}
}
