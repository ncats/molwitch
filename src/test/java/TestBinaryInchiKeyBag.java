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

import gov.nih.ncats.molwitch.inchi.BinaryInchiKey;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;
import static org.junit.Assert.*;
public class TestBinaryInchiKeyBag {

    private static BinaryInchiKey.BinaryInchiKeyBag sut;

    @BeforeClass
    public static void setup(){
        TreeSet<String> set = new TreeSet<>();
        /*
        GLDQAMYCGOIJDV-UHFFFAOYSA-N
QZDSXQJWBGMRLU-UHFFFAOYSA-N
VUQLHQFKACOHNZ-UHFFFAOYSA-N
NMDWGEGFJUBKLB-UHFFFAOYSA-N
         */
        set.add("GLDQAMYCGOIJDV-UHFFFAOYSA-N");
        set.add("QZDSXQJWBGMRLU-UHFFFAOYSA-N");
        set.add("VUQLHQFKACOHNZ-UHFFFAOYSA-N");
        set.add("NMDWGEGFJUBKLB-UHFFFAOYSA-N");

        /*
        AAAACIDXVXFWLZ-IIZQIQNSSA-N
AAAACIDXVXFWLZ-NTFBLANLSA-N
AAAACIDXVXFWLZ-UHFFFAOYSA-N
AAAACIDXVXFWLZ-WIMVFMHDSA-N
AAAACIDXVXFWLZ-YGJKTCCFSA-N
         */
        set.add("AAAACIDXVXFWLZ-IIZQIQNSSA-N");
        set.add("AAAACIDXVXFWLZ-NTFBLANLSA-N");
        set.add("AAAACIDXVXFWLZ-UHFFFAOYSA-N");
//        set.add("AAAACIDXVXFWLZ-WIMVFMHDSA-N");
        set.add("AAAACIDXVXFWLZ-YGJKTCCFSA-N");
        sut = BinaryInchiKey.BinaryInchiKeyBag.fromSortedSet(set);
    }

    @Test
    public void containsStereo(){
        assertTrue(sut.containsStereo("GLDQAMYCGOIJDV-UHFFFAOYSA-N"));
        assertTrue(sut.containsStereo("QZDSXQJWBGMRLU-UHFFFAOYSA-N"));
        assertTrue(sut.containsStereo("VUQLHQFKACOHNZ-UHFFFAOYSA-N"));
        assertTrue(sut.containsStereo("NMDWGEGFJUBKLB-UHFFFAOYSA-N"));

        assertTrue(sut.containsStereo("AAAACIDXVXFWLZ-IIZQIQNSSA-N"));
        assertTrue(sut.containsStereo("AAAACIDXVXFWLZ-NTFBLANLSA-N"));
        assertTrue(sut.containsStereo("AAAACIDXVXFWLZ-UHFFFAOYSA-N"));

        assertTrue(sut.containsStereo("AAAACIDXVXFWLZ-YGJKTCCFSA-N"));
    }

    @Test
    public void containsStereoInsensitive(){
        assertTrue(sut.containsInsensitive("GLDQAMYCGOIJDV-UHFFFAOYSA-N"));
        assertTrue(sut.containsInsensitive("QZDSXQJWBGMRLU-UHFFFAOYSA-N"));
        assertTrue(sut.containsInsensitive("VUQLHQFKACOHNZ-UHFFFAOYSA-N"));
        assertTrue(sut.containsInsensitive("NMDWGEGFJUBKLB-UHFFFAOYSA-N"));

        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-IIZQIQNSSA-N"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-NTFBLANLSA-N"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-UHFFFAOYSA-N"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-WIMVFMHDSA-N"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-YGJKTCCFSA-N"));
    }

    @Test
    public void containsStereoInsensitiveProton(){
        assertTrue(sut.containsInsensitive("GLDQAMYCGOIJDV-UHFFFAOYSA-O"));
        assertTrue(sut.containsInsensitive("QZDSXQJWBGMRLU-UHFFFAOYSA-M"));
        assertTrue(sut.containsInsensitive("VUQLHQFKACOHNZ-UHFFFAOYSA-L"));
        assertTrue(sut.containsInsensitive("NMDWGEGFJUBKLB-UHFFFAOYSA-P"));

        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-IIZQIQNSSA-N"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-NTFBLANLSA-M"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-UHFFFAOYSA-O"));
        assertTrue(sut.containsInsensitive("AAAACIDXVXFWLZ-WIMVFMHDSA-P"));
    }

    @Test
    public void doesNotContainStereo(){
        assertFalse(sut.containsStereo("RDJUHLUBPADHNP-UHFFFAOYSA-N"));
        //
        assertFalse(sut.containsStereo("OVPRPPOVAXRCED-UHFFFAOYSA-N"));

        assertFalse(sut.containsStereo("AAAACIDXVXFWLZ-WIMVFMHDSA-N"));
    }
    @Test
    public void doesNotContainStereoInsensitive(){
        assertFalse(sut.containsInsensitive("RDJUHLUBPADHNP-UHFFFAOYSA-N"));
        assertFalse(sut.containsInsensitive("OVPRPPOVAXRCED-UHFFFAOYSA-N"));

    }
    @Test
    public void doesNotContainStereoInsensitiveProtonLayer(){
        assertFalse(sut.containsInsensitive("RDJUHLUBPADHNP-UHFFFAOYSA-M"));
        assertFalse(sut.containsInsensitive("OVPRPPOVAXRCED-UHFFFAOYSA-L"));
        assertFalse(sut.containsInsensitive("OVPRPPOVAXRCED-UHFFFAOYSA-O"));
        assertFalse(sut.containsInsensitive("OVPRPPOVAXRCED-UHFFFAOYSA-P"));
    }

    @Test
    public void containsCheckResult(){
        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.EXACT, sut.contains("GLDQAMYCGOIJDV-UHFFFAOYSA-N").getResultType());
        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.EXACT, sut.contains("QZDSXQJWBGMRLU-UHFFFAOYSA-N").getResultType());
        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.EXACT, sut.contains("VUQLHQFKACOHNZ-UHFFFAOYSA-N").getResultType());
        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.EXACT, sut.contains("NMDWGEGFJUBKLB-UHFFFAOYSA-N").getResultType());

        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.EXACT, sut.contains("AAAACIDXVXFWLZ-IIZQIQNSSA-N").getResultType());
        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.EXACT, sut.contains("AAAACIDXVXFWLZ-NTFBLANLSA-N").getResultType());
        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.EXACT, sut.contains("AAAACIDXVXFWLZ-UHFFFAOYSA-N").getResultType());

        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.EXACT, sut.contains("AAAACIDXVXFWLZ-YGJKTCCFSA-N").getResultType());
            //these are fake stereo parts
        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.STEREO_INSENSITIVE, sut.contains("GLDQAMYCGOIJDV-SDFDSFDFDF-N").getResultType());
        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.STEREO_INSENSITIVE, sut.contains("QZDSXQJWBGMRLU-SDFDSFDFDF-N").getResultType());
        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.STEREO_INSENSITIVE, sut.contains("VUQLHQFKACOHNZ-SDFDSFDFDF-N").getResultType());
        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.STEREO_INSENSITIVE, sut.contains("NMDWGEGFJUBKLB-SDFDSFDFDF-N").getResultType());

        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.STEREO_INSENSITIVE, sut.contains("AAAACIDXVXFWLZ-SDFDSFDFDF-N").getResultType());
       assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.STEREO_INSENSITIVE, sut.contains("AAAACIDXVXFWLZ-WIMVFMHDSA-N").getResultType());


        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.NO_MATCH, sut.contains("RDJUHLUBPADHNP-UHFFFAOYSA-N").getResultType());
        assertEquals(BinaryInchiKey.BinaryInchiKeyBag.ResultType.NO_MATCH, sut.contains("OVPRPPOVAXRCED-UHFFFAOYSA-N").getResultType());
    }


}
