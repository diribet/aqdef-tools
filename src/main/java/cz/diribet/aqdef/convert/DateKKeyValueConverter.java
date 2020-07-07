package cz.diribet.aqdef.convert;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Vlastimil Dolejs
 *
 */
public class DateKKeyValueConverter implements IKKeyValueConverter<Date> {

	private static final DateTimeFormatter OUTPUT_FORMATTER = newDateFormat("dd.MM.yyyy/HH:mm:ss");

	private static final List<DateTimeFormatter> INPUT_FORMATTERS = List.of(
		newDateFormat("d.M.yy/H:m:s"),
		newDateFormat("d.M.yy/H:m"),
		newDateFormat("d.M.yyyy/H:m:s"),
		newDateFormat("d.M.yyyy/H:m"),

		newDateFormat("M/d/yy/H:m:s"),
		newDateFormat("M/d/yy/H:m"),
		newDateFormat("M/d/yyyy/H:m:s"),
		newDateFormat("M/d/yyyy/H:m"),

		newDateFormat("yy-M-d/H:m:s"),
		newDateFormat("yy-M-d/H:m"),
		newDateFormat("yyyy-M-d/H:m:s"),
		newDateFormat("yyyy-M-d/H:m"),

		newDateFormat("d.M.yy H:m:s"),
		newDateFormat("d.M.yy H:m"),
		newDateFormat("d.M.yyyy H:m:s"),
		newDateFormat("d.M.yyyy H:m"),

		newDateFormat("M/d/yy H:m:s"),
		newDateFormat("M/d/yy H:m"),
		newDateFormat("M/d/yyyy H:m:s"),
		newDateFormat("M/d/yyyy H:m"),

		newDateFormat("yy-M-d H:m:s"),
		newDateFormat("yy-M-d H:m"),
		newDateFormat("yyyy-M-d H:m:s"),
		newDateFormat("yyyy-M-d H:m"),

		newDateFormat("d.M.yy.H:m:s"),
		newDateFormat("d.M.yy.H:m"),
		newDateFormat("d.M.yyyy.H:m:s"),
		newDateFormat("d.M.yyyy.H:m"),

		newDateFormat("M/d/yy.H:m:s"),
		newDateFormat("M/d/yy.H:m"),
		newDateFormat("M/d/yyyy.H:m:s"),
		newDateFormat("M/d/yyyy.H:m"),

		newDateFormat("yy-M-d.H:m:s"),
		newDateFormat("yy-M-d.H:m"),
		newDateFormat("yyyy-M-d.H:m:s"),
		newDateFormat("yyyy-M-d.H:m")
	);

	private static DateTimeFormatter newDateFormat(String pattern) {
		return DateTimeFormatter.ofPattern(pattern);
	}

	@Override
	public Date convert(String value) throws KKeyValueConversionException {
		if (StringUtils.isEmpty(value)) {
			return null;
		}

		for (DateTimeFormatter inputFormat : INPUT_FORMATTERS) {
			try {
				LocalDateTime dateTime = LocalDateTime.parse(value, inputFormat);
				return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());

			} catch (Throwable ignored) {}
		}

		throw new KKeyValueConversionException("Failed to convert value:" + value + " to Date. Unsupported format.");
	}

	@Override
	public String toString(Date value) {
		if (value == null) {
			return null;
		}

		LocalDateTime localDateTime = LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());

		return OUTPUT_FORMATTER.format(localDateTime);
	}

}
