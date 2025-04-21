/*
 * NCATS-MOLWITCH
 *
 * Copyright 2025 NIH/NCATS
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

package gov.nih.ncats.molwitch.isotopes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeightInterval {

	private final double lower, upper;

	private static final Pattern pattern = Pattern.compile("\\[\\s*(\\d+\\.\\d+)\\s*,\\s*(\\d+\\.\\d+)\\s*\\]");
	
	public static WeightInterval parse(String s) {
		Matcher m = pattern.matcher(s);
		if(!m.find()) {
			ValueWithUncertainty v = ValueWithUncertainty.parse(s);
			return new WeightInterval(v.getLowerBounds().doubleValue(), v.getUpperBounds().doubleValue());
//			throw new IllegalArgumentException("'"+s+"' does not match interval pattern");
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
