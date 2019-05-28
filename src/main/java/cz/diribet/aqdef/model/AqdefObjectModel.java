package cz.diribet.aqdef.model;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.commons.collections4.MapUtils;

import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.parser.AqdefParser;
import cz.diribet.aqdef.writer.AqdefWriter;

/**
 * Object model of AQDEF content.
 * <p>
 * Provides methods to
 * <ul>
 * <li>manipulate with the content ({@code getXXX}, {@code putXXX},
 * {@code removeXXX}, {@code filterXXX})</li>
 * <li>iterate through the content {@code forEachXXX}</li>
 * </ul>
 * </p>
 * <p>
 * Use {@link AqdefParser} to read AQDEF content and {@link AqdefWriter} to
 * write this object model as AQDEF content.
 * </p>
 *
 * @author Vlastimil Dolejs
 *
 * @see AqdefParser
 * @see AqdefWriter
 */
public class AqdefObjectModel {

	//*******************************************
	// Attributes
	//*******************************************

	private Map<PartIndex, PartEntries> partEntries = newEntriesMap();

	private Map<PartIndex, Map<CharacteristicIndex, CharacteristicEntries>> characteristicEntries = newEntriesMap();

	private Map<PartIndex, Map<GroupIndex, GroupEntries>> groupEntries = newEntriesMap();

	private Map<PartIndex, Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>>> valueEntries = newEntriesMap();

	private AqdefHierarchy hierarchy = new AqdefHierarchy();

	//*******************************************
	// Methods
	//*******************************************

	public void putPartEntry(KKey key, PartIndex index, Object value) {
		if (value == null) {
			return;
		}

		PartEntries entriesWithIndex = partEntries.computeIfAbsent(index, PartEntries::new);
		entriesWithIndex.put(key, value);
	}

	public void putPartEntries(PartEntries newPartEntries) {
		PartEntries entriesWithIndex = partEntries.computeIfAbsent(newPartEntries.getIndex(), PartEntries::new);
		entriesWithIndex.putAll(newPartEntries, true);
	}

	/**
	 * Removes part entries with the given index.
	 * Characteristic and value entries of given part are preserved!
	 *
	 * @param index
	 */
	private PartEntries removePartEntries(PartIndex index) {
		return partEntries.remove(index);
	}

	public void putCharacteristicEntry(KKey key, CharacteristicIndex characteristicIndex, Object value) {
		if (value == null) {
			return;
		}

		CharacteristicEntries entriesWithIndex = computeCharacteristicEntriesIfAbsent(characteristicIndex);
		entriesWithIndex.put(key, value);
	}

	public void putCharacteristicEntries(CharacteristicEntries newCharacteristicEntries) {
		CharacteristicIndex characteristicIndex = newCharacteristicEntries.getIndex();
		CharacteristicEntries entriesWithIndex = computeCharacteristicEntriesIfAbsent(characteristicIndex);
		entriesWithIndex.putAll(newCharacteristicEntries, true);
	}

	private CharacteristicEntries computeCharacteristicEntriesIfAbsent(CharacteristicIndex characteristicIndex) {
		PartIndex partIndex = characteristicIndex.getPartIndex();

		Map<CharacteristicIndex, CharacteristicEntries> entriesWithPartIndex =
				characteristicEntries.computeIfAbsent(partIndex, i -> newEntriesMap());

		return entriesWithPartIndex.computeIfAbsent(characteristicIndex, CharacteristicEntries::new);
	}

	/**
	 * Removes characteristic entries with the given index.
	 * Value entries of the given characteristic are preserved!
	 *
	 * @param index
	 * @return
	 */
	private CharacteristicEntries removeCharacteristicEntries(CharacteristicIndex index) {
		Map<CharacteristicIndex, CharacteristicEntries> entriesWithPartIndex = characteristicEntries.get(index.getPartIndex());

		if (entriesWithPartIndex != null) {
			CharacteristicEntries removedEntries = entriesWithPartIndex.remove(index);

			// cleanup empty entries
			if (entriesWithPartIndex.isEmpty()) {
				characteristicEntries.remove(index.getPartIndex());
			}

			return removedEntries;
		}

		return null;
	}

	public void putGroupEntry(KKey key, GroupIndex groupIndex, Object value) {
		if (value == null) {
			return;
		}

		PartIndex partIndex = groupIndex.getPartIndex();

		Map<GroupIndex, GroupEntries> entriesWithPartIndex =
				groupEntries.computeIfAbsent(partIndex, i -> newEntriesMap());

		GroupEntries entriesWithIndex =
				entriesWithPartIndex.computeIfAbsent(groupIndex, GroupEntries::new);
		entriesWithIndex.put(key, value);
	}

	public void putValueEntry(KKey key, ValueIndex valueIndex, Object value) {
		if (value == null) {
			return;
		}

		ValueEntries entriesWithIndex = computeValueEntriesIfAbsent(valueIndex);
		entriesWithIndex.put(key, value);
	}

	public void putValueEntries(ValueEntries newValueEntries) {
		ValueIndex valueIndex = newValueEntries.getIndex();
		ValueEntries entriesWithIndex = computeValueEntriesIfAbsent(valueIndex);
		entriesWithIndex.putAll(newValueEntries, true);
	}

	private ValueEntries computeValueEntriesIfAbsent(ValueIndex valueIndex) {
		PartIndex partIndex = valueIndex.getPartIndex();

		Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> entriesWithPartIndex =
				valueEntries.computeIfAbsent(partIndex, i -> newEntriesMap());

		CharacteristicIndex characteristicIndex = valueIndex.getCharacteristicIndex();

		Map<ValueIndex, ValueEntries> entriesWithCharacteristicIndex =
				entriesWithPartIndex.computeIfAbsent(characteristicIndex, i -> newEntriesMap());

		return entriesWithCharacteristicIndex.computeIfAbsent(valueIndex, ValueEntries::new);
	}

	/**
	 * Removes all values of characteristic with the given index.
	 *
	 * @param index
	 * @return
	 */
	private List<ValueEntries> removeValueEntries(CharacteristicIndex index) {
		Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> entriesWithPartIndex = valueEntries.get(index.getPartIndex());

		if (entriesWithPartIndex != null) {
			Map<ValueIndex, ValueEntries> removedValueEntries = entriesWithPartIndex.remove(index);

			// cleanup empty entries
			if (entriesWithPartIndex.isEmpty()) {
				valueEntries.remove(index.getPartIndex());
			}

			if (removedValueEntries != null) {
				return new ArrayList<>(removedValueEntries.values());
			}
		}

		return new ArrayList<>();
	}

	public void putHierarchyEntry(KKey kKey, Integer nodeIndex, Object value) {
		hierarchy.putEntry(kKey, nodeIndex, value);
	}

	/**
	 * Returns indexes of all parts in this object model.
	 *
	 * @return
	 */
	public List<PartIndex> getPartIndexes() {
		return new ArrayList<>(partEntries.keySet());
	}

	public PartEntries getPartEntries(int index) {
		return getPartEntries(PartIndex.of(index));
	}

	public PartEntries getPartEntries(PartIndex index) {
		return partEntries.get(index);
	}

	/**
	 * @return all parts in this object model
	 */
	public List<PartEntries> getParts() {
		return new ArrayList<>(partEntries.values());
	}

	/**
	 * Returns indexes of all characteristics of a part with the given index.
	 *
	 * @return
	 */
	public List<CharacteristicIndex> getCharacteristicIndexes(PartIndex partIndex) {
		Map<CharacteristicIndex, CharacteristicEntries> entriesWithPartIndex = characteristicEntries.get(partIndex);

		if (entriesWithPartIndex == null) {
			return new ArrayList<>();
		} else {
			return new ArrayList<>(entriesWithPartIndex.keySet());
		}
	}

	public CharacteristicEntries getCharacteristicEntries(int partIndex, int characteristicIndex) {
		return getCharacteristicEntries(CharacteristicIndex.of(PartIndex.of(partIndex), characteristicIndex));
	}

	public CharacteristicEntries getCharacteristicEntries(CharacteristicIndex characteristicIndex) {
		Map<CharacteristicIndex, CharacteristicEntries> entriesWithPartIndex = characteristicEntries.get(characteristicIndex.getPartIndex());

		if (entriesWithPartIndex == null) {
			return null;
		} else {
			return entriesWithPartIndex.get(characteristicIndex);
		}
	}

	/**
	 * Returns all the characteristics of a part with the given index.
	 *
	 * @param partIndex
	 * @return
	 */
	public List<CharacteristicEntries> getCharacteristics(PartIndex partIndex) {
		Map<CharacteristicIndex, CharacteristicEntries> entriesWithPartIndex = characteristicEntries.get(partIndex);

		if (entriesWithPartIndex == null) {
			return new ArrayList<>();
		} else {
			return new ArrayList<>(entriesWithPartIndex.values());
		}
	}

	/**
	 * Returns all the groups of a part with the given index.
	 *
	 * @param partIndex
	 * @return
	 */
	public List<GroupEntries> getGroups(PartIndex partIndex) {
		Map<GroupIndex, GroupEntries> entriesWithPartIndex = groupEntries.get(partIndex);

		if (entriesWithPartIndex == null) {
			return new ArrayList<>();
		} else {
			return new ArrayList<>(entriesWithPartIndex.values());
		}
	}

	/**
	 * Returns indexes of all values of a characteristics with the given index.
	 *
	 * @return
	 */
	public List<ValueIndex> getValueIndexes(CharacteristicIndex characteristicIndex) {
		Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> entriesWithPartIndex = valueEntries.get(characteristicIndex.getPartIndex());

		if (entriesWithPartIndex == null) {
			return new ArrayList<>();
		} else {
			Map<ValueIndex, ValueEntries> entriesWithCharacteristicIndex = entriesWithPartIndex.get(characteristicIndex);

			if (entriesWithCharacteristicIndex == null) {
				return new ArrayList<>();
			} else {
				return new ArrayList<>(entriesWithCharacteristicIndex.keySet());
			}
		}
	}

	/**
	 * Returns indexes of all values in this object model.
	 * @return
	 */
	public List<ValueIndex> getValueIndexes() {
		return valueEntries.values()
						   .stream()
						   .flatMap(e -> e.values().stream())
						   .flatMap(e -> e.keySet().stream())
						   .collect(toList());
	}

	public ValueEntries getValueEntries(int partIndex, int characteristicIndex, int valueIndex) {
		return getValueEntries(ValueIndex.of(CharacteristicIndex.of(PartIndex.of(partIndex), characteristicIndex), valueIndex));
	}

	public ValueEntries getValueEntries(ValueIndex valueIndex) {
		Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> entriesWithPartIndex = valueEntries.get(valueIndex.getPartIndex());

		if (entriesWithPartIndex == null) {
			return null;
		} else {
			Map<ValueIndex, ValueEntries> entriesWithCharacteristicIndex = entriesWithPartIndex.get(valueIndex.getCharacteristicIndex());

			if (entriesWithCharacteristicIndex == null) {
				return null;
			} else {
				return entriesWithCharacteristicIndex.get(valueIndex);
			}
		}
	}

	/**
	 * @return all the values in this model object
	 */
	public List<ValueEntries> getValues() {
		return valueEntries.values()
						   .stream()
						   .flatMap(e -> e.values().stream())
						   .flatMap(e -> e.values().stream())
						   .collect(toList());
	}

	/**
	 *
	 * @param characteristicIndex
	 * @return
	 * @deprecated use {@link #getValues(CharacteristicIndex)} instead
	 */
	@Deprecated
	public List<ValueEntries> getValueEntries(CharacteristicIndex characteristicIndex) {
		return getValues(characteristicIndex);
	}

	public List<ValueEntries> getValues(CharacteristicIndex characteristicIndex) {
		Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> entriesWithPartIndex = valueEntries.get(characteristicIndex.getPartIndex());

		if (entriesWithPartIndex == null) {
			return new ArrayList<>();
		} else {
			Map<ValueIndex, ValueEntries> entriesWithCharacteristicIndex = entriesWithPartIndex.get(characteristicIndex);

			if (entriesWithCharacteristicIndex == null) {
				return new ArrayList<>();
			} else {
				return new ArrayList<>(entriesWithCharacteristicIndex.values());
			}
		}
	}


	public List<ValueSet> getValueSets(PartIndex partIndex) {
		Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> entriesWithPartIndex = valueEntries.get(partIndex);

		List<ValueSet> valueSets = new ArrayList<>();

		for (Entry<CharacteristicIndex, Map<ValueIndex, ValueEntries>> entriesOfCharacteristic : entriesWithPartIndex.entrySet()) {
			CharacteristicIndex characteristicIndex = entriesOfCharacteristic.getKey();
			Map<ValueIndex, ValueEntries> valuesOfCharacteristic = entriesOfCharacteristic.getValue();

			int counter = 0;
			for (Entry<ValueIndex, ValueEntries> valueEntries : valuesOfCharacteristic.entrySet()) {
				while (counter >= valueSets.size()) {
					valueSets.add(new ValueSet());
				}
				ValueSet valueSet = valueSets.get(counter);
				valueSet.addValueOfCharacteristic(characteristicIndex, valueEntries.getValue());
				counter++;
			}
		}

		return valueSets;
	}

	/**
	 * Finds part index to which the given characteristic index belongs.
	 * <p>
	 * You should call this method only after this {@link AqdefObjectModel} is fully created.
	 * </p>
	 *
	 * @param characteristicIndex
	 * @return
	 */
	public PartIndex findPartIndexForCharacteristic(int characteristicIndex) {
		for (Map<CharacteristicIndex, CharacteristicEntries> characteristics : characteristicEntries.values()) {
			for (CharacteristicIndex characteristic : characteristics.keySet()) {
				if (characteristic.getCharacteristicIndex() == characteristicIndex) {
					return characteristic.getPartIndex();
				}
			}
		}

		return null;
	}

	public Set<CharacteristicIndex> findCharacteristicIndexesForPart(PartIndex partIndex, CharacteristicOfSinglePartPredicate predicate) {
		Set<CharacteristicIndex> characteristicIndexes = new HashSet<>();

		Map<CharacteristicIndex, CharacteristicEntries> partCharacteristics = characteristicEntries.get(partIndex);

		if (partCharacteristics != null) {
			for (CharacteristicEntries entries : partCharacteristics.values()) {
				if (predicate.test(entries)) {
					characteristicIndexes.add(entries.getIndex());
				}
			}
		}

		return characteristicIndexes;
	}

	/**
	 * Iterates through all parts
	 *
	 * @param consumer
	 */
	public void forEachPart(PartConsumer consumer) {
		partEntries.forEach((partIndex, part) -> {
			consumer.accept(part);
		});
	}

	/**
	 * Iterates through all characteristics of all parts
	 *
	 * @param consumer
	 */
	public void forEachCharacteristic(CharacteristicConsumer consumer) {
		partEntries.forEach((partIndex, part) -> {
			Map<CharacteristicIndex, CharacteristicEntries> characteristicsOfPart = characteristicEntries.get(part.getIndex());

			if (characteristicsOfPart != null) {
				characteristicsOfPart.forEach((characteristicIndex, characteristic) -> {
					consumer.accept(part, characteristic);
				});
			}
		});
	}

	/**
	 * Iterates through all characteristics of the given part. Most of the time it will be used together with {@link #forEachPart(PartConsumer)}.
	 * <pre>
	 * model.forEachPart(part -> {
	 *     // do something with the part
	 *
	 *     model.forEachCharacteristic(part, characteristic -> {
	 *         // do something with the characteristic
	 *     });
	 * })
	 * </pre>
	 * @param part
	 * @param consumer
	 */
	public void forEachCharacteristic(PartEntries part, CharacteristicOfSinglePartConsumer consumer) {
		Map<CharacteristicIndex, CharacteristicEntries> characteristicsOfPart = characteristicEntries.get(part.getIndex());

		if (characteristicsOfPart != null) {
			characteristicsOfPart.forEach((characteristicIndex, characteristic) -> {
				consumer.accept(characteristic);
			});
		}
	}

	/**
	 * Iterates through all logical groups of all parts
	 *
	 * @param consumer
	 */
	public void forEachGroup(GroupConsumer consumer) {
		partEntries.forEach((partIndex, part) -> {
			Map<GroupIndex, GroupEntries> groupsOfPart = groupEntries.get(part.getIndex());

			if (groupsOfPart != null) {
				groupsOfPart.forEach((groupIndex, group) -> {
					consumer.accept(part, group);
				});
			}
		});
	}

	/**
	 * Iterates through all logical groups of the given part. Similar to {@link #forEachCharacteristic(PartEntries, CharacteristicOfSinglePartConsumer)}
	 *
	 * @param part
	 * @param consumer
	 */
	public void forEachGroup(PartEntries part, GroupOfSinglePartConsumer consumer) {
		Map<GroupIndex, GroupEntries> groupsOfPart = groupEntries.get(part.getIndex());

		if (groupsOfPart != null) {
			groupsOfPart.forEach((groupIndex, group) -> {
				consumer.accept(group);
			});
		}
	}

	/**
	 * Iterates through all values.
	 *
	 * @param consumer
	 */
	public void forEachValue(ValueConsumer consumer) {
		partEntries.forEach((partIndex, part) -> {
			Map<CharacteristicIndex, CharacteristicEntries> characteristicsOfPart = characteristicEntries.get(part.getIndex());

			if (characteristicsOfPart != null) {
				characteristicsOfPart.forEach((characteristicIndex, characteristic) -> {
					Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> valuesOfPart = valueEntries.get(part.getIndex());

					if (valuesOfPart != null) {
						Map<ValueIndex, ValueEntries> values = valuesOfPart.get(characteristic.getIndex());

						if (values != null) {
							values.forEach((valueIndex, value) -> {
								consumer.accept(part, characteristic, value);
							});
						}
					}
				});
			}
		});
	}

	/**
	 * Iterates through all values of the given part. Most of the time it will be used together with {@link #forEachPart(PartConsumer)}.
	 * <pre>
	 * model.forEachPart(part -> {
	 *     // do something with the part
	 *
	 *     model.forEachValue(part, (characteristic, value) -> {
	 *         // do something with the value
	 *     });
	 * })
	 * </pre>
	 *
	 * @param part
	 * @param consumer
	 */
	public void forEachValue(PartEntries part, ValueOfSinglePartConsumer consumer) {
		Map<CharacteristicIndex, CharacteristicEntries> characteristicsOfPart = characteristicEntries.get(part.getIndex());

		if (characteristicsOfPart != null) {
			characteristicsOfPart.forEach((characteristicIndex, characteristic) -> {
				Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> valuesOfPart = valueEntries.get(part.getIndex());

				if (valuesOfPart != null) {
					Map<ValueIndex, ValueEntries> values = valuesOfPart.get(characteristic.getIndex());

					if (values != null) {
						values.forEach((valueIndex, value) -> {
							consumer.accept(characteristic, value);
						});
					}
				}
			});
		}
	}

	/**
	 * Iterates through all values of the given characteristic. Most of the time it will be used together with {@link #forEachCharacteristic(CharacteristicConsumer)}.
	 * <pre>
	 * model.forEachCharacteristic(part, characteristic -> {
	 *     // do something with the characteristic
	 *
	 *     model.forEachValue(part, characteristic, value -> {
	 *         // do something with the value
	 *     });
	 * })
	 * </pre>
	 *
	 * @param part
	 * @param characteristic
	 * @param consumer
	 */
	public void forEachValue(PartEntries part, CharacteristicEntries characteristic, ValueOfSingleCharacteristicConsumer consumer) {
		Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> valuesOfPart = valueEntries.get(part.getIndex());

		if (valuesOfPart != null) {
			Map<ValueIndex, ValueEntries> values = valuesOfPart.get(characteristic.getIndex());

			if (values != null) {
				values.forEach((valueIndex, value) -> {
					consumer.accept(value);
				});
			}
		}
	}

	/**
	 * Removes all parts that do not match the given predicate.
	 * If a part is removed it's characteristics and values are also removed.
	 *
	 * @param predicate
	 */
	public void filterParts(PartPredicate predicate) {
		Iterator<Map.Entry<PartIndex, PartEntries>> iterator = partEntries.entrySet().iterator();
		while (iterator.hasNext()) {

			Entry<PartIndex, PartEntries> entry = iterator.next();
			PartIndex partIndex = entry.getKey();
			PartEntries part = entry.getValue();

			if (!predicate.test(part)) {
				iterator.remove();

				// remove characteristics and values for that part
				characteristicEntries.remove(partIndex);
				valueEntries.remove(partIndex);
			}
		}
	}

	/**
	 * Removes all characteristics that do not match the given predicate.
	 * If a characteristic is removed it's values are also removed.
	 *
	 * @param predicate
	 */
	public void filterCharacteristics(CharacteristicPredicate predicate) {
		partEntries.forEach((partIndex, part) -> {
			Map<CharacteristicIndex, CharacteristicEntries> characteristicsOfPart = characteristicEntries.get(part.getIndex());

			if (characteristicsOfPart != null) {
				Iterator<Entry<CharacteristicIndex, CharacteristicEntries>> iterator = characteristicsOfPart.entrySet().iterator();
				while (iterator.hasNext()) {

					Entry<CharacteristicIndex, CharacteristicEntries> entry = iterator.next();
					CharacteristicIndex characteristicIndex = entry.getKey();
					CharacteristicEntries characteristic = entry.getValue();

					if (!predicate.test(part, characteristic)) {
						iterator.remove();

						// remove values for that characteristic
						removeValueEntries(characteristicIndex);
					}
				}
			}
		});
	}

	/**
	 * Removes all characteristics of the given part that do not match the given predicate.
	 * If a characteristic is removed it's values are also removed.
	 *
	 * @param part
	 * @param predicate
	 */
	public void filterCharacteristics(PartEntries part, CharacteristicOfSinglePartPredicate predicate) {
		Map<CharacteristicIndex, CharacteristicEntries> characteristicsOfPart = characteristicEntries.get(part.getIndex());

		if (characteristicsOfPart != null) {
			Iterator<Entry<CharacteristicIndex, CharacteristicEntries>> iterator = characteristicsOfPart.entrySet().iterator();
			while (iterator.hasNext()) {

				Entry<CharacteristicIndex, CharacteristicEntries> entry = iterator.next();
				CharacteristicIndex characteristicIndex = entry.getKey();
				CharacteristicEntries characteristic = entry.getValue();

				if (!predicate.test(characteristic)) {
					iterator.remove();

					// remove values for that characteristic
					removeValueEntries(characteristicIndex);
				}
			}
		}
	}

	/**
	 * Removes all groups that do not match the given predicate.
	 *
	 * @param predicate
	 */
	public void filterGroups(GroupPredicate predicate) {
		partEntries.forEach((partIndex, part) -> {
			Map<GroupIndex, GroupEntries> groupsOfPart = groupEntries.get(part.getIndex());

			if (groupsOfPart != null) {
				Iterator<Entry<GroupIndex, GroupEntries>> iterator = groupsOfPart.entrySet().iterator();
				while (iterator.hasNext()) {
					GroupEntries group = iterator.next().getValue();

					if (!predicate.test(part, group)) {
						iterator.remove();
					}
				}
			}
		});
	}

	/**
	 * Removes all groups of the given part that do not match the given predicate.
	 *
	 * @param part
	 * @param predicate
	 */
	public void filterGroups(PartEntries part, GroupOfSinglePartPredicate predicate) {
		Map<GroupIndex, GroupEntries> groupsOfPart = groupEntries.get(part.getIndex());

		if (groupsOfPart != null) {
			Iterator<Entry<GroupIndex, GroupEntries>> iterator = groupsOfPart.entrySet().iterator();
			while (iterator.hasNext()) {
				GroupEntries group = iterator.next().getValue();

				if (!predicate.test(group)) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Removes all values that do not match the given predicate.
	 *
	 * @param predicate
	 */
	public void filterValues(ValuePredicate predicate) {
		partEntries.forEach((partIndex, part) -> {
			Map<CharacteristicIndex, CharacteristicEntries> characteristicsOfPart = characteristicEntries.get(part.getIndex());

			if (characteristicsOfPart != null) {
				characteristicsOfPart.forEach((characteristicIndex, characteristic) -> {
					Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> valuesOfPart = valueEntries.get(part.getIndex());

					if (valuesOfPart != null) {
						Map<ValueIndex, ValueEntries> values = valuesOfPart.get(characteristic.getIndex());

						if (values != null) {
							Iterator<Entry<ValueIndex, ValueEntries>> iterator = values.entrySet().iterator();
							while (iterator.hasNext()) {

								Entry<ValueIndex, ValueEntries> entry = iterator.next();
								ValueEntries value = entry.getValue();

								if (!predicate.test(part, characteristic, value)) {
									iterator.remove();
								}
							}
						}
					}
				});
			}
		});
	}

	/**
	 * Removes all values of the given part that do not match the given predicate.
	 *
	 * @param part
	 * @param predicate
	 */
	public void filterValues(PartEntries part, ValueOfSinglePartPredicate predicate) {
		Map<CharacteristicIndex, CharacteristicEntries> characteristicsOfPart = characteristicEntries.get(part.getIndex());

		if (characteristicsOfPart != null) {
			characteristicsOfPart.forEach((characteristicIndex, characteristic) -> {
				Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> valuesOfPart = valueEntries.get(part.getIndex());

				if (valuesOfPart != null) {
					Map<ValueIndex, ValueEntries> values = valuesOfPart.get(characteristic.getIndex());

					if (values != null) {
						Iterator<Entry<ValueIndex, ValueEntries>> iterator = values.entrySet().iterator();
						while (iterator.hasNext()) {

							Entry<ValueIndex, ValueEntries> entry = iterator.next();
							ValueEntries value = entry.getValue();

							if (!predicate.test(characteristic, value)) {
								iterator.remove();
							}
						}
					}
				}
			});
		}
	}

	/**
	 * Removes all values of the given characteristic that do not match the given predicate.
	 *
	 * @param part
	 * @param characteristic
	 * @param predicate
	 */
	public void filterValues(PartEntries part, CharacteristicEntries characteristic, ValueOfSingleCharacteristicPredicate predicate) {
		Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> valuesOfPart = valueEntries.get(part.getIndex());

		if (valuesOfPart != null) {
			Map<ValueIndex, ValueEntries> values = valuesOfPart.get(characteristic.getIndex());

			if (values != null) {
				Iterator<Entry<ValueIndex, ValueEntries>> iterator = values.entrySet().iterator();
				while (iterator.hasNext()) {

					Entry<ValueIndex, ValueEntries> entry = iterator.next();
					ValueEntries value = entry.getValue();

					if (!predicate.test(value)) {
						iterator.remove();
					}
				}
			}
		}
	}

	public boolean containsPart(PartEntries part) {
		Objects.requireNonNull(part);
		return partEntries.containsKey(part.getIndex());
	}

	public boolean containsCharacteristic(CharacteristicEntries characteristic) {
		Objects.requireNonNull(characteristic);

		CharacteristicIndex characteristicIndex = characteristic.getIndex();
		Map<CharacteristicIndex, CharacteristicEntries> characteristicsOfPart =
				characteristicEntries.get(characteristicIndex.getPartIndex());

		if (characteristicsOfPart == null) {
			return false;
		} else {
			return characteristicsOfPart.containsKey(characteristicIndex);
		}
	}

	public boolean containsValue(ValueEntries value) {
		Objects.requireNonNull(value);

		ValueIndex valueIndex = value.getIndex();

		Map<CharacteristicIndex, Map<ValueIndex, ValueEntries>> valuesOfPart = valueEntries.get(valueIndex.getPartIndex());

		if (valuesOfPart == null) {
			return false;
		} else {
			Map<ValueIndex, ValueEntries> valuesOfCharacteristic =
					valuesOfPart.get(valueIndex.getCharacteristicIndex());

			if (valuesOfCharacteristic == null) {
				return false;
			} else {
				return valuesOfCharacteristic.containsKey(valueIndex);
			}
		}
	}

	/**
	 * Finds value of the given K-key from any part / characteristic / value.
	 * <p>
	 * There is no guarantee from which part / characteristic / value this K-key value will be taken (whether from the first or other).
	 * </p>
	 *
	 * @param key any K-key of part / characteristic / value
	 * @return
	 * @throws IllegalArgumentException if the K-key is not for part / characteristic / value lavel.
	 */
	public Object getAnyValueOf(KKey key) {
		if (key.isPartLevel()) {

			return partEntries.values().stream().findAny().map((entries) -> entries.getValue(key)).orElse(null);

		} else if (key.isCharacteristicLevel()) {

			return characteristicEntries.values().stream().findAny().map((entriesOfPart) -> {
				return entriesOfPart.values().stream().findAny().map((entries) -> entries.getValue(key)).orElse(null);
			}).orElse(null);

		} else if (key.isGroupLevel()) {

			return groupEntries.values().stream().findAny().map((entriesOfPart) -> {
				return entriesOfPart.values().stream().findAny().map((entries) -> entries.getValue(key)).orElse(null);
			}).orElse(null);

		} else if (key.isValueLevel()) {

			return valueEntries.values().stream().findAny().map((entriesOfPart) -> {
				return entriesOfPart.values().stream().findAny().map((entriesOfCharacteristic) -> {
					return entriesOfCharacteristic.values().stream().findAny().map((entries) -> entries.getValue(key)).orElse(null);
				}).orElse(null);
			}).orElse(null);

		} else {
			throw new IllegalArgumentException(String.format("Invalid k-key %s. Value can be obtained only for part / characteristic / value keys.", key));
		}
	}

	/**
	 * Normalize the AQDEF content.
	 * <ul>
	 * <li>Apply all /0 K-keys on all parts / characteristics / values and then
	 * remove them from object model.</li>
	 * <li>Complement the hierarchy so there are no nodes/characteristics wihout a
	 * parent part node. This may happen when hierarchy was created from simple
	 * characteristics grouping (K2030/K2031).</li>
	 * </ul>
	 */
	public void normalize() {
		// normalize part entries
		PartEntries entriesForAllParts = removePartEntries(PartIndex.of(0));

		if (MapUtils.isNotEmpty(entriesForAllParts)) {
			if (getPartIndexes().isEmpty()) {

				// if there are no part entries then /0 entries become /1 entries
				PartIndex partIndex = PartIndex.of(1);
				partEntries.put(partIndex, entriesForAllParts.withIndex(partIndex));

			} else {

				// put /0 entries to all existing parts
				forEachPart((part) -> {
					part.putAll(entriesForAllParts.withIndex(part.getIndex()), false);
				});

			}
		}

		// normalize characteristic and value entries
		CharacteristicIndex indexForAllCharacteristicsOfAllParts = CharacteristicIndex.of(PartIndex.of(0), 0);
		CharacteristicEntries entriesForAllCharacteristicsOfAllParts = new CharacteristicEntries(indexForAllCharacteristicsOfAllParts);

		CharacteristicEntries characteristicEntriesForAllCharacteristicsOfAllParts =
				removeCharacteristicEntries(indexForAllCharacteristicsOfAllParts);
		if (MapUtils.isNotEmpty(characteristicEntriesForAllCharacteristicsOfAllParts)) {
			entriesForAllCharacteristicsOfAllParts.putAll(characteristicEntriesForAllCharacteristicsOfAllParts, false);
		}

		List<ValueEntries> entriesForAllValuesOfAllParts = removeValueEntries(indexForAllCharacteristicsOfAllParts);
		Map<Integer, List<ValueEntries>> entriesForAllValuesByValueIndex =
				entriesForAllValuesOfAllParts
											.stream()
											.collect(groupingBy(e -> e.getIndex().getValueIndex()));

		forEachPart((part) -> {
			CharacteristicIndex indexForAllCharacteristics = CharacteristicIndex.of(part.getIndex(), 0);

			CharacteristicEntries entriesForAllCharacteristics = removeCharacteristicEntries(indexForAllCharacteristics);
			if (MapUtils.isNotEmpty(entriesForAllCharacteristics)) {
				entriesForAllCharacteristicsOfAllParts.putAll(entriesForAllCharacteristics, false);
			}
		});

		forEachCharacteristic((part, characteristic) -> {
			characteristic.putAll(entriesForAllCharacteristicsOfAllParts.withIndex(characteristic.getIndex()), false);

			forEachValue(part, characteristic, (value) -> {
				// value entries for all values are applied to single value set for all characteristic values
				List<ValueEntries> entriesForValueIndex = entriesForAllValuesByValueIndex.get(value.getIndex().getValueIndex());

				if (entriesForValueIndex != null) {
					for (ValueEntries valueEntries : entriesForValueIndex) {
						value.putAll(valueEntries.withIndex(value.getIndex()), false);
					}
				}
			});
		});

		hierarchy = hierarchy.normalize(this);
	}

	/**
	 * Returns total number of characteristics of all parts in this object model
	 *
	 * @return
	 */
	public int getCharacteristicCount() {
		AtomicInteger characteristicCount = new AtomicInteger();

		forEachCharacteristic((part, characteristic) -> characteristicCount.incrementAndGet());

		return characteristicCount.get();
	}

	/**
	 * Returns total number of values of all parts and characteristics in this object model
	 *
	 * @return
	 */
	public int getValueCount() {
		AtomicInteger count = new AtomicInteger();

		forEachValue((part, characteristic, value) -> count.incrementAndGet());

		return count.get();
	}

	private <K, V> Map<K, V> newEntriesMap() {
		return new ConcurrentSkipListMap<>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((characteristicEntries == null) ? 0 : characteristicEntries.hashCode());
		result = prime * result + ((groupEntries == null) ? 0 : groupEntries.hashCode());
		result = prime * result + ((hierarchy == null) ? 0 : hierarchy.hashCode());
		result = prime * result + ((partEntries == null) ? 0 : partEntries.hashCode());
		result = prime * result + ((valueEntries == null) ? 0 : valueEntries.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AqdefObjectModel)) {
			return false;
		}
		AqdefObjectModel other = (AqdefObjectModel) obj;
		if (characteristicEntries == null) {
			if (other.characteristicEntries != null) {
				return false;
			}
		} else if (!characteristicEntries.equals(other.characteristicEntries)) {
			return false;
		}
		if (groupEntries == null) {
			if (other.groupEntries != null) {
				return false;
			}
		} else if (!groupEntries.equals(other.groupEntries)) {
			return false;
		}
		if (hierarchy == null) {
			if (other.hierarchy != null) {
				return false;
			}
		} else if (!hierarchy.equals(other.hierarchy)) {
			return false;
		}
		if (partEntries == null) {
			if (other.partEntries != null) {
				return false;
			}
		} else if (!partEntries.equals(other.partEntries)) {
			return false;
		}
		if (valueEntries == null) {
			if (other.valueEntries != null) {
				return false;
			}
		} else if (!valueEntries.equals(other.valueEntries)) {
			return false;
		}
		return true;
	}

	//*******************************************
	// Getters / setters
	//*******************************************

	public AqdefHierarchy getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(AqdefHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	//*******************************************
	// Inner classes
	//*******************************************

	@FunctionalInterface
	public static interface PartConsumer {
		void accept(PartEntries part);
	}

	@FunctionalInterface
	public static interface CharacteristicConsumer {
		void accept(PartEntries part, CharacteristicEntries characteristic);
	}

	@FunctionalInterface
	public static interface CharacteristicOfSinglePartConsumer {
		void accept(CharacteristicEntries characteristic);
	}

	@FunctionalInterface
	public static interface GroupConsumer {
		void accept(PartEntries part, GroupEntries characteristic);
	}

	@FunctionalInterface
	public static interface GroupOfSinglePartConsumer {
		void accept(GroupEntries characteristic);
	}

	@FunctionalInterface
	public static interface ValueConsumer {
		void accept(PartEntries part, CharacteristicEntries characteristic, ValueEntries value);
	}

	@FunctionalInterface
	public static interface ValueOfSinglePartConsumer {
		void accept(CharacteristicEntries characteristic, ValueEntries value);
	}

	@FunctionalInterface
	public static interface ValueOfSingleCharacteristicConsumer {
		void accept(ValueEntries value);
	}

	@FunctionalInterface
	public static interface PartPredicate {
		boolean test(PartEntries part);
	}

	@FunctionalInterface
	public static interface CharacteristicPredicate {
		boolean test(PartEntries part, CharacteristicEntries characteristic);
	}

	@FunctionalInterface
	public static interface CharacteristicOfSinglePartPredicate {
		boolean test(CharacteristicEntries characteristic);
	}

	@FunctionalInterface
	public static interface GroupPredicate {
		boolean test(PartEntries part, GroupEntries group);
	}

	@FunctionalInterface
	public static interface GroupOfSinglePartPredicate {
		boolean test(GroupEntries group);
	}

	@FunctionalInterface
	public static interface ValuePredicate {
		boolean test(PartEntries part, CharacteristicEntries characteristic, ValueEntries value);
	}

	@FunctionalInterface
	public static interface ValueOfSinglePartPredicate {
		boolean test(CharacteristicEntries characteristic, ValueEntries value);
	}

	@FunctionalInterface
	public static interface ValueOfSingleCharacteristicPredicate {
		boolean test(ValueEntries value);
	}

	public static class PartEntries extends Entries<PartEntry, PartIndex> {

		public PartEntries(PartIndex index) {
			super(index);
		}

		@Override
		public PartEntries withIndex(PartIndex index) {
			PartEntries copy = new PartEntries(index);

			List<PartEntry> entriesCopy = values()
												.stream()
												.map(e -> new PartEntry(e.getKey(), index, e.getValue()))
												.collect(toList());
			copy.putAll(entriesCopy, true);
			return copy;
		}

		@Override
		protected PartEntry newEntry(KKey key, PartIndex index, Object value) {
			return new PartEntry(key, index, value);
		}

	}

	public static class CharacteristicEntries extends Entries<CharacteristicEntry, CharacteristicIndex> {

		public CharacteristicEntries(CharacteristicIndex index) {
			super(index);
		}

		@Override
		public CharacteristicEntries withIndex(CharacteristicIndex index) {
			CharacteristicEntries copy = new CharacteristicEntries(index);

			List<CharacteristicEntry> entriesCopy = values()
														.stream()
														.map(e -> new CharacteristicEntry(e.getKey(), index, e.getValue()))
														.collect(toList());
			copy.putAll(entriesCopy, true);
			return copy;
		}

		@Override
		protected CharacteristicEntry newEntry(KKey key, CharacteristicIndex index, Object value) {
			return new CharacteristicEntry(key, index, value);
		}

	}

	public static class GroupEntries extends Entries<GroupEntry, GroupIndex> {

		public GroupEntries(GroupIndex index) {
			super(index);
		}

		@Override
		public GroupEntries withIndex(GroupIndex index) {
			GroupEntries copy = new GroupEntries(index);

			List<GroupEntry> entriesCopy = values()
												.stream()
												.map(e -> new GroupEntry(e.getKey(), index, e.getValue()))
												.collect(toList());
			copy.putAll(entriesCopy, true);
			return copy;
		}

		@Override
		protected GroupEntry newEntry(KKey key, GroupIndex index, Object value) {
			return new GroupEntry(key, index, value);
		}

	}

	public static class ValueEntries extends Entries<ValueEntry, ValueIndex> {

		public ValueEntries(ValueIndex index) {
			super(index);
		}

		@Override
		public ValueEntries withIndex(ValueIndex index) {
			ValueEntries copy = new ValueEntries(index);

			List<ValueEntry> entriesCopy = values()
												.stream()
												.map(e -> new ValueEntry(e.getKey(), index, e.getValue()))
												.collect(toList());
			copy.putAll(entriesCopy, true);
			return copy;
		}

		@Override
		protected ValueEntry newEntry(KKey key, ValueIndex index, Object value) {
			return new ValueEntry(key, index, value);
		}

	}

	public static abstract class Entries<E extends AbstractEntry<I>, I> extends HashMap<KKey, E> implements IHasKKeyValues {

		private final I index;

		public Entries(I index) {
			super();
			this.index = index;
		}

		public void put(String key, Object value) {
			put(KKey.of(key), value);
		}

		public void put(KKey key, Object value) {
			put(newEntry(key, index, value));
		}

		public void put(E entry) {
			KKey key = entry.getKey();
			put(key, entry);
		}

		@Override
		public E put(KKey key, E entry) {
			if (!Objects.equals(index, entry.getIndex())) {
				throw new IllegalArgumentException("Index of the entry (" + entry.getIndex() + ") does not match entries index (" + index + ")");
			}

			return super.put(key, entry);
		}

		public void putAll(Collection<? extends E> entries, boolean overwriteExisting) {
			for (E entry : entries) {
				if (overwriteExisting) {
					put(entry.getKey(), entry);
				} else {
					putIfAbsent(entry.getKey(), entry);
				}
			}
		}

		public void putAll(Map<? extends KKey, ? extends E> entries, boolean overwriteExisting) {
			putAll(entries.values(), overwriteExisting);
		}

		public E get(KKey key) {
			return super.get(key);
		}

		public <T> T getValue(String key) {
			return getValue(KKey.of(key));
		}

		public <T> T getValue(String key, T defaultValue) {
			return getValue(KKey.of(key), defaultValue);
		}

		public <T> T getValue(KKey key, T defaultValue) {
			T value = getValue(key);

			return value == null ? defaultValue : value;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getValue(KKey key) {
			E entry = get(key);

			if (entry == null) {
				return null;
			} else {
				return (T) entry.getValue();
			}
		}

		public E remove(String key) {
			return remove(KKey.of(key));
		}

		public E remove(KKey key) {
			return super.remove(key);
		}

		public I getIndex() {
			return index;
		}

		public void forEachEntry(Consumer<E> action) {
			values().forEach(action);
		}

		protected abstract E newEntry(KKey key, I index, Object value);

		/**
		 * Creates a copy of this entries with a given index.
		 *
		 * @param index
		 * @return
		 */
		public abstract Entries<E, I> withIndex(I index);

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((index == null) ? 0 : index.hashCode());
			return result;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (!(obj instanceof Entries)) {
				return false;
			}
			Entries other = (Entries) obj;
			if (index == null) {
				if (other.index != null) {
					return false;
				}
			} else if (!index.equals(other.index)) {
				return false;
			}
			return true;
		}

	}

	public static abstract class AbstractEntry<I> {
		private final KKey key;
		private final I index;
		private final Object value;

		public AbstractEntry(KKey key, I index, Object value) {
			super();
			this.key = key;
			this.index = index;
			this.value = value;
		}

		public KKey getKey() {
			return key;
		}

		public I getIndex() {
			return index;
		}

		public Object getValue() {
			return value;
		}

		/**
		 * Whether this entry has given key.
		 *
		 * @param otherkey
		 * @return
		 */
		public boolean hasKey(KKey otherkey) {
			return key.equals(otherkey);
		}

		@Override
		public String toString() {
			if (value == null) {
				return "null";
			} else {
				return value.toString();
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((index == null) ? 0 : index.hashCode());
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof AbstractEntry)) {
				return false;
			}
			AbstractEntry other = (AbstractEntry) obj;
			if (index == null) {
				if (other.index != null) {
					return false;
				}
			} else if (!index.equals(other.index)) {
				return false;
			}
			if (key == null) {
				if (other.key != null) {
					return false;
				}
			} else if (!key.equals(other.key)) {
				return false;
			}
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}

	}

	public static class PartEntry extends AbstractEntry<PartIndex> {

		public PartEntry(KKey key, PartIndex index, Object value) {
			super(validateKey(key), index, value);
		}

		private static KKey validateKey(KKey key) {
			if (!key.isPartLevel() && !key.isCustomPartLevel()) {
				throw new IllegalArgumentException("K-Key of part type expected, but found: " + key);
			}
			return key;
		}
	}

	public static class CharacteristicEntry extends AbstractEntry<CharacteristicIndex> {

		public CharacteristicEntry(KKey key, CharacteristicIndex index, Object value) {
			super(validateKey(key), index, value);
		}

		private static KKey validateKey(KKey key) {
			if (!key.isCharacteristicLevel() && !key.isCustomCharacteristicLevel()) {
				throw new IllegalArgumentException("K-Key of characteristic type expected, but found: " + key);
			}
			return key;
		}
	}

	public static class ValueEntry extends AbstractEntry<ValueIndex>{

		public ValueEntry(KKey key, ValueIndex index, Object value) {
			super(validateKey(key), index, value);
		}

		private static KKey validateKey(KKey key) {
			if (!key.isValueLevel() && !key.isCustomValueLevel()) {
				throw new IllegalArgumentException("K-Key of value type expected, but found: " + key);
			}
			return key;
		}
	}

	public static class GroupEntry extends AbstractEntry<GroupIndex> {

		public GroupEntry(KKey key, GroupIndex index, Object value) {
			super(validateKey(key), index, value);
		}

		private static KKey validateKey(KKey key) {
			if (!key.isGroupLevel()) {
				throw new IllegalArgumentException("K-Key of group type expected, but found: " + key);
			}
			return key;
		}
	}

	/**
	 * Contains one "set" of values for all characteristics. <br>
	 *
	 * @author Vlastimil Dolejs
	 *
	 */
	public static class ValueSet {
		private final TreeMap<CharacteristicIndex, ValueEntries> valuesOfCharacteristics;

		public ValueSet() {
			this(new TreeMap<>());
		}

		public ValueSet(TreeMap<CharacteristicIndex, ValueEntries> valuesOfCharacteristics) {
			super();
			this.valuesOfCharacteristics = valuesOfCharacteristics;
		}

		public void addValueOfCharacteristic(CharacteristicIndex characteristicIndex, ValueEntries valueEntries) {
			valuesOfCharacteristics.put(characteristicIndex, valueEntries);
		}

		public List<CharacteristicIndex> getCharacteristicIndexes() {
			return new ArrayList<>(valuesOfCharacteristics.keySet());
		}

		public ValueEntries getValuesOfCharacteristic(CharacteristicIndex characteristicIndex) {
			return valuesOfCharacteristics.get(characteristicIndex);
		}

		public List<ValueEntries> getValues() {
			return new ArrayList<>(valuesOfCharacteristics.values());
		}
	}

}
