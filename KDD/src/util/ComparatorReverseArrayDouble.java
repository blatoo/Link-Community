package util;

import java.util.Arrays;
import java.util.Comparator;

public class ComparatorReverseArrayDouble implements Comparator<double[]>{
	
	int col;
	
	public ComparatorReverseArrayDouble(int col){
		this.col = col;
	}
	
	@Override
	public int compare(double[] a, double[] b){
		if(a[col] > b[col])
			return -1;
		return a[col] < b[col] ? 1 : 0;
		
	}
	
}
