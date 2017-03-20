package leverageFactory;

import static info.DataPfad.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileManager.Location;

import gnu.trove.iterator.TShortIntIterator;
import gnu.trove.iterator.TShortObjectIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TShortArrayList;
import gnu.trove.map.hash.TShortIntHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import util.FktCollection;
import util.MyBitSet;

public class SelectLeverage {

	/**
	 * make the weighted graph to an unweighted graph by divide the leverage and choose the reciprocal edge 
	 * @param mass
	 * @param Pfad
	 * @param inputFile
	 * @param outputFile
	 */
	public static void halbLeverage(int mass, String Pfad, String inputFile, String outputFile) {

		// film-film Graph with Leverage bauen

		TShortObjectHashMap<TShortIntHashMap> hm = new TShortObjectHashMap<TShortIntHashMap>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(
					inputFile));
			String line = br.readLine();
			StringTokenizer st;

			while (line != null) {
				st = new StringTokenizer(line, ";");
				int lev = Integer.parseInt(st.nextToken());
				short film1 = Short.parseShort(st.nextToken());
				short film2 = Short.parseShort(st.nextToken());

				if (hm.contains(film1) == false)
					hm.put(film1, new TShortIntHashMap());
				if (hm.contains(film2) == false)
					hm.put(film2, new TShortIntHashMap());

				hm.get(film1).put(film2, lev);
				hm.get(film2).put(film1, lev);

				line = br.readLine();
			}

			int cnt = 0;

			TShortObjectIterator<TShortIntHashMap> it = hm.iterator();
			while (it.hasNext()) {
				it.advance();
				cnt += it.value().size();
			}
			System.out.println("Der Graph hat "+cnt+" Kanten.");

			br.close();
			br = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

		hm.compact();

//		System.out.println("hm Matrix with voll leverage ist fertig");
		System.out.println("beginn zu halbieren...");

		// halbiere die Leverage

		// TShortObjectIterator<TShortIntHashMap> it = hm.iterator();

		for (TShortObjectIterator<TShortIntHashMap> it = hm.iterator(); it
				.hasNext();) {
			it.advance();

			// Notize die maximale Leverage und halbiere sie

			TIntArrayList tar = new TIntArrayList(it.value().values());
			int max = tar.max();
			int threshold = max / mass;

			// Entfernen die Kanten mit kleine Leverage
			for (TShortIntIterator it2 = it.value().iterator(); it2.hasNext();) {
				it2.advance();
				if (it2.value() < threshold)
					it2.remove();
			}

		}

		int cnt = 0;

		for (TShortObjectIterator<TShortIntHashMap> it = hm.iterator(); it
				.hasNext();) {

			it.advance();
			cnt += it.value().size();
		}

		System.out.println("Nach dem Leverage halbieren gibt es noch " + cnt
				+ " Kanten(multi)");

		for (TShortObjectIterator<TShortIntHashMap> it = hm.iterator(); it
				.hasNext();) {
			it.advance();
			for (TShortIntIterator it2 = it.value().iterator(); it2.hasNext();) {
				it2.advance();
				if (hm.get(it2.key()).contains(it.key()) == false)
					it2.remove();
			}

		}
		// Anzahl den Kanten zählen
		cnt = 0;
		for (TShortObjectIterator<TShortIntHashMap> it = hm.iterator(); it
				.hasNext();) {
			it.advance();
			cnt += it.value().size();
//			if (it.value().size() == 0)
//				System.out.println(it.key());
		}

		System.out.println("Nach dem sysmetrie bleibt noch " + cnt + " Kanten");

		// 做成一个adjazenzmatrix(mbs),可传出，也可保存。
		TShortObjectHashMap<MyBitSet> adjM = new TShortObjectHashMap<MyBitSet>();
		short[] folge = hm.keys();
		Arrays.sort(folge);
		for (short e1 : folge) {
			if (hm.get(e1).size() > 0) {
				short[] friendsListe = hm.get(e1).keys();
				MyBitSet friends = new MyBitSet();
				for (short e2 : friendsListe)
					friends.set(e2);
				adjM.put(e1, friends);
			}
		}
		
		
		// 将adjM写入Leverage文件
		try{
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			folge = adjM.keys();
			Arrays.sort(folge);
			for(short e1 : folge){
				MyBitSet friends = adjM.get(e1);
				for(int i = friends.nextSetBit(0); i>=0; i=friends.nextSetBit(i+1)){
					if(i>e1)
						break;
					bw.write(e1+";"+i+"\n");
				}
			}
			
			bw.close();
			bw = null;
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Der halbLeverage.csv hat "+FktCollection.numberOfLines(outputFile)+" Kanten");
		

			

		//保存 adjM
		try {
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(Pfad + "adjM_filmfilm.ser"));
			os.writeObject(adjM);

			os.close();
			os = null;

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
// //		 Hier zähle ich die Film Degree in Film-Film Graph
//
//		cnt = 0;
//
//		ArrayList<short[]> filmfilmDegree = new ArrayList<short[]>();
//
//		for (TShortObjectIterator<MyBitSet> it = adjM.iterator(); it.hasNext();) {
//			it.advance();
//			cnt += it.value().cardinality();
//			short[] filmDegree = { it.key(), (short) it.value().cardinality() };
//			filmfilmDegree.add(filmDegree);
//
//		}
//
//		System.out.println("adjM hat " + cnt + " Kanten");
//
//		Collections.sort(filmfilmDegree, new Comparator<short[]>() {
//			@Override
//			public int compare(short[] a, short[] b) {
//				if (b[1] > a[1])
//					return 1;
//				return b[1] == a[1] ? 0 : -1;
//			}
//		});
//		
//		cnt =0;
//		try{
//			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad+"HalbLeveragefilmDegree.txt"));
//			
//			for(short[] e : filmfilmDegree){
//				bw.write(e[0]+","+e[1]+"\n");
//				cnt += e[1];
//			}
//			
//			System.out.println("Gesamte Degree ist: "+cnt);
//			bw.close();
//			bw = null;
//			
//			
//		}catch (IOException e) {
//			e.printStackTrace();
//		}

	}
	
	public static void selectSValue(String inputFile, String outputFile, int threshold){
		
		try{
			
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			String line = br.readLine();
			
			StringTokenizer st;
			
			while(line != null){
				st = new StringTokenizer(line, ";");
				int svalue = Integer.parseInt(st.nextToken())/10000;
				
				if(svalue < threshold){
					System.out.println(svalue);
					line = br.readLine();
					continue;
				}
				String film1 = st.nextToken();
				String film2 = st.nextToken();
				bw.write(film1+";"+film2+"\n");
				System.out.println(film1+";"+film2);
				
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
	 * @param args
	 */
	public static void main(String[] args) {
		Pfad_doc = "/home/adrian/Conny/Netflix/Diplom1/";
		Pfad = "/home/adrian/Conny/Netflix/Diplom1/ClCo/svalue12/";
//		halbLeverage(2, Pfad, Leverage_withoutoneCSV,Pfad+"halbLeverage.csv");
		selectSValue(Pfad_doc+"SValue.csv", Pfad+"svalue12.csv", 12);
		
		
	}

}
