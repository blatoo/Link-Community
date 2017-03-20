package unweighted;

import util.Edge;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

import util.FktCollection;
import util.MyBitSet;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TShortHashSet;

/**
 * schneller Densityrechen
 * 
 * @author conny
 * 
 */
public class Cluster {

	public String inputFile;
	public int numberOfEdges;
	public String Pfad;
	
	public Cluster(String inputFile, String Pfad){
		this.inputFile = inputFile;
		this.numberOfEdges = FktCollection.numberOfLines(inputFile);
		this.Pfad = Pfad;
	}

	public short[][] kanten_Filme() {

		short[][] kantezuFilme = new short[numberOfEdges][2];
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line = br.readLine();

			StringTokenizer st;
			int cnt = -1;

			while (line != null) {
				cnt++;
				st = new StringTokenizer(line, ";");
				// st.nextToken();
				kantezuFilme[cnt][0] = Short.parseShort(st.nextToken());
				kantezuFilme[cnt][1] = Short.parseShort(st.nextToken());

				line = br.readLine();
			}

			br.close();
			br = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Kanten zu Filme liste ist fertig gestellt!");
		return kantezuFilme;
	}

	public double partitionDensity(short[][] kantenzuFilme,
			TIntObjectHashMap<TIntHashSet> hm) {

		TIntObjectIterator<TIntHashSet> it = hm.iterator();
		double density = 0;
		while (it.hasNext()) {
			it.advance();
			double mc = it.value().size();
			int[] memberFilme = it.value().toArray();
			TShortHashSet ths = new TShortHashSet();
			for (int e : memberFilme) {
				ths.addAll(kantenzuFilme[e]);
			}
			double ncminuseins = ths.size() - 1;
			density += mc * (mc - ncminuseins)
					/ ((ncminuseins - 1) * ncminuseins);
		}

		density = density * 2 / numberOfEdges;

		return density;

	}

	public static void FilmeimKluster(short[][] kantenzuFilme,
			TIntObjectHashMap<TIntHashSet> hm, int[] kanten_cl, String Pfad) {
		TIntObjectHashMap<TShortHashSet> hm_FilmeimKluster = new TIntObjectHashMap<TShortHashSet>();
		for (TIntObjectIterator<TIntHashSet> it = hm.iterator(); it.hasNext();) {
			it.advance();
			TShortHashSet filme = new TShortHashSet();
			for (TIntIterator it2 = it.value().iterator(); it2.hasNext();) {
				filme.addAll(kantenzuFilme[it2.next()]);
			}
			hm_FilmeimKluster.put(it.key(), filme);
		}

		System.out.println("Teilkluster:" + hm_FilmeimKluster.size());

		int cnt = 0;

		for (int i = 0; i < kanten_cl.length; i++) {
			if (kanten_cl[i] == -1) {
				cnt++;
				hm_FilmeimKluster.put(i, new TShortHashSet(kantenzuFilme[i]));
			}
		}
		System.out.println("ungeklustered Kanten sind: " + cnt);
		System.out.println("Alle Kluster:" + hm_FilmeimKluster.size());

		FktCollection.serObjectWrite_wrap(Pfad + "FilmeimKluster.ser", hm_FilmeimKluster);

	}

	/**
	 * 修改过后的partition density, 直接用 hm_Filme算，而不用每次计算组内的电影数了。
	 * 
	 * @param hm_Filme
	 * @return
	 */
	public double partitionDensity_easy(
			TIntObjectHashMap<TShortHashSet> hm_Filme,
			TIntObjectHashMap<TIntHashSet> hm) {

		double partitionDensity = 0;

		TIntObjectIterator<TShortHashSet> it = hm_Filme.iterator();
		while (it.hasNext()) {
			it.advance();
			double mc = hm.get(it.key()).size();
			double ncminuseins = it.value().size() - 1;

			partitionDensity += (double) mc * (mc - ncminuseins)
					/ ((ncminuseins - 1) * ncminuseins);

		}

		partitionDensity = (double) partitionDensity * 2 / numberOfEdges;

		return partitionDensity;
	}

	public void reportCluster(TIntObjectHashMap<TIntHashSet> hm,
			int[] kanten_cl, String Pfad) {
		int hm_size = hm.size();
		System.out.println(hm_size);

		int unclustered = 0;
		for (int i = 0; i < numberOfEdges; i++) {
			if (kanten_cl[i] < 0)
				unclustered++;
		}

		System.out.println("unclustered Edges: " + unclustered);
		System.out.println("clustered Edges: " + (numberOfEdges - unclustered));

		int[][] clusters = new int[hm_size][2];

		TIntObjectIterator<TIntHashSet> it = hm.iterator();
		int cnt = -1;
		while (it.hasNext()) {
			it.advance();
			cnt++;
			clusters[cnt][0] = it.key();
			clusters[cnt][1] = it.value().size();
		}

		Arrays.sort(clusters, new Comparator<int[]>() {
			@Override
			public int compare(int[] a, int[] b) {
				if (b[1] > a[1])
					return 1;
				return b[1] == a[1] ? 0 : -1;
			}
		});

		short[][] kanten_FilmeList = kanten_Filme();
		FktCollection.serObjectWrite_wrap(Pfad + "kanten_FilmeList.ser", kanten_FilmeList);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad
					+ "Kluster-Ergebnis.txt"));
			bw.write("Es gibt insgesamt " + hm_size + " clusters\n");

			for (int i = 0; i < hm_size; i++) {
				bw.write(clusters[i][0] + "," + clusters[i][1] + ",");
				TShortHashSet hs = new TShortHashSet();
				TIntIterator tintit = hm.get(clusters[i][0]).iterator();
				while (tintit.hasNext()) {
					int kante = tintit.next();
					hs.add(kanten_FilmeList[kante][0]);
					hs.add(kanten_FilmeList[kante][1]);
				}

				int ncminusone = hs.size() - 1;

				double dc = (double) (clusters[i][1] - ncminusone)
						/ ((hs.size() * ncminusone) / 2 - ncminusone);

				bw.write(hs.size() + "," + dc + "\n");

				short[] member = hs.toArray();
				Arrays.sort(member);
				for (short e : member) {
					bw.write(e + ",");

				}
				bw.newLine();

			}

			bw.close();
			bw = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void clustertomaxdensity(double maxdensitysimi,
			short[][] kantenzuFilme, TObjectIntHashMap<Edge> hm_KN,
			TShortObjectHashMap<MyBitSet> mbs, String Pfad) {

		// initialize array[int]: Um der ClusterNummer von Kanten zu suchen
		int[] kanten_cl = new int[numberOfEdges];
		for (int i = 0; i < numberOfEdges; i++)
			kanten_cl[i] = -1;
		System.out
				.println("Array[int] für Kanten Cluster Nummer ist fertig initializiert!");

		// // Für Klsuterung: TintObjecthashMap<HashSet<int>>
		// TShortObjectHashMap<MyBitSet> mbs = (TShortObjectHashMap<MyBitSet>)
		// FktCollection
		// .serObjectRead_wrap(tshHMser);
		FktCollection.mem("aaa");

		long t1 = System.currentTimeMillis();

		TIntObjectHashMap<TIntHashSet> hm = new TIntObjectHashMap<TIntHashSet>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(Pfad
					+ "similarity_sorted_90.txt"));

			String line = br.readLine();
			StringTokenizer st;
			int cnt = 0;
			double simi = 0;
			short x, y;
			Edge edge1, edge2;
			int e1, e2, cl1, cl2, cl1_size, cl2_size;
			while (line != null) {
				cnt++;
				st = new StringTokenizer(line, ";");
				simi = Double.parseDouble(st.nextToken());
				if (simi < maxdensitysimi)
					break;

				x = Short.parseShort(st.nextToken());
				y = Short.parseShort(st.nextToken());

//				System.out.println(simi + ": " + x + ", " + y);

				MyBitSet cross = mbs.get(x).myand(mbs.get(y));

				for (int i = cross.nextSetBit(0); i >= 0; i = cross
						.nextSetBit(i + 1)) {
					edge1 = new Edge(x, i);
					edge2 = new Edge(y, i);
					e1 = hm_KN.get(edge1);
					e2 = hm_KN.get(edge2);
					cl1 = kanten_cl[e1];
					cl2 = kanten_cl[e2];

					// Hier klustern! Wichtig!
					if (cl1 < 0 && cl2 < 0) {
						TIntHashSet hs = new TIntHashSet(2);
						hs.add(e1);
						hs.add(e2);
						hm.put(e1, hs);
						kanten_cl[e1] = e1;
						kanten_cl[e2] = e1;
					} else if (cl1 >= 0 && cl2 < 0) {
						hm.get(cl1).add(e2);
						kanten_cl[e2] = cl1;
					} else if (cl1 < 0 && cl2 >= 0) {
						hm.get(cl2).add(e1);
						kanten_cl[e1] = cl2;
					} else {
						if (cl1 != cl2) {
							cl1_size = hm.get(cl1).size();
							cl2_size = hm.get(cl2).size();
							if (cl1_size > cl2_size) {
								int[] cl2_member = hm.get(cl2).toArray();
								hm.get(cl1).addAll(cl2_member);
								hm.remove(cl2);
								for (int e : cl2_member)
									kanten_cl[e] = cl1;
							} else {
								int[] cl1_member = hm.get(cl1).toArray();
								hm.get(cl2).addAll(cl1_member);
								hm.remove(cl1);
								for (int e : cl1_member)
									kanten_cl[e] = cl2;
							}
						}
					}

				}

				line = br.readLine();

			}

			long t2 = (System.currentTimeMillis() - t1) / 1000;

			System.out.println("Cluster to MaxDensity uses " + t2 + " seconds");

			FktCollection.serObjectWrite_wrap(Pfad + "hm.ser", hm);

			reportCluster(hm, kanten_cl, Pfad);

			br.close();
			br = null;

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// save the kanten_cl
		FktCollection.serObjectWrite_wrap(Pfad+"KantenzuKluster.ser", kanten_cl);

		FilmeimKluster(kantenzuFilme, hm, kanten_cl, Pfad);

		// reportCluster(hm, kanten_cl);

	}

	public void cluster() {
		// kanten zu Filme List
		short[][] kantenzuFilme = kanten_Filme();

		// initialize array[int]: Um der ClusterNummer von Kanten zu suchen
		int[] kanten_cl = new int[numberOfEdges];
		for (int i = 0; i < numberOfEdges; i++)
			kanten_cl[i] = -1;
		System.out
				.println("Array[int] für Kanten Cluster Nummer ist fertig initializiert!");

		// initialize the KantenNummer list.
		// TObjectIntHashMap: Key-Kanten, Value-KantenNummer
		TObjectIntHashMap<Edge> hm_filmezuKante = new TObjectIntHashMap<Edge>(
				numberOfEdges);
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));

			String line = br.readLine();
			StringTokenizer st;
			int cnt = 0;

			while (line != null) {
				st = new StringTokenizer(line, ";");
				// st.nextToken();
				Edge edge = new Edge(Short.parseShort(st.nextToken()),
						Short.parseShort(st.nextToken()));
				hm_filmezuKante.put(edge, cnt);
				if (cnt % 10000 == 1)
					System.out.println(cnt + edge.toString());
				cnt++;
				line = br.readLine();
			}

			br.close();
			br = null;

			System.out.println("KantenNummerList ist fertig erzeugt!");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// save the filmezuKante.ser [(film1, film2)-> KantenNr.]
		FktCollection.serObjectWrite_wrap(Pfad + "filmezuKante.ser", hm_filmezuKante);

		// Für Klsuterung: TintObjecthashMap<HashSet<int>>
		// 第一种方法用Leveragehalbieren.java做出的serＦile.目前不用这种方法，因为别人可能没有serFile.
		// TShortObjectHashMap<MyBitSet> mbs =
		// (TShortObjectHashMap<MyBitSet>)util.FktCollection.serObjectRead_wrap(tshHMser);

		// 读取txt，生成mbs.
		TShortObjectHashMap<MyBitSet> mbs = new TShortObjectHashMap<MyBitSet>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));

			String line = br.readLine();

			StringTokenizer st;

			while (line != null) {
				st = new StringTokenizer(line, ";");
				short film1 = Short.parseShort(st.nextToken());
				short film2 = Short.parseShort(st.nextToken());
				if (!mbs.containsKey(film1)) {
					mbs.put(film1, new MyBitSet());
				}
				if (!mbs.containsKey(film2)) {
					mbs.put(film2, new MyBitSet());
				}
				mbs.get(film1).set(film2);
				mbs.get(film2).set(film1);

				line = br.readLine();
			}

			br.close();
			br = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

		FktCollection.mem("aaa");

		long t1 = System.currentTimeMillis();

		TIntObjectHashMap<TIntHashSet> hm = new TIntObjectHashMap<TIntHashSet>();
		TIntObjectHashMap<TShortHashSet> hm_Filme = new TIntObjectHashMap<TShortHashSet>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(Pfad
					+ "similarity_sorted_90.txt"));
			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad
					+ "density.txt"));

			String line = br.readLine();
			StringTokenizer st;
			int cnt = 0;
			double simi = 0;
			double simi_old = 0;
			double maxdensity = 0, maxdensitysimi = 0;
			short x, y;
			Edge edge1, edge2;
			int e1, e2, cl1, cl2, cl1_size, cl2_size;
			while (line != null) {
				cnt++;
				st = new StringTokenizer(line, ";");
				simi = Double.parseDouble(st.nextToken());
				// if (simi < 0.7)
				// break;

				if (simi_old != simi) {
					double density = partitionDensity_easy(hm_Filme, hm);
					bw.write(simi_old + "," + density + "\n");

					if (maxdensity < density) {
						maxdensity = density;
						maxdensitysimi = simi_old;
					}

				}

				x = Short.parseShort(st.nextToken());
				y = Short.parseShort(st.nextToken());

//				System.out.println(simi + ": " + x + ", " + y);

				MyBitSet cross = mbs.get(x).myand(mbs.get(y));

				for (int i = cross.nextSetBit(0); i >= 0; i = cross
						.nextSetBit(i + 1)) {
					edge1 = new Edge(x, i);
					edge2 = new Edge(y, i);
					e1 = hm_filmezuKante.get(edge1);
					e2 = hm_filmezuKante.get(edge2);
					cl1 = kanten_cl[e1];
					cl2 = kanten_cl[e2];

					// Hier klustern! Wichtig!
					if (cl1 < 0 && cl2 < 0) {
						TIntHashSet hs = new TIntHashSet(2);
						hs.add(e1);
						hs.add(e2);
						hm.put(e1, hs);
						kanten_cl[e1] = e1;
						kanten_cl[e2] = e1;

						TShortHashSet hs2 = new TShortHashSet();
						hs2.addAll(kantenzuFilme[e1]);
						hs2.addAll(kantenzuFilme[e2]);
						hm_Filme.put(e1, hs2);

					} else if (cl1 >= 0 && cl2 < 0) {
						hm.get(cl1).add(e2);
						kanten_cl[e2] = cl1;
						hm_Filme.get(cl1).addAll(kantenzuFilme[e2]);

					} else if (cl1 < 0 && cl2 >= 0) {
						hm.get(cl2).add(e1);
						kanten_cl[e1] = cl2;
						hm_Filme.get(cl2).addAll(kantenzuFilme[e1]);

					} else {
						if (cl1 != cl2) {
							cl1_size = hm.get(cl1).size();
							cl2_size = hm.get(cl2).size();
							if (cl1_size > cl2_size) {
								int[] cl2_member = hm.get(cl2).toArray();
								hm.get(cl1).addAll(cl2_member);
								hm.remove(cl2);
								for (int e : cl2_member)
									kanten_cl[e] = cl1;

								hm_Filme.get(cl1).addAll(hm_Filme.get(cl2));
								hm_Filme.remove(cl2);

							} else {
								int[] cl1_member = hm.get(cl1).toArray();
								hm.get(cl2).addAll(cl1_member);
								hm.remove(cl1);
								for (int e : cl1_member)
									kanten_cl[e] = cl2;

								hm_Filme.get(cl2).addAll(hm_Filme.get(cl1));
								hm_Filme.remove(cl1);
							}
						}
					}

				}

				simi_old = simi;

				line = br.readLine();

			}

			long t2 = (System.currentTimeMillis() - t1) / 1000;

			System.out.println("It uses " + t2 + " minites");

			hm = null;
			kanten_cl = null;

			bw.write("maxdensity = " + maxdensity
					+ ", max density similarity = " + maxdensitysimi);
			bw.close();
			bw = null;

			br.close();
			br = null;

			clustertomaxdensity(maxdensitysimi, kantenzuFilme, hm_filmezuKante,
					mbs, Pfad);

			System.out.println("maxdensity = " + maxdensity
					+ ", max density similarity = " + maxdensitysimi);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		String Pfad = "/home/adrian/Conny/Netflix/Diplom1/output/";
		Cluster cl = new Cluster(Pfad+"halbLeverage.csv", Pfad);
		cl.cluster();

	}

}
