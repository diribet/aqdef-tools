package cz.diribet.aqdef.convert;

public interface IKKeyValueConverter<T> {

	public T convert(String value) throws KKeyValueConversionException;

	public String toString(T value);

}
