package cz.diribet.aqdef.model;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.KKey.Level;

/**
 * Aggregates entries of value, characteristic and part.
 * This simplifies access to values of K-keys and passing of entries to other objects (crate).
 *
 * @author Vlastimil Dolejs
 *
 */
public class ValueEntriesAggregator implements IHasKKeyValues {
	//*******************************************
	// Attributes
	//*******************************************

	private static final Logger LOG = LoggerFactory.getLogger(ValueEntriesAggregator.class);

	private final IHasKKeyValues partEntries;
	private final IHasKKeyValues characteristicEntries;
	private final IHasKKeyValues valueEntries;

	//*******************************************
	// Constructors
	//*******************************************

	public ValueEntriesAggregator(IHasKKeyValues partEntries, IHasKKeyValues characteristicEntries, IHasKKeyValues valueEntries) {
		super();

		requireNonNull(partEntries);
		requireNonNull(characteristicEntries);
		requireNonNull(valueEntries);

		this.partEntries = partEntries;
		this.characteristicEntries = characteristicEntries;
		this.valueEntries = valueEntries;
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

			case VALUE:
			case CUSTOM_VALUE:
				return valueEntries.getValue(kKey);

			default:
				LOG.warn("Unknown k-key level: " + kKey);
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

	public IHasKKeyValues getValueEntries() {
		return valueEntries;
	}

}
