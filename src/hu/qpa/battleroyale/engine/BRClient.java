package hu.qpa.battleroyale.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class BRClient {
	HttpClient client;

	public BRClient() {
		this.client = createClient();
	}

	private HttpClient createClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	/**
	 * Calls the WS method with the given argument
	 * 
	 * @param url
	 *            url of the method
	 * @param params
	 *            the name-value parameter pairs
	 * @return
	 * @throws IOException
	 *             if there is a problem with the connection
	 * @throws MalformedServiceResponseException
	 */
	public String callWSMethod(String url, List<? extends NameValuePair> params)
			throws IOException {

		Log.d(getClass().getSimpleName(),
				"Sending request with parameters: " + params.toString());
		
		String charset = HTTP.UTF_8;
		HttpPost httpPost = new HttpPost(url);
		UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, charset);
		httpPost.setEntity(ent);
		httpPost.setHeader(HTTP.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE
				+ HTTP.CHARSET_PARAM + charset);
		httpPost.setHeader(HTTP.CONTENT_ENCODING, charset);

		String content = "";
		try {
			client.getConnectionManager().closeExpiredConnections();
			HttpResponse response = client.execute(httpPost);

			final int statusCode = response.getStatusLine().getStatusCode();

			HttpEntity responseEntity = response.getEntity();
			content = convertStreamToString(responseEntity.getContent());

			if (statusCode != HttpStatus.SC_OK) {
				Log.w(getClass().getSimpleName(), "Response:\n" + content);
				throw new HttpResponseException(statusCode, "Error "
						+ statusCode + " for URL " + url);

			}

			return content;

		} catch (IOException e) {
			httpPost.abort();
			Log.e(getClass().getSimpleName(), "Error for URL " + url, e);
			throw e;
		}

	}

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
