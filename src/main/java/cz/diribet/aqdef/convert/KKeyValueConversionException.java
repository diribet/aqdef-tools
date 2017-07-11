package cz.diribet.aqdef.convert;

public class KKeyValueConversionException extends Exception {

	public KKeyValueConversionException(String value, Class<?> dataType, Throwable cause) {
		super("Failed to convert value: " + value + " to data type: " + dataType.getName(), cause);
	}

	public KKeyValueConversionException(String message, Throwable cause) {
		super(message, cause);
	}

}
