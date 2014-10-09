package edu.uwg.Joeculberson.connect64;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;
import android.graphics.Point;

public class FileIO {
	public FileIO() {

	}

	public HashMap<Integer, HashMap<Point, Integer>> getAllLevels(InputStream is) {
		HashMap<Integer, HashMap<Point, Integer>> levels = new HashMap<Integer, HashMap<Point, Integer>>();
		Scanner fileScanner = new Scanner(is);
		
		while (fileScanner.hasNextLine()) {
			String currLine = fileScanner.nextLine();
			
			String[] levelAndBoard = currLine.split("#");
			Integer levelNumber = Integer.parseInt(levelAndBoard[0]);
			String[] boardSetup = levelAndBoard[1].split(" ~ ");
			
			HashMap<Point, Integer> givenSpaces = new HashMap<Point, Integer>();
			for (int i = 0; i < boardSetup.length; i++) {
				String[] coordsAndVal = boardSetup[i].split(",");
				String[] yValAndCellVal = coordsAndVal[1].split(":");
				Point coords = new Point(Integer.parseInt(coordsAndVal[0]), Integer.parseInt(yValAndCellVal[0]));
				givenSpaces.put(coords, Integer.parseInt(yValAndCellVal[1]));
			}
			levels.put(levelNumber, givenSpaces);
		}
		fileScanner.close();
		return levels;
	}

	public File[] getAllFilesInFolder(String path) {
		File folder = new File(path);
		return folder.listFiles();
	}
}
