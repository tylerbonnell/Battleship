/**
 * Pretty basic Point class, nothing fancy. Used to represent grid locations
 * on a BattleshipBoard object. eg "F8" represents the F row, 8 column.
 */

public class Point {
	public int row;
	public int col;
	private static final int BASE_LETTER = (int) 'A';

	// Creates a new Point object with this row and col. Used when creating
	// Points relative to each other.
	public Point(int row, int col) {
		this.row = row;
		this.col = col;
	}

	// Creates a new Point object based off a string like "E2"
	public Point(String point) {
		row = (int) point.charAt(0) - BASE_LETTER;
		col = Integer.parseInt(point.substring(1)) - 1;
	}

	// Returns the Point in its battleship format (character and number)
	public String toString() {
		return "" + ((char)(row + BASE_LETTER)) + (col + 1);
	}

	// Two points are equal if they have the same row and col values
	public boolean equals(Object o) {
		if (!(o instanceof Point))
			return false;
		Point p = (Point) o;
		return p.row == row && p.col == col;
	}

	// Two points have the same hashCode if they have the same row and col values
	public int hashCode() {
		int hash = 1;
		hash = hash * 17 + row;
		hash = hash * 31 * col;
		return hash;
	}
}