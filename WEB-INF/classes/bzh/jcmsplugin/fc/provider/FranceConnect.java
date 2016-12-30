package bzh.jcmsplugin.fc.provider;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import com.jalios.jcms.Channel;
import com.jalios.jcmsplugin.socialauth.SocialAuthOAuthProvider;
import com.jalios.jcmsplugin.socialauth.UserInfos;
import com.jalios.util.JProperties;
import com.jalios.util.JPropertiesListener;
import com.jalios.util.Util;

public class FranceConnect extends bzh.jcmsplugin.fc.oauth.FranceConnect
		implements JPropertiesListener, SocialAuthOAuthProvider {
	private static Logger logger = Logger.getLogger(FranceConnect.class);
	private Channel channel = Channel.getChannel();

	private String scope = "";
	private String userInfoUrl = "";

	public FranceConnect() {
		super();
		initProperties();

	}

	@Override
	public void propertiesChange(JProperties arg0) {
		initProperties();
	}

	private void initProperties() {
		this.scope = channel.getProperty("jcmsplugin.socialauth.provider.franceconnect.scope");
		this.userInfoUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnect.userInfoUrl");

	}

	public String getScope() {

		return scope;
	}

	public UserInfos getUserInfos(Token paramToken) {
		try {

			OAuthRequest localOAuthRequest = new OAuthRequest(Verb.GET, this.userInfoUrl + "?schema=openid");
			StringBuilder sb = new StringBuilder();
			sb.append("Bearer ");
			sb.append(paramToken.getToken());
			localOAuthRequest.addHeader("Authorization", sb.toString());

			Response localResponse = localOAuthRequest.send();
			String str1 = localResponse.getBody();
			if (logger.isTraceEnabled()) {
				logger.trace("ResponseBody from FranceConnect : " + str1);
			}

			ObjectMapper localObjectMapper = new ObjectMapper();

			JSONObject localJSONObject1 = new JSONObject(str1);
			String siret = "";
			String nomUsage = "";
			String email = "";
			String nomPatronymique = "";
			String prenom = "";
			String sub = "";
			String picture = "";
			String gender = "";
			String infos = "";

			if (localJSONObject1.has("sub"))
				sub = localJSONObject1.getString("sub");

			if (localJSONObject1.has("siret"))
				siret = localJSONObject1.getString("siret");
			if (localJSONObject1.has("preferred_username"))
				nomUsage = localJSONObject1.getString("preferred_username");
			if (localJSONObject1.has("email")) {
				email = localJSONObject1.getString("email");
				
			}
			if (localJSONObject1.has("birthdate"))
				infos = localJSONObject1.getString("birthdate");
			if (localJSONObject1.has("birthplace"))
				infos += " - " + localJSONObject1.getString("birthplace");
			if (localJSONObject1.has("birthcountry"))
				infos += " - " + localJSONObject1.getString("birthcountry");

			if (localJSONObject1.has("given_name"))
				prenom = localJSONObject1.getString("given_name");
			if (localJSONObject1.has("family_name"))
				infos += "\nNom de naissance : " + localJSONObject1.getString("family_name");

			if (localJSONObject1.has("picture"))
				picture = localJSONObject1.getString("picture");

			return new UserInfos(sub, email, prenom,nomUsage, picture);

		} catch (Exception localException) {
			logger.warn("Could not retrieve user informations on FranceConnect", localException);
		}
		return null;
	}

	public String getIcon() {
		return "plugins/FranceConnectPlugin/images/fc.png";
	}
}