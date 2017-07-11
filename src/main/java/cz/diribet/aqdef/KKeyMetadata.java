package cz.diribet.aqdef;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

import cz.diribet.aqdef.convert.BigDecimalKKeyValueConverter;
import cz.diribet.aqdef.convert.DateKKeyValueConverter;
import cz.diribet.aqdef.convert.IKKeyValueConverter;
import cz.diribet.aqdef.convert.IntegerKKeyValueConverter;
import cz.diribet.aqdef.convert.StringKKeyValueConverter;

/**
 * Contains metadata of a {@link KKey}.
 * <p>
 * This class is immutable and therefore thread-safe.
 * </p>
 * @author Vlastimil Dolejs
 *
 */
public final class KKeyMetadata {

	private final String columnName;
	private final Class<?> dataType;

	/**
	 * Length of data (for String and Number data types)
	 */
	private final Integer length;

	private final IKKeyValueConverter<?> converter;

	/**
	 * Whether values of this column should be saved to DB.
	 * There are some K-keys which should not be written to DB or has unknown column name.
	 */
	private final boolean saveToDb;

	private KKeyMetadata(
						String columnName,
						Class<?> dataType,
						Integer length,
						IKKeyValueConverter<?> converter,
						boolean saveToDb) {
		super();

		requireNonNull(columnName);
		requireNonNull(dataType);
		requireNonNull(converter);

		this.columnName = columnName;
		this.dataType = dataType;
		this.length = length;
		this.converter = converter;
		this.saveToDb = saveToDb;
	}

	public static KKeyMetadata of(String columnName, Class<?> dataType) {
		return of(columnName, dataType, (Integer) null);
	}

	public static KKeyMetadata of(String columnName, Class<?> dataType, boolean saveToDb) {
		return of(columnName, dataType, (Integer) null, saveToDb);
	}

	public static KKeyMetadata of(String columnName, Class<?> dataType, Integer length) {
		return of(columnName, dataType, length, true);
	}

	public static KKeyMetadata of(String columnName, Class<?> dataType, Integer length, boolean saveToDb) {
		IKKeyValueConverter<?> converter;
		if (String.class.equals(dataType) || UUID.class.equals(dataType)) {

			converter = new StringKKeyValueConverter();

		} else if (Integer.class.equals(dataType)) {

			converter = new IntegerKKeyValueConverter();

		} else if (BigDecimal.class.equals(dataType)) {

			converter = new BigDecimalKKeyValueConverter();

		} else if (Date.class.equals(dataType)) {

			converter = new DateKKeyValueConverter();

		} else {

			throw new IllegalArgumentException("There is no converter defined for data type: " + dataType.getName());

		}

		return new KKeyMetadata(columnName, dataType, length, converter, saveToDb);
	}

	public static <T> KKeyMetadata of(String columnName, Class<T> dataType, IKKeyValueConverter<T> converter) {
		return of(columnName, dataType, converter, true);
	}

	public static <T> KKeyMetadata of(String columnName, Class<T> dataType, IKKeyValueConverter<T> converter, boolean saveToDb) {
		return new KKeyMetadata(columnName, dataType, null, converter, saveToDb);
	}

	public static <T> KKeyMetadata of(String columnName, Class<T> dataType, int length, IKKeyValueConverter<T> converter) {
		return new KKeyMetadata(columnName, dataType, length, converter, true);
	}

	/**
	 * Returns the maximum length of the content of this K-key as defined in AQDEF format documentation.
	 *
	 * @return
	 */
	public Integer getLength() {
		return length;
	}

	/**
	 * Returns name of the DB column in the Q-DAS database where this K-key is stored.
	 *
	 * @return
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * Returns datatype of this K-key.
	 *
	 * @return
	 */
	public Class<?> getDataType() {
		return dataType;
	}

	/**
	 * Returns converter that can be used to convert the value of this K-key from and to the textual representation of AQDEF
	 * format.
	 *
	 * @return
	 */
	public IKKeyValueConverter<?> getConverter() {
		return converter;
	}

	/**
	 * Whether this K-key is stored in Q-DAS database.
	 *
	 * @return
	 */
	public boolean isSaveToDb() {
		return saveToDb;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((length == null) ? 0 : length.hashCode());
		result = prime * result + (saveToDb ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof KKeyMetadata)) {
			return false;
		}
		KKeyMetadata other = (KKeyMetadata) obj;
		if (columnName == null) {
			if (other.columnName != null) {
				return false;
			}
		} else if (!columnName.equals(other.columnName)) {
			return false;
		}
		if (dataType == null) {
			if (other.dataType != null) {
				return false;
			}
		} else if (!dataType.equals(other.dataType)) {
			return false;
		}
		if (length == null) {
			if (other.length != null) {
				return false;
			}
		} else if (!length.equals(other.length)) {
			return false;
		}
		if (saveToDb != other.saveToDb) {
			return false;
		}
		return true;
	}

}