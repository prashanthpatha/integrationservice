package ac.integration.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ac.integration.util.Helper;

/**
 * Service specifically for handling slack related calls
 * 
 * @author patpr14
 *
 */

public class SlackService {
	
	public static final Logger logger = LoggerFactory.getLogger(SlackService.class);

	//public static final String SLACK_INCOMING_WEBHOOK = "https://hooks.slack.com/services/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";

	public static final String SLACK_OAUTH_ACCESS_URL = "https://slack.com/api/oauth.access";

	Helper helper;

	public SlackService() {
		helper = new Helper();
	}

	public void postMessageToSlackChannel(String message, String incomingWebbookURL) throws ClientProtocolException, IOException {
		logger.info("message : " + message);
		logger.info("Posting to hook : " + incomingWebbookURL);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(incomingWebbookURL);
		StringEntity entity = new StringEntity("{\"text\" : \"" + message + "\"}");
		entity.setContentType("application/json");
		httpPost.setEntity(entity);
		httpClient.execute(httpPost);
	}
	
	public void postMessageToSlackChannels(String message) throws ClientProtocolException, IOException {
		String[] hooks = helper.getIncomingWebhookURLs();
		
		for (int i = 0; i < hooks.length; i++) {
			postMessageToSlackChannel(message,hooks[i]);
		}
	}

	public void getSlackResources(String code) throws URISyntaxException, ClientProtocolException, IOException {

		Properties properties = helper.getProperties("slack.properties");

		HttpClient httpClient = HttpClientBuilder.create().build();
		URIBuilder uriBuilder = new URIBuilder(SLACK_OAUTH_ACCESS_URL);
		uriBuilder.setParameter("client_id", properties.getProperty("client_id"))
				.setParameter("client_secret", properties.getProperty("client_secret")).setParameter("code", code);
		// .setParameter("redirect_uri", "");

		HttpGet httpGet = new HttpGet(uriBuilder.build());
		
		HttpResponse httpResponse = httpClient.execute(httpGet);
		
		ResponseHandler<String> handler = new BasicResponseHandler();
		String responseBody = handler.handleResponse(httpResponse);
		int responseCode = httpResponse.getStatusLine().getStatusCode();
		
		logger.info("\n*********** Begin : OAuth Access response *************\n");
		logger.info("\nresponse code : " + responseCode + "\n");
		logger.info(responseBody);
		logger.info("\n************* End : OAuth Access response ***********\n");
		
		helper.extractAndSaveIncomingWebhookURL(responseBody);
	}
}
