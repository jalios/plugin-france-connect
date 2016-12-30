package bzh.jcmsplugin.fc.oauth;

import com.jalios.jcmsplugin.oauth.BasicOAuthProvider;
import com.jalios.jcmsplugin.oauth.OAuthProvider;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.YahooApi;

public abstract class FranceConnect extends BasicOAuthProvider implements OAuthProvider {
	public static final String ID = "franceconnect";
	public static final String NAME = "FranceConnect";

	protected Class<? extends Api> getApiClass() {
		return FranceConnectApi.class;
	}

	public String getId() {
		return "franceconnect";
	}

	public String getName() {
		return "FranceConnect";
	}

	@Override
	public String getGrantType() {
		// TODO Auto-generated method stub
		return "authorization_code";
	}

	@Override
	public String getScope() {
		// TODO Auto-generated method stub
		return "openid profile";
	}

}
