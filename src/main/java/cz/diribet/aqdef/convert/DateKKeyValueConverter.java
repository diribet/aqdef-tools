package cz.diribet.aqdef.convert;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

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
		newDateFormat("yyyy-M-d.H:m"),

		DateTimeFormatter.ISO_LOCAL_DATE_TIME,
		DateTimeFormatter.ISO_OFFSET_DATE_TIME
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
			// Offset date time
			try {
				OffsetDateTime dateTime = OffsetDateTime.parse(value, inputFormat);
				return Date.from(dateTime.toInstant());

			} catch (Throwable ignored) {}

			// Local date time
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
