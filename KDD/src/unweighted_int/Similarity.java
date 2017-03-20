package unweighted_int;

//import static info.DataPfad.*;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import util.ComparatorReverseArrayIntIntFloat;
import util.FktCollection;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

public class Similarity {
	
	private String Pfad;
	private String inputFile;
	private String has_FrBIN;
	private String similarityBIN;
	
	public Similarity(String Pfad, String inputFile){
		this.Pfad = Pfad;
		this.inputFile = inputFile;
		this.has_FrBIN = Pfad+"has_Fr.binary";
		this.similarityBIN = Pfad+"similarity.binary";
		
	}
	
	public TIntObjectHashMap<TIntHashSet> adjM(){
		TIntObjectHashMap<TIntHashSet> adjM = new TIntObjectHashMap<TIntHashSet>();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			
			String line = br.readLine();
			
			StringTokenizer st = new StringTokenizer(line, ";");
			if(st.countTokens() == 2){
				
				while(line != null){
					st = new StringTokenizer(line, ";");
					
					int film1 = Integer.parseInt(st.nextToken());
					int film2 = Integer.parseInt(st.nextToken());
					
					if(!adjM.containsKey(film1)){
						adjM.put(film1, new TIntHashSet());
					}
					if(!adjM.containsKey(film2)){
						adjM.put(film2, new TIntHashSet());
					}
					
					adjM.get(film1).add(film2);
					adjM.get(film2).add(film1);
					
					
					line = br.readLine();
				}
				
			}else {
				while(line != null){
					st = new StringTokenizer(line, ";");
					st.nextToken();
					int film1 = Integer.parseInt(st.nextToken());
					int film2 = Integer.parseInt(st.nextToken());
					
					if(!adjM.containsKey(film1)){
						adjM.put(film1, new TIntHashSet());
					}
					if(!adjM.containsKey(film2)){
						adjM.put(film2, new TIntHashSet());
					}
					
					adjM.get(film1).add(film2);
					adjM.get(film2).add(film1);
					
					
					line = br.readLine();
				}
			}
			

			
			
			
			br.close();
			br = null;
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		FktCollection.serObjectWrite_wrap(Pfad+"adjM_clustercluster.ser", adjM);
		
		return adjM;
	}
	
	public void hasFriends(TIntObjectHashMap<TIntHashSet> adjM){
		
		int[] clusterIds = adjM.keys();

		try{
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(has_FrBIN)));
			
			for(int i = 0; i<clusterIds.length-1; i++){
				for(int j = i+1; j<clusterIds.length; j++){

					TIntHashSet i_friends = adjM.get(clusterIds[i]);
					TIntHashSet j_friends = adjM.get(clusterIds[j]);
					
					if(i_friends.size() < j_friends.size()){
						for(TIntIterator it = i_friends.iterator(); it.hasNext();){
							if(j_friends.contains(it.next())){
								dos.writeInt(clusterIds[i]);
								dos.writeInt(clusterIds[j]);
								break;
							}
							
						}
						
						
					}else {
						for(TIntIterator it = j_friends.iterator(); it.hasNext();){
							if(i_friends.contains(it.next())){
								dos.writeInt(clusterIds[i]);
								dos.writeInt(clusterIds[j]);
//								System.out.println(clusterIds[i]+","+clusterIds[j]);
								break;
							}
						}
						
					}
					
					
					
				}
				
			}
			
			dos.close();
			dos = null;
		}catch (IOException e) {
			e.printStackTrace();
		}
		

		
	}
	public void simisort(){
		
		try{
			
			ArrayList<Number[]> ar = new ArrayList<Number[]>();
			File file = new File(similarityBIN);
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(has_FrBIN)));
			DataInputStream dis2 = new DataInputStream(new BufferedInputStream(new FileInputStream(similarityBIN)));
			int numberOfPaars = (int)file.length()/4;
			for(int i = 0; i<numberOfPaars; i++){
				Number[] simi = new Number[3];
				float readSimi = dis2.readFloat();
				if(readSimi >= 0.1){
					simi[0] = dis.readInt();
					simi[1] = dis.readInt();
					simi[2] = readSimi;
					ar.add(simi);
				}else {
					dis.skip(8);
				}
				
			}
			System.out.println("There are "+ar.size()+ " similaries >= 0.1");
			Collections.sort(ar, new ComparatorReverseArrayIntIntFloat(2));
			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad+"similarity_sorted_90.txt"));
			
			for(int i = 0; i<ar.size(); i++){
				bw.write(ar.get(i)[2].floatValue()+";"+ar.get(i)[0].intValue()+";"+ar.get(i)[1].intValue()+"\n");
				
			}
			
			bw.close();
			bw = null;
			
			dis.close();
			dis = null;
			dis2.close();
			dis2 = null;
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void similarityCal(){

		TIntObjectHashMap<TIntHashSet> adjM = adjM();
		
		hasFriends(adjM);
		
		for(TIntObjectIterator<TIntHashSet> it = adjM.iterator(); it.hasNext();){
			it.advance();
			adjM.get(it.key()).add(it.key());
			
		}
		
		try{
			File file = new File(has_FrBIN);
			DataInputStream din = new DataInputStream(new BufferedInputStream(new FileInputStream(has_FrBIN)));
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(similarityBIN)));
			int pairs = (int)(file.length()/8);
			
			int cnt = 0;
			
			while(cnt < pairs){ 
				int cluster1 = din.readInt();
				int cluster2 = din.readInt();
				
				TIntHashSet friends1 = adjM.get(cluster1);
				TIntHashSet friends2 = adjM.get(cluster2);

				int numerator, denominator;
				if(friends1.size() < friends2.size()){
					numerator = 0;
					denominator = friends2.size();
					for(TIntIterator it = friends1.iterator(); it.hasNext();){
						if(friends2.contains(it.next())){
							numerator++;
						}else {
							denominator++;
						}
						
					}
					
				}else {
					numerator = 0;
					denominator = friends1.size();
					for(TIntIterator it = friends2.iterator(); it.hasNext();){
						if(friends1.contains(it.next())){
							numerator++;
						}else {
							denominator++;
						}
					}
				}
				
					double simi = (double)numerator/denominator;
					dos.writeFloat((float)simi);
//					System.out.println((float)simi+";"+cluster1+";"+cluster2);
				
				cnt++;
			}
			
			
			
			dos.close();
			dos = null;
			din.close();
			din = null;
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		FktCollection.mem("Free Memory...");
		
		simisort();
	}
	

	
}
