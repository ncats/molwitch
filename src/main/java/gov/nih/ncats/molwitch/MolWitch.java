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

package gov.nih.ncats.molwitch;

import java.util.ServiceLoader;

import gov.nih.ncats.common.util.CachedSupplier;
import gov.nih.ncats.molwitch.spi.WitchModule;

public class MolWitch {
	private static CachedSupplier<WitchModule> loadedModule = CachedSupplier.runOnce(()->{
		WitchModule mod=null;
		for(WitchModule m : ServiceLoader.load(WitchModule.class)){
			mod= m;
		}
		if(mod ==null){
			throw new IllegalStateException("could not find a module!!!! ");
		}
		return mod;
	});

	

	public static String getModuleName(){
		return loadedModule.get().getName();
	}

}
