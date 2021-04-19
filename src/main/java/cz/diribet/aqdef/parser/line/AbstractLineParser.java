package cz.diribet.aqdef.parser.line;

import java.util.Set;

import cz.diribet.aqdef.AqdefConstants;
import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.KKeyMetadata;
import cz.diribet.aqdef.KKeyRepository;
import cz.diribet.aqdef.convert.IKKeyValueConverter;
import cz.diribet.aqdef.model.AqdefObjectModel;
import cz.diribet.aqdef.parser.ParserContext;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public abstract class AbstractLineParser implements AqdefConstants {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractLineParser.class);

    private final KKeyRepository kKeyRepository = KKeyRepository.getInstance();

    /**
     * Set to true if the invalid K-key logging should be suppressed for all K-keys.
     */
    private boolean suppressInvalidKKeyLogging = false;

    /**
     * Set K-keys for which the invalid K-key logging should be suppressed.
     */
    private Set<KKey> suppressInvalidKKeyLoggingFor;

    public abstract boolean isLineSupported(String line);
    public abstract void parseLine(String line, AqdefObjectModel aqdefObjectModel, ParserContext parserContext);

    protected Object convertValue(KKey key,
                                  String valueString,
                                  ParserContext parserContext) throws UnknownKKeyException, ValueConversionException {

        if (StringUtils.isBlank(valueString)) {
            return null;
        }

        KKeyMetadata kKeyMetadata = kKeyRepository.getMetadataFor(key);

        if (kKeyMetadata == null) {
            if (isInvalidKKeyLoggingEnabled(key)) {
                LOG.warn("{} Unknown k-key: {}. Value will be discarded.",
                         ParserContext.lineLogContext(parserContext),
                         key);
            }

            throw new UnknownKKeyException(key);
        }

        IKKeyValueConverter<?> converter = kKeyMetadata.getConverter();

        try {
            return converter.convert(valueString);

        } catch (Throwable e) {
            if (isInvalidKKeyLoggingEnabled(key)) {
                String message =
                        ParserContext.lineLogContext(parserContext) +
                        " Failed to convert value: " + valueString + " of K-key: " + key +
                        " using converter: " + converter + ". The value will be discarded.";

                LOG.warn(message, e);
            }

            throw new ValueConversionException(valueString, key, converter, e);
        }
    }

    protected boolean isInvalidKKeyLoggingEnabled(KKey kKey) {
        if (suppressInvalidKKeyLogging) {
            return false;
        }

        return suppressInvalidKKeyLoggingFor == null || !suppressInvalidKKeyLoggingFor.contains(kKey);
    }

    @RequiredArgsConstructor
    @Getter
    static class UnknownKKeyException extends Exception {
        private final KKey key;
    }

    @Getter
    static class ValueConversionException extends Exception {

        private final String value;
        private final KKey key;
        private final IKKeyValueConverter<?> converter;

        ValueConversionException(String value,
                                 KKey key,
                                 IKKeyValueConverter<?> converter,
                                 Throwable cause) {
            super(cause);

            this.value = value;
            this.key = key;
            this.converter = converter;
        }

    }
}
