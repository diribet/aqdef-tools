package cz.diribet.aqdef.model.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import cz.diribet.aqdef.AqdefConstants;
import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.model.AqdefHierarchy;
import cz.diribet.aqdef.model.AqdefHierarchy.HierarchyEntry;
import cz.diribet.aqdef.model.CharacteristicIndex;
import cz.diribet.aqdef.model.NodeIndex;
import cz.diribet.aqdef.model.PartIndex;

/**
 * Builder that creates {@link AqdefHierarchy}. Hierarchy is created as K5xxx K-key entries.
 *
 *
 * @author Vlastimil Dolejs
 *
 */
public class AqdefHierarchyBuilder implements AqdefConstants {
	//*******************************************
	// Attributes
	//*******************************************

	private static final Logger LOG = LoggerFactory.getLogger(AqdefHierarchyBuilder.class);

	private boolean containsHierarchy = false;

	private final List<AbstractHierarchyNode> nodes = new ArrayList<>();
	private final List<AbstractNodeBindingToParent> nodeBindings = new ArrayList<>();

	private final Map<Integer, Integer> partIndexToHierarchyNodeIndex = new HashMap<>();
	private final BiMap<CharacteristicId, Integer> characteristicIdToHierarchyNodeIndex = HashBiMap.create();

	//*******************************************
	// Methods
	//*******************************************

	public AqdefHierarchy getHierarchy() {
		AqdefHierarchy hierarchy = new AqdefHierarchy();

		if (containsHierarchy) {
			try {
				writeNodes(hierarchy);
				writeNodeBindings(hierarchy);
			} catch (InvalidHierarchyException e) {
				LOG.warn("AQDEF hierarchy was not created. Reason: {}", e.getMessage());
				hierarchy = new AqdefHierarchy();
			}
		}

		return hierarchy;
	}

	private void writeNodes(AqdefHierarchy hierarchy) {
		for (AbstractHierarchyNode node : nodes) {
			hierarchy.putEntry(node.createEntry());
		}
	}

	private void writeNodeBindings(AqdefHierarchy hierarchy) throws InvalidHierarchyException {
		for (AbstractNodeBindingToParent nodeBinding : nodeBindings) {

			CharacteristicId parentCharacteristicId = nodeBinding.getParentCharacteristicId();
			Integer parentNodeIndex;
			if (parentCharacteristicId.getCharacteristicId() == null) {
				// node doesn't have parent characteristic (top level node) - in this case node is bound to part
				Integer partIndex = nodeBinding.getParentCharacteristicId().getPartIndex();
				parentNodeIndex = partIndexToHierarchyNodeIndex.get(partIndex);

				if (parentNodeIndex == null) {
					String message = "Part with index (" + partIndex + ") was not found in given data.";
					throw new InvalidHierarchyException(message);
				}
			} else {
				// parent node is characteristic
				parentNodeIndex = characteristicIdToHierarchyNodeIndex.get(parentCharacteristicId);

				if (parentNodeIndex == null) {
					String message = "Characteristic with part index " + parentCharacteristicId.getPartIndex() +
									" and characteristic id " + parentCharacteristicId.getCharacteristicId() +
									" was not found in given data.";
					throw new InvalidHierarchyException(message);
				}
			}


			KKey kKey;
			Integer identifier;
			if (isNode(nodeBinding)) {
				kKey = KKey.of("K5103");
				identifier = nodeBinding.getHierarchyNodeIndex();
			} else {
				if (nodeBinding instanceof CharacteristicNodeBindingToParent) {
					CharacteristicNodeBindingToParent characteristicNodeBinding = (CharacteristicNodeBindingToParent) nodeBinding;
					identifier = characteristicNodeBinding.getCharacteristicIndex();
				} else {
					throw new IllegalStateException("Binding should be CharacteristicNodeBindingToParent.");
				}
				kKey = KKey.of("K5102");
			}

			HierarchyEntry entry = new HierarchyEntry(
													kKey,
													NodeIndex.of(parentNodeIndex),
													identifier);
			hierarchy.putEntry(entry);
		}
	}

	/**
	 * If the child node of the given {@code nodeBinding} is group or characteristic with child characteristics.
	 *
	 * @param nodeBinding
	 * @return
	 */
	private boolean isNode(AbstractNodeBindingToParent nodeBinding) {
		return nodeBinding instanceof GroupNodeBindingToParent ||
				characteristicContainsChildCharacteristics(nodeBinding.getHierarchyNodeIndex());
	}

	private boolean characteristicContainsChildCharacteristics(Integer hierarchyNodeIndex) {
		CharacteristicId characteristicId = characteristicIdToHierarchyNodeIndex.inverse().get(hierarchyNodeIndex);

		return nodeBindings.stream().anyMatch(nodeBinding -> {
			CharacteristicId parentCharacteristicId = nodeBinding.getParentCharacteristicId();
			return parentCharacteristicId != null && parentCharacteristicId.equals(characteristicId);
		});
	}

	public void createHierarchyNodeOfPart(int hierarchyNodeIndex, int partIndex) {
		nodes.add(new PartHierarchyNode(hierarchyNodeIndex, partIndex));
		partIndexToHierarchyNodeIndex.put(partIndex, hierarchyNodeIndex);
	}

	public void createHierarchyNodeOfCharacteristic(int hierarchyNodeIndex, int partIndex, int characteristicIndex, int characteristicId, Integer parentCharacteristicId) {
		parentCharacteristicId = resolveEmptyParentCharacteristicId(parentCharacteristicId);

		nodes.add(new CharacteristicHierarchyNode(hierarchyNodeIndex, partIndex, characteristicIndex));

		characteristicIdToHierarchyNodeIndex.put(new CharacteristicId(partIndex, characteristicId), hierarchyNodeIndex);

		nodeBindings.add(new CharacteristicNodeBindingToParent(hierarchyNodeIndex, new CharacteristicId(partIndex, parentCharacteristicId), characteristicIndex));
		if (parentCharacteristicId != null) {
			containsHierarchy = true;
		}
	}

	public void createHierarchyNodeOfGroup(int hierarchyNodeIndex, int partIndex, int groupIndex, int characteristicId, Integer parentCharacteristicId) {
		parentCharacteristicId = resolveEmptyParentCharacteristicId(parentCharacteristicId);

		nodes.add(new GroupHierarchyNode(hierarchyNodeIndex, partIndex, groupIndex));

		characteristicIdToHierarchyNodeIndex.put(new CharacteristicId(partIndex, characteristicId), hierarchyNodeIndex);

		nodeBindings.add(new GroupNodeBindingToParent(hierarchyNodeIndex, new CharacteristicId(partIndex, parentCharacteristicId)));

		containsHierarchy = true;
	}

	private Integer resolveEmptyParentCharacteristicId(Integer parentCharacteristicId) {
		if (parentCharacteristicId != null && parentCharacteristicId == 0) {
			return null;
		} else {
			return parentCharacteristicId;
		}
	}

	//*******************************************
	// Inner classes
	//*******************************************

	private static abstract class AbstractNodeBindingToParent {
		private final int hierarchyNodeIndex;
		private final CharacteristicId parentCharacteristicId;

		public AbstractNodeBindingToParent(int hierarchyNodeIndex, CharacteristicId parentCharacteristicId) {
			super();
			this.hierarchyNodeIndex = hierarchyNodeIndex;
			this.parentCharacteristicId = parentCharacteristicId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + hierarchyNodeIndex;
			result = prime * result + ((parentCharacteristicId == null) ? 0 : parentCharacteristicId.hashCode());
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
			if (!(obj instanceof AbstractNodeBindingToParent)) {
				return false;
			}
			AbstractNodeBindingToParent other = (AbstractNodeBindingToParent) obj;
			if (hierarchyNodeIndex != other.hierarchyNodeIndex) {
				return false;
			}
			if (parentCharacteristicId == null) {
				if (other.parentCharacteristicId != null) {
					return false;
				}
			} else if (!parentCharacteristicId.equals(other.parentCharacteristicId)) {
				return false;
			}
			return true;
		}

		public int getHierarchyNodeIndex() {
			return hierarchyNodeIndex;
		}

		public CharacteristicId getParentCharacteristicId() {
			return parentCharacteristicId;
		}
	}

	private static class CharacteristicNodeBindingToParent extends AbstractNodeBindingToParent {
		private final int characteristicIndex;

		public CharacteristicNodeBindingToParent(int hierarchyNodeIndex, CharacteristicId parentCharacteristicId, int characteristicIndex) {
			super(hierarchyNodeIndex, parentCharacteristicId);
			this.characteristicIndex = characteristicIndex;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + characteristicIndex;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (!(obj instanceof CharacteristicNodeBindingToParent)) {
				return false;
			}
			CharacteristicNodeBindingToParent other = (CharacteristicNodeBindingToParent) obj;
			if (characteristicIndex != other.characteristicIndex) {
				return false;
			}
			return true;
		}

		public int getCharacteristicIndex() {
			return characteristicIndex;
		}

	}

	private static class GroupNodeBindingToParent extends AbstractNodeBindingToParent {
		public GroupNodeBindingToParent(int hierarchyNodeIndex, CharacteristicId parentCharacteristicId) {
			super(hierarchyNodeIndex, parentCharacteristicId);
		}
	}

	private static class CharacteristicId {
		private final Integer partIndex;
		private final Integer characteristicId;

		public CharacteristicId(Integer partIndex, Integer characteristicId) {
			super();
			this.partIndex = partIndex;
			this.characteristicId = characteristicId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((partIndex == null) ? 0 : partIndex.hashCode());
			result = prime * result + ((characteristicId == null) ? 0 : characteristicId.hashCode());
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
			if (!(obj instanceof CharacteristicId)) {
				return false;
			}
			CharacteristicId other = (CharacteristicId) obj;
			if (partIndex == null) {
				if (other.partIndex != null) {
					return false;
				}
			} else if (!partIndex.equals(other.partIndex)) {
				return false;
			}
			if (characteristicId == null) {
				if (other.characteristicId != null) {
					return false;
				}
			} else if (!characteristicId.equals(other.characteristicId)) {
				return false;
			}
			return true;
		}

		public Integer getPartIndex() {
			return partIndex;
		}

		public Integer getCharacteristicId() {
			return characteristicId;
		}
	}

	public static abstract class AbstractHierarchyNode {
		private final int nodeIndex;

		public AbstractHierarchyNode(int nodeIndex) {
			this.nodeIndex = nodeIndex;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + nodeIndex;
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
			if (!(obj instanceof AbstractHierarchyNode)) {
				return false;
			}
			AbstractHierarchyNode other = (AbstractHierarchyNode) obj;
			if (nodeIndex != other.nodeIndex) {
				return false;
			}
			return true;
		}

		public NodeIndex getNodeIndex() {
			return NodeIndex.of(nodeIndex);
		}

		protected abstract HierarchyEntry createEntry();
	}

	public static class PartHierarchyNode extends AbstractHierarchyNode {
		private final PartIndex partIndex;

		public PartHierarchyNode(int nodeIndex, int partIndex) {
			super(nodeIndex);
			this.partIndex = PartIndex.of(partIndex);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((partIndex == null) ? 0 : partIndex.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (!(obj instanceof PartHierarchyNode)) {
				return false;
			}
			PartHierarchyNode other = (PartHierarchyNode) obj;
			if (partIndex == null) {
				if (other.partIndex != null) {
					return false;
				}
			} else if (!partIndex.equals(other.partIndex)) {
				return false;
			}
			return true;
		}

		@Override
		protected HierarchyEntry createEntry() {
			return new HierarchyEntry(KKey.of("K5111"), getNodeIndex(), partIndex.getIndex());
		}

	}

	public static class CharacteristicHierarchyNode extends AbstractHierarchyNode {
		private final CharacteristicIndex characteristicIndex;

		public CharacteristicHierarchyNode(int nodeIndex, Integer partIndex, int characteristicIndex) {
			super(nodeIndex);
			this.characteristicIndex = CharacteristicIndex.of(partIndex, characteristicIndex);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((characteristicIndex == null) ? 0 : characteristicIndex.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (!(obj instanceof CharacteristicHierarchyNode)) {
				return false;
			}
			CharacteristicHierarchyNode other = (CharacteristicHierarchyNode) obj;
			if (characteristicIndex == null) {
				if (other.characteristicIndex != null) {
					return false;
				}
			} else if (!characteristicIndex.equals(other.characteristicIndex)) {
				return false;
			}
			return true;
		}

		@Override
		protected HierarchyEntry createEntry() {
			return new HierarchyEntry(KKey.of("K5112"), getNodeIndex(), characteristicIndex.getCharacteristicIndex());
		}

	}

	public static class GroupHierarchyNode extends AbstractHierarchyNode {
		private final PartIndex partIndex;
		private final int groupIndex;

		public GroupHierarchyNode(int nodeIndex, Integer partIndex, int groupIndex) {
			super(nodeIndex);
			this.partIndex = PartIndex.of(partIndex);
			this.groupIndex = groupIndex;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((partIndex == null) ? 0 : partIndex.hashCode());
			result = prime * result + groupIndex;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (!(obj instanceof GroupHierarchyNode)) {
				return false;
			}
			GroupHierarchyNode other = (GroupHierarchyNode) obj;
			if (partIndex == null) {
				if (other.partIndex != null) {
					return false;
				}
			} else if (!partIndex.equals(other.partIndex)) {
				return false;
			}
			if (groupIndex != other.groupIndex) {
				return false;
			}
			return true;
		}

		@Override
		protected HierarchyEntry createEntry() {
			return new HierarchyEntry(KKey.of("K5113"), getNodeIndex(), groupIndex);
		}

	}

	private static class InvalidHierarchyException extends Exception {

		public InvalidHierarchyException(String message) {
			super(message);
		}

	}
}
