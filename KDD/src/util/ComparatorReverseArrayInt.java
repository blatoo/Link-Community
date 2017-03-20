package util;

import java.util.Arrays;
import java.util.Comparator;

public class ComparatorReverseArrayInt implements Comparator<int[]>{
	
	int col;
	
	public ComparatorReverseArrayInt(int col){
		this.col = col;
	}
	
	@Override
	public int compare(int[] a, int[] b){
		
		if(a[col] > b[col])
			return -1;
		return a[col] < b[col]? 1 : 0;
	}
	




	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
