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

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValueWithUncertainty implements Comparable<ValueWithUncertainty>{
	private final BigDecimal value;
	private final BigDecimal uncertainty;
	private final BigDecimal lowerBounds, upperBounds;
	
	private static final Pattern pattern = Pattern.compile("(\\d*\\.\\d+)\\((\\d+)\\)?");
	private static final Pattern integerPattern = Pattern.compile("(\\d+)");

	public static ValueWithUncertainty parse(String s) {
		Matcher m = pattern.matcher(s);
		if(!m.find()) {
			m = integerPattern.matcher(s);
			if(m.find()){
				try {
					return new ValueWithUncertainty(new BigDecimal(s), BigDecimal.ZERO);
				}catch(NumberFormatException e){
					throw new IllegalArgumentException("could not parse " + s,e);
				}
			}
			throw new IllegalArgumentException("'"+ s +"' does not match valid pattern " + pattern.pattern());
		}
		String value = m.group(1);
		if(m.group(2) ==null) {
			return new ValueWithUncertainty(new BigDecimal(value), BigDecimal.ZERO);
		}
		int numOfDecimals = value.length() - value.indexOf('.');
		StringBuilder padd = new StringBuilder();
		if(numOfDecimals >0) {
			padd.append('.');
		}
		for(int i=0; i< numOfDecimals; i++) {
			padd.append('0');
		}
		padd.append(m.group(2));
		return new ValueWithUncertainty(new BigDecimal(value), new BigDecimal(padd.toString()));
	}
	
	public ValueWithUncertainty(BigDecimal value, BigDecimal uncertainty) {
		this.value = Objects.requireNonNull(value);
		this.uncertainty = Objects.requireNonNull(uncertainty);
		
		this.lowerBounds = value.subtract(uncertainty);
		this.upperBounds = value.add(uncertainty);
	}
	public BigDecimal getValue() {
		return value;
	}
	public BigDecimal getUncertainty() {
		return uncertainty;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uncertainty == null) ? 0 : uncertainty.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ValueWithUncertainty other = (ValueWithUncertainty) obj;
		if (uncertainty == null) {
			if (other.uncertainty != null)
				return false;
		} else if (!uncertainty.equals(other.uncertainty))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public BigDecimal getLowerBounds() {
		return lowerBounds;
	}

	public BigDecimal getUpperBounds() {
		return upperBounds;
	}

	public boolean meetsCriteria(BigDecimal value) {
		return lowerBounds.compareTo(value) <=0 && upperBounds.compareTo(value) >=0;
	}
	public boolean meetsCriteria(double value) {
		return meetsCriteria(BigDecimal.valueOf(value));
	}

	@Override
	public int compareTo(ValueWithUncertainty o) {
		if(lowerBounds.compareTo(o.upperBounds) > 0) {
			return 1;
		}
		if(upperBounds.compareTo(o.lowerBounds) < 0) {
			return -1;
		}
		//if we are here we overlap
		int lowCmp = lowerBounds.compareTo(o.lowerBounds);
		if( lowCmp<0) {
			return -1;
		}
		return upperBounds.compareTo(o.upperBounds);
		
	}
	
	
}
