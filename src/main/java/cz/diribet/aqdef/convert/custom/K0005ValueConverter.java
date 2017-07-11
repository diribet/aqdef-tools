package cz.diribet.aqdef.convert.custom;

import static java.util.stream.Collectors.joining;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import cz.diribet.aqdef.convert.IKKeyValueConverter;
import cz.diribet.aqdef.convert.KKeyValueConversionException;

/**
 * Transform K0005 to list of event ids. <br>
 * Sample content of K0005: "100, 101, 150"
 *
 * @author Vlastimil Dolejs
 *
 */
public class K0005ValueConverter implements IKKeyValueConverter<List<Integer>> {

	@Override
	public List<Integer> convert(String value) throws KKeyValueConversionException {
		if (StringUtils.isBlank(value)) {
			return null;
		}

		try {
			String[] eventIds = value.split(",");

			List<Integer> result = Lists.newArrayListWithCapacity(eventIds.length);

			for (String eventIdText : eventIds) {
				try {

					if (StringUtils.isNotBlank(eventIdText)) {
						eventIdText = eventIdText.trim();
						Integer eventId = Integer.valueOf(eventIdText);
						result.add(eventId);
					}

				} catch (NumberFormatException e) {
					throw new RuntimeException("Event id is not a valid integer: " + eventIdText, e);
				}
			}

			return result;

		} catch (Throwable e) {
			throw new KKeyValueConversionException("Invalid K0005 content (K0005=" + value + "). K0005 should contain integer event ids separated by , (colon).", e);
		}
	}

	@Override
	public String toString(List<Integer> value) {
		if (CollectionUtils.isEmpty(value)) {
			return null;
		}

		return value.stream().map((i) -> i.toString()).collect(joining(","));
	}

}
