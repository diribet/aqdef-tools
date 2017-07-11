package cz.diribet.aqdef.convert;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * @author Vlastimil Dolejs
 *
 */
public class DateKKeyValueConverter implements IKKeyValueConverter<Date> {

	private final FastDateFormat outputFormat = FastDateFormat.getInstance("dd.MM.yyyy/HH:mm:ss");

	private final FastDateFormat[] inputFormats = new FastDateFormat[] {
		FastDateFormat.getInstance("dd.MM.yyyy/HH:mm:ss"),
		FastDateFormat.getInstance("MM/dd/yyyy/HH:mm:ss"),
		FastDateFormat.getInstance("yyyy-MM-dd/HH:mm:ss"),
		FastDateFormat.getInstance("dd.MM.yyyy/HH:mm"),
		FastDateFormat.getInstance("MM/dd/yyyy/HH:mm"),
		FastDateFormat.getInstance("yyyy-MM-dd/HH:mm"),
		FastDateFormat.getInstance("dd.MM.yyyy HH:mm:ss"),
		FastDateFormat.getInstance("MM/dd/yyyy HH:mm:ss"),
		FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss"),
		FastDateFormat.getInstance("dd.MM.yyyy HH:mm"),
		FastDateFormat.getInstance("MM/dd/yyyy HH:mm"),
		FastDateFormat.getInstance("yyyy-MM-dd HH:mm")
	};

	@Override
	public Date convert(String value) throws KKeyValueConversionException {
		if (StringUtils.isEmpty(value)) {
			return null;
		}

		Throwable firstException = null;
		for (FastDateFormat inputFormat : inputFormats) {
			try {
				return inputFormat.parse(value);
			} catch (Throwable e) {
				if (firstException == null) {
					firstException = e;
				}
			}
		}

		throw new KKeyValueConversionException(value, Date.class, firstException);
	}

	@Override
	public String toString(Date value) {
		if (value == null) {
			return null;
		}

		return outputFormat.format(value);
	}

}
