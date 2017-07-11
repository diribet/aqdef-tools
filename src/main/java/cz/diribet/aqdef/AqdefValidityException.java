package cz.diribet.aqdef;

/**
 * Indicates that the AQDEF content has invalid structure, format or is malformed in another way.
 *
 * @author Vlastimil Dolejs
 *
 */
public class AqdefValidityException extends RuntimeException {

	public AqdefValidityException() {
		super("Invalid AQDEF structure.");
	}

	public AqdefValidityException(String message, Throwable cause) {
		super("Invalid AQDEF structure: " + message, cause);
	}

	public AqdefValidityException(String message) {
		super("Invalid AQDEF structure: " + message);
	}

	public AqdefValidityException(Throwable cause) {
		super("Invalid AQDEF structure.", cause);
	}
}
