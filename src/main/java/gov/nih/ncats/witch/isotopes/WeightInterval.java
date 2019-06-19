package gov.nih.ncats.witch.isotopes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeightInterval {

	private final double lower, upper;

	private static final Pattern pattern = Pattern.compile("\\[\\s*(\\d+\\.\\d+)\\s*,\\s*(\\\\d+\\\\.\\\\d+)\\\\s*\\]");
	
	public static WeightInterval parse(String s) {
		Matcher m = pattern.matcher(s);
		if(!m.find()) {
			throw new IllegalArgumentException("'"+s+"' does not match interval pattern");
		}
		return new WeightInterval(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
		
	}
	public WeightInterval(double lower, double upper) {
		this.lower = lower;
		this.upper = upper;
	}
	public double getLower() {
		return lower;
	}
	public double getUpper() {
		return upper;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(lower);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(upper);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		WeightInterval other = (WeightInterval) obj;
		if (Double.doubleToLongBits(lower) != Double.doubleToLongBits(other.lower))
			return false;
		if (Double.doubleToLongBits(upper) != Double.doubleToLongBits(other.upper))
			return false;
		return true;
	}
	
	
}
