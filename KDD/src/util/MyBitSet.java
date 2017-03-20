package util;

import java.util.BitSet;

/**
 * @author Conny Gu This Class ist extends from Class BitSet, it has just more
 *         funktions than his Vater.
 * 
 */

public class MyBitSet extends BitSet {
	
	
	public MyBitSet(){
		super(0);
	}
	
	public MyBitSet(int nbits){
		super(nbits);
	}
	
	/**
	 * This myand() funktion will return a BiSet and itself won't be changed. :D
	 * 
	 * @param b
	 * @return MyBitSet
	 */
	public MyBitSet myand(MyBitSet b) {
		MyBitSet a = (MyBitSet)this.clone();
		a.and(b); 
		return a;
	}
	
	public MyBitSet myor(MyBitSet b){
		MyBitSet a = (MyBitSet)this.clone();
		a.or(b);
		return a;
	}
	
	/**
	 * This myandNot() funktion will return a BiSet and itself won't be changed. :D
	 * 
	 * @param b
	 * @return MyBitSet
	 */
	
	public MyBitSet myandNot(MyBitSet b){
		MyBitSet a = (MyBitSet)this.clone();
		a.andNot(b);
		return a;
	}

}
