package gov.nih.ncats.witch.fingerprint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import gov.nih.ncats.witch.spi.FingerprinterImpl;
/**
 * Lookup for the different {@link Fingerprint}
 * implementations available to use.
 * 
 * @author katzelda
 *
 */
public class Fingerprinters {

	private static ThreadLocal<ServiceLoader<FingerprinterImpl>> loader = ThreadLocal.withInitial(() ->ServiceLoader.load(FingerprinterImpl.class));
	 
	private static Fingerprinter defaultImpl;
	
	static{
		for(FingerprinterImpl f : loader.get()){
			if(f.isDefault()){
				defaultImpl = f.createDefaultFingerprinter();
				break;
			}
		}
	}
	
		public static class PathBasedSpecification implements FingerprintSpecification{
			public static final int DEFAULT_LENGTH =1024;
			public static final int DEFAULT_DEPTH =6;
			
			private int length = DEFAULT_LENGTH;
			private int depth = DEFAULT_DEPTH;
			
			public int getDepth() {
				return depth;
			}
			public PathBasedSpecification setDepth(int depth) {
				this.depth = depth;
				return this;
			}
			public int getLength() {
				return length;
			}
			public PathBasedSpecification setLength(int length) {
				this.length = length;
				return this;
			}
			@Override
			public String name() {
				return PATH_BASED.name();
			}
		}
		public static abstract class AbstractCfpOptions<T extends AbstractCfpOptions<?>> implements FingerprintSpecification{
			public static int DEFAULT_DIAMETER = 6;
			public static int DEFAULT_BITLENGTH = 1024;
			
			private int diameter = DEFAULT_DIAMETER;
			private int bitLength = DEFAULT_BITLENGTH;
			
			protected abstract T getThis();
			public T setDiameter(int diameter){
				if(diameter < 2 || diameter > 6){
					throw new IllegalArgumentException("diameter must be between 2 and 6");
				}
				this.diameter = diameter;
				return getThis();
			}
			
			public T setRadius(int radius){
				return setDiameter(radius *2);
			}
			
			
			public int getBitLength() {
				return bitLength;
			}
			public T setBitLength(int bitLength) {
				this.bitLength = bitLength;
				return getThis();
			}
			

			public int getDiameter() {
				return diameter;
			}
		}
		public static class EcfpSpecification extends AbstractCfpOptions<EcfpSpecification>{
			
			public EcfpSpecification(){
				//use defaults
			}
			public EcfpSpecification(int diameter) {
				 setDiameter(diameter);
			}

			
			@Override
			protected EcfpSpecification getThis() {
				return this;
			}
			@Override
			public String name() {
				return ECFP.name();
			}

			
			
		}
		
		public static class FcfpSpecification extends AbstractCfpOptions<FcfpSpecification>{
			
			public FcfpSpecification(){
				//use defaults
			}
			public FcfpSpecification(int diameter) {
				 setDiameter(diameter);
			}

			
			@Override
			protected FcfpSpecification getThis() {
				return this;
			}
			@Override
			public String name() {
				return FCFP.name();
			}

			
			
		}
	

	/**
	 * Marker interface for describing the specification
	 * for how a fingerprint should be computed.
	 * 
	 * @author katzelda
	 *
	 */
	public interface FingerprintSpecification{
		/**
		 * The name of the fingerprint algorithm.
		 * @return The name of the algorithm as a String.
		 */
		public String name();
		

		public static interface PATH_BASED {
			public static PathBasedSpecification create(){
				return new PathBasedSpecification();
			}
			
			public static String name() {
				return "PATH_BASED";
			}
		}
		public static interface ECFP{
			public static EcfpSpecification create() {
				return new EcfpSpecification();
			}
			
			public static String name() {
				return "ECFP";
			}
		}
		public static interface FCFP{
			public static FcfpSpecification withRadius(int radius){
				return new FcfpSpecification().setRadius(radius);
			}
			public static FcfpSpecification withDiameter(int diameter){
				return new FcfpSpecification(diameter);
			}
			public static String name() {
				return "ECFP";
			}
		}
	}
	
	
	public static Stream<String> getSupportedAlgorithms(){
		Iterator<FingerprinterImpl> iter = loader.get().iterator();
		List<String> list = new ArrayList<>();
		while(iter.hasNext()) {
			FingerprinterImpl current = iter.next();
			list.addAll(current.getSupportedAlgorithmNames());
		}
		return list.stream().filter(Objects::nonNull);
	}
	/**
	 * Get the {@link Fingerprint} with the given fingerprint algorithm name.
	 * @param fingerPrinterOptions specification for the finger printer algorithm to use.
	 * @return the {@link Fingerprinter} implementation that supports that algorithm
	 * or {@code null} if no {@link Fingerprinter} is found that supports it.
	 */
	public static Fingerprinter getFingerprinter(FingerprintSpecification fingerPrinterOptions){
		Iterator<FingerprinterImpl> iter = loader.get().iterator();
		FingerprinterImpl found=null;
		while(found ==null && iter.hasNext()){
			FingerprinterImpl current = iter.next();
			if(current.supports(fingerPrinterOptions)){
				found= current;
			}
		}
		if(found ==null){
			return null;
		}
		return found.createFingerPrinterFor(fingerPrinterOptions);
	}
	
	/**
	 * Get the default {@link Fingerprint} implementation.
	 * @return the default {@link Fingerprinter}.
	 *
	 */
	public static Fingerprinter getDefault() {
		return defaultImpl;
	}
}
