package cz.diribet.aqdef;

/**
 * @author Vlastimil Dolejs
 *
 */
public interface AqdefConstants {

	/**
	 * Separates lines of data file
	 */
	String LINE_SEPARATOR = "\r\n";

	/**
	 * Separates value from k-key on single line
	 */
	String VALUES_SEPARATOR = " ";

	/**
	 * Separates fields of single characteristic in lines with measured values (notation without the use of K-Keys)<br>
	 * The sequence of fields is:<br>
	 * 1 Value<br>
	 * 2 Attribute<br>
	 * 3 Date/Time<br>
	 * 4 Events<br>
	 * 5 Batch number<br>
	 * 6 Nest number<br>
	 * 7 Operator number<br>
	 * 8 Machine number<br>
	 * 9 Process parameter<br>
	 * 10 Gage number<br>
	 */
	String MEASURED_VALUES_DATA_SEPARATOR = String.valueOf((char) 20);

	/**
	 * Separates characteristic portions in lines with measured values (notation without the use of K-Keys)
	 */
	String MEASURED_VALUES_CHARACTERISTIC_SEPARATOR = String.valueOf((char) 15);

}
