/**
 * Made by Tyler Bonnell (github.com/tylerbonnell)
 * This is a battleship game designed to be run on command line.
 * When you run it, optional arguments are 1. a string which will
 * be truncated at 3 chars to use as your ship icon, and 2. the
 * string "-c" which enabled cheaty mode (shows your enemies)
 */

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BattleshipGame {

	private static final String LETTERS = "ABCDEFGHIJ";
	private static final int WIDTH = 122; // output width in terms of chars
	private static boolean fakeWin; // used to bypass the game. enter "hax" at any point
									// and you'll win at the end of the round

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		String customShip = "< >";
		String enemyShip = "   ";
		if (args.length > 0 && !args[0].equals("-c"))
			customShip = parseArgs(args[0]);
		if (args.length > 1 && args[1].equals("-c") || args[0].equals("-c"))
			enemyShip = ">:(";
		BattleshipBoard playerBoard = new BattleshipBoard(customShip);
		Scanner console = new Scanner(System.in);
		intro();
		readLine(console);
		int choice = 0;
		while (choice != 1 && choice != 2) {
			printTitle();
			clearConsole(5);
			choice = getStartChoice(console);
		}
		if (choice == 1)
			spawnShips(playerBoard);
		else
			addPlayerShips(playerBoard, console);
		BattleshipBoard enemyBoard = new BattleshipBoard(enemyShip);
		spawnShips(enemyBoard);
		EnemyPlayer enemy = new EnemyPlayer(playerBoard);
		playGame(playerBoard, enemyBoard, enemy, console);
		clearConsole();
		if (playerBoard.hasShipsAlive() || fakeWin)
			win();
		else
			lose();
		clearConsole(3);
		printStats(startTime, playerBoard, enemyBoard);
	}

	// Once everything has been set up, this runs the actual game.
	public static void playGame(BattleshipBoard playerBoard, BattleshipBoard enemyBoard, EnemyPlayer enemy, Scanner console) {
		playerBoard.resetShipAmount();
		enemyBoard.resetShipAmount();
		String lastGuess = "";
		while (playerBoard.hasShipsAlive() && enemyBoard.hasShipsAlive() && !fakeWin) {
			int guessResult = -1;
			while (guessResult <= 0) {
				clearConsole();
				printGameBoard(playerBoard, enemyBoard);
				System.out.print("\n  " + lastGuess + "Where would you like to fire? ");
				lastGuess = readLine(console);
				guessResult = enemyBoard.guessLocation(lastGuess);
				if (guessResult == 0)
					lastGuess = "\"" + lastGuess + "\" is not valid input. ";
				else lastGuess = "";
			}
			clearConsole();
			printGameBoard(playerBoard, enemyBoard);
			System.out.println();
			if (guessResult == 1) {
				System.out.print("  You missed!");
			} else if (guessResult == 2) {
				System.out.print("  You hit a battleship!");
			} else if (guessResult == 3) {
				System.out.print("  You sank their battleship!");
			}
			if (enemyBoard.hasShipsAlive()) {
				System.out.print(" Press enter for the enemy to make their move. ");
				readLine(console);
				int enemyGuess = enemy.makeGuess();
				if (enemyGuess == 0) {
					lastGuess = "The enemy missed at " + playerBoard.lastGuess() + ". ";
				} else if (enemyGuess == 1) {
					lastGuess = "The enemy hit your battleship at " + playerBoard.lastGuess() + "! ";
				} else if (enemyGuess == 2) {
					lastGuess = "The enemy sank your battleship at " + playerBoard.lastGuess() + "! ";
				}
				if (!playerBoard.hasShipsAlive()) {
					clearConsole();
					printGameBoard(playerBoard, enemyBoard);
					System.out.print("  The enemy sank your battleship at " + playerBoard.lastGuess()
							+ ". Press enter to continue. ");
					readLine(console);
				}
			} else {
				System.out.print(" Press enter to continue. ");
				readLine(console);
			}
		}
	}

	// Prompts the user for input/places ships onto their board
	public static void addPlayerShips(BattleshipBoard board, Scanner console) {
		printBoardPreGame(board);
		System.out.print("\n  ");
		while (!board.allShipsPlaced()) {
			System.out.print("Where would you like to place a ship? ");
			String lastInput = readLine(console);
			int placed = board.addShip(lastInput);
			clearConsole();
			printBoardPreGame(board);
			System.out.println();
			if (placed == 0)
				System.out.print("  \"" + lastInput + "\" is not valid input. ");
			else if (placed == 1)
				System.out.print("  Ship could not be placed. ");
			else if (!board.allShipsPlaced())
				System.out.print("  Ship placed! ");
		}
		System.out.print("  All ships have been placed. Press enter to begin. ");
		readLine(console);
	}

	// Formats the optional player ship string argument
	public static String parseArgs(String s) {
		if (s.length() == 1) {
			return " " + s + " ";
		} else {
			return (s + "      ").substring(0, 3);
		}
	}

	// There is no way to clear the console cross-platform that isn't
	// super hacky and way too time intensive, so we settle for spamming \n
	public static void clearConsole() {
		clearConsole(100);
	}
	public static void clearConsole(int n) {
		for (int i = 0; i < n; i++)
			System.out.println();
	}

	// Prints the player board and the amount of ships they have left to place.
	// Used if the player opts for manual ship placement instead of random.
	public static void printBoardPreGame(BattleshipBoard board) {
		System.out.println("                  YOUR BOARD\n");
		String spaces = "        ";
		String[] boardStrings = board.getBoardStrings();
		String[] middle = board.getShipNumStrings();
		for (int i = 0; i < boardStrings.length; i++) {
			System.out.print(boardStrings[i]);
			System.out.print(i >= middle.length ? "" : spaces + middle[i]);
			System.out.println();
		}
	}

	// Used for the general display during the game. Prints both boards, as well as how many
	// ships there are still remaining for each player.
	public static void printGameBoard(BattleshipBoard playerBoard, BattleshipBoard enemyBoard) {
		System.out.print("                  YOUR BOARD                            ");
		System.out.println("                                        ENEMY BOARD\n");
		String[] pbDisplay = playerBoard.getBoardStrings();
		String[] ebDisplay = enemyBoard.getBoardStrings();
		String[] middleTop = playerBoard.getShipNumStrings();
		String[] middleBottom = enemyBoard.getShipNumStrings();
		middleBottom[1] = "       ENEMY FLEET";
		ArrayList<String> mid = new ArrayList(Arrays.asList(middleTop));
		mid.add(0, "                                ");
		mid.add("                                ");
		mid.add("                                ");
		mid.addAll(Arrays.asList(middleBottom));
		for (int i = 0; i < ebDisplay.length; i++) {
			String s = pbDisplay[i] + "     " + (i >= mid.size() ? "" : mid.get(i));
			s += "                                                                                         ";
			System.out.println(s.substring(0, 79) + ebDisplay[i]);
		}
	}

	// Generates ships randomly around the board based on the ship amounts set in the board class
	private static void spawnShips(BattleshipBoard board) {
		int[] shipAmounts = board.shipAmounts();
		for (int i = 0; i < shipAmounts.length; i++) {
			while (shipAmounts[i] > 0) {
				int validity = 0;
				while (validity != 2) {
					boolean vertical = Math.random() >= .5;
					String s = "";
					if (vertical) {
						char c = LETTERS.charAt((int) (Math.random() * LETTERS.length()));
						int row = (int) (Math.random() * (10 - i)) + 1;
						s = "" + c + row + (i > 0 ? "-" + c + (row + i) : "");
					} else {
						int row = (int) (Math.random() * 10) + 1;
						String shortLetters = LETTERS.substring(0, LETTERS.length() - i);
						char c = shortLetters.charAt((int) (Math.random() * shortLetters.length()));
						char endC = LETTERS.charAt(LETTERS.indexOf(c) + i);
						s = "" + c + row + (i > 0 ? "-" + endC + row : "");
					}
					//System.out.println(s);
					validity = board.addShip(s);
				}
				shipAmounts[i]--;
			}
		}
	}

	// Determines whether the player will manually place their ships or have it done for them
	public static int getStartChoice(Scanner console) {
		int choice = 0;
		System.out.println("1. Randomly generate the positions of your battleships");
		System.out.println("2. Manually enter the positions of your battleships");
		System.out.print("\nEnter either \"1\" or \"2\": ");
		try {
			choice = Integer.parseInt(readLine(console));
		} catch (NumberFormatException e) {}
		return choice;
	}

	// Reads in the next line of input and returns it, if the player hasn't entered any special commands
	// These commands are either "q" to quit, or "hax" to bypass the game to the win screen
	public static String readLine(Scanner console) {
		String s = console.nextLine();
		if (s.equals("q")) {
			System.exit(0);
		} else if (s.equals("hax")) {
			fakeWin = true;
		}
		return s;
	}

	// Tells you that you're a boss
	public static void win() {
		System.out.println(center(" __   __  _______  __   __    _     _  ___   __    _  __ "));
		System.out.println(center("|  | |  ||       ||  | |  |  | | _ | ||   | |  |  | ||  |"));
		System.out.println(center("|  |_|  ||   _   ||  | |  |  | || || ||   | |   |_| ||  |"));
		System.out.println(center("|       ||  | |  ||  |_|  |  |       ||   | |       ||  |"));
		System.out.println(center("|_     _||  |_|  ||       |  |       ||   | |  _    ||__|"));
		System.out.println(center("  |   |  |       ||       |  |   _   ||   | | | |   | __ "));
		System.out.println(center("  |___|  |_______||_______|  |__| |__||___| |_|  |__||__|"));
	}

	// Tells you how you got owned
	public static void lose() {
		System.out.println(center(" __   __  _______  __   __    ___      _______  _______  _______ "));
		System.out.println(center("|  | |  ||       ||  | |  |  |   |    |       ||       ||       |"));
		System.out.println(center("|  |_|  ||   _   ||  | |  |  |   |    |   _   ||  _____||    ___|"));
		System.out.println(center("|       ||  | |  ||  |_|  |  |   |    |  | |  || |_____ |   |___ "));
		System.out.println(center("|_     _||  |_|  ||       |  |   |___ |  |_|  ||_____  ||    ___|"));
		System.out.println(center("  |   |  |       ||       |  |       ||       | _____| ||   |___ "));
		System.out.println(center("  |___|  |_______||_______|  |_______||_______||_______||_______|"));
	}

	// Centers a string based on the WIDTH constant
	public static String center(String s) {
		int diff = WIDTH - s.length();
		String spaces = "                                                                    ".substring(0, (diff - 1)/2);
		return spaces + s + spaces;
	}

	// Prints out the time played, as well as player and AI accuracy
	public static void printStats(long startTime, BattleshipBoard player, BattleshipBoard enemy) {
		long millis = System.currentTimeMillis() - startTime;
		String s = String.format("%d minutes, %d seconds", TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		System.out.println(center("Game Duration: " + s));
		System.out.println(center(String.format("Player Accuracy: %.1f", enemy.totalHits * 100.0 /enemy.totalGuesses) + "%"));
		System.out.println(center(String.format("Enemy Accuracy: %.1f", player.totalHits * 100.0 /player.totalGuesses) + "%"));
		System.out.println(center("Number of Rounds: " + enemy.totalGuesses));
		clearConsole(6);
	}

	// Prints the intro paragraph
	public static void intro() {
		printTitle();
		System.out.println(center("Welcome to Command-Line Battleship! \n"));
		System.out.println(center("The rules are exactly the same as the classic board game. When prompted, enter a guess"));
		System.out.println(center("in the form \"D4\", where \"D\" is any letter A-J and \"4\" is any number 1-10."));
		System.out.println(center("If at any time you want to quit, simply enter \"q\".\n"));
		System.out.println(center("PRESS ENTER TO BEGIN\n"));
	}

	// Prints the ASCII art title
	public static void printTitle() {
		clearConsole();
		System.out.println("=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
		System.out.println(center(" _______  _______  __   __  __   __  _______  __    _  ______          ___      ___   __    _  _______ "));
		System.out.println(center("|       ||       ||  |_|  ||  |_|  ||       ||  |  | ||      |        |   |    |   | |  |  | ||       |"));
		System.out.println(center("|       ||   _   ||       ||       ||   _   ||   |_| ||  _    | ____  |   |    |   | |   |_| ||    ___|"));
		System.out.println(center("|      _||  | |  ||       ||       ||  |_|  ||       || | |   ||____| |   |    |   | |       ||   |___ "));
		System.out.println(center("|     |  |  |_|  ||       ||       ||       ||  _    || |_|   |       |   |___ |   | |  _    ||    ___|"));
		System.out.println(center("|     |_ |       || ||_|| || ||_|| ||   _   || | |   ||       |       |       ||   | | | |   ||   |___ "));
		System.out.println(center("|_______||_______||_|   |_||_|   |_||__| |__||_|  |__||______|        |_______||___| |_|  |__||_______|"));
		System.out.println(center(" _______  _______  _______  _______  ___      _______  _______  __   __  ___   _______ "));
		System.out.println(center("|  _    ||       ||       ||       ||   |    |       ||       ||  | |  ||   | |       |"));
		System.out.println(center("| |_|   ||   _   ||_     _||_     _||   |    |    ___||  _____||  |_|  ||   | |    _  |"));
		System.out.println(center("|       ||  |_|  |  |   |    |   |  |   |    |   |___ | |_____ |       ||   | |   |_| |"));
		System.out.println(center("|  _   | |       |  |   |    |   |  |   |___ |    ___||_____  ||   _   ||   | |    ___|"));
		System.out.println(center("| |_|   ||   _   |  |   |    |   |  |       ||   |___  _____| ||  | |  ||   | |   |    "));
		System.out.println(center("|_______||__| |__|  |___|    |___|  |_______||_______||_______||__| |__||___| |___|    "));
		System.out.println("\n~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=\n");
	}
}
