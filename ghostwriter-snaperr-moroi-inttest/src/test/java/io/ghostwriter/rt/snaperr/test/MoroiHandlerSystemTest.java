package io.ghostwriter.rt.snaperr.test;

import org.junit.Test;
import io.ghostwriter.GhostWriter;

/**
 * Created by pal on 3/29/17.
 */
public class MoroiHandlerSystemTest {

    
    static {
	System.setProperty("io.ghostwriter.ghostwriter-snaperr-moroi-inttest.config_file", "classpath://META-INF/ghostwriter_config.properties");	
    }

	@Test
	public void testMoroiAccess() {

	int param1 = 1;
	GhostWriter.entering(this, "test", "param1", param1);
	
	int var1 = 1;
	GhostWriter.valueChange(this, "test", "var1", var1);
	
	GhostWriter.onError(this, "test", new RuntimeException("Intended exception thrown at test"));

	}

}
