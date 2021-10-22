package cz.diribet.aqdef.model.builder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.model.AqdefObjectModel;
import cz.diribet.aqdef.model.CatalogRecordIndex;
import cz.diribet.aqdef.model.CharacteristicIndex;
import cz.diribet.aqdef.model.GroupIndex;
import cz.diribet.aqdef.model.PartIndex;
import cz.diribet.aqdef.model.ValueIndex;

/**
 * Builder that simplifies creation of {@link AqdefObjectModel}. <br>
 * Sample usage:
 *
 * <pre>
 * AqdefObjectModelBuilder builder = new AqdefObjectModelBuilder();
 *
 * builder.createPartEntry("K1001", "part number");
 * builder.createPartEntry("K1002", "part title");
 *
 * for (int i = 0; i <= 2; i++) {
 * 	builder.createCharacteristicEntry("K2001", "characteristic number " + i);
 *
 * 	for (int j = 0; j <= 2; j++) {
 * 		builder.createValueEntry("K0001", new BigDecimal(j));
 * 		builder.createValueEntry("K0004", new Date());
 * 		builder.nextValue();
 * 	}
 *
 * 	builder.nextCharacteristic();
 * }
 *
 * AqdefObjectModel objectModel = builder.build();
 * </pre>
 * <p>
 * Don't forget to call {@link #nextPart()}, {@link #nextCharacteristic()}, {@link #nextValue()} before you start writing new
 * record.
 * </p>
 *
 * @author Vlastimil Dolejs
 *
 */
public class AqdefObjectModelBuilder {
	//*******************************************
	// Attributes
	//*******************************************

	private final AqdefObjectModel aqdefObjectModel;
	private final AqdefHierarchyBuilder hierarchyBuilder;

	private final AtomicInteger partIndex = new AtomicInteger(1);
	private final AtomicInteger characteristicIndex = new AtomicInteger(1);
	private final AtomicInteger valueIndex = new AtomicInteger(1);
	private final AtomicInteger groupIndex = new AtomicInteger(1);
	private final AtomicInteger catalogRecordIndex = new AtomicInteger(1);

	private final AtomicInteger hierarchyNodeIndex = new AtomicInteger(1);

	//*******************************************
	// Constructors
	//*******************************************

	public AqdefObjectModelBuilder() {
		aqdefObjectModel = new AqdefObjectModel();
		hierarchyBuilder = new AqdefHierarchyBuilder();
	}

	//*******************************************
	// Methods
	//*******************************************

	public AqdefObjectModel build() {
		aqdefObjectModel.setHierarchy(hierarchyBuilder.getHierarchy());

		return aqdefObjectModel;
	}

	public void createPartEntry(String key, Object value) {
		createPartEntry(KKey.of(key), value);
	}

	public void createPartEntry(KKey key, Object value) {
		if (value == null) {
			return;
		}

		aqdefObjectModel.putPartEntry(key, currentPartIndex(), value);
	}

	/**
	 * Replaces value of a given K-key with a new value for a single part
	 * identified by the part {@code index} or for all parts if index is 0.
	 * <p>
	 * If the K-key doesn't have value, it will be added.
	 * </p>
	 *
	 * @param key
	 * @param index of part whose value will be replaced or 0
	 * @param value new value
	 */
	public void replacePartEntry(String key, int index, Object value) {
		KKey kKey = KKey.of(key);

		List<PartIndex> affectedPartIndexes;

		if (index == 0) {
			affectedPartIndexes = aqdefObjectModel.getPartIndexes();
		} else {
			affectedPartIndexes = Collections.singletonList(PartIndex.of(index));
		}

		affectedPartIndexes.forEach(partIndex -> aqdefObjectModel.putPartEntry(kKey, partIndex, value));
	}

	public void createCharacteristicEntry(String key, Object value) {
		createCharacteristicEntry(KKey.of(key), value);
	}

	public void createCharacteristicEntry(KKey key, Object value) {
		if (value == null) {
			return;
		}

		aqdefObjectModel.putCharacteristicEntry(key, currentCharacteristicIndex(), value);
	}

	/**
	 * Replaces value of a given K-key with a new value for:
	 * <ul>
	 *   <li>single characteristic of a single part if both {@code partIndex} and {@code characteristicIndex} are provided</li>
	 *   <li>single characteristic of all parts if {@code partIndex = 0} and {@code characteristicIndex > 0}</li>
	 *   <li>all characteristics of single part if {@code partIndex > 0} and {@code characteristicIndex = 0}</li>
	 *   <li>all characteristics of all parts if {@code partIndex = 0} and {@code characteristicIndex = 0}</li>
	 * </ul>
	 * <p>
	 * If the K-key doesn't have value, it will be added.
	 * </p>
	 *
	 * @param key
	 * @param partIndex
	 * @param characteristicIndex
	 * @param value
	 */
	public void replaceCharacteristicEntry(String key, int partIndex, int characteristicIndex, Object value) {
		KKey kKey = KKey.of(key);

		List<PartIndex> affectedPartIndexes;
		if (partIndex == 0) {
			affectedPartIndexes = aqdefObjectModel.getPartIndexes();
		} else {
			affectedPartIndexes = Collections.singletonList(PartIndex.of(partIndex));
		}

		affectedPartIndexes.forEach(affectedPartIndex -> {

			List<CharacteristicIndex> affectedCharacteristicIndexes;
			if (characteristicIndex == 0) {
				affectedCharacteristicIndexes = aqdefObjectModel.getCharacteristicIndexes(affectedPartIndex);
			} else {
				affectedCharacteristicIndexes = Collections.singletonList(CharacteristicIndex.of(affectedPartIndex, characteristicIndex));
			}

			affectedCharacteristicIndexes.forEach(affectedCharacteristicIndex -> {
				aqdefObjectModel.putCharacteristicEntry(kKey, affectedCharacteristicIndex, value);
			});
		});
	}

	public void createGroupEntry(String key, Object value) {
		createGroupEntry(KKey.of(key), value);
	}

	public void createGroupEntry(KKey key, Object value) {
		if (value == null) {
			return;
		}

		aqdefObjectModel.putGroupEntry(key, currentGroupIndex(), value);
	}

	public void createValueEntry(String key, Object value) {
		createValueEntry(KKey.of(key), value);
	}

	public void createValueEntry(KKey key, Object value) {
		if (value == null) {
			return;
		}

		aqdefObjectModel.putValueEntry(key, currentValueIndex(), value);
	}

	public void createHierarchyNodeOfPart() {
		hierarchyBuilder.createHierarchyNodeOfPart(hierarchyNodeIndex.getAndIncrement(), partIndex.get());
	}

	public void createHierarchyNodeOfCharacteristic(int characteristicId, Integer parentCharacteristicId) {
		hierarchyBuilder.createHierarchyNodeOfCharacteristic(
												hierarchyNodeIndex.getAndIncrement(),
												partIndex.get(),
												characteristicIndex.get(),
												characteristicId,
												parentCharacteristicId);
	}

	public void createHierarchyNodeOfGroup(int characteristicId, Integer parentCharacteristicId) {
		hierarchyBuilder.createHierarchyNodeOfGroup(
												hierarchyNodeIndex.getAndIncrement(),
												partIndex.get(),
												groupIndex.get(),
												characteristicId,
												parentCharacteristicId);
	}

	public void createCatalogRecordEntry(String key, Object value) {
		createCatalogRecordEntry(KKey.of(key), value);
	}

	public void createCatalogRecordEntry(KKey key, Object value) {
		if (value == null) {
			return;
		}

		aqdefObjectModel.putCatalogRecordEntry(key, currentCatalogRecordIndex(), value);
	}

	/**
	 * You have to call this method after all data of current part (and its characteristics and values) are written.
	 */
	public void nextPart() {
		partIndex.incrementAndGet();
	}

	/**
	 * You have to call this method after all data of current characteristic (and its values) are written.
	 */
	public void nextCharacteristic() {
		characteristicIndex.incrementAndGet();
		valueIndex.set(1);
	}

	/**
	 * You have to call this method after all data of current value are written.
	 */
	public void nextValue() {
		valueIndex.incrementAndGet();
	}

	/**
	 * You have to call this method after all data of current group are written.
	 */
	public void nextGroup() {
		groupIndex.incrementAndGet();
	}

	/**
	 * You have to call this method after all data of the current catalog record are written.
	 */
	public void nextCatalogRecord() {
		catalogRecordIndex.incrementAndGet();
	}

	private PartIndex currentPartIndex() {
		return PartIndex.of(partIndex.get());
	}

	private CharacteristicIndex currentCharacteristicIndex() {
		return CharacteristicIndex.of(currentPartIndex(), characteristicIndex.get());
	}

	private GroupIndex currentGroupIndex() {
		return GroupIndex.of(currentPartIndex(), groupIndex.get());
	}

	private ValueIndex currentValueIndex() {
		return ValueIndex.of(currentCharacteristicIndex(), valueIndex.get());
	}

	private CatalogRecordIndex currentCatalogRecordIndex() {
		return CatalogRecordIndex.of(catalogRecordIndex.get());
	}

}
