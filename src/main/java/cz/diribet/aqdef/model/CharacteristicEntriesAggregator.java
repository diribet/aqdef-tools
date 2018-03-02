package cz.diribet.aqdef.model;

import static java.util.Objects.requireNonNull;

import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.KKey.Level;

/**
 * Aggregates entries of characteristic and part.
 * This simplifies access to the values of the K-keys and passing of the entries to the other objects (crate).
 *
 * @author Vlastimil Dolejs
 *
 */
public class CharacteristicEntriesAggregator implements IHasKKeyValues {
	//*******************************************
	// Attributes
	//*******************************************

	private final IHasKKeyValues partEntries;
	private final IHasKKeyValues characteristicEntries;

	//*******************************************
	// Constructors
	//*******************************************

	public CharacteristicEntriesAggregator(IHasKKeyValues partEntries, IHasKKeyValues characteristicEntries) {
		super();

		this.partEntries = requireNonNull(partEntries);
		this.characteristicEntries = requireNonNull(characteristicEntries);
	}

	//*******************************************
	// Methods
	//*******************************************

	@Override
	public <T> T getValue(KKey kKey) {
		Level level = kKey.getLevel();

		switch (level) {
			case PART:
			case CUSTOM_PART:
				return partEntries.getValue(kKey);

			case CHARACTERISTIC:
			case CUSTOM_CHARACTERISTIC:
				return characteristicEntries.getValue(kKey);

			default:
				return null;
		}
	}

	//*******************************************
	// Getters / setters
	//*******************************************

	public IHasKKeyValues getPartEntries() {
		return partEntries;
	}

	public IHasKKeyValues getCharacteristicEntries() {
		return characteristicEntries;
	}

}
