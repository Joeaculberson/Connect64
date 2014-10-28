package edu.uwg.Joeculberson.connect64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class Connect64Activity extends Activity {

	public static final int BOARD_WIDTH = 8;
	public static final int BOARD_HEIGHT = 8;

	private static final String LEVELS_TXT_LOCATION = "levels.txt";
	private static final String SELECTED_LEVEL_KEY = "SELECTED_LEVEL";
	private static final int HIGHEST_PICKER_VAL = 64;
	private static final int LOWEST_PICKER_VAL = 1;
	private static final int TWO_CHARACTERS_LONG = 2;
	private static final int DEFAULT_SELECTED_LEVEL = 1;
	private static final String NUMBER_PICKER_KEY = "NUMBER_PICKER_KEY";
	private static final String SELECTED_CELL_KEY = "SELECTED_CELL_KEY";
	private static final String IS_CONGRATS_SHOWING_KEY = "IS_CONGRATS_SHOWING_KEY";
	private static final String SELECTED_CELL_COLOR = "#00BFFF";
	private static final String TIMER_KEY = "TIMER_KEY";

	private TextView[][] gameBoard;

	private NumberPicker numberPicker;

	private Button deleteButton;
	private Button restartButton;
	private Button submitButton;

	HashMap<Integer, HashMap<Point, Integer>> levels;
	private int selectedLevel;
	private TextView selectedCell;
	private CheckBox selectionOnlyModeCheckBox;
	private ListView levelsListView;
	private CustomAdapter listAdapter;
	private SharedPreferences sharedPref;

	private TextView timerTextView;
	private long startTime;
	private long accumulatedMillis;
	private long currentMillis;
	
	private boolean congratsDialogFragmentIsVisible;

	private NextLevelDialogFragment nlDialog;

	private Handler timerHandler;
	private Runnable timerRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_connect64);

		BoardListener boardListener = new BoardListener();
		this.gameBoard = new TextView[BOARD_WIDTH][BOARD_HEIGHT];

		GridLayout gridView = (GridLayout) this
				.findViewById(R.id.connect64Container);
		this.setTextViewListenersAndSetFont(boardListener, gridView);

		this.selectedLevel = DEFAULT_SELECTED_LEVEL;

		this.readInLevels();
		this.setUpNumberPicker();
		
		this.timerTextView = (TextView) findViewById(R.id.timerTextView);
		this.startTime = SystemClock.uptimeMillis();

		this.sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		if (sharedPref.contains(SELECTED_LEVEL_KEY)) {
			this.loadPreviousState(sharedPref);
		} else {
			this.setBoard(this.selectedLevel);
			this.determineAndSetStartingNumber();
			this.accumulatedMillis = 0;
			this.congratsDialogFragmentIsVisible = false;
		}

		

		this.setUpButtons();
		this.setUpListView();
		
		this.timerHandler = new Handler();

		this.timerRunnable = new Runnable() {

			@Override
			public void run() {
				convertAndSetTime();

				timerHandler.postDelayed(this, 500);
			}
		};

		this.timerHandler.postDelayed(this.timerRunnable, 0);

	}
	
	private void convertAndSetTime() {
		currentMillis = SystemClock.uptimeMillis() - startTime
				+ accumulatedMillis;
		int seconds = (int) (currentMillis / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;

		timerTextView.setText(String
				.format("%d:%02d", minutes, seconds));
	}

	private void loadPreviousState(SharedPreferences sharedPref) {
		
		this.selectedLevel = sharedPref.getInt(SELECTED_LEVEL_KEY, 0);
		for (int i = 0; i < BOARD_WIDTH; i++) {
			for (int j = 0; j < BOARD_HEIGHT; j++) {
				this.loadSavedBoard(sharedPref, i, j);
			}
		}
		this.numberPicker.setValue(sharedPref.getInt(NUMBER_PICKER_KEY, 1));
		String selectedCellCoords = sharedPref.getString(SELECTED_CELL_KEY,
				null);
		this.accumulatedMillis = sharedPref.getLong(TIMER_KEY, -99);
		this.convertAndSetTime();
		if (selectedCellCoords != null) {
			String[] coords = selectedCellCoords.split(",");
			this.selectedCell = this.gameBoard[Integer.parseInt(coords[0])][Integer
					.parseInt(coords[1])];
			this.setSelectedCellColor();
		}
		this.congratsDialogFragmentIsVisible = sharedPref.getBoolean(IS_CONGRATS_SHOWING_KEY, false);
		if (this.congratsDialogFragmentIsVisible) {
			this.nlDialog = new NextLevelDialogFragment();
			this.nlDialog.show(getFragmentManager(), "NextLevel");
		}
	}

	private void loadSavedBoard(SharedPreferences sharedPref, int i, int j) {
		if (!sharedPref.getString(i + "," + j, "null").trim().equals("")) {
			if (this.isGivenNumber(Integer.parseInt(sharedPref.getString(
					i + "," + j, "null").trim()))) {
				this.gameBoard[i][j].setBackgroundColor(Color.LTGRAY);
				this.gameBoard[i][j].setTextColor(Color.BLUE);
			}
		}
		this.gameBoard[i][j].setText(sharedPref.getString(i + "," + j, "null"));
	}

	private void setUpListView() {
		ArrayList<String> levelsList = new ArrayList<String>();

		this.listAdapter = new CustomAdapter(this,
				android.R.layout.simple_list_item_1, levelsList);

		this.levelsListView = (ListView) findViewById(R.id.levelsListView);
		this.levelsListView.setAdapter(this.listAdapter);

		for (Integer currLevel : this.levels.keySet()) {
			this.listAdapter.add(getString(R.string.level) + " " + currLevel);
		}

		this.levelsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				for (int i = 0; i < levelsListView.getChildCount(); i++) {
					levelsListView.getChildAt(i).setBackgroundColor(
							Color.TRANSPARENT);
				}
				String selectedLevel = ((TextView) v).getText().toString()
						.split(" ")[1].trim();
				setToSelectedLevel(Integer.parseInt(selectedLevel));
				((TextView) v).setBackgroundColor(Color.LTGRAY);
				resetClock();
			}

		});
	}

	private void resetClock() {
		accumulatedMillis = 0;
		currentMillis = 0;
		startTime = SystemClock.uptimeMillis();
		timerHandler.postDelayed(timerRunnable, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!this.congratsDialogFragmentIsVisible) {
			this.startTime = SystemClock.uptimeMillis();
			this.timerHandler.postDelayed(this.timerRunnable, 0);
		}
	}


	@Override
	protected void onPause() {
		super.onPause();
		this.timerHandler.removeCallbacks(timerRunnable);
		this.accumulatedMillis = this.currentMillis;
		this.saveGameState();
	}

	private void saveGameState() {
		SharedPreferences sharedPref = this
				.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.clear();
		editor.commit();

		saveBoard(editor);
		editor.putInt(SELECTED_LEVEL_KEY, this.selectedLevel);
		editor.putInt(NUMBER_PICKER_KEY, this.numberPicker.getValue());
		editor.putLong(TIMER_KEY, this.accumulatedMillis);
		editor.putBoolean(IS_CONGRATS_SHOWING_KEY, this.congratsDialogFragmentIsVisible);
		editor.commit();
	}

	private void saveBoard(SharedPreferences.Editor editor) {
		for (int i = 0; i < BOARD_WIDTH; i++) {
			for (int j = 0; j < BOARD_HEIGHT; j++) {
				editor.putString(i + "," + j, this.gameBoard[i][j].getText()
						.toString());
				if (this.gameBoard[i][j] == this.selectedCell) {
					editor.putString(SELECTED_CELL_KEY, i + "," + j);
				}
			}
		}
	}

	private void readInLevels() {
		FileIO fileIO = new FileIO();
		try {
			this.levels = fileIO.getAllLevels(getAssets().open(
					String.format(LEVELS_TXT_LOCATION)));
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

	private void setTextViewListenersAndSetFont(BoardListener boardListener,
			GridLayout gridView) {
		ArrayList<TextView> textViews = this.getAllChildren(gridView);
		int counter = 0;
		for (int i = 0; i < BOARD_WIDTH; i++) {
			for (int j = 0; j < BOARD_HEIGHT; j++) {
				textViews.get(counter).setOnClickListener(boardListener);
				this.gameBoard[i][j] = textViews.get(counter);
				this.gameBoard[i][j].setTypeface(Typeface.MONOSPACE);
				this.gameBoard[i][j].setText("  ");
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
		if (this.isGivenNumber(selectedNumber)) {
			while (this.isGivenNumber(selectedNumber)) {
				selectedNumber = selectedNumber + 1;
			}
		}

		this.numberPicker.setValue(selectedNumber);
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
		return true;
	}

	private void setToSelectedLevel(int selectedLevel) {
		this.selectedLevel = selectedLevel;
		this.restartGame();
	}

	private void restartGame() {
		this.clearBoard();
		this.setBoard(this.selectedLevel);
		this.determineAndSetStartingNumber();
		if (this.selectedCell != null) {
			ColorDrawable buttonColor = (ColorDrawable) this.selectedCell
					.getBackground();
			if (buttonColor.getColor() != Color.LTGRAY) {
				this.selectedCell.setBackgroundColor(Color.WHITE);
				this.selectedCell.setTextColor(Color.BLACK);
			}
			this.selectedCell = null;
		}
		this.resetClock();
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

	private void setSelectedCellColor() {
		this.selectedCell.setBackgroundColor(Color
				.parseColor(SELECTED_CELL_COLOR));
		this.selectedCell.setTextColor(Color.BLUE);
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
				MediaPlayer sound = MediaPlayer.create(Connect64Activity.this,
						R.raw.cheering);
				sound.start();
				nlDialog = new NextLevelDialogFragment();
				Connect64Activity.this.congratsDialogFragmentIsVisible = true;
				nlDialog.show(getFragmentManager(), "NextLevel");
			} else {
				Toast.makeText(Connect64Activity.this, "Incorrect solution",
						Toast.LENGTH_SHORT).show();
			}
		}

		private void deleteCell() {
			if (Connect64Activity.this.selectedCell != null) {
				if (!Connect64Activity.this.selectedCell.getText().toString()
						.equals("  ")) {
					Connect64Activity.this.numberPicker.setValue(Integer
							.parseInt(Connect64Activity.this.selectedCell
									.getText().toString().trim()));
					Connect64Activity.this.selectedCell.setText("  ");
				}
			}
		}

	}

	private class BoardListener implements OnClickListener {

		private static final String BLANK_CELL = "  ";

		@Override
		public void onClick(View v) {
			if (Connect64Activity.this.selectionOnlyModeCheckBox.isChecked()) {
				this.setCellColorToNormal();
				Connect64Activity.this.selectedCell = (TextView) v;
				if (isNumeric(Connect64Activity.this.selectedCell.getText()
						.toString().trim())
						&& Connect64Activity.this.isGivenNumber(Integer
								.parseInt(selectedCell.getText().toString()
										.trim()))) {
					this.displayErrorAndSetSelectedToNull();
				} else {
					Connect64Activity.this.setSelectedCellColor();
				}
			} else {
				int selectedNumber = Connect64Activity.this.numberPicker
						.getValue();
				this.setCellColorToNormal();
				this.setNumberToCell(v, selectedNumber);
			}
		}

		private void setCellColorToNormal() {
			if (Connect64Activity.this.selectedCell != null) {
				Connect64Activity.this.selectedCell
						.setBackgroundColor(Color.WHITE);
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
			if (isNumeric(Connect64Activity.this.selectedCell.getText()
					.toString().trim())
					&& Connect64Activity.this
							.isGivenNumber(Integer.parseInt(selectedCell
									.getText().toString().trim()))) {
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
					R.string.cannot_alter_given_numbers, Toast.LENGTH_SHORT)
					.show();
		}

		private void setCellNumberAndIncrementSelector(int selectedNumber) {
			Connect64Activity.this.setSelectedCellColor();
			if (!isNumeric(Connect64Activity.this.selectedCell.getText()
					.toString())) {
				this.insertNumberInCellAndIncrementCounter(selectedNumber);
			}
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
					if (!Connect64Activity.this.gameBoard[i][j].getText()
							.toString().equals(BLANK_CELL)) {
						if (Integer
								.parseInt(Connect64Activity.this.gameBoard[i][j]
										.getText().toString().trim()) == selectedNumber) {
							return true;
						}
					}
				}
			}
			return false;
		}

	}

	private class NextLevelDialogFragment extends DialogFragment {
		private static final int LAST_LEVEL = 8;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getString(R.string.level_complete));
			timerHandler.removeCallbacks(timerRunnable);
			if (selectedLevel == LAST_LEVEL) {
				builder.setMessage(R.string.congratulations_message)
						.setPositiveButton(R.string.start_over,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										goToLevel(1);
										Connect64Activity.this.congratsDialogFragmentIsVisible = false;
									}
								})
						.setNegativeButton(R.string.replay,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										Connect64Activity.this.restartGame();
										Connect64Activity.this.congratsDialogFragmentIsVisible = false;
									}
								});
			} else {
				builder.setMessage(R.string.congratulations_message)
						.setPositiveButton(R.string.next_level,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {

										goToLevel(selectedLevel + 1);
										Connect64Activity.this.congratsDialogFragmentIsVisible = false;
									}

								})
						.setNegativeButton(R.string.replay,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										Connect64Activity.this.restartGame();
										Connect64Activity.this.congratsDialogFragmentIsVisible = false;
									}
								});

			}
			return builder.create();
		}

		private void goToLevel(int level) {
			Connect64Activity.this.levelsListView.getChildAt(
					Connect64Activity.this.selectedLevel - 1)
					.setBackgroundColor(Color.TRANSPARENT);
			Connect64Activity.this.selectedLevel = level;
			Connect64Activity.this
					.setToSelectedLevel(Connect64Activity.this.selectedLevel);
			Connect64Activity.this.levelsListView.getChildAt(
					Connect64Activity.this.selectedLevel - 1)
					.setBackgroundColor(Color.LTGRAY);

		}

	}

	private class CustomAdapter extends ArrayAdapter<String> {

		public CustomAdapter(Context context, int resource, List<String> objects) {
			super(context, resource, objects);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			if (sharedPref.getInt(SELECTED_LEVEL_KEY, -1) != -1) {
				if ((selectedLevel - 1) == position) {
					v.setBackgroundColor(Color.LTGRAY);
				} else {
					v.setBackgroundColor(Color.TRANSPARENT);
				}
			}
			return v;
		}

	}

}
