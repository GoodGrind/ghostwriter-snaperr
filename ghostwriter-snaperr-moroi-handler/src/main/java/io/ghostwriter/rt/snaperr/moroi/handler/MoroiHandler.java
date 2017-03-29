package io.ghostwriter.rt.snaperr.moroi.handler;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import io.ghostwriter.rt.snaperr.Logger;
import io.ghostwriter.rt.snaperr.api.TriggerHandler;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


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

	//    public static void main(String[] args) {
	//
	//        final String usage = "Usage:" + MoroiHandler.class.getName() + " [moroiUrl] [moroiAppUUID]"
	//                + "\t\t send test message to Moroi\n" + "\n" + "Arguments:\n"
	//                + "\tmoroiUrl        e.g. http(s)://localhost:4443/api/exception\n"
	//                + "\tmoroiAppUUID    UUID of your application registered in Moroi";
	//
	//        if (args.length < 2) {
	//            System.out.println("Invalid arguments\n\n" + usage);
	//            System.exit(-1);
	//        }
	//
	//        final String moroiUrl = args[0];
	//        final String moroiAppUUID = args[1];
	//        final boolean noSslHostnameVerification = args.length > 2 ? Boolean.parseBoolean(args[2]) : false;
	//
	//        System.out.println("moroiUrl: " + moroiUrl);
	//        System.out.println("moroiAppUUID: " + moroiAppUUID);
	//        System.out.println("noSslHostnameVerification: " + noSslHostnameVerification);
	//
	//        MoroiSerializer serializer2 = new MoroiSerializer(moroiAppUUID);
	//        MoroiHandler moroiHandler = new MoroiHandler(moroiUrl, noSslHostnameVerification);
	//        final ReferenceTracker referenceTracker = new StackBasedReferenceTracker();
	//        SnaperrTracer ghostWriterSnaperr = new SnaperrTracer(referenceTracker, serializer2, moroiHandler, -1L, -1);
	//
	//        String METHOD_NAME = "main";
	//        ghostWriterSnaperr.entering(MoroiHandler.class, METHOD_NAME);
	//        ghostWriterSnaperr.valueChange(MoroiHandler.class, METHOD_NAME, "x", 1);
	//        ghostWriterSnaperr.valueChange(MoroiHandler.class, METHOD_NAME, "y", 2);
	//
	//        String METHOD_NAME2 = "main2";
	//        ghostWriterSnaperr.entering(MoroiHandler.class, METHOD_NAME2);
	//        ghostWriterSnaperr.valueChange(MoroiHandler.class, METHOD_NAME2, "x1", 1);
	//        ghostWriterSnaperr.valueChange(MoroiHandler.class, METHOD_NAME2, "y2", 2);
	//
	//        ghostWriterSnaperr.onError(MoroiHandler.class, METHOD_NAME2, new IllegalArgumentException());
	//        System.out.println("Message sent to Moroi, please check your moroi instance!");
	//    }

	protected OkHttpClient createOkHttpClient(boolean sslNoHostnameVerify) {
		OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

		if (sslNoHostnameVerify) {
			setNoHostnameVerification(httpClientBuilder);
		}

		return httpClientBuilder.build();
	}

	protected void setNoHostnameVerification(OkHttpClient.Builder httpClientBuilder) {

		httpClientBuilder.hostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				LOG.warn("SSL Hostname verification is disabled");
				return true;
			}
		});

		try {
			final X509TrustManager[] trustAllCerts = new X509TrustManager[] {
					new X509TrustManager() {
						@Override
						public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws
								CertificateException {
						}

						@Override
						public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws
								CertificateException {
						}

						@Override
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return new java.security.cert.X509Certificate[] {};
						}
					}
			};

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			;
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			httpClientBuilder.sslSocketFactory(sslSocketFactory, trustAllCerts[0]);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void onError(String serializedError) {
		try {
			postError(serializedError);
		}
		catch (Exception e) {
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
		}
		catch (IOException e) {
			LOG.error("something went wrong when communicating with Moroi", e);
		}
	}

}
