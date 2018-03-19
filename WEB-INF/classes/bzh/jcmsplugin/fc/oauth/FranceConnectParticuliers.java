package bzh.jcmsplugin.fc.oauth;

import org.scribe.builder.api.Api;

import bzh.jcmsplugin.fc.FranceConnectType;

public class FranceConnectParticuliers extends AbstractFranceConnectProvider {

  @Override
  protected FranceConnectType getType() {
    return FranceConnectType.PARTICULIERS;
  }

  @Override
  protected Class<? extends Api> getApiClass() {
    return FranceConnectParticuliersApi.class;
  }
}

