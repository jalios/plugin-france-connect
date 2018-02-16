package bzh.jcmsplugin.fc.provider;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.scribe.builder.api.Api;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import com.jalios.jcms.Channel;
import com.jalios.jcms.context.JcmsContext;
import com.jalios.jcmsplugin.socialauth.SocialAuthOAuthProvider;
import com.jalios.jcmsplugin.socialauth.UserInfos;
import com.jalios.util.JProperties;
import com.jalios.util.JPropertiesListener;

import bzh.jcmsplugin.fc.oauth.FranceConnectEntreprisesApi;

public class FranceConnectEntreprises extends bzh.jcmsplugin.fc.oauth.FranceConnectEntreprises
		implements JPropertiesListener, SocialAuthOAuthProvider {
	private static Logger logger = Logger.getLogger(FranceConnectEntreprises.class);
	private Channel channel = Channel.getChannel();

	private String scope = "";
	private String userInfoUrl = "";

	public FranceConnectEntreprises() {
		super();
		initProperties();

	}

	@Override
	public void propertiesChange(JProperties arg0) {
		initProperties();
	}

	private void initProperties() {
		this.scope = channel.getProperty("jcmsplugin.socialauth.provider.franceconnectentreprises.scope");
		this.userInfoUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnectentreprises.userInfoUrl");

	}

	public String getScope() {

		return scope;
	}

	@Override
	protected Class<? extends Api> getApiClass() {
		// TODO Auto-generated method stub
		return FranceConnectEntreprisesApi.class;
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


			JSONObject localJSONObject1 = new JSONObject(str1);
			String nomUsage = "";
			String email = "";
			String prenom = "";
			String sub = "";
			String siret ="";
			if (localJSONObject1.has("sub"))
				sub = localJSONObject1.getString("sub");
			
			
			if (localJSONObject1.has("given_name"))
				prenom = localJSONObject1.getString("given_name");

			
			if (localJSONObject1.has("family_name"))
				nomUsage = localJSONObject1.getString("family_name");
			if (localJSONObject1.has("preferred_username"))
				nomUsage = localJSONObject1.getString("preferred_username");
			
			if (localJSONObject1.has("siret")){
				
				// on se sert du nom pour fixer le siret en attendant une modif du userinfos par jalios
				// pour pouvoir ajouter plus d'infos.
				
				
				siret = localJSONObject1.getString("siret");
				nomUsage+=".siret"+siret ;
				
			}
			
			if (localJSONObject1.has("email")) {
				email = localJSONObject1.getString("email");

			}

			UserInfos ui =  new UserInfos(sub, nomUsage, prenom, email, null);
			return ui;

		} catch (Exception localException) {
			logger.warn("Could not retrieve user informations on FranceConnect", localException);
		}
		return null;
	}

	public String getIcon() {
		return "plugins/FranceConnectPlugin/images/fce.png";
	}
}