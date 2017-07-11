package cz.diribet.aqdef.convert;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Vlastimil Dolejs
 *
 */
public class IntegerKKeyValueConverter implements IKKeyValueConverter<Integer> {

	@Override
	public Integer convert(String value) throws KKeyValueConversionException {
		if (StringUtils.isEmpty(value)) {
			return null;
		}

		try {
			return Integer.valueOf(value);
		} catch (Throwable e) {
			throw new KKeyValueConversionException(value, Integer.class, e);
		}
	}

	@Override
	public String toString(Integer value) {
		if (value == null) {
			return null;
		} else {
			return value.toString();
		}
	}

}
