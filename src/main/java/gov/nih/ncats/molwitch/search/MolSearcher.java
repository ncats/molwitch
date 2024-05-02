/*
 * NCATS-MOLWITCH
 *
 * Copyright 2024 NIH/NCATS
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

import gov.nih.ncats.molwitch.Chemical;

import java.util.Optional;

/**
 * Searches a given target against somekind of query criteria.
 */
public interface MolSearcher {
    /**
     * Search the given target chemical
     * and return the hit positions against the query.
     * @param target the chemical to search against the query; can not be null.
     *
     * @return an Optional of the hit positions;
     * if there are no search results the Optional will be empty.
     *
     * @throws NullPointerException if target is null.
     */
    Optional<int[]> search(Chemical target);
}
