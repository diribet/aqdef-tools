package cz.diribet.aqdef.convert;

/**
 * @author Vlastimil Dolejs
 *
 */
public class StringKKeyValueConverter implements IKKeyValueConverter<String> {

	@Override
	public String convert(String value) {
		return value;
	}

	@Override
	public String toString(String value) {
		return value;
	}

}
