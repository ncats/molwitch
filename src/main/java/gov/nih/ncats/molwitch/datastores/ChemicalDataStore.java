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

package gov.nih.ncats.molwitch.datastores;

import gov.nih.ncats.common.io.InputStreamSupplier;
import gov.nih.ncats.common.iter.CloseableIterator;
import gov.nih.ncats.molwitch.Chemical;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public interface ChemicalDataStore extends Closeable {

    long getSize();

    String getRaw(long offset);

    Chemical get(long offset);

    CloseableIterator<String> getRawIterator();

    CloseableIterator<Chemical> getIterator();


    static ChemicalDataStore forFile(File f) throws IOException{
        return forFile(InputStreamSupplier.forFile(f));
    }

    static ChemicalDataStore forURL(URL url) throws IOException{
        return forFile(InputStreamSupplier.forResourse(url));
    }

    static ChemicalDataStore forFile(InputStreamSupplier inputStreamSupplier) throws IOException{
        return new FileChemicalDataStore(inputStreamSupplier);
    }

    static ChemicalDataStore forFile(File f, int estimatedNumberOfRecords) throws IOException{
        return forFile(InputStreamSupplier.forFile(f), estimatedNumberOfRecords);
    }

    static ChemicalDataStore forURL(URL url, int estimatedNumberOfRecords) throws IOException{
        return forFile(InputStreamSupplier.forResourse(url), estimatedNumberOfRecords);
    }

    static ChemicalDataStore forFile(InputStreamSupplier inputStreamSupplier, int estimatedNumberOfRecords) throws IOException{
        return new FileChemicalDataStore(inputStreamSupplier, estimatedNumberOfRecords);
    }
}
