package bzh.jcmsplugin.fc.oauth;

import org.scribe.builder.api.Api;

import bzh.jcmsplugin.fc.FranceConnectType;

public class FranceConnectEntreprises extends AbstractFranceConnectProvider {

  @Override
  protected FranceConnectType getType() {
    return FranceConnectType.ENTREPRISES;
  }

  @Override
  protected Class<? extends Api> getApiClass() {
    return FranceConnectEntreprisesApi.class;
  }
}
