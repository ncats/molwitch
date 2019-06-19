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


import gov.nih.ncats.common.util.CachedSupplier;
import gov.nih.ncats.witch.spi.ChemicalImplFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class to get the spi implementation
 * that is used.
 * 
 * @author katzelda
 *
 */
public final class ImplUtil {

	private static ThreadLocal<ServiceLoader<ChemicalImplFactory>> implLoaders = ThreadLocal.withInitial( ()-> ServiceLoader.load(ChemicalImplFactory.class));
	private static CachedSupplier<ChemicalImplFactory> defaultFactory = CachedSupplier.of(()->{
		
		Iterator<ChemicalImplFactory> iter = implLoaders.get().iterator();
		if(!iter.hasNext()) {
			return null;
		}
		ChemicalImplFactory defaultImp = iter.next();
		while(iter.hasNext()) {
			ChemicalImplFactory f = iter.next();
			if(f.isDefault()) {
				defaultImp = f;
			}
		}
		return defaultImp;
	});
	
	private static Map<String, ChemicalImplFactory> formatMap =new ConcurrentHashMap<>();
	
	private ImplUtil(){
		//can not instantiate
	}
	public static ChemicalImplFactory getChemicalImplFactory() {
		return defaultFactory.get();
	}
	public static ChemicalImplFactory getChemicalImplFactory(String format) {
		
		return formatMap.computeIfAbsent(format, k->{
			for(ChemicalImplFactory factory : implLoaders.get()) {
				if(factory.supports(k)) {
					return factory;
				}
			}
			return null;
		});
	}
}
