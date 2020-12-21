package cz.diribet.aqdef.parser.line;

import cz.diribet.aqdef.AqdefValidityException;
import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.convert.BigDecimalKKeyValueConverter;
import cz.diribet.aqdef.convert.KKeyValueConversionException;
import cz.diribet.aqdef.model.AqdefObjectModel;
import cz.diribet.aqdef.model.AqdefObjectModel.CharacteristicEntries;
import cz.diribet.aqdef.model.CharacteristicIndex;
import cz.diribet.aqdef.model.PartIndex;
import cz.diribet.aqdef.model.ValueIndex;
import cz.diribet.aqdef.parser.ParserContext;
import lombok.NonNull;

public class BinaryLineParser extends AbstractLineParser {

    private static final String IGNORED_BINARY_KEY = "ignore";

    private static final String[] BINARY_VALUE_PORTIONS = new String[] {
            "K0001", "K0002", "K0004", "K0005", "K0006", "K0007", "K0008", "K0010", "K0011", "K0012"};

    private static final String[] BINARY_ATTRIBUTE_VALUE_PORTIONS = new String[] {
            "K0020", "K0021", IGNORED_BINARY_KEY, "K0002", "K0004", "K0005", "K0006", "K0007", "K0008", "K0010", "K0011", "K0012"};

    @Override
    public boolean isLineSupported(@NonNull String line) {
        if (line.contains(MEASURED_VALUES_CHARACTERISTIC_SEPARATOR) ||
            line.contains(MEASURED_VALUES_DATA_SEPARATOR)) {

            return true;
        }

        try {
            // line with a single measured value
            new BigDecimalKKeyValueConverter().convert(line);
            return true;

        } catch (KKeyValueConversionException ignore) {}

        return false;
    }

    @Override
    public void parseLine(@NonNull String line,
                          @NonNull AqdefObjectModel aqdefObjectModel,
                          @NonNull ParserContext parserContext) {

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
                    switch (characteristicType) {
                        case 1:
                        case 5:
                        case 6:
                            // 1 - attribute / 5, 6 - error log sheet
                            isAttributeCharacteristic = true;
                            break;

                        default:
                            isAttributeCharacteristic = false;
                    }
                }
            }

            // if the information about characteristic type is not available,
            // then try to guess it from binary portion count
            if (isAttributeCharacteristic == null) {

                // attribute values has more than 10 portions
                isAttributeCharacteristic = dataPortions.length > 10;
            }

            String[] dataPortionKeys =
                    isAttributeCharacteristic
                            ? BINARY_ATTRIBUTE_VALUE_PORTIONS
                            : BINARY_VALUE_PORTIONS;

            for (int i = 0; i < dataPortions.length; i++) {
                String dataPortion = dataPortions[i];
                String key = dataPortionKeys[i];

                if (IGNORED_BINARY_KEY.equals(key)) {
                    continue;
                }

                KKey kKey = KKey.of(key);
                Object value;

                try {
                    value = convertValue(kKey, dataPortion, parserContext);

                } catch (UnknownKKeyException | ValueConversionException e) {
                    //TODO: 2016/04/11 - vlasta: we should provide information that parsed AqdefObjectModel doesn't contain all data?
                    value = null;
                }

                if (value != null) {
                    ValueIndex valueIndex = parserContext.getValueIndexCounter().getIndex(characteristicIndex, kKey);
                    aqdefObjectModel.putValueEntry(kKey, valueIndex, value);
                }
            }

            characteristicIntIndex++;
        }
    }
}
