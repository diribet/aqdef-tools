package cz.diribet.aqdef;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import cz.diribet.aqdef.catalog.Catalog;
import cz.diribet.aqdef.catalog.CatalogField;
import cz.diribet.aqdef.convert.BooleanKKeyValueConverter;
import cz.diribet.aqdef.convert.IKKeyValueConverter;
import cz.diribet.aqdef.convert.custom.K0005ValueConverter;
import cz.diribet.aqdef.convert.custom.K0006ValueConverter;
import cz.diribet.aqdef.convert.custom.K0020ValueConverter;

/**
 * Repository that stores all K-keys (with metadata) of:
 * <ul>
 *   <li>part (K1xxx)</li>
 *   <li>characteristic (K2xxx)</li>
 *   <li>value (K0xxx)</li>
 *   <li>structure (K5xxx)</li>
 *   <li>quality control charts (K8xxx)</li>
 * </ul>
 * If you are looking for catalog K-keys (K4xxx) see {@link CatalogField} and {@link Catalog} enums.
 * <p>
 * Repository is a singleton. It can be retrieved by {@link #getInstance()} method.
 * </p>
 * @author Vlastimil Dolejs
 *
 */
public final class KKeyRepository {
	//*******************************************
	// Attributes
	//*******************************************

	private static final KKeyRepository INSTANCE = new KKeyRepository();

	private final ImmutableMap<KKey, KKeyMetadata> kKeysWithMetadata;

	private final ImmutableList<KKey> allKKeys;
	private final ImmutableList<KKey> partKKeys;
	private final ImmutableList<KKey> characteristicKKeys;
	private final ImmutableList<KKey> valueKKeys;

	//*******************************************
	// Constructors
	//*******************************************

	private KKeyRepository() {
		SortedMap<KKey, KKeyMetadata> kKeysWithMetadata = new TreeMap<>();
		kKeysWithMetadata.putAll(new DefaultKKeyProvider().createKKeysWithMetadata());
		kKeysWithMetadata.putAll(new CustomKKeyProvider().createKKeysWithMetadata()); /* override and fill in missing K-keys */

		ServiceLoader<IKKeyProvider> thirdpartyProviders = ServiceLoader.load(IKKeyProvider.class);
		for (IKKeyProvider provider : thirdpartyProviders) {
			kKeysWithMetadata.putAll(provider.createKKeysWithMetadata());
		}

		this.kKeysWithMetadata = ImmutableMap.copyOf(kKeysWithMetadata);

		this.allKKeys = filteredKKeys(this.kKeysWithMetadata, (e) -> true);
		this.partKKeys = filteredKKeys(this.kKeysWithMetadata, (e) -> e.isPartLevel());
		this.characteristicKKeys = filteredKKeys(this.kKeysWithMetadata, (e) -> e.isCharacteristicLevel());
		this.valueKKeys = filteredKKeys(this.kKeysWithMetadata, (e) -> e.isValueLevel());
	}

	public static KKeyRepository getInstance() {
		return INSTANCE;
	}

	//*******************************************
	// Methods
	//*******************************************

	private ImmutableList<KKey> filteredKKeys(Map<KKey, KKeyMetadata> map, Predicate<KKey> predicate) {
		List<KKey> filteredKKeys = map.keySet()
											.stream()
											.filter(predicate)
											.sorted()
											.collect(toList());
		return ImmutableList.copyOf(filteredKKeys);
	}

	/**
	 * Finds {@link KKeyMetadata metadata} for a given K-key.
	 * <p>
	 * Returns <b>null</b> if there is no metadata for the given K-key.
	 * </p>
	 *
	 * @param kKey
	 * @return
	 */
	public KKeyMetadata getMetadataFor(KKey kKey) {
		return kKeysWithMetadata.get(kKey);
	}

	/**
	 * @return all sorted K-keys
	 */
	public ImmutableList<KKey> getAllKKeys() {
		return allKKeys;
	}

	/**
	 * @return sorted K-keys of part
	 */
	public ImmutableList<KKey> getPartKKeys() {
		return partKKeys;
	}

	/**
	 * @return sorted K-keys of characteristic
	 */
	public ImmutableList<KKey> getCharacteristicKKeys() {
		return characteristicKKeys;
	}

	/**
	 * @return sorted K-keys of value
	 */
	public ImmutableList<KKey> getValueKKeys() {
		return valueKKeys;
	}

	//*******************************************
	// Inner classes
	//*******************************************

	/**
	 * Custom K-keys with metadata that will fill in missing or override incorrect keys.
	 */
	private static class CustomKKeyProvider implements IKKeyProvider {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Map<KKey, KKeyMetadata> createKKeysWithMetadata() {
			Map<KKey, KKeyMetadata> keys = new HashMap<>();

			// replace of wrongly generated key
			keys.put(KKey.of("K0001"), KKeyMetadata.builder()
													.columnName("WVWERT")
													.dataType(BigDecimal.class)
													.respectsCharacteristicDecimalSettings(true)
													.build());

			// target / nominal
			keys.put(KKey.of("K2100"), KKeyMetadata.builder()
													.columnName("MEZIELWERT")
													.dataType(BigDecimal.class)
													.respectsCharacteristicDecimalSettings(true)
													.build());

			keys.put(KKey.of("K2101"), KKeyMetadata.builder()
													.columnName("MENENNMAS")
													.dataType(BigDecimal.class)
													.respectsCharacteristicDecimalSettings(true)
													.build());

			// limits
			keys.put(KKey.of("K2110"), KKeyMetadata.builder()
													.columnName("MEUGW")
													.dataType(BigDecimal.class)
													.respectsCharacteristicDecimalSettings(true)
													.build());

			keys.put(KKey.of("K2111"), KKeyMetadata.builder()
													.columnName("MEOGW")
													.dataType(BigDecimal.class)
													.respectsCharacteristicDecimalSettings(true)
													.build());

			keys.put(KKey.of("K2114"), KKeyMetadata.builder()
													.columnName("MEUGSCHROTT")
													.dataType(BigDecimal.class)
													.respectsCharacteristicDecimalSettings(true)
													.build());

			keys.put(KKey.of("K2115"), KKeyMetadata.builder()
													.columnName("MEOGSCHROTT")
													.dataType(BigDecimal.class)
													.respectsCharacteristicDecimalSettings(true)
													.build());

			keys.put(KKey.of("K2116"), KKeyMetadata.builder()
													.columnName("MENORMISTUN")
													.dataType(BigDecimal.class)
													.respectsCharacteristicDecimalSettings(true)
													.build());

			keys.put(KKey.of("K2117"), KKeyMetadata.builder()
													.columnName("MENORMISTOB")
													.dataType(BigDecimal.class)
													.respectsCharacteristicDecimalSettings(true)
													.build());

			keys.put(KKey.of("K2130"), KKeyMetadata.builder()
													.columnName("MEPLAUSIUN")
													.dataType(BigDecimal.class)
													.respectsCharacteristicDecimalSettings(true)
													.build());

			keys.put(KKey.of("K2131"), KKeyMetadata.builder()
													.columnName("MEPLAUSIOB")
													.dataType(BigDecimal.class)
													.respectsCharacteristicDecimalSettings(true)
													.build());


			// K0005 has special converter that converts it to list of event ids (List<Integer>)
			keys.put(KKey.of("K0005"), KKeyMetadata.builder()
													.columnName("WV0005")
													.dataType(List.class, (IKKeyValueConverter) new K0005ValueConverter())
													.build());

			// K0006 has special converter which trims leading # sign
			keys.put(KKey.of("K0006"), KKeyMetadata.builder()
													.columnName("WVCHARGE")
													.dataType(String.class, new K0006ValueConverter())
													.build());

			// K0011 has special coding similar to K0005, but we do not support conversion of this field
			keys.put(KKey.of("K0011"), KKeyMetadata.of("WV0011", String.class));

			// surrogate keys of part and characteristic
			keys.put(KKey.of("K1000"), KKeyMetadata.of("TETEIL", Integer.class, false)); //Teile-Id  (K1000)
			keys.put(KKey.of("K2000"), KKeyMetadata.of("MEMERKMAL", Integer.class, false));

			// K1017 is boolean
			keys.put(KKey.of("K1017"), KKeyMetadata.builder()
													.columnName("TE_1017")
													.dataType(Boolean.class, new BooleanKKeyValueConverter()) // Integer 3 in the DB
													.build());

			// specification limits defined as relative allowance to the nominal
			// they are not stored in DB (absolute specification limits K2110/K2111 are calculated from them and stored in DB)
			keys.put(KKey.of("K2112"), KKeyMetadata.builder()
													.columnName("?K2112?")
													.dataType(BigDecimal.class)
													.saveToDb(false)
													.respectsCharacteristicDecimalSettings(true)
													.build());

			keys.put(KKey.of("K2113"), KKeyMetadata.builder()
													.columnName("?K2113?")
													.dataType(BigDecimal.class)
													.saveToDb(false)
													.respectsCharacteristicDecimalSettings(true)
													.build());

			// attribute values and error log sheets - these two keys are encoded to K0001 DB column
			keys.put(KKey.of("K0020"), KKeyMetadata.builder()
													.columnName("?K0020?")
													.dataType(Integer.class, new K0020ValueConverter())
													.saveToDb(false)
													.build()); // subgroup size
			keys.put(KKey.of("K0021"), KKeyMetadata.of("?K0021?", Integer.class, false)); // number of errors

			// missings K-keys
			keys.put(KKey.of("K1040"), KKeyMetadata.of("?K1040?", Integer.class, 5, false));
			keys.put(KKey.of("K2008"), KKeyMetadata.of("MEPRUEFORT", Integer.class)); // group type
			keys.put(KKey.of("K2015"), KKeyMetadata.of("MEGLMITT", Integer.class, 3));
			keys.put(KKey.of("K2023"), KKeyMetadata.of("?K2023?", Integer.class, 3, false)); //Transformationsart (K2023)
			keys.put(KKey.of("K2031"), KKeyMetadata.of("MEUPPERMERKMAL", Integer.class, 5));
			keys.put(KKey.of("K2076"), KKeyMetadata.of("MEPRUEFBEGINND", Date.class));
			keys.put(KKey.of("K2080"), KKeyMetadata.of("MEMASSN", Integer.class, 5));
			keys.put(KKey.of("K2143"), KKeyMetadata.of("MEEINHREL", String.class, 20));
			keys.put(KKey.of("K2144"), KKeyMetadata.of("MEADDFAKREL", BigDecimal.class));
			keys.put(KKey.of("K2145"), KKeyMetadata.of("MEMULFAKREL", BigDecimal.class));
			keys.put(KKey.of("K2437"), KKeyMetadata.of("MEPRUEFENDED", Date.class));


			keys.put(KKey.of("K0054"), KKeyMetadata.of("WV0054", String.class, 32));
			keys.put(KKey.of("K0055"), KKeyMetadata.of("WV0055", String.class, 32));
			keys.put(KKey.of("K0056"), KKeyMetadata.of("WV0056", String.class, 32));
			keys.put(KKey.of("K0057"), KKeyMetadata.of("WV0057", String.class, 32));
			keys.put(KKey.of("K0058"), KKeyMetadata.of("WV0058", String.class, 32));
			keys.put(KKey.of("K0059"), KKeyMetadata.of("WV0059", String.class, 32));
			keys.put(KKey.of("K0060"), KKeyMetadata.of("WV0060", String.class, 32));
			keys.put(KKey.of("K0061"), KKeyMetadata.of("WV0061", Integer.class, 10));
			keys.put(KKey.of("K0062"), KKeyMetadata.of("WV0062", Integer.class, 10));
			keys.put(KKey.of("K0063"), KKeyMetadata.of("WV0063", Integer.class, 10));
			keys.put(KKey.of("K0080"), KKeyMetadata.of("WV0080", String.class, 64));
			keys.put(KKey.of("K0081"), KKeyMetadata.of("WV0081", Integer.class, 5));

			// K500x

			// keys of logical group data - in DB they are written as K200x but with different characteristic type
			// In AQDEF they are written as separate keys
			keys.put(KKey.of("K5001"), KKeyMetadata.of("MEMERKNR", String.class, 20));
			keys.put(KKey.of("K5002"), KKeyMetadata.of("MEMERKBEZ", String.class, 80));
			keys.put(KKey.of("K5003"), KKeyMetadata.of("MEKURZBEZ", String.class, 20));

			keys.put(KKey.of("K5007"), KKeyMetadata.of("?K5007?", String.class, 20, false));
			keys.put(KKey.of("K5045"), KKeyMetadata.of("?K5045?", String.class, 80, false));
			keys.put(KKey.of("K5090"), KKeyMetadata.of("?K5090?", String.class, 255, false));

			// hierarchy
			keys.put(KKey.of("K5101"), KKeyMetadata.of("?K5101?", Integer.class, false));
			keys.put(KKey.of("K5102"), KKeyMetadata.of("?K5102?", Integer.class, false));
			keys.put(KKey.of("K5103"), KKeyMetadata.of("?K5103?", Integer.class, false));
			keys.put(KKey.of("K5111"), KKeyMetadata.of("?K5111?", Integer.class, false));
			keys.put(KKey.of("K5112"), KKeyMetadata.of("?K5112?", Integer.class, false));
			keys.put(KKey.of("K5113"), KKeyMetadata.of("?K5113?", Integer.class, false));

			// missing K8xxx keys - we don't know how they are stored in DB
			keys.put(KKey.of("K8006"), KKeyMetadata.of("?K8006?", BigDecimal.class, false));
			keys.put(KKey.of("K8007"), KKeyMetadata.of("?K8007?", BigDecimal.class, false));
			keys.put(KKey.of("K8010"), KKeyMetadata.of("?K8010?", String.class, false));
			keys.put(KKey.of("K8011"), KKeyMetadata.of("?K8011?", BigDecimal.class, false));
			keys.put(KKey.of("K8012"), KKeyMetadata.of("?K8012?", BigDecimal.class, false));
			keys.put(KKey.of("K8013"), KKeyMetadata.of("?K8013?", BigDecimal.class, false));
			keys.put(KKey.of("K8014"), KKeyMetadata.of("?K8014?", BigDecimal.class, false));
			keys.put(KKey.of("K8015"), KKeyMetadata.of("?K8015?", BigDecimal.class, false));
			keys.put(KKey.of("K8106"), KKeyMetadata.of("?K8106?", BigDecimal.class, false));
			keys.put(KKey.of("K8107"), KKeyMetadata.of("?K8107?", BigDecimal.class, false));
			keys.put(KKey.of("K8110"), KKeyMetadata.of("?K8110?", String.class, false));
			keys.put(KKey.of("K8111"), KKeyMetadata.of("?K8111?", BigDecimal.class, false));
			keys.put(KKey.of("K8112"), KKeyMetadata.of("?K8112?", BigDecimal.class, false));
			keys.put(KKey.of("K8113"), KKeyMetadata.of("?K8113?", BigDecimal.class, false));
			keys.put(KKey.of("K8114"), KKeyMetadata.of("?K8114?", BigDecimal.class, false));
			keys.put(KKey.of("K8115"), KKeyMetadata.of("?K8115?", BigDecimal.class, false));

			// missing K85xx - they are related to characteristic
			keys.put(KKey.of("K8503"), KKeyMetadata.of("METRANSART", Integer.class, 3, false));
			keys.put(KKey.of("K8505"), KKeyMetadata.of("?K8505?", Integer.class, 5, false));
			keys.put(KKey.of("K8524"), KKeyMetadata.of("?K8524?", BigDecimal.class, false));
			keys.put(KKey.of("K8525"), KKeyMetadata.of("?K8525?", BigDecimal.class, false));

			// part user fields
			keys.put(KKey.of("K1800"), KKeyMetadata.of("?K1800?", String.class, 50, false));
			keys.put(KKey.of("K1801"), KKeyMetadata.of("?K1801?", String.class, 1, false));
			keys.put(KKey.of("K1802"), KKeyMetadata.of("?K1802?", String.class, 255, false));
			keys.put(KKey.of("K1810"), KKeyMetadata.of("?K1810?", String.class, 50, false));
			keys.put(KKey.of("K1811"), KKeyMetadata.of("?K1811?", String.class, 1, false));
			keys.put(KKey.of("K1812"), KKeyMetadata.of("?K1812?", String.class, 255, false));
			keys.put(KKey.of("K1820"), KKeyMetadata.of("?K1820?", String.class, 50, false));
			keys.put(KKey.of("K1821"), KKeyMetadata.of("?K1821?", String.class, 1, false));
			keys.put(KKey.of("K1822"), KKeyMetadata.of("?K1822?", String.class, 255, false));
			keys.put(KKey.of("K1830"), KKeyMetadata.of("?K1830?", String.class, 50, false));
			keys.put(KKey.of("K1831"), KKeyMetadata.of("?K1831?", String.class, 1, false));
			keys.put(KKey.of("K1832"), KKeyMetadata.of("?K1832?", String.class, 255, false));
			keys.put(KKey.of("K1840"), KKeyMetadata.of("?K1840?", String.class, 50, false));
			keys.put(KKey.of("K1841"), KKeyMetadata.of("?K1841?", String.class, 1, false));
			keys.put(KKey.of("K1842"), KKeyMetadata.of("?K1842?", String.class, 255, false));
			keys.put(KKey.of("K1850"), KKeyMetadata.of("?K1850?", String.class, 50, false));
			keys.put(KKey.of("K1851"), KKeyMetadata.of("?K1851?", String.class, 1, false));
			keys.put(KKey.of("K1852"), KKeyMetadata.of("?K1852?", String.class, 255, false));
			keys.put(KKey.of("K1860"), KKeyMetadata.of("?K1860?", String.class, 50, false));
			keys.put(KKey.of("K1861"), KKeyMetadata.of("?K1861?", String.class, 1, false));
			keys.put(KKey.of("K1862"), KKeyMetadata.of("?K1862?", String.class, 255, false));
			keys.put(KKey.of("K1870"), KKeyMetadata.of("?K1870?", String.class, 50, false));
			keys.put(KKey.of("K1871"), KKeyMetadata.of("?K1871?", String.class, 1, false));
			keys.put(KKey.of("K1872"), KKeyMetadata.of("?K1872?", String.class, 255, false));
			keys.put(KKey.of("K1880"), KKeyMetadata.of("?K1880?", String.class, 50, false));
			keys.put(KKey.of("K1881"), KKeyMetadata.of("?K1881?", String.class, 1, false));
			keys.put(KKey.of("K1882"), KKeyMetadata.of("?K1882?", String.class, 255, false));
			keys.put(KKey.of("K1890"), KKeyMetadata.of("?K1890?", String.class, 50, false));
			keys.put(KKey.of("K1891"), KKeyMetadata.of("?K1891?", String.class, 1, false));
			keys.put(KKey.of("K1892"), KKeyMetadata.of("?K1892?", String.class, 255, false));

			// characteristic user fields
			keys.put(KKey.of("K2800"), KKeyMetadata.of("?K2800?", String.class, 50, false));
			keys.put(KKey.of("K2801"), KKeyMetadata.of("?K2801?", String.class, 1, false));
			keys.put(KKey.of("K2802"), KKeyMetadata.of("?K2802?", String.class, 255, false));
			keys.put(KKey.of("K2810"), KKeyMetadata.of("?K2810?", String.class, 50, false));
			keys.put(KKey.of("K2811"), KKeyMetadata.of("?K2811?", String.class, 1, false));
			keys.put(KKey.of("K2812"), KKeyMetadata.of("?K2812?", String.class, 255, false));
			keys.put(KKey.of("K2820"), KKeyMetadata.of("?K2820?", String.class, 50, false));
			keys.put(KKey.of("K2821"), KKeyMetadata.of("?K2821?", String.class, 1, false));
			keys.put(KKey.of("K2822"), KKeyMetadata.of("?K2822?", String.class, 255, false));
			keys.put(KKey.of("K2830"), KKeyMetadata.of("?K2830?", String.class, 50, false));
			keys.put(KKey.of("K2831"), KKeyMetadata.of("?K2831?", String.class, 1, false));
			keys.put(KKey.of("K2832"), KKeyMetadata.of("?K2832?", String.class, 255, false));
			keys.put(KKey.of("K2840"), KKeyMetadata.of("?K2840?", String.class, 50, false));
			keys.put(KKey.of("K2841"), KKeyMetadata.of("?K2841?", String.class, 1, false));
			keys.put(KKey.of("K2842"), KKeyMetadata.of("?K2842?", String.class, 255, false));
			keys.put(KKey.of("K2850"), KKeyMetadata.of("?K2850?", String.class, 50, false));
			keys.put(KKey.of("K2851"), KKeyMetadata.of("?K2851?", String.class, 1, false));
			keys.put(KKey.of("K2852"), KKeyMetadata.of("?K2852?", String.class, 255, false));
			keys.put(KKey.of("K2860"), KKeyMetadata.of("?K2860?", String.class, 50, false));
			keys.put(KKey.of("K2861"), KKeyMetadata.of("?K2861?", String.class, 1, false));
			keys.put(KKey.of("K2862"), KKeyMetadata.of("?K2862?", String.class, 255, false));
			keys.put(KKey.of("K2870"), KKeyMetadata.of("?K2870?", String.class, 50, false));
			keys.put(KKey.of("K2871"), KKeyMetadata.of("?K2871?", String.class, 1, false));
			keys.put(KKey.of("K2872"), KKeyMetadata.of("?K2872?", String.class, 255, false));
			keys.put(KKey.of("K2880"), KKeyMetadata.of("?K2880?", String.class, 50, false));
			keys.put(KKey.of("K2881"), KKeyMetadata.of("?K2881?", String.class, 1, false));
			keys.put(KKey.of("K2882"), KKeyMetadata.of("?K2882?", String.class, 255, false));
			keys.put(KKey.of("K2890"), KKeyMetadata.of("?K2890?", String.class, 50, false));
			keys.put(KKey.of("K2891"), KKeyMetadata.of("?K2891?", String.class, 1, false));
			keys.put(KKey.of("K2892"), KKeyMetadata.of("?K2892?", String.class, 255, false));

			return keys;
		}

	}

	/**
	 * Default K-keys with metadata automatically generated from Q-DAS MDB database
	 */
	private static class DefaultKKeyProvider implements IKKeyProvider {

		@Override
		public Map<KKey, KKeyMetadata> createKKeysWithMetadata() {
			Map<KKey, KKeyMetadata> keys = new HashMap<>();

			keys.put(KKey.of("K1001"), KKeyMetadata.of("TETEILNR", String.class, 30)); //Teile-Nr  (K1001)
			keys.put(KKey.of("K1002"), KKeyMetadata.of("TEBEZEICH", String.class, 80)); //Bezeichnung des Teiles (K1002)
			keys.put(KKey.of("K1010"), KKeyMetadata.of("TEDPFLICHT", Integer.class, 3)); //Dokumentationspflicht / Wichtung  (K1010)
			keys.put(KKey.of("K1900"), KKeyMetadata.of("TEBEM", String.class, 255)); //Bemerkung (K1900)
			keys.put(KKey.of("K1021"), KKeyMetadata.of("TEHERSTELLERNR", String.class, 20)); //Hersteller-Nr. (K1021)
			keys.put(KKey.of("K1022"), KKeyMetadata.of("TEHERSTELLERBEZ", String.class, 80)); //Hersteller-Bezeichnung (K1022)
			keys.put(KKey.of("K1031"), KKeyMetadata.of("TEWERKSTOFFNR", String.class, 20)); //Werkstoff-Nr. (K1031)
			keys.put(KKey.of("K1032"), KKeyMetadata.of("TEWERKSTOFFBEZ", String.class, 40)); //Werkstoff-Bezeichnung (K1032)
			keys.put(KKey.of("K1041"), KKeyMetadata.of("TEZEICHNUNGNR", String.class, 30)); //Zeichnuns-Nr. (K1041)
			keys.put(KKey.of("K1042"), KKeyMetadata.of("TEZEICHNUNGAEND", String.class, 20)); //Zeichnung ï¿½nderung (K1042)
			keys.put(KKey.of("K1043"), KKeyMetadata.of("TEZEICHNUNGINDEX", String.class, 40)); //Zeichnung Index (K1043)
			keys.put(KKey.of("K1053"), KKeyMetadata.of("TEAUFTRAGSTR", String.class, 40)); //Auftrags-Nr. (K1053)
			keys.put(KKey.of("K1051"), KKeyMetadata.of("TEAUFTRAGGBNR", String.class, 20)); //Auftraggeber-Nr (K1051)
			keys.put(KKey.of("K1052"), KKeyMetadata.of("TEAUFTRAGGBBEZ", String.class, 40)); //Auftraggeber-Bezeichnung (K1052)
			keys.put(KKey.of("K1061"), KKeyMetadata.of("TEKUNDENR", String.class, 20)); //Kunden-Nr. (K1061)
			keys.put(KKey.of("K1062"), KKeyMetadata.of("TEKUNDEBEZ", String.class, 40)); //Kunden-Bezeichnung (K1062)
			keys.put(KKey.of("K1071"), KKeyMetadata.of("TELIEFERANTNR", String.class, 20)); //Lieferanten-Nr. (K1071)
			keys.put(KKey.of("K1072"), KKeyMetadata.of("TELIEFERANTBEZ", String.class, 40)); //Lieferanten-Bezeichnung (K1072)
			keys.put(KKey.of("K1201"), KKeyMetadata.of("TEPREINRNR", String.class, 24)); //Prï¿½feinrichtungs-Nr. (K1201)
			keys.put(KKey.of("K1202"), KKeyMetadata.of("TEPREINRBEZ", String.class, 40)); //Prï¿½feinrichtungs-Bezeichnung (K1202)
			keys.put(KKey.of("K1203"), KKeyMetadata.of("TEPRGRUNDBEZ", String.class, 80)); //Prï¿½fgrund (K1203)
			keys.put(KKey.of("K1204"), KKeyMetadata.of("TEPRBEGINNSTR", String.class, 40)); //Prï¿½fbeginn (K1204)
			keys.put(KKey.of("K1205"), KKeyMetadata.of("TEPRENDESTR", String.class, 40)); //Prï¿½fende (K1205)
			keys.put(KKey.of("K1003"), KKeyMetadata.of("TEKURZBEZEICH", String.class, 20)); //Teil Kurzbezeichnung (K1003)
			keys.put(KKey.of("K1004"), KKeyMetadata.of("TEAENDSTAND", String.class, 20)); //ï¿½nderungsstand des Teils (K1004)
			keys.put(KKey.of("K1005"), KKeyMetadata.of("TEERZEUGNIS", String.class, 40)); //Erzeugnis (K1005)
			keys.put(KKey.of("K1081"), KKeyMetadata.of("TEMASCHINENR", String.class, 24)); //Maschine Nummer (K1081)
			keys.put(KKey.of("K1082"), KKeyMetadata.of("TEMASCHINEBEZ", String.class, 40)); //Maschine Bezeichnung (K1082)
			keys.put(KKey.of("K1085"), KKeyMetadata.of("TEMASCHINEORT", String.class, 40)); //Maschine Standort (K1085)
			keys.put(KKey.of("K1086"), KKeyMetadata.of("TEARBEITSGANG", String.class, 40)); //Arbeitsgang (K1086)
			keys.put(KKey.of("K1100"), KKeyMetadata.of("TEBEREICH", String.class, 40)); //Bereich im Werk (K1100)
			keys.put(KKey.of("K1101"), KKeyMetadata.of("TEABT", String.class, 40)); //Abteilung (K1101)
			keys.put(KKey.of("K1206"), KKeyMetadata.of("TEPRPLATZ", String.class, 40)); //Prï¿½fplatz (K1206)
			keys.put(KKey.of("K1207"), KKeyMetadata.of("TEPPLANERST", String.class)); //Prï¿½fplanersteller (K1207)
			keys.put(KKey.of("K1023"), KKeyMetadata.of("TEHERSTELLERKEY", Integer.class, 5)); //Key des Herstellers (K1023)
			keys.put(KKey.of("K1033"), KKeyMetadata.of("TEWERKSTOFFKEY", Integer.class, 5)); //Key des Werkstoffes (K1033)
			keys.put(KKey.of("K1044"), KKeyMetadata.of("TEZEICHNUNGKEY", Integer.class, 5)); //Key der Zeichnung (K1044)
			keys.put(KKey.of("K1054"), KKeyMetadata.of("TEAUFTRAGGBKEY", Integer.class, 5)); //Key des Auftraggebers (K1054)
			keys.put(KKey.of("K1063"), KKeyMetadata.of("TEKUNDEKEY", Integer.class, 5)); //Key des Kunden (K1063)
			keys.put(KKey.of("K1073"), KKeyMetadata.of("TELIEFERANTKEY", Integer.class, 5)); //Key des Lieferanten (K1073)
			keys.put(KKey.of("K1083"), KKeyMetadata.of("TEMASCHINEKEY", Integer.class, 5)); //Key der Maschine (K1083)
			keys.put(KKey.of("K1208"), KKeyMetadata.of("TEPREINRKEY", Integer.class, 5)); //Key der Prï¿½feinrichtung (K1208)
			keys.put(KKey.of("K1007"), KKeyMetadata.of("TENRKURZ", String.class, 20)); //Teile-Nr. Kurzbezeichnung (K1007)
			keys.put(KKey.of("K1102"), KKeyMetadata.of("TEWERKSTATT", String.class, 40)); //Werkstatt (K1102)
			keys.put(KKey.of("K1211"), KKeyMetadata.of("TENORMNR", String.class, 20)); //Normal Nummer (K1211)
			keys.put(KKey.of("K1212"), KKeyMetadata.of("TENORMBEZ", String.class, 40)); //Normal Bezeichnung (K1212)
			keys.put(KKey.of("K1215"), KKeyMetadata.of("TENORMAL", Integer.class, 5)); //Normal (K1215)
			keys.put(KKey.of("K1008"), KKeyMetadata.of("TETYP", String.class, 20)); //Teiletyp (K1008)
			keys.put(KKey.of("K1009"), KKeyMetadata.of("TECODE", String.class, 20)); //Teilecode (K1009)
			keys.put(KKey.of("K1011"), KKeyMetadata.of("TEVARIANTE", String.class, 20)); //Variante (K1011)
			keys.put(KKey.of("K1012"), KKeyMetadata.of("TESACHNRZUS", String.class, 20)); //Sachnummer Zusatz (K1012)
			keys.put(KKey.of("K1013"), KKeyMetadata.of("TESACHNRIDX", String.class, 20)); //Sachnummer Index (K1013)
			keys.put(KKey.of("K1014"), KKeyMetadata.of("TETEILIDENT", String.class, 20)); //Teile-Ident. (K1014)
			keys.put(KKey.of("K1103"), KKeyMetadata.of("TEKOSTST", String.class, 40)); //Kostenstelle (K1103)
			keys.put(KKey.of("K1104"), KKeyMetadata.of("TESCHICHT", String.class, 20)); //Schicht (K1104)
			keys.put(KKey.of("K1110"), KKeyMetadata.of("TEBESTNR", String.class, 20)); //Bestellnummer (K1110)
			keys.put(KKey.of("K1111"), KKeyMetadata.of("TEWARENEINNR", String.class, 20)); //Wareneingangsnummer (K1111)
			keys.put(KKey.of("K1112"), KKeyMetadata.of("TEWUERFEL", String.class)); //Wï¿½rfel (K1112)
			keys.put(KKey.of("K1113"), KKeyMetadata.of("TEPOSITION", String.class)); //Position (K1113)
			keys.put(KKey.of("K1114"), KKeyMetadata.of("TEVORRICHT", String.class)); //Vorrichtung (K1114)
			keys.put(KKey.of("K1115"), KKeyMetadata.of("TEFERTDAT", String.class)); //Fertigungsdatum (K1115)
			keys.put(KKey.of("K1209"), KKeyMetadata.of("TEPRUEFART", String.class, 20)); //Prï¿½fart (K1209)
			keys.put(KKey.of("K1230"), KKeyMetadata.of("TEMESSRAUM", String.class, 40)); //Meï¿½raum (K1230)
			keys.put(KKey.of("K1231"), KKeyMetadata.of("TEMESSPROGNR", String.class, 20)); //Meï¿½programmnummer (K1231)
			keys.put(KKey.of("K1232"), KKeyMetadata.of("TEMESSPROGVER", String.class, 20)); //Meï¿½programmversion (K1232)
			keys.put(KKey.of("K1223"), KKeyMetadata.of("TEPRUEFERKEY", Integer.class, 5)); //Prï¿½fer Key (K1223)
			keys.put(KKey.of("K1221"), KKeyMetadata.of("TEPRUEFERNR", String.class, 20)); //Prï¿½fernummer (K1221)
			keys.put(KKey.of("K1222"), KKeyMetadata.of("TEPRUEFERNAME", String.class, 40)); //Prï¿½fername (K1222)
			keys.put(KKey.of("K1016"), KKeyMetadata.of("TEZSB_1016", String.class, 30)); //"Zusammenbauteil" (K1016)
			keys.put(KKey.of("K1350"), KKeyMetadata.of("TEREPORTFILE_1350", String.class, 50)); //Verknï¿½pfung des Teils mit einer Berichtsdatei (*.def) (K1350)
			keys.put(KKey.of("K1045"), KKeyMetadata.of("TE_1045", String.class, 20)); //Zeichnungsgï¿½ltigkeitsdatum (K1045)
			keys.put(KKey.of("K1046"), KKeyMetadata.of("TE_1046", String.class, 60)); //Zeichnungsdateiname (K1046)
			keys.put(KKey.of("K1047"), KKeyMetadata.of("TE_1047", String.class, 20)); //Grundzeichnung Nummer (K1047)
			keys.put(KKey.of("K1300"), KKeyMetadata.of("TE_1300", Integer.class)); //Auswertestrategie (K1300)
			keys.put(KKey.of("K1301"), KKeyMetadata.of("TE_1301", Integer.class, 5)); //Mandant (K1301)
			keys.put(KKey.of("K1302"), KKeyMetadata.of("TE_1302", String.class, 40)); //Prï¿½flos (K1302)
			keys.put(KKey.of("K1311"), KKeyMetadata.of("TE_1311", String.class, 40)); //Fertigungsauftrag (K1311)
			keys.put(KKey.of("K1341"), KKeyMetadata.of("TE_1341", String.class, 20)); //Prï¿½fplan Nummertext (K1341)
			keys.put(KKey.of("K1342"), KKeyMetadata.of("TE_1342", String.class, 40)); //Prï¿½fplanindex (K1342)
			keys.put(KKey.of("K1343"), KKeyMetadata.of("TE_1343", String.class, 20)); //Prï¿½fplan Erstellungsdatum (K1343)
			keys.put(KKey.of("K1303"), KKeyMetadata.of("TEWERK", String.class, 40)); //Werk (K1303)
			keys.put(KKey.of("K1210"), KKeyMetadata.of("TEMESSTYP", Integer.class, 5)); //Messtyp (K1210)
			keys.put(KKey.of("K1344"), KKeyMetadata.of("TE_1344", String.class, 40)); //Prï¿½fplan Ersteller (K1344)
			keys.put(KKey.of("K1015"), KKeyMetadata.of("TE_1015", Integer.class, 3)); //Modul-Key mit dem das Teil erstellt wurde
			keys.put(KKey.of("K1017"), KKeyMetadata.of("TE_1017", Integer.class, 3)); //Prï¿½fplan gesperrt (K1017)
			keys.put(KKey.of("K1087"), KKeyMetadata.of("TE_1087", String.class)); //Arbeitsgangbezeichnung (K1087)
			keys.put(KKey.of("K1018"), KKeyMetadata.of("TE_1018", Integer.class)); //zugehï¿½riges Masterteil (K1018)
			keys.put(KKey.of("K1401"), KKeyMetadata.of("TE_1401", Integer.class)); //destra vp_typ (K1401)
			keys.put(KKey.of("K1402"), KKeyMetadata.of("TE_1402", Integer.class)); //destra rg_typ (K1402)
			keys.put(KKey.of("K1403"), KKeyMetadata.of("TE_1403", Integer.class)); //destra va_typ (K1403)
			keys.put(KKey.of("K1404"), KKeyMetadata.of("TE_1404", Integer.class)); //destra vp_k (K1404)
			keys.put(KKey.of("K1405"), KKeyMetadata.of("TE_1405", Integer.class)); //destra vp_p (K1405)
			keys.put(KKey.of("K1407"), KKeyMetadata.of("TE_1407", Integer.class)); //destra rg_polynom (K1407)
			keys.put(KKey.of("K1408"), KKeyMetadata.of("TE_1408", Integer.class)); //destra rg_ww  (K1408)
			keys.put(KKey.of("K1410"), KKeyMetadata.of("TE_1410", String.class)); //destra up_def_block (K1410)
			keys.put(KKey.of("K1411"), KKeyMetadata.of("TE_1411", Integer.class)); //destra akt_destra_typ (K1411)
			keys.put(KKey.of("K1091"), KKeyMetadata.of("TE_1091", String.class)); //Linien-Nr (K1091)
			keys.put(KKey.of("K1092"), KKeyMetadata.of("TE_1092", String.class)); //Linienbezeichnung (K1092)
			keys.put(KKey.of("K1105"), KKeyMetadata.of("TE_1105", String.class)); //Werksbereich-Nr. (K1105)
			keys.put(KKey.of("K1106"), KKeyMetadata.of("TE_1106", String.class)); //Abteilung-Nr. (K1106)
			keys.put(KKey.of("K1107"), KKeyMetadata.of("TE_1107", String.class)); //Werkstatt-Nr. (K1107)
			keys.put(KKey.of("K1108"), KKeyMetadata.of("TE_1108", String.class)); //Kostenstelle-Nr. (K1108)
			keys.put(KKey.of("K1304"), KKeyMetadata.of("TE_1304", String.class)); //Werk-Nr (K1304)
			keys.put(KKey.of("K1048"), KKeyMetadata.of("TE_1048", String.class)); //CAD-Zeichnungsname (K1048)
			keys.put(KKey.of("K0001"), KKeyMetadata.of("WVWERTNR", BigDecimal.class, 22)); //Wert (K0001)
			keys.put(KKey.of("K0002"), KKeyMetadata.of("WVATTRIBUT", Integer.class, 5)); //Attribut (K0002)
			keys.put(KKey.of("K0008"), KKeyMetadata.of("WVPRUEFER", Integer.class, 10)); //Prï¿½fer Key (K0008)
			keys.put(KKey.of("K0012"), KKeyMetadata.of("WVPRUEFMIT", Integer.class, 10)); //Prï¿½fmittel (K0012)
			keys.put(KKey.of("K0010"), KKeyMetadata.of("WVMASCHINE", Integer.class, 10)); //Maschinen-Nr (K0010)
			keys.put(KKey.of("K0007"), KKeyMetadata.of("WVNEST", Integer.class, 10)); //Nest-Nr (K0007)
			keys.put(KKey.of("K0004"), KKeyMetadata.of("WVDATZEIT", Date.class)); //Datum/Zeit (K0004)
			keys.put(KKey.of("K0006"), KKeyMetadata.of("WVCHARGE", String.class, 14)); //Chargen-/Ident.-Nr. (K0006)
			keys.put(KKey.of("K0053"), KKeyMetadata.of("WVAUFTRAG", String.class, 20)); //Key des Auftrags (K0053)
			keys.put(KKey.of("K0031"), KKeyMetadata.of("WV0031", Integer.class)); //ID der Messung/Untersuchung (K0031)
			keys.put(KKey.of("K0034"), KKeyMetadata.of("WV0034", Integer.class)); //Messungsstatus fï¿½r SAP-ï¿½bertragung (K0034)
			keys.put(KKey.of("K0009"), KKeyMetadata.of("WV0009", String.class, 255)); //Short Text
			keys.put(KKey.of("K0014"), KKeyMetadata.of("WV0014", String.class, 40)); //Short Text
			keys.put(KKey.of("K0015"), KKeyMetadata.of("WV0015", Integer.class, 5)); //Number
			keys.put(KKey.of("K0016"), KKeyMetadata.of("WV0016", String.class)); //Short Text
			keys.put(KKey.of("K0017"), KKeyMetadata.of("WV0017", String.class)); //Short Text
			keys.put(KKey.of("K0097"), KKeyMetadata.of("WV0097", UUID.class)); //Number
			keys.put(KKey.of("K2001"), KKeyMetadata.of("MEMERKNR", String.class, 20)); //Merkmals-Nr (K2001)
			keys.put(KKey.of("K2002"), KKeyMetadata.of("MEMERKBEZ", String.class, 80)); //Bezeichnung des Merkmals (K2002)
			keys.put(KKey.of("K2101"), KKeyMetadata.of("MENENNMAS", BigDecimal.class, 22)); //Nennmaï¿½ numerisch (K2101)
			keys.put(KKey.of("K2120"), KKeyMetadata.of("MEARTUGW", Integer.class, 3)); //Art des UGW : Abmaï¿½(1) ,Grenzwert(2) oder Natï¿½rliche Grenze(3), sonst wird Grenzwert angenommen (K2120)
			keys.put(KKey.of("K2121"), KKeyMetadata.of("MEARTOGW", Integer.class, 3)); //Art des OGW : Abmaï¿½(1) ,Grenzwert(2) oder Natï¿½rliche Grenze(3), sonst wird Grenzwert angenommen (K2121)
			keys.put(KKey.of("K2240"), KKeyMetadata.of("MEARTPLAUSIUNT", Integer.class)); //Temperatur Methode (K2240) (ehemals: Art der Plausigrenze unten : Keine Grenze (0),ï¿½berprï¿½fung bei Eingabe(1),ï¿½berprï¿½fung Eingabe und File-IO(2))
			keys.put(KKey.of("K2241"), KKeyMetadata.of("MEARTPLAUSIOB", Integer.class)); //Komponente Methode (K2241) (ehemals: Art der Plausigrenze oben : Keine Grenze (0),ï¿½berprï¿½fung bei Eingabe(1),ï¿½berprï¿½fung Eingabe und File-IO(2))
			keys.put(KKey.of("K2110"), KKeyMetadata.of("MEUGW", BigDecimal.class, 22)); //UGW numerisch (K2110)
			keys.put(KKey.of("K2111"), KKeyMetadata.of("MEOGW", BigDecimal.class, 22)); //OGW numerisch (K2111)
			keys.put(KKey.of("K2504"), KKeyMetadata.of("MEFSK", Integer.class, 3)); //ï¿½nderungsstatus Zeichnung (K2504) (ehemals: Nr. der Fehlersammelkarte))
			keys.put(KKey.of("K2163"), KKeyMetadata.of("MEFEHLKOST", BigDecimal.class, 22)); //Fehlerkosten (K2163)
			keys.put(KKey.of("K2006"), KKeyMetadata.of("MEDPFLICHT", Integer.class, 5)); //Dokupflichtig (K2006)
			keys.put(KKey.of("K2141"), KKeyMetadata.of("MEEINHEIT", Integer.class, 5)); //Masseinheit (K2141)
			keys.put(KKey.of("K2022"), KKeyMetadata.of("MEAUFLOES", Integer.class, 5)); //Aufloesung = Anzahl Nachkommastellen (K2022)
			keys.put(KKey.of("K2013"), KKeyMetadata.of("MEKLASSENW", BigDecimal.class, 22)); //Klassenweite (K2013)
			keys.put(KKey.of("K2311"), KKeyMetadata.of("MEFERTARTNR", String.class, 20)); //Fertigungsart-Nr. (K2311)
			keys.put(KKey.of("K2312"), KKeyMetadata.of("MEFERTART", String.class, 40)); //Fertigungsart (K2312)
			keys.put(KKey.of("K2405"), KKeyMetadata.of("MEPRUEFMIT", Integer.class, 5)); //Prï¿½fmittel. (K2405)
			keys.put(KKey.of("K2403"), KKeyMetadata.of("MEPMGRUPPET", String.class, 80)); //Prï¿½fmittel-Gruppe Text (K2403)
			keys.put(KKey.of("K2402"), KKeyMetadata.of("MEPRUEFMITT", String.class, 80)); //Prï¿½fmittel Text (K2402)
			keys.put(KKey.of("K2401"), KKeyMetadata.of("MEPRUEFMITNRT", String.class, 40)); //Prï¿½fmittel-Nr. Text (K2401)
			keys.put(KKey.of("K2041"), KKeyMetadata.of("MEERFART", Integer.class, 3)); //Erfassungsart (K2041)
			keys.put(KKey.of("K2305"), KKeyMetadata.of("MEMASCHINE", Integer.class, 5)); //Maschinen-Nr (K2305)
			keys.put(KKey.of("K2900"), KKeyMetadata.of("MEBEMERK", String.class, 255)); //Bemerkung (K2900)
			keys.put(KKey.of("K2205"), KKeyMetadata.of("MEUMFSTICH", Integer.class, 5)); //Stichprobenumfang (K2205)
			keys.put(KKey.of("K2220"), KKeyMetadata.of("MEANZPRUEF", Integer.class, 5)); //Anzahl Prï¿½fer (K2220)
			keys.put(KKey.of("K2221"), KKeyMetadata.of("MEANZWIED", Integer.class, 5)); //Anzahl Wiederholungen (K2221)
			keys.put(KKey.of("K2205"), KKeyMetadata.of("MEANZTEILE", Integer.class, 5)); //Anzahl zu prï¿½fender Teile (K2205)
			keys.put(KKey.of("K2021"), KKeyMetadata.of("MEFORMEL", String.class, 255)); //Verknï¿½pfungsformel (K2021)
			keys.put(KKey.of("K2024"), KKeyMetadata.of("METRANSPA", BigDecimal.class, 22)); //Transformationsparameter a (K2024)
			keys.put(KKey.of("K2025"), KKeyMetadata.of("METRANSPB", BigDecimal.class, 22)); //Transformationsparameter b (K2025)
			keys.put(KKey.of("K2026"), KKeyMetadata.of("METRANSPC", BigDecimal.class, 22)); //Transformationsparameter c (K2026)
			keys.put(KKey.of("K2027"), KKeyMetadata.of("METRANSPD", BigDecimal.class, 22)); //Transformationsparameter d (K2027)
			keys.put(KKey.of("K2502"), KKeyMetadata.of("MEAUSWART", Integer.class, 3)); //Darstellungsformat Toleranz (K2502) (ehemals: Auswerteart)
			keys.put(KKey.of("K2202"), KKeyMetadata.of("MEAUSWTYP", Integer.class, 3)); //Auswertetyp (K2202)
			keys.put(KKey.of("K2004"), KKeyMetadata.of("MEMERKART", Integer.class, 5)); //Art des Merkmals (K2004)
			keys.put(KKey.of("K2011"), KKeyMetadata.of("MEVERTFORM", Integer.class, 5)); //Verteilungsform (K2011)
			keys.put(KKey.of("K2130"), KKeyMetadata.of("MEPLAUSIUN", BigDecimal.class, 22)); //Plausibgrenze unten (K2130)
			keys.put(KKey.of("K2131"), KKeyMetadata.of("MEPLAUSIOB", BigDecimal.class, 22)); //Plausibgrenze oben (K2131)
			keys.put(KKey.of("K2201"), KKeyMetadata.of("MEPROSTREU", BigDecimal.class, 22)); //Prozeï¿½streuung numerisch (K2201)
			keys.put(KKey.of("K2217"), KKeyMetadata.of("MENORMISTSTR", String.class, 80)); //Normal Hersteller (K2217) - (ehemals: Istmaï¿½ des Normals String - geï¿½ndert 11.02.2005)
			keys.put(KKey.of("K2213"), KKeyMetadata.of("MENORMIST", BigDecimal.class, 22)); //Istmaï¿½ des Normals numerisch (K2213)
			keys.put(KKey.of("K2211"), KKeyMetadata.of("MENORMNR", String.class, 40)); //Nummer des Normals (als String) (K2211)
			keys.put(KKey.of("K2212"), KKeyMetadata.of("MENORMBEZ", String.class, 40)); //Bezeichnung des Normals (K2212)
			keys.put(KKey.of("K2007"), KKeyMetadata.of("MESTEUERB", Integer.class, 5)); //steuerbar (K2007)
			keys.put(KKey.of("K2060"), KKeyMetadata.of("MEEREIGKAT", String.class, 50)); //Ereigniskatalog (K2060)
			keys.put(KKey.of("K2005"), KKeyMetadata.of("MEMERKKLASSE", Integer.class, 5)); //Merkmals-Klasse (K2005)
			keys.put(KKey.of("K2009"), KKeyMetadata.of("MEUNTERSART", Integer.class)); //Untersuchungs-Art (qs-STAT-Modul) (K2010)
			keys.put(KKey.of("K2234"), KKeyMetadata.of("MEANZORDKLASSE", Integer.class)); //Unabhï¿½ngige Einflussgrï¿½ï¿½en (K2234)  (ehemals: Anzahl Ordinalklassen (K2014) - Feld ist entfallen)
			keys.put(KKey.of("K2503"), KKeyMetadata.of("MEAUTOERKENNUNG", Integer.class, 3)); //Bemaï¿½ungstyp (K2503) (ehemals: automatische Werteerkennung (K2020))
			keys.put(KKey.of("K2501"), KKeyMetadata.of("MEATTR", Integer.class, 3)); //Bemaï¿½ungsattribut (K2501) (ehemals: Attribut fï¿½r Regression)
			keys.put(KKey.of("K2072"), KKeyMetadata.of("METRANSFEINGA", BigDecimal.class, 22)); //Lineare Transformation fï¿½r Eingabe a*xB (Parameter a) (K2072)
			keys.put(KKey.of("K2071"), KKeyMetadata.of("METRANSFEINGB", BigDecimal.class, 22)); //Lineare Transformation fï¿½r Eingabe a*xB (Parameter b) (K2071)
			keys.put(KKey.of("K2045"), KKeyMetadata.of("MEERFKANAL", String.class, 20)); //Erfassungskanal (K2045)
			keys.put(KKey.of("K2046"), KKeyMetadata.of("MEERFSUBKANAL", String.class, 20)); //Erfassungssubkanal (K2046)
			keys.put(KKey.of("K2012"), KKeyMetadata.of("MENACHARBEIT", Integer.class)); //Unterscheidung bei attr. Merkmalen : 0 - Grenzwerte,1-Nacharbeit (K2012)
			keys.put(KKey.of("K2100"), KKeyMetadata.of("MEZIELWERT", BigDecimal.class, 22)); //Sollwert/Zielwert (K2100)
			keys.put(KKey.of("K2102"), KKeyMetadata.of("MEPMAX", BigDecimal.class, 22)); //Zur Berechnung von cpk-Werten bei attributiven Merkmalen (K2102)
			keys.put(KKey.of("K2142"), KKeyMetadata.of("MEEINHEITTEXT", String.class, 20)); //Einheit als Text fï¿½r alte Struktur (K2142)
			keys.put(KKey.of("K2160"), KKeyMetadata.of("MELOSUMFANG", Integer.class, 5)); //Losumfang (K2160)
			keys.put(KKey.of("K2161"), KKeyMetadata.of("MEKOSTENNACHARBEIT", BigDecimal.class, 22)); //Kosten Nacharbeit (K2161)
			keys.put(KKey.of("K2162"), KKeyMetadata.of("MEKOSTENAUSSCHUSS", BigDecimal.class, 22)); //Kosten Ausschuï¿½ (K2162)
			keys.put(KKey.of("K2301"), KKeyMetadata.of("MEMASCHNR", String.class, 20)); //Maschinen-Nr. (K2301)
			keys.put(KKey.of("K2302"), KKeyMetadata.of("MEMASCHBEZ", String.class, 40)); //Maschinen-Bez. (K2302)
			keys.put(KKey.of("K2303"), KKeyMetadata.of("MEABT", String.class, 40)); //Abteilung (K2303)
			keys.put(KKey.of("K2304"), KKeyMetadata.of("MESTANDORT", String.class, 40)); //Standort (K2304)
			keys.put(KKey.of("K2320"), KKeyMetadata.of("MEAUFTRNR", String.class, 20)); //Auftrags-Nr. (K2320)
			keys.put(KKey.of("K2323"), KKeyMetadata.of("MEAUFTRAGGEBNR", Integer.class, 5)); //Auftraggeber-Nr. (K2323)
			keys.put(KKey.of("K2321"), KKeyMetadata.of("MEAUFTRAGGEBNRT", String.class, 20)); //Auftraggeber-Nr.-Text (K2321)
			keys.put(KKey.of("K2322"), KKeyMetadata.of("MEAUFTRAGGEB", String.class, 40)); //Auftraggeber (K2322)
			keys.put(KKey.of("K2410"), KKeyMetadata.of("MEPRUEFORTT", String.class, 40)); //Prï¿½fort Text  (K2410)
			keys.put(KKey.of("K2411"), KKeyMetadata.of("MEPRUEFBEGINN", String.class, 80)); //Prï¿½fbeginn (String) (K2411)
			keys.put(KKey.of("K2412"), KKeyMetadata.of("MEPRUEFENDE", String.class, 80)); //Prï¿½fende (String) (K2412)
			keys.put(KKey.of("K2423"), KKeyMetadata.of("MEPRUEFER", Integer.class, 5)); //Prï¿½fer (K2423)
			keys.put(KKey.of("K2421"), KKeyMetadata.of("MEPRUEFERNR", String.class, 20)); //Prï¿½fer-Nr. (K2421)
			keys.put(KKey.of("K2422"), KKeyMetadata.of("MEPRUEFERNAME", String.class, 40)); //Prï¿½fer-Name (K2422)
			keys.put(KKey.of("K2901"), KKeyMetadata.of("MEPRUEFBEDING", String.class, 80)); //Prï¿½fbedingungen (K2901)
			keys.put(KKey.of("K2019"), KKeyMetadata.of("MEPRUEFMITNR", Integer.class)); //Gruppen-Nr der Ausprï¿½gung (fï¿½r ordinale Merkmale) (K2019)  (Nummer bei FSK )(K2031)
			keys.put(KKey.of("K2030"), KKeyMetadata.of("MEAUGROUP", Integer.class, 5)); //Nr. des ï¿½bergeordneten Merkmals (K2030)
			keys.put(KKey.of("K2151"), KKeyMetadata.of("METOLERANZTEXT", String.class, 20)); //Toleranz als Text (K2151)
			keys.put(KKey.of("K2333"), KKeyMetadata.of("MEWERKSTCK", Integer.class, 5)); //Werkstï¿½ck-Nr. (K2333)
			keys.put(KKey.of("K2332"), KKeyMetadata.of("MEWERKSTCKTEXT", String.class, 40)); //Werkstï¿½ckbezeichnung (K2332)
			keys.put(KKey.of("K2404"), KKeyMetadata.of("MEPMAUFLOES", BigDecimal.class, 22)); //Auflï¿½sung/Meï¿½unsicherheit des Prï¿½fmittels (K2404)
			keys.put(KKey.of("K2215"), KKeyMetadata.of("MENORMAL", Integer.class, 5)); //Normal-Key (K2215)
			keys.put(KKey.of("K2214"), KKeyMetadata.of("MENORMALTEMP", BigDecimal.class, 22)); //Temperatur des Normals (K2214)
			keys.put(KKey.of("K2331"), KKeyMetadata.of("MEWERKSTCKNR", String.class, 20)); //Werkstï¿½ck-Nr. (String) (K2331)
			keys.put(KKey.of("K2003"), KKeyMetadata.of("MEKURZBEZ", String.class, 20)); //Kurzbezeichnung (K2003)
			keys.put(KKey.of("K2114"), KKeyMetadata.of("MEUGSCHROTT", BigDecimal.class, 22)); //Untere Schrottgrenze (K2114)
			keys.put(KKey.of("K2115"), KKeyMetadata.of("MEOGSCHROTT", BigDecimal.class, 22)); //Obere Schrottgrenze (K2115)
			keys.put(KKey.of("K2225"), KKeyMetadata.of("MECG", BigDecimal.class, 22)); //ermittelter Cg-Wert (K2225)
			keys.put(KKey.of("K2226"), KKeyMetadata.of("MECGK", BigDecimal.class, 22)); //ermittelter Cgk-Wert (K2226)
			keys.put(KKey.of("K2227"), KKeyMetadata.of("MEABWGC", BigDecimal.class, 22)); //Abweichung GC Typ3- GC Typ 1 (K2227)
			keys.put(KKey.of("K2243"), KKeyMetadata.of("MEZEICHN", String.class, 80)); //Zeichnung Dateiname (K2243)
			keys.put(KKey.of("K2313"), KKeyMetadata.of("MEFERTARTKEY", Integer.class, 5)); //Key der Fertigungsart (K2313)
			keys.put(KKey.of("K2406"), KKeyMetadata.of("MEPMHERST", String.class, 40)); //Prï¿½fmittelhersteller (K2406)
			keys.put(KKey.of("K2042"), KKeyMetadata.of("MEERFNR", Integer.class, 5)); //Erfassungsgerï¿½t Nr. (K2042)
			keys.put(KKey.of("K2043"), KKeyMetadata.of("MEERFNAME", String.class, 40)); //Erfassungsgerï¿½t Name (K2043)
			keys.put(KKey.of("K2044"), KKeyMetadata.of("MEERFINDEX", Integer.class, 5)); //Erfassungsgerï¿½t Index (K2044)
			keys.put(KKey.of("K2047"), KKeyMetadata.of("MEANFINDEX", Integer.class, 3)); //Software-Anforderung Index (K2047)
			keys.put(KKey.of("K2051"), KKeyMetadata.of("MEINTERFACE", Integer.class, 3)); //Schnittstelle (K2051)
			keys.put(KKey.of("K2052"), KKeyMetadata.of("MEBAUD", Integer.class, 5)); //Baudrate (K2052)
			keys.put(KKey.of("K2053"), KKeyMetadata.of("MEIRQ", Integer.class, 3)); //IRQ-Nummer (K2053)
			keys.put(KKey.of("K2054"), KKeyMetadata.of("MEPARITY", Integer.class, 3)); //Paritï¿½t (K2054)
			keys.put(KKey.of("K2055"), KKeyMetadata.of("MEDATA", Integer.class, 3)); //Datenbits (K2055)
			keys.put(KKey.of("K2056"), KKeyMetadata.of("MESTOP", Integer.class, 3)); //Stopbits (K2056)
			keys.put(KKey.of("K2061"), KKeyMetadata.of("MEPZPKAT", Integer.class, 5)); //Prozeï¿½parameterkatalog (K2061)
			keys.put(KKey.of("K2152"), KKeyMetadata.of("METOLERANZCALC", BigDecimal.class, 22)); //berechnete Toleranz (K2152)
			keys.put(KKey.of("K2306"), KKeyMetadata.of("MEBEREICH", String.class, 40)); //Bereich im Werk / Halle (K2306)
			keys.put(KKey.of("K2307"), KKeyMetadata.of("MEPTM", String.class, 40)); //PTM-Nr. (K2307)
			keys.put(KKey.of("K2341"), KKeyMetadata.of("MEPPLANNRT", String.class, 20)); //Prï¿½fplannummer Text (K2341)
			keys.put(KKey.of("K2342"), KKeyMetadata.of("MEPPLAN", String.class, 40)); //Prï¿½fplan-Name (K2342)
			keys.put(KKey.of("K2343"), KKeyMetadata.of("MEPPLANDAT", String.class, 20)); //Prï¿½fplan-Erstellungsdatum (K2343)
			keys.put(KKey.of("K2344"), KKeyMetadata.of("MEPPLANERST", String.class, 40)); //Prï¿½fplanersteller (K2344)
			keys.put(KKey.of("K2407"), KKeyMetadata.of("MESPCNR", String.class, 20)); //SPC-Gerï¿½te Nummer (K2407)
			keys.put(KKey.of("K2408"), KKeyMetadata.of("MESPCHERST", String.class, 20)); //SPC-Gerï¿½te Hersteller (K2408)
			keys.put(KKey.of("K2409"), KKeyMetadata.of("MESPCTYP", String.class, 20)); //SPC-Gerï¿½te Typ (K2409)
			keys.put(KKey.of("K2116"), KKeyMetadata.of("MENORMISTUN", BigDecimal.class)); //Temperaturkonstante (K2237) (ehemals: Unterer Istwert normal (K2251))
			keys.put(KKey.of("K2117"), KKeyMetadata.of("MENORMISTOB", BigDecimal.class)); //Temperatur Werkstï¿½ck (K2238) (ehemals: Oberer Istwert normal (K2252))
			keys.put(KKey.of("K2216"), KKeyMetadata.of("MENORMALSERNR", String.class, 20)); //Normal-Seriennummer (K2216) [ab hier: Felder aus MERKMAL_AD]
			keys.put(KKey.of("K2415"), KKeyMetadata.of("MEPRUEFMITSERNR", String.class, 20)); //Prï¿½fmittel-Seriennummer (K2415)
			keys.put(KKey.of("K2416"), KKeyMetadata.of("MEANZGERAET", String.class, 40)); //Anzeigegerï¿½t (K2416)
			keys.put(KKey.of("K2261"), KKeyMetadata.of("MEREFTEILNRSTR", String.class, 40)); //Referenzteil-Nummer (K2261)
			keys.put(KKey.of("K2262"), KKeyMetadata.of("MEREFTEILBEZ", String.class, 40)); //Referenzteil-Bezeichnung (K2262)
			keys.put(KKey.of("K2263"), KKeyMetadata.of("MEREFTEILIST", BigDecimal.class, 22)); //Referenzteil-Istwert (K2263)
			keys.put(KKey.of("K2264"), KKeyMetadata.of("MEREFTEILTEMP", BigDecimal.class, 22)); //Referenzteil-Temperatur (K2264)
			keys.put(KKey.of("K2265"), KKeyMetadata.of("MEREFTEILNR", Integer.class, 3)); //Referenzteil-Nummer (num) (K2265)
			keys.put(KKey.of("K2266"), KKeyMetadata.of("MEREFTEILSERNR", String.class, 40)); //Referenzteil-Seriennummer (K2266)
			keys.put(KKey.of("K2271"), KKeyMetadata.of("MEKALTEILUNRSTR", String.class)); //Kalibrierteil-Nummer unten (K2271)
			keys.put(KKey.of("K2272"), KKeyMetadata.of("MEKALTEILUBEZ", String.class)); //Kalibrierteil-Bezeichnung unten (K2272)
			keys.put(KKey.of("K2273"), KKeyMetadata.of("MEKALTEILUIST", BigDecimal.class)); //Kalibrierteil-Istwert unten (K2273)
			keys.put(KKey.of("K2274"), KKeyMetadata.of("MEKALTEILUTEMP", BigDecimal.class)); //Kalibrierteil-Temperatur unten (K2274)
			keys.put(KKey.of("K2275"), KKeyMetadata.of("MEKALTEILUNR", Integer.class)); //Kalibrierteil-Nummer (num) unten (K2275)
			keys.put(KKey.of("K2276"), KKeyMetadata.of("MEKALTEILUSERNR", String.class)); //Kalibrierteil-Seriennummer unten (K2276)
			keys.put(KKey.of("K2281"), KKeyMetadata.of("MEKALTEILMNRSTR", String.class, 40)); //Kalibrierteil-Nummer mitte (K2281)
			keys.put(KKey.of("K2282"), KKeyMetadata.of("MEKALTEILMBEZ", String.class, 40)); //Kalibrierteil-Bezeichnung mitte (K2282)
			keys.put(KKey.of("K2283"), KKeyMetadata.of("MEKALTEILMIST", BigDecimal.class, 22)); //Kalibrierteil-Istwert mitte (K2283)
			keys.put(KKey.of("K2284"), KKeyMetadata.of("MEKALTEILMTEMP", BigDecimal.class, 22)); //Kalibrierteil-Temperatur mitte (K2284)
			keys.put(KKey.of("K2285"), KKeyMetadata.of("MEKALTEILMNR", Integer.class, 3)); //Kalibrierteil-Nummer (num) mitte (K2285)
			keys.put(KKey.of("K2286"), KKeyMetadata.of("MEKALTEILMSERNR", String.class, 40)); //Kalibrierteil-Seriennummer mitte (K2286)
			keys.put(KKey.of("K2291"), KKeyMetadata.of("MEKALTEILONRSTR", String.class)); //Kalibrierteil-Nummer oben (K2291)
			keys.put(KKey.of("K2292"), KKeyMetadata.of("MEKALTEILOBEZ", String.class)); //Kalibrierteil-Bezeichnung oben (K2292)
			keys.put(KKey.of("K2293"), KKeyMetadata.of("MEKALTEILOIST", BigDecimal.class)); //Kalibrierteil-Istwert oben (K2293)
			keys.put(KKey.of("K2294"), KKeyMetadata.of("MEKALTEILOTEMP", BigDecimal.class)); //Kalibrierteil-Temperatur oben (K2294)
			keys.put(KKey.of("K2295"), KKeyMetadata.of("MEKALTEILONR", Integer.class)); //Kalibrierteil-Nummer (num) oben (K2295)
			keys.put(KKey.of("K2296"), KKeyMetadata.of("MEKALTEILOSERNR", String.class)); //Kalibrierteil-Seriennummer oben (K2296)
			keys.put(KKey.of("K2048"), KKeyMetadata.of("MEUEBERKAN", Integer.class, 3)); //ï¿½bernahmekanal (K2048)
			keys.put(KKey.of("K2090"), KKeyMetadata.of("MEMERKCODE", String.class, 40)); //Merkmalcode (K2090)
			keys.put(KKey.of("K2091"), KKeyMetadata.of("MEMERKINDEX", String.class, 20)); //Merkmalindex (K2091)
			keys.put(KKey.of("K2092"), KKeyMetadata.of("MEMERKTEXT", String.class, 50)); //Merkmalstext (K2092)
			keys.put(KKey.of("K2093"), KKeyMetadata.of("MEBEARBZUST", String.class, 80)); //Bearbeitungszustand (K2093)
			keys.put(KKey.of("K2095"), KKeyMetadata.of("MEELEMCODE", String.class, 40)); //Elementcode (K2095)
			keys.put(KKey.of("K2096"), KKeyMetadata.of("MEELEMINDEX", String.class, 20)); //Elementindex (K2096)
			keys.put(KKey.of("K2097"), KKeyMetadata.of("MEELEMTEXT", String.class, 50)); //Elementtext (K2097)
			keys.put(KKey.of("K2098"), KKeyMetadata.of("MEELEMADR", String.class, 20)); //Elementadresse (K2098)
			keys.put(KKey.of("K2074"), KKeyMetadata.of("MECALIBADD", BigDecimal.class, 22)); //Einstelloffset (K2074)
			keys.put(KKey.of("K2075"), KKeyMetadata.of("MECALIBMULT", BigDecimal.class, 22)); //Einstellfaktor (K2075)
			keys.put(KKey.of("K2105"), KKeyMetadata.of("MEANZNIAUSGEF", Integer.class, 5)); //Anzahl nicht ausgefallener Teile (Modul RB) (K2105)
			keys.put(KKey.of("K2203"), KKeyMetadata.of("MEGCKONVART", Integer.class)); //Konvertierungsart GC (K2203)
			keys.put(KKey.of("K2222"), KKeyMetadata.of("MEANZREF", Integer.class, 5)); //Anzahl Referenzmessungen (K2222)
			keys.put(KKey.of("K2244"), KKeyMetadata.of("MEREFPKTX", Integer.class, 5)); //Referenzpunkt X (K2244)
			keys.put(KKey.of("K2245"), KKeyMetadata.of("MEREFPKTY", Integer.class, 5)); //Referenzpunkt Y (K2245)
			keys.put(KKey.of("K2246"), KKeyMetadata.of("MEREFPKTZ", Integer.class, 5)); //Referenzpunkt Z (K2246)
			keys.put(KKey.of("K2430"), KKeyMetadata.of("ME_2430", Integer.class, 5)); //Bemusterungsart EMPB (K2430)
			keys.put(KKey.of("K2432"), KKeyMetadata.of("ME_2432", Integer.class, 5)); //Einzelwertausgabe EMPB (K2432)
			keys.put(KKey.of("K2434"), KKeyMetadata.of("ME_2434", Integer.class, 5)); //Prozessfï¿½higkeitsnachweis EMPB (K2434)
			keys.put(KKey.of("K2436"), KKeyMetadata.of("ME_2436", String.class, 10)); //Test Freq. EMPB/PPAP (K2436)
			keys.put(KKey.of("K2438"), KKeyMetadata.of("ME_2438", String.class, 10)); //Qty. tested EMPB/PPAP (K2438)
			keys.put(KKey.of("K2440"), KKeyMetadata.of("ME_2440", String.class, 40)); //ZSB-Komponente (K2440)
			keys.put(KKey.of("K2442"), KKeyMetadata.of("ME_2442", String.class, 12)); //Masse der ZSB-Komponente (K2442)
			keys.put(KKey.of("K2444"), KKeyMetadata.of("ME_2444", String.class, 40)); //Material der ZSB-Komponente (K2444)
			keys.put(KKey.of("K2448"), KKeyMetadata.of("ME_2446", String.class, 40)); //Herstellerbezogene Produktbezeichnung der ZSB-Komponente (K2448)
			keys.put(KKey.of("K2448"), KKeyMetadata.of("ME_2448", String.class, 40)); //Hersteller der ZSB-Komponente (K2448)
			keys.put(KKey.of("K2073"), KKeyMetadata.of("ME_2073", BigDecimal.class, 22)); //Einstellmaï¿½ (K2073)
			keys.put(KKey.of("K2107"), KKeyMetadata.of("ME_2107", BigDecimal.class)); //Erweiterungsfaktor fï¿½r erweiterte Meï¿½unsicherheit (K2107)
			keys.put(KKey.of("K2170"), KKeyMetadata.of("ME_2170", BigDecimal.class, 22)); //(K2170)
			keys.put(KKey.of("K2171"), KKeyMetadata.of("ME_2171", BigDecimal.class, 22)); //(K2171)
			keys.put(KKey.of("K2172"), KKeyMetadata.of("ME_2172", BigDecimal.class, 22)); //(K2172)
			keys.put(KKey.of("K2173"), KKeyMetadata.of("ME_2173", BigDecimal.class, 22)); //(K2173)
			keys.put(KKey.of("K2228"), KKeyMetadata.of("ME_2228", BigDecimal.class, 22)); //(K2228)
			keys.put(KKey.of("K2229"), KKeyMetadata.of("ME_2229", BigDecimal.class)); //(K2229)
			keys.put(KKey.of("K2230"), KKeyMetadata.of("ME_2230", BigDecimal.class)); //(K2230)
			keys.put(KKey.of("K2231"), KKeyMetadata.of("ME_2231", BigDecimal.class)); //(K2231)
			keys.put(KKey.of("K2232"), KKeyMetadata.of("ME_2232", BigDecimal.class)); //(K2232)
			keys.put(KKey.of("K2233"), KKeyMetadata.of("ME_2233", BigDecimal.class, 22)); //(K2233)
			keys.put(KKey.of("K2235"), KKeyMetadata.of("ME_2235", BigDecimal.class, 22)); //(K2235)
			keys.put(KKey.of("K2236"), KKeyMetadata.of("ME_2236", BigDecimal.class, 22)); //(K2236)
			keys.put(KKey.of("K2016"), KKeyMetadata.of("ME_2016", Integer.class, 3)); //(K2016)
			keys.put(KKey.of("K8500"), KKeyMetadata.of("MEUMFPROZ", Integer.class, 5)); //Stichprobenumfang Prozeï¿½fï¿½higkeit (K8500)
			keys.put(KKey.of("K8501"), KKeyMetadata.of("MEGLEITSTUMF", Integer.class, 3)); //fï¿½r gleitende Mittelwertkarten Umfang der Gesamtstichprobe (K8501)
			keys.put(KKey.of("K8502"), KKeyMetadata.of("MESTIFREQT", String.class, 40)); //Stichprobenfrequenz (String) (K8502)
			keys.put(KKey.of("K8504"), KKeyMetadata.of("MESTIFREQ", Integer.class, 5)); //Stichprobenfrequenz (K8504)
			keys.put(KKey.of("K8510"), KKeyMetadata.of("MECP", BigDecimal.class, 22)); //Cp-Wert (K8510)
			keys.put(KKey.of("K8511"), KKeyMetadata.of("MECPK", BigDecimal.class, 22)); //Cpk-Wert (K8511)
			keys.put(KKey.of("K8520"), KKeyMetadata.of("MEVORGCP", BigDecimal.class, 22)); //Vorgabe Cp Wert (K8520)
			keys.put(KKey.of("K8521"), KKeyMetadata.of("MEVORGCPK", BigDecimal.class, 22)); //Vorgabe Cpk Wert (K8521)
			keys.put(KKey.of("K8522"), KKeyMetadata.of("MECPFIX", BigDecimal.class, 22)); //Gefixter Cp-Wert (K8522)
			keys.put(KKey.of("K8523"), KKeyMetadata.of("MECPKFIX", BigDecimal.class, 22)); //Gefixter Cpk-Wert (K8523)
			keys.put(KKey.of("K8530"), KKeyMetadata.of("ME_8530", Integer.class, 5)); //Prozeï¿½fï¿½higkeit EMPB (K8530)
			keys.put(KKey.of("K8531"), KKeyMetadata.of("ME_8531", BigDecimal.class, 22)); //Eingegebener Fï¿½higkeitsindex Cp EMPB (K8531)
			keys.put(KKey.of("K8532"), KKeyMetadata.of("ME_8532", BigDecimal.class, 22)); //Eingegebener Fï¿½higkeitsindex Cpk EMPB (K8532)
			keys.put(KKey.of("K8540"), KKeyMetadata.of("ME_8540", Integer.class, 5)); //Bewertung EMPB (K8540)
			keys.put(KKey.of("K8600"), KKeyMetadata.of("MEKORRSTRAT", Integer.class, 3)); //Korrekturstrategie (K8600)
			keys.put(KKey.of("K8610"), KKeyMetadata.of("MEUKG", BigDecimal.class, 22)); //Untere Korrekturgrenze (K8610)
			keys.put(KKey.of("K8611"), KKeyMetadata.of("MEOKG", BigDecimal.class, 22)); //Obere Korrekturgrenze (K8611)
			keys.put(KKey.of("K8612"), KKeyMetadata.of("MEPUFFERSIZE", Integer.class, 3)); //Puffergroesse (K8612)
			keys.put(KKey.of("K8613"), KKeyMetadata.of("MEKORRZIEL", BigDecimal.class, 22)); //Korrekturzielwert (K8613)

			return keys;
		}

	}

}
