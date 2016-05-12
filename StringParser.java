public class StringParser {

	private static final String SPLIT_REGEX = ":::";
	private static final String CONCAT_STRING = "::";

	public static String encode(final String e) {
		if (e == null) {
			return "null";
		}
		String c = e;
		c = c.replaceAll(":", "\\\\:\\\\");
		return c;
	}

	public static String decode(final String d) {
		if (d == null) {
			return "null";
		}
		String r = d;
		r = r.replaceAll("\\\\:\\\\", ":");
		return r;
	}

	public static String[] splitPlayers(final String i) {
		return i.split(SPLIT_REGEX);
	}

	public static int splitAndGetID(final String s) {
		return Integer.parseInt(s.split(CONCAT_STRING)[0]);
	}

	public static String splitAndDecodeName(final String s) {
		return decode(s.split(CONCAT_STRING)[1]);
	}

	public static String concat(final String id, final String name) {
		return id + CONCAT_STRING + name;
	}

	public static String concat(final int id, final String name) {
		return concat(id + "", name);
	}

	public static String concat(final int id, final int name) {
		return concat(id + "", name + "");
	}

	public static String stringPairsTogether(final String pair1,
			final String pair2) {
		return pair1 + SPLIT_REGEX + pair2;
	}
}
