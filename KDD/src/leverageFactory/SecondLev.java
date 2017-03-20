package leverageFactory;

import static info.DataPfad.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import util.FktCollection;
import util.MyBitSet;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TShortHashSet;

public class SecondLev {

	public static void writeLeverage(String intputFileSER, String outputFile, String outputFile2) {
		TIntObjectHashMap<TShortHashSet> filmeinKluster = (TIntObjectHashMap<TShortHashSet>) FktCollection
				.serObjectRead_wrap(intputFileSER);
		TIntObjectHashMap<MyBitSet> filmeinKluster_mbs = new TIntObjectHashMap<MyBitSet>();

		for (TIntObjectIterator<TShortHashSet> it = filmeinKluster.iterator(); it
				.hasNext();) {
			it.advance();
			MyBitSet filme = new MyBitSet();
			for (TShortIterator it_filme = it.value().iterator(); it_filme
					.hasNext();) {
				filme.set(it_filme.next());
			}
			filmeinKluster_mbs.put(it.key(), filme);
		}

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(outputFile2));

			int[] clusterIds = filmeinKluster_mbs.keys();
			int cnt = 0;
			for (int i = 0; i < clusterIds.length - 1; i++) {
				boolean iso = true;
				for (int j = i+1; j < clusterIds.length; j++) {
					if (filmeinKluster_mbs.get(clusterIds[i]).intersects(
							filmeinKluster_mbs.get(clusterIds[j]))) {
						bw.write(filmeinKluster_mbs.get(clusterIds[i])
								.myand(filmeinKluster_mbs.get(clusterIds[j]))
								.cardinality()+";"+clusterIds[i]+";"+clusterIds[j]+"\n");
						iso = false;
					}

				}
				if(iso == true){
					System.out.println(clusterIds[i]);
					bw2.write(clusterIds[i]+",");
					cnt++;
				}

			}
			
			System.out.println("insgesamt "+cnt+" isolated clusters.");
			
			bw2.close();
			bw2 = null;
			
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
		writeLeverage(Pfad + "FilmeimKluster.ser", Pfad + "SecondLeverage.csv", Pfad+"isolatedCluster.txt");

	}

}
