package cz.diribet.aqdef.convert;


/**
 * @author Vlastimil Dolejs
 *
 */
public class BooleanKKeyValueConverter implements IKKeyValueConverter<Boolean> {

	private final IntegerKKeyValueConverter integerConverter = new IntegerKKeyValueConverter();

	@Override
	public Boolean convert(String value) throws KKeyValueConversionException {
		Integer integerValue = integerConverter.convert(value);

		if (integerValue == null) {
			return null;
		} else if (integerValue == 1) {
			return true;
		} else if (integerValue == 0) {
			return false;
		} else {
			throw new KKeyValueConversionException(value, Boolean.class, null);
		}
	}

	@Override
	public String toString(Boolean value) {
		if (value == null) {
			return null;
		} else {
			return value ? "1" : "0";
		}
	}

}
