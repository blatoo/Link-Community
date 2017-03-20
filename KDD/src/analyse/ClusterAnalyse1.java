package analyse;

import static info.DataPfad.*;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.iterator.TShortObjectIterator;
import gnu.trove.list.array.TShortArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TShortHashSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

import util.BasisFkt;
import util.ComparatorReverseArrayInt;
import util.Edge;
import util.FktCollection;
import util.MyBitSet;

import static info.DataPfad.*;

public class ClusterAnalyse1 {

	/**
	 * Read the film Degree(how many users has rated it.)
	 * 
	 * @return
	 */
	public static TShortArrayList filmDegreeLesen() {
		// Read the FilmDegree
		TShortArrayList tar = new TShortArrayList();
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(filmDegreeCSV));

			String line = br.readLine();

			StringTokenizer st;

			while (line != null) {
				st = new StringTokenizer(line, ",");
				st.nextToken();
				tar.add(Short.parseShort(st.nextToken()));
				line = br.readLine();
			}

			br.close();
			br = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return tar;
	}

	/**
	 * Lesen alle Serien in eine TShortObjectHashMap<TShortHashSet> Mache alle
	 * Filme, deren Degree < 1 sind, weg. Lösche alle Kluster, deren size < 2
	 * sind.
	 */
	public static TShortObjectHashMap<TShortHashSet> serienLesen() {
		// Read the FilmDegree
		TShortArrayList filmDegree = filmDegreeLesen();

		// Read the movie_titlesSeasons and put them in a TShortObjectHashMap.
		TShortObjectHashMap<TShortHashSet> serien = new TShortObjectHashMap<TShortHashSet>();

		short serienNummer = 0;
		int countFilme = 0;

		try {

			BufferedReader br = new BufferedReader(new FileReader(
					movieTitlesSeason));

			String line = br.readLine();

			serien.put(serienNummer, new TShortHashSet());

			StringTokenizer st;

			while (line != null) {
				if (line.trim().length() == 0) {
					serienNummer++;
					serien.put(serienNummer, new TShortHashSet());
					line = br.readLine();
				}

				if (line.startsWith("!"))
					line = br.readLine();

				countFilme++;
				st = new StringTokenizer(line, "#");
				st.nextToken();
				short filmID = (short) (Short.parseShort(st.nextToken()) - 1);
				if (filmDegree.get(filmID) < 2) {
					// System.out.println(filmID+":"+line);
					line = br.readLine();
					continue;
				}

				serien.get(serienNummer).add(filmID);

				line = br.readLine();
			}

			System.out.println("SerienNummer = 0 ~ " + serienNummer);
			System.out.println("Anzahl Seriens = " + serien.size());
			System.out.println("Anzahl Filme = " + countFilme);

			br.close();
			br = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

		// delete the cluster, which size < 2

		for (TShortObjectIterator<TShortHashSet> it = serien.iterator(); it
				.hasNext();) {
			it.advance();
			if (it.value().size() < 2) {
				it.remove();
			}

		}

		System.out.println("Anzahl Seriens nach dem Schneiden = "
				+ serien.size());

		return serien;

	}

	/**
	 * 测算多少Seriens在一个cluster中，也就是说没被分开。
	 */
	public static void analyse1() {

		TShortObjectHashMap<TShortHashSet> serien = serienLesen();

		TIntObjectHashMap<TShortHashSet> filmeimKluster = (TIntObjectHashMap<TShortHashSet>) FktCollection
				.serObjectRead_wrap(Pfad + "FilmeimKluster.ser");

		TShortObjectHashMap<TIntHashSet> serieninKlustern = new TShortObjectHashMap<TIntHashSet>();

		// 测试哪个Serien是没被分割的，将其写入serieninKlustern

		for (TShortObjectIterator<TShortHashSet> it = serien.iterator(); it
				.hasNext();) {

			it.advance();
			serieninKlustern.put(it.key(), new TIntHashSet());
			for (TIntObjectIterator<TShortHashSet> it2 = filmeimKluster
					.iterator(); it2.hasNext();) {
				it2.advance();
				if (it2.value().containsAll(it.value())) {
					serieninKlustern.get(it.key()).add(it2.key());
				}
			}

		}

		String[][] movieTitles = BasisFkt.movieTitlesRead(movieTitlesTXT,
				numberOfFilms);
		short[] serienNummer = serien.keys();

		// 写出被分割的Serien, 并将被分割的Serien写入一个Object,并保存。

		TShortObjectHashMap<TShortHashSet> serienSeparat = new TShortObjectHashMap<TShortHashSet>();

		try {

			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad
					+ "Serien_separat.txt"));

			Arrays.sort(serienNummer);
			int cnt = 0;
			for (short e : serienNummer) {
				if (serieninKlustern.get(e).size() == 0) {
					cnt++;
					serienSeparat.put(e, serien.get(e));
					TShortIterator it = serien.get(e).iterator();
					short filmNummer = it.next();
					String MovieSeason = movieTitles[filmNummer][1];
					// System.out.println("test: "+MovieSeason +
					// ","+filmNummer);
					MovieSeason = (MovieSeason.substring(0,
							MovieSeason.indexOf(": Season"))).trim();
					bw.write(MovieSeason + ":\n");
					bw.write(serien.get(e).toString() + "\n");
				}

			}
			System.out.println("Insgesamt " + cnt
					+ " Kluster, die separiert sind.");

			FktCollection.serObjectWrite_wrap(Pfad + "Serien_separat.ser",
					serienSeparat);

			bw.close();
			bw = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 测算被分割的Serien，被clustern最大包含量。
	 */
	public static void analyse2() {
		TShortObjectHashMap<MyBitSet> adjM = (TShortObjectHashMap<MyBitSet>) FktCollection
				.serObjectRead_wrap(Pfad + "adjM_filmfilm.ser");
		TObjectIntHashMap<Edge> hm_filmezuKante = (TObjectIntHashMap<Edge>) FktCollection
				.serObjectRead_wrap(Pfad + "filmezuKante.ser");
		TShortObjectHashMap<TShortHashSet> serienSeparat = (TShortObjectHashMap<TShortHashSet>) FktCollection
				.serObjectRead_wrap(Pfad + "Serien_separat.ser");
		TIntObjectHashMap<TShortHashSet> filmeinKluster = (TIntObjectHashMap<TShortHashSet>) FktCollection
				.serObjectRead_wrap(Pfad + "FilmeimKluster.ser");
		int[] kanten_cl = (int[]) FktCollection.serObjectRead_wrap(Pfad
				+ "KantenzuKluster.ser");
		String[][] movieTitles = BasisFkt.movieTitlesRead(movieTitlesTXT,
				numberOfFilms);

		// Für jeder Serien
		for (TShortObjectIterator<TShortHashSet> it = serienSeparat.iterator(); it
				.hasNext();) {
			it.advance();

			// if(it.value().size()<3)
			// continue;

			short[] seasons = it.value().toArray();
			TIntHashSet inKluster = new TIntHashSet();
			// berechne wie viele Kluster hat der Serien teilgenommen(für jeder
			// Seasons in diesem Serien)
			for (short e : seasons) {
				if (!adjM.contains(e)) {
					continue;
				}

				MyBitSet friends = adjM.get(e);
				for (int i = friends.nextSetBit(0); i >= 0; i = friends
						.nextSetBit(i + 1)) {
					int Kante = hm_filmezuKante.get(new Edge(e, i));
					if (kanten_cl[Kante] == -1) {
						inKluster.add(Kante);
					} else {
						inKluster.add(kanten_cl[Kante]);
					}
				}
			}

			// Rechne die max inclusive Kluster.
			TIntObjectHashMap<TShortHashSet> kluster_Elemente = new TIntObjectHashMap<TShortHashSet>();

			for (TIntIterator it_inKluster = inKluster.iterator(); it_inKluster
					.hasNext();) {
				kluster_Elemente.put(it_inKluster.next(), new TShortHashSet());
			}

			for (TIntIterator it2 = inKluster.iterator(); it2.hasNext();) {
				int kluster = it2.next();
				// System.out.print(kluster+",");
				TShortHashSet hs = filmeinKluster.get(kluster);
				for (short e1 : seasons) {
					// System.out.print(e1+":");
					// System.out.println(hs.toString());
					if (hs.contains(e1)) {
						kluster_Elemente.get(kluster).add(e1);
					}
				}
			}

			System.out.println("this Serien has " + it.value().size()
					+ " seasons : " + it.value().toString());

			for (TIntObjectIterator<TShortHashSet> it_klusterElemente = kluster_Elemente
					.iterator(); it_klusterElemente.hasNext();) {
				it_klusterElemente.advance();
				System.out.print("size: " + it_klusterElemente.value().size()
						+ ", {");
				for (TShortIterator it_filmId = it_klusterElemente.value()
						.iterator(); it_filmId.hasNext();) {
					int filmId = it_filmId.next();
					System.out.print(filmId + "," + movieTitles[filmId][1]
							+ "|");
				}
				System.out.println("}");
				// System.out.println(it_klusterElemente.value().toString());

			}
			System.out.println();

		}

	}

	/**
	 * analyse the density of the Cluster which maximal incluse the seriens.
	 */
	public static void analyse3() {
		TShortObjectHashMap<TShortHashSet> serien = serienLesen();

		String[][] movietitles = BasisFkt.movieTitlesRead(movieTitlesTXT,
				numberOfFilms);

		TShortObjectHashMap<MyBitSet> adjM = (TShortObjectHashMap<MyBitSet>) FktCollection
				.serObjectRead_wrap(Pfad + "adjM_filmfilm.ser");
		TObjectIntHashMap<Edge> hm_filmezuKante = (TObjectIntHashMap<Edge>) FktCollection
				.serObjectRead_wrap(Pfad + "filmezuKante.ser");
		TIntObjectHashMap<TShortHashSet> filmeinKluster = (TIntObjectHashMap<TShortHashSet>) FktCollection
				.serObjectRead_wrap(Pfad + "FilmeimKluster.ser");
		TIntObjectHashMap<TIntHashSet> hm = (TIntObjectHashMap<TIntHashSet>) FktCollection
				.serObjectRead_wrap(Pfad + "hm.ser");
		int[] kanten_cl = (int[]) FktCollection.serObjectRead_wrap(Pfad
				+ "KantenzuKluster.ser");

		for (TShortObjectIterator<TShortHashSet> it_serien = serien.iterator(); it_serien
				.hasNext();) {
			// take a serien
			it_serien.advance();
			int anzahlSeasons = it_serien.value().size();

			// Creat a clusterGroup which include all the cluster that the
			// seasons take apart in.
			TIntHashSet clusterGroup = new TIntHashSet();

			// take a season and find all the cluster which it has take apart
			// in.
			for (TShortIterator it_serienMember = it_serien.value().iterator(); it_serienMember
					.hasNext();) {

				short member = it_serienMember.next();
				// find all the "member"'s neighbor with adjM, and all the
				// correspond edges.
				if (!adjM.contains(member)) {
					// System.out.println(movietitles[member]);
					continue;

				}
				MyBitSet neighbor = adjM.get(member);

				// System.out.print(movietitles[member]+"'s neighbor has "+neighbor.cardinality()+" Friends:\n");

				for (int i = neighbor.nextSetBit(0); i >= 0; i = neighbor
						.nextSetBit(i + 1)) {
					int edgeId = hm_filmezuKante.get(new Edge(member, i));
					int clusterId = kanten_cl[edgeId];
					if (clusterId == -1) {
						clusterGroup.add(edgeId);

					} else {
						clusterGroup.add(clusterId);
					}
				}

			}

			// System.out.println("this serien is included in "
			// + clusterGroup.size() + " clusters");

			// find the maximal inclusionscluster.

			short[] seasons = it_serien.value().toArray();
			ArrayList<int[]> ar_cluster_inclusionAnzahl = new ArrayList<int[]>();

			for (TIntIterator it_clusterGroup = clusterGroup.iterator(); it_clusterGroup
					.hasNext();) {
				int cnt = 0;
				int clusterId = it_clusterGroup.next();
				for (short e : seasons) {
					if (filmeinKluster.get(clusterId).contains(e)) {
						cnt++;
					}
				}

				ar_cluster_inclusionAnzahl.add(new int[] { clusterId, cnt });
			}

			// order the clusters by included number of seasons
			Collections.sort(ar_cluster_inclusionAnzahl,
					new Comparator<int[]>() {
						@Override
						public int compare(int[] a, int[] b) {
							if (b[1] > a[1])
								return 1;
							return b[1] == a[1] ? 0 : -1;
						}
					});

			// retain the max included clusters
			if (ar_cluster_inclusionAnzahl.size() == 0) {
				continue;
			}

			int maxNumber = ar_cluster_inclusionAnzahl.get(0)[1];

			if (maxNumber == anzahlSeasons) {
				System.out.println("***Achtung***");
			}
			System.out.println("maxNumber = " + maxNumber + " This serien has "
					+ anzahlSeasons + " seasons");

			ArrayList<int[]> ar_cluster_inclusionAnzahl2 = new ArrayList<int[]>();

			for (int i = 0; i < ar_cluster_inclusionAnzahl.size(); i++) {
				if (ar_cluster_inclusionAnzahl.get(i)[1] == maxNumber) {
					ar_cluster_inclusionAnzahl2.add(ar_cluster_inclusionAnzahl
							.get(i));
				} else {
					break;
				}

			}

			ar_cluster_inclusionAnzahl = null;

			// calculate the density of the Clusters

			for (int i = 0; i < ar_cluster_inclusionAnzahl2.size(); i++) {

				int clusterId = ar_cluster_inclusionAnzahl2.get(i)[0];
				// int seasonsInTheCluster =
				// ar_cluster_inclusionAnzahl2.get(i)[0];

				if (maxNumber == 2 && filmeinKluster.get(clusterId).size() == 2) {

					System.out.println(ar_cluster_inclusionAnzahl2.get(i)[0]
							+ "," + ar_cluster_inclusionAnzahl2.get(i)[1]
							+ ", PERFECT! density = 0");
					continue;
				}

				double density = BasisFkt.densityCal(
						ar_cluster_inclusionAnzahl2.get(i)[0], filmeinKluster,
						hm);
				System.out.println(ar_cluster_inclusionAnzahl2.get(i)[0] + ","
						+ ar_cluster_inclusionAnzahl2.get(i)[1]
						+ ", density = " + density);

			}

			System.out.println();

		}

	}

	/**
	 * Caculate the isolated Vertices: (number of all the vertices - isolated
	 * Vertices) / number of all the Vertices
	 * 
	 * @param inputFile
	 */
	public static void communityCoverage(String Pfad) {
		TIntObjectHashMap<TShortHashSet> moviesInCluster = (TIntObjectHashMap<TShortHashSet>) FktCollection
				.serObjectRead_wrap(Pfad + "FilmeimKluster.ser");
		MyBitSet mbs = new MyBitSet();
		mbs.set(0, 17770);
		for (TIntObjectIterator<TShortHashSet> it = moviesInCluster.iterator(); it
				.hasNext();) {
			it.advance();
			for (TShortIterator it2 = it.value().iterator(); it2.hasNext();) {
				mbs.clear(it2.next());

			}

		}

		System.out.println("There are " + mbs.cardinality()
				+ " isolated movies");
		System.out.println("Community coverage is "
				+ (double) (numberOfFilms - mbs.cardinality()) / 17770);

	}

	public static void overlapQuality(String Pfad) {

		int[] kanten_cl = (int[]) FktCollection.serObjectRead_wrap(Pfad
				+ "KantenzuKluster.ser");
		TShortObjectHashMap<MyBitSet> adjM = (TShortObjectHashMap<MyBitSet>) FktCollection
				.serObjectRead_wrap(Pfad + "adjM_filmfilm.ser");
		TObjectIntHashMap<Edge> hm_filmezuKante = (TObjectIntHashMap<Edge>) FktCollection
				.serObjectRead_wrap(Pfad + "filmezuKante.ser");

		HashMap<Integer, Integer> film_anzahlTeilgennomenCls = new HashMap<Integer, Integer>();

		for (TShortObjectIterator<MyBitSet> it = adjM.iterator(); it.hasNext();) {
			it.advance();
			int film1 = it.key();
			MyBitSet friends = it.value();
			HashSet<Integer> MengederClusters = new HashSet<Integer>();
			for (int i = friends.nextSetBit(0); i >= 0; i = friends
					.nextSetBit(i + 1)) {
				Edge edge = new Edge(film1, i);
				int kanteId = hm_filmezuKante.get(edge);
				int cluserId = kanten_cl[kanteId];
				if (cluserId == -1) {
					cluserId = kanteId;
				}
				MengederClusters.add(cluserId);
			}

			film_anzahlTeilgennomenCls.put(film1, MengederClusters.size());
		}

		// show filmId : Anzahl der teilgenommenen Clusters, and give the result
		int sum = 0;
		for (Map.Entry<Integer, Integer> entry : film_anzahlTeilgennomenCls
				.entrySet()) {
			// System.out.println(entry.getKey() + "," + entry.getValue());
			sum += entry.getValue();
		}
		double overlap = (double) sum / numberOfFilms;
		System.out.println("Overlap Quality is: " + overlap);

	}

	/**
	 * Cluster Result sorted by local density. Output: Pfad+"ClusterPropose.txt"
	 * 
	 * @param Pfad
	 */
	public static void ClusterPropose(String Pfad) {
		TIntObjectHashMap<TIntHashSet> hm = (TIntObjectHashMap<TIntHashSet>) FktCollection
				.serObjectRead_wrap(Pfad + "hm.ser");
		TIntObjectHashMap<TShortHashSet> filmeimCluster = (TIntObjectHashMap<TShortHashSet>) FktCollection
				.serObjectRead_wrap(Pfad + "FilmeimKluster.ser");

		// String[] MovieTitles = FktCollection.movieTitlesRead();
		String[][] mt = BasisFkt.movieTitlesRead(movieTitlesTXT, numberOfFilms);

		Number[][] clusterId_Density = new Number[filmeimCluster.size()][2];
		int cnt = 0;
		for (TIntObjectIterator<TShortHashSet> it = filmeimCluster.iterator(); it
				.hasNext();) {
			it.advance();
			int clusterId = it.key();

			if (hm.get(clusterId) == null) {
				clusterId_Density[cnt][0] = clusterId;
				clusterId_Density[cnt][1] = 0;
				cnt++;
				continue;
			}

			int nc = filmeimCluster.get(clusterId).size();
			int mc = hm.get(clusterId).size();

			int zaehler = mc - nc + 1;
			int nenner = nc * (nc - 1) / 2 - nc + 1;

			double density = (double) zaehler / nenner;
			clusterId_Density[cnt][0] = clusterId;
			clusterId_Density[cnt][1] = density;
			cnt++;
		}

		Arrays.sort(clusterId_Density, new Comparator<Number[]>() {
			@Override
			public int compare(Number[] a, Number[] b) {
				if (a[1].doubleValue() > b[1].doubleValue())
					return -1;
				return a[1].doubleValue() == b[1].doubleValue() ? 0 : 1;
			}
		});

		// System.out.println(clusterId_Density.length);
		// System.out.println(clusterId_Density[clusterId_Density.length-1][0]+","+clusterId_Density[clusterId_Density.length-1][1].doubleValue());
		// System.out.println(clusterId_Density[0][0]+","+clusterId_Density[0][1].doubleValue());

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad
					+ "ClusterPropose.txt"));

			bw.write("Insgesamt " + filmeimCluster.size() + " clusters\n\n");

			for (int i = 0; i < clusterId_Density.length; i++) {
				int clusterId = clusterId_Density[i][0].intValue();
				double density = clusterId_Density[i][1].doubleValue();
				TShortHashSet hs = filmeimCluster.get(clusterId);

				bw.write("clusterId: " + clusterId + "; density: " + density
						+ "; Anzahl der Filme: " + hs.size() + "\n");

				for (TShortIterator it = hs.iterator(); it.hasNext();) {
					int filmId = it.next();
					bw.write("\t(" + (filmId + 1) + "," + mt[filmId][0] + ","
							+ mt[filmId][1] + ")\n");
				}

			}

			bw.close();
			bw = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		cnt = 0;
		int[][] clusterId_Size = new int[filmeimCluster.size()][2];
		for (TIntObjectIterator<TShortHashSet> it = filmeimCluster.iterator(); it
				.hasNext();) {
			it.advance();
			clusterId_Size[cnt][0] = it.key();
			clusterId_Size[cnt][1] = it.value().size();
			cnt++;
		}

		Arrays.sort(clusterId_Size, new Comparator<int[]>() {
			@Override
			public int compare(int[] a, int[] b) {
				if (a[1] > b[1])
					return 1;
				return a[1] < b[1] ? -1 : 0;

			}
		});

		for (int i = 0; i < clusterId_Size.length - 1; i++) {
			for (int j = i + 1; j < clusterId_Size.length; j++) {
				if (filmeimCluster.get(clusterId_Size[j][0]).containsAll(
						filmeimCluster.get(clusterId_Size[i][0]))) {
					filmeimCluster.remove(clusterId_Size[i][0]);
					break;
				}
			}

		}

		cnt = 0;
		Number[][] clusterId_Density_new = new Number[filmeimCluster.size()][2];
		for (Number[] e : clusterId_Density) {
			if (filmeimCluster.get(e[0].intValue()) == null) {
				continue;
			}
			clusterId_Density_new[cnt] = e;
			cnt++;

		}

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad
					+ "ClusterPropose_simple.txt"));

			bw.write("Insgesamt " + filmeimCluster.size() + " clusters\n\n");

			for (int i = 0; i < clusterId_Density_new.length; i++) {
				int clusterId = clusterId_Density_new[i][0].intValue();
				double density = clusterId_Density_new[i][1].doubleValue();
				TShortHashSet hs = filmeimCluster.get(clusterId);

				bw.write("clusterId: " + clusterId + "; density: " + density
						+ "; Anzahl der Filme: " + hs.size() + "\n");

				for (TShortIterator it = hs.iterator(); it.hasNext();) {
					int filmId = it.next();
					bw.write("\t(" + (filmId + 1) + "," + mt[filmId][0] + ","
							+ mt[filmId][1] + ")\n");
				}

			}

			bw.close();
			bw = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void ClusterPropose_int(String Pfad) {

		TIntObjectHashMap<TIntHashSet> hm = (TIntObjectHashMap<TIntHashSet>) FktCollection
				.serObjectRead_wrap(Pfad + "hm.ser");
		TIntObjectHashMap<TIntHashSet> filmeimCluster = (TIntObjectHashMap<TIntHashSet>) FktCollection
				.serObjectRead_wrap(Pfad + "FilmeimKluster.ser");

		// String[] MovieTitles = FktCollection.movieTitlesRead();
		String[][] mt = BasisFkt.movieTitlesRead(movieTitlesTXT, numberOfFilms);

		Number[][] clusterId_Density = new Number[filmeimCluster.size()][2];
		int cnt = 0;
		for (TIntObjectIterator<TIntHashSet> it = filmeimCluster.iterator(); it
				.hasNext();) {
			it.advance();
			int clusterId = it.key();

			if (hm.get(clusterId) == null) {
				clusterId_Density[cnt][0] = clusterId;
				clusterId_Density[cnt][1] = 0;
				cnt++;
				continue;
			}

			int nc = filmeimCluster.get(clusterId).size();
			int mc = hm.get(clusterId).size();

			int zaehler = mc - nc + 1;
			int nenner = nc * (nc - 1) / 2 - nc + 1;

			double density = (double) zaehler / nenner;
			clusterId_Density[cnt][0] = clusterId;
			clusterId_Density[cnt][1] = density;
			cnt++;
		}

		Arrays.sort(clusterId_Density, new Comparator<Number[]>() {
			@Override
			public int compare(Number[] a, Number[] b) {
				if (a[1].doubleValue() > b[1].doubleValue())
					return -1;
				return a[1].doubleValue() == b[1].doubleValue() ? 0 : 1;
			}
		});

		// System.out.println(clusterId_Density.length);
		// System.out.println(clusterId_Density[clusterId_Density.length-1][0]+","+clusterId_Density[clusterId_Density.length-1][1].doubleValue());
		// System.out.println(clusterId_Density[0][0]+","+clusterId_Density[0][1].doubleValue());

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(Pfad
					+ "ClusterPropose.txt"));

			bw.write("Insgesamt " + filmeimCluster.size() + " clusters\n\n");

			for (int i = 0; i < clusterId_Density.length; i++) {
				int clusterId = clusterId_Density[i][0].intValue();
				double density = clusterId_Density[i][1].doubleValue();
				TIntHashSet hs = filmeimCluster.get(clusterId);

				bw.write("clusterId: " + clusterId + "; density: " + density
						+ "; Anzahl der Filme: " + hs.size() + "\n");

				for (TIntIterator it = hs.iterator(); it.hasNext();) {
					int filmId = it.next();
					bw.write("\t(" + (filmId + 1) + "," + mt[filmId][0] + ","
							+ mt[filmId][1] + ")\n");
				}

			}

			bw.close();
			bw = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void clusterProposeSec(String Pfad1, String Pfad2,
			String movieTitlesTXT,String inputFileIsolatedCluster, String outputFile) {
		TIntObjectHashMap<TIntHashSet> l2_KlusternimKluster = (TIntObjectHashMap<TIntHashSet>) FktCollection
				.serObjectRead_wrap(Pfad1 + "FilmeimKluster.ser");
		TIntObjectHashMap<TShortHashSet> l1_filmeimKluster = (TIntObjectHashMap<TShortHashSet>) FktCollection
				.serObjectRead_wrap(Pfad2 + "FilmeimKluster.ser");
//		System.out.println(l2_KlusternimKluster.size());
//		System.out.println(l1_filmeimKluster.size());
		
		TIntObjectHashMap<TShortHashSet> fink = new TIntObjectHashMap<TShortHashSet>();

		for (TIntObjectIterator<TIntHashSet> it = l2_KlusternimKluster.iterator(); it
				.hasNext();) {
			it.advance();
			TShortHashSet hs = new TShortHashSet();
			for (TIntIterator it2 = it.value().iterator(); it2.hasNext();) {
				int clusterId = it2.next();
				hs.addAll(l1_filmeimKluster.get(clusterId));

			}
			fink.put(it.key(), hs);

		}	

		String[][] movieTitles = BasisFkt.movieTitlesRead(movieTitlesTXT,
				numberOfFilms);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

			int[][] sizecompare = new int[fink.size()][2];
			int cnt = 0;
			for (TIntObjectIterator<TShortHashSet> it = fink.iterator(); it
					.hasNext();) {

				it.advance();
				sizecompare[cnt][0] = it.key();
				sizecompare[cnt][1] = it.value().size();
				cnt++;

			}

			Arrays.sort(sizecompare, new ComparatorReverseArrayInt(1));

			bw.write(sizecompare.length + " clusters in total\n");
			bw.newLine();
			for (int i = 0; i < sizecompare.length; i++) {
				bw.write("clusterId = " + sizecompare[i][0]
						+ ", numberOfFilms = " + sizecompare[i][1]+"\n");
				for (TShortIterator it = fink.get(sizecompare[i][0]).iterator(); it
						.hasNext();) {
					short filmId = it.next();
					bw.write("\t" + movieTitles[filmId][0] + ","
							+ movieTitles[filmId][1] + "\n");
				}

			}
			
			System.out.println(sizecompare.length);


			
			// write the isolated Clusterpropose
			
			File file = new File(inputFileIsolatedCluster);
			if(file.exists() && file.length() != 0){
				BufferedReader br = new BufferedReader(new FileReader(inputFileIsolatedCluster));
				String[] line = br.readLine().split(",");
				int[] isoClusters = new int[line.length];
				for(int i = 0; i<line.length; i++){
					isoClusters[i] = Integer.parseInt(line[i]);
				}
				String outputIso = outputFile.substring(0, outputFile.length()-4)+"_isoClusters.txt";
				bw = new BufferedWriter(new FileWriter(outputIso));
				bw.write("Here are "+isoClusters.length+" isolated Clusters\n");
				bw.newLine();
				for(int i = 0; i<isoClusters.length; i++){
					bw.write("clusterId = "+isoClusters[i]+"; numberOfFilms = "+l1_filmeimKluster.get(isoClusters[i]).size()+"\n");
					for(TShortIterator it = l1_filmeimKluster.get(isoClusters[i]).iterator(); it.hasNext();){
						int filmId = it.next();
						bw.write("\t"+movieTitles[filmId][0]+","+movieTitles[filmId][1]+"\n");
						
					}
					
					
				}
				
				
				
			}
			
			bw.close();
			bw = null;

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 analyse1();
		 analyse2();
		 analyse3();
		 communityCoverage(Pfad);
		 overlapQuality(Pfad);
		 ClusterPropose(Pfad);

//		String Pfad1 = "/home/adrian/Conny/Netflix/Diplom1/output/nextLevel/";
//		String Pfad2 = "/home/adrian/Conny/Netflix/Diplom1/output/";
//		String inputFileIsolatedCluster = Pfad2+"isolatedCluster.txt";
//		clusterProposeSec(Pfad1, Pfad2, movieTitlesTXT, inputFileIsolatedCluster,Pfad1
//				+ "ClusterPropose2.txt");
//		System.out.println(inputFileIsolatedCluster.substring(0,inputFileIsolatedCluster.length()-4)+"_isoClusters.txt");

	}

}
