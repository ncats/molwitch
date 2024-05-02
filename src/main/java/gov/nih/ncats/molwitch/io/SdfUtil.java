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

        private static final Pattern ATOM_SYMBOL_PATTERN = Pattern.compile(".{3}");

        private ReadState currentReadState = ReadState.BEGIN;
        public CleanSdfIterator(BufferedReader reader) throws IOException{
            this.reader = new PushbackBufferedReader(reader);

            currentRecord = readNextRecord();
        }

        private enum ReadState{

            BEGIN{
                @Override
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer, Map<PARSE_PROPERTIES, Object> properties) throws IOException {
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
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer, Map<PARSE_PROPERTIES, Object> properties) throws IOException{
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
                    //we will process the counts line next so don't write it out
                    //but if we get here we should have a header of 4 lines
                    //so only write out the first 3
                    for(int i=0; i<header.size()-1; i++){
                        buffer.append(header.get(i)).append("\n");
                    }
                    String lastLine = header.get(header.size() - 1);
                    if(lastLine.endsWith("V2000")) {
                        //unread counts line
                        reader.pushBack(header.get(3));
                    }else{
                        buffer.append(lastLine).append("\n");
                    }

                    //we're done reading the header
                    return COUNTS_LINE;
                }
            },
            COUNTS_LINE{
                @Override
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer, Map<PARSE_PROPERTIES, Object> properties) throws IOException {
                    String line = reader.readLine();
//                    System.out.println(line);
                    //aaabbblllfffcccsssxxxrrrpppiiimmmvvvvvv
                    int numAtoms = Integer.parseInt(line.substring(0,3).trim());
                    int numBonds = Integer.parseInt(line.substring(3,6).trim());
                    int atomLists = Integer.parseInt(line.substring(6,9).trim());

                    int chiral = Integer.parseInt(line.substring(12,15).trim());
                    int stexts = Integer.parseInt(line.substring(15,18).trim());
                    //TODO for consistency keep the ignored part the same maybe make that configurable?
                    String ignoredPart = line.substring(18,30);
                    int numAdditionalProperties = Integer.parseInt(line.substring(30,33).trim());
                    //ignore the rest

                    buffer.append(String.format("%3d%3d%3d%3d%3d%3d%s%3d V2000\n", numAtoms, numBonds, atomLists, 0,chiral, stexts, ignoredPart, numAdditionalProperties));
                    properties.put(PARSE_PROPERTIES.EXPECTED_NUM_ATOMS, numAtoms);
                    properties.put(PARSE_PROPERTIES.EXPECTED_NUM_BONDS, numBonds);
                    return ATOM_LIST;
                }
            },
            ATOM_LIST{
                @Override
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer, Map<PARSE_PROPERTIES, Object> properties) throws IOException {
                    int numAtoms = (Integer) properties.get(PARSE_PROPERTIES.EXPECTED_NUM_ATOMS);
                    int numBonds = (Integer) properties.get(PARSE_PROPERTIES.EXPECTED_NUM_BONDS);
                    for(int i=0; i< numAtoms; i++){
                        String line = reader.readLine();
                        //for now assume the line is formatted correctly
                        //except possibly leading whitespace
                        //due to copy and paste mistakes or weird formatting from editors
                        int indexofFirstDecimal = line.indexOf('.');
                        String xCoordinateIntPart = line.substring(0, indexofFirstDecimal).trim();

                        buffer.append(String.format("%5s", xCoordinateIntPart)).append(line.substring(indexofFirstDecimal)).append("\n");
                    }
                    return BOND_LIST;
                }
            },
            BOND_LIST{
                @Override
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer, Map<PARSE_PROPERTIES, Object> properties) throws IOException {
                    int numAtoms = (Integer) properties.get(PARSE_PROPERTIES.EXPECTED_NUM_ATOMS);
                    int numBonds = (Integer) properties.get(PARSE_PROPERTIES.EXPECTED_NUM_BONDS);

                    for(int i=0; i< numBonds; i++){
                        String line = reader.readLine();
                        //for now assume the line is formatted correctly
                        //except possibly leading whitespace
                        //due to copy and paste mistakes or weird formatting from editors
                        char[] asChars = line.toCharArray();
                        int j=0;
                        while(asChars[j] <= ' '){
                            j++;
                        }
                        String leftTrimmed = new String(asChars, j, asChars.length-j);
                        try(Scanner scanner = new Scanner(leftTrimmed)){
                            int index = scanner.nextInt();
                            if(index < 10){
                                buffer.append("  ").append(leftTrimmed).append("\n");
                            }else if(index < 100){
                                buffer.append(" ").append(leftTrimmed).append("\n");
                            }else if(index < 1000){
                                //3 digit first atom index 2 or less digit 2nd index
                                buffer.append(leftTrimmed).append("\n");
                            }else if(index <10_000){
                                    //4 digits = abbb
                                    buffer.append("  ").append(leftTrimmed).append("\n");
                            }else if(index < 100_000){
                                //5 digits = aabbb
                                buffer.append(" ").append(leftTrimmed).append("\n");
                            }else{
                                //6 digits aaabbb
                                buffer.append(leftTrimmed).append("\n");
                            }


                        }

                    }
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
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer, Map<PARSE_PROPERTIES, Object> properties) throws IOException {

                    Map<Integer, String> knownSgroups = new TreeMap<>();
                    Map<Integer, String> sgroupLabels = new TreeMap<>();
                    Map<Integer, DataBuilder> dataBuilders = new TreeMap<>();

                    Set<Integer> removedSgroups = new HashSet<>();
                    DataBuilder currentDataBuilder=null;
                    String line;
                    while( (line = reader.readLine()) !=null){
                        if(currentDataBuilder !=null && !line.startsWith("M  SCD") && !line.startsWith("M  SED")){
                            //incorrectly formatted DATA group block..
                            //supposed to be consecutive SCD(s) followed by SED line
                            //just write out what we have
                            buffer.append(currentDataBuilder.format());
                            currentDataBuilder = null;
                        }
                        if(line.startsWith("M  CHG")) {
                            try (Scanner scanner = new Scanner(line)) {
                                scanner.next(); //M
                                scanner.next(); // CHG
                                int numCharges = scanner.nextInt();

                                //break into blocks of 8
                                while(numCharges>0) {

                                    int chargesOnLine = Math.min(8, numCharges);

                                    buffer.append("M  CHG").append(String.format("%3s", chargesOnLine));
                                    for (int j = 0; j < chargesOnLine; j++) {
                                        buffer.append(String.format(" %3s %3s", scanner.next(), scanner.next()));
                                    }
                                    buffer.append("\n");
                                    numCharges-=8;
                                }

                            }
                        }else if(SGROUP_DEF_PATTERN.matcher(line).find()) {
                            try (Scanner scanner = new Scanner(line)) {
                                scanner.next(); //M
                                scanner.next(); // STY
                                //we could have multiple STY lines we don't want to duplicate records
                                //so only write out the cleaned up ones defined in this line
                                Set<Integer> groupsDefinedInThisLine = new LinkedHashSet<>();
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
                                    if(knownSgroups.containsKey(num)){
                                        //we have a duplicate s-group number!
                                        //TODO how do we handle that? error out? overwrite?
                                        //for now just assume it's an error and do not redeclare it
                                    }else{
                                        knownSgroups.put(num, sgroupType);
                                        groupsDefinedInThisLine.add(num);
                                    }

                                }
                                if(groupsDefinedInThisLine.size() <=8) {
                                    buffer.append("M  STY  " + groupsDefinedInThisLine.size());
                                    for (Integer num : groupsDefinedInThisLine) {
                                        String type = knownSgroups.get(num);
                                        buffer.append(String.format(" %3s %3s", num, type));
                                    }
                                    buffer.append("\n");
                                }else{
                                    int numLeft = groupsDefinedInThisLine.size();
                                    Iterator<Integer> iter = groupsDefinedInThisLine.iterator();
                                    do {
                                        buffer.append("M  STY  " + Math.min(numLeft, 8));

                                        for (int i = 0; i < 8 && iter.hasNext(); i++) {
                                            Integer num = iter.next();
                                            String type = knownSgroups.get(num);
                                            buffer.append(String.format(" %3s %3s", num, type));
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
//                                    if (numAtoms <= 15) {
//                                        //fine as is
//                                        buffer.append(line).append("\n");
//                                    } else {
                                        //break into blocks of 15
                                        int numLines = numAtoms / 15 + 1;

                                        for (int i = 0; i < numLines; i++) {
                                            int atomsOnLine = Math.min(15, (numAtoms - (i * 15)));

                                            int validAtoms=0;
                                            StringBuilder tmp = new StringBuilder();
                                             for (int j = 0; j < atomsOnLine; j++) {
                                                 int offset =  scanner.nextInt();
                                                 if(offset>0) {
                                                     validAtoms++;
                                                     tmp.append(String.format(" %3d", offset));
                                                 }
                                            }
                                             tmp.append("\n");
                                            if(validAtoms >0) {
                                                buffer.append(String.format("M  SAL %3d%3d", sgroupNumber, validAtoms)).append(tmp);
                                            }
//                                        }
                                    }
                                }else if("SED".equals(typeCode)){
                                    if(currentDataBuilder ==null){
                                        currentDataBuilder =  new DataBuilder(sgroupNumber);
                                    }else{
                                        //already have a current data builder
                                        if(sgroupNumber.intValue() != currentDataBuilder.sgroupNum){
                                            //a different group !?
                                            //write old one out
                                            buffer.append(currentDataBuilder.format());
                                            //now make new one...
                                            currentDataBuilder =  new DataBuilder(sgroupNumber);
                                        }
                                    }

                                    currentDataBuilder.handleSED(scanner.nextLine());
                                    buffer.append(currentDataBuilder.format());
                                    currentDataBuilder=null;


                                }else if("SCD".equals(typeCode)){
                                    if(currentDataBuilder ==null){
                                        currentDataBuilder =  new DataBuilder(sgroupNumber);
                                    }else{
                                        //already have a current data builder
                                        if(sgroupNumber.intValue() != currentDataBuilder.sgroupNum){
                                            //a different group !?
                                            //write old one out
                                            buffer.append(currentDataBuilder.format());
                                            //now make new one...
                                            currentDataBuilder =  new DataBuilder(sgroupNumber);
                                        }
                                    }
                                    currentDataBuilder.handleSCD(scanner.nextLine());
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
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer, Map<PARSE_PROPERTIES, Object> properties) throws IOException {

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
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer, Map<PARSE_PROPERTIES, Object> properties) throws IOException {

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
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer, Map<PARSE_PROPERTIES, Object> properties) throws IOException {
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
                public ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer, Map<PARSE_PROPERTIES, Object> properties) throws IOException {
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
             * @param properties any properties that need to be set in one readState and used in a later state
             * @return the next {@link ReadState} based on the lines read from the reader.
             * @throws IOException if there are any problems reading the lines or parsing the data that was read.
             */
            public abstract ReadState readClean(PushbackBufferedReader reader, StringBuilder buffer, Map<PARSE_PROPERTIES, Object> properties) throws IOException;

        }
        private String readNextRecord() throws IOException {
            buffer.setLength(0); //clear old state
            Map<PARSE_PROPERTIES, Object> properties = new EnumMap<>(PARSE_PROPERTIES.class);
            try {
                while (currentReadState != ReadState.EOF) {
                    currentReadState = currentReadState.readClean(reader, buffer, properties);
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

    private enum PARSE_PROPERTIES{
        EXPECTED_NUM_ATOMS,
        EXPECTED_NUM_BONDS;
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

    private static final class DataBuilder{
        private boolean seenSED=false;
        private StringBuilder builder = new StringBuilder(200);
        private final int sgroupNum;

        public DataBuilder(int sgroupNum){
            this.sgroupNum=sgroupNum;
        }
        public void handleSED(String text){
            handleSCD(text);
            seenSED=true;
        }
        public void handleSCD(String text){
            if(seenSED){
                //TODO already seen SED!!
                //do nothing I guess
                return;
            }
            if(Character.isWhitespace(text.charAt(0))){
                builder.append(text.substring(1));
            }else{
                builder.append(text);
            }
        }

        public String format(){
            //right trim but not left
            String rightTrimmed = builder.toString().replaceAll("\\s+$","");
            if(rightTrimmed.length()<=69){
                //only one line
                return String.format("M  SED %3d %s\n",sgroupNum, rightTrimmed);
            }
            StringBuilder builder = new StringBuilder(200);
            do {

                builder.append(String.format("M  SCD %3d %s\n", sgroupNum, rightTrimmed.substring(0, 70)));
                rightTrimmed = rightTrimmed.substring(70);
            }while(rightTrimmed.length()>70);
             builder.append(String.format("M  SED %3d %s\n",sgroupNum, rightTrimmed));
             return builder.toString();
        }

    }

}
