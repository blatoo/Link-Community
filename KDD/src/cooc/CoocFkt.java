package cooc;

import java.util.ArrayList;
import java.util.Random;

import util.BasisFkt;
import util.MyBitSet;

public class CoocFkt {

	public static void read_cooccurence_from_DegreeMatrix_noClear(
			MyBitSet[] degreeM, int[][] cooc) {

//		 for every user read the films which he has rated, and the calculate
//		 the cooc
//		 Method1 with ArrayList
		 ArrayList<Integer> userInfo = new ArrayList<Integer>();
		
		 for (int user = 0; user < degreeM.length; user++) {
		 userInfo.clear();
		
		 for (int filmID = degreeM[user].nextSetBit(0); filmID >= 0; filmID =
		 degreeM[user]
		 .nextSetBit(filmID + 1)) {
		 userInfo.add(filmID);
		 }
		
		 for (int i = 0; i < userInfo.size(); i++) {
		 for (int j = i + 1; j < userInfo.size(); j++) {
		 cooc[userInfo.get(i)][userInfo.get(j)]++;
		 }
		 }
		
		 }
		

//		// Method2 with array
//		
//		
//		for(int userID = 0; userID<degreeM.length; userID++){
//			
//			int[] userInfo = new int[degreeM[userID].cardinality()];
//			int cnt = 0;
//			for(int filmID = degreeM[userID].nextSetBit(0); filmID >= 0; filmID = degreeM[userID].nextSetBit(filmID+1)){
//				userInfo[cnt] = filmID;
//				cnt++;	
//			}
//
//			for(int i = 0; i<userInfo.length-1; i++){
//				for(int j= i+1; j<userInfo.length; j++){
//					cooc[userInfo[i]][userInfo[j]]++;
//				}
//				
//			}
//		}
		
		

	}
	
	/**
	 * create the edges
	 * format: int[numberOfEdges][2]
	 * 		   edge[cnt][0] = userId, edge[cnt][1] = movieId 
	 * @param degreeM
	 * @return
	 */
	public static int[][] createEdges(MyBitSet[] degreeM){
		
		int numberOfEdges = BasisFkt.numberOfOnes(degreeM);
		
		int[][] edges = new int[numberOfEdges][2];
		
		int cnt = -1;
		
		for(int i = 0; i<degreeM.length; i++){
			for(int j = degreeM[i].nextSetBit(0); j>=0; j = degreeM[i].nextSetBit(j+1)){
				cnt++;
				edges[cnt][0] = i;
				edges[cnt][1] = j;
				
			}
			
		}
		
		return edges;
	}
	
	/**
	 * swap the int[][] edges and MyBitSet[] degreeM
	 * @param lengthOfWalks
	 * @param edges
	 * @param degreeM
	 */
	public static void swap(int lengthOfWalks, int[][] edges, MyBitSet[] degreeM,  int seed){
		int numberOfEdges = edges.length;
		
		int unswapable = 0;
		
		// Begin swap and change cooc.
		Random generator_edge = new Random(seed);
		
		for(int i=0; i<lengthOfWalks; i++){
			
			// Find any edge pair.
			int edgeNumber1 = generator_edge.nextInt(numberOfEdges);
			int edgeNumber2;
			do{
				edgeNumber2 = generator_edge.nextInt(numberOfEdges);
			}while(edgeNumber1==edgeNumber2);
			
			// Check if the edge pair are swapable
			
			int user_m = edges[edgeNumber1][0];
			int film_p = edges[edgeNumber1][1];
			int user_n = edges[edgeNumber2][0];
			int film_q = edges[edgeNumber2][1];
			
			// Check if they are swapable.
			if(degreeM[user_m].get(film_q) == true || degreeM[user_n].get(film_p) == true){
				unswapable++;
				continue;
			}
			
			// If they are swapable, then
			
			// change the edges's Information
			edges[edgeNumber1][1] = film_q;
			edges[edgeNumber2][1] = film_p;
			
			// change the degreeM's Information
			degreeM[user_m].flip(film_p);
			degreeM[user_m].flip(film_q);
			degreeM[user_n].flip(film_p);
			degreeM[user_n].flip(film_q);
			
		}
		
		System.out.println("unswapable steps = "+unswapable);
		
	}
	
	/**
	 * (die rechte oben cooc Matrix * 5000), addiere sie dann nach link unten.
	 * 
	 * @param adjM
	 * @return
	 */
	public static void addhalfMatrixm5000(int[][] cooc, int numberOfSample) {
		int length = cooc.length;
		for (int i = 0; i < length; i++) {
			for (int j = i + 1; j < length; j++) {
				if (cooc[i][j] != 0) {
					cooc[j][i] += numberOfSample * cooc[i][j];
				}

			}
		}
	}
	
	public static void clearRightUpperCooc(int[][] cooc) {
		for (int i = 0; i < cooc.length - 1; i++) {
			for (int j = i + 1; j < cooc.length; j++) {
				cooc[i][j] = 0;
			}

		}

	}
	
	public static void substracthalfMatrix(int[][] cooc) {
		int length = cooc.length;
		for (int i = 0; i < length; i++) {
			for (int j = i + 1; j < length; j++) {
				if (cooc[i][j] != 0) {
					cooc[j][i] -= cooc[i][j];
				}
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
