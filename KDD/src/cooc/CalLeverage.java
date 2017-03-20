package cooc;

import util.MyBitSet;
import util.BasisFkt;

/**
 * Calculate Leverage
 * 
 * @author conny
 *
 */

public class CalLeverage {
	
	/**
	 * swap 4n steps and then calculate the Leverage.
	 * Method: swap and then read the cooccurence for every random sample.
	 * Comment: it is slow for fewer users, but it is fast for many users.
	 * @param inputFile: this is the binary inputfile: userInfo_good_Simple.binary
	 * @param cooc
	 * @param numberOfSampleUsers
	 * @param startUser
	 * @param numberOfSample
	 */

	public static void runCooc1(String inputFile, int[][] cooc, int numberOfSampleUsers, int startUser, int numberOfSample){
		// Read the degreeMatrix: userId: movieId, movieId...
		
		MyBitSet[] degreeM = BasisFkt.degreeMatrix_MBS(inputFile, numberOfSampleUsers, startUser);
		
		int[][] edges = CoocFkt.createEdges(degreeM);
		
		CoocFkt.read_cooccurence_from_DegreeMatrix_noClear(degreeM, cooc);
		
		CoocFkt.addhalfMatrixm5000(cooc, numberOfSample);
		
		CoocFkt.clearRightUpperCooc(cooc);
		
		int lengthOfWalks = 4*edges.length;

		CoocFkt.swap(lengthOfWalks, edges, degreeM, -1);
		
		lengthOfWalks = (int)(numberOfSampleUsers * Math.log(numberOfSampleUsers));
	
		for(int i = 0; i<numberOfSample; i++){
			System.out.println("Round" + i + "............................");
			long t1 = System.currentTimeMillis();
			CoocFkt.swap(lengthOfWalks, edges, degreeM, i);
			CoocFkt.read_cooccurence_from_DegreeMatrix_noClear(degreeM, cooc);
			long t2 = System.currentTimeMillis();
			System.out.println("this round uses "+(t2-t1)/1000 +" seconds");
		}

		CoocFkt.substracthalfMatrix(cooc);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
