package cz.diribet.aqdef;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import cz.diribet.aqdef.catalog.CatalogField;
import cz.diribet.aqdef.convert.IKKeyValueConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

/**
 * K-key (also referred to as 'Key field' / 'K field' in the documentation of AQDEF format)
 * <p>
 * Represents single key in AQDEF format (e.g. K1001) and can be used to create AQDEF data structure (K-key : value pairs).
 * </p>
 * <p>
 * You can obtain {@link KKey} instance by calling {@link KKey#of(String)}. <br>
 * K-key instances are thread safe, immutable and cached. If you call {@link KKey#of(String)} for the same K-key multiple times,
 * you will probably get the same instances of {@link KKey} (this behavior is not guaranteed).
 * </p>
 * <p>
 * K-key also provides some {@link KKeyMetadata metadata} like datatype of the K-key. These can be retrieved like this:
 * <pre>
 *  KKey kKey = KKey.of("K1001");
 *  KKeyMetadata metadata = kKey.getMetadata();
 * </pre>
 * Or you can directly read metadata properties using delegate methods {@link #getDataType()}, {@link #getConverter()} etc.
 * </p>
 *
 *
 * @author Vlastimil Dolejs
 *
 */
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class KKey implements Serializable, Comparable<KKey> {

	public enum Level {
		PART, CHARACTERISTIC, VALUE,
		CUSTOM_PART, CUSTOM_CHARACTERISTIC, CUSTOM_VALUE,
		GROUP, HIERARCHY, SIMPLE_HIERARCHY, CATALOG, CUSTOM_CATALOG, UNKNOWN
	}

	private static final LoadingCache<String, KKey> CACHE = CacheBuilder.newBuilder().weakValues().build(new CacheLoader<>() {

		@Override
		public KKey load(String key) throws Exception {
			return new KKey(key);
		}
	});

	@Getter
	@EqualsAndHashCode.Include
	private final String key;

	private transient Level level;
	private transient KKeyMetadata metadata;

	private KKey(String key) {
		this.key = key;
	}

	/**
	 * Gets the instance of {@link KKey} for the given {@code key}.
	 * Returned instance may not be a new instance, but reused instance from cache.
	 *
	 * @param key
	 * @return
	 */
	public static KKey of(String key) {
		try {
			return CACHE.get(key);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * See {@link KKeyMetadata#getConverter()}
	 *
	 * @return
	 */
	public IKKeyValueConverter<?> getConverter() {
		if (metadata == null) {
			metadata = findMetadata();
		}

		if (metadata == null || metadata.getConverter() == null) {
			log.error("Can't find converter for k-key: {}", key);
			return null;
		} else {
			return metadata.getConverter();
		}
	}

	/**
	 * See {@link KKeyMetadata#getColumnName()}
	 *
	 * @return
	 */
	public String getDbColumnName() {
		if (metadata == null) {
			metadata = findMetadata();
		}

		if (metadata == null || metadata.getColumnName() == null) {
			log.error("Can't find DB column name for k-key: {}", key);
			return null;
		} else {
			return metadata.getColumnName();
		}
	}

	/**
	 * See {@link KKeyMetadata#getDataType()}
	 *
	 * @return
	 */
	public Class<?> getDataType() {
		if (metadata == null) {
			metadata = findMetadata();
		}

		if (metadata == null || metadata.getDataType() == null) {
			log.error("Can't find data type of k-key: {}", key);
			return null;
		} else {
			return metadata.getDataType();
		}
	}

	/**
	 * See {@link KKeyMetadata#isRespectsCharacteristicDecimalSettings()}
	 *
	 * @return
	 */
	public boolean isRespectsCharacteristicDecimalSettings() {
		if (metadata == null) {
			metadata = findMetadata();
		}

		if (metadata == null) {
			log.error("Can't find metadata of k-key: {}", key);
			return false;
		} else {
			return metadata.isRespectsCharacteristicDecimalSettings();
		}
	}

	/**
	 * Gets {@link KKeyMetadata metadata} of this K-key
	 *
	 * @return
	 */
	public KKeyMetadata getMetadata() {
		if (metadata == null) {
			metadata = findMetadata();

			if (metadata == null) {
				log.error("Can't find metadata for k-key: {}", key);
				return null;
			}
		}

		return metadata;
	}

	private KKeyMetadata findMetadata() {
		if (getLevel() == Level.CATALOG) {
			return CatalogField.getMetadataFor(this);
		} else {
			return KKeyRepository.getInstance().getMetadataFor(this);
		}
	}

	/**
	 * Returns {@link Level} of the AQDEF structure this K-key belongs to.
	 *
	 * @return
	 */
	public Level getLevel() {
		if (level == null) {
			level = determineLevel();
		}
		return level;
	}

	private Level determineLevel() {
		if (key == null) {
			log.error("K-key with missing key");
			return Level.UNKNOWN;
		}

		if (key.equalsIgnoreCase("K2030") || key.equalsIgnoreCase("K2031")) {
			return Level.SIMPLE_HIERARCHY;

		} else if (key.startsWith("K1")) {
			return Level.PART;

		} else if (key.startsWith("K2") // characteristic properties
				|| key.startsWith("K8")) { // control chart properties

			return Level.CHARACTERISTIC;

		} else if (key.startsWith("K0")) {
			return Level.VALUE;

		} else if (key.startsWith("K4")) {
			return Level.CATALOG;

		} else if (key.startsWith("K50")) {
			return Level.GROUP;

		} else if (key.startsWith("K51")) {
			return Level.HIERARCHY;

		} else if (key.startsWith("KX0")) {
			return Level.CUSTOM_VALUE;

		} else if (key.startsWith("KX1")) {
			return Level.CUSTOM_PART;

		} else if (key.startsWith("KX2")) {
			return Level.CUSTOM_CHARACTERISTIC;

		} else if (key.startsWith("KX4")) {
			return Level.CUSTOM_CATALOG;

		} else {
			log.error("Unknown level of k-key: {}", key);
			return Level.UNKNOWN;
		}
	}
	/**
	 * @return whether this key represents information for part (dil / teil)
	 */
	public boolean isPartLevel() {
		return getLevel() == Level.PART;
	}

	/**
	 * @return whether this key represents information for part (dil / teil) and is defined as custom
	 */
	public boolean isCustomPartLevel() {
		return getLevel() == Level.CUSTOM_PART;
	}

	/**
	 * @return whether this key represents information for part (dil / teil) regardless whether it is custom or not
	 */
	public boolean isAnyPartLevel() {
		return isPartLevel() || isCustomPartLevel();
	}

	/**
	 * @return whether this key represents information for characterstic (znak / merkmal)
	 */
	public boolean isCharacteristicLevel() {
		return getLevel() == Level.CHARACTERISTIC;
	}

	/**
	 * @return whether this key represents information for characterstic (znak / merkmal) and is defined as custom
	 */
	public boolean isCustomCharacteristicLevel() {
		return getLevel() == Level.CUSTOM_CHARACTERISTIC;
	}

	/**
	 * @return whether this key represents information for characterstic (znak / merkmal) regardless whether it is custom or not
	 */
	public boolean isAnyCharacteristicLevel() {
		return isCharacteristicLevel() || isCustomCharacteristicLevel();
	}

	/**
	 * @return whether this key represents information for value (hodnota / wertevar)
	 */
	public boolean isValueLevel() {
		return getLevel() == Level.VALUE;
	}

	/**
	 * @return whether this key represents information for value (hodnota / wertevar) and is defined as custom
	 */
	public boolean isCustomValueLevel() {
		return getLevel() == Level.CUSTOM_VALUE;
	}

	/**
	 * @return whether this key represents information for value (hodnota / wertevar) regardless whether it is custom or not
	 */
	public boolean isAnyValueLevel() {
		return isValueLevel() || isCustomValueLevel();
	}

	/**
	 * @return whether this key represents information about logical group
	 */
	public boolean isGroupLevel() {
		return getLevel() == Level.GROUP;
	}

	/**
	 * @return whether this key represents information about hierarchy
	 */
	public boolean isHierarchyLevel() {
		return getLevel() == Level.HIERARCHY;
	}

	/**
	 * @return whether this key represents information about simple hierarchy
	 */
	public boolean isSimpleHierarchyLevel() {
		return getLevel() == Level.SIMPLE_HIERARCHY;
	}

	/**
	 * @return whether this key represents information about catalog record
	 */
	public boolean isCatalogLevel() {
		return getLevel() == Level.CATALOG;
	}

	/**
	 * @return whether this key represents information about catalog record and is defined as custom
	 */
	public boolean isCustomCatalogLevel() {
		return getLevel() == Level.CUSTOM_CATALOG;
	}

	/**
	 * @return whether this key represents information about catalog record regardless whether it is custom or not
	 */
	public boolean isAnyCatalogLevel() {
		return isCatalogLevel() || isCustomCatalogLevel();
	}

	/**
	 * @return whether this key represents our custom key
	 */
	public boolean isCustom() {
		return isCustomPartLevel() || isCustomCharacteristicLevel() || isCustomCatalogLevel() || isCustomValueLevel();
	}

	/**
	 * @return whether this k-key should be written to DFQ file
	 */
	public boolean shouldBeWrittenToDfq() {
        return !isCustom();
    }

	@Override
	public String toString() {
		return key;
	}

	@Override
	public int compareTo(KKey o) {
		if (key == null) {
			return o.key == null ? 0 : 1;

		} else if (o.key == null) {
			return -1;
		}

		String thisKey = keyForCompare(key);
		String otherKey = keyForCompare(o.key);

		return thisKey.compareTo(otherKey);
	}

	private String keyForCompare(String key) {
		// K0020 and K0021 has to be written of the same position as K0001 - this will change the order for these 2 keys
		if (key.equals("K0020")) {
			return "K0001.20";
		}

		if (key.equals("K0021")) {
			return "K0001.21";
		}

		return key;
	}
}
