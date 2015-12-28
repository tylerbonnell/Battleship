/**
 * This is basically just an abstraction of a 2d array, but
 * it has to keep track of a bunch of different stuff. It maintains
 * statistics for the game, as well as all the ships, guesses, and hits
 * that are made by the other player.
 */

import java.util.*;

public class BattleshipBoard {

	// The board is represented by:
	//   0 = empty
	//   1 = ship
	//   2 = destroyed
	//   3 = missed shot
	private int[][] board = new int[10][10];
	private Ship[][] shipLocations = new Ship[10][10];
	private int[] shipAmount = {1, 2, 2, 1, 1};
	private int[] ships;
	private static final String LETTERS = "ABCDEFGHIJ";
	public int shipSquaresLeft;
	private String[] icons = {"   ", "< >", " X ", " * "};
	private Point lastHitLocation;

	// stats
	public int totalGuesses;
	public int totalHits;

	// Constructs a new board. shipIcon is what is used to represent the ship in the display.
	public BattleshipBoard(String shipIcon) {
		icons[1] = shipIcon;
		ships = shipAmount.clone();
	}

	// basically, returns whether or not the play with this board has lost yet
	public boolean hasShipsAlive() {
		return shipSquaresLeft > 0;
	}

	// returns the grid location of the last guess
	public String lastGuess() {
		return lastHitLocation.toString();
	}

	// returns 0 if there is an error parsing the guess, 1 for miss, 2 for hit, 3 for sink
	public int guessLocation(String guess) {
		guess = guess.toUpperCase();
		if (!guess.matches("[A-J][1-9]") && !guess.matches("[A-J]10"))
			return 0;
		Point p = new Point(guess);
		int pos = board[p.row][p.col];
		lastHitLocation = p;
		totalGuesses++;
		if (pos == 1) { // they hit the ship!
			board[p.row][p.col] = 2;
			shipLocations[p.row][p.col].damage();
			shipSquaresLeft--;
			totalHits++;
			if (!shipLocations[p.row][p.col].isAlive()) {
				ships[shipLocations[p.row][p.col].size - 1]--;
				return 3;
			}
			return 2;
		} else if (pos == 2) {
			return 1;
		} else { // they missed!
			board[p.row][p.col] = 3;
			return 1;
		}
	}

	// Returns 0 if there is an error, 1 if a ship of that size can't be placed,
	// 2 if it's successful
	public int addShip(String squares) {
		if (squares.length() == 0 || squares.length() > 7)
			return 0;
		squares = squares.toUpperCase();
		String[] ends = squares.split("-");
		int validity = validRow(ends);
        // validity = the length of the ship in squares
 		if (validity == -1)
			return 0;
		if (validity > ships.length || ships[validity - 1] <= 0)
			return 1;

		Point[] points = null;
		if (validity == 1)
			points = new Point[]{new Point(ends[0])};
		else
			points = buildRows(ends, validity);
		if (!addShip(points))
			return 1;
		placeShipOnMap(points);
		ships[validity - 1]--;
		shipSquaresLeft += validity;
		return 2;
	}

	// Adds ship squares to the grid
	public void placeShipOnMap(Point[] points) {
		Ship s = new Ship(points.length);
		for (Point p : points)
			shipLocations[p.row][p.col] = s;
	}

	// Used for manual placing of ships
	public boolean allShipsPlaced() {
		for (int i : ships)
			if (i != 0) return false;
		return true;
	}

	// Sets the amount of ships (used in the display) to the default amount
	public void resetShipAmount() {
		ships = shipAmount.clone();
	}

	// Adds an array of points to the grid
	private boolean addShip(Point[] squares) {
		for (Point p : squares)
			if (p.row > 10 || p.col > 10 || board[p.row][p.col] == 1)
				return false;
		for (Point p : squares)
			board[p.row][p.col] = 1;
		return true;
	}

	// Generates a row of Points from a string like "A3-A6"
	private Point[] buildRows(String[] ends, int length) {
		Point[] points = new Point[length];
		points[0] = new Point(ends[0]);
		points[length - 1] = new Point(ends[1]);
		if (points[0].row > points[length - 1].row || points[0].col > points[length - 1].col) {
			Point p = points[0];
			points[0] = points[length - 1];
			points[length - 1] = p;
		}
		if (points[0].row == points[length - 1].row) {
			for (int i = 1; i < length; i++) {
				points[i] = new Point(points[0].row, points[0].col + i);
			}
		} else if (points[0].col == points[length - 1].col) {
			for (int i = 1; i < length; i++) {
				points[i] = new Point(points[0].row + i, points[0].col);
			}
		}
		return points;
	}

	// Returns -1 if it's a malformed string/row, otherwise returns the length of the row
	// A correct row is in the format C10 or B3-B6 (inclusive on both ends)
	private int validRow(String[] ends) {
		for (String s : ends)
			if (!s.matches("[A-J][1-9]") && !s.matches("[A-J]10"))
				return -1;
		if (ends.length == 1) return 1;
		int row1 = (int)ends[0].charAt(0);
		int row2 = (int)ends[1].charAt(0);
		int col1 = Integer.parseInt(ends[0].substring(1));
		int col2 = Integer.parseInt(ends[1].substring(1));
		if (row1 == row2 || col1 == col2) {
			return Math.abs(row1 - row2) + Math.abs(col1 - col2) + 1;
		} else {
			return -1;
		}
	}

	// Generates/returns an arrray of strings that are used to show the grid on the display
	public String[] getBoardStrings() {
		String[] s = new String[board.length * 2 + 2];
		s[0] = "    1   2   3   4   5   6   7   8   9   10 ";
		for (int i = 1; i < s.length; i += 2)
			s[i] = "  +---+---+---+---+---+---+---+---+---+---+";
		for (int i = 0; i < board.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(LETTERS.charAt(i));
			sb.append(' ');
			for (int j = 0; j < board[i].length; j++) {
				sb.append('|');
				sb.append(icons[board[i][j]]);
			}
			sb.append('|');
			s[i*2 + 2] = sb.toString();
		}
		return s;
	}

	// Returns an array of strings that are used to display the # of ships left in the display
	public String[] getShipNumStrings() {
		String[] s = new String[ships.length + 3];
		String[] names = {"Aircraft Carrier", "Battleship      ", "Cruiser         ", "Destroyer       ", "Submarine       "};
		s[0] = "";
		s[1] = "          FLEET";
		s[2] = "#   Ship              Size";
		for (int i = 0; i < s.length - 3; i++) {
			s[i + 3] = "" + ships[4-i] + "x  " + names[i] + "  " + (5-i);
		}
		return s;
	}

	// Returns the value of each type of ship (sizes 1-5)
	public int[] shipAmounts() {
		return ships.clone();
	}

	// Ship data structure used to represent multi-square ships
	private class Ship {
		private int partsLeft;
		public int size;

		public Ship(int parts) {
			partsLeft = parts;
			size = parts;
		}

		public void damage() {
			partsLeft--;
		}

		public boolean isAlive() {
			return partsLeft > 0;
		}
	}
}
