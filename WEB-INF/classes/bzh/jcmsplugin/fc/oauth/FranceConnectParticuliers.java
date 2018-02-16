package bzh.jcmsplugin.fc.oauth;

import com.jalios.jcms.Channel;
import com.jalios.jcmsplugin.oauth.BasicOAuthProvider;
import com.jalios.jcmsplugin.oauth.OAuthProvider;
import com.jalios.util.JProperties;
import com.jalios.util.JPropertiesListener;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.YahooApi;

public abstract class FranceConnectParticuliers extends BasicOAuthProvider implements OAuthProvider, JPropertiesListener {

	public static final String ID = "franceconnectparticuliers";
	public static final String NAME = "FranceConnectParticuliers";
	private String scope = "";
	private String grantType = "";

	public FranceConnectParticuliers(){
		
		super();
		initProperties();
	}
	
	
	
	@Override
	public void propertiesChange(JProperties arg0) {
		initProperties();
	}

	private void initProperties() {
		this.scope = Channel.getChannel().getProperty("jcmsplugin.socialauth.provider.franceconnectparticuliers.scope");
		this.grantType = Channel.getChannel().getProperty("jcmsplugin.socialauth.provider.franceconnectparticuliers.userInfoUrl");

	}

	
	protected Class<? extends Api> getApiClass() {
		return FranceConnectParticuliersApi.class;
	}

	public String getId() {
		return "franceconnectparticuliers";
	}

	public String getName() {
		return "FranceConnectParticuliers"; 
	}

	@Override
	public String getGrantType() {
		// TODO Auto-generated method stub
		return this.grantType;
	}

	@Override
	public String getScope() {
		// TODO Auto-generated method stub
		return this.scope;
	}

}
