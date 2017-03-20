package leverageFactory;

import static info.DataPfad.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import util.BasisFkt;
import util.ComparatorReverseArrayInt;
import util.FktCollection;

public class ReadLeverage {

	/**
	 * write just Leverage.csv in Pfad folder, File name is Leverage.csv
	 */
	public static void writeLeverageCSV(String Pfad, int numberOfFilms) {

		try {

			int[][] cooc = (int[][]) FktCollection.serObjectRead_wrap(Pfad
					+ "Leverage.ser");

			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad
					+ "Leverage.csv"));

			for (int i = 1; i < numberOfFilms; i++) {
				for (int j = 0; j < i; j++) {
					if (cooc[i][j] > 0) {
						bw.write(cooc[i][j] + ";" + i + ";" + j + "\n");
					}

				}

			}

			bw.close();
			bw = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * OutputFile Format: rank, leverage, filmId, year, Title
	 * @param filmId
	 * @param inputFile
	 * @param movieTitlesTXT
	 * @param numberOfFilms
	 * @param numberOfSample
	 */
	public static void LeverageCheck(int filmId, String inputFile,
			String movieTitlesTXT, int numberOfFilms, int numberOfSample) {

		ArrayList<int[]> friends = new ArrayList<int[]>();
		String[][] movieTitles = BasisFkt.movieTitlesRead(movieTitlesTXT,
				numberOfFilms);

		try {

			BufferedReader br = new BufferedReader(new FileReader(inputFile));

			String line = br.readLine();

			StringTokenizer st;
			while (line != null) {
				st = new StringTokenizer(line, ";");
				int leverage = Integer.parseInt(st.nextToken());
				int film1 = Integer.parseInt(st.nextToken());
				int film2 = Integer.parseInt(st.nextToken());

				if (film1 == filmId) {
					friends.add(new int[] { film2, leverage });

				}
				if (film2 == filmId) {
					friends.add(new int[] { film1, leverage });
				}

				line = br.readLine();
			}

			br.close();
			br = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

		Collections.sort(friends, new ComparatorReverseArrayInt(1));

		System.out.println(filmId + ": " + movieTitles[filmId][0] + ","
				+ movieTitles[filmId][1]);

		for (int i = 0; i < 10; i++) {
			System.out.println(i + "te: " + (double) friends.get(i)[1]
					/ numberOfSample + ", " + friends.get(i)[0] + ", "
					+ movieTitles[friends.get(i)[0]][0] + ","
					+ movieTitles[friends.get(i)[0]][1]);
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int filmId = 7229;

//		Leverage_withoutoneCSV = "/home/adrian/Conny/Netflix/calLeverage/leverage5000Sample/Leverage_withoutone.csv";
		LeverageCheck(filmId, Leverage_withoutoneCSV, movieTitlesTXT,
				numberOfFilms, 5000);

	}

}
