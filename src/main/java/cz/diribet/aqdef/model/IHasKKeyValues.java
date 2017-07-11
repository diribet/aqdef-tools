package cz.diribet.aqdef.model;

import cz.diribet.aqdef.KKey;

/**
 * Simple interface that enable access to object fields using K-keys.
 *
 * @author Vlastimil Dolejs
 *
 */
public interface IHasKKeyValues {

	public <T> T getValue(KKey kKey);

}
