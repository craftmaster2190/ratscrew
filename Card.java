import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Contains a single Card entity
 * 
 * @author Bryce J. Fisher, Carlos Arredondo
 * 
 */
public class Card {

	private static final String PNG = ".png";
	private static final String IMAGE_FOLDER = "image" + File.separator
			+ "card" + File.separator;
	private final Image img;
	private final ImageIcon icon;
	private final int value; // 1-13, 11=Jack, etc
	private final int suit;
	private Player owner = null;
	private int x, y;
	private JLabel jLabel;
	private boolean isFaceUp = true;
	private boolean isVertical = true;
	private boolean isBlueBacked = true;

	private static final int SUIT_MINIMUM_INTEGER = 20;

	/**
	 * Constant Value for Hearts
	 */
	public static final int SUIT_HEARTS = SUIT_MINIMUM_INTEGER + 1;
	/**
	 * Constant Value for Clubs
	 */
	public static final int SUIT_CLUBS = SUIT_MINIMUM_INTEGER + 3;
	/**
	 * Constant Value for Diamonds
	 */
	public static final int SUIT_DIAMONDS = SUIT_MINIMUM_INTEGER + 2;
	/**
	 * Constant Value for Spades
	 */
	public static final int SUIT_SPADES = SUIT_MINIMUM_INTEGER;

	/**
	 * Constant Value for Ace
	 */
	public static final int VALUE_ACE = 1;
	/**
	 * Constant Value for Jack
	 */
	public static final int VALUE_JACK = 11;
	/**
	 * Constant Value for Queen
	 */
	public static final int VALUE_QUEEN = 12;
	/**
	 * Constant Value for King
	 */
	public static final int VALUE_KING = 13;
	private final String fileName;

	/**
	 * Create a new card
	 * 
	 * @param value
	 * @param suit
	 */
	public Card(final int value, final int suit) {
		this.value = value;
		this.suit = suit;
		final int imageValue = value + VALUE_KING
				* (suit - SUIT_MINIMUM_INTEGER);

		fileName = IMAGE_FOLDER + imageValue + PNG;
		icon = new ImageIcon(getFileName());
		img = icon.getImage();

		setjLabel(new JLabel(icon));
	}

	/**
	 * Create a deck of 52 cards
	 * 
	 * @return
	 */
	public static Card[] createDeckOf52Cards() {
		final Card[] array = new Card[52];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 13; j++) {
				array[j + i * 13] = new Card(j + 1, i + SUIT_MINIMUM_INTEGER);
			}
		}
		return array;
	}

	/**
	 * Get card name
	 */
	public String getCardName() {
		String valueString = "";
		switch (value) {
		case VALUE_ACE:
			valueString = "Ace";
			break;
		case VALUE_JACK:
			valueString = "Jack";
			break;
		case VALUE_QUEEN:
			valueString = "Queen";
			break;
		case VALUE_KING:
			valueString = "King";
			break;
		default:
			valueString += value;
		}

		String suitString = "";
		switch (suit) {
		case SUIT_CLUBS:
			suitString = "Clubs";
			break;
		case SUIT_SPADES:
			suitString = "Spades";
			break;
		case SUIT_DIAMONDS:
			suitString = "Diamonds";
			break;
		case SUIT_HEARTS:
			suitString = "Hearts";
			break;
		default:
		}

		return valueString + " of " + suitString;
	}

	/**
	 * Get card's value
	 * 
	 * @return
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Get card's suit
	 * 
	 * @return
	 */
	public int getSuit() {
		return suit;
	}

	/**
	 * Get Card's owner, null if un-owned
	 * 
	 * @return
	 */
	public Player getOwner() {
		return owner;
	}

	/**
	 * Set owner of this card, should be null if no owner.
	 * 
	 * @param player
	 */
	public void setOwner(final Player player) {
		owner = player;
	}

	/**
	 * Get Card's image for GUI
	 * 
	 * @return
	 */
	public Image getImg() {
		if (isFaceUp) {
			return img;
		}
		return getCardBackImage(isVertical(), isBlueBacked());
	}

	/**
	 * Returns getCardName()
	 */
	public String toString() {

		return getCardName();
	}

	public boolean isRoyal() {
		return value == VALUE_JACK || value == VALUE_QUEEN
				|| value == VALUE_KING || value == VALUE_ACE;
	}

	/**
	 * Shuffle a deck or pile of cards.
	 * 
	 * @param cards
	 */
	public static void shuffle(final Card[] cards) {
		Collections.shuffle(Arrays.asList(cards));
	}

	/**
	 * Shuffle a deck or pile of cards.
	 * 
	 * @param cards
	 */
	public static void shuffle(final ArrayList<Card> cards) {
		Collections.shuffle(cards);
	}

	/**
	 * Get the back of the card image.
	 * 
	 * @param verticalIfTrueHorizontalIfFalse
	 * @param blueIfTrueRedIfFalse
	 * @return return the requested image file
	 */
	public static Image getCardBackImage(
			final boolean verticalIfTrueHorizontalIfFalse,
			final boolean blueIfTrueRedIfFalse) {
		if (verticalIfTrueHorizontalIfFalse) {
			if (blueIfTrueRedIfFalse) {
				return new ImageIcon(IMAGE_FOLDER + "b1fv" + PNG).getImage();
			}
			return new ImageIcon(IMAGE_FOLDER + "b2fv" + PNG).getImage();
		}
		if (blueIfTrueRedIfFalse) {
			return new ImageIcon(IMAGE_FOLDER + "b1fh" + PNG).getImage();
		}
		return new ImageIcon(IMAGE_FOLDER + "b2fh" + PNG).getImage();
	}

	public int getY() {
		return y;
	}

	public void setY(final int y) {
		this.y = y;
		setUpperLeftCorner(getX(), getY());
	}

	public int getX() {
		return x;
	}

	public void setX(final int x) {
		this.x = x;
		setUpperLeftCorner(getX(), getY());
	}

	public void setUpperLeftCorner(final int x, final int y) {
		this.x = x;
		this.y = y;
		getjLabel().setBounds(getX(), getY(), getWidth(), getHeight());
	}

	public void setCenter(final int x, final int y) {
		this.x = x - getWidth() / 2;
		this.y = y - getHeight() / 2;
		getjLabel().setBounds(getX(), getY(), getWidth(), getHeight());
	}

	public Point getCenter() {
		final Point point = new Point();
		point.x = x + getWidth() / 2;
		point.y = y + getHeight() / 2;
		return point;
	}

	public JLabel getjLabel() {
		return jLabel;
	}

	public void setjLabel(final JLabel jLabel) {
		this.jLabel = jLabel;
	}

	public ImageIcon getIcon() {
		if (isFaceUp()) {
			return icon;
		}
		return new ImageIcon(getCardBackImage(isVertical(), isBlueBacked()));
	}

	public int getWidth() {
		return getIcon().getIconWidth();
	}

	public int getHeight() {
		return getIcon().getIconHeight();
	}

	public void setCenter(final double x, final double y) {
		setCenter((int) x, (int) y);

	}

	public String getFileName() {
		return fileName;
	}

	public boolean isFaceUp() {
		return isFaceUp;
	}

	public void setFaceUp(final boolean isFaceUp) {
		this.isFaceUp = isFaceUp;
		jLabel.setIcon(getIcon());
	}

	public boolean isVertical() {
		return isVertical;
	}

	public void setVertical(final boolean isVertical) {
		this.isVertical = isVertical;
		jLabel.setIcon(getIcon());
	}

	public boolean isBlueBacked() {
		return isBlueBacked;
	}

	public void setBlueBacked(final boolean isBlueBacked) {
		this.isBlueBacked = isBlueBacked;
		jLabel.setIcon(getIcon());
	}

	public boolean equals(final Card card) {
		// TODO Auto-generated method stub
		return getValue() == card.getValue() && getSuit() == card.getSuit();
	}
}
