/*
 * NCATS-MOLWITCH
 *
 * Copyright 2023 NIH/NCATS
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

package gov.nih.ncats.molwitch.internal.source;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import gov.nih.ncats.molwitch.ChemicalSource;

public class StringSource implements ChemicalSource{

	private final String data;
	private final Type type;
	
	private final Map<String,String> properties = new HashMap<>();
	public StringSource(String data, Type type) {
		this(data, type, true);
	}
	public StringSource(String data, Type type, boolean trim) {
		Objects.requireNonNull(data);
		if(trim){
			this.data = data.trim();
		}else{
			this.data = data;
		}
		this.type = Objects.requireNonNull(type);
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getData() {
		return data;
	}

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		
		if(!(obj instanceof ChemicalSource)){
			return false;
		}
		ChemicalSource other = (ChemicalSource) obj;
		return other.getType()==getType() && data.equals(other.getData());
	}

	@Override
	public String toString() {
		return "StringSource [ type=" + type + ",  data=" + data + "]";
	}

}
