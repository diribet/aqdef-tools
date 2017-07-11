package cz.diribet.aqdef.convert;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Vlastimil Dolejs
 *
 */
public class BigDecimalKKeyValueConverter implements IKKeyValueConverter<BigDecimal> {

	@Override
	public BigDecimal convert(String value) throws KKeyValueConversionException {
		if (StringUtils.isEmpty(value)) {
			return null;
		}

		value = value.replaceAll(",", ".");

		try {
			return new BigDecimal(value);
		} catch (Throwable e) {
			throw new KKeyValueConversionException(value, BigDecimal.class, e);
		}
	}

	@Override
	public String toString(BigDecimal value) {
		if (value == null) {
			return null;
		}

		return value.toPlainString();
	}

}
