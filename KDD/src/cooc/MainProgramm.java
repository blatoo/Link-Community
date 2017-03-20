package cooc;

import static info.DataPfad.*;
import leverageFactory.LeverageBearbeiten;
import leverageFactory.ReadLeverage;
import util.FktCollection;
import util.MyBitSet;
import util.BasisFkt;

public class MainProgramm {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int numberOfSampleUsers = 20000;
		int numberOfSample = 5000;
		int startUser = 1;
		
//		BasisFkt.userInfoExtract(data3user, userInfoBin);
//		
//		BasisFkt.writeMovieDegree(userInfoBin, filmDegreeCSV, numberOfSampleUsers, startUser, numberOfFilms);

		int[][] cooc = new int[numberOfFilms][numberOfFilms];

		MyBitSet[] degreeM = BasisFkt
				.degreeMatrix_MBS(Pfad_doc + "userInfo_good_Simple.binary",
						numberOfSampleUsers, startUser);

		long t1 = System.currentTimeMillis();

		CalLeverage.runCooc1(Pfad_doc + "userInfo_good_Simple.binary", cooc,
				numberOfSampleUsers, startUser, numberOfSample);

		long t2 = System.currentTimeMillis();

		System.out.println("It uses " + (t2 - t1) / 1000 + " seconds");

		FktCollection.serObjectWrite_wrap(Pfad_doc + "Leverage.ser", cooc);

		ReadLeverage.writeLeverageCSV(Pfad_doc, numberOfFilms);

		LeverageBearbeiten.levNoOneDegree(leverageCSV, Leverage_withoutoneCSV,
				filmDegreeCSV, numberOfFilms);
		
		LeverageBearbeiten.getSValue(Leverage_withoutoneCSV, filmDegreeCSV, SValueCSV, numberOfFilms);
	}

}
