/*
 * NCATS-MOLWITCH
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

package gov.nih.ncats.molwitch.internal.source;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import gov.nih.ncats.molwitch.ChemicalSource;

public class LazySource implements ChemicalSource{

	private final Type type;
	private final Supplier<String> supplier;
	private String data;
	private final Map<String,String> properties = new HashMap<>();
	
	public LazySource(Type type, Supplier<String> supplier) {
		this.type = Objects.requireNonNull(type);
		this.supplier = Objects.requireNonNull(supplier);
	}

	@Override
	public Type getType() {
		return type;
	}
	

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public synchronized String getData() {
		if(data !=null){
			return data;
		}
		String temp= supplier.get();
		if(temp ==null){
			throw new NullPointerException("could not get source data");
		}
		data=temp;
		return data;
	}

}
