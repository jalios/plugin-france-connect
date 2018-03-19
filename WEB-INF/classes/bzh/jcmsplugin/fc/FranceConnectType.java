package bzh.jcmsplugin.fc;

/**
 * Type of FranceConnect connexion
 * 
 * @since fc-1.9
 */
public enum FranceConnectType {
  ENTREPRISES(),
  PARTICULIERS();
  
  /**
   * Retrieve the technical suffix used in configuration and I18N properties 
   * to differentiate implementation of connexion type.<p>
   * Current implementation is to use the lower case name of this enum value.
   * @return a suffix such as "entreprises" (never return null)
   */
  public String getSuffix() {
    return toString().toLowerCase();
  }
}