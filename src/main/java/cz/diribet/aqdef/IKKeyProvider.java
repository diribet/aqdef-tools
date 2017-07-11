package cz.diribet.aqdef;

import java.util.Map;

/**
 * Service Provider Interface that allows clients to contribute own custom K-keys with metadata 
 * This can also be used to to override metadata of predefined K-keys.
 *
 * @author Vlastimil Dolejs
 *
 */
public interface IKKeyProvider {

	Map<KKey, KKeyMetadata> createKKeysWithMetadata();

}
