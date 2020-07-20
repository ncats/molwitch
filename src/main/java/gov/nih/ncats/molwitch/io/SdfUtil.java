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

package gov.nih.ncats.molwitch.io;

import gov.nih.ncats.common.iter.CloseableIterator;
import gov.nih.ncats.molwitch.SGroup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.regex.Pattern;

class SdfUtil {

    /**
     * Iterator of a Molecule Records from a SDF or Mol file.
     * Each call to Next reads multiple lines and returns a String
     * of the "cleaned" complete molecule record.
     */
    public static class CleanSdfIterator implements CloseableIterator<String> {

        private PushbackBufferedReader reader;

        private StringBuilder buffer = new StringBuilder(10_240);
        private String currentRecord;

        private ReadState currentReadState = ReadState.BEGIN;
        public CleanSdfIterator(BufferedReader reader) throws IOException{
            this.reader = new PushbackBufferedReader(reader);

            currentRecord = readNextRecord();
        }

        private enum ReadState{

            BEGIN{
                @Override
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer) throws IOException {
                    return reader.peekLine() ==null? EOF : HEADER;
                }
            },
            /**
             * Reads and cleans the header section.
             * Current cleans:
             * 1. adds leading blank line if the header is missing the first blank line
             *      (often result from copy and paste or whitespace removal errors)
             */
            HEADER{
                @Override
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer) throws IOException{
                    List<String> header = new ArrayList<>(4);

                    for(int i=0; i<4; i++){
                        String line = reader.readLine();
                        if(line ==null){
                            throw new IOException("invalid mol header early EOF : " + header);
                        }
                        header.add(line);
                        if(line.contains("V2000")){
                            break;
                        }
                    }
                    boolean valid=header.size() ==4;

                    if(header.size() ==3){
                        boolean firstLineBlank =header.get(0).trim().isEmpty();
                        boolean secondLineBlank =header.get(1).trim().isEmpty();
                        boolean thirdLineBlank =header.get(2).trim().isEmpty();

                        //common copy and paste error first blank line is missing
                        if( !firstLineBlank && secondLineBlank && !thirdLineBlank){
                            //add blank line
                            header.add(0, "");
                            valid =true;
                        }else if (firstLineBlank && !secondLineBlank && !thirdLineBlank){
                            //missing comment line...
                            //add blank line
                            header.add(2, "");
                            valid =true;
                        }else if(!firstLineBlank && !secondLineBlank && !thirdLineBlank){
                            //all 3 lines have text so we're missing a line
                            //but is it missing the first line which is the name or is it
                            //missing the 3rd line which is the comment?
                            //for now we will check for internal whitespace and assume the
                            // name line can't have whitespace

                            if(header.get(0).trim().matches("\\S+\\s+\\S+$")){
                                //first line has spaces so it's probably the program name line
                                //add blank line
                                header.add(0, "");
                                valid =true;
                            }else {
                                //if we are here, then our first line has no whitespace so it's a name...?
                                //missing comment line...
                                //add blank line
                                header.add(2, "");
                                valid =true;
                            }
                        }
                    }
                    if(!valid){
                        throw new IOException("invalid mol header early EOF : " + header);
                    }
                    for(String l : header){
                        buffer.append(l).append("\n");
                    }

                    //we're done reading the header
                    return CONNECTION_TABLE;
                }
            },
            /**
             * Cleans the connection table section of  the mol file.
             * Current cleans:
             *   1. the M  END line is forced to have 2 spaces between M and END.
             *   2. ignore anything after the M  END line start, as it sometimes is added by accident in a few tools
             *   3. break M  CHG lines with more than 8 charges into multiple lines with at most 8 charges per line.
             *   4. Removes SGroup lines of unknown type
             *   5. Removes any Sgroup lines that reference Sgroup not defined in STY
             *   6. break M  STY lines with more than 8 Sgroups into multiple lines with at most 8 S-groups per line.
             */
            CONNECTION_TABLE{

                private Pattern END_PATTERN = Pattern.compile("^\\s*M\\s+END");
                private Pattern SGROUP_DEF_PATTERN = Pattern.compile("^\\s*M\\s+STY(.+)");
                private Pattern SGROUP_CATCHALL_PATTERN = Pattern.compile("^\\s*M\\s+S..(.+)");


                @Override
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer) throws IOException {

                    Map<Integer, String> knownSgroups = new TreeMap<>();
                    Set<Integer> removedSgroups = new HashSet<>();
                    String line;
                    while( (line = reader.readLine()) !=null){

                        if(line.startsWith("M  CHG")) {
                            try (Scanner scanner = new Scanner(line)) {
                                scanner.next(); //M
                                scanner.next(); // CHG
                                int numCharges = scanner.nextInt();
                                if (numCharges <= 8) {
                                    //fine as is
                                    buffer.append(line).append("\n");
                                } else {
                                    //break into blocks of 8
                                    int numLines = numCharges / 8 + 1;

                                    for (int i = 0; i < numLines; i++) {
                                        int chargesOnLine = Math.min(8, (numCharges - (i * 8)));

                                        buffer.append("M  CHG").append(String.format("%3s", chargesOnLine));
                                        for (int j = 0; j < chargesOnLine; j++) {
                                            buffer.append(String.format(" %3s %3s", scanner.next(), scanner.next()));
                                        }
                                        buffer.append("\n");
                                    }
                                }
                            }
                        }else if(SGROUP_DEF_PATTERN.matcher(line).find()) {
                            try (Scanner scanner = new Scanner(line)) {
                                scanner.next(); //M
                                scanner.next(); // STY

                                int numberOfRecords = scanner.nextInt();
                                if (numberOfRecords > 8) {
                                    //need to split this into lines of 8
                                }
                                for (int i = 0; i < numberOfRecords; i++) {
                                    Integer num = scanner.nextInt();
                                    String sgroupType = scanner.next();

                                    if (SGroup.SGroupType.valueByTypeName(sgroupType) == null) {
                                        //not a valid sgroup type ignore ?
                                        removedSgroups.add(num);
                                        continue;
                                    }
                                    //when we are here we have a valid group
                                    String oldSgroup = knownSgroups.put(num, sgroupType);
                                    if (oldSgroup != null) {
                                        //we have a duplicate s-group number!
                                        //TODO how do we handle that? error out? overwrite?
                                    }
                                }
                                if(knownSgroups.size() <=8) {
                                    buffer.append("M  STY  " + knownSgroups.size());
                                    for (Map.Entry<Integer, String> entry : knownSgroups.entrySet()) {
                                        buffer.append(String.format(" %3s %3s", entry.getKey(), entry.getValue()));
                                    }
                                    buffer.append("\n");
                                }else{
                                    int numLeft = knownSgroups.size();
                                    do {
                                        buffer.append("M  STY  " + Math.min(numLeft, 8));
                                        Iterator<Map.Entry<Integer, String>> iter = knownSgroups.entrySet().iterator();
                                        for (int i = 0; i < 8 && iter.hasNext(); i++) {
                                            Map.Entry<Integer, String> entry = iter.next();
                                            buffer.append(String.format(" %3s %3s", entry.getKey(), entry.getValue()));
                                        }
                                        buffer.append("\n");
                                        numLeft -= 8;
                                    }while(numLeft>0);

                                }
                            }
                        }else if(SGROUP_CATCHALL_PATTERN.matcher(line).find()){
                            try (Scanner scanner = new Scanner(line)) {
                                scanner.next(); //M
                                String typeCode = scanner.next(); // SXX
                                //GSRS-1596
                                //not all S group lines have a group number
                                if("SDS".equals(typeCode)){
                                    //format is M SDS EXPn15
                                    String expansion = scanner.next("[A-Z]+");
                                    if("EXP".equals(expansion)){
                                        int count =scanner.nextInt();
                                        if(count <=15) {
                                            buffer.append(String.format("M  SDS EXP%3d", count));
                                            for (int i = 0; i < count; i++) {
                                                buffer.append(String.format(" %3d", scanner.nextInt()));
                                            }
                                            buffer.append("\n");
                                        }else{
                                            int numLeft = count;
                                            do{
                                                int currentLine = Math.min(15, numLeft);
                                                buffer.append(String.format("M  SDS EXP%3d", currentLine));
                                                for (int i = 0; i < currentLine; i++) {
                                                    buffer.append(String.format(" %3d", scanner.nextInt()));
                                                }
                                                buffer.append("\n");
                                                numLeft -=15;
                                            }while(numLeft>0);
                                        }
                                        continue;
                                    }
                                    //if we are here it's an SDS line that's not an EXP ?
                                    //write out as is ?
                                    buffer.append(line).append("\n");
                                    continue;
                                }
                                //TODO should we check is something is valid?

                                Integer sgroupNumber = scanner.nextInt();
                                //only write out sgroups we haven't removed
                                String sgroupType = knownSgroups.get(sgroupNumber);
                                if(sgroupType ==null || !isValidSgroupLineForType(sgroupType, typeCode)){
                                    //not valid don't write it out
                                    continue;
                                }else if("SAL".equals(typeCode)){
                                    int numAtoms = scanner.nextInt();
                                    if (numAtoms <= 8) {
                                        //fine as is
                                        buffer.append(line).append("\n");
                                    } else {
                                        //break into blocks of 8
                                        int numLines = numAtoms / 8 + 1;

                                        for (int i = 0; i < numLines; i++) {
                                            int atomsOnLine = Math.min(8, (numAtoms - (i * 8)));

                                            buffer.append("M  SAL").append(String.format(" %3d %2d", sgroupNumber, atomsOnLine));
                                            for (int j = 0; j < atomsOnLine; j++) {
                                                buffer.append(String.format(" %3d", scanner.nextInt()));
                                            }
                                            buffer.append("\n");
                                        }
                                    }
                                }else{
                                    buffer.append(line).append("\n");
                                }
                            }
                        } else if(END_PATTERN.matcher(line).find()){
                            buffer.append("M  END"); //NOTE 2 spaces
                            if(reader.peekLine() !=null){
                                buffer.append("\n");
                            }
                            break;
                        }else{
                            buffer.append(line).append("\n");
                        }
                    }
                    if(line ==null){
                        throw new IOException("reached EOF while in connection table");
                    }
                    return BEFORE_DATA_ITEMS;
                }
            },
            /**
             * Cleans the lines between the connection table and any properties of an SD formatted record.
             *
             * Current Cleans:
             * 1. Removes all leading blank lines.  The spec allows at most 1 blank line but some vendors put 2.
             */
            BEFORE_DATA_ITEMS{
                @Override
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer) throws IOException {

                    String line;
                    while( (line = reader.readLine()) !=null){
                        // some vendors have extra blank lines space between M  END and the start of data items
                        if(line.trim().isEmpty()){
                            continue;
                        }
                        if(line.startsWith(">")){
                            //begin of data item block
                            reader.pushBack(line);
                            break;
                        }
                    }
                    if(line ==null){
                        return EOF;
                    }
                    return DATA_ITEMS;


                }
                },
            /**
             * Cleans the properties of a sd formatted record.
             *
             * Current Cleans:
             *  1. removes final new lines after last $$$$ if there are any.
             */
            DATA_ITEMS{
                @Override
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer) throws IOException {

                    String line;
                    while( (line = reader.readLine()) !=null ){
                        buffer.append(line);

                        if(line.startsWith("$$$$")){
                            String nextLine = reader.peekLine();
                            if(nextLine !=null){
                                //more data
                                buffer.append("\n");
                            }
                            return DELIMITER;
                        }else{
                            buffer.append("\n");
                        }
                    }
                    //if we get here we got to EOF before $$$$
                    return EOF;
                }
            },
            /**
             * Area between properties and next header if there are multiple records in file.
             *
             * Current cleans:
             *  1. removes extra blank lines between $$$$ and next header. some vendors have extra blank lines.
             */
            DELIMITER {
                @Override
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer) throws IOException {
                    //We've already read the $$$$ part
                    //so just remove extra blank lines until the next record
                    String line;
                    int numBlankLines = 0;
                    while ((line = reader.readLine()) != null) {

                        //there are 2 possibilities if we are inside this loop:
                        // 1 : we have a blank line
                        // 2: we have a non-blank line which is either the optional name or the program line in the header

                        //there can be at most only 1 blank line in the beginning of the header
                        //so ignore extra blank lines
                        // and either way we have to pushback the line so the header can be correctly parsed
                        //(the header state will correctly add missing blank lines for us)

                        if (line.trim().isEmpty()) {

                            numBlankLines++;
                        } else {
                            //found a non-blank line we're in the header now
                            //unread the header line.
                            reader.pushBack(line);
                            //if there were  no blank lines yet
                            //then we have probably a valid header with optional name
                            //so don't do anything else.

                            if (numBlankLines != 0) {

                                //found blank lines
                                reader.pushBack(""); //unread 1 empty line no matter how many lines read
                            }
                            return HEADER;
                        }
                    }

                    return EOF;
                }
            },
            EOF{
                @Override
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer) throws IOException {
                    //infinite Loop?

                    return EOF;
                }
            }
            ;


            /**
             * Read potentially multiple lines from the reader, clean up the read lines and append
             * those cleaned lines to the given StringBuilder.
             * @param reader the {@link PushbackBufferedReader} to read from.
             * @param buffer the StringBuilder to write to.
             * @return the next {@link ReadState} based on the lines read from the reader.
             * @throws IOException if there are any problems reading the lines or parsing the data that was read.
             */
            public abstract ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer) throws IOException;

        }
        private String readNextRecord() throws IOException {
            buffer.setLength(0); //clear old state
            try {
                while (currentReadState != ReadState.EOF) {
                    currentReadState = currentReadState.readClean(reader, buffer);
                    //we check delimiter here because if we put it up in the while loop
                    //with the EOF check then when  on the 2nd call to next() we never enter the while loop!
                    if (currentReadState == ReadState.DELIMITER) {
                        break;
                    }
                }

                if (buffer.length() > 0) {
                    return buffer.toString();
                }
                return null;
            }catch(Exception e){
                //usually a runtime exception from scanner
                throw new IOException("error parsing ctfile",e);
            }
        }
        @Override
        public boolean hasNext(){
            return currentRecord !=null;
        }
        @Override
        public String next(){
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            String ret = currentRecord;
            try {
                currentRecord = readNextRecord();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return ret;
        }

        @Override
        public void close() throws IOException {
            currentRecord=null;
            reader.close();

        }
    }

    /**
     * Will close the inputReader but not the outputWriter inase multiple files should be written to same writer.
     * @param inputReader
     * @param outputWriter
     * @throws IOException
     */
    public static void copyClean(BufferedReader inputReader, BufferedWriter outputWriter) throws IOException{
        try(CleanSdfIterator reader = new CleanSdfIterator(inputReader)) {
            while (reader.hasNext()) {
                outputWriter.write(reader.next());
            }
        }
    }

    private static boolean isValidSgroupLineForType(String sgroupType, String typeCode){
        if("SPA".equals(typeCode) && !("MUL".equals(sgroupType))){
            //parent atoms only allowed for multiple groups?
            return false;
        }
        return true;
    }

}
