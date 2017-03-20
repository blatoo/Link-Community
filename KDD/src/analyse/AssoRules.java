package analyse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TShortHashSet;
import cooc.CoocFkt;
import util.BasisFkt;
import util.ComparatorReverseArrayDouble;
import util.FktCollection;
import util.MyBitSet;

public class AssoRules {

	private String Pfad_doc;
	private String Pfad;
	private String movieTitlesTXT;
	private String userInfo_good_SimpleBIN;
	private int numberOfSampleUsers;
	private int numberOfSamples;
	private int startUser;
	private String filmeimKlusterSER;
	private String filmDegreeCSV;
	private int numberOfFilms = 17770;

	public AssoRules(String Pfad_doc, String Pfad, String movieTitlesTXT,
			String userInfo_good_SimpleBIN, int numberOfSampleUsers,
			int numberOfSamples, int startUser) {
		this.Pfad_doc = Pfad_doc;
		this.Pfad = Pfad;
		this.movieTitlesTXT = movieTitlesTXT;
		this.userInfo_good_SimpleBIN = userInfo_good_SimpleBIN;
		this.numberOfSampleUsers = numberOfSampleUsers;
		this.numberOfSamples = numberOfSamples;
		this.startUser = startUser;
		this.filmeimKlusterSER = Pfad + "FilmeimKluster.ser";
		this.filmDegreeCSV = Pfad_doc + "filmDegree20000.csv";

	}

	/**
	 * Contains the MyBitSet mbs percent of hs?
	 * 
	 * @param mbs
	 * @param hs
	 * @param percent
	 * @return if yes then return 1, otherwise 0;
	 */
	public int contain(MyBitSet mbs, TShortHashSet hs, double percent) {

		int contain = 0;

		int threshold = (int) Math.ceil((hs.size() * percent));

		short[] filmIds = hs.toArray();

		int cnt = 0;

		for (short e : filmIds) {
			if (mbs.get(e) == true) {
				cnt++;
				if (cnt >= threshold) {
					return contain = 1;
				}
			}

		}

		return contain;

	}

	/**
	 * Contains the MyBitSet mbs percent of hs? it returns by the way whether
	 * the user voted the film
	 * 
	 * @param mbs
	 * @param hs
	 * @param percent
	 * @return if yes then return 1, otherwise 0;
	 */
	public int[] contain2(MyBitSet mbs, TShortHashSet hs, double percent) {

		int[] contain = { 0, 0 };

		int threshold = (int) Math.ceil((hs.size() * percent));

		short[] filmIds = hs.toArray();

		int cnt = 0;
		boolean voted = false;

		for (short e : filmIds) {
			if (mbs.get(e) == true) {
				cnt++;
				voted = true;
				if (cnt >= threshold) {
					contain[0] = 1;
					break;
				}
			}

		}

		if (voted == true) {
			contain[1] = 1;
		} else {
			contain[1] = 0;
		}
		return contain;

	}

	public void buyTogether(int clusterId, double percent) {

		int[] movieDegree = BasisFkt.movieDegreeRead(filmDegreeCSV, 17770);
		String[][] movieTitles = BasisFkt.movieTitlesRead(movieTitlesTXT, numberOfFilms);

		MyBitSet[] degreeM = BasisFkt.degreeMatrix_MBS(userInfo_good_SimpleBIN,
				numberOfSampleUsers, startUser);

		int[][] edges = CoocFkt.createEdges(degreeM);

		TIntObjectHashMap<TShortHashSet> filmeimKluster = (TIntObjectHashMap<TShortHashSet>) FktCollection
				.serObjectRead_wrap(filmeimKlusterSER);

		int cnt = 0;
		int cntall = 0;

		short[] filmIds = filmeimKluster.get(clusterId).toArray();
		
		System.out.println("clusterId = "+clusterId+", this Cluster has "+filmIds.length+" movies, "+percent*100+"% movies of this cluster is "+(int)Math.ceil(filmIds.length*percent));
		for(int i = 0; i<filmIds.length; i++){
			System.out.println("\t"+movieTitles[filmIds[i]][0]+","+movieTitles[filmIds[i]][1]);
			
		}

		for (int i = 0; i < numberOfSampleUsers; i++) {

			int[] res = contain2(degreeM[i], filmeimKluster.get(clusterId), percent);
			cnt += res[0];
			cntall += res[1];
		}
		
		System.out.println("There are " + cnt
				+ " users voted "+(int)(percent*100)+"% movies in the Cluster " + clusterId);
		System.out.println("There are " + cntall
				+ " users voted at least one of the movies in the Cluster " + clusterId);
//		System.out.println("The minDegree is " + minDegree);

		int lengthOfWalks = 4 * edges.length;

		CoocFkt.swap(lengthOfWalks, edges, degreeM, -1);

		lengthOfWalks = (int) (numberOfSampleUsers * Math
				.log(numberOfSampleUsers));

		cnt = 0;
		cntall = 0;
		
		for(int round = 0; round < numberOfSamples; round++){
			CoocFkt.swap(lengthOfWalks, edges, degreeM, round);
			System.out.println("round "+round+"...");
			for (int i = 0; i < numberOfSampleUsers; i++) {
				
				int[] res = contain2(degreeM[i], filmeimKluster.get(clusterId), percent);
				cnt += res[0];
				cntall += res[1];

			}
			
		}
		
		cnt = cnt/numberOfSamples;
		cntall = cntall/numberOfSamples;
		

				
		System.out.println("There are " + cnt
				+ " users in the random sample voted "+(int)(percent*100)+"% movies in the Cluster " + clusterId);
		System.out.println("There are " + cntall
				+ " users in the random sample voted at least one of the movies in the Cluster " + clusterId);
		

	}
	
	
	public void buyTogetherAll(double percent, int numberOfSamples, String outputFile){
//		int[] movieDegree = BasisFkt.movieDegreeRead(filmDegreeCSV, numberOfFilms);
		String[][] movieTitles = BasisFkt.movieTitlesRead(movieTitlesTXT, numberOfFilms);

		MyBitSet[] degreeM = BasisFkt.degreeMatrix_MBS(userInfo_good_SimpleBIN,
				numberOfSampleUsers, startUser);

		int[][] edges = CoocFkt.createEdges(degreeM);

		TIntObjectHashMap<TShortHashSet> filmeimKluster = (TIntObjectHashMap<TShortHashSet>) FktCollection
				.serObjectRead_wrap(filmeimKlusterSER);
		
		for(TIntObjectIterator<TShortHashSet> it = filmeimKluster.iterator(); it.hasNext();){
			it.advance();
			if(it.value().size()==2){
				it.remove();
			}
			
		}
		
//		int cl = 114;
//		
//		int[] kankan = new int[2];
//		
//		for(int i = 0; i<numberOfSampleUsers; i++){
//			int[] res = contain2(degreeM[i], filmeimKluster.get(cl), percent);
//			kankan[0] += res[0];
//			kankan[1] += res[1];
//			
//		}
//		
//		System.out.println(kankan[0]+","+kankan[1]);
		
		
		int[] clusterIds = filmeimKluster.keys();
		
		double[][] obser = new double[filmeimKluster.size()][6];
		
		for(int i = 0; i<clusterIds.length; i++){
			obser[i][0] = clusterIds[i];
		}
		
		for(int i = 0; i<numberOfSampleUsers; i++){
			
			for(int j = 0; j<clusterIds.length; j++){
				
				int[] res = contain2(degreeM[i], filmeimKluster.get(clusterIds[j]), percent);
				obser[j][1] += res[0];
				obser[j][2] += res[1];
				
			}
			
		}	
		
		int lengthOfWalks = 4 * edges.length;

		CoocFkt.swap(lengthOfWalks, edges, degreeM, -1);

		lengthOfWalks = (int) (numberOfSampleUsers * Math
				.log(numberOfSampleUsers));
		
		for(int i = 0; i<numberOfSamples; i++){
			System.out.print("Round "+i+": ");
			CoocFkt.swap(lengthOfWalks, edges, degreeM, i);
			for(int j = 0; j<numberOfSampleUsers; j++){
				for(int k = 0; k<clusterIds.length; k++){
					int[] res = contain2(degreeM[j], filmeimKluster.get(clusterIds[k]), percent);
					
					obser[k][3] += res[0];
					obser[k][4] += res[1];
					
				}
				
			}
			
		}
		
		for(int i = 0; i<clusterIds.length; i++){
			obser[i][3] /= numberOfSamples;
			obser[i][4] /= numberOfSamples;
			obser[i][5] = obser[i][1]*obser[i][4]/((obser[i][3]+1)*obser[i][2]);
		}
		
		Arrays.sort(obser, new ComparatorReverseArrayDouble(5));
		
		for(int i = 0; i<clusterIds.length; i++){
			System.out.println(i+"te: "+(int)obser[i][0]+","+(int)obser[i][1]+","+(int)obser[i][2]+","+obser[i][3]+","+obser[i][4]+","+obser[i][5]);
			
		}
		
		
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			for(int i = 0; i<clusterIds.length; i++){
				bw.write(i+"te: ClusterId = "+(int)obser[i][0]+"; numberOfFilms = "+filmeimKluster.get((int)obser[i][0]).size()+" ("+(int)obser[i][1]+","+(int)obser[i][2]+","+obser[i][3]+","+obser[i][4]+","+obser[i][5]+")\n");
//				bw.write(i+"te: ClusterId = "+(int)obser[i][0]+"; ("+(int)obser[i][1]+","+(int)obser[i][2]+","+obser[i][3]+","+obser[i][4]+","+obser[i][5]+")\n");

				for(TShortIterator it = filmeimKluster.get((int)obser[i][0]).iterator(); it.hasNext();){
					short movieId = it.next();
					bw.write("\t"+movieId+","+movieTitles[movieId][0]+","+movieTitles[movieId][1]+"\n");
					
				}
				
			}
			
			bw.close();
			bw = null;
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {

		String Pfad_doc = "/home/adrian/Conny/Netflix/Diplom1/";
		String Pfad = "/home/adrian/Conny/Netflix/Diplom1/ClCo/svalue12/";
		String movieTitlesTXT = Pfad_doc + "movie_titles.txt";
		String userInfo_good_SimpleBIN = Pfad_doc
				+ "userInfo_good_Simple.binary";
		int numberOfSampleUsers = 20000;
		int numberOfSamples = 100;
		int startUser = 1;

		AssoRules as = new AssoRules(Pfad_doc, Pfad, movieTitlesTXT,
				userInfo_good_SimpleBIN, numberOfSampleUsers, numberOfSamples,
				startUser);
//		as.buyTogether(15560, 0.6 );
		as.buyTogetherAll(0.6, numberOfSamples, Pfad+"ClusterPropose_sorted_bigthan3.txt");

	}

}
