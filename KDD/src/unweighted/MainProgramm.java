package unweighted;

import static info.DataPfad.*;

public class MainProgramm {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String inputFile = Pfad+"svalue12.csv";

		Similarity.similarityCal(Pfad, inputFile);
		
		Cluster group = new Cluster(inputFile, Pfad);
		
		group.cluster();
		
	}

}
