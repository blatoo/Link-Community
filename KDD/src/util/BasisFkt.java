package util;

//import static info.DataPfad.*;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TShortHashSet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

public class BasisFkt {

	/**
	 * Get the Simple Data Information, which rating >= 4 Format:
	 * [-1][FilmID][FilmID]...[-32768]
	 * 
	 * @param inputFile
	 * @param outputFile
	 */
	public static void userInfoExtract(String inputFile, String outputFile) {
		File file = new File(inputFile);
		ArrayList<Integer> user_Films = new ArrayList<Integer>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			DataOutputStream out = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(outputFile)));
			String maxUserID = null;
			String line = br.readLine();
			System.out.println(line);
			String[] line_Elements = new String[3];
			int count = 0;
			while (line != null) {
				line_Elements = line.split(",");

				if (!line_Elements[1].equals(maxUserID)) {

					// System.out.println(line_Elements[0] + " "
					// + line_Elements[1] + " " + line_Elements[2]);

					Collections.sort(user_Films);
					for (int i = 0; i < user_Films.size(); i++) {
						out.writeShort(user_Films.get(i));
					}
					user_Films.clear();
					out.writeShort(-1);
					maxUserID = line_Elements[1];

					count++;

					if (count % 10000 == 1) {
						System.out.println("count = " + count + " "
								+ "maxUserID = " + maxUserID);
					}
				}

				if (Integer.parseInt(line_Elements[2]) < 4) {
					line = br.readLine();
					continue;
				}
				user_Films.add(Integer.parseInt(line_Elements[0]));

				line = br.readLine();

			}
			Collections.sort(user_Films);
			for (int i = 0; i < user_Films.size(); i++) {
				out.writeShort(user_Films.get(i));
			}
			out.writeShort(-32768);

			System.out.println("count = " + count);

			out.close();
			br.close();

		} catch (IOException e) {
			System.err.println(e.toString());
		}

	}

	/**
	 * Calculate Degree. Nodes in the left side. Also: [userid] ->
	 * [filmID][filmID][filmID][filmID]
	 * 
	 * @param inputFile
	 * @param numberOfSampleUsers
	 * @return
	 */
	public static MyBitSet[] degreeMatrix_MBS(String inputFile,
			int numberOfSampleUsers, int startUser) {

		MyBitSet[] user_films = new MyBitSet[numberOfSampleUsers];
		for (int i = 0; i < numberOfSampleUsers; i++) {
			user_films[i] = new MyBitSet();
		}

		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(
					new FileInputStream(inputFile)));

			// Find the start position.
			int count = 0;
			int a = 0;
			while (count < startUser) {
				if ((a = in.readShort()) == -1) {
					count++;
				}
			}

			// Read in the Bitset Matrix.
			count = 1;

			while (count < numberOfSampleUsers + 1) {

				if ((a = in.readShort()) == -1) {
					count++;
					// System.out.println();
					continue;
				} else if (a == -32768) {
					break;
				}

				user_films[count - 1].set(a - 1);
				// System.out.print(a + " ");

			}

		} catch (IOException e) {
			System.err.println(e.toString());
		}

		return user_films;

	}

	/**
	 * Read adjacenceMatrix from a binary File
	 * 
	 * @param inputFile
	 * @param numberOfSampleUsers
	 * @param startUser
	 * @return
	 */
	public static MyBitSet[] adjacenceMatrix_MBS(String inputFile,
			int numberOfSampleUsers, int startUser, int numberOfFilms) {
		if (numberOfSampleUsers + startUser - 1 > 480189) {
			System.out.println("Not enough user to test");
		}

		MyBitSet[] aM = new MyBitSet[numberOfFilms];
		for (int i = 0; i < numberOfFilms; i++) {
			aM[i] = new MyBitSet();
		}

		try {
			RandomAccessFile in = new RandomAccessFile(inputFile, "r");
			int a = 0;
			int count = 0;
			while (count < startUser) {
				a = in.readShort();
				if (a < 0) {
					count++;
				}
			}

			count = 1;
			while (count <= numberOfSampleUsers) {
				if ((a = in.readShort()) < 0) {
					count++;
					// if (count % 100 == 1) {
					// System.out.println(count);
					// }
					continue;
				}
				aM[a - 1].set(count - 1);
			}

		} catch (IOException e) {
			System.err.println(e.toString());
		}

		return aM;
	}

	/**
	 * adjazenzMatrix: film - film, ohne sich selbst
	 * 
	 * @param inputFile
	 * @return TShortObjectHashMap<MyBitSet>
	 */

	public static TShortObjectHashMap<util.MyBitSet> getAdjMatrix(
			String inputFile) {
		TShortObjectHashMap<util.MyBitSet> hm = new TShortObjectHashMap<util.MyBitSet>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line = br.readLine();

			short s = -1;
			short t = -1;
			int cnt = -1;
			StringTokenizer st = new StringTokenizer(line, ";");
			if (st.countTokens() == 3) {
				while (line != null) {
					cnt++;
					st = new StringTokenizer(line, ";");
					st.nextToken();
					s = Short.parseShort(st.nextToken());
					t = Short.parseShort(st.nextToken());
					if (hm.containsKey(s)) {
						hm.get(s).set(t);
					} else {
						hm.put(s, new util.MyBitSet());
						hm.get(s).set(t);
					}
					if (hm.containsKey(t)) {
						hm.get(t).set(s);
					} else {
						hm.put(t, new util.MyBitSet());
						hm.get(t).set(s);
					}
					line = br.readLine();
					// System.out.println(cnt+"te: "+line);
					if (cnt % 1000000 == 1) {
						System.out.println(cnt + "te: (" + s + ", " + t + ")");
					}
				}

			} else {
				while (line != null) {
					cnt++;
					st = new StringTokenizer(line, ";");
					s = Short.parseShort(st.nextToken());
					t = Short.parseShort(st.nextToken());
					if (hm.containsKey(s)) {
						hm.get(s).set(t);
					} else {
						hm.put(s, new util.MyBitSet());
						hm.get(s).set(t);
					}
					if (hm.containsKey(t)) {
						hm.get(t).set(s);
					} else {
						hm.put(t, new util.MyBitSet());
						hm.get(t).set(s);
					}
					line = br.readLine();
					// System.out.println(cnt+"te: "+line);
					if (cnt % 1000000 == 1) {
						System.out.println(cnt + "te: (" + s + ", " + t + ")");
					}
				}

				System.out
						.println("Input Film-Film AdjazenzMatrix fertig! Insgesamt "
								+ hm.size() + " unisolierte Filme");

				br.close();
				br = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return hm;
	}

	/**
	 * count the number of elements in a (MyBitSet[])Matrix
	 * 
	 * @param adjM
	 * @return
	 */
	public static int numberOfOnes(MyBitSet[] adjM) {
		int number = 0;
		int length = adjM.length;
		for (int i = 0; i < length; i++) {
			number += adjM[i].cardinality();
		}

		return number;
	}

	public static void writeMovieDegree(String userInfoBin,
			String filmDegreeCSV, int numberOfSampleUsers, int startUser,
			int numberOfFilms) {

		MyBitSet[] adjM = adjacenceMatrix_MBS(userInfoBin, numberOfSampleUsers,
				startUser, numberOfFilms);

		try {

			BufferedWriter bw = new BufferedWriter(
					new FileWriter(filmDegreeCSV));

			for (int i = 0; i < adjM.length; i++) {
				bw.write(i + "," + adjM[i].cardinality() + "\n");
			}

			bw.close();
			bw = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static int[] movieDegreeRead(String movieDegreeCSV, int numberOfFilms) {
		int[] movieDegree = new int[numberOfFilms];

		try {
			BufferedReader br = new BufferedReader(new FileReader(
					movieDegreeCSV));

			String line = br.readLine();
			StringTokenizer st;
			for (int i = 0; i < numberOfFilms; i++) {

				st = new StringTokenizer(line, ",");

				st.nextToken();

				movieDegree[i] = Integer.parseInt(st.nextToken());

				line = br.readLine();
			}

			br.close();
			br = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return movieDegree;

	}

	/**
	 * read the movieTitles
	 * Format: movieId -> year,title
	 * @param movieTitlesTXT
	 * @param numberOfFilms
	 * @return
	 */
	public static String[][] movieTitlesRead(String movieTitlesTXT,
			int numberOfFilms) {
		String[][] mT = new String[numberOfFilms][2];

		try {

			BufferedReader br = new BufferedReader(new FileReader(
					movieTitlesTXT));

			String line = br.readLine();
			int cnt = 0;
			while (line != null) {
				String[] line_ele = line.split(",", 3);
				mT[cnt][0] = line_ele[1];
				mT[cnt][1] = line_ele[2];

				cnt++;
				line = br.readLine();

			}

			br.close();
			br = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return mT;

	}

	/**
	 * calculate the local density
	 * 
	 * Formel: D_c = [mc - (nc - 1)] /[nc*(nc - 1)/2 - (nc - 1)]
	 * 
	 * @param clusterId
	 * @param filmeinKluster
	 *            : ClusterId -> Member(filmID)
	 * @param hm
	 *            : ClusterId -> Member(KantenId)
	 * @return density
	 */
	public static double densityCal(int clusterId,
			TIntObjectHashMap<TShortHashSet> filmeinKluster,
			TIntObjectHashMap<TIntHashSet> hm) {
		double density = 0;

		if (hm.get(clusterId) == null) {
			return 0;
		}

		int nc = filmeinKluster.get(clusterId).size();

		int mc = hm.get(clusterId).size();

		int zaehler = mc - nc + 1;
		int nenner = nc * (nc - 1) / 2 - nc + 1;

		density = (double) zaehler / nenner;

		return density;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numberOfSampleUsers = 20000;
		int startUser = 1;
		// adjacenceMatrix_MBS(userInfoBin, numberOfSampleUsers, startUser,
		// numberOfFilms);
		// writeMovieDegree(userInfoBin, filmDegreeCSV, numberOfSampleUsers,
		// startUser, numberOfFilms);

	}

}
