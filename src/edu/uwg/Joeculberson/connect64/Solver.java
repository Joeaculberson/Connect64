package edu.uwg.Joeculberson.connect64;

import java.util.HashMap;

import android.graphics.Point;
import android.widget.TextView;

public class Solver {

	private static final int TOTAL_CELLS = 63;

	private TextView[][] gameBoard;

	private final static int MAX_X = 7, MAX_Y = 7;

	/**
	 * Constructor for the Solver class.
	 * 
	 * Precondition: gameBoard != null
	 * 
	 * Postcondition: gameBoard == gameBoard
	 * 
	 * @param gameBoard
	 */
	public Solver(TextView[][] gameBoard) {
		this.gameBoard = gameBoard;

	}

	/**
	 * Returns true if the submitted solution is correct.
	 * <p>
	 * Preconditions: none
	 * 
	 * @return true if the submitted solution is correct, false otherwise.
	 */
	public boolean solveGame() {
		TextView currentCell = null;
		boolean oneInserted = false;
		int positionX = 0;
		int positionY = 0;
		
		for (int i = 0; i < Connect64Activity.BOARD_HEIGHT; i++) {
			for (int j = 0; j < Connect64Activity.BOARD_WIDTH; j++) {
				if (this.gameBoard[i][j].getText().toString().trim()
						.equals("1")) {
					currentCell = this.gameBoard[i][j];
					positionX = i;
					positionY = j;
					oneInserted = true;
				}
			}
		}
		
		if (oneInserted == false) {
			return false;
		}

		for(int i = 0; i < TOTAL_CELLS; i++) {
			HashMap<Point, String> neighbors = this.getNeighbors(positionX,positionY, MAX_X, MAX_Y);
			if (neighbors.containsValue("")) {
				return false;
			}
			
			int nextVal = Integer.parseInt(currentCell.getText().toString().trim()) + 1;
			if (!neighbors.containsValue(nextVal + "")) {
				return false;
			} else {
				for (Point currPoint : neighbors.keySet()) {
					if (Integer.parseInt(neighbors.get(currPoint).trim()) == nextVal){
						currentCell = this.gameBoard[currPoint.x][currPoint.y];
						positionX = currPoint.x;
						positionY = currPoint.y;
					}
				}
			}
		}
		
		return true;
	}
	
	private HashMap<Point, String> getNeighbors(int x, int y, int maxX, int maxY) {
		HashMap<Point, String> neighbors = new HashMap<Point, String>();

		if (x > 0) {
			neighbors.put(new Point(x - 1, y), this.gameBoard[x - 1][y]
					.getText().toString().trim());
		}
		if (y > 0) {
			neighbors.put(new Point(x, y - 1), this.gameBoard[x][y - 1]
					.getText().toString().trim());
		}
		if (x < maxX) {
			neighbors.put(new Point(x + 1, y), this.gameBoard[x + 1][y]
					.getText().toString().trim());
		}
		if (y < maxY) {
			neighbors.put(new Point(x, y + 1), this.gameBoard[x][y + 1]
					.getText().toString().trim());
		}
		return neighbors;
	}

}
