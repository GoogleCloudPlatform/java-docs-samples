// [START versioned_hostnames]
public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	resp.setContentType("text/plain");
	Environment env = ApiProxy.getCurrentEnvironment();
	resp.getWriter().println("default_version_hostname: "
			+ env.getAttributes().get("com.google.appengine.runtime.default_version_hostname"));
}
// [END versioned_hostnames]

// [START asserting_identity_to_Google_APIs]
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
// Note that any JSON parser can be used; this one is used for illustrative purposes.
import org.json.JSONObject;
import org.json.JSONTokener;


public String createShortUrl(String longUrl) throws Exception {
	try {
		ArrayList<String> scopes = new ArrayList<String>();
		scopes.add("https://www.googleapis.com/auth/urlshortener");
		AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();
		AppIdentityService.GetAccessTokenResult accessToken = appIdentity.getAccessToken(scopes);
		// The token asserts the identity reported by appIdentity.getServiceAccountName()
		JSONObject request = new JSONObject();
		request.put("longUrl", longUrl);

		URL url = new URL("https://www.googleapis.com/urlshortener/v1/url?pp=1");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.addRequestProperty("Content-Type", "application/json");
		connection.addRequestProperty("Authorization", "Bearer  " + accessToken.getAccessToken());

		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		request.write(writer);
		writer.close();

		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			// Note: Should check the content-encoding.
			JSONTokener response_tokens = new JSONTokener(connection.getInputStream());
			JSONObject response = new JSONObject(response_tokens);
			return (String) response.get("id");
		} else {
			throw new Exception();
		}
	} catch (Exception e) {
		// Error handling elided.
		throw e;
	}
}
// [END asserting_identity_to_Google_APIs]
