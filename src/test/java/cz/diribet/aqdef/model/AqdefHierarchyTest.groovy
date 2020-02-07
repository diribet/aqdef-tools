package cz.diribet.aqdef.model

import cz.diribet.aqdef.parser.AqdefParser
import spock.lang.Specification

class AqdefHierarchyTest extends Specification {

	/**
	 * <pre>
	 * Part – node1
	 *     Characteristic1
	 *     Characteristic2 – node2
	 *         Characteristic3
	 *         Characteristic4
	 *     Group1 – node3
	 *         Characteristic5
	 *         Characteristic6
	 * </pre>
	 */
	static def DFQ = """
		K0100 6
		K1001/1 part_1
		K2001/1 characteristic_1
		K2001/2 characteristic_2
		K2001/3 characteristic_3
		K2001/4 characteristic_4
		K2001/5 characteristic_5
		K2001/6 characteristic_6
		K5001/1 group_1
		K5111/1 1
		K5112/2 2
		K5113/3 1
		K5103/1 2
		K5102/2 3
		K5102/2 4
		K5103/1 3
		K5102/3 5
		K5102/3 6
	"""

	def "hierarchy is created properly"() {
		when:
			def dfqHierarchy = parse(DFQ)

			def partNodeIndex = NodeIndex.of(1)
			def characteristic_2_nodeIndex = NodeIndex.of(2)
			def group_2_nodeIndex = NodeIndex.of(3)

			def partIndex = PartIndex.of(1)

			def characteristic_2 = CharacteristicIndex.of(partIndex, 2)
			def characteristic_3 = CharacteristicIndex.of(partIndex, 3)
			def characteristic_4 = CharacteristicIndex.of(partIndex, 4)
			def characteristic_5 = CharacteristicIndex.of(partIndex, 5)
			def characteristic_6 = CharacteristicIndex.of(partIndex, 6)

			def group_1 = GroupIndex.of(partIndex, 1)

		then:
			dfqHierarchy.nodeDefinitions.size() == 3
			dfqHierarchy.nodeBindings[partNodeIndex].size() == 2
			dfqHierarchy.nodeBindings[characteristic_2_nodeIndex].size() == 2
			dfqHierarchy.nodeBindings[group_2_nodeIndex].size() == 2
			dfqHierarchy.getParentIndex(characteristic_3).get() == characteristic_2
			dfqHierarchy.getParentIndex(characteristic_4).get() == characteristic_2
			dfqHierarchy.getParentIndex(characteristic_5).get() == group_1
			dfqHierarchy.getParentIndex(characteristic_6).get() == group_1
	}

	def "remove part node"() {
		given:
			def dfqHierarchy = parse(DFQ)

		when:
			dfqHierarchy.removeHierarchyForPart(PartIndex.of(1))

		then:
			dfqHierarchy.nodeDefinitions.size() == 0
			dfqHierarchy.nodeBindings.size() == 0
	}

	def "remove characteristic node"() {
		given:
			def dfqHierarchy = parse(DFQ)

			def partNodeIndex = NodeIndex.of(1)
			def characteristic_2_nodeIndex = NodeIndex.of(2)
			def group_2_nodeIndex = NodeIndex.of(3)

			def partIndex = PartIndex.of(1)

			def characteristic_3 = CharacteristicIndex.of(partIndex, 3)
			def characteristic_4 = CharacteristicIndex.of(partIndex, 4)
			def characteristic_5 = CharacteristicIndex.of(partIndex, 5)
			def characteristic_6 = CharacteristicIndex.of(partIndex, 6)

			def group_1 = GroupIndex.of(partIndex, 1)

		when:
			dfqHierarchy.removeHierarchyForCharacteristic(CharacteristicIndex.of(1, 2))

		then:
			dfqHierarchy.nodeDefinitions.size() == 2
			dfqHierarchy.nodeBindings[partNodeIndex].size() == 1
			dfqHierarchy.nodeBindings[characteristic_2_nodeIndex] == null
			dfqHierarchy.nodeBindings[group_2_nodeIndex].size() == 2
			dfqHierarchy.getParentIndex(characteristic_3).isPresent() == false
			dfqHierarchy.getParentIndex(characteristic_4).isPresent() == false
			dfqHierarchy.getParentIndex(characteristic_5).get() == group_1
			dfqHierarchy.getParentIndex(characteristic_6).get() == group_1
	}

	def "remove characteristic leaf"() {
		given:
			def dfqHierarchy = parse(DFQ)

			def partNodeIndex = NodeIndex.of(1)
			def characteristic_2_nodeIndex = NodeIndex.of(2)
			def group_2_nodeIndex = NodeIndex.of(3)

			def partIndex = PartIndex.of(1)

			def characteristic_2 = CharacteristicIndex.of(partIndex, 2)
			def characteristic_3 = CharacteristicIndex.of(partIndex, 3)
			def characteristic_4 = CharacteristicIndex.of(partIndex, 4)
			def characteristic_5 = CharacteristicIndex.of(partIndex, 5)
			def characteristic_6 = CharacteristicIndex.of(partIndex, 6)

			def group_1 = GroupIndex.of(partIndex, 1)

		when:
			dfqHierarchy.removeHierarchyForCharacteristic(characteristic_4)

		then:
			dfqHierarchy.nodeDefinitions.size() == 3
			dfqHierarchy.nodeBindings[partNodeIndex].size() == 2
			dfqHierarchy.nodeBindings[characteristic_2_nodeIndex].size() == 1
			dfqHierarchy.nodeBindings[group_2_nodeIndex].size() == 2
			dfqHierarchy.getParentIndex(characteristic_3).get() == characteristic_2
			dfqHierarchy.getParentIndex(characteristic_4).isPresent() == false
			dfqHierarchy.getParentIndex(characteristic_5).get() == group_1
			dfqHierarchy.getParentIndex(characteristic_6).get() == group_1
	}

	def "remove group node"() {
		given:
			def dfqHierarchy = parse(DFQ)

			def partNodeIndex = NodeIndex.of(1)
			def characteristic_2_nodeIndex = NodeIndex.of(2)
			def group_2_nodeIndex = NodeIndex.of(3)

			def partIndex = PartIndex.of(1)

			def characteristic_2 = CharacteristicIndex.of(partIndex, 2)
			def characteristic_3 = CharacteristicIndex.of(partIndex, 3)
			def characteristic_4 = CharacteristicIndex.of(partIndex, 4)
			def characteristic_5 = CharacteristicIndex.of(partIndex, 5)
			def characteristic_6 = CharacteristicIndex.of(partIndex, 6)

		when:
			dfqHierarchy.removeHierarchyForGroup(GroupIndex.of(1, 1))

		then:
			dfqHierarchy.nodeDefinitions.size() == 2
			dfqHierarchy.nodeBindings[partNodeIndex].size() == 1
			dfqHierarchy.nodeBindings[characteristic_2_nodeIndex].size() == 2
			dfqHierarchy.nodeBindings[group_2_nodeIndex] == null
			dfqHierarchy.getParentIndex(characteristic_3).get() == characteristic_2
			dfqHierarchy.getParentIndex(characteristic_4).get() == characteristic_2
			dfqHierarchy.getParentIndex(characteristic_5).isPresent() == false
			dfqHierarchy.getParentIndex(characteristic_6).isPresent() == false
	}

	def parse(String dfq) {
		def model = new AqdefParser().parse(dfq)
		return model.hierarchy
	}

}
