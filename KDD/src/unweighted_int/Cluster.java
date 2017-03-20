package unweighted_int;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
//import gnu.trove.set.hash.TShortHashSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;


import util.ComparatorReverseArrayDouble;
import util.ComparatorReverseArrayDouble2Col;
import util.EdgeInt;
import util.FktCollection;

public class Cluster {

	public String inputFile;
	public int numberOfEdges;
	public String Pfad;
	public String simiSorted90TXT;
	public String densityTXT;

	public Cluster(String inputFile, String Pfad) {
		this.inputFile = inputFile;
		this.numberOfEdges = FktCollection.numberOfLines(inputFile);
		this.Pfad = Pfad;
		this.simiSorted90TXT = Pfad + "similarity_sorted_90.txt";
		this.densityTXT = Pfad+"density.txt";
	}
	

	
	public void list(int[][] kantezuFilme, TObjectIntHashMap<EdgeInt> hm_filmezuKante){
		
		try {

			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line = br.readLine();

			StringTokenizer st = new StringTokenizer(line, ";");

			int cnt = -1;

			if (st.countTokens() == 3) {
				while (line != null) {
					cnt++;
					st = new StringTokenizer(line, ";");
					st.nextToken();
					int film1 = Integer.parseInt(st.nextToken());
					int film2 = Integer.parseInt(st.nextToken());
					kantezuFilme[cnt][0] = film1;
					kantezuFilme[cnt][1] = film2;
					
					EdgeInt edge = new EdgeInt(film1, film2);
					
					hm_filmezuKante.put(edge, cnt);
					
					line = br.readLine();

				}
			} else {
				
				while (line != null) {
					cnt++;
					st = new StringTokenizer(line, ";");
					int film1 = Integer.parseInt(st.nextToken());
					int film2 = Integer.parseInt(st.nextToken());
					kantezuFilme[cnt][0] = film1;
					kantezuFilme[cnt][1] = film2;
					
					EdgeInt edge = new EdgeInt(film1, film2);
					
					hm_filmezuKante.put(edge, cnt);
					
					line = br.readLine();

				}
				
			}

			br.close();
			br = null;

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}

	/**
	 * 修改过后的partition density, 直接用 hm_Filme算，而不用每次计算组内的电影数了。
	 * 
	 * @param hm_Filme
	 * @return
	 */
	public double partitionDensity_easy(
			TIntObjectHashMap<TIntHashSet> hm_Filme,
			TIntObjectHashMap<TIntHashSet> hm) {

		double partitionDensity = 0;

		TIntObjectIterator<TIntHashSet> it = hm_Filme.iterator();
		while (it.hasNext()) {
			it.advance();
			double mc = hm.get(it.key()).size();
			double ncminuseins = it.value().size() - 1;

			partitionDensity +=  mc * (mc - ncminuseins)
					/ ((ncminuseins - 1) * ncminuseins);

		}

		partitionDensity = partitionDensity * 2 / numberOfEdges;

		return partitionDensity;
	}
	
	public void clusterToDensity(double maxdensitysimi, int[][] kantezuFilme, TObjectIntHashMap<EdgeInt> hm_filmezuKante, TIntObjectHashMap<TIntHashSet> adjM, String Pfad){
		
		// kanteId -> clusterId
		int[] kanten_cl = new int[numberOfEdges];
		for(int i = 0; i<numberOfEdges; i++){
			kanten_cl[i] = -1;
		}
		
		TIntObjectHashMap<TIntHashSet> hm = new TIntObjectHashMap<TIntHashSet>();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(simiSorted90TXT));
			
			String line = br.readLine();
			StringTokenizer st;
			double simi = 0;
			int x,y;
			EdgeInt edge1, edge2;
			int e1, e2, cl1, cl2, cl1_size, cl2_size;
			
			long t1 = System.currentTimeMillis();
			while(line != null){
				st = new StringTokenizer(line, ";");
				simi = Double.parseDouble(st.nextToken());
				if(simi < maxdensitysimi)
					break;
				
				x = Integer.parseInt(st.nextToken());
				y = Integer.parseInt(st.nextToken());
				
				TIntHashSet cross = FktCollection.THSetCross(adjM.get(x), adjM.get(y));
				
				for(TIntIterator it = cross.iterator(); it.hasNext();){
					int friend = it.next();
					edge1 = new EdgeInt(x, friend);
					edge2 = new EdgeInt(y, friend);
					
					e1 = hm_filmezuKante.get(edge1);
					e2 = hm_filmezuKante.get(edge2);
					
					cl1 = kanten_cl[e1];
					cl2 = kanten_cl[e2];
					
					// Cluster! Here is very important!
					if(cl1 < 0 && cl2 < 0){
						TIntHashSet hs = new TIntHashSet(2);
						hs.add(e1);
						hs.add(e2);
						hm.put(e1, hs);
						kanten_cl[e1] = e1;
						kanten_cl[e2] = e1;
											
					}else if(cl1 >= 0 && cl2 < 0){
						hm.get(cl1).add(e2);
						kanten_cl[e2] = cl1;
						
					}else if(cl1 < 0 && cl2 >= 0){
						hm.get(cl2).add(e1);
						kanten_cl[e1] = cl2;
						
					}else {
						if(cl1 != cl2){
							cl1_size = hm.get(cl1).size();
							cl2_size = hm.get(cl2).size();
							if(cl1_size > cl2_size){
								
								for(TIntIterator it2 = hm.get(cl2).iterator(); it2.hasNext();){
									kanten_cl[it2.next()] = cl1;
								}
								
								hm.get(cl1).addAll(hm.get(cl2));
								hm.remove(cl2);
								
								
							}else {
							
								for(TIntIterator it2 = hm.get(cl1).iterator(); it2.hasNext();){
									kanten_cl[it2.next()] = cl2;
								}
								
								hm.get(cl2).addAll(hm.get(cl1));
								hm.remove(cl1);
								
							}
							
						}
						
					}
					
				}		
				
				line = br.readLine();
				
			}
		
			long t2 = (System.currentTimeMillis() -t1)/1000;
			
			System.out.println("It takes "+t2+" seconds to cluster to maxDensity");
			
			// put the Movies in Cluster
			
			TIntObjectHashMap<TIntHashSet> hm_FilmeimKluster = new TIntObjectHashMap<TIntHashSet>();
			
			for(TIntObjectIterator<TIntHashSet> it = hm.iterator(); it.hasNext();){
				it.advance();
				TIntHashSet filme = new TIntHashSet();
				for(TIntIterator it2 = it.value().iterator(); it2.hasNext();){
					filme.addAll(kantezuFilme[it2.next()]);
				}
				hm_FilmeimKluster.put(it.key(), filme);
			}
			
			int cnt = 0;
			
			for(int i = 0; i<kanten_cl.length; i++){
				if(kanten_cl[i] == -1){
					cnt++;
					hm_FilmeimKluster.put(i, new TIntHashSet(kantezuFilme[i]));
				}
			}
			
			System.out.println("There are "+cnt+" edges not be clustered.");
			System.out.println("There are "+hm_FilmeimKluster.size()+" clusters");
			
			FktCollection.serObjectWrite_wrap(Pfad+"hm.ser", hm);
			
			FktCollection.serObjectWrite_wrap(Pfad+"FilmeimKluster.ser", hm_FilmeimKluster);
			
			
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		
	}
	
	
	
	public void cluster() {
		
		//kanteId -> film1,film2
		//film1, film2 -> kanteId
		int[][] kantezuFilme = new int[numberOfEdges][2];
		TObjectIntHashMap<EdgeInt> hm_filmezuKante = new TObjectIntHashMap<EdgeInt>(numberOfEdges);
		
		list(kantezuFilme, hm_filmezuKante);
		FktCollection.serObjectWrite_wrap(Pfad+"filmezuKante.ser", hm_filmezuKante);
		
		
		
		//Read adjazentMatrix: filmId -> neighbours
		File file = new File(Pfad+"adjM_clustercluster.ser");

		TIntObjectHashMap<TIntHashSet> adjM;
		
		if(!file.exists()){
			Similarity sm = new Similarity(Pfad, inputFile);
			adjM = sm.adjM();
		}else{
			adjM = (TIntObjectHashMap<TIntHashSet>)FktCollection.serObjectRead_wrap(Pfad+"adjM_clustercluster.ser");
			
		}
		
		
		// kanteId -> clusterId
		int[] kanten_cl = new int[numberOfEdges];
		for(int i = 0; i<numberOfEdges; i++){
			kanten_cl[i] = -1;
			
		}
		
		// begin cluster
		TIntObjectHashMap<TIntHashSet> hm = new TIntObjectHashMap<TIntHashSet>();
		TIntObjectHashMap<TIntHashSet> hm_Filme = new TIntObjectHashMap<TIntHashSet>();
		
		
		try{
			
			long t1 = System.currentTimeMillis();

			
			BufferedReader br = new BufferedReader(new FileReader(simiSorted90TXT));
			BufferedWriter bw = new BufferedWriter(new FileWriter(densityTXT));
			
			String line = br.readLine();
			StringTokenizer st;
			int cnt = 0;
			double simi = 0;
			double simi_old = 0;
			double maxdensity = 0, maxdensitysimi = 0;
			int x, y;
			EdgeInt edge1, edge2;
			int e1, e2, cl1, cl2, cl1_size, cl2_size;
			
			while(line != null){
				cnt++;
				st = new StringTokenizer(line, ";");
				simi = Double.parseDouble(st.nextToken());
				
				if(simi_old != simi){
					double density = partitionDensity_easy(hm_Filme, hm);
					bw.write(simi_old+" "+density+"\n");
					
					if(maxdensity < density){
						maxdensity = density;
						maxdensitysimi = simi_old;
					}
				}
				
				x = Integer.parseInt(st.nextToken());
				y = Integer.parseInt(st.nextToken());
				
				TIntHashSet cross = FktCollection.THSetCross(adjM.get(x), adjM.get(y));
				
				for(TIntIterator it = cross.iterator(); it.hasNext();){
					int friend = it.next();
					edge1 = new EdgeInt(x, friend);
					edge2 = new EdgeInt(y, friend);
					
					e1 = hm_filmezuKante.get(edge1);
					e2 = hm_filmezuKante.get(edge2);
					
					cl1 = kanten_cl[e1];
					cl2 = kanten_cl[e2];
					
					// Cluster! Here is very important!
					if(cl1 < 0 && cl2 < 0){
						TIntHashSet hs = new TIntHashSet(2);
						hs.add(e1);
						hs.add(e2);
						hm.put(e1, hs);
						kanten_cl[e1] = e1;
						kanten_cl[e2] = e1;
						
						TIntHashSet hs2 = new TIntHashSet();
						hs2.addAll(kantezuFilme[e1]);
						hs2.addAll(kantezuFilme[e2]);
						hm_Filme.put(e1, hs2);
						
					}else if(cl1 >= 0 && cl2 < 0){
						hm.get(cl1).add(e2);
						kanten_cl[e2] = cl1;
						hm_Filme.get(cl1).addAll(kantezuFilme[e2]);
						
					}else if(cl1 < 0 && cl2 >= 0){
						hm.get(cl2).add(e1);
						kanten_cl[e1] = cl2;
						hm_Filme.get(cl2).addAll(kantezuFilme[e1]);
						
					}else {
						if(cl1 != cl2){
							cl1_size = hm.get(cl1).size();
							cl2_size = hm.get(cl2).size();
							if(cl1_size > cl2_size){
								
								for(TIntIterator it2 = hm.get(cl2).iterator(); it2.hasNext();){
									kanten_cl[it2.next()] = cl1;
								}
								
								hm.get(cl1).addAll(hm.get(cl2));
								hm.remove(cl2);
								
								hm_Filme.get(cl1).addAll(hm_Filme.get(cl2));
								hm_Filme.remove(cl2);								
								
							}else {
							
								for(TIntIterator it2 = hm.get(cl1).iterator(); it2.hasNext();){
									kanten_cl[it2.next()] = cl2;
								}
								
								hm.get(cl2).addAll(hm.get(cl1));
								hm.remove(cl1);
								
								hm_Filme.get(cl2).addAll(hm_Filme.get(cl1));
								hm_Filme.remove(cl1);
								
							}
							
						}
						
					}
					
				}
				
				simi_old = simi;
				
				line = br.readLine();
			}
			
			long t2 = (System.currentTimeMillis() -t1)/1000;
			
			System.out.println("It takes "+t2+" seconds to cluster");
			
			hm = null;
			kanten_cl = null;
			
			bw.write("#maxdensity = "+maxdensity+", max density similarity = "+maxdensitysimi);
			
			bw.close();
			bw = null;
			
			br.close();
			br = null;
			
			clusterToDensity(maxdensitysimi, kantezuFilme, hm_filmezuKante, adjM, Pfad);
			
		}catch (IOException e) {
			e.printStackTrace();
		}

		
		
		
	}
	
	public void report(String outputFile){
		
		TIntObjectHashMap<TIntHashSet> hm_FilmeimKluster = (TIntObjectHashMap<TIntHashSet>)FktCollection.serObjectRead_wrap(Pfad+"FilmeimKluster.ser");
		TIntObjectHashMap<TIntHashSet> hm = (TIntObjectHashMap<TIntHashSet>)FktCollection.serObjectRead_wrap(Pfad+"hm.ser");
		
		TIntArrayList ar = new TIntArrayList();
		double[][] clusters = new double[hm.size()][3];
		int cnt = 0;
		for(TIntObjectIterator<TIntHashSet> it = hm_FilmeimKluster.iterator(); it.hasNext();){
			it.advance();
			if(hm.contains(it.key())){
				double mc = hm.get(it.key()).size();
				double nc = it.value().size();
				double local = (mc-nc+1) / (nc*(nc-1)/2 - nc+1);
				
				clusters[cnt][0] = it.key();
				clusters[cnt][1] = local;
				clusters[cnt][2] = nc;
				
				cnt++;
				
			}else {
				ar.add(it.key());
			}
		}
		

		
		
		try{

			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			bw.write("There are "+hm_FilmeimKluster.size()+" clusters, "+clusters.length+" of them have more than 3 members, and "+ar.size()+" have just 2 members.\n");

			Arrays.sort(clusters, new ComparatorReverseArrayDouble(2));
			
			bw.write("The biggest cluster is cluster "+(int)clusters[0][0]+", it has "+(int)clusters[0][2] +" members and the density is "+clusters[0][1]+"\n");
			
			Arrays.sort(clusters, new ComparatorReverseArrayDouble2Col(1, 2));
			
			for(int i = 0; i<clusters.length; i++){
				
				bw.write("clusterId = "+(int)clusters[i][0]+", density = "+clusters[i][1] +", number of edges = "+hm.get((int)clusters[i][0]).size()+ ", number of members = "+(int)clusters[i][2]+"\n");
				
				TIntHashSet ts = hm_FilmeimKluster.get((int)clusters[i][0]);
				
				for(TIntIterator it = ts.iterator(); it.hasNext();){
					
					bw.write(it.next()+",");
					
				}
				
				
				bw.newLine();
				
			}
			
			
			for(int i = 0; i<ar.size(); i++){
				bw.write("clusterId = "+ar.get(i)+", density = 0,  number of edges = 1, number of members = 2\n");
				
				TIntHashSet ts = hm_FilmeimKluster.get(ar.get(i));
				for(TIntIterator it = ts.iterator(); it.hasNext();){
					
					bw.write(it.next()+",");
				}
				bw.newLine();
			}
			
			bw.close();
			bw = null;
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		
	}
	

}
