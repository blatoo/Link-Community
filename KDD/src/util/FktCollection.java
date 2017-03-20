package util;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FktCollection {

	/**
	 * Free memory
	 * 
	 * @param st
	 * @return
	 * @throws Exception
	 */
	public static long mem(String st) {
		System.out.println("Free useless memory... sleep 500...");
		Runtime r = Runtime.getRuntime();
		System.gc();
		System.gc();
		System.gc();
		
		try{
			Thread.sleep(500);
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.gc();
		System.gc();
		System.gc();
		long l = (r.totalMemory() - r.freeMemory());

		if (st.length() > 0) {
			System.out.println(st + ": " + l);
		}
		return l;
	}
	
	public static void serObjectWrite_wrap(String outPutFile, Object obj) {
		try {
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(outPutFile));
			os.writeObject(obj);
			os.close();
			os = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static Object serObjectRead_wrap(String serFile){
		
		Object ob = new Object();
		
		try{
			ObjectInputStream is = new ObjectInputStream(new FileInputStream(serFile));
			ob = is.readObject();
			is.close();
			is = null;

		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		return ob;
		
	}
	
	/**http://leepoint.net/notes-java/data/arrays/arrays-ex-reverse.html
	 * Reverse an Array, it is oft use for sort an array des
	 * @param b
	 */
	public static void reverse(float[] b) {
		  
		   for (int left=0, right=b.length-1; left<right; left++, right--) {
			    // exchange the first and last
			    float temp = b[left]; b[left]  = b[right]; b[right] = temp;
			}

		}//endmethod reverse
	
	/**
	 * count the number of lines of the inputFile (txt Format)
	 * @param inputFile
	 * @return
	 */
	public static int numberOfLines(String inputFile) {
		int lineNumber = 0;
		try {
			LineNumberReader lr = new LineNumberReader(
					new FileReader(inputFile));
			while (lr.readLine() != null)
				;
			lineNumber = lr.getLineNumber();

			lr.close();
			lr = null;
		} catch (IOException e) {
			e.toString();
		}

		return lineNumber;
	}
	
	
	public static TIntHashSet THSetCross(TIntHashSet a, TIntHashSet b){
		TIntHashSet cross = new TIntHashSet();
		
		if(a.size() < b.size()){
			for(TIntIterator it = a.iterator(); it.hasNext();){
				int friend = it.next();
				if(b.contains(friend)){
					cross.add(friend);
				}
				
			}
			
			
		}else {
			for(TIntIterator it = b.iterator(); it.hasNext();){
				int friend = it.next();
				if(a.contains(friend)){
					cross.add(friend);
				}
			}
		}
		return cross;
	}
	
	public static boolean THSetInterset(TIntHashSet a, TIntHashSet b){
		if(a.size() == 0 || b.size() == 0){
			return false;
		}
		
		if(a.size() < b.size()){
			for(TIntIterator it = a.iterator(); it.hasNext();){
				if(b.contains(it.next())){
					return true;
				}
			}
		}else {
			for(TIntIterator it = b.iterator(); it.hasNext();){
				if(a.contains(it.next())){
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	
	public static void main(String[] args) {
		
	}

}
