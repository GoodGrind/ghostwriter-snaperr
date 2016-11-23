package io.ghostwriter.rt.snaperr;

import io.ghostwriter.TracerProvider;
import io.ghostwriter.rt.snaperr.handler.MoroiHandler;
import io.ghostwriter.rt.snaperr.serializer.JsonSerializer;
import io.ghostwriter.rt.snaperr.serializer.TriggerSerializer;
import io.ghostwriter.rt.snaperr.tracker.ReferenceTracker;
import io.ghostwriter.rt.snaperr.tracker.StackBasedReferenceTracker;
import io.ghostwriter.rt.snaperr.trigger.TriggerHandler;

import java.util.Properties;

public class GhostWriterSnaperrProvider implements TracerProvider<GhostWriterSnaperr> {

    private final Logger LOG = Logger.getLogger(GhostWriterSnaperrProvider.class.getName());

    private GhostWriterSnaperr ghostWriterSnaperr;
    private boolean gwSetupSuccessful;

    @Override
    public GhostWriterSnaperr getTracer() {
        synchronized (GhostWriterSnaperrProvider.class) {

            if (gwSetupSuccessful) {
                return ghostWriterSnaperr;
            }

            try {
                final ConfigurationReader configReader = new ConfigurationReader();

                final String gwAppName = configReader.getGwAppName();
                final Properties gwProperties = configReader.getGwProperties(gwAppName);

                final String moroiErrorUrl = gwProperties.getProperty(ConfigurationReader.CFG_MOROI_ERROR_URL);
                final String moroiAppUUID = gwProperties.getProperty(ConfigurationReader.CFG_MOROI_APP_UUID);
                final boolean noSslHostnameVerification = Boolean.parseBoolean(gwProperties.getProperty(ConfigurationReader.CFG_MOROI_NO_HOSTNAME_VERIFICATION, "false"));

                final ReferenceTracker referenceTracker = new StackBasedReferenceTracker();
                final TriggerSerializer<String> triggerSerializer = new JsonSerializer(moroiAppUUID);
                final TriggerHandler triggerHandler = new MoroiHandler(moroiErrorUrl, triggerSerializer, noSslHostnameVerification);

                final String strThrottleWindowSize = gwProperties.getProperty(ConfigurationReader.CFG_THROTTLE_WINDOW_SIZE,
                        String.valueOf(GhostWriterSnaperr.DEFAULT_ERROR_WINDOW_SIZE_MS));
                final long throttleWindowSize = Long.parseLong(strThrottleWindowSize);

                final String strThrottleMaxErrorsInWindow = gwProperties.getProperty(ConfigurationReader.CFG_THROTTLE_MAX_ERRORS,
                        String.valueOf(GhostWriterSnaperr.DEFAULT_MAX_ERROR_COUNT_IN_WINDOW));
                final int throttleMaxErrorsInWindow = Integer.parseInt(strThrottleMaxErrorsInWindow);


                ghostWriterSnaperr = new GhostWriterSnaperr(referenceTracker, triggerHandler, throttleWindowSize,
                        throttleMaxErrorsInWindow);

                gwSetupSuccessful = true;

                return ghostWriterSnaperr;

            } catch (Exception e) {
                LOG.error("GhostWriter configuration failed, skip using GhostWriter: " + e.getMessage(), e);
                gwSetupSuccessful = false;
                return null;
            }
        }
    }

}
