package unweighted_int;


public class MainProgramm {

	public static void main(String[] args) {
		String Pfad_doc = "/home/adrian/Conny/KDD/testsmall/";
		String inputFile = Pfad_doc+"clusterLeverage.csv";
		String Pfad = "/home/adrian/Conny/KDD/testsmall/";
		
		Similarity xs = new Similarity(Pfad, inputFile);
		xs.similarityCal();
		
		Cluster cl = new Cluster(inputFile, Pfad);
		cl.cluster();

		cl.report(Pfad+"Cluster-Ergebnis.txt");
	}

}
