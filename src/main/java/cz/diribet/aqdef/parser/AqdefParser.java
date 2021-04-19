package cz.diribet.aqdef.parser;

import cz.diribet.aqdef.AqdefConstants;
import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.model.AqdefObjectModel;
import cz.diribet.aqdef.parser.line.AbstractLineParser;
import cz.diribet.aqdef.parser.line.BinaryLineParser;
import cz.diribet.aqdef.parser.line.KKeyLineParser;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.Set;

/**
 * Parses {@link AqdefObjectModel} from a AQDEF content (file or other data source)
 *
 * @author Vlastimil Dolejs
 *
 */
public class AqdefParser implements AqdefConstants {

	private static final Logger LOG = LoggerFactory.getLogger(AqdefParser.class);

	private final AbstractLineParser kKeyLineParser = new KKeyLineParser();
	private final AbstractLineParser binaryLineParser = new BinaryLineParser();

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

		int lineIndex = 1;

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
		if (kKeyLineParser.isLineSupported(line)) {
			kKeyLineParser.parseLine(line, aqdefObjectModel, context);

		} else if (binaryLineParser.isLineSupported(line)) {
			binaryLineParser.parseLine(line, aqdefObjectModel, context);

		} else {
			LOG.warn("{} Invalid line format. This line will be discarded. Line content: {}",
					 ParserContext.lineLogContext(context),
					 line);
		}
	}

	private InputStream createFileInputStream(File file, String encoding) throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(file);

		if ("utf-8".equalsIgnoreCase(encoding)) {
			inputStream = new BOMInputStream(inputStream);
		}

		return inputStream;
	}

	/**
	 * Set to true if the invalid K-key logging should be suppressed for all K-keys.
	 */
	public void setSuppressInvalidKKeyLogging(boolean suppressInvalidKKeyLogging) {
		kKeyLineParser.setSuppressInvalidKKeyLogging(suppressInvalidKKeyLogging);
		binaryLineParser.setSuppressInvalidKKeyLogging(suppressInvalidKKeyLogging);
	}

	/**
	 * Set K-keys for which the invalid K-key logging should be suppressed.
	 */
	public void setSuppressInvalidKKeyLoggingFor(Set<KKey> suppressInvalidKKeyLoggingFor) {
		kKeyLineParser.setSuppressInvalidKKeyLoggingFor(suppressInvalidKKeyLoggingFor);
		binaryLineParser.setSuppressInvalidKKeyLoggingFor(suppressInvalidKKeyLoggingFor);
	}

	private static class DfqParserException extends RuntimeException {

		DfqParserException(ParserContext context, Throwable cause) {
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

}
