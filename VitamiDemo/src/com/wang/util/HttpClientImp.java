package com.wang.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;

public class HttpClientImp {
	
	// private static final String TAG = "HttpClient";

		private static final int MAX_CONNECTIONS_PER_ROUTE = 8;
		private static final int MAX_CONNECTION = 16;

		public static final String COOKIE = "Cookie";
		public static final String SESSIONID = "JSESSIONID=";
		public static final int TIMEOUT = 11000;

		private static final String DEFAULT_CLIENT_VERSION = "com.znisea.linju";
		private static final String CLIENT_VERSION_HEADER = "User-Agent";
		private final HttpClient mHttpClient;
		private final String mClientVersion;

		private static HttpClientImp Instance = null;

		public static HttpClientImp getInstance() {
			synchronized (HttpClientImp.class) {
				if (Instance == null) {
					synchronized (HttpClientImp.class) {
						Instance = new HttpClientImp();
					}
				}
				return Instance;
			}
		}

		private HttpClientImp() {
			mHttpClient = createHttpClient();
			mClientVersion = DEFAULT_CLIENT_VERSION;
		}

		@SuppressWarnings("deprecation")
		public HttpPost createImageHttpPost(String url, String token, File file,
				String tutorialId, String stepId)
				throws UnsupportedEncodingException {
			HttpPost httpPost = new HttpPost(url);
			String BOUNDARY = "----WebKitFormBoundaryRHLsVmxsgrW2JzMs";

			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE, BOUNDARY,
					Charset.defaultCharset());

			FileBody fileBody = new FileBody(file);
			reqEntity.addPart("tutorial", new StringBody(tutorialId));
			reqEntity.addPart("step", new StringBody(stepId));
			reqEntity.addPart("image", fileBody);


			try {
				httpPost.setEntity(reqEntity);
			} catch (Exception e1) {
				throw new IllegalArgumentException(
						"Unable to encode http parameters.");
			}
			return httpPost;
		}

		
		/**
		 * Create a thread-safe client. This client does not do redirecting, to
		 * allow us to capture correct "error" codes.
		 * 
		 * @return HttpClient
		 */
		public static final DefaultHttpClient createHttpClient() {

			SSLSocketFactory sf = null;
			try {
				KeyStore trustStore = KeyStore.getInstance(KeyStore
						.getDefaultType());
				trustStore.load(null, null);
				sf = new MySSLSocketFactory(trustStore);
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			} catch (Exception e) {
				// LogUnit.Log(TAG, "keystore error");
				e.printStackTrace();
			}

			// Sets up the http part of the service.
			final SchemeRegistry supportedSchemes = new SchemeRegistry();

			// Register the "http" protocol scheme, it is required
			// by the default operator to look up socket factories.
			// final SocketFactory sf = PlainSocketFactory.getSocketFactory();
			// supportedSchemes.register(new Scheme("http", sf, 80));
			supportedSchemes.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			supportedSchemes.register(new Scheme("https", sf, 443));

			// Set some client http client parameter defaults.
			final HttpParams httpParams = createHttpParams();
			HttpClientParams.setRedirecting(httpParams, false);

			final ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					httpParams, supportedSchemes);
			return new DefaultHttpClient(ccm, httpParams);
		}

		/**
		 * Create the default HTTP protocol parameters.
		 */
		private static final HttpParams createHttpParams() {
			final HttpParams params = new BasicHttpParams();

			// Turn off stale checking. Our connections break all the time anyway,
			// and it's not worth it to pay the penalty of checking every time.
			HttpConnectionParams.setStaleCheckingEnabled(params, false);
			HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, TIMEOUT);
			HttpConnectionParams.setSocketBufferSize(params, 8192);

			ConnManagerParams.setTimeout(params, TIMEOUT);
			ConnManagerParams.setMaxTotalConnections(params, MAX_CONNECTION);
			ConnManagerParams.setMaxConnectionsPerRoute(params, CONN_PER_ROUTE);

			return params;
		}
		
		public static class MySSLSocketFactory extends SSLSocketFactory {
			SSLContext sslContext = SSLContext.getInstance("TLS");

			public MySSLSocketFactory(KeyStore truststore)
					throws NoSuchAlgorithmException, KeyManagementException,
					KeyStoreException, UnrecoverableKeyException {
				super(truststore);

				TrustManager tm = new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
					}

					public void checkServerTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
					}

					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
				};

				sslContext.init(null, new TrustManager[] { tm }, null);
			}

			@Override
			public Socket createSocket(Socket socket, String host, int port,
					boolean autoClose) throws IOException, UnknownHostException {
				return sslContext.getSocketFactory().createSocket(socket, host,
						port, autoClose);
			}

			@Override
			public Socket createSocket() throws IOException {
				return sslContext.getSocketFactory().createSocket();
			}
		}
		
		
		/** The default maximum number of connections allowed per host */
		private static final ConnPerRoute CONN_PER_ROUTE = new ConnPerRoute() {

			public int getMaxForRoute(HttpRoute route) {
				return MAX_CONNECTIONS_PER_ROUTE;
			}

		};
		
		
		/**
		 * execute() an httpRequest catching exceptions and returning null instead.
		 * 
		 * @param httpRequest
		 * @return
		 * @throws IOException
		 */
		public HttpResponse executeHttpRequest(HttpRequestBase httpRequest)
				throws IOException {
			try {
				mHttpClient.getConnectionManager().closeExpiredConnections();
				return mHttpClient.execute(httpRequest);
			} catch (IOException e) {
				httpRequest.abort();
				throw e;
			}
		}

		/**
		 * create HttpGet
		 */
		public HttpGet createHttpGet(String url, String sessionId) {
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader(CLIENT_VERSION_HEADER, mClientVersion);
			if (!TextUtils.isEmpty(sessionId)) {
				httpGet.addHeader(COOKIE, SESSIONID + sessionId);
			}
			return httpGet;
		}

		/**
		 * create HttpGet
		 */
		public HttpGet createHttpGet(String url, String sessionId,
				List<NameValuePair> nameValuePairs) {
			String query = URLEncodedUtils.format(nameValuePairs, HTTP.UTF_8);
			HttpGet httpGet = new HttpGet(url + "?" + query);
			httpGet.addHeader(CLIENT_VERSION_HEADER, mClientVersion);
			if (!TextUtils.isEmpty(sessionId)) {
				httpGet.addHeader(COOKIE, SESSIONID + sessionId);
			}
			return httpGet;
		}
		
		
		public String getForString(String url)
				throws Exception {
			HttpGet httpGet = createHttpGet(url,null);
			HttpResponse response = executeHttpRequest(httpGet);

			switch (response.getStatusLine().getStatusCode()) {
			case 200:
				try {
					return EntityUtils.toString(response.getEntity());
				} catch (ParseException e) {
					throw new Exception(e.getMessage());
				}

			case 401:
				response.getEntity().consumeContent();
				throw new IllegalArgumentException(response.getStatusLine()
						.toString());

			case 404:
				response.getEntity().consumeContent();
				throw new Exception(response.getStatusLine().toString());

			case 400:
				response.getEntity().consumeContent();
				throw new IllegalArgumentException(response.getStatusLine()
						.toString());

			default:
				response.getEntity().consumeContent();
				throw new Exception(response.getStatusLine().toString());
			}

		}


}
