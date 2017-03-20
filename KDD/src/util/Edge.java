package util;

import java.io.Serializable;

public class Edge implements Serializable, Cloneable {

	private short x = 0;
	private short y = 0;



	public Edge(short a1, short a2) {
		if (a1 > a2) {
			this.x = a1;
			this.y = a2;
		} else {
			this.x = a2;
			this.y = a1;
		}
	}
	
	public Edge(int a1, int a2) {
		if (a1 > a2) {
			this.x = (short)a1;
			this.y = (short)a2;
		} else {
			this.x = (short)a2;
			this.y = (short)a1;
		}
	}
	

	public short getX() {
		return x;
	}

	public short getY() {
		return y;
	}
	public void setX(short x) {
		this.x = x;
	}
	public void setX(int x) {
		this.x = (short)x;
	}

	public void setY(short y) {
		this.y = y;
	}
	public void setY(int y) {
		this.y = (short)y;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + this.x + "," + this.y + ")";
	}

	public void println(){
		System.out.println("(" + this.x + "," + this.y + ")");
	}
	

}
