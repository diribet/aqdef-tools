package cz.diribet.aqdef.parser.line;

import cz.diribet.aqdef.AqdefValidityException;
import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.model.*;
import cz.diribet.aqdef.parser.ParserContext;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KKeyLineParser extends AbstractLineParser {

    private static final Logger LOG = LoggerFactory.getLogger(KKeyLineParser.class);

    /**
     * These keys does not contain any information and we can safely ignore them.
     */
    private static final String[] IGNORED_KEYS = new String[] { "K0100", "K100", "K0101", "K101" };

    /**
     * These keys contain Q-DAS qs-STAT properietary internal configuration so we can safely ignore them.
     */
    private static final String[] PROPRIETARY_QDAS_KEYS = new String[] { "K1998", "K2998", "K2999", "K5098", "K5080" };

    @Override
    public boolean isLineSupported(@NonNull String line) {
        return line.startsWith("K");
    }

    @Override
    public void parseLine(@NonNull String line,
                          @NonNull AqdefObjectModel aqdefObjectModel,
                          @NonNull ParserContext parserContext) {

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

        int index = 1;
        Integer valueIndexNumber = null;

        if (hasIndex) {
            String indexString = line.substring(6, firstSpaceIndex);

            if (StringUtils.isNotBlank(indexString)) {
                int valueIndexSeparatorPosition = indexString.indexOf("/");

                if (valueIndexSeparatorPosition == -1) {
                    try {
                        index = Integer.parseInt(indexString);

                    } catch (NumberFormatException e) {
                        throw new AqdefValidityException("K-key index is invalid: " + indexString, e);
                    }
                } else {
                    if (kKey.isValueLevel()) {
                        try {
                            index = Integer.parseInt(indexString.substring(0, valueIndexSeparatorPosition));
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
            value = convertValue(kKey, valueString, parserContext);

        } catch (UnknownKKeyException | ValueConversionException e) {
            //TODO: 2016/04/11 - vlasta: we should provide information that parsed AqdefObjectModel doesn't contain all data?
            value = null;
        }

        if (value == null && !kKey.isValueLevel()) {
            return;
        }

        KKeyContext kKeyContext = KKeyContext.of(kKey, value, index, valueIndexNumber);

        if (kKey.isPartLevel()) {
            handlePartLevel(aqdefObjectModel, kKeyContext, parserContext);

        } else if (kKey.isCharacteristicLevel()) {
            handleCharacteristicLevel(aqdefObjectModel, kKeyContext, parserContext);

        } else if (kKey.isGroupLevel()) {
            handleGroupLevel(aqdefObjectModel, kKeyContext, parserContext);

        } else if (kKey.isValueLevel()) {
            handleValueLevel(aqdefObjectModel, kKeyContext, parserContext);

        } else if (kKey.isHierarchyLevel() || kKey.isSimpleHierarchyLevel()) {
            aqdefObjectModel.putHierarchyEntry(kKey, index, value);

        } else {
            if (isInvalidKKeyLoggingEnabled(kKey)) {
                LOG.warn("{} Unknown level of k-key {}. Key will be ignored! ",
                         ParserContext.lineLogContext(parserContext),
                         kKey);
            }
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

    private void handlePartLevel(AqdefObjectModel aqdefObjectModel,
                                 KKeyContext kKeyContext,
                                 ParserContext parserContext) {

        PartIndex partIndex = PartIndex.of(kKeyContext.getIndex());
        aqdefObjectModel.putPartEntry(kKeyContext.getKKey(), partIndex, kKeyContext.getValue());

        parserContext.setCurrentPartIndex(partIndex);
    }

    private void handleCharacteristicLevel(AqdefObjectModel aqdefObjectModel,
                                           KKeyContext kKeyContext,
                                           ParserContext parserContext) {

        PartIndex partIndex = getPartIndexForCharacteristicOrGroup(kKeyContext, parserContext);
        CharacteristicIndex characteristicIndex = CharacteristicIndex.of(partIndex, kKeyContext.getIndex());
        aqdefObjectModel.putCharacteristicEntry(kKeyContext.getKKey(), characteristicIndex, kKeyContext.getValue());
    }

    private void handleGroupLevel(AqdefObjectModel aqdefObjectModel,
                                  KKeyContext kKeyContext,
                                  ParserContext parserContext) {

        PartIndex partIndex = getPartIndexForCharacteristicOrGroup(kKeyContext, parserContext);
        GroupIndex groupIndex = GroupIndex.of(partIndex, kKeyContext.getIndex());
        aqdefObjectModel.putGroupEntry(kKeyContext.getKKey(), groupIndex, kKeyContext.getValue());
    }

    private PartIndex getPartIndexForCharacteristicOrGroup(KKeyContext kKeyContext,
                                                           ParserContext parserContext) {

        if (kKeyContext.getIndex() == 0) {
            return PartIndex.of(0);

        } else {
            PartIndex partIndex = parserContext.getCurrentPartIndex();

            if (partIndex == null || partIndex.getIndex() == null || partIndex.getIndex() == 0) {
                // no part k-key found before this characteristic/group - add it to the first part
                partIndex = PartIndex.of(1);
            }

            return partIndex;
        }
    }

    private void handleValueLevel(AqdefObjectModel aqdefObjectModel,
                                  KKeyContext kKeyContext,
                                  ParserContext parserContext) {

        KKey kKey = kKeyContext.getKKey();
        int index = kKeyContext.getIndex();
        Integer valueIndexNumber = kKeyContext.getValueIndexNumber();

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
            valueIndex = parserContext.getValueIndexCounter().getIndex(characteristicIndex, kKey);

        } else {
            valueIndex = ValueIndex.of(characteristicIndex, valueIndexNumber);
        }

        aqdefObjectModel.putValueEntry(kKey, valueIndex, kKeyContext.getValue());
    }

    @Data(staticConstructor = "of")
    private static class KKeyContext {

        private final KKey kKey;
        private final Object value;
        private final int index;
        private final Integer valueIndexNumber;
    }
}
