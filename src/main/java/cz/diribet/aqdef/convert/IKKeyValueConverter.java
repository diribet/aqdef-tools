package cz.diribet.aqdef.convert;

import java.io.Serializable;

public interface IKKeyValueConverter<T> extends Serializable {

	public T convert(String value) throws KKeyValueConversionException;

	public String toString(T value);

}
