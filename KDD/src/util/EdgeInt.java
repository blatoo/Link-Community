package util;

import java.io.Serializable;

public class EdgeInt implements Serializable, Cloneable {

	private int x = 0;
	private int y = 0;

	public EdgeInt(int a1, int a2) {
		if (a1 > a2) {
			this.x = a1;
			this.y = a2;
		} else {
			this.x = a2;
			this.y = a1;
		}
	}
	

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = (int)y;
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
		EdgeInt other = (EdgeInt) obj;
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
