package io.ghostwriter.rt.snaperr;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Reads the runtime configuration values of GhostWriter for the instrumented application.
 * Please read {@link #getGwProperties(String)} for further insight.
 *
 * @author pal
 */
public class ConfigurationReader {

    public static final String CFG_MOROI_NO_HOSTNAME_VERIFICATION = "moroi.no_ssl_hostname_verification";
    static final String CONFIG_FILE_PROTOCOL_SYSTEM_PROPERTY = "systemProperty";
    static final String CONFIG_FILE_PROTOCOL_CLASSPATH = "classpath://";
    static final String CONFIG_FILE_PROTOCOL_FILE = "file://";
    static final String CFG_GW_PROPERTY_NAME_PREFIX = "io.ghostwriter.";
    static final String CFG_GW_APP_NAME = CFG_GW_PROPERTY_NAME_PREFIX + "app_name";
    public static final String CFG_MOROI_APP_UUID = "moroi.application_uuid";
    /* Below are the available Snaperr config properties */
    static final String CFG_CONFIG_FILE = "config_file";
    public static final String CFG_MOROI_ERROR_URL = "moroi.error_url";
    static final String CFG_THROTTLE_WINDOW_SIZE = "throttle.window_size";
    static final String CFG_THROTTLE_MAX_ERRORS = "throttle.max_errors_in_window";
    private static final String[] GW_APP_CFG_FILE_PATHS = {"io.ghostwriter.application",
            "META-INF/io.ghostwriter.application", "WEB-INF/io.ghostwriter.application"};
    private final Logger LOG = Logger.getLogger(GhostWriterSnaperrProvider.class.getName());

    /**
     * Tries to find the application name in either of the {@link #GW_APP_CFG_FILE_PATHS}.
     * If the above file is found and an application name is successfully read, then
     * all further ghostwriter properties need to be prefixed such as <i>io.ghostwriter.appName</i>.
     * Otherwise the ghostwriter configuration properties are prefixed by <i>io.ghostwriter</i>
     * <p>A <i>io.ghostwriter[.appName].config_file</i> system property (jvm property) must be present.
     * It's value must be specified either of the following ways:
     * <li><i>file://</i> e.g. file:///path/to/ghostwriter.properties,
     * <li><i>classpath://</i> e.g. classpath://META-INF/ghostwriter.properties,
     * <li><i>systemProperty</i> exact.
     * <p>Based on the value of <i>io.ghostwriter[.appName].config_file</i> system property,
     * following Ghostwriter properties will be read from the corresponding source. Each
     * property name must include the application name, if it was specified.
     *
     * @param gwAppName name of the ghostwriter application name. Either empty string or returned by {@link #getGwAppName()}
     * @return All the GhostWriter runtime parameters without the <i>'io.ghostwriter[.appName].'</i> prefix i.e.
     * <i>'io.ghostwriter.myApplication.moroi.error_url'</i> will become <i>'moroi.error_url'</i>
     */
    public Properties getGwProperties(final String gwAppName) {
        final String propertyNamePrefix = getGwPropertyNamePrefix(gwAppName);
        final String configFileSysPropName = getGwConfigFilePropertyName(propertyNamePrefix);

        final String gwConfigConfigFilePath = getGwConfigFilePath(configFileSysPropName);

        if (gwConfigConfigFilePath == null) {
            throw new ConfigurationException("Please set system property (JVM property) '" + configFileSysPropName
                    + "' to point to the GhostWriter configuration file");
        }

        Properties gwConfigFileProperties = null;
        try {
	    if (gwConfigConfigFilePath.startsWith(CONFIG_FILE_PROTOCOL_FILE)) {
		gwConfigFileProperties = readFromFile(configFileSysPropName, gwConfigConfigFilePath);
	    }
	    else if (gwConfigConfigFilePath.startsWith(CONFIG_FILE_PROTOCOL_CLASSPATH)) {
		gwConfigFileProperties = readFromClassPath(configFileSysPropName, gwConfigConfigFilePath);
	    }
	    else if (gwConfigConfigFilePath.startsWith(CONFIG_FILE_PROTOCOL_SYSTEM_PROPERTY)) {
		gwConfigFileProperties = readFromSystemProperty(configFileSysPropName, gwConfigConfigFilePath);
	    }
	    else {
		throw new ConfigurationException("Unrecogized value for system property '" + configFileSysPropName
			+ "', please set either:" + "file:///path/to/file, classpath://META-INF/path, systemProperty");
	    }
	}
	catch (IOException e) {
	    throw new ConfigurationException("The " + gwConfigConfigFilePath + " does not exist or is not readable."
		    + " The file is set in system property '" + configFileSysPropName + "'");
	}

        LOG.debug("Reading configuration file '" + gwConfigConfigFilePath + "'");
        Properties result = filterGwProperties(propertyNamePrefix, gwConfigFileProperties);

        return result;
    }

    String getGwConfigFilePath(String configFileSysPropName) {
        Properties jvmProperties = System.getProperties();
        String configFilePath = jvmProperties.getProperty(configFileSysPropName);
        return configFilePath;
    }

    String getGwConfigFilePropertyName(final String propertyNamePrefix) {
        return propertyNamePrefix + CFG_CONFIG_FILE;
    }

    /**
     * @param gwAppName
     * @return <li> <i>'io.ghostwriter.'</i> if param <i>gwAppName</i> is empty
     * <li> <i>'io.ghostwriter.gwAppName'</i> if param <i>gwAppName</i> is not empty
     */
    String getGwPropertyNamePrefix(String gwAppName) {
        return "".equals(gwAppName) ? CFG_GW_PROPERTY_NAME_PREFIX
                : CFG_GW_PROPERTY_NAME_PREFIX + gwAppName + ".";
    }


    /**
     * @param propertyNamePrefix as returned by {@link #getGwPropertyNamePrefix(String)}
     * @param gwPropertiesFile   as specified in <i>'io.ghostwriter[.appName].config_file'</i> jvm property
     * @return all the properties that start with <i>'io.ghostwriter[.appName].'</i>
     */
    Properties filterGwProperties(final String propertyNamePrefix, Properties gwPropertiesFile) {
        Properties result = new Properties();

	/*
     * We cannot use Hashtable.entrySet() because that does not contain the
	 * properties... curse of Properties class, confusion between
	 * composition and inheritance
	 */
        @SuppressWarnings("unchecked")
        Enumeration<String> propertyNames = (Enumeration<String>) gwPropertiesFile.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String key = propertyNames.nextElement();
            String value = gwPropertiesFile.getProperty(key);

            if (!key.startsWith(propertyNamePrefix)) {
                LOG.debug("Skipping configuration value named '" + key + "' because it does not start with '"
                        + propertyNamePrefix + "'");
                continue;
            }

            String subKey = key.substring((propertyNamePrefix).length());

            LOG.info("Reading configuaration '" + key + "=" + value + "'");
            result.setProperty(subKey, value);
        }

        return result;
    }

    Properties readFromFile(final String configFileSysProp, final String gwConfigPropertyValue) throws IOException {
        final Properties gwProperties = new Properties();
        final String gwConfigFilePath = gwConfigPropertyValue.replace(CONFIG_FILE_PROTOCOL_FILE, "");

        final InputStream gwConfigFileIS = new FileInputStream(gwConfigFilePath);

        gwProperties.load(gwConfigFileIS);
        gwProperties.setProperty(configFileSysProp, gwConfigPropertyValue);

        return gwProperties;
    }

    Properties readFromClassPath(final String configFileSysProp, final String gwConfigPropertyValue)
            throws IOException {
        final Properties gwProperties = new Properties();
        final String gwConfigFilePath = gwConfigPropertyValue.replace(CONFIG_FILE_PROTOCOL_CLASSPATH, "");

        final InputStream gwConfigFileIS = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(gwConfigFilePath);

        gwProperties.load(gwConfigFileIS);
        gwProperties.setProperty(configFileSysProp, gwConfigPropertyValue);

        return gwProperties;
    }

    Properties readFromSystemProperty(final String configFileSysProp, final String gwConfigPropertyValue) {
        Properties systemProperties = System.getProperties();
        final Properties gwProperties = new Properties(systemProperties);
        return gwProperties;
    }

    public String getGwAppName() {
        String appConfigFileName = findGwConfigFile();

        if (appConfigFileName == null) {
            StringBuilder paths = new StringBuilder();
            for (String cfgFilePath : GW_APP_CFG_FILE_PATHS) {
                paths.append(cfgFilePath).append(", ");
            }

            LOG.info("There is no GhostWriter application name config file present on the classpath."
                    + "Expected configuration names will be without application names: io.ghostwriter[.appName].moroi.error_url "
                    + "If you wish to deploy multiple applications in an application server, create a file named "
                    + GW_APP_CFG_FILE_PATHS + " under the following paths with the content '" + CFG_GW_APP_NAME
                    + "=yourAppName'. Paths: " + paths.toString());
            return "";
        }

        InputStream appConfigFileIS = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(appConfigFileName);

        Properties gwAppConfigFileProp = new Properties();
        try {
            gwAppConfigFileProp.load(appConfigFileIS);
            String gwAppName = gwAppConfigFileProp.getProperty(CFG_GW_APP_NAME);

            LOG.info("GhostWriter application name '" + gwAppName + "' is found in file: " + appConfigFileName);

            return gwAppName;
        } catch (IOException e) {
            throw new ConfigurationException("Cannot read configuration file on classpath: " + appConfigFileName, e);
        }
    }

    String findGwConfigFile() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for (int i = 0; i < GW_APP_CFG_FILE_PATHS.length; i++) {
            URL path = classLoader.getResource(GW_APP_CFG_FILE_PATHS[i]);
            if (path != null) {
                LOG.debug("Found GW Application name config file at: " + path);
                return GW_APP_CFG_FILE_PATHS[i];
            }
        }

        return null;
    }

}
