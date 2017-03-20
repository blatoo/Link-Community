package util;

import static info.DataPfad.*;

public class CompareNumberOfSamples {
	
	public static void diviationtest(String ser1, int numberOfSample1, String ser2, int numberOfSample2){
		
		int[][] cooc1 = (int[][]) FktCollection.serObjectRead_wrap(ser1);
		int[][] cooc2 = (int[][]) FktCollection.serObjectRead_wrap(ser2);
		
		double sigma = 0;
		for(int i = 1; i<numberOfFilms; i++){
			for(int j = 0; j<i; j++){
				double lev1 = (double)cooc1[i][j]/numberOfSample1;
				double lev2 = (double)cooc2[i][j]/numberOfSample2;
				double def = lev1-lev2;
				double abweich = def*def;
				sigma += abweich;
			}
		}
		
		int elements = numberOfFilms*(numberOfFilms-1)/2;
		sigma = sigma/elements;
		sigma = Math.sqrt(sigma);
		System.out.println("sigma = "+sigma);
		
		
	}
	

	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String ser1 = "/home/adrian/Conny/Netflix/Diplom1/Leverage.ser";
		String ser2 = "/home/adrian/Conny/Netflix/calLeverage/leverage5000Sample/Leverage.ser";
		
		diviationtest(ser1, 200, ser2, 5000);

	
	}

}
