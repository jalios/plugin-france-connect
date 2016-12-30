package bzh.jcmsplugin.fc.oauth;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

public class FranceConnectApi extends DefaultApi20 {
	private static final String AUTHORIZATION_URL = "https://fcp.integ01.dev-franceconnect.fr/api/v1/authorize?client_id=%s&response_type=code&redirect_uri=%s";
	private static String scope = "openid profile";
	private static String state = "test";

	

	public String getAccessTokenEndpoint() {
		return "https://fcp.integ01.dev-franceconnect.fr/api/v1/token";
	}

	public String getAuthorizationUrl(OAuthConfig config) {
		Preconditions.checkValidUrl(config.getCallback(),
				"Must provide a valid url as callback. FranceConnect does not support OOB");
		return String.format(
				"https://fcp.integ01.dev-franceconnect.fr/api/v1/authorize?client_id=%s&response_type=code&scope="
						+ scope + "&state=" + state + "&nonce=toto&redirect_uri=%s",
				new Object[] { config.getApiKey(), OAuthEncoder.encode(config.getCallback()) });
	}



	public AccessTokenExtractor getAccessTokenExtractor() {
		return new  JsonTokenExtractor();
	}

	@Override
	public Verb getAccessTokenVerb() {
		// TODO Auto-generated method stub
		return Verb.POST;
	}
	
	

}