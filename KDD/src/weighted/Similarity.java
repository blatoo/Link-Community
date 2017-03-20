package weighted;

import static util.FktCollection.*;
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
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

import util.BasisFkt;
import util.MyBitSet;

import gnu.trove.iterator.TShortFloatIterator;
import gnu.trove.iterator.TShortObjectIterator;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.map.hash.TShortFloatHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;

/*
 * 目前最快的Similarity程序。
 * 还缺生成has_Fr程式！！！
 */
public class Similarity {
	
	String Pfad;
	String inputFile;
	int numberOfFilms;
	int mass;
	
	public Similarity(String inputFile, String Pfad, int numberOfFilms, int mass){
		this.Pfad = Pfad;
		this.inputFile = inputFile;
		this.numberOfFilms = numberOfFilms;
		this.mass = mass;
	}

	public void adjM_HM() {
		TShortObjectHashMap<MyBitSet> adjM = BasisFkt.getAdjMatrix(inputFile);
		
		serObjectWrite_wrap(Pfad+"adjM_filmfilm.ser", adjM);
		
		System.out.println(inputFile);
		
		try{
					
			//Wenn intersect, dann schreibt
			
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Pfad+"has_Fr.binary")));
			short[] Friends = adjM.keys();
			
			Arrays.sort(Friends);
			
			for(int i = 0; i<Friends.length; i++){
				for(int j = i+1; j<Friends.length; j++){
					if(adjM.get(Friends[i]).intersects(adjM.get(Friends[j]))){
					dos.writeShort(Friends[i]);
					dos.writeShort(Friends[j]);
						
					}
					
				}
				
			}
			
			dos.close();
			dos = null;
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		

		
		
	}

	/**
	 * 生成Adjazenzlist 1）读入矩阵 2）计算 aii 3) 把aii填入矩阵
	 * @param mass use Leverage -> mass = numberOfSamples
	 *             use SValue -> mass = 100
	 */
	public TShortObjectHashMap<TShortFloatHashMap> adjList(int mass) {
		TShortObjectHashMap<TShortFloatHashMap> hm = new TShortObjectHashMap<TShortFloatHashMap>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));

			String line = br.readLine();
			StringTokenizer st;
			short s = -1, t = -1;
			float svalue;
			int cnt = -1;
			long t1 = System.currentTimeMillis();
			while (line != null) {
				cnt++;
				st = new StringTokenizer(line, ";");
				svalue = Float.parseFloat(st.nextToken())/mass;
				s = Short.parseShort(st.nextToken());
				t = Short.parseShort(st.nextToken());

				TShortFloatHashMap subhm = hm.get(s);
				if (subhm != null) {
					subhm.put(t, svalue);
				} else {
					hm.put(s, new TShortFloatHashMap(16, (float) 0.75,
							(short) -1, (float) -1.0));
					hm.get(s).put(t, svalue);
				}

				subhm = hm.get(t);
				if (subhm != null) {
					subhm.put(s, svalue);
				} else {
					hm.put(t, new TShortFloatHashMap(16, (float) 0.75,
							(short) -1, (float) -1.0));
					hm.get(t).put(s, svalue);
				}

				if (cnt % 100000 == 1)
					System.out.println(cnt + "te: (" + t + "," + s
							+ "), wert:" + svalue);
				line = br.readLine();
			}
			long t2 = System.currentTimeMillis() - t1;
			System.out.println("it uses " + t2 / 1000 + " seconds");

			br.close();
			br = null;

			// calculate the aii and put it into adjazent List
			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad
					+ "aii.txt"));
			TShortObjectIterator<TShortFloatHashMap> it = hm.iterator();
			while (it.hasNext()) {
				double sum = 0;
				it.advance();
				float[] value = it.value().values();
				for (int i = 0; i < value.length; i++)
					sum += (double) value[i];
				it.value().put(it.key(), (float) (sum / value.length));
			}

			for (int i = 0; i < 17770; i++) {
				if (hm.containsKey((short) i))
					bw.write(i + ":" + hm.get((short) i).get((short) i) + "\n");
			}

			bw.close();
			bw = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(hm.size());

		return hm;
	}

	/**
	 * 计算similarity 计算aih2,写入文件aih2.txt 计算similarity 输出similarity
	 * similarity.binary
	 * 
	 * @param hm
	 *            是从上面的adjList()中获得
	 */
	public void simi(TShortObjectHashMap<TShortFloatHashMap> hm) {
		double[] aih2 = new double[numberOfFilms];

		for (int i = 0; i < numberOfFilms; i++) {
			if (hm.contains((short) i)) {
				TShortFloatHashMap thm = hm.get((short) i);
				short[] nachbarn = thm.keys();
				double sum = 0;
				for (int j = 0; j < nachbarn.length; j++) {
					sum += (double) thm.get(nachbarn[j])
							* (double) thm.get(nachbarn[j]);
				}
				aih2[i] = sum;

			} else {
				aih2[i] = 0;
			}
		}

		// Write the ai² into aih2.txt, so that you can check the value.

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad
					+ "aih2.txt"));

			for (int i = 0; i < numberOfFilms; i++) {
				if (aih2[i] != 0) {
					bw.write(i + "," + aih2[i] + "\n");
				}
			}

			bw.close();
			bw = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Calculate the similarity

		try {
			File file = new File(Pfad + "has_Fr.binary");
			File file_out = new File(Pfad + "similarity.binary");

			long pairs = file.length() / 4;
			short s = -1, t = -1;
			int cnt = -1;
			double similarity = 0;

			DataInputStream in = new DataInputStream(new BufferedInputStream(
					new FileInputStream(file)));
			DataOutputStream out = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(file_out)));
			long t1 = System.currentTimeMillis();
			for (long i = 0; i < pairs; i++) {
				similarity = 0;
				cnt++;
				s = in.readShort();
				t = in.readShort();

				TShortFloatHashMap thms = hm.get(s);
				TShortFloatHashMap thmt = hm.get(t);
				if(thms == null || thmt == null){
					System.out.println("s = "+s+"; t = "+t);
				}
				if (thms.size() < thmt.size()) {

					for (TShortFloatIterator e = thms.iterator(); e.hasNext();) {
						e.advance();
						if (thmt.containsKey(e.key()))
							similarity += (double) thmt.get(e.key())
									* (double) e.value();
					}
				} else {
					for (TShortFloatIterator e = thmt.iterator(); e.hasNext();) {
						e.advance();
						if (thms.containsKey(e.key()))
							similarity += (double) thms.get(e.key())
									* (double) e.value();

					}

				}

				similarity = similarity / (aih2[s] + aih2[t] - similarity);
				out.writeFloat((float) similarity);

				if (cnt % 1000000 == 1) {
					System.out.print(cnt + "-te: s = " + s + ", t = " + t
							+ ", similarity = " + (float) similarity);
					long t2 = System.currentTimeMillis() - t1;
					System.out.println("; it uses " + t2 / 1000 + " seconds");

				}

			}

			out.close();
			out = null;
			in.close();
			in = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sortSimilarity() {
		String has_Fr = Pfad + "has_Fr.binary";
		String similarity = Pfad + "similarity.binary";
		String sorted_similarity_90 = Pfad + "similarity_sorted_90.txt";
		File file_similarity = new File(similarity);
		int numberOfpaar = (int) (file_similarity.length() / 4);

		try {
			DataInputStream in1 = new DataInputStream(new BufferedInputStream(
					new FileInputStream(has_Fr)));
			DataInputStream in2 = new DataInputStream(new BufferedInputStream(
					new FileInputStream(similarity)));
			int cnt = 0;
			float thisSimi = 0;
			for (int i = 0; i < numberOfpaar; i++) {
				thisSimi = in2.readFloat();
				if (thisSimi >= 0.1) {
					cnt++;

				}
			}
			System.out.println("insgesamt " + cnt + " similarity >= 0.1");

			float[][] simi = new float[cnt][3];

			in2 = new DataInputStream(new BufferedInputStream(
					new FileInputStream(similarity)));
			for (int i = 0; i < simi.length; i++) {
				simi[i][0] = in1.readShort();
				simi[i][1] = in1.readShort();
				simi[i][2] = in2.readFloat();
			}

			Arrays.sort(simi, new Comparator<float[]>() {
				@Override
				public int compare(float[] a, float[] b) {
					if (b[2] > a[2])
						return 1;
					return b[2] == a[2] ? 0 : -1;
				}
			});

			in1.close();
			in1 = null;
			in2.close();
			in2 = null;

			System.out.println("simi_max = " + simi[0][2] + " ; (" + simi[0][0]
					+ "," + simi[0][1] + ")");

			BufferedWriter bw = new BufferedWriter(new FileWriter(
					sorted_similarity_90));
			for (int i = 0; i < simi.length; i++) {
				if (simi[i][2] < 0.1)
					break;
				bw.write(simi[i][2] + ";" + (short) simi[i][0] + ";"
						+ (short) simi[i][1] + "\n");

			}

			bw.close();
			bw = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void similarityCal(){
		adjM_HM();
		simi(adjList(5000));
		sortSimilarity();
	}

	public static void main(String[] args) {
		
		String Pfad = "/home/adrian/Conny/Netflix/Diplom1/weighted/leverage/";
		String inputFile = "/home/adrian/Conny/Netflix/Diplom1/Leverage_withoutone.csv";
		int numberOfFilms = 17770;
		
		Similarity simi = new Similarity(inputFile, Pfad, numberOfFilms, 5000);
		simi.similarityCal();
		
		
	}

}
