package bzh.jcmsplugin.fc.oauth;

import java.util.Map;

import org.apache.log4j.Logger;
import org.scribe.builder.api.Api;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import com.jalios.jcms.Channel;
import com.jalios.jcmsplugin.oauth.BasicOAuthProvider;
import com.jalios.jcmsplugin.oauth.OAuthProvider;
import com.jalios.jcmsplugin.socialauth.SocialAuthOAuthProvider;
import com.jalios.jcmsplugin.socialauth.UserInfos;
import com.jalios.util.JProperties;
import com.jalios.util.JPropertiesListener;
import com.jalios.util.Util;

import bzh.jcmsplugin.fc.FranceConnectType;
import bzh.jcmsplugin.fc.extractors.FranceConnectJsonTokenExtractor;

/**
 * Abstract implementation for both OAuthProvider and SocialAuthOAuthProvider for FranceConnect.
 * 
 * @since fc-1.9
 */
public abstract class AbstractFranceConnectProvider extends BasicOAuthProvider
    implements OAuthProvider, SocialAuthOAuthProvider, JPropertiesListener {

  private Logger logger = Logger.getLogger(getClass());
  private Channel channel = Channel.getChannel();
  
	private String scope = "";
	private String grantType = "";
  private String logoutUrl = "";

  //-------------------------------------------------------------------
	// Abstract methods that must be implemented by provider implementation.
  //-------------------------------------------------------------------
  
	/**
	 * Retrieve the type of FranceConnect implementation.
	 * @return a FranceConnectType enum, must not return null.
	 */
	abstract protected FranceConnectType getType();

  /**
   * Retrieve the API Class for this FranceConnect implementation.
   * @return a Scribe API class, must not return null.
   */
	abstract protected Class<? extends Api> getApiClass();
  
	//-------------------------------------------------------------------
  // Constructor
  //-------------------------------------------------------------------
	
	public AbstractFranceConnectProvider(){
		super();
		initProperties();
	}

  //-------------------------------------------------------------------
  // Properties
  //-------------------------------------------------------------------
	
	@Override
	public void propertiesChange(JProperties arg0) {
		initProperties();
	}

	private void initProperties() {
		this.scope = channel.getProperty("jcmsplugin.socialauth.provider.franceconnect"+getType().getSuffix()+".scope");
		this.grantType = channel.getProperty("jcmsplugin.socialauth.provider.franceconnect"+getType().getSuffix()+".userInfoUrl");
		
    this.logoutUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnect"+getType().getSuffix()+".logoutUrl");    
	}
	
  //-------------------------------------------------------------------
  // Getter
  //-------------------------------------------------------------------
  

	public String getId() {
		return "franceconnect"+getType().getSuffix();
	}

	public String getName() {
		return "FranceConnect "+getType().getSuffix(); 
	}

	@Override
	public String getGrantType() {
		return this.grantType;
	}

	@Override
	public String getScope() {
		return this.scope;
	}
  
  /**
   * Return the logout URL to be used when signing out from FranceConnect.
   * @return the URL specified in the propert (if an empty value is specified logout will only be performed locally)
   */
  public String getLogoutUrl() {
    return logoutUrl;
  }

  //-------------------------------------------------------------------
  // SocialAuthOAuthProvider implementation
  //-------------------------------------------------------------------
  
  public UserInfos getUserInfos(Token paramToken) {
    try {
      final OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, this.grantType + "?schema=openid");
      oauthRequest.addHeader("Authorization", "Bearer " + paramToken.getToken());

      final Response response = oauthRequest.send();
      final String jsonResponseStr = response.getBody();
      if (logger.isTraceEnabled()) {
        logger.trace("ResponseBody from FranceConnect : " + jsonResponseStr);
      }

      final Map<?,?> map = FranceConnectJsonTokenExtractor.json2map(jsonResponseStr);
      if (map == null) {
        logger.warn("Could not read user informations on FranceConnect, response was : " + jsonResponseStr);
        return null;
      }
      
      final String login = Util.getString(map.get("sub"), "");
      final String nomUsage = Util.getString(map.get("family_name"), "");
      final String prenom = Util.getString(map.get("given_name"), "");
      final String email = Util.getString(map.get("email"), "");
      
      UserInfos ui = new UserInfos(login, nomUsage, prenom, email, null);
      
      // Recopy all element from the response
      // For example, when used with FranceConnect entreprise, it will include the siret number 
      for (Map.Entry<?,?> entry : map.entrySet()) {
        Object keyObj = entry.getKey();
        Object valueObj = entry.getValue();
        if (keyObj instanceof String && valueObj instanceof String) {
          String key = Util.getString(entry.getKey(), "");
          String value = Util.getString(entry.getValue(), "");
          ui.putData(key, value);
        }
      }

      return ui;

    } catch (Exception localException) {
      logger.warn("Could not retrieve user informations on FranceConnect", localException);
    }
    return null;
  }

  public String getIcon() {
    return "jcmsplugin-fc.login-" + getType().getSuffix();
  }
}
