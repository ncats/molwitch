/*
 * NCATS-MOLWITCH
 *
 * Copyright 2021 NIH/NCATS
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

import gov.nih.ncats.common.io.InputStreamSupplier;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.SortedSet;

/**
 * A binary encoded InchiKey
 */
public class BinaryInchiKey {

    private static final BigInteger SHIFT = BigInteger.valueOf(1).shiftLeft(48);
    private static final BigInteger MASK = new BigInteger("FFFFFFFFFFFFFFFFFFFF000000000000", 16);

    private static short[][] LOOKUP_2 = new short[26][26];
    private static short[][][] LOOKUP_3 = new short[26][26][26];
    static{
        for(int i=0; i< 26; i++){
            for(int j=0; j< 26; j++){
                for(int k=0; k< 26; k++){
                    int value = (676* i) + (26 * j) + k;
                    if(value >=12844){
                        value -= 516;
                    }
                    if(value > 2704){
                        value -= 676;
                    }
                    LOOKUP_3[i][j][k]= (short) value;
                }
                LOOKUP_2[i][j]= (short)((26 * i) + j);

            }
        }
    }
    public static class BinaryInchiKeyBag{

        private final BigInteger[] array;

        public static BinaryInchiKeyBag fromSortedFile(File sortedInchikeyFile) throws IOException {
            InputStreamSupplier supplier = InputStreamSupplier.forFile(sortedInchikeyFile);
            //read the whole file 2x - first to get the number of records to efficiently size our array
            int count=0;
            try(BufferedReader in = new BufferedReader( new InputStreamReader(supplier.get()))){

                while( in.readLine() !=null){
                    count++;
                }
            }
            BigInteger[] a = new BigInteger[count];
            count=0;
            try(BufferedReader in = new BufferedReader( new InputStreamReader(supplier.get()))){
                String line;
                while( (line = in.readLine()) !=null){
                    a[count++] = encode(line);
                }
            }
            return new BinaryInchiKeyBag(a);
        }
        public static BinaryInchiKeyBag fromSortedSet(SortedSet<String> inchiKeys){
            BigInteger[] a = new BigInteger[inchiKeys.size()];
            Iterator<String> iter = inchiKeys.iterator();
            int i=0;
            while(iter.hasNext()){
                a[i++] = encode(iter.next());
            }
            return new BinaryInchiKeyBag(a);
        }
        private BinaryInchiKeyBag(BigInteger[] array) {
            this.array = array;
        }


        public InchiKeySearchResult contains(String inchiKey){
            BigInteger exact = encode(inchiKey);
            if(Arrays.binarySearch(array, exact) >=0){
                return new InchiKeySearchResult(inchiKey, ResultType.EXACT);
            }
            BigInteger lowInsensitive = exact.and(MASK);
            BigInteger highInsensitive = lowInsensitive.add(SHIFT);
            if(containsInsensitive(lowInsensitive, highInsensitive)){
                return new InchiKeySearchResult(inchiKey, ResultType.STEREO_INSENSITIVE);
            }
            return new InchiKeySearchResult(inchiKey, ResultType.NO_MATCH);

        }
        public boolean containsStereo(String inchiKey){
            BigInteger value = encode(inchiKey);
            return Arrays.binarySearch(array, value) >=0;
        }

        public boolean containsInsensitive(String inchiKey){
            //stereo insensitive
            BigInteger[] pair = encodeInsensitive(inchiKey);
            return containsInsensitive(pair[0], pair[1]);
        }

        private boolean containsInsensitive(BigInteger low, BigInteger high ) {
            int lowResult = Arrays.binarySearch(array,  low);
            int highResult = Arrays.binarySearch(array,  high);
            if(lowResult ==highResult){
                //no match
                return false;
            }
            if(Math.abs(lowResult) < Math.abs(highResult)){
                //there is something in between low and high so we have an insensitive match
                return true;
            }
            return false;
        }

        public static class InchiKeySearchResult{
            private final String inchiKey;
            private final ResultType resultType;

            public String getInchiKey() {
                return inchiKey;
            }

            public ResultType getResultType() {
                return resultType;
            }

            public InchiKeySearchResult(String inchiKey, ResultType resultType) {
                this.inchiKey = inchiKey;
                this.resultType = resultType;
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
        public enum ResultType{
            EXACT,
            STEREO_INSENSITIVE,
            NO_MATCH

        }
    }

    private static BigInteger[] encodeInsensitive(String inchiKey) {

        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        ShortBuffer buf = byteBuffer.asShortBuffer();
        char[] chars = inchiKey.toCharArray();
        buf.put((short) encode3(chars, 0));
        buf.put((short) encode3(chars, 3));
        buf.put((short) encode3(chars, 6));
        buf.put((short) encode3(chars, 9));
        buf.put((short) encode2(chars, 12));

        BigInteger low = new BigInteger(byteBuffer.array());

        BigInteger high = low.add(SHIFT);
        return new BigInteger[]{low, high};

    }
    private static BigInteger encode(String inchiKey){
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        ShortBuffer buf = byteBuffer.asShortBuffer();
        char[] chars = inchiKey.toCharArray();

        buf.put(encode3b(chars, 0));
        buf.put(encode3b(chars, 3));
        buf.put(encode3b(chars, 6));
        buf.put(encode3b(chars, 9));
        buf.put(encode2b(chars, 12));

        //stereo part
        buf.put(encode3b(chars, 15));
        buf.put(encode3b(chars, 18));
        buf.put(encode2b(chars, 21));

        //the rest of the key usually SA-N is for "standard flag, A= version 1 and N for no protonation.  we can assume all inchis we see are SA
        //do we care about protonation?

        byte[] array = byteBuffer.array();
        array[array.length -2]|= ((byte)chars[chars.length - 1]) <<2;
        return new BigInteger(array);

    }
    private static short encode3b(char[] chars, int offset){
        return LOOKUP_3[chars[offset]-'A'][chars[offset+1]-'A'][chars[offset+2]-'A'];
    }
    private static int encode3(char[] chars, int offset){
        //TODO should we turn this into a table lookup?
        int value = 676* (chars[offset]-'A') + 26 * (chars[offset+1]- 'A') + (chars[offset +2]-'A');
        if(value >=12844){
            value -= 516;
        }
        if(value > 2704){
            value -= 676;
        }
        return value;
    }

    private static short encode2b(char[] chars, int offset){
        return LOOKUP_2[chars[offset]-'A'][chars[offset+1]-'A'];
    }
    private static int encode2(char[] chars, int offset){
        return 26 * (chars[offset]- 'A') + (chars[offset+1]-'A');
    }
}
