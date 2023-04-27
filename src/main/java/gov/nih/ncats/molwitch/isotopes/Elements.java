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

package gov.nih.ncats.molwitch.isotopes;

import java.util.HashMap;
import java.util.Map;

public class Elements {
    private static String[] elements = new String[119];

    private static Map<String, Integer> SymbolMap;
    static{
        elements[1] = "H";
        elements[2] = "He";
        elements[3] = "Li";
        elements[4] = "Be";
        elements[5] = "B";
        elements[6] = "C";
        elements[7] = "N";
        elements[8] = "O";
        elements[9] = "F";
        elements[10] = "Ne";
        elements[11] = "Na";
        elements[12] = "Mg";
        elements[13] = "Al";
        elements[14] = "Si";
        elements[15] = "P";
        elements[16] = "S";
        elements[17] = "Cl";
        elements[18] = "Ar";
        elements[19] = "K";
        elements[20] = "Ca";
        elements[21] = "Sc";
        elements[22] = "Ti";
        elements[23] = "V";
        elements[24] = "Cr";
        elements[25] = "Mn";
        elements[26] = "Fe";
        elements[27] = "Co";
        elements[28] = "Ni";
        elements[29] = "Cu";
        elements[30] = "Zn";
        elements[31] = "Ga";
        elements[32] = "Ge";
        elements[33] = "As";
        elements[34] = "Se";
        elements[35] = "Br";
        elements[36] = "Kr";
        elements[37] = "Rb";
        elements[38] = "Sr";
        elements[39] = "Y";
        elements[40] = "Zr";
        elements[41] = "Nb";
        elements[42] = "Mo";
        elements[43] = "Tc";
        elements[44] = "Ru";
        elements[45] = "Rh";
        elements[46] = "Pd";
        elements[47] = "Ag";
        elements[48] = "Cd";
        elements[49] = "In";
        elements[50] = "Sn";
        elements[51] = "Sb";
        elements[52] = "Te";
        elements[53] = "I";
        elements[54] = "Xe";
        elements[55] = "Cs";
        elements[56] = "Ba";
        elements[57] = "La";
        elements[58] = "Ce";
        elements[59] = "Pr";
        elements[60] = "Nd";
        elements[61] = "Pm";
        elements[62] = "Sm";
        elements[63] = "Eu";
        elements[64] = "Gd";
        elements[65] = "Tb";
        elements[66] = "Dy";
        elements[67] = "Ho";
        elements[68] = "Er";
        elements[69] = "Tm";
        elements[70] = "Yb";
        elements[71] = "Lu";
        elements[72] = "Hf";
        elements[73] = "Ta";
        elements[74] = "W";
        elements[75] = "Re";
        elements[76] = "Os";
        elements[77] = "Ir";
        elements[78] = "Pt";
        elements[79] = "Au";
        elements[80] = "Hg";
        elements[81] = "Tl";
        elements[82] = "Pb";
        elements[83] = "Bi";
        elements[84] = "Po";
        elements[85] = "At";
        elements[86] = "Rn";
        elements[87] = "Fr";
        elements[88] = "Ra";
        elements[89] = "Ac";
        elements[90] = "Th";
        elements[91] = "Pa";
        elements[92] = "U";
        elements[93] = "Np";
        elements[94] = "Pu";
        elements[95] = "Am";
        elements[96] = "Cm";
        elements[97] = "Bk";
        elements[98] = "Cf";
        elements[99] = "Es";
        elements[100] = "Fm";
        elements[101] = "Md";
        elements[102] = "No";
        elements[103] = "Lr";
        elements[104] = "Rf";
        elements[105] = "Db";
        elements[106] = "Sg";
        elements[107] = "Bh";
        elements[108] = "Hs";
        elements[109] = "Mt";
        elements[110] = "Ds";
        elements[111] = "Rg";
        elements[112] = "Cn";
        elements[113] = "Nh";
        elements[114] = "Fl";
        elements[115] = "Mc";
        elements[116] = "Lv";
        elements[117] = "Ts";
        elements[118] = "Og";

        SymbolMap = new HashMap<>();
        for(int i=1; i< elements.length; i++){
            SymbolMap.put(elements[i], i);
        }

    }

    public static boolean isElementSymbol(String s){
        return SymbolMap.containsKey(s);
    }
    public static String getSymbolByAtomicNumber(int atomicNumber){
        return elements[atomicNumber];
    }

    public static boolean isMetal(int atomicNumber){
        return ElementData.isMetal(atomicNumber);
    }
}
