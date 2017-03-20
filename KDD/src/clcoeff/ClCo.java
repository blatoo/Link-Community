package clcoeff;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.iterator.TShortObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TShortHashSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileManager.Location;

import util.FktCollection;

public class ClCo {
	
	public static String Pfad_doc = "/home/adrian/Conny/Netflix/Diplom1/";
	public static String Pfad = "/home/adrian/Conny/Netflix/Diplom1/ClCo/";
	public static int numberOfFilms = 17770;
	
	public static void testDistin(String inputFile){
		
		TIntHashSet ts = new TIntHashSet();
		try{
			BufferedReader br = new BufferedReader(new FileReader(inputFile));

			String line = br.readLine();
			
			StringTokenizer st;
			
			while(line != null){
				st = new StringTokenizer(line, ";");
				int gewicht = Integer.parseInt(st.nextToken());
				ts.add(gewicht);
				
				
				line = br.readLine();
			}
			
			
			br.close();
			br = null;
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(ts.size());
		
		
		
		
	}


	public static void removetheEdges(HashSet<short[]> hs, TShortObjectHashMap<TShortHashSet> adjM){
		for(Iterator<short[]> it = hs.iterator(); it.hasNext();){
			short[] edge = it.next();
			adjM.get(edge[0]).remove(edge[1]);
			adjM.get(edge[1]).remove(edge[0]);
		}
		
		for(TShortObjectIterator<TShortHashSet> it = adjM.iterator(); it.hasNext();){
			it.advance();
			if(it.value().size() == 0){
				it.remove();
			}else {
				it.value().compact();
			}
			
		}
		
		adjM.compact();
		
	}
	
	public static double localCoeff(TShortObjectHashMap<TShortHashSet> adjM, short film){
		
		double coeff = 0;
		short[] friends = adjM.get(film).toArray();
		
		if(friends.length == 1){
			return coeff;
		}
		
		int tau = friends.length*(friends.length-1)/2;
		
		int lambda = 0;
		
		for(int i = 0; i<friends.length-1; i++){
			for(int j =i+1; j<friends.length; j++){
				if(adjM.get(friends[i]).contains(friends[j])){
					lambda++;
				}
				
			}
			
		}
		
		
		coeff = (double) lambda/tau;
		
		return coeff;
		
	}
	                                
	
	
	public static void clcoe1(String inputFile){
		
		//Read the SValue.csv into a hashMap: svalue-> edges(film1, film2)
		TIntObjectHashMap<HashSet<short[]>> infoHM = new TIntObjectHashMap<HashSet<short[]>>();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			
			String line = br.readLine();
			
			StringTokenizer st;
			
			while(line != null){
				
				st = new StringTokenizer(line, ";");
				
				int wert = Integer.parseInt(st.nextToken())/10000;
				
				if(infoHM.contains(wert)){
					infoHM.get(wert).add(new short[]{Short.parseShort(st.nextToken()), Short.parseShort(st.nextToken())});
				}else {
					infoHM.put(wert, new HashSet<short[]>());
					
				}
				
				
				line = br.readLine();
			}
			
			br.close();
			br = null;
			

			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
		infoHM.remove(0);
		
		int[] wertList = infoHM.keys();
		
		Arrays.sort(wertList);
		
		TShortObjectHashMap<TShortHashSet> adjM = new TShortObjectHashMap<TShortHashSet>();
		
		for(int i = 0; i<numberOfFilms; i++){
			adjM.put((short)i, new TShortHashSet());
		}

		for(TIntObjectIterator<HashSet<short[]>> it = infoHM.iterator(); it.hasNext();){
			it.advance();
			for(Iterator<short[]> edgeIt = it.value().iterator(); edgeIt.hasNext();){
				short[] edge = edgeIt.next();			
				adjM.get(edge[0]).add(edge[1]);
				adjM.get(edge[1]).add(edge[0]);
				
			}
		}
		
		for(TShortObjectIterator<TShortHashSet> it = adjM.iterator(); it.hasNext();){
			it.advance();
			if(it.value().size() == 0){
				it.remove();
			}else {
				it.value().compact();
			}
		}

		adjM.compact();
		
		System.out.println("haha...");
		
		double maxCoeff = 0;
		int swert = 0;
		
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad+"ClusterCoeff.txt"));
			long t1 = System.currentTimeMillis();

			for(int i = 0; i<wertList.length; i++){
				
				Double averageCoeff = 0.0;
				for(TShortObjectIterator<TShortHashSet> it = adjM.iterator(); it.hasNext();){
					it.advance();
					double local = localCoeff(adjM, it.key());
					averageCoeff += local;
				}
				
				averageCoeff = averageCoeff/adjM.size();
				System.out.println("averageCoeff = "+averageCoeff+", adjM.size = "+adjM.size());


				long t2 = System.currentTimeMillis()-t1;
				System.out.println(wertList[i]+", "+averageCoeff);
				bw.write(wertList[i]+" "+averageCoeff+"\n");
				System.out.println("round "+i+" uses "+t2/1000+" seconds");
				
				if(averageCoeff > maxCoeff){
					maxCoeff = averageCoeff;
					swert = wertList[i];
				}
				
				System.out.println();
				
				
				removetheEdges(infoHM.get(wertList[i]), adjM);
				
			}
			
			bw.write("maxCoeff = "+maxCoeff+", where svalue = "+swert);
			
			bw.close();
			bw = null;
		}catch (IOException e) {
			e.printStackTrace();
		}
		

		

		

		
		
		
		
		
		
		
		
		
		
		
	}
	
	
	public static void main(String[] args) {
//		testDistin(Pfad_doc+"SValue.csv");
		String inputFile = Pfad_doc+"SValue.csv";
		clcoe1(inputFile);
		
	}
}
