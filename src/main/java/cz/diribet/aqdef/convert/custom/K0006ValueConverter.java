package cz.diribet.aqdef.convert.custom;

import org.apache.commons.lang3.StringUtils;

import cz.diribet.aqdef.convert.IKKeyValueConverter;
import cz.diribet.aqdef.convert.KKeyValueConversionException;

/**
 * Removes leading # from K0006 (charge).
 *
 * @author Vlastimil Dolejs
 *
 */
public class K0006ValueConverter implements IKKeyValueConverter<String> {

	@Override
	public String convert(String value) throws KKeyValueConversionException {
		if (StringUtils.isEmpty(value)) {
			return null;
		}

		if (value.startsWith("#")) {
			value = value.substring(1);
		}

		return value;
	}

	@Override
	public String toString(String value) {
		if (value == null) {
			return null;
		}

		return new StringBuilder().append("#").append(value).toString();
	}

}
