package cz.diribet.aqdef.model;

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import cz.diribet.aqdef.KKey
import spock.lang.Specification

class SimpleAqdefHierarchyTest extends Specification {

	def "characteristic hierarchy with 1 level" () {
		given:
			CharacteristicIndex parentIndex = CharacteristicIndex.of(1, 1)
			CharacteristicIndex childIndex = CharacteristicIndex.of(1, 2)

			AqdefHierarchy dfqHierarchy = new AqdefHierarchy()

			def input = [
				new EntryRow("K2030", 1, 1),
				new EntryRow("K2031", 2, 1)
			]

		when:
			input.each {
				dfqHierarchy.putEntry(it.key, it.index, it.value)
			}

		then:
			dfqHierarchy.nodeDefinitions.size() == 1
			dfqHierarchy.nodeBindings.size() == 1
			dfqHierarchy.getParentIndex(childIndex).get() == parentIndex
	}

	def "characteristic hierarchy with 2 levels" () {
		given:
			CharacteristicIndex parentLevel0Index = CharacteristicIndex.of(1, 1)
			CharacteristicIndex parentLevel1Index = CharacteristicIndex.of(1, 2)
			CharacteristicIndex childLevel1Index = CharacteristicIndex.of(1, 2)
			CharacteristicIndex childLevel2Index = CharacteristicIndex.of(1, 3)

			AqdefHierarchy dfqHierarchy = new AqdefHierarchy()

			def input = [
				new EntryRow("K2030", 1, 1),
				new EntryRow("K2030", 2, 2),
				new EntryRow("K2031", 2, 1),
				new EntryRow("K2031", 3, 2)
			]

		when:
			input.each {
				dfqHierarchy.putEntry(it.key, it.index, it.value)
			}

		then:
			dfqHierarchy.nodeDefinitions.size() == 2
			dfqHierarchy.nodeBindings.size() == 2
			dfqHierarchy.getParentIndex(childLevel1Index).get() == parentLevel0Index
			dfqHierarchy.getParentIndex(childLevel2Index).get() == parentLevel1Index
	}

	def "mixed hierarchy types throws a runtime exception" () {
		given:
			AqdefHierarchy dfqHierarchy = new AqdefHierarchy()

			def input = [
				new EntryRow("K2030", 1, 1),
				new EntryRow("K2031", 2, 1),
				new EntryRow("K5112", 1, 1)
			]

		when:
			input.each {
				dfqHierarchy.putEntry(it.key, it.index, it.value)
			}

		then:
			thrown(RuntimeException)
	}

	class EntryRow {

		def key;
		def index;
		def value;

		private EntryRow(def key, def index, def value) {
			this.key = KKey.of(key);
			this.index = index;
			this.value = value;
		}

	}

}
