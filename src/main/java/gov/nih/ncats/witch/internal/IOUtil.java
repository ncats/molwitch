package gov.nih.ncats.witch.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;

public final class IOUtil {

	private IOUtil(){
		//can not instantiate
	}
	public static void mkdirs(File dir) throws IOException{
		if(dir ==null){
			return;
		}
		//use new Java 7 method
		//which will throw a meaningful IOException if there are permission or file problems
		Files.createDirectories(dir.toPath());
	}
	
	
	
}
