package unweighted;

//import static info.DataPfad.*;

public class MainProgramm2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String Pfad = "/home/adrian/Conny/KDD/testShort2/";
		
		String inputFile = Pfad+"short_test.txt";

		Similarity.similarityCal(Pfad, inputFile);
		
		Cluster group = new Cluster(inputFile, Pfad);
		
		group.cluster();
		
	}

}
