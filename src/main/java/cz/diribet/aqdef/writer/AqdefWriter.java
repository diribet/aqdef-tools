package cz.diribet.aqdef.writer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.Objects;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;

import cz.diribet.aqdef.AqdefConstants;
import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.convert.IKKeyValueConverter;
import cz.diribet.aqdef.model.AqdefHierarchy.HierarchyEntry;
import cz.diribet.aqdef.model.AqdefObjectModel;
import cz.diribet.aqdef.model.AqdefObjectModel.CharacteristicEntries;
import cz.diribet.aqdef.model.AqdefObjectModel.CharacteristicEntry;
import cz.diribet.aqdef.model.AqdefObjectModel.GroupEntries;
import cz.diribet.aqdef.model.AqdefObjectModel.GroupEntry;
import cz.diribet.aqdef.model.AqdefObjectModel.PartEntries;
import cz.diribet.aqdef.model.AqdefObjectModel.PartEntry;
import cz.diribet.aqdef.model.AqdefObjectModel.ValueEntries;
import cz.diribet.aqdef.model.AqdefObjectModel.ValueEntry;

/**
 * Writes {@link AqdefObjectModel} to AQDFQ text structure.
 * <p>
 * You can call {@link #writeTo(Writer)} to write DFQ content to a given writer
 * or {@link #getData()} to get DFQ content as a String. DFQ content is created
 * lazily when one of these methods is called.
 * </p>
 *
 * @author Vlastimil Dolejs
 *
 */
public class AqdefWriter implements AqdefConstants {
	//*******************************************
	// Methods
	//*******************************************

	/**
	 * Creates AQDFQ structure and returns it as a String
	 *
	 * @param aqdefObjectModel
	 *            model to be written, must not be {@code null}
	 * @return AQDFQ content as a String, never {@code null}
	 */
	public String writeToString(AqdefObjectModel aqdefObjectModel) {
		Objects.requireNonNull(aqdefObjectModel);

		StringBuilderWriter fileContent = new StringBuilderWriter();

		try {
			writeTo(aqdefObjectModel, fileContent);

		} catch (IOException e) {
			throw new RuntimeException("Failed to write AQDFQ model content", e);
		}

		return fileContent.toString();
	}

	/**
	 * Creates AQDEF structure and writes it to a given <code>writer</code>
	 *
	 * @param aqdefObjectModel
	 *            model to be written, must not be {@code null}
	 * @param writer
	 *            writer to write model to, must not be {@code null}
	 * @throws IOException
	 *             thrown when some I/O error occur
	 */
	public void writeTo(AqdefObjectModel aqdefObjectModel, Writer writer) throws IOException {
		Objects.requireNonNull(aqdefObjectModel);
		Objects.requireNonNull(writer);

		writeEntries(aqdefObjectModel, writer);
	}

	private void writeEntries(AqdefObjectModel aqdefObjectModel, Writer writer) throws IOException {
		aqdefObjectModel.normalize();

		try {
			// AQDEF structure always starts with the total number of characteristics
			write("K0100", null, Integer.toString(aqdefObjectModel.getCharacteristicCount()), writer);

			aqdefObjectModel.forEachPart(part -> {
				write(part, writer);

				aqdefObjectModel.forEachCharacteristic(part, (characteristic) -> {
					write(characteristic, writer);

					aqdefObjectModel.forEachValue(part, characteristic, (value) -> {
						write(value, writer);
					});
				});

				aqdefObjectModel.forEachGroup(part, (group) -> {
					write(group, writer);
				});
			});

			aqdefObjectModel.getHierarchy().forEachNodeDefinition(nodeDefinition -> {
				write(nodeDefinition, writer);
			});

			aqdefObjectModel.getHierarchy().forEachNodeBinding(nodeBinding -> {
				write(nodeBinding, writer);
			});

		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}

	private void write(PartEntries part, Writer writer) throws UncheckedIOException {
		part.values()
			.stream()
			.sorted(Comparator.comparing(PartEntry::getKey))
			.forEach(partEntry -> write(partEntry, writer));
	}

	private void write(PartEntry entry, Writer writer) throws UncheckedIOException {
		KKey kKey = entry.getKey();

		write(kKey.getKey(), entry.getIndex().getIndex(), convertValueOfKKey(kKey, entry.getValue()), writer);
	}

	private void write(CharacteristicEntries characteristic, Writer writer) throws UncheckedIOException {
		characteristic.values()
					  .stream()
					  .sorted(Comparator.comparing(CharacteristicEntry::getKey))
					  .forEach(characteristicEntry -> write(characteristicEntry, writer));
	}

	private void write(CharacteristicEntry entry, Writer writer) throws UncheckedIOException {
		KKey kKey = entry.getKey();

		write(kKey.getKey(), entry.getIndex().getCharacteristicIndex(), convertValueOfKKey(kKey, entry.getValue()), writer);
	}

	private void write(GroupEntries group, Writer writer) throws UncheckedIOException {
		group.values()
			 .stream()
			 .sorted(Comparator.comparing(GroupEntry::getKey))
			 .forEach(characteristicEntry -> write(characteristicEntry, writer));
	}

	private void write(GroupEntry entry, Writer writer) throws UncheckedIOException {
		KKey kKey = entry.getKey();

		write(kKey.getKey(), entry.getIndex().getGroupIndex(), convertValueOfKKey(kKey, entry.getValue()), writer);
	}

	private void write(ValueEntries value, Writer writer) throws UncheckedIOException {
		value.values()
			 .stream()
			 .sorted(Comparator.comparing(ValueEntry::getKey))
			 .forEach(valueEntry -> write(valueEntry, writer));
	}

	private void write(ValueEntry entry, Writer writer) throws UncheckedIOException {
		KKey kKey = entry.getKey();
		Integer characteristicIndex = entry.getIndex().getCharacteristicIndex().getCharacteristicIndex();
		String value = convertValueOfKKey(kKey, entry.getValue());

		write(kKey.getKey(), characteristicIndex, value, writer);
	}

	private void write(HierarchyEntry entry, Writer writer) throws UncheckedIOException {
		KKey kKey = entry.getKey();

		write(kKey.getKey(), entry.getIndex().getIndex(), convertValueOfKKey(kKey, entry.getValue()), writer);
	}

	private void write(String key, Integer index, String value, Writer writer) throws UncheckedIOException {
		try {
			writer.write(key);

			if (index != null) {
				writer.write("/");
				writer.write(index.toString());
			}

			writer.write(VALUES_SEPARATOR);
			writer.write(StringUtils.defaultString(value, StringUtils.EMPTY));
			writer.write(LINE_SEPARATOR);

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private String convertValueOfKKey(KKey kKey, Object value) {
		String result;

		try {
			IKKeyValueConverter<Object> converter = (IKKeyValueConverter<Object>) kKey.getConverter();
			if (converter == null) {
				throw new IllegalArgumentException("Can't find converter for k-key " + kKey);
			}

			result = converter.toString(value);

		} catch (Throwable e) {
			throw new RuntimeException("Failed to convert value (" + Objects.toString(value) + ") of k-key " + kKey + " to string", e);
		}

		if (result != null) {
			result = result.trim();
		}

		return result;
	}

}
