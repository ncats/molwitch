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

import gov.nih.ncats.common.io.IOUtil;
import gov.nih.ncats.common.io.InputStreamSupplier;
import gov.nih.ncats.common.io.TextLineParser;
import gov.nih.ncats.common.iter.CloseableIterator;
import gov.nih.ncats.common.util.Range;
import gov.nih.ncats.molwitch.Chemical;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class FileChemicalDataStore implements ChemicalDataStore {

    private final InputStreamSupplier inputStreamSupplier;
    private List<Range> recordRanges;

    FileChemicalDataStore(InputStreamSupplier inputStreamSupplier) throws IOException{
        this(inputStreamSupplier, 1000);

    }

    FileChemicalDataStore(InputStreamSupplier inputStreamSupplier, int estimatedNumberOfRecords) throws IOException{
        this.inputStreamSupplier = Objects.requireNonNull(inputStreamSupplier);
        recordRanges = new ArrayList<>(estimatedNumberOfRecords);
        parseRanges();
    }

    private void parseRanges() throws IOException{
        long startOffset=0;
        try(TextLineParser parser = new TextLineParser(inputStreamSupplier.get())){
            while(parser.hasNextLine()){
                String line = parser.nextLine();
                if(line.startsWith("$$$$")){
                    long nextOffset =parser.getPosition();
                    recordRanges.add(new Range(startOffset, nextOffset -1));
                    startOffset= nextOffset;
                }
            }
        }
    }
    @Override
    public long getSize() {
        return recordRanges.size();
    }

    @Override
    public String getRaw(long offset) {
        Range range = recordRanges.get( (int) offset);
        byte[] array = new byte[(int) range.getLength()];
        try(InputStream in = inputStreamSupplier.get()){
            IOUtil.blockingSkip(in, range.getBegin());
            IOUtil.blockingRead(in, array);
            return new String(array);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Chemical get(long offset) {
        String raw = getRaw(offset);
        if (raw != null) {
            try {
                return Chemical.parse(raw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
     return null;
    }

    @Override
    public CloseableIterator<String> getRawIterator() {
        return null;
    }

    @Override
    public CloseableIterator<Chemical> getIterator() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

}
