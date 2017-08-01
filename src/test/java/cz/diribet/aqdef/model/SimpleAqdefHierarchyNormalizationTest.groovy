package cz.diribet.aqdef.model;

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import cz.diribet.aqdef.parser.AqdefParser
import spock.lang.Specification

/**
 * @author Honza Krakora
 *
 */
class SimpleAqdefHierarchyNormalizationTest extends Specification {

	static def CHARACTERISTIC_FLAT = """
		K0100 2
		K1001 part_1
		K2001/1 characteristic_1
		K2001/2 characteristic_2
	"""

	static def CHARACTERISTIC_LEVEL_1 = """
		K0100 3
		K1001 part_1
		K2001/1 characteristic_1
		K2030/1 1
		K2031/1
		K2001/2 characteristic_2
		K2030/2
		K2031/2 1
		K2001/3 characteristic_3
		K2030/3
		K2031/3
	"""

	def "characteristic flat hierarchy is empty" () {
		given:
			AqdefObjectModel aqdefObjectModel = parse(CHARACTERISTIC_FLAT)
			AqdefHierarchy hierarchy = aqdefObjectModel.getHierarchy()

		when:
			hierarchy = hierarchy.normalize(aqdefObjectModel)

		then:
			hierarchy.isEmpty()
	}

	def "characteristic hierarchy with level 1" () {
		given:
			AqdefObjectModel aqdefObjectModel = parse(CHARACTERISTIC_LEVEL_1)
			AqdefHierarchy hierarchy = aqdefObjectModel.getHierarchy()

			NodeIndex partNodeIndex = NodeIndex.of(1)
			NodeIndex characteristic_1_nodeIndex = NodeIndex.of(2)

			CharacteristicIndex characteristic_1 = CharacteristicIndex.of(1, 1)
			CharacteristicIndex characteristic_2 = CharacteristicIndex.of(1, 2)
			CharacteristicIndex characteristic_3 = CharacteristicIndex.of(1, 3)

		when:
			hierarchy = hierarchy.normalize(aqdefObjectModel)

		then:
			hierarchy.nodeDefinitions.size() == 2
			hierarchy.getParentIndex(characteristic_2).get() == characteristic_1
			hierarchy.getParentNodeIndexOfCharacteristic(characteristic_2).get() == characteristic_1_nodeIndex
			hierarchy.getParentNodeIndexOfCharacteristic(characteristic_3).get() == partNodeIndex
	}

	def parse(String dfq) {
		def parser = new AqdefParser()
		return parser.parse(dfq)
	}

}
