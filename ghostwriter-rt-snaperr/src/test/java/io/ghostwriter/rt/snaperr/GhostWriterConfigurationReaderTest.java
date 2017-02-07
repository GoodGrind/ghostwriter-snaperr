package io.ghostwriter.rt.snaperr;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.Properties;

/**
 * Testing {@link ConfigurationReader}
 * @author pal
 *
 */
public class GhostWriterConfigurationReaderTest {

    private static final String GW_TEST_APP = "gwTestApp";

    @Test
    public void find_the_gw_configuration_file_Test() {
        ConfigurationReader cfg = new ConfigurationReader();

        String findGwConfigFile = cfg.findGwConfigFile();

        Assert.assertNotNull("Finding GW application name config file on classpath", findGwConfigFile);
    }

    @Test
    public void get_gw_app_name_from_config_file_Test() {
        ConfigurationReader cfg = new ConfigurationReader();

        String gwAppName = cfg.getGwAppName();

        Assert.assertEquals("Application name is fetched and equals to expected", GW_TEST_APP, gwAppName);
    }

    @Test
    public void get_gw_properties_with_app_name_Test1() {
        System.setProperty(ConfigurationReader.CFG_GW_PROPERTY_NAME_PREFIX + GW_TEST_APP + "."
                + ConfigurationReader.CFG_CONFIG_FILE, "systemProperty");
        System.setProperty(ConfigurationReader.CFG_GW_PROPERTY_NAME_PREFIX + "one", "one");
        System.setProperty(ConfigurationReader.CFG_GW_PROPERTY_NAME_PREFIX + "two", "two");
        System.setProperty(ConfigurationReader.CFG_GW_PROPERTY_NAME_PREFIX + GW_TEST_APP + ".gwOne",
                "gwOne");
        System.setProperty(ConfigurationReader.CFG_GW_PROPERTY_NAME_PREFIX + GW_TEST_APP + ".gwTwo",
                "gwTwo");

        ConfigurationReader cfg = new ConfigurationReader();
        String gwAppName = cfg.getGwAppName();
        Properties gwProperties = cfg.getGwProperties(gwAppName);

        int propCount = gwProperties.size();

        Assert.assertEquals("Only the expected number of properties are found", 3, propCount);

        Assert.assertEquals("gwOne Property found", "gwOne", gwProperties.getProperty("gwOne"));
        Assert.assertEquals("gwTwo Property found", "gwTwo", gwProperties.getProperty("gwTwo"));
        Assert.assertEquals(
                ConfigurationReader.CFG_GW_PROPERTY_NAME_PREFIX + GW_TEST_APP + "."
                        + ConfigurationReader.CFG_CONFIG_FILE + " Property found",
                "systemProperty", gwProperties.getProperty(ConfigurationReader.CFG_CONFIG_FILE));
    }

    @Test
    public void get_gw_properties_with_app_name_from_classpath_Test() {
        System.setProperty(
                ConfigurationReader.CFG_GW_PROPERTY_NAME_PREFIX + GW_TEST_APP + "."
                        + ConfigurationReader.CFG_CONFIG_FILE,
                "classpath://META-INF/ghostwriter_config.properties");

        ConfigurationReader cfg = new ConfigurationReader();
        String gwAppName = cfg.getGwAppName();
        Properties gwProperties = cfg.getGwProperties(gwAppName);

        int propCount = gwProperties.size();

        Assert.assertEquals("Only the expected number of properties are found", 3, propCount);

        Assert.assertEquals("gwOne Property found", "gwOne", gwProperties.getProperty("gwOne"));
        Assert.assertEquals("gwTwo Property found", "gwTwo", gwProperties.getProperty("gwTwo"));
        Assert.assertEquals(
                ConfigurationReader.CFG_GW_PROPERTY_NAME_PREFIX + GW_TEST_APP + "."
                        + ConfigurationReader.CFG_CONFIG_FILE + " Property found",
                "classpath://META-INF/ghostwriter_config.properties",
                gwProperties.getProperty(ConfigurationReader.CFG_CONFIG_FILE));
    }

    @Test
    public void get_gw_properties_with_app_name_from_file_Test() {
        URL configResource = Thread.currentThread().getContextClassLoader().getResource("META-INF/ghostwriter_config.properties");
        String configFilePath = configResource.getPath();
        System.setProperty(
                ConfigurationReader.CFG_GW_PROPERTY_NAME_PREFIX + GW_TEST_APP + "."
                        + ConfigurationReader.CFG_CONFIG_FILE,
                "file://" + configFilePath);

        ConfigurationReader cfg = new ConfigurationReader();
        String gwAppName = cfg.getGwAppName();
        Properties gwProperties = cfg.getGwProperties(gwAppName);

        int propCount = gwProperties.size();

        Assert.assertEquals("Only the expected number of properties are found", 3, propCount);

        Assert.assertEquals("gwOne Property found", "gwOne", gwProperties.getProperty("gwOne"));
        Assert.assertEquals("gwTwo Property found", "gwTwo", gwProperties.getProperty("gwTwo"));
        Assert.assertEquals(
                ConfigurationReader.CFG_GW_PROPERTY_NAME_PREFIX + GW_TEST_APP + "."
                        + ConfigurationReader.CFG_CONFIG_FILE + " Property found",
                "file://" + configFilePath,
                gwProperties.getProperty(ConfigurationReader.CFG_CONFIG_FILE));
    }
}