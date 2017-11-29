package cz.diribet.aqdef.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import cz.diribet.aqdef.AqdefConstants;
import cz.diribet.aqdef.AqdefValidityException;
import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.KKeyMetadata;
import cz.diribet.aqdef.KKeyRepository;
import cz.diribet.aqdef.convert.IKKeyValueConverter;
import cz.diribet.aqdef.model.AqdefObjectModel;
import cz.diribet.aqdef.model.AqdefObjectModel.CharacteristicEntries;
import cz.diribet.aqdef.model.CharacteristicIndex;
import cz.diribet.aqdef.model.GroupIndex;
import cz.diribet.aqdef.model.PartIndex;
import cz.diribet.aqdef.model.ValueIndex;

/**
 * Parses {@link AqdefObjectModel} from a AQDEF content (file or other data source).
 *
 * @author Vlastimil Dolejs
 *
 */
public class AqdefParser implements AqdefConstants {
	//*******************************************
	// Attributes
	//*******************************************

	private static final Logger LOG = LoggerFactory.getLogger(AqdefParser.class);

	private static final String IGNORED_BINARY_KEY = "ignore";

	private static final String[] BINARY_VALUE_PORTIONS = new String[] {
		"K0001", "K0002", "K0004", "K0005", "K0006", "K0007", "K0008", "K0010", "K0011", "K0012"};

	private static final String[] BINARY_ATTRIBUTE_VALUE_PORTIONS = new String[] {
		"K0020", "K0021", IGNORED_BINARY_KEY, "K0002", "K0004", "K0005", "K0006", "K0007", "K0008", "K0010", "K0011", "K0012"};

	/**
	 * These keys does not contain any information and we can safely ignore them.
	 */
	private static final String[] IGNORED_KEYS = new String[] { "K0100", "K100", "K0101", "K101" };

	/**
	 * These keys contain Q-DAS qs-STAT properietary internal configuration so we can safely ignore them.
	 */
	private static final String[] PROPRIETARY_QDAS_KEYS = new String[] { "K1998", "K2998", "K2999", "K5098", "K5080" };

	private final KKeyRepository kKeyRepository;

	//*******************************************
	// Constructors
	//*******************************************

	public AqdefParser() {
		this.kKeyRepository = KKeyRepository.getInstance();
	}

	//*******************************************
	// Methods
	//*******************************************

	public AqdefObjectModel parse(String content) throws IOException {
		return parse(new StringReader(content));
	}

	public AqdefObjectModel parse(Path file, String encoding) throws IOException {
		return parse(file.toFile(), encoding);
	}

	public AqdefObjectModel parse(File file, String encoding) throws IOException {
		try (InputStream fileInputStream = createFileInputStream(file, encoding)) {
			return parse(fileInputStream, encoding);
		}
	}

	public AqdefObjectModel parse(InputStream inputStream, String encoding) throws IOException {
		try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, encoding)) {
			return parse(inputStreamReader);
		}
	}

	public AqdefObjectModel parse(Reader reader) throws IOException {
		AqdefObjectModel aqdefObjectModel = new AqdefObjectModel();
		ParserContext context = new ParserContext();

		int lineIndex = 0;
		try (BufferedReader bufferedReader = new BufferedReader(reader)) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				context.setCurrentLine(lineIndex);

				try {
					line = line.trim();

					if (StringUtils.isNotEmpty(line)) {
						parseLine(line, aqdefObjectModel, context);
					}
				} catch (Throwable e) {
					throw new DfqParserException(context, e);
				}

				lineIndex++;
			}
		}

		aqdefObjectModel.normalize();

		return aqdefObjectModel;
	}


	private void parseLine(String line, AqdefObjectModel aqdefObjectModel, ParserContext context) {
		if (isKKeyLine(line)) {
			parseKKeyLine(line, aqdefObjectModel, context);
		} else if (isBinaryDataLine(line)) {
			parseBinaryDataLine(line, aqdefObjectModel, context);
		} else {
			LOG.warn("{} Invalid line format. This line will be discarded. Line content: {}", lineLogContext(context), line);
		}
	}

	private boolean isKKeyLine(String line) {
		return line.startsWith("K");
	}

	private boolean isBinaryDataLine(String line) {
		return line.indexOf(MEASURED_VALUES_DATA_SEPARATOR) >= 0;
	}

	private void parseKKeyLine(String line, AqdefObjectModel aqdefObjectModel, ParserContext context) {
		if (shouldIgnoreKKeyLine(line)) {
			return;
		}

		String key = line.substring(0, 5);
		KKey kKey = KKey.of(key);

		boolean hasIndex = false;
		if (line.length() > 5) {
			hasIndex = line.charAt(5) == '/';
		}

		int firstSpaceIndex = line.indexOf(" ", 5);
		if (firstSpaceIndex == -1) {
			firstSpaceIndex = line.length();
		}

		Integer index = 1;
		Integer valueIndexNumber = null;
		if (hasIndex) {
			String indexString = line.substring(6, firstSpaceIndex);
			if (StringUtils.isNotBlank(indexString)) {
				int valueIndexSeparatorPosition = indexString.indexOf("/");
				if (valueIndexSeparatorPosition == -1) {
					try {
						index = Integer.valueOf(indexString);
					} catch (NumberFormatException e) {
						throw new AqdefValidityException("K-key index is invalid: " + indexString, e);
					}
				} else {
					if (kKey.isValueLevel()) {
						try {
							index = Integer.valueOf(indexString.substring(0, valueIndexSeparatorPosition));
							valueIndexNumber = Integer.valueOf(indexString.substring(valueIndexSeparatorPosition + 1));
						} catch (NumberFormatException e) {
							throw new AqdefValidityException("K-key index is invalid: " + indexString, e);
						}
					} else {
						throw new AqdefValidityException("K-key index (" + indexString + ") contains a value index but the K-key (" + kKey + ") is not a value key.");
					}
				}
			}
		}

		String valueString = line.substring(firstSpaceIndex).trim();

		Object value;
		try {
			value = convertValue(kKey, valueString, context);
		} catch (UnknownKKeyException | ValueConversionException e) {
			//TODO: 2016/04/11 - vlasta: we should provide information that parsed AqdefObjectModel doesn't contain all data?
			value = null;
		}

		if (value == null) {
			return;
		}

		if (kKey.isPartLevel()) {

			PartIndex partIndex = PartIndex.of(index);
			aqdefObjectModel.putPartEntry(kKey, partIndex, value);

			context.setCurrentPartIndex(partIndex);

		} else if (kKey.isCharacteristicLevel()) {

			PartIndex partIndex;
			if (index == 0) {
				partIndex = PartIndex.of(0);
			} else {
				partIndex = context.getCurrentPartIndex();
				if (partIndex == null || partIndex.getIndex() == null || partIndex.getIndex() == 0) {
					// no part k-key found before this characteristic - add it to the first part
					partIndex = PartIndex.of(1);
				}
			}
			CharacteristicIndex characteristicIndex = CharacteristicIndex.of(partIndex, index);

			aqdefObjectModel.putCharacteristicEntry(kKey, characteristicIndex, value);

		} else if (kKey.isGroupLevel()) {

			PartIndex partIndex;
			if (index == 0) {
				partIndex = PartIndex.of(0);
			} else {
				partIndex = context.getCurrentPartIndex();
				if (partIndex == null || partIndex.getIndex() == null || partIndex.getIndex() == 0) {
					// no part k-key found before this group - add it to the first part
					partIndex = PartIndex.of(1);
				}
			}
			GroupIndex groupIndex = GroupIndex.of(partIndex, index);

			aqdefObjectModel.putGroupEntry(kKey, groupIndex, value);

		} else if (kKey.isValueLevel()) {

			PartIndex partIndex;
			if (index == 0) {
				partIndex = PartIndex.of(0);
			} else {
				partIndex = aqdefObjectModel.findPartIndexForCharacteristic(index);
				if (partIndex == null) {
					throw new AqdefValidityException("Characteristic with index " + index + " was not found. Can't parse value.");
				}
			}
			CharacteristicIndex characteristicIndex = CharacteristicIndex.of(partIndex, index);
			ValueIndex valueIndex;
			if (valueIndexNumber == null) {
				valueIndex = context.getValueIndexCounter().getIndex(characteristicIndex, kKey);
			} else {
				valueIndex = ValueIndex.of(characteristicIndex, valueIndexNumber);
			}

			aqdefObjectModel.putValueEntry(kKey, valueIndex, value);

		} else if (kKey.isHierarchyLevel() || kKey.isSimpleHierarchyLevel()) {

			aqdefObjectModel.putHierarchyEntry(kKey, index, value);

		} else {

			LOG.warn("{} Unknown level of k-key {}. Key will be ignored! ", lineLogContext(context), kKey);

		}
	}

	private boolean shouldIgnoreKKeyLine(String line) {
		if (line.length() < 5) {
			return true;
		}

		for (String ignoredKey : IGNORED_KEYS) {
			if (line.startsWith(ignoredKey + " ") || line.startsWith(ignoredKey + "/")) {
				return true;
			}
		}

		for (String proprietaryKey : PROPRIETARY_QDAS_KEYS) {
			if (line.startsWith(proprietaryKey)) {
				return true;
			}
		}

		return false;
	}

	private Object convertValue(KKey key, String valueString, ParserContext context) throws UnknownKKeyException, ValueConversionException {
		if (StringUtils.isBlank(valueString)) {
			return null;
		}

		KKeyMetadata kKeyMetadata = kKeyRepository.getMetadataFor(key);
		if (kKeyMetadata == null) {
			LOG.warn("{} Unknown k-key: {}. Value will be discarded.", lineLogContext(context), key);
			throw new UnknownKKeyException(key);
		}

		IKKeyValueConverter<?> converter = kKeyMetadata.getConverter();

		try {

			return converter.convert(valueString);

		} catch (Throwable e) {
			LOG.warn(lineLogContext(context) + " Failed to convert value: " + valueString + " of K-key: " + key + " using converter: " + converter + ". The value will be discarded.", e);
			throw new ValueConversionException(valueString, key, converter, e);
		}
	}

	private void parseBinaryDataLine(String line, AqdefObjectModel aqdefObjectModel, ParserContext context) {
		String[] characteristicPortions = line.split(MEASURED_VALUES_CHARACTERISTIC_SEPARATOR);

		int characteristicIntIndex = 1;
		for (String characteristicPortion : characteristicPortions) {
			String[] dataPortions = characteristicPortion.split(MEASURED_VALUES_DATA_SEPARATOR);

			PartIndex partIndex = aqdefObjectModel.findPartIndexForCharacteristic(characteristicIntIndex);
			if (partIndex == null) {
				throw new AqdefValidityException("Characteristic with index " + characteristicIntIndex + " was not found. Can't parse value.");
			}
			CharacteristicIndex characteristicIndex = CharacteristicIndex.of(partIndex, characteristicIntIndex);

			// recognize binary value format for characterstic type
			Boolean isAttributeCharacteristic = null;
			CharacteristicEntries characteristicEntries = aqdefObjectModel.getCharacteristicEntries(characteristicIndex);
			if (characteristicEntries != null) {
				Integer characteristicType = characteristicEntries.getValue("K2004");

				if (characteristicType != null) {
					if (characteristicType == 1 || characteristicType == 5 || characteristicType == 6) { // 1 - attribute / 5, 6 - error log sheet
						isAttributeCharacteristic = true;
					} else {
						isAttributeCharacteristic = false;
					}
				}
			}

			// if the information about characteristic type is not available, then try to guess it from binary portion count
			if (isAttributeCharacteristic == null) {
				isAttributeCharacteristic = dataPortions.length > 10; // attribute values has more than 10 portions
			}

			String[] dataPortionKeys;
			if (isAttributeCharacteristic) {
				dataPortionKeys = BINARY_ATTRIBUTE_VALUE_PORTIONS;
			} else {
				dataPortionKeys = BINARY_VALUE_PORTIONS;
			}

			for (int i = 0; i < dataPortions.length; i++) {
				String dataPortion = dataPortions[i];
				String key = dataPortionKeys[i];

				if (key == IGNORED_BINARY_KEY) {
					continue;
				}

				KKey kKey = KKey.of(key);

				Object value;
				try {
					value = convertValue(kKey, dataPortion, context);
				} catch (UnknownKKeyException | ValueConversionException e) {
					//TODO: 2016/04/11 - vlasta: we should provide information that parsed AqdefObjectModel doesn't contain all data?
					value = null;
				}

				if (value != null) {
					ValueIndex valueIndex = context.getValueIndexCounter().getIndex(characteristicIndex, kKey);

					aqdefObjectModel.putValueEntry(kKey, valueIndex, value);
				}
			}

			characteristicIntIndex++;
		}
	}

	private String lineLogContext(ParserContext context) {
		return new StringBuilder().append("Line ").append(context.getCurrentLine()).append(":").toString();
	}

	private InputStream createFileInputStream(File file, String encoding) throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(file);

		if ("utf-8".equalsIgnoreCase(encoding)) {
			inputStream = new BOMInputStream(inputStream);
		}

		return inputStream;
	}

	//*******************************************
	// Inner classes
	//*******************************************

	/**
	 * @author Vlastimil Dolejs
	 *
	 */
	private static class ValueIndexCounter {
		private final Map<CharacteristicIndex, Set<KKey>> keys = new HashMap<>();
		private final Map<CharacteristicIndex, Integer> indexes = new HashMap<>();

		public ValueIndex getIndex(CharacteristicIndex characteristicIndex, KKey key) {
			Set<KKey> keysOfCurrentValue = keys.get(characteristicIndex);

			if (keysOfCurrentValue == null) {
				keysOfCurrentValue = Sets.newHashSet();
				keys.put(characteristicIndex, keysOfCurrentValue);
			}

			Integer currentIndex = indexes.get(characteristicIndex);
			currentIndex = currentIndex == null ? 1 : currentIndex;

			if (keysOfCurrentValue.contains(key)) {
				// this key was already parsed, so it is new value
				keysOfCurrentValue.clear();
				currentIndex++;
				indexes.put(characteristicIndex, currentIndex);
			}
			keysOfCurrentValue.add(key);

			return ValueIndex.of(characteristicIndex, currentIndex);
		}
	}

	/**
	 * @author Vlastimil Dolejs
	 *
	 */
	private static class ParserContext {
		private int currentLine;
		private PartIndex currentPartIndex;
		private final ValueIndexCounter valueIndexCounter = new ValueIndexCounter();

		public int getCurrentLine() {
			return currentLine;
		}

		public void setCurrentLine(int currentLine) {
			this.currentLine = currentLine;
		}

		public PartIndex getCurrentPartIndex() {
			return currentPartIndex;
		}

		public void setCurrentPartIndex(PartIndex currentPartIndex) {
			this.currentPartIndex = currentPartIndex;
		}

		public ValueIndexCounter getValueIndexCounter() {
			return valueIndexCounter;
		}
	}

	//*******************************************
	// Exceptions
	//*******************************************

	private static class DfqParserException extends RuntimeException {
		public DfqParserException(ParserContext context, Throwable cause) {
			super(message(context, cause), cause);
		}

		private static String message(ParserContext context, Throwable cause) {
			String message = "Failed to parse DFQ file. Error at line: " + context.getCurrentLine();

			if (cause != null && cause.getMessage() != null) {
				message += " Cause: " + cause.getMessage();
			}

			return message;
		}
	}

	@SuppressWarnings("unused")
	private static class UnknownKKeyException extends Exception {
		private final KKey key;

		public UnknownKKeyException(KKey key) {
			super();
			this.key = key;
		}

		public KKey getKey() {
			return key;
		}

	}

	@SuppressWarnings("unused")
	private static class ValueConversionException extends Exception {
		private final String value;
		private final KKey key;
		private final IKKeyValueConverter<?> converter;

		public ValueConversionException(String value, KKey key, IKKeyValueConverter<?> converter, Throwable cause) {
			super(cause);

			this.value = value;
			this.key = key;
			this.converter = converter;
		}

		public String getValue() {
			return value;
		}

		public KKey getKey() {
			return key;
		}

		public IKKeyValueConverter<?> getConverter() {
			return converter;
		}

	}
}
