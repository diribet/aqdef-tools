package cz.diribet.aqdef.model.builder;

import static org.junit.Assert.*

import spock.lang.Specification

class AqdefHierarchyBuilderTest extends Specification {

	def "valid hierarchy is written"() {

		when:
			AqdefHierarchyBuilder builder = new AqdefHierarchyBuilder()

			builder.createHierarchyNodeOfPart(1, 1)
			builder.createHierarchyNodeOfCharacteristic(2, 1, 1, 1, null)
			builder.createHierarchyNodeOfCharacteristic(3, 1, 2, 2, null)
			builder.createHierarchyNodeOfCharacteristic(4, 1, 3, 3, 2)

			def hierarchy = builder.getHierarchy()

		then:
			!hierarchy.isEmpty()

	}

	def "invalid hierarchy is not written"() {

		when:
			AqdefHierarchyBuilder builder = new AqdefHierarchyBuilder()

			builder.createHierarchyNodeOfPart(1, 1)
			builder.createHierarchyNodeOfCharacteristic(2, 1, 1, 1, null)
			builder.createHierarchyNodeOfCharacteristic(3, 1, 2, 2, null)
			builder.createHierarchyNodeOfCharacteristic(4, 1, 3, 3, 8)

			def hierarchy = builder.getHierarchy()

		then:
			hierarchy.isEmpty()

	}
}