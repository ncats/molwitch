/*
 * NCATS-WITCH
 *
 * Copyright 2019 NIH/NCATS
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

package gov.nih.ncats.witch.io;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public interface ChemFormat {

	interface ChemFormatWriterSpecification {
		

		
		String getFormatName();
		
		HydrogenEncoding getHydrogenEncoding();
	}
	
	interface AromaticAwareChemFormatWriterSpecification extends ChemFormatWriterSpecification{
		KekulizationEncoding getKekulization();
	}
	
	public enum HydrogenEncoding{
		MAKE_EXPLICIT,
		MAKE_IMPLICIT,
		AS_IS;
	}
	public enum KekulizationEncoding{
		KEKULE,
		FORCE_AROMATIC
	}
	public String getName();
	
	public static class SdfFormatSpecification implements ChemFormatWriterSpecification{
		public static final String NAME = "sdf";
		private MolFormatSpecification molSpec = new MolFormatSpecification();
		
		@Override
		public String getFormatName() {
			return NAME;
		}

		@Override
		public HydrogenEncoding getHydrogenEncoding() {
			return molSpec.getHydrogenEncoding();
		}

		public MolFormatSpecification getMolSpec() {
			return molSpec;
		}

		public void setMolSpec(MolFormatSpecification molSpec) {
			this.molSpec = molSpec;
		}
		
	}
	
	public static class MolFormatSpecification implements AromaticAwareChemFormatWriterSpecification{
		public static final String NAME = "mol";
		private HydrogenEncoding hydrogenEncoding = HydrogenEncoding.AS_IS; 
		private Version version = Version.V2000;
		private KekulizationEncoding kekulization = KekulizationEncoding.KEKULE;
		private CoordinateOptions coordinateOptions = CoordinateOptions.AS_IS;
		
		public enum CoordinateOptions{
			AS_IS,
			FORCE_3D,
			FORCE_2D
		}
		public enum Version{
			V2000,
			V3000
			;
			private static Pattern VERSION_PATTERN = Pattern.compile("^[v|V]?(\\d+)$");
			public static Version parse(int version) {
				if(version == 3000) {
					return Version.V3000;
				}
				if(version == 2000) {
					return Version.V2000;
				}
				throw new IllegalArgumentException("unknown version '"+ version +"'");
			}
			public static Version parse(String version) {
				Matcher m = VERSION_PATTERN.matcher(version);
				if(m.find()) {
					return parse(Integer.parseInt(m.group(1)));
				}
				throw new IllegalArgumentException("unknown version '"+ version +"'");
			}
			
		}
		public MolFormatSpecification() {
			
		}
		
		
		public CoordinateOptions getCoordinateOptions() {
			return coordinateOptions;
		}


		public MolFormatSpecification setCoordinateOptions(CoordinateOptions coordinateOptions) {
			this.coordinateOptions = coordinateOptions;
			return this;
		}


		public MolFormatSpecification(Version version) {
			this.version = Objects.requireNonNull(version);
		}

		@Override
		public String getFormatName() {
			return NAME;
		}

		@Override
		public HydrogenEncoding getHydrogenEncoding() {
			return hydrogenEncoding;
		}

		public Version getVersion() {
			return version;
		}

		public MolFormatSpecification setVersion(Version version) {
			this.version = Objects.requireNonNull(version);
			return this;
		}

		public MolFormatSpecification setHydrogenEncoding(HydrogenEncoding hydrogenEncoding) {
			this.hydrogenEncoding = Objects.requireNonNull(hydrogenEncoding);
			return this;
		}
		@Override
		public KekulizationEncoding getKekulization() {
			return kekulization;
		}
		public MolFormatSpecification setKekulization(KekulizationEncoding kekulization) {
			this.kekulization = Objects.requireNonNull(kekulization);
			return this;
		}


		@Override
		public String toString() {
			return "MolFormatSpecification [hydrogenEncoding=" + hydrogenEncoding + ", version=" + version
					+ ", kekulization=" + kekulization + ", coordinateOptions=" + coordinateOptions + "]";
		}
		
		
		
	}
	
	public static class SmilesFormatWriterSpecification implements AromaticAwareChemFormatWriterSpecification{
		public static final String NAME = "SMILES";
		
		public enum StereoEncoding{
			INCLUDE_STEREO,
			EXCLUDE_STEREO;
		}
		public enum CanonicalizationEncoding{
			CANONICAL,
			NON_CANONICAL;
		}
		private StereoEncoding encodeStereo = StereoEncoding.INCLUDE_STEREO;
		private HydrogenEncoding hydrogenEncoding = HydrogenEncoding.AS_IS; 
		private KekulizationEncoding kekulization = KekulizationEncoding.FORCE_AROMATIC;
		private CanonicalizationEncoding canonization = CanonicalizationEncoding.NON_CANONICAL;
		
		
		public StereoEncoding getEncodeStereo() {
			return encodeStereo;
		}
		public SmilesFormatWriterSpecification setEncodeStereo(StereoEncoding encodeStereo) {
			this.encodeStereo = encodeStereo;
			return this;
		}
		public HydrogenEncoding getHydrogenEncoding() {
			return hydrogenEncoding;
		}
		public SmilesFormatWriterSpecification setHydrogenEncoding(HydrogenEncoding hydrogenEncoding) {
			this.hydrogenEncoding = hydrogenEncoding;
			return this;
		}
		@Override
		public KekulizationEncoding getKekulization() {
			return kekulization;
		}
		public SmilesFormatWriterSpecification setKekulization(KekulizationEncoding kekulization) {
			this.kekulization = kekulization;
			return this;
		}
		public CanonicalizationEncoding getCanonization() {
			return canonization;
		}
		public SmilesFormatWriterSpecification setCanonization(CanonicalizationEncoding canonization) {
			this.canonization = canonization;
			return this;
		}
		@Override
		public String getFormatName() {
			return SmilesChemFormat.name();
		}
		@Override
		public String toString() {
			return "SmilesFormatWriterSpecification [encodeStereo=" + encodeStereo + ", hydrogenEncoding="
					+ hydrogenEncoding + ", kekulization=" + kekulization + ", canonization=" + canonization + "]";
		}
	
		
		
	}
	public static interface SmilesChemFormat{

		public static String name() {
			return "SMILES";
		}
		

		public static SmilesFormatWriterSpecification createOptions() {
			return new SmilesFormatWriterSpecification();
		}

		
	}
}
