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

package gov.nih.ncats.molwitch.inchi;

import gov.nih.ncats.common.util.MapUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of {@link InchiKeyBag}
 * that stores inchis in a series of HashSets,
 * while this should work for small datasets it's not recommended
 * for larger datasets.
 *
 * @see BinaryInchiKeyBag
 */
public class DefaultInchiKeyBag implements InchiKeyBag{
    private Set<String> fullInchiKey;
    private Set<String> insensitiveInchiKeys;

    public static DefaultInchiKeyBag fromCollection(Collection<String> collection){
        return new DefaultInchiKeyBag(collection);
    }

    private DefaultInchiKeyBag(Collection<String> collection){
        fullInchiKey = new HashSet<>(MapUtil.computeMinHashMapSizeWithoutRehashing(collection.size()));
        insensitiveInchiKeys = new HashSet<>(collection.size());
        for(String s : collection){
            if(InchiUtil.isValidInchiKey(s)) {
                fullInchiKey.add(s);
                insensitiveInchiKeys.add(InchiUtil.getConnectivityLayer(s));
            }
        }
    }
    @Override
    public InchiKeySearchResult find(String inchiKey) {
        if(contains(inchiKey)){
            return new InchiKeySearchResult(inchiKey, InchiKeySearchResultType.EXACT);
        }
        if(containsInsensitive(inchiKey)){
            return new InchiKeySearchResult(inchiKey, InchiKeySearchResultType.STEREO_INSENSITIVE);
        }
        return new InchiKeySearchResult(inchiKey, InchiKeySearchResultType.NO_MATCH);
    }

    @Override
    public boolean contains(String inchiKey) {
        return fullInchiKey.contains(inchiKey);
    }

    @Override
    public boolean containsInsensitive(String inchiKey) {
        return insensitiveInchiKeys.contains(InchiUtil.getConnectivityLayer(inchiKey));
    }
}
