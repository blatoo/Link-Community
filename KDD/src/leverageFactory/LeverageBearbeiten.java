package leverageFactory;

import static util.FktCollection.*;

import gnu.trove.map.hash.TShortObjectHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import util.BasisFkt;
import util.MyBitSet;

public class LeverageBearbeiten {
	
	
	public static void levNoOneDegree(String leverageCSV, String Leverage_withoutone, String filmDegreeCSV, int numberOfFilms){
		
		// Lesen filmDegree in einen Array
		int[] filmDg = new int[numberOfFilms];
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(filmDegreeCSV));
			String line = br.readLine();
			StringTokenizer st;
			
			while(line != null){
				st = new StringTokenizer(line, ",");
				filmDg[Integer.parseInt(st.nextToken())] = Integer.parseInt(st.nextToken());
				line = br.readLine();
			}
			
			br.close();
			br = null;
			
		}catch (IOException e) {
			e.printStackTrace();
		}

		// Begin filter and write
		
		try{
			
			BufferedReader br = new BufferedReader(new FileReader(leverageCSV));
			BufferedWriter bw = new BufferedWriter(new FileWriter(Leverage_withoutone));
			
			String line = br.readLine();
			StringTokenizer st;
			
			while(line != null){
				st = new StringTokenizer(line, ";");
				st.nextToken();
				int film1 = Integer.parseInt(st.nextToken());
				int film2 = Integer.parseInt(st.nextToken());
				if(filmDg[film1] > 1 && filmDg[film2] > 1)
					bw.write(line+"\n");
				line = br.readLine();
			}
			bw.close();
			bw = null;
			
			br.close();
			br = null;
			
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void getSValue(String Leverage_withoutone, String filmDegreeCSV, String SValueCSV, int numberOfFilms){
		try{
			
			BufferedReader br = new BufferedReader(new FileReader(Leverage_withoutone));
			BufferedWriter bw = new BufferedWriter(new FileWriter(SValueCSV));
			
			int[] movieDegree = BasisFkt.movieDegreeRead(filmDegreeCSV, numberOfFilms);
			
			String line = br.readLine();
			StringTokenizer st;
			while(line != null){
				st = new StringTokenizer(line, ";");
				int lev = Integer.parseInt(st.nextToken());
				int film1 = Integer.parseInt(st.nextToken());
				int film2 = Integer.parseInt(st.nextToken());
				
				int maxDegree = movieDegree[film1] > movieDegree[film2] ? movieDegree[film1] : movieDegree[film2]; 
				
				int svalue = lev*100/maxDegree;
				
				bw.write(svalue+";"+film1+";"+film2+"\n");
				
				
				line = br.readLine();
			}
			
			
			
			bw.close();
			bw = null;
			br.close();
			br = null;
			
			
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Leverage to an adjazenz matrix. outputFile is Pfad+"adjM_filmfilm.ser"
	 * @param inputFile
	 * @param Pfad
	 */
	public static void levToAdjMatrix_unweighted(String inputFile, String Pfad){
		TShortObjectHashMap<MyBitSet> adjM = new TShortObjectHashMap<MyBitSet>();
		try{
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line = br.readLine();
		StringTokenizer st;
		while(line != null){
			st = new StringTokenizer(line, ";");
			short film1 = Short.parseShort(st.nextToken());
			short film2 = Short.parseShort(st.nextToken());
			
			if(!adjM.containsKey(film1)){
				adjM.put(film1, new MyBitSet());
			}
			if(!adjM.containsKey(film2)){
				adjM.put(film2, new MyBitSet());
			}
			
			adjM.get(film1).set(film2);
			adjM.get(film2).set(film1);
			
			line = br.readLine();
		}
		
		
		
		br.close();
		br = null;
			
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		serObjectWrite_wrap(Pfad+"adjM_filmfilm.ser", adjM);
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String Pfad_doc = "/home/adrian/Conny/Netflix/Diplom1/";
		String Pfad = "/home/adrian/Conny/Netflix/Diplom1/output/";
		levToAdjMatrix_unweighted(Pfad+"halbLeverage.csv", Pfad);
		
		
	}

}
