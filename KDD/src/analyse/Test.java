package analyse;

import java.util.ArrayList;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TShortHashSet;
import util.BasisFkt;
import util.FktCollection;
import util.MyBitSet;
import cooc.CoocFkt;

import static info.DataPfad.*;

public class Test {
	
	public static void test1(int clusterId){
		
		int[] movieDegree = BasisFkt.movieDegreeRead(filmDegreeCSV, 17770);
		String[][] movieTitles = BasisFkt.movieTitlesRead(movieTitlesTXT, numberOfFilms);
		String filmeimKlusterSER = Pfad + "FilmeimKluster.ser";


		MyBitSet[] degreeM = BasisFkt.degreeMatrix_MBS(userInfoBin,
				20000, 1);

		int[][] edges = CoocFkt.createEdges(degreeM);

		TIntObjectHashMap<TShortHashSet> filmeimKluster = (TIntObjectHashMap<TShortHashSet>) FktCollection
				.serObjectRead_wrap(filmeimKlusterSER);
		
		short[] filmIds = filmeimKluster.get(clusterId).toArray();
		
		for(int i = 0; i<20000; i++){
			ArrayList<Short> ar = new ArrayList<Short>();
			for(short e:filmIds){
				if(degreeM[i].get(e)){
					ar.add(e);
				}
			}
			if(ar.size()!=0){
				System.out.print("user "+i+" loves: ");
				for(int j = 0; j<ar.size(); j++){
					System.out.print(ar.get(j)+", ");
				}
				System.out.println();

			}
			
		}
		
		int threshold = 2;
		int votedusers = 0;
		for(int i = 0; i<20000; i++){
			int cnt = 0;
			boolean treffen = false;
			for(short e : filmIds){
				if(degreeM[i].get(e) == true){
					cnt++;
					if(cnt >= threshold){
						treffen = true;
						break;
					}
				}
				
			}
			
			if(treffen){
				votedusers++;

			}
			
			
		}
		
		System.out.println(votedusers);
		
		
	}

	public static void test2(){
		int a = 1000000;
		double c = 2.7;
		
		Number b = a;
		
		System.out.println(b.doubleValue());
		
	}
	
	
	public static void main(String[] args) {
		
		test2();
		
	}

}
