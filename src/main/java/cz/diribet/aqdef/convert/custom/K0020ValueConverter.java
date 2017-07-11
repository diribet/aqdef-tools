package cz.diribet.aqdef.convert.custom;

import cz.diribet.aqdef.convert.IKKeyValueConverter;
import cz.diribet.aqdef.convert.IntegerKKeyValueConverter;
import cz.diribet.aqdef.convert.KKeyValueConversionException;

/**
 * Divides value of K0020 by 1000.
 *
 * @author Vlastimil Dolejs
 *
 */
public class K0020ValueConverter implements IKKeyValueConverter<Integer> {

	private IntegerKKeyValueConverter integerConverter = new IntegerKKeyValueConverter();

	@Override
	public Integer convert(String value) throws KKeyValueConversionException {
		Integer number = integerConverter.convert(value);

		if (number == null) {
			return null;
		} else {
			return number / 1000;
		}
	}

	@Override
	public String toString(Integer value) {
		if (value == null) {
			return null;
		}

		return integerConverter.toString(value * 1000);
	}

}
