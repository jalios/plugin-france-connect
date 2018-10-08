package bzh.jcmsplugin.fc.oauth;

import com.jalios.util.JPropertiesListener;

import bzh.jcmsplugin.fc.FranceConnectType;

public class FranceConnectAgentsApi extends AbstractFranceConnectApi implements JPropertiesListener {
  
  @Override
  protected FranceConnectType getType() {
    return FranceConnectType.AGENTS;
  }

}