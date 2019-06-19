/*
 * NCATS-WITCH
 *
 * Copyright 2019 NIH/NCATS
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gov.nih.ncats.witch;

import java.util.Comparator;
import java.util.OptionalDouble;


public abstract class AtomCoordinates implements Comparable<AtomCoordinates>{


	public static final Comparator<AtomCoordinates> DEFAULT_COMPARATOR = Comparator.comparingDouble(AtomCoordinates::getY)
			.thenComparing(Comparator.comparingDouble(AtomCoordinates::getX));

	private final double x,y;

	private AtomCoordinates(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double[] xy() {
		return new double[] {x,y};
	}
	public OptionalDouble getZ() {
		return OptionalDouble.empty();
	}

	public boolean is2D() {
		return true;
	}

	public boolean is3D() {
		return false;
	}
	
	public static AtomCoordinates valueOf(double x, double y, double z) {
		return new AtomCoordinates3D(x, y, z);
	}
	
	public static AtomCoordinates valueOf(double x, double y) {
		return new AtomCoordinates2D(x, y);
	}
	
	/**
     * Returns the angle between this point and that point.
     * @return the angle in radians (between –&pi; and &pi;) between this point and that point (0 if equal)
     */
    public double angleTo(AtomCoordinates that) {
        double dx = that.getX() - getX() ;
        double dy = that.getY() - getY();
        return Math.atan2(dy, dx);
    }

    /**
     * Returns the square of the Euclidean distance between this point and that point.
     * @param that the other point
     * @return the square of the Euclidean distance between this point and that point
     */
    public double distanceSquaredTo(AtomCoordinates that) {
        double dx = this.getX() - that.getX();
        double dy = this.getY() - that.getY();
        return dx*dx + dy*dy;
    }

    /**
     * Compares two points by y-coordinate, breaking ties by x-coordinate.
     * Formally, the invoking point (x0, y0) is less than the argument point (x1, y1)
     * if and only if either {@code y0 < y1} or if {@code y0 == y1} and {@code x0 < x1}.
     *
     * @param  that the other point
     * @return the value {@code 0} if this string is equal to the argument
     *         string (precisely when {@code equals()} returns {@code true});
     *         a negative integer if this point is less than the argument
     *         point; and a positive integer if this point is greater than the
     *         argument point
     */
    @Override
    public int compareTo(AtomCoordinates that) {
    		return  DEFAULT_COMPARATOR.compare(this, that);
      
    }

    /**
     * Returns true if a→b→c is a counterclockwise turn.
     * @param a first point
     * @param b second point
     * @param c third point
     * @return { -1, 0, +1 } if a→b→c is a { clockwise, collinear; counterclocwise } turn.
     */
    public static int ccw(AtomCoordinates a, AtomCoordinates b, AtomCoordinates c) {
        double area2 = (b.getX()-a.getX())*(c.getY()-a.getY()) - (b.getY()-a.getY())*(c.getX()-a.getX());
        if      (area2 < 0) return -1;
        else if (area2 > 0) return +1;
        else                return  0;
    }
    
    
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AtomCoordinates)) {
			return false;
		}
		AtomCoordinates other = (AtomCoordinates) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		return true;
	}
	
	



	@Override
	public String toString() {
		if(getZ().isPresent()){
			return "AtomCoordinates [x=" + x + ", y=" + y +", z=" + getZ().getAsDouble() + "]";
		}
		return "AtomCoordinates [x=" + x + ", y=" + y + "]";
	}





	private static final class AtomCoordinates2D extends AtomCoordinates{


	    	public AtomCoordinates2D(double x, double y) {
	    		super(x,y);
	    	
	    }
	    	@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (!(obj instanceof AtomCoordinates2D)) {
				return false;
			}
			return true;
	    	}
    }

	private static final class AtomCoordinates3D extends AtomCoordinates {

		private final double z;

		public AtomCoordinates3D(double x, double y, double z) {
			super(x, y);
			this.z = z;
		}

		@Override
		public OptionalDouble getZ() {
			return OptionalDouble.of(z);
		}

		@Override
		public boolean is2D() {
			return false;
		}

		@Override
		public boolean is3D() {
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			long temp;
			temp = Double.doubleToLongBits(z);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (!(obj instanceof AtomCoordinates3D)) {
				return false;
			}
			AtomCoordinates3D other = (AtomCoordinates3D) obj;
			if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z)) {
				return false;
			}
			return true;
		}

	}

}
