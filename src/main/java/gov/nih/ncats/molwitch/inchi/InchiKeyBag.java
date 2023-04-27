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

package gov.nih.ncats.molwitch.inchi;

import java.util.Objects;

/**
 * An object wrapping a collection of inchi keys where you can
 * interrogate whether a given inchi key exists in this bag
 * either exactly (full stereo) or in an inexact non-stereo search.
 */
public interface InchiKeyBag {
    /**
     * Look to see if the given inchi key is contained in this Bag.
     * The implementation should be the same as but perhaps more efficent than
     * <pre>
     * {@code
     * if(contains(inchiKey)){
     *   return new InchiKeySearchResult(inchiKey, InchiKeySearchResultType.EXACT);
     * }
     * if(containsInsensitive(inchiKey)){
     *   return new InchiKeySearchResult(inchiKey, InchiKeySearchResultType.STEREO_INSENSITIVE);
     * }
     * return new InchiKeySearchResult(inchiKey, InchiKeySearchResultType.NO_MATCH);}
     * </pre>
     * @param inchiKey the inchi key to look for; can not be null.
     * @return an {@link InchiKeySearchResult} which will have the passed in key
     * and the ResultType if it's not found or if it's exact or inexact match.
     * @throws NullPointerException if inchiKey is null.
     */
    InchiKeySearchResult find(String inchiKey);

    /**
     * Does this bag contain this exact inchi key.
     * @param inchiKey the inchi key to check.
     * @return {@code true} if the exact inchi is in this bag;
     * {@code false} otherwise.
     */
    boolean contains(String inchiKey);
    /**
     * Does this bag contain this any inchi key with the same
     * connectivity portion not considering any stereo.
     * @param inchiKey the inchi key to check.
     * @return {@code true} if there is a stereo in-exact match is in this bag;
     * {@code false} otherwise.
     */
    boolean containsInsensitive(String inchiKey);

    enum InchiKeySearchResultType {
        EXACT,
        STEREO_INSENSITIVE,
        NO_MATCH

    }

    class InchiKeySearchResult {
        private final String inchiKey;
        private final InchiKeySearchResultType resultType;

        /**
         * The inchi key that was searched for.
         * @return the inchi key as a String.
         */
        public String getInchiKey() {
            return inchiKey;
        }

        /**
         * The search result type.
         * @return the {@link InchiKeySearchResultType}, will never be null.
         */
        public InchiKeySearchResultType getResultType() {
            return resultType;
        }

        public InchiKeySearchResult(String inchiKey, InchiKeySearchResultType resultType) {
            this.inchiKey = Objects.requireNonNull(inchiKey);
            this.resultType = Objects.requireNonNull(resultType);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InchiKeySearchResult)) return false;
            InchiKeySearchResult that = (InchiKeySearchResult) o;
            return inchiKey.equals(that.inchiKey) &&
                    resultType == that.resultType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(inchiKey, resultType);
        }
    }
}
