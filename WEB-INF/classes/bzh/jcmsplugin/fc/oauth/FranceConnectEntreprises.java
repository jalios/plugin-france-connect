package bzh.jcmsplugin.fc.oauth;

import org.scribe.builder.api.Api;

import com.jalios.jcms.Channel;
import com.jalios.jcmsplugin.oauth.BasicOAuthProvider;
import com.jalios.jcmsplugin.oauth.OAuthProvider;
import com.jalios.util.JProperties;
import com.jalios.util.JPropertiesListener;

public abstract class FranceConnectEntreprises extends BasicOAuthProvider implements OAuthProvider, JPropertiesListener {

	public static final String ID = "franceconnectentreprises";
	public static final String NAME = "FranceConnectEntreprises";
	private String scope = "";
	private String grantType = "";

	public FranceConnectEntreprises(){
		
		super();
		initProperties();
	}
	
	 
	
	@Override
	public void propertiesChange(JProperties arg0) {
		initProperties();
	}

	private void initProperties() {
		this.scope = Channel.getChannel().getProperty("jcmsplugin.socialauth.provider.franceconnectentreprises.scope");
		this.grantType = Channel.getChannel().getProperty("jcmsplugin.socialauth.provider.franceconnectentreprises.userInfoUrl");

	}

	
	protected Class<? extends Api> getApiClass() {
		return FranceConnectParticuliersApi.class;
	}

	public String getId() {
		return "franceconnectentreprises";
	}

	public String getName() {
		return "FranceConnect Entreprises"; 
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
