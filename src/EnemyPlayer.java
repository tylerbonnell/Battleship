/**
 * This is a fairly basic AI player for the game. It could probably
 * be improved, but it performs about on par with me (so maybe I
 * just suck). It will generate random guesses until it hits a ship.
 * Once it hits something, it will begin to guess around that area,
 * and figure out if the ship is vertical or horizontal. It will
 * then destroy the ship and return to randomly guessing locations
 * that it hasn't guessed prior.
 */

import java.util.*;

public class EnemyPlayer {

	private BattleshipBoard playerBoard;
	private Stack<Guess> knownShips;
	private Set<Point> allGuessedPoints;

	public EnemyPlayer(BattleshipBoard playerBoard) {
		knownShips = new Stack<Guess>();
		allGuessedPoints = new HashSet<Point>();
		this.playerBoard = playerBoard;
	}

	// returns 0 if it misses, 1 if it hits a ship, 2 if it sinks one
	public int makeGuess() {
		// if he can, the enemy should guess based on previous hits
		while (!knownShips.isEmpty()) {
			Guess g = knownShips.peek();
			Point p = g.nextGuess();
			if (p == null) {
				knownShips.pop();
			} else {
				allGuessedPoints.add(p);
				int result = playerBoard.guessLocation(p.toString());
				if (result == 3)
					knownShips.pop();
				else g.hit(result == 2);
				return result - 1;
			}
		} // otherwise, choose randomly
		Point p = new Point((int)(Math.random() * 10), (int)(Math.random() * 10));
		while (allGuessedPoints.contains(p))
			p = new Point((int)(Math.random() * 10), (int)(Math.random() * 10));
		allGuessedPoints.add(p);
		int result = playerBoard.guessLocation(p.toString()) - 1;
		if (result == 1) {
			knownShips.push(new Guess(p));
		}
		return result;
	}


	// used when the enemy hits a ship. It will guess, figure out if
	// the ship is vertical or horizontal, and destroy it
	private class Guess {

		private Point origin;
		private Queue<Point> nextGuessUp;
		private Queue<Point> nextGuessDown;
		private Queue<Point> nextGuessLeft;
		private Queue<Point> nextGuessRight;
		private int lastDirection;
		private Queue<Point>[] directions;

		public Guess(Point p) {
			origin = p;
			// will guess at most 4 in each direction because the largest ship is 5 long
			addPointsInDirection(nextGuessUp = new LinkedList<Point>(), -1, 0, 4);
			addPointsInDirection(nextGuessDown = new LinkedList<Point>(), 1, 0, 4);
			addPointsInDirection(nextGuessLeft = new LinkedList<Point>(), 0, -1, 4);
			addPointsInDirection(nextGuessRight = new LinkedList<Point>(), 0, 1, 4);
			directions = new Queue[]{nextGuessUp, nextGuessDown, nextGuessLeft, nextGuessRight};
		}

		// if it returns null, there are no points to guess. eliminate this Guess object.
		public Point nextGuess() {
			// guess up, then down, then left, then right
			for (int i = 0; i < directions.length; i++) {
				lastDirection = i;
				if (!directions[i].isEmpty()) {
					Point p = directions[i].remove();
					if (p != null)
						return p;
				}
			}
			return null;
		}

		// If we didn't hit, we stop searching that direction by getting rid of
		// all locations that we planned to search in that direction
		public void hit(boolean hitShip) {
			if (!hitShip) {
				directions[lastDirection].clear();
			}
		}

		// Fills a queue with points in a certain direction
		private void addPointsInDirection(Queue<Point> queue, int rowDir, int colDir, int amount) {
			for (int i = 1; i <= amount; i++) {
				Point p = getPointInDirection(origin, rowDir * i, colDir * i);
				if (p != null)
					queue.add(p);
			}
		}

		// eg getPointInDirection(p, 1, 0) gets the point directly above p.
		// Returns null if outside the box or already guessed.
		private Point getPointInDirection(Point from, int rowChange, int colChange) {
			int newRow = from.row + rowChange;
			int newCol = from.col + colChange;
			Point p = new Point(newRow, newCol);
			if (newRow < 0 || newRow > 9 || newCol < 0 || newCol > 9 || allGuessedPoints.contains(p))
				return null;
			return p;
		}
	}
}
