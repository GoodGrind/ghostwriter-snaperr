package io.ghostwriter.rt.snaperr.handler;

import okhttp3.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import io.ghostwriter.rt.snaperr.Logger;
import io.ghostwriter.rt.snaperr.SnaperrTracer;
import io.ghostwriter.rt.snaperr.api.ReferenceTracker;
import io.ghostwriter.rt.snaperr.api.TriggerHandler;
import io.ghostwriter.rt.snaperr.moroi.serializer.MoroiSerializer;
import io.ghostwriter.rt.snaperr.tracker.StackBasedReferenceTracker;

import java.io.IOException;


public class MoroiHandler implements TriggerHandler<String> {


    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final Logger LOG = Logger.getLogger(MoroiHandler.class.getName());
    private final String moroiErrorUrl;

    private final boolean noSslHostnameVerification;

    /**
     * OkHttpClient is threadsafe
     */
    private final OkHttpClient httpClient;


    public MoroiHandler(String moroiErrorUrl, boolean noSslHostnameVerification) {
        this.moroiErrorUrl = moroiErrorUrl;
        this.noSslHostnameVerification = noSslHostnameVerification;

        httpClient = createOkHttpClient(noSslHostnameVerification);
    }

    public static void main(String[] args) {

        final String usage = "Usage:" + MoroiHandler.class.getName() + " [moroiUrl] [moroiAppUUID]"
                + "\t\t send test message to Moroi\n" + "\n" + "Arguments:\n"
                + "\tmoroiUrl        e.g. http(s)://localhost:4443/api/exception\n"
                + "\tmoroiAppUUID    UUID of your application registered in Moroi";

        if (args.length < 2) {
            System.out.println("Invalid arguments\n\n" + usage);
            System.exit(-1);
        }

        final String moroiUrl = args[0];
        final String moroiAppUUID = args[1];
        final boolean noSslHostnameVerification = args.length > 2 ? Boolean.parseBoolean(args[2]) : false;

        System.out.println("moroiUrl: " + moroiUrl);
        System.out.println("moroiAppUUID: " + moroiAppUUID);
        System.out.println("noSslHostnameVerification: " + noSslHostnameVerification);

        MoroiSerializer serializer2 = new MoroiSerializer(moroiAppUUID);
        MoroiHandler moroiHandler = new MoroiHandler(moroiUrl, noSslHostnameVerification);
        final ReferenceTracker referenceTracker = new StackBasedReferenceTracker();
        SnaperrTracer ghostWriterSnaperr = new SnaperrTracer(referenceTracker, serializer2, moroiHandler, -1L, -1);

        String METHOD_NAME = "main";
        ghostWriterSnaperr.entering(MoroiHandler.class, METHOD_NAME);
        ghostWriterSnaperr.valueChange(MoroiHandler.class, METHOD_NAME, "x", 1);
        ghostWriterSnaperr.valueChange(MoroiHandler.class, METHOD_NAME, "y", 2);

        String METHOD_NAME2 = "main2";
        ghostWriterSnaperr.entering(MoroiHandler.class, METHOD_NAME2);
        ghostWriterSnaperr.valueChange(MoroiHandler.class, METHOD_NAME2, "x1", 1);
        ghostWriterSnaperr.valueChange(MoroiHandler.class, METHOD_NAME2, "y2", 2);

        ghostWriterSnaperr.onError(MoroiHandler.class, METHOD_NAME2, new IllegalArgumentException());
        System.out.println("Message sent to Moroi, please check your moroi instance!");
    }

    protected OkHttpClient createOkHttpClient(boolean sslNoHostnameVerify) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

        if (sslNoHostnameVerify) {
            httpClientBuilder.hostnameVerifier(new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        }

        return httpClientBuilder.build();
    }

    @Override
    public void onError(String serializedError) {
        try {
            postError(serializedError);
        } catch (Exception e) {
            LOG.error("Something went wrong when serializing and sending error to Moroi", e);
        }
    }

    @Override
    public void onTimeout(String serializedTimeout) {
        throw new UnsupportedOperationException();
    }

    private void postError(String json) {
        RequestBody body = RequestBody.create(JSON, json);
        LOG.debug("postError req>>>> " + json);
        Request request = new Request.Builder()
                .url(moroiErrorUrl)
                .post(body)
                .build();

        try {
            Response response = httpClient.newCall(request).execute();

            LOG.debug("postError <<<<res " + response.body().string());
            response.close();
        } catch (IOException e) {
            LOG.error("something went wrong when communicating with Moroi", e);
        }
    }

}
