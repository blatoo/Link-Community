package unweighted;

import static util.FktCollection.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import util.BasisFkt;
import util.FktCollection;
import util.MyBitSet;
import gnu.trove.iterator.TShortObjectIterator;
import gnu.trove.map.hash.TShortObjectHashMap;

public class Similarity {

	public static void simisort(String Pfad){
		
		try{
			ArrayList<float[]> ar = new ArrayList<float[]>();
			File file = new File(Pfad+"similarity.binary");
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(Pfad+"has_Fr.binary")));
			DataInputStream dis2 = new DataInputStream(new BufferedInputStream(new FileInputStream(Pfad+"similarity.binary")));
			int numberOfPaars = (int)file.length()/4;
			for(int i = 0; i<numberOfPaars; i++){
				float[] simi = new float[3];
				float readSimi = dis2.readFloat();
				if(readSimi >= 0.1){

					simi[0] = dis.readShort();
					simi[1] = dis.readShort();
					simi[2] = readSimi;
					ar.add(simi);
					
//					if(simi[0] == 5367)
//						System.out.println(simi[0]+","+simi[1]+","+simi[2]);
				}else {
					dis.skip(4);
				}
				
			}
			
			Collections.sort(ar, new Comparator<float[]>() {
				@Override
				public int compare(float[] a, float[] b) {
					if (b[2] > a[2])
						return 1;
					return b[2] == a[2] ? 0 : -1;
				}
			});
			
			System.out.println("There are "+ar.size()+ " Similaries >= 0.1");
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad+"similarity_sorted_90.txt"));
			for(int i = 0; i<ar.size(); i++){
				bw.write(ar.get(i)[2]+";"+(short)ar.get(i)[0]+";"+(short)ar.get(i)[1]+"\n");
//				if(ar.get(i)[0]==5367){
//					System.out.println(ar.get(i)[2]+";"+(short)ar.get(i)[0]+";"+(short)ar.get(i)[1]);
//				}
				
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

	public static void similarityCal(String Pfad, String inputFile) {

		// 读取Film-Film adjazenzMatrix, 这个matrix不包含自己本身

//		TShortObjectHashMap<MyBitSet> adjM = (TShortObjectHashMap<MyBitSet>) serObjectRead_wrap(Pfad
//				+ "adjM_filmfilm.ser");
		TShortObjectHashMap<MyBitSet> adjM = BasisFkt.getAdjMatrix(inputFile);
		FktCollection.serObjectWrite_wrap(Pfad+"adjM_filmfilm.ser", adjM);
		// 开始算similarity了

		// 首先写has_Fr.binary

		short[] Friends = adjM.keys();
		Arrays.sort(Friends);

		try {
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(Pfad
							+ "has_Fr.binary")));

			for (int i = 0; i < Friends.length; i++) {
				for (int j = i + 1; j < Friends.length; j++) {
					if (adjM.get(Friends[i]).intersects(adjM.get(Friends[j])) == true) {
						dos.writeShort(Friends[i]);
						dos.writeShort(Friends[j]);
//						System.out.println(Friends[i]+","+Friends[j]);
						
					}
				}
			}

			dos.close();
			dos = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

		// 加工这个所读取的adjazenzMatrix, 即加上本身，这是算similarity的要求。

		TShortObjectIterator<MyBitSet> it = adjM.iterator();

		while (it.hasNext()) {
			it.advance();
			it.value().set(it.key());
		}

		
		// 读取has_Fr.binary, 以此计算similarity
		
		try{
			File file = new File(Pfad + "has_Fr.binary");
			DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Pfad+"similarity.binary")));
			int paar = (int)(file.length()/4);
			
			short film1 = 0, film2 = 0;
			double similarity = 0;
			int cnt = -1;
			long t1 = System.currentTimeMillis();
			for(int i = 0; i<paar; i++){
				cnt++;
				film1 = is.readShort();
				film2 = is.readShort();
				similarity = (double)(adjM.get(film1).myand(adjM.get(film2))).cardinality() / (adjM.get(film1).myor(adjM.get(film2)).cardinality());
				os.writeFloat((float)similarity);
				
				if(cnt % 1000000 == 1){
					long t2 = (System.currentTimeMillis()-t1)/1000;
					System.out.println(cnt+"-te: film1 = "+film1+", film2 = "+film2+", similarity = "+(float)similarity+" : It uses "+t2+" seconds");
				}
				
			}
			
			os.close();
			os = null;
			
			is.close();
			is = null;
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		simisort(Pfad);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String Pfad = "/home/adrian/Conny/Netflix/Diplom1/output/";

		similarityCal(Pfad, Pfad+"halbLeverage.csv");
		
	}

}
