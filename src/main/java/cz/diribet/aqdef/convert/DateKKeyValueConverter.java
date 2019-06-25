package cz.diribet.aqdef.convert;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * @author Vlastimil Dolejs
 *
 */
public class DateKKeyValueConverter implements IKKeyValueConverter<Date> {

	private final FastDateFormat outputFormat = newDateFormat("dd.MM.yyyy/HH:mm:ss");

	private final FastDateFormat[] inputFormats = new FastDateFormat[] {
		newDateFormat("dd.MM.yy/HH:mm:ss"),
		newDateFormat("MM/dd/yy/HH:mm:ss"),
		newDateFormat("yy-MM-dd/HH:mm:ss"),
		newDateFormat("dd.MM.yy/HH:mm"),
		newDateFormat("MM/dd/yy/HH:mm"),
		newDateFormat("yy-MM-dd/HH:mm"),
		newDateFormat("dd.MM.yy HH:mm:ss"),
		newDateFormat("MM/dd/yy HH:mm:ss"),
		newDateFormat("yy-MM-dd HH:mm:ss"),
		newDateFormat("dd.MM.yy HH:mm"),
		newDateFormat("MM/dd/yy HH:mm"),
		newDateFormat("yy-MM-dd HH:mm")
	};

	private FastDateFormat newDateFormat(String pattern) {
		return FastDateFormat.getInstance(pattern, TimeZone.getDefault());
	}

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
