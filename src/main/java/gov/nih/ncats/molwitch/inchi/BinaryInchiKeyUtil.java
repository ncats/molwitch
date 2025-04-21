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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

final class BinaryInchiKeyUtil {
    static final BigInteger SHIFT = BigInteger.valueOf(1).shiftLeft(48);
    static final BigInteger MASK = new BigInteger("FFFFFFFFFFFFFFFFFFFF000000000000", 16);

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
    private BinaryInchiKeyUtil(){
        //can not instantiate
    }
    static BigInteger encode(String inchiKey){
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
    static short encode3b(char[] chars, int offset){
        return LOOKUP_3[chars[offset]-'A'][chars[offset+1]-'A'][chars[offset+2]-'A'];
    }
    static int encode3(char[] chars, int offset){
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

    static short encode2b(char[] chars, int offset){
        return LOOKUP_2[chars[offset]-'A'][chars[offset+1]-'A'];
    }
    static int encode2(char[] chars, int offset){
        return 26 * (chars[offset]- 'A') + (chars[offset+1]-'A');
    }

    static BigInteger[] encodeInsensitive(String inchiKey) {

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
}
