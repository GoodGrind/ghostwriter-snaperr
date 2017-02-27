package io.ghostwriter.rt.snaperr.test;


import org.junit.Test;

import io.ghostwriter.GhostWriter;

public class JsonSlf4jTest {
    
    static {
	System.setProperty("io.ghostwriter.ghostwriter-snaperr-inttest.config_file", "classpath://META-INF/ghostwriter_config.properties");	
    }

    @Test
    public void test() {
	
	int param1 = 1;
	GhostWriter.entering(this, "test", "param1", param1);
	
	int var1 = 1;
	GhostWriter.valueChange(this, "test", "var1", var1);
	
	GhostWriter.onError(this, "test", new RuntimeException("Intended exception thrown at test"));
    }
    	
}
