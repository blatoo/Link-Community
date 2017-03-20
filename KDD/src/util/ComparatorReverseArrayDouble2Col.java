package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ComparatorReverseArrayDouble2Col implements Comparator<double[]> {

	int col1;
	int col2;
	public ComparatorReverseArrayDouble2Col(int col1, int col2){
		this.col1 = col1;
		this.col2 = col2;
		
	}
	
	
	@Override
	public int compare(double[] a, double[] b){
	
		if(a[col1] > b[col1]){
			return -1;
		}else if (a[col1] == b[col1]) {
			if(a[col2] > b[col2]){
				return -1;
			}else if (a[col2] < b[col2]) {
				return 1;
			}else{
				return 0;
			}
		}
		
		return 1;
		
	}

//	public static void main(String[] args) {
//
//		double[] abc = new double[]{1, 2, 3};
//		double[] bbc = new double[]{1, 2, 4};
//		
//		ArrayList<double[]> ar = new ArrayList<double[]>();
//		ar.add(abc);
//		ar.add(bbc);
//		ar.add(new double[]{1,3,2});
//		ar.add(new double[]{1,3,5});
//
//		
//		Collections.sort(ar, new ComparatorReverseArrayDouble2Col(1, 2));
//		
//		for(int i = 0; i<ar.size(); i++){
//			System.out.println(ar.get(i)[0]+","+ar.get(i)[1]+","+ar.get(i)[2]);
//			
//		}
//		
//	}

}
