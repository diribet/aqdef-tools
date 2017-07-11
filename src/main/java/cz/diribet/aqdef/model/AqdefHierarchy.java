package cz.diribet.aqdef.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.apache.commons.collections4.CollectionUtils;

import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.model.AqdefObjectModel.AbstractEntry;

/**
 * Contains information about hierarchy of <i>parts / characteristics /
 * groups</i>.
 * <p>
 * This information if contained in {@code K51xx} keys. The simplified
 * characteristics hierarchy from {@code K2030/K2031} keys is also supported,
 * but is internally transformed to the {@code K51xx} structure.
 * </p>
 * <p>
 * Note that both hierarchy types together are NOT supported.
 * </p>
 *
 * @author Vlastimil Dolejs
 *
 */
public class AqdefHierarchy {
	//*******************************************
	// Attributes
	//*******************************************

	private static final KKey KEY_PART_NODE = KKey.of("K5111");
	private static final KKey KEY_CHARACTERISTIC_NODE = KKey.of("K5112");
	private static final KKey KEY_GROUP_NODE = KKey.of("K5113");

	private static final KKey KEY_NODE_BINDING = KKey.of("K5103");
	private static final KKey KEY_CHARACTERISTIC_BINDING = KKey.of("K5102");

	private static final KKey KEY_SIMPLE_GROUPING_CHARACTERISTIC_PARENT = KKey.of("K2030");
	private static final KKey KEY_SIMPLE_GROUPING_CHARACTERISTIC_CHILD = KKey.of("K2031");

	private TreeMap<NodeIndex, HierarchyEntry> nodeDefinitions = new TreeMap<>();
	private TreeMap<NodeIndex, List<HierarchyEntry>> nodeBindings = new TreeMap<>();

	private boolean containsHierarchyInformation;
	private boolean containsSimpleHierarchyInformation;

	//*******************************************
	// Methods
	//*******************************************

	public void putEntry(KKey kKey, Integer index, Object value) {
		requireNonNull(kKey);

		if (kKey.isSimpleHierarchyLevel()) {
			putSimpleHierarchyEntry(kKey, index, value);
			return;
		}

		NodeIndex nodeIndex = NodeIndex.of(index);

		HierarchyEntry hierarchyEntry;
		if (isNodeDefinition(kKey) || isBinding(kKey)) {

			hierarchyEntry = new HierarchyEntry(kKey, nodeIndex, (Integer) value);

		} else {
			throw new IllegalArgumentException("Unknown hierarchy entry. Key: " + kKey + " Value: " + Objects.toString(value));
		}

		putEntry(hierarchyEntry);
	}

	public void putEntry(HierarchyEntry entry) {
		requireNonNull(entry);

		KKey kKey = entry.getKey();

		if (kKey.isSimpleHierarchyLevel()) {
			throw new RuntimeException("Direct insertion of simple hierarchy entry is not supported.");
		}

		if (containsSimpleHierarchyInformation) {
			throw new RuntimeException("Combination of hierarchy (K51xx) and simple hierarchy (K2030/2031) is not supported.");
		}

		putEntryInternal(entry);

		containsHierarchyInformation = true;
	}

	private void putSimpleHierarchyEntry(KKey kKey, Integer index, Object value) {
		if (containsHierarchyInformation) {
			throw new RuntimeException("Combination of hierarchy (K51xx) and simple hierarchy (K2030/2031) is not supported.");
		}

		if (isCharacteristicSimpleGroupingParent(kKey)) {

			// create characteristic node
			NodeIndex nodeIndex = NodeIndex.of((Integer) value);
			HierarchyEntry hierarchyEntry = new HierarchyEntry(KEY_CHARACTERISTIC_NODE, nodeIndex, index);
			putEntryInternal(hierarchyEntry);

		} else if (isCharacteristicSimpleGroupingChild(kKey)) {

			// bind characteristic to its parent node
			NodeIndex nodeIndex = NodeIndex.of((Integer) value);
			HierarchyEntry hierarchyEntry = new HierarchyEntry(KEY_CHARACTERISTIC_BINDING, nodeIndex, index);
			putEntryInternal(hierarchyEntry);

		} else {
			throw new IllegalArgumentException("Unknown simple hierarchy entry. Key: " + kKey + " Value: " + Objects.toString(value));
		}

		containsSimpleHierarchyInformation = true;
	}

	private void putEntryInternal(HierarchyEntry entry) {
		KKey kKey = entry.getKey();

		if (isNodeDefinition(kKey)) {

			nodeDefinitions.put(entry.getIndex(), entry);

		} else if (isBinding(kKey)) {

			nodeBindings.computeIfAbsent(entry.getIndex(), k -> new ArrayList<>()).add(entry);

		} else {
			throw new IllegalArgumentException("Unknown hierarchy entry. Key: " + kKey + " Value: " + Objects.toString(entry.getValue()));
		}
	}

	private boolean isBinding(KKey kKey) {
		return isNodeBinding(kKey) || isCharacteristicBinding(kKey);
	}

	/**
	 * Binding between node and another node (group or characteristic that contins child characteristics).
	 * @param kKey
	 * @return
	 */
	private boolean isNodeBinding(KKey kKey) {
		return kKey.equals(KEY_NODE_BINDING);
	}

	/**
	 * Binding between node and characteristic that does not contin child characteristics.
	 *
	 * @param kKey
	 * @return
	 */
	private boolean isCharacteristicBinding(KKey kKey) {
		return kKey.equals(KEY_CHARACTERISTIC_BINDING);
	}


	private boolean isNodeDefinition(KKey kKey) {
		return isPartNode(kKey) || isCharacteristicNode(kKey) || isGroupNode(kKey);
	}

	private boolean isPartNode(KKey kKey) {
		return kKey.equals(KEY_PART_NODE);
	}

	private boolean isCharacteristicNode(KKey kKey) {
		return kKey.equals(KEY_CHARACTERISTIC_NODE);
	}

	private boolean isGroupNode(KKey kKey) {
		return kKey.equals(KEY_GROUP_NODE);
	}

	private boolean isCharacteristicSimpleGroupingParent(KKey kKey) {
		return kKey.equals(KEY_SIMPLE_GROUPING_CHARACTERISTIC_PARENT);
	}

	private boolean isCharacteristicSimpleGroupingChild(KKey kKey) {
		return kKey.equals(KEY_SIMPLE_GROUPING_CHARACTERISTIC_CHILD);
	}

	public void forEachNodeDefinition(Consumer<HierarchyEntry> action) {
		nodeDefinitions.values().forEach(action);
	}

	public void forEachNodeBinding(Consumer<HierarchyEntry> action) {
		nodeBindings.values().stream().flatMap(List::stream).forEach(action);
	}

	public boolean isEmpty() {
		return nodeDefinitions.isEmpty() && nodeBindings.isEmpty();
	}

	public boolean hasChildren(CharacteristicIndex characteristicIndex) {
		Optional<NodeIndex> nodeIndexOfCharacteristic = getNodeIndexOfCharacteristic(characteristicIndex);

		if (nodeIndexOfCharacteristic.isPresent()) {
			List<HierarchyEntry> children = nodeBindings.get(nodeIndexOfCharacteristic.get());
			return CollectionUtils.isNotEmpty(children);
		}

		return false;
	}

	/**
	 * Find index of parent characteristic or group of given characteristic.
	 *
	 * @param characteristicIndex
	 * @return optional containing {@link CharacteristicIndex} or {@link GroupIndex} of parent, or empty optional if given
	 *         characteristic do not have a parent
	 */
	public Optional<Object> getParentIndex(CharacteristicIndex characteristicIndex) {
		Optional<NodeIndex> parentNodeIndexOfCharacteristic = getParentNodeIndexOfCharacteristic(characteristicIndex);

		if (parentNodeIndexOfCharacteristic.isPresent()) {
			// characteristic is directly assigned to parent
			return getCharacteristicOrGroupIndexOfNode(parentNodeIndexOfCharacteristic.get(), characteristicIndex.getPartIndex());
		} else {
			Optional<NodeIndex> nodeIndexOfCharacteristic = getNodeIndexOfCharacteristic(characteristicIndex);

			if (nodeIndexOfCharacteristic.isPresent()) {
				// characteristic is defined as node that may be assigned to parent node
				Optional<NodeIndex> parentNodeIndex = getParentNodeIndexOfNode(nodeIndexOfCharacteristic.get());

				if (parentNodeIndex.isPresent()) {
					return getCharacteristicOrGroupIndexOfNode(parentNodeIndex.get(), characteristicIndex.getPartIndex());
				}
			}

		}

		return Optional.empty();
	}

	/**
	 * Find index of parent characteristic or group of given group.
	 *
	 * @param groupIndex
	 * @return optional containing {@link CharacteristicIndex} or {@link GroupIndex} of parent, or empty optional if given
	 *         group do not have a parent
	 */
	public Optional<Object> getParentIndex(GroupIndex groupIndex) {
		Optional<NodeIndex> nodeIndexOfGroup = getNodeIndexOfGroup(groupIndex);

		if (nodeIndexOfGroup.isPresent()) {
			Optional<NodeIndex> parentNodeIndex = getParentNodeIndexOfNode(nodeIndexOfGroup.get());

			if (parentNodeIndex.isPresent()) {
				return getCharacteristicOrGroupIndexOfNode(parentNodeIndex.get(), groupIndex.getPartIndex());
			}
		}

		return Optional.empty();
	}

	/**
	 * If the given node is characteristic or group returns its {@link CharacteristicIndex} or {@link GroupIndex}.
	 *
	 * @param nodeIndex
	 * @param partIndex
	 * @return
	 */
	private Optional<Object> getCharacteristicOrGroupIndexOfNode(NodeIndex nodeIndex, PartIndex partIndex) {
		HierarchyEntry nodeDefinition = nodeDefinitions.get(nodeIndex);
		Integer index = (Integer) nodeDefinition.getValue();

		if (nodeDefinition.getKey().equals(KEY_CHARACTERISTIC_NODE)) {
			return Optional.of(CharacteristicIndex.of(partIndex, index));
		} else if (nodeDefinition.getKey().equals(KEY_GROUP_NODE)) {
			return Optional.of(GroupIndex.of(partIndex, index));
		} else {
			return Optional.empty();
		}
	}

	private Optional<NodeIndex> getParentNodeIndexOfNode(NodeIndex nodeIndex) {
		return nodeBindings.values()
								.stream()
								.flatMap(list -> list.stream())
								.filter(hierarchyEntry -> {
									return hierarchyEntry.getKey().equals(KEY_NODE_BINDING)
											&& nodeIndex.getIndex().equals(hierarchyEntry.getValue());
								})
								.map(hierarchyEntry -> hierarchyEntry.getIndex())
								.findAny();
	}

	private Optional<NodeIndex> getParentNodeIndexOfCharacteristic(CharacteristicIndex characteristicIndex) {
		return nodeBindings.values()
								.stream()
								.flatMap(list -> list.stream())
								.filter(hierarchyEntry -> {
									return hierarchyEntry.getKey().equals(KEY_CHARACTERISTIC_BINDING)
											&& characteristicIndex.getCharacteristicIndex().equals(hierarchyEntry.getValue());
								})
								.map(hierarchyEntry -> hierarchyEntry.getIndex())
								.findAny();
	}

	private Optional<NodeIndex> getNodeIndexOfGroup(GroupIndex groupIndex) {
		requireNonNull(groupIndex);
		requireNonNull(groupIndex.getGroupIndex());

		Integer groupIndexInt = groupIndex.getGroupIndex();

		return nodeDefinitions.entrySet()
										.stream()
										.filter(entry -> {
											HierarchyEntry hierarchyEntry = entry.getValue();
											return KEY_GROUP_NODE.equals(hierarchyEntry.getKey())
													&& groupIndexInt.equals(hierarchyEntry.getValue());
										})
										.map(entry -> entry.getKey())
										.findAny();
	}

	private Optional<NodeIndex> getNodeIndexOfCharacteristic(CharacteristicIndex characteristicIndex) {
		requireNonNull(characteristicIndex);
		requireNonNull(characteristicIndex.getCharacteristicIndex());

		Integer characteristicIndexInt = characteristicIndex.getCharacteristicIndex();

		return nodeDefinitions.entrySet()
										.stream()
										.filter(entry -> {
											HierarchyEntry hierarchyEntry = entry.getValue();
											return KEY_CHARACTERISTIC_NODE.equals(hierarchyEntry.getKey())
													&& characteristicIndexInt.equals(hierarchyEntry.getValue());
										})
										.map(entry -> entry.getKey())
										.findAny();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeBindings == null) ? 0 : nodeBindings.hashCode());
		result = prime * result + ((nodeDefinitions == null) ? 0 : nodeDefinitions.hashCode());
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
		if (!(obj instanceof AqdefHierarchy)) {
			return false;
		}
		AqdefHierarchy other = (AqdefHierarchy) obj;
		if (nodeBindings == null) {
			if (other.nodeBindings != null) {
				return false;
			}
		} else if (!nodeBindings.equals(other.nodeBindings)) {
			return false;
		}
		if (nodeDefinitions == null) {
			if (other.nodeDefinitions != null) {
				return false;
			}
		} else if (!nodeDefinitions.equals(other.nodeDefinitions)) {
			return false;
		}
		return true;
	}

	//*******************************************
	// Inner classes
	//*******************************************

	public static class HierarchyEntry extends AbstractEntry<NodeIndex> {

		public HierarchyEntry(KKey key, NodeIndex index, Integer value) {
			super(validateKey(key), index, value);
		}

		private static KKey validateKey(KKey key) {
			if (!key.isHierarchyLevel()) {
				throw new IllegalArgumentException("K-Key of hierarchy type expected, but found: " + key);
			}
			return key;
		}
	}

}
