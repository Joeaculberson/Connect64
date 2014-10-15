package edu.uwg.Joeculberson.connect64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class Connect64Activity extends Activity {

	private static final int HIGHEST_PICKER_VAL = 64;
	private static final int LOWEST_PICKER_VAL = 1;
	private static final int BOARD_WIDTH = 8;
	private static final int BOARD_HEIGHT = 8;
	private static final int TWO_CHARACTERS_LONG = 2;
	private static final int DEFAULT_SELECTED_LEVEL = 1;

	private TextView[][] gameBoard;

	private NumberPicker numberPicker;

	private Button deleteButton;
	private Button restartButton;
	private Button submitButton;

	HashMap<Integer, HashMap<Point, Integer>> levels;
	private int selectedLevel;
	private TextView selectedCell;
	private TextView selectedLevelText;
	private CheckBox selectionOnlyModeCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect64);

		BoardListener boardListener = new BoardListener();
		this.gameBoard = new TextView[BOARD_WIDTH][BOARD_HEIGHT];

		GridLayout gridView = (GridLayout) this
				.findViewById(R.id.connect64Container);
		this.setTextViewListeners(boardListener, gridView);
		this.setFont();

		this.selectedLevel = DEFAULT_SELECTED_LEVEL;
		this.selectedLevelText = (TextView) findViewById(R.id.levelText);
		this.selectedLevelText.append(this.selectedLevel + "");

		this.readInLevels();
		this.setUpNumberPicker();
		this.determineAndSetStartingNumber();
		this.setUpButtons();

	}

	private void readInLevels() {
		FileIO fileIO = new FileIO();
		try {
			this.levels = fileIO.getAllLevels(getAssets().open(
					String.format("levels.txt")));
			this.setBoard(this.selectedLevel);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setUpNumberPicker() {
		this.numberPicker = (NumberPicker) findViewById(R.id.numberPicker1);
		this.numberPicker.setMaxValue(HIGHEST_PICKER_VAL);
		this.numberPicker.setMinValue(LOWEST_PICKER_VAL);
		this.numberPicker
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
	}

	private void setTextViewListeners(BoardListener boardListener,
			GridLayout gridView) {
		ArrayList<TextView> textViews = this.getAllChildren(gridView);
		int counter = 0;
		for (int i = 0; i < BOARD_WIDTH; i++) {
			for (int j = 0; j < BOARD_HEIGHT; j++) {
				textViews.get(counter).setOnClickListener(boardListener);
				this.gameBoard[i][j] = textViews.get(counter);
				counter++;
			}
		}
	}

	private void setUpButtons() {
		this.deleteButton = (Button) findViewById(R.id.deleteButton);
		this.restartButton = (Button) findViewById(R.id.restartButton);
		this.selectionOnlyModeCheckBox = (CheckBox) findViewById(R.id.selectionOnlyModeCheckBox);
		this.submitButton = (Button) findViewById(R.id.submitButton);
		
		ButtonListener buttonListener = new ButtonListener();
		this.deleteButton.setOnClickListener(buttonListener);
		this.restartButton.setOnClickListener(buttonListener);
		this.selectionOnlyModeCheckBox.setOnClickListener(buttonListener);
		this.submitButton.setOnClickListener(buttonListener);
	}

	private ArrayList<TextView> getAllChildren(GridLayout layout) {
		ArrayList<TextView> textViews = new ArrayList<TextView>();
		for (int i = 0; i < layout.getChildCount(); i++) {
			TextView v = (TextView) layout.getChildAt(i);
			textViews.add(v);
		}
		return textViews;
	}

	private void determineAndSetStartingNumber() {
		int selectedNumber = 1;
		if (isGivenNumber(selectedNumber)) {
			while (isGivenNumber(selectedNumber)) {
				selectedNumber = selectedNumber + 1;
			}
		}

		this.numberPicker.setValue(selectedNumber);
	}

	private void setFont() {
		for (int i = 0; i < BOARD_WIDTH; i++) {
			for (int j = 0; j < BOARD_HEIGHT; j++) {
				this.gameBoard[i][j].setTypeface(Typeface.MONOSPACE);
				this.gameBoard[i][j].setText("  ");
			}
		}
	}

	private void setBoard(Integer level) {
		HashMap<Integer, HashMap<Point, Integer>> levelsTemp;
		levelsTemp = this.levels;

		for (Point coord : levelsTemp.get(level).keySet()) {
			this.gameBoard[coord.x][coord.y].setText(levelsTemp.get(
					selectedLevel).get(coord)
					+ "");
			if ((levelsTemp.get(selectedLevel).get(coord) + "").length() < TWO_CHARACTERS_LONG) {
				this.gameBoard[coord.x][coord.y].append(" ");
			}
			this.gameBoard[coord.x][coord.y].setBackgroundColor(Color.LTGRAY);
			this.gameBoard[coord.x][coord.y].setTextColor(Color.BLUE);
		}
	}

	private boolean isGivenNumber(int selectedNumber) {
		HashMap<Point, Integer> board = this.levels
				.get(Connect64Activity.this.selectedLevel);
		return board.values().contains(selectedNumber);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void restartGame() {
		this.clearBoard();
		this.setBoard(this.selectedLevel);
		this.determineAndSetStartingNumber();
		if (this.selectedCell != null) {
			ColorDrawable buttonColor = (ColorDrawable) this.selectedCell.getBackground();
			if (buttonColor.getColor() != Color.LTGRAY) {
				this.selectedCell.setBackgroundColor(Color.WHITE);
				this.selectedCell.setTextColor(Color.BLACK);
			}
			this.selectedCell = null;
		}
	}

	private void clearBoard() {
		for (int i = 0; i < BOARD_WIDTH; i++) {
			for (int j = 0; j < BOARD_HEIGHT; j++) {
				this.gameBoard[i][j].setText("  ");
				this.gameBoard[i][j].setBackgroundColor(Color.WHITE);
				this.gameBoard[i][j].setTextColor(Color.BLACK);
			}
		}
	}

	private class ButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.restartButton) {
				Connect64Activity.this.restartGame();
			} else if (v.getId() == R.id.deleteButton) {
				this.deleteCell();
			} else if (v.getId() == R.id.submitButton) {
				this.ensureSolutionIsCorrect();
			}
		}

		private void ensureSolutionIsCorrect() {
			Solver solver = new Solver(Connect64Activity.this.gameBoard);
			boolean isCorrect = solver.solveGame();
			
			if (isCorrect) {
				NextLevelDialogFragment nlDialog = new NextLevelDialogFragment();
				nlDialog.show(getFragmentManager(), "NextLevel");
			} else {
				Toast.makeText(Connect64Activity.this, "Incorrect solution", Toast.LENGTH_SHORT).show();
			}
		}

		private void deleteCell() {
			if (Connect64Activity.this.selectedCell != null) {
				Connect64Activity.this.selectedCell.setText("  ");
			}
		}

	}

	private class BoardListener implements OnClickListener {

		private static final String SELECTED_CELL_COLOR = "#00BFFF";
		private static final String BLANK_CELL = "  ";

		@Override
		public void onClick(View v) {
			if (Connect64Activity.this.selectionOnlyModeCheckBox.isChecked()) {
				this.setCellColorToNormal();
				Connect64Activity.this.selectedCell = (TextView) v;
				if (isNumeric(Connect64Activity.this.selectedCell.getText()
					.toString().trim())
					&& Connect64Activity.this
							.isGivenNumber(Integer.parseInt(selectedCell
									.getText().toString().trim()))) {
					this.displayErrorAndSetSelectedToNull();
				} else {
					this.setSelectedCellColor();
				}
			} else {
				int selectedNumber = Connect64Activity.this.numberPicker.getValue();
				this.setCellColorToNormal();
				this.setNumberToCell(v, selectedNumber);
			}
		}

		private void setCellColorToNormal() {
			if (Connect64Activity.this.selectedCell != null) {
				Connect64Activity.this.selectedCell.setBackgroundColor(Color.WHITE);
				Connect64Activity.this.selectedCell.setTextColor(Color.BLACK);
			}
		}

		private void setNumberToCell(View v, int selectedNumber) {
			Connect64Activity.this.selectedCell = (TextView) v;
			if (!numberAlreadyUsed(selectedNumber)) {
				this.processSelectedNumber(selectedNumber);
			} else {
				this.displayErrorMessage(selectedNumber);
			}
		}

		private void processSelectedNumber(int selectedNumber) {
			if (isNumeric(Connect64Activity.this.selectedCell.getText().toString().trim()) && Connect64Activity.this.isGivenNumber(Integer.parseInt(selectedCell.getText().toString().trim()))) {
				this.displayErrorAndSetSelectedToNull();
			} else {
				this.setCellNumberAndIncrementSelector(selectedNumber);
			}
		}

		private void displayErrorMessage(int selectedNumber) {
			if (isNumeric(Connect64Activity.this.selectedCell.getText()
					.toString().trim())
					&& Connect64Activity.this
							.isGivenNumber(Integer.parseInt(selectedCell
									.getText().toString().trim()))) {
				this.displayErrorAndSetSelectedToNull();
			} else {
				this.displayAlreadyUsedMessage(selectedNumber);
			}
		}

		private void displayAlreadyUsedMessage(int selectedNumber) {
			Toast.makeText(Connect64Activity.this,
					selectedNumber + " has already been used.",
					Toast.LENGTH_SHORT).show();
		}

		private void displayErrorAndSetSelectedToNull() {
			this.displayCannotAlterGivenNumbersMessage();
			Connect64Activity.this.selectedCell = null;
		}

		private void displayCannotAlterGivenNumbersMessage() {
			Toast.makeText(Connect64Activity.this,
					R.string.cannot_alter_given_numbers, Toast.LENGTH_SHORT).show();
		}

		private void setCellNumberAndIncrementSelector(int selectedNumber) {
			this.setSelectedCellColor();
			if (!isNumeric(Connect64Activity.this.selectedCell.getText()
					.toString())) {
				this.insertNumberInCellAndIncrementCounter(selectedNumber);
			}
		}

		private void setSelectedCellColor() {
			Connect64Activity.this.selectedCell.setBackgroundColor(Color
					.parseColor(SELECTED_CELL_COLOR));
			Connect64Activity.this.selectedCell.setTextColor(Color.BLUE);
		}

		private void insertNumberInCellAndIncrementCounter(int selectedNumber) {
			Connect64Activity.this.selectedCell.setText(selectedNumber + "");
			if ((selectedNumber + "").length() < 2) {
				Connect64Activity.this.selectedCell.append(" ");
			}

			if ((selectedNumber + 1) != 65) {
				this.incrementCounter(selectedNumber);
			}
		}

		private void incrementCounter(int selectedNumber) {
			selectedNumber = selectedNumber + 1;
			HashMap<Point, Integer> board = Connect64Activity.this.levels
					.get(Connect64Activity.this.selectedLevel);

			if (board.values().contains(selectedNumber)) {
				while (board.values().contains(selectedNumber)) {
					selectedNumber = selectedNumber + 1;
				}
			}

			Connect64Activity.this.numberPicker.setValue(selectedNumber);
		}

		private boolean isNumeric(String str) {
			try {
				@SuppressWarnings("unused")
				double d = Double.parseDouble(str);
			} catch (NumberFormatException nfe) {
				return false;
			}
			return true;
		}

		private boolean numberAlreadyUsed(int selectedNumber) {
			for (int i = 0; i < Connect64Activity.BOARD_HEIGHT; i++) {
				for (int j = 0; j < Connect64Activity.BOARD_WIDTH; j++) {
					if (!Connect64Activity.this.gameBoard[i][j].getText().toString().equals(BLANK_CELL)) {
						if (Integer.parseInt(Connect64Activity.this.gameBoard[i][j].getText().toString().trim()) == selectedNumber) {
							return true;
						}
					}
				}
			}
			return false;
		}

	}
	
	private class NextLevelDialogFragment extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage(R.string.congratulations_message)
	               .setPositiveButton(R.string.next_level, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       Connect64Activity.this.selectedLevel += 1;
	                       Connect64Activity.this.restartGame();
	                   }
	               })
	               .setNegativeButton(R.string.replay, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       Connect64Activity.this.restartGame();
	                   }
	               });
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
	}
	
}
