package bzh.jcmsplugin.fc.oauth;

import org.scribe.builder.api.Api;

import bzh.jcmsplugin.fc.FranceConnectType;

public class FranceConnectAgents extends AbstractFranceConnectProvider {

  @Override
  protected FranceConnectType getType() {
    return FranceConnectType.AGENTS;
  }

  @Override
  protected Class<? extends Api> getApiClass() {
    return FranceConnectAgentsApi.class;
  }
}
