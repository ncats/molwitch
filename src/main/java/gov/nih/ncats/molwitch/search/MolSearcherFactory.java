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

package gov.nih.ncats.molwitch.search;

import gov.nih.ncats.common.util.CachedSupplier;
import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.spi.MolSearcherImplFactory;

import java.util.*;

public class MolSearcherFactory {

    private static CachedSupplier<List<MolSearcherImplFactory>> searchers = CachedSupplier.runOnce(()->{
        List<MolSearcherImplFactory> list = new ArrayList<>();
        for(MolSearcherImplFactory w : ServiceLoader.load(MolSearcherImplFactory.class)){
            list.add(w);
        }
        return list;
    });
    private MolSearcherFactory(){
        //can not instantiate
    }
    public static Optional<MolSearcher> create(String smartsPattern){
        Iterator<MolSearcherImplFactory> iterator = searchers.get().iterator();
        if(iterator.hasNext()){
            return Optional.ofNullable(iterator.next().create(smartsPattern));
        }
        return Optional.empty();
    }

    public static Optional<MolSearcher> create(Chemical query){

        Iterator<MolSearcherImplFactory> iterator = searchers.get().iterator();
        if(iterator.hasNext()){
            return Optional.ofNullable(iterator.next().create(query));
        }
        return Optional.empty();
    }
}
