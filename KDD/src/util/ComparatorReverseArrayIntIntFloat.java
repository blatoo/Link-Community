package util;

import java.util.Comparator;

public class ComparatorReverseArrayIntIntFloat implements Comparator<Number[]> {
	int col;
	public ComparatorReverseArrayIntIntFloat(int col){
		this.col = col;
	}
	@Override
	public int compare(Number[] a, Number[] b){
		if(a[col].floatValue() > b[col].floatValue()){
			return -1;
		}
		return a[col].floatValue() < b[col].floatValue() ? 1 : 0;
		
	}

}
