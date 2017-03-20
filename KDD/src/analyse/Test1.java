package analyse;

import gnu.trove.set.hash.TShortHashSet;

import java.util.Random;

import util.MyBitSet;

public class Test1 {

	public static void test1() {
		Random rand = new Random(1);

		for (int i = 0; i < 10; i++) {

			int number = rand.nextInt(100);

			System.out.print(number + ", ");

		}
		System.out.println();
		rand = new Random(2);

		for (int i = 0; i < 10; i++) {

			int number = rand.nextInt(100);

			System.out.print(number + ", ");

		}

		System.out.println();
	}

	public static void test2() {
		String Pfad_doc = "/home/adrian/Conny/Netflix/Diplom1/";
		String Pfad = "/home/adrian/Conny/Netflix/Diplom1/output/";
		String movieTitlesTXT = Pfad_doc + "movie_titles.txt";
		String userInfo_good_SimpleBIN = Pfad_doc
				+ "userInfo_good_Simple.binary";
		int numberOfSampleUsers = 20000;
		int numberOfSamples = 5000;
		int startUser = 1;

		AssoRules as = new AssoRules(Pfad_doc, Pfad, movieTitlesTXT,
				userInfo_good_SimpleBIN, numberOfSampleUsers, numberOfSamples,
				startUser);

		MyBitSet mbs = new MyBitSet();
//		mbs.set(1);
//		mbs.set(2);
//		mbs.set(3);
//		mbs.set(4);
//		mbs.set(5);

		TShortHashSet hs = new TShortHashSet();
		hs.addAll(new short[]{1,2,5,7,9});
		double percent = 0.6;
		int[] con = as.contain2(mbs, hs, percent);

		System.out.println(con[0]+","+con[1]);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test2();
	}

}
