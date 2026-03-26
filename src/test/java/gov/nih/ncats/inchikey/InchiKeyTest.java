package gov.nih.ncats.inchikey;

import gov.nih.ncats.molwitch.inchi.InChiResult;
import gov.nih.ncats.molwitch.inchi.Inchi;
import gov.nih.ncats.molwitch.inchi.InchiKey;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.junit.Assert.*;

public class InchiKeyTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void nullConstructorThrowsNull(){
        expectedException.expect(NullPointerException.class);
        new InchiKey(null);
    }

    @Test
    public void notValidInchiKeyShouldThrowIllegalArg(){
        System.out.println("hello from test");
        expectedException.expect(IllegalArgumentException.class);
        new InchiKey("not valid inchi key");
    }
    @Test
    public void toStringIsFullKey(){
        String key = "GLDQAMYCGOIJDV-UHFFFAOYSA-N";
        InchiKey k1 = new InchiKey(key);

        assertEquals(key, k1.toString());
    }

    @Test
    public void getters(){
        String key = "GLDQAMYCGOIJDV-UHFFFAOYSA-N";
        InchiKey k1 = new InchiKey(key);

        assertEquals("GLDQAMYCGOIJDV", k1.getConnectivityLayer());
        assertTrue(k1.isStandard());
        assertFalse(k1.hasStereo());
        assertEquals(0, k1.getProtonation());
        assertEquals(1, k1.getVersion());
    }
    @Test
    public void sameKeysAreEqual(){
        String key = "GLDQAMYCGOIJDV-UHFFFAOYSA-N";
        InchiKey k1 = new InchiKey(key);
        InchiKey k2 = new InchiKey(key);

        assertEquals(k1, k2);
    }

    @Test
    public void withStereo(){
        InchiKey k1 = new InchiKey("AAAACIDXVXFWLZ-IIZQIQNSSA-N");
        assertTrue(k1.hasStereo());
    }

    @Test
    public void sameConnectivity(){
        InchiKey k1 = new InchiKey("AAAACIDXVXFWLZ-UHFFFAOYSA-N");
        InchiKey k2 = new InchiKey("AAAACIDXVXFWLZ-IIZQIQNSSA-N");

        assertNotEquals(k1, k2);
        assertTrue(k1.hasSameConnectivity(k2));
        assertTrue(k2.hasSameConnectivity(k1));

        assertFalse(k1.hasStereo());
        assertTrue(k2.hasStereo());
    }

    @Test
    public void differentConnectivity(){
        InchiKey k1 = new InchiKey("AAAACIDXVXFWLZ-UHFFFAOYSA-N");
        InchiKey k2 = new InchiKey("GLDQAMYCGOIJDV-UHFFFAOYSA-N");

        assertFalse(k1.hasSameConnectivity(k2));
        assertFalse(k2.hasSameConnectivity(k1));
    }

    @Test
    public void fromMol() throws IOException {
        String molfile1="\n" +
                "  ACCLDraw01052215342D\n" +
                "\n" +
                "  5  4  0  0  0  0  0  0  0  0999 V2000\n" +
                "    4.8750   -6.7188    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    5.8979   -6.1282    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    5.8979   -4.9467    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    6.9210   -4.3560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "    4.8750   -7.9002    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "  2  3  1  0  0  0  0\n" +
                "  3  4  1  0  0  0  0\n" +
                "  1  5  1  0  0  0  0\n" +
                "M  END\n";

        InChiResult inChiResult = Inchi.computeInchiFromMol(molfile1);
        System.out.println(inChiResult.getInchi());
        InchiKey inchiKey = inChiResult.getInchiKey().get();
        assertEquals("RTZKZFJDLAIYFH-UHFFFAOYSA-N", inchiKey.toString());
    }
}
