package cz.diribet.aqdef.catalog;

/**
 * AQDEF catalogs
 *
 * @author Vlastimil Dolejs
 * @see CatalogField
 */
public enum Catalog {
	//*******************************************
	// Attributes
	//*******************************************

	SUPPLIER("LIEFERAN"),
	CUSTOMER("KUNDE"),
	EMPLOYEE("MITARB"),
	OPERATOR("PRUEFER"),
	MACHINE("MASCHINE"),
	TOOL("NEST"),
	MANUFACTURER("HERSTELL"),
	MATERIAL("WERKSTOF"),
	UNIT("EINHEIT"),
	DRAWING("ZEICHN"),
	PRODUCT("ERZEUGNIS"),
	PURCHASE_ORDER("PAUFTRAG"),

	/**
	 * BEWARE - table is the same for Event, Cause and Measure catalogs - catalogs are separated by ID offset (OMG WTF?)
	 */
	EVENT("EREIGTXT"),

	/**
	 * BEWARE - table is the same for Event, Cause and Measure catalogs - catalogs are separated by ID offset (OMG WTF?)
	 */
	CAUSE("EREIGTXT"),
	/**
	 * BEWARE - table is the same for Event, Cause and Measure catalogs - catalogs are separated by ID offset (OMG WTF?)
	 */
	MEASURE("EREIGTXT"),

	ORDINAL_CLASS("ORDKLASS"),
	CONTRACTOR("AUFTRGEB"),
	GAGE("PRUEFMIT"),
	PROCESS_PARAMETER("PROZPARAMTXT"),
	K0061("KAT_4270"),
	K0062("KAT_4280"),
	K0063("KAT_4290");

	private final String tableName;

	//*******************************************
	// Constructors
	//*******************************************

	private Catalog(String tableName) {
		this.tableName = tableName;
	}

	//*******************************************
	// Getters / setters
	//*******************************************

	public String getTableName() {
		return tableName;
	}

}
