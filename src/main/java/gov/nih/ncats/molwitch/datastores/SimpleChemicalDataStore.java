/*
 * NCATS-MOLWITCH
 *
 * Copyright 2020 NIH/NCATS
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

package gov.nih.ncats.molwitch.datastores;

import gov.nih.ncats.common.iter.CloseableIterator;
import gov.nih.ncats.molwitch.Chemical;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

class SimpleChemicalDataStore implements ChemicalDataStore{

    private final String raw;
    private final Chemical chemical;

    SimpleChemicalDataStore(String raw) throws IOException {
        this.raw = Objects.requireNonNull(raw);
        chemical = Chemical.parse(raw);
    }
    @Override
    public long getSize() {
        return 1;
    }

    @Override
    public String getRaw(long offset) {
        return raw;
    }

    @Override
    public Chemical get(long offset) {
        if(offset !=0){
            throw new IndexOutOfBoundsException(Long.toString(offset));
        }
        return chemical;
    }

    @Override
    public CloseableIterator<String> getRawIterator() {
        return CloseableIterator.wrap(Arrays.asList(raw).iterator());
    }

    @Override
    public CloseableIterator<Chemical> getIterator() {
        return CloseableIterator.wrap(Arrays.asList(chemical).iterator());
    }

    @Override
    public void close() {
            //no-op
    }
}
