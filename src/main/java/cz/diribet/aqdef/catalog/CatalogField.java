package cz.diribet.aqdef.catalog;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.KKeyMetadata;

/**
 * @author Vlastimil Dolejs
 * @see Catalog
 */
public enum CatalogField {

	K4020(Catalog.SUPPLIER, KKey.of("K4020_ID"), KKeyMetadata.of("LILFDNR", Integer.class), CatalogFieldType.ID),
	K4022(Catalog.SUPPLIER, KKey.of("K4022"), KKeyMetadata.of("LINR", String.class, 20)),
	K4023(Catalog.SUPPLIER, KKey.of("K4023"), KKeyMetadata.of("LINAME1", String.class, 80)),
	K4024(Catalog.SUPPLIER, KKey.of("K4024"), KKeyMetadata.of("LINAME2", String.class, 80)),
	K4025(Catalog.SUPPLIER, KKey.of("K4025"), KKeyMetadata.of("LIWERKSSCHL", String.class, 50)),
	K4026(Catalog.SUPPLIER, KKey.of("K4026"), KKeyMetadata.of("LIWERK", String.class, 50)),
	K4027(Catalog.SUPPLIER, KKey.of("K4027"), KKeyMetadata.of("LISTRASSE", String.class, 50)),
	K4028(Catalog.SUPPLIER, KKey.of("K4028"), KKeyMetadata.of("LIORT", String.class, 50)),
	K4029(Catalog.SUPPLIER, KKey.of("K4029"), KKeyMetadata.of("LILAND", String.class, 50)),
	K4521(Catalog.SUPPLIER, KKey.of("K4521"), KKeyMetadata.of("LISTATE", Integer.class), CatalogFieldType.STATE),
	K4522(Catalog.SUPPLIER, KKey.of("K4522"), KKeyMetadata.of("LIMEMO", String.class, 255)),

	K4000(Catalog.CUSTOMER, KKey.of("K4000_ID"), KKeyMetadata.of("KULFDNR", Integer.class), CatalogFieldType.ID),
	K4002(Catalog.CUSTOMER, KKey.of("K4002"), KKeyMetadata.of("KUNR", String.class, 20)),
	K4003(Catalog.CUSTOMER, KKey.of("K4003"), KKeyMetadata.of("KUNAME1", String.class, 80)),
	K4004(Catalog.CUSTOMER, KKey.of("K4004"), KKeyMetadata.of("KUNAME2", String.class, 80)),
	K4005(Catalog.CUSTOMER, KKey.of("K4005"), KKeyMetadata.of("KUWERKSSCHL", String.class, 50)),
	K4006(Catalog.CUSTOMER, KKey.of("K4006"), KKeyMetadata.of("KUWERK", String.class, 50)),
	K4007(Catalog.CUSTOMER, KKey.of("K4007"), KKeyMetadata.of("KUSTRASSE", String.class, 50)),
	K4008(Catalog.CUSTOMER, KKey.of("K4008"), KKeyMetadata.of("KUORT", String.class, 50)),
	K4009(Catalog.CUSTOMER, KKey.of("K4009"), KKeyMetadata.of("KULAND", String.class, 50)),
	K4501(Catalog.CUSTOMER, KKey.of("K4501"), KKeyMetadata.of("KUSTATE", Integer.class), CatalogFieldType.STATE),
	K4502(Catalog.CUSTOMER, KKey.of("K4502"), KKeyMetadata.of("KUMEMO", String.class, 255)),

	K4120(Catalog.EMPLOYEE, KKey.of("K4120_ID"), KKeyMetadata.of("MIMITARB", Integer.class), CatalogFieldType.ID),
	K4122(Catalog.EMPLOYEE, KKey.of("K4122"), KKeyMetadata.of("MINAME1", String.class, 50)),
	K4123(Catalog.EMPLOYEE, KKey.of("K4123"), KKeyMetadata.of("MINAME2", String.class, 50)),
	K4124(Catalog.EMPLOYEE, KKey.of("K4124"), KKeyMetadata.of("MIABT", String.class, 50)),
	K4125(Catalog.EMPLOYEE, KKey.of("K4125"), KKeyMetadata.of("MITELEFON", String.class, 50)),
	K4126(Catalog.EMPLOYEE, KKey.of("K4126"), KKeyMetadata.of("MIFAX", String.class, 50)),
	K4127(Catalog.EMPLOYEE, KKey.of("K4127"), KKeyMetadata.of("MIEMAIL", String.class, 50)),
	K4128(Catalog.EMPLOYEE, KKey.of("K4128"), KKeyMetadata.of("MIPOS", String.class, 30)),
	K4129(Catalog.EMPLOYEE, KKey.of("K4129"), KKeyMetadata.of("MIANREDE", String.class, 15)),
	K4621(Catalog.EMPLOYEE, KKey.of("K4621"), KKeyMetadata.of("MISTATE", Integer.class), CatalogFieldType.STATE),
	K4622(Catalog.EMPLOYEE, KKey.of("K4622"), KKeyMetadata.of("MIBEMERK", String.class, 200)),

	K4090(Catalog.OPERATOR, KKey.of("K4090_ID"), KKeyMetadata.of("PRPRUEFER", Integer.class), CatalogFieldType.ID),
	K4092(Catalog.OPERATOR, KKey.of("K4092"), KKeyMetadata.of("PRNAME", String.class, 50)),
	K4093(Catalog.OPERATOR, KKey.of("K4093"), KKeyMetadata.of("PRVORNAME", String.class, 50)),
	K4094(Catalog.OPERATOR, KKey.of("K4094"), KKeyMetadata.of("PRABT", String.class, 50)),
	K4095(Catalog.OPERATOR, KKey.of("K4095"), KKeyMetadata.of("PRTELEFON", String.class, 50)),
	K4096(Catalog.OPERATOR, KKey.of("K4096"), KKeyMetadata.of("PRFAX", String.class, 50)),
	K4097(Catalog.OPERATOR, KKey.of("K4097"), KKeyMetadata.of("PREMAIL", String.class, 50)),
	K4098(Catalog.OPERATOR, KKey.of("K4098"), KKeyMetadata.of("PRPOS", String.class, 30)),
	K4099(Catalog.OPERATOR, KKey.of("K4099"), KKeyMetadata.of("PRANREDE", String.class, 15)),
	K4591(Catalog.OPERATOR, KKey.of("K4591"), KKeyMetadata.of("PRSTATE", Integer.class), CatalogFieldType.STATE),
	K4592(Catalog.OPERATOR, KKey.of("K4592"), KKeyMetadata.of("PRBEMERK", String.class, 200)),

	K4060(Catalog.MACHINE, KKey.of("K4060_ID"), KKeyMetadata.of("MAMASCHINE", Integer.class), CatalogFieldType.ID),
	K4062(Catalog.MACHINE, KKey.of("K4062"), KKeyMetadata.of("MANR", String.class, 40)),
	K4063(Catalog.MACHINE, KKey.of("K4063"), KKeyMetadata.of("MABEZ", String.class, 100)),
	K4064(Catalog.MACHINE, KKey.of("K4064"), KKeyMetadata.of("MABEREICH", String.class, 50)),
	K4065(Catalog.MACHINE, KKey.of("K4065"), KKeyMetadata.of("MAABT", String.class, 50)),
	K4066(Catalog.MACHINE, KKey.of("K4066"), KKeyMetadata.of("MAOPNR", String.class, 50)),
	K4067(Catalog.MACHINE, KKey.of("K4067"), KKeyMetadata.of("MAEXTREFNR", String.class, 50)),
	K4561(Catalog.MACHINE, KKey.of("K4561"), KKeyMetadata.of("MASTATE", Integer.class), CatalogFieldType.STATE),
	K4562(Catalog.MACHINE, KKey.of("K4562"), KKeyMetadata.of("MABESCH", String.class, 200)),

	K4250(Catalog.TOOL, KKey.of("K4250_ID"), KKeyMetadata.of("NENEST", Integer.class), CatalogFieldType.ID),
	K4252(Catalog.TOOL, KKey.of("K4252"), KKeyMetadata.of("NEBESCH", String.class, 80)),
	K4253(Catalog.TOOL, KKey.of("K4253"), KKeyMetadata.of("SMART_NENR", String.class, 100)),
	K4751(Catalog.TOOL, KKey.of("K4751"), KKeyMetadata.of("NESTATE", Integer.class), CatalogFieldType.STATE),
	K4752(Catalog.TOOL, KKey.of("K4752"), KKeyMetadata.of("NEBEMERK", String.class, 200)),

	K4010(Catalog.MANUFACTURER, KKey.of("K4010_ID"), KKeyMetadata.of("HELFDNR", Integer.class), CatalogFieldType.ID),
	K4012(Catalog.MANUFACTURER, KKey.of("K4012"), KKeyMetadata.of("HENR", String.class, 50)),
	K4013(Catalog.MANUFACTURER, KKey.of("K4013"), KKeyMetadata.of("HENAME1", String.class, 80)),
	K4014(Catalog.MANUFACTURER, KKey.of("K4014"), KKeyMetadata.of("HENAME2", String.class, 80)),
	K4015(Catalog.MANUFACTURER, KKey.of("K4015"), KKeyMetadata.of("HEWERKSSCHL", String.class, 50)),
	K4016(Catalog.MANUFACTURER, KKey.of("K4016"), KKeyMetadata.of("HEWERK", String.class, 50)),
	K4017(Catalog.MANUFACTURER, KKey.of("K4017"), KKeyMetadata.of("HESTRASSE", String.class, 50)),
	K4018(Catalog.MANUFACTURER, KKey.of("K4018"), KKeyMetadata.of("HEORT", String.class, 50)),
	K4019(Catalog.MANUFACTURER, KKey.of("K4019"), KKeyMetadata.of("HELAND", String.class, 50)),
	K4512(Catalog.MANUFACTURER, KKey.of("K4512"), KKeyMetadata.of("HEMEMO", String.class)),
	K4511(Catalog.MANUFACTURER, KKey.of("K4511"), KKeyMetadata.of("HESTATE", Integer.class), CatalogFieldType.STATE),

	K4040(Catalog.MATERIAL, KKey.of("K4040_ID"), KKeyMetadata.of("WSLFDNR", Integer.class), CatalogFieldType.ID),
	K4042(Catalog.MATERIAL, KKey.of("K4042"), KKeyMetadata.of("WSNR", String.class, 50)),
	K4043(Catalog.MATERIAL, KKey.of("K4043"), KKeyMetadata.of("WSBEZEICH", String.class, 100)),
	K4541(Catalog.MATERIAL, KKey.of("K4541"), KKeyMetadata.of("WSSTATE", Integer.class), CatalogFieldType.STATE),
	K4542(Catalog.MATERIAL, KKey.of("K4542"), KKeyMetadata.of("WSBEMERK", String.class, 200)),

	K4080(Catalog.UNIT, KKey.of("K4080_ID"), KKeyMetadata.of("EIEINHEIT", Integer.class), CatalogFieldType.ID),
	K4082(Catalog.UNIT, KKey.of("K4082"), KKeyMetadata.of("EIEINHTEXT", String.class, 100)),
	K4581(Catalog.UNIT, KKey.of("K4581"), KKeyMetadata.of("EISTATE", Integer.class), CatalogFieldType.STATE),
	K4582(Catalog.UNIT, KKey.of("K4582"), KKeyMetadata.of("EIBEMERK", String.class, 200)),

	K4050(Catalog.DRAWING, KKey.of("K4050_ID"), KKeyMetadata.of("ZNTEIL", Integer.class), CatalogFieldType.ID),
	K4052(Catalog.DRAWING, KKey.of("K4052"), KKeyMetadata.of("ZNZNR", String.class, 40)),
	K4053(Catalog.DRAWING, KKey.of("K4053"), KKeyMetadata.of("ZNZNRINDEX", String.class, 100)),
	K4551(Catalog.DRAWING, KKey.of("K4551"), KKeyMetadata.of("ZNSTATE", Integer.class), CatalogFieldType.STATE),
	K4552(Catalog.DRAWING, KKey.of("K4552"), KKeyMetadata.of("ZNBEMERK", String.class, 200)),

	K4110(Catalog.PRODUCT, KKey.of("K4110_ID"), KKeyMetadata.of("EZERZEUGNIS", Integer.class), CatalogFieldType.ID),
	K4112(Catalog.PRODUCT, KKey.of("K4112"), KKeyMetadata.of("EZNUMMER", String.class, 20)),
	K4113(Catalog.PRODUCT, KKey.of("K4113"), KKeyMetadata.of("EZBEZ", String.class, 80)),
	K4114(Catalog.PRODUCT, KKey.of("K4114"), KKeyMetadata.of("EZKUNDE", Integer.class)), // vazba na CUSTOMER
	K4611(Catalog.PRODUCT, KKey.of("K4611"), KKeyMetadata.of("EZSTATE", Integer.class), CatalogFieldType.STATE),
	K4612(Catalog.PRODUCT, KKey.of("K4612"), KKeyMetadata.of("EZBEMERK", String.class, 200)),

	K4030(Catalog.PURCHASE_ORDER, KKey.of("K4030_ID"), KKeyMetadata.of("PAAUFTRAG", Integer.class), CatalogFieldType.ID),
	K4032(Catalog.PURCHASE_ORDER, KKey.of("K4032"), KKeyMetadata.of("PAAUFTRAGNR", String.class, 40)),
	K4033(Catalog.PURCHASE_ORDER, KKey.of("K4033"), KKeyMetadata.of("PABEZEICH", String.class, 100)),
	K4531(Catalog.PURCHASE_ORDER, KKey.of("K4531"), KKeyMetadata.of("PASTATE", Integer.class), CatalogFieldType.STATE),
	K4532(Catalog.PURCHASE_ORDER, KKey.of("K4532"), KKeyMetadata.of("PABEMERK", String.class, 200)),

	K4230(Catalog.ORDINAL_CLASS, KKey.of("K4230_ID"), KKeyMetadata.of("OKKEY", Integer.class), CatalogFieldType.ID),
	K4232(Catalog.ORDINAL_CLASS, KKey.of("K4232"), KKeyMetadata.of("OKNR", String.class, 40)),
	K4233(Catalog.ORDINAL_CLASS, KKey.of("K4233"), KKeyMetadata.of("OKBEZ", String.class, 100)),
	K4234(Catalog.ORDINAL_CLASS, KKey.of("K4234"), KKeyMetadata.of("OKKURZBEZ", String.class, 20)),
	K4235(Catalog.ORDINAL_CLASS, KKey.of("K4235"), KKeyMetadata.of("OKBEWERT", Integer.class)),
	K4236(Catalog.ORDINAL_CLASS, KKey.of("K4236"), KKeyMetadata.of("OKSTATE", Integer.class), CatalogFieldType.STATE),
	K4731(Catalog.ORDINAL_CLASS, KKey.of("K4731"), KKeyMetadata.of("OKRANG", Integer.class)),
	K4732(Catalog.ORDINAL_CLASS, KKey.of("K4732"), KKeyMetadata.of("OKBEMERK", String.class, 200)),

	K4100(Catalog.CONTRACTOR, KKey.of("K4100_ID"), KKeyMetadata.of("AULFDNR", Integer.class), CatalogFieldType.ID),
	K4102(Catalog.CONTRACTOR, KKey.of("K4102"), KKeyMetadata.of("AUNR", String.class, 50)),
	K4103(Catalog.CONTRACTOR, KKey.of("K4103"), KKeyMetadata.of("AUNAME1", String.class, 100)),
	K4601(Catalog.CONTRACTOR, KKey.of("K4601"), KKeyMetadata.of("AUGSTATE", Integer.class), CatalogFieldType.STATE),
	K4602(Catalog.CONTRACTOR, KKey.of("K4602"), KKeyMetadata.of("AUMEMO", String.class)),

	K4070(Catalog.GAGE, KKey.of("K4070"), KKeyMetadata.of("PMPRUEFMIT", Integer.class), CatalogFieldType.ID),
	K4072(Catalog.GAGE, KKey.of("K4072"), KKeyMetadata.of("PMNR", String.class, 40)),
	K4073(Catalog.GAGE, KKey.of("K4073"), KKeyMetadata.of("PMBEZ", String.class, 80)),
	K4074(Catalog.GAGE, KKey.of("K4074"), KKeyMetadata.of("SMART_PGBEZ", String.class, 80)),
	K4075(Catalog.GAGE, KKey.of("K4075"), KKeyMetadata.of("PMLETZTDAT", Date.class)),
	K4076(Catalog.GAGE, KKey.of("K4076"), KKeyMetadata.of("PMNAECHDAT", Date.class)),
	K4077(Catalog.GAGE, KKey.of("K4077"), KKeyMetadata.of("PMIPADDR", String.class, 30)),
	K4078(Catalog.GAGE, KKey.of("K4078"), KKeyMetadata.of("PMEINSORT", String.class, 50)),
	K4079(Catalog.GAGE, KKey.of("K4079"), KKeyMetadata.of("PMCOMP", String.class, 50)),
	K4571(Catalog.GAGE, KKey.of("K4571"), KKeyMetadata.of("PMSTATE", Integer.class), CatalogFieldType.STATE),
	K4572(Catalog.GAGE, KKey.of("K4572"), KKeyMetadata.of("PM_BESCH", String.class, 200)),
	K4575(Catalog.GAGE, KKey.of("K4575"), KKeyMetadata.of("PMQVERS", String.class, 30)),
	K4576(Catalog.GAGE, KKey.of("K4576"), KKeyMetadata.of("PMSOFTW", String.class, 50)),

	K4240(Catalog.PROCESS_PARAMETER, KKey.of("K4240_ID"), KKeyMetadata.of("PPNR", Integer.class), CatalogFieldType.ID),
	K4242(Catalog.PROCESS_PARAMETER, KKey.of("K4242"), KKeyMetadata.of("PPNRTEXT", String.class, 40)),
	K4244(Catalog.PROCESS_PARAMETER, KKey.of("K4244"), KKeyMetadata.of("PPKURZTEXT", String.class, 20)),
	K4243(Catalog.PROCESS_PARAMETER, KKey.of("K4243"), KKeyMetadata.of("PPLANGTEXT", String.class, 100)),
	K4741(Catalog.PROCESS_PARAMETER, KKey.of("K4741"), KKeyMetadata.of("PPSTATE", Integer.class), CatalogFieldType.STATE),
	K4742(Catalog.PROCESS_PARAMETER, KKey.of("K4742"), KKeyMetadata.of("PPBEMERK", String.class, 200)),

	K4270(Catalog.K0061, KKey.of("K4270_ID"), KKeyMetadata.of("KATKEY", Integer.class), CatalogFieldType.ID),
	K4272(Catalog.K0061, KKey.of("K4272"), KKeyMetadata.of("NR", String.class, 20)),
	K4273(Catalog.K0061, KKey.of("K4273"), KKeyMetadata.of("BEZ", String.class, 100)),
	K4771(Catalog.K0061, KKey.of("K4771"), KKeyMetadata.of("STATE", Integer.class), CatalogFieldType.STATE),
	K4772(Catalog.K0061, KKey.of("K4772"), KKeyMetadata.of("BEMERK", String.class, 200)),

	K4280(Catalog.K0062, KKey.of("K4280_ID"), KKeyMetadata.of("KATKEY", Integer.class), CatalogFieldType.ID),
	K4282(Catalog.K0062, KKey.of("K4282"), KKeyMetadata.of("NR", String.class, 20)),
	K4283(Catalog.K0062, KKey.of("K4283"), KKeyMetadata.of("BEZ", String.class, 100)),
	K4781(Catalog.K0062, KKey.of("K4781"), KKeyMetadata.of("STATE", Integer.class), CatalogFieldType.STATE),
	K4782(Catalog.K0062, KKey.of("K4782"), KKeyMetadata.of("BEMERK", String.class, 200)),

	K4290(Catalog.K0063, KKey.of("K4290"), KKeyMetadata.of("KATKEY", Integer.class), CatalogFieldType.ID),
	K4292(Catalog.K0063, KKey.of("K4292"), KKeyMetadata.of("NR", String.class, 20)),
	K4293(Catalog.K0063, KKey.of("K4293"), KKeyMetadata.of("BEZ", String.class, 100)),
	K4791(Catalog.K0063, KKey.of("K4791"), KKeyMetadata.of("STATE", Integer.class), CatalogFieldType.STATE),
	K4792(Catalog.K0063, KKey.of("K4792"), KKeyMetadata.of("BEMERK", String.class, 200));

	private static final Map<Catalog, List<CatalogField>> FIELDS_BY_CATALOG;
	private static final Map<KKey, CatalogField> FIELDS_BY_KEY;

	static {
		FIELDS_BY_CATALOG = Arrays.stream(values()).collect(groupingBy(CatalogField::getCatalog));
		FIELDS_BY_KEY = Arrays.stream(values()).collect(toMap(CatalogField::getKKey, Function.identity()));
	}

	private final Catalog catalog;
	private final KKey kKey;
	private final KKeyMetadata metadata;
	private final CatalogFieldType type;

	private CatalogField(Catalog catalog, KKey kKey, KKeyMetadata metadata) {
		this(catalog, kKey, metadata, CatalogFieldType.DATA);
	}

	private CatalogField(Catalog catalog, KKey kKey, KKeyMetadata metadata, CatalogFieldType type) {
		this.catalog = catalog;
		this.kKey = kKey;
		this.metadata = metadata;
		this.type = type;
	}

	public Catalog getCatalog() {
		return catalog;
	}

	public KKey getKKey() {
		return kKey;
	}

	public KKeyMetadata getMetadata() {
		return metadata;
	}

	public CatalogFieldType getType() {
		return type;
	}

	/**
	 * @return whether this field contains data of catalog entry (is not an identifier of active/non-active state field)
	 */
	public boolean isDataField() {
		return type == CatalogFieldType.DATA;
	}

	/**
	 * @return whether this field is identifier (surrogate key) of the given catalog
	 */
	public boolean isIdField() {
		return type == CatalogFieldType.ID;
	}

	public static Set<Catalog> catalogsWithDefinedFields() {
		return new HashSet<>(FIELDS_BY_CATALOG.keySet());
	}

	/**
	 * @param catalog
	 * @return
	 * @throws IllegalArgumentException if there are no {@link CatalogField fields} defined for the given catalog
	 */
	public static List<CatalogField> fieldsOfCatalog(Catalog catalog) {
		List<CatalogField> fields = FIELDS_BY_CATALOG.get(catalog);

		if (fields == null) {
			throw new IllegalArgumentException("Unknown catalog " + catalog);
		}

		return fields;
	}

	/**
	 * Gets the {@link CatalogField field} which is identifier (surrogate key) of the given catalog.
	 *
	 * @param catalog
	 * @return
	 */
	public static CatalogField idFieldOfCatalog(Catalog catalog) {
		return fieldsOfCatalog(catalog)
									.stream()
									.filter(field -> field.getType() == CatalogFieldType.ID)
									.findAny()
									.orElseThrow(() -> new IllegalStateException("Catalog " + catalog + " does not have ID column defined."));
	}

	public static List<KKey> getKKeysOfCatalog(Catalog catalog) {
		return getKKeysOfCatalog(catalog, e -> true);
	}

	public static List<KKey> getKKeysOfCatalog(Catalog catalog, Predicate<CatalogField> predicate) {
		return fieldsOfCatalog(catalog)
									.stream()
									.filter(predicate)
									.map(CatalogField::getKKey)
									.sorted()
									.collect(toList());
	}

	public static CatalogField forKKey(KKey kKey) {
		return FIELDS_BY_KEY.get(kKey);
	}

	public static KKeyMetadata getMetadataFor(KKey kKey) {
		CatalogField field = forKKey(kKey);

		return field == null ? null : field.getMetadata();
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public enum CatalogFieldType {
		/**
		 * Identifier of catalog (surrogate key)
		 */
		ID,

		/**
		 * Data fields contains actial data of the catalog record
		 */
		DATA,

		/**
		 * State fields tells if the catalog record is active or non-active (soft deleted)
		 */
		STATE;
	}
}
