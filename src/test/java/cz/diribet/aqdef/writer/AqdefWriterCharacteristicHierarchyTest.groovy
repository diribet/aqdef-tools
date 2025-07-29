package cz.diribet.aqdef.writer

import java.text.SimpleDateFormat

import cz.diribet.aqdef.model.builder.AqdefObjectModelBuilder
import spock.lang.Ignore
import spock.lang.Specification

class AqdefWriterCharacteristicHierarchyTest extends Specification {
	final static BASE_DATE_FOR_VALUES = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse("1.1.2013 15:18:31")

	def createDfqObjectModel(partTemplates) {
		def builder = new AqdefObjectModelBuilder()

		for (partTemplate in partTemplates) {
			def partIndex = partTemplate.partIndex

			builder.createPartEntry("K1000", partIndex)
			builder.createPartEntry("K1001", "<part_number_${partIndex}>".toString())
			builder.createPartEntry("K1002", "<part_title_${partIndex}>".toString())

			builder.createHierarchyNodeOfPart()

			for (characteristicTemplate in partTemplate.characteristics) {
				def characteristicIndex = characteristicTemplate.characteristicIndex;

				def isGroup = characteristicTemplate.hierarchyType == 10

				builder.createCharacteristicEntry("K2000", characteristicIndex)

				if (isGroup) {
					builder.createGroupEntry("K5002", "<group_${characteristicIndex}>".toString())
					builder.createHierarchyNodeOfGroup(characteristicIndex, characteristicTemplate.parentCharacteristic)
				} else {
					builder.createCharacteristicEntry("K2001", "<characteristic_code_${characteristicIndex}>".toString())
					builder.createHierarchyNodeOfCharacteristic(characteristicIndex, characteristicTemplate.parentCharacteristic)
				}
				builder.createCharacteristicEntry("K2004", characteristicTemplate.characteristicType)

				if (characteristicTemplate.numberOfValues > 0) {
					for (valueIndex in 1..characteristicTemplate.numberOfValues) {
						builder.createValueEntry("K0001", valueIndex / 10)
						builder.createValueEntry("K0004", BASE_DATE_FOR_VALUES + (valueIndex - 1))
						builder.nextValue()
					}
				}

				if (isGroup) {
					builder.nextGroup()
				} else {
					builder.nextCharacteristic()
				}
			}

			builder.nextPart()
		}

		return builder.build()
	}

	def "create DFQ for single part with single parent and 2 child characteristics"() {
		when:
			def objectModel = createDfqObjectModel([[
												partIndex: 1,
												characteristics:[
														[characteristicIndex: 1, numberOfValues: 1, hierarchyType: 12, parentCharacteristic: 0],
														[characteristicIndex: 2, numberOfValues: 1, hierarchyType: 0, parentCharacteristic: 1],
														[characteristicIndex: 3, numberOfValues: 1, hierarchyType: 0, parentCharacteristic: 1]
													]
												]]);

			def content = new AqdefWriter().writeToString(objectModel)
			def expectedContent = getClass().getResourceAsStream("characteristicHierarchy_singlePart_threeCharacteristics.dfq").text

		then:
			content == expectedContent
	}

	def "create DFQ for single part with single group and 2 child characteristics"() {
		when:
			def objectModel = createDfqObjectModel([[
												partIndex: 1,
												characteristics:[
														[characteristicIndex: 1, numberOfValues: 0, hierarchyType: 10, parentCharacteristic: 0],
														[characteristicIndex: 2, numberOfValues: 1, hierarchyType: 0, parentCharacteristic: 1],
														[characteristicIndex: 3, numberOfValues: 1, hierarchyType: 0, parentCharacteristic: 1]
													]
												]])

			def content = new AqdefWriter().writeToString(objectModel)
			def expectedContent = getClass().getResourceAsStream("characteristicHierarchy_singlePart_singleGroup_twoCharacteristics.dfq").text

		then:
			content == expectedContent
	}

	def "create DFQ for single part with characteristic that has child characteristic and with 2 groups each having single child characteristic"() {
		when:
			def objectModel = createDfqObjectModel([[
												partIndex: 1,
												characteristics:[
														[characteristicIndex: 1, numberOfValues: 1, hierarchyType: 12, parentCharacteristic: 0],
														[characteristicIndex: 2, numberOfValues: 1, hierarchyType: 0, parentCharacteristic: 1],
														[characteristicIndex: 3, numberOfValues: 0, hierarchyType: 10, parentCharacteristic: 0],
														[characteristicIndex: 4, numberOfValues: 1, hierarchyType: 0, parentCharacteristic: 3],
														[characteristicIndex: 5, numberOfValues: 0, hierarchyType: 10, parentCharacteristic: 0],
														[characteristicIndex: 6, numberOfValues: 1, hierarchyType: 0, parentCharacteristic: 5]
													]
												]])

			def content = new AqdefWriter().writeToString(objectModel)
			def expectedContent = getClass().getResourceAsStream("characteristicHierarchy_singlePart_twoChildCharacteristics_twoGroups.dfq").text

		then:
			content == expectedContent
	}

	def "create DFQ for two parts, each with single group and 2 child characteristics"() {
		when:
			def objectModel = createDfqObjectModel([[
				                                        partIndex: 1,
				                                        characteristics:[
					                                        [characteristicIndex: 1, numberOfValues: 0, hierarchyType: 10, parentCharacteristic: 0],
					                                        [characteristicIndex: 2, numberOfValues: 1, hierarchyType: 0, parentCharacteristic: 1],
					                                        [characteristicIndex: 3, numberOfValues: 1, hierarchyType: 0, parentCharacteristic: 1]
				                                        ]
			                                        ],[
				                                        partIndex: 2,
				                                        characteristics:[
					                                        [characteristicIndex: 4, numberOfValues: 0, hierarchyType: 10, parentCharacteristic: 0],
					                                        [characteristicIndex: 5, numberOfValues: 0, hierarchyType: 12, parentCharacteristic: 4],
					                                        [characteristicIndex: 6, numberOfValues: 1, hierarchyType: 0, parentCharacteristic: 5],
					                                        [characteristicIndex: 7, numberOfValues: 1, hierarchyType: 0, parentCharacteristic: 5]
				                                        ]
			                                        ]])

			def content = new AqdefWriter().writeToString(objectModel)
			def expectedContent = getClass().getResourceAsStream("characteristicHierarchy_twoParts_singleGroup_twoCharacteristics.dfq").text

		then:
			content == expectedContent
	}

	//TODO: 2017/07/05 - vlasta: order of characteristics is handled only by our high level DfqObjectModelBuilder
	@Ignore
	def "characteristics have correct order in DFQ file"() {
		when:
			def objectModel = createDfqObjectModel([[
												partIndex: 1,
												characteristics:[
														[characteristicIndex: 1, numberOfValues: 0, characteristicType: 0, hierarchyType: 0, parentCharacteristic: 0],
														[characteristicIndex: 2, numberOfValues: 0, characteristicType: 6, hierarchyType: 12, parentCharacteristic: 0],
														[characteristicIndex: 3, numberOfValues: 0, characteristicType: 5, hierarchyType: 0, parentCharacteristic: 2],
														[characteristicIndex: 4, numberOfValues: 0, characteristicType: 0, hierarchyType: 0, parentCharacteristic: 0],
														[characteristicIndex: 5, numberOfValues: 0, characteristicType: 0, hierarchyType: 10, parentCharacteristic: 0],
														[characteristicIndex: 6, numberOfValues: 0, characteristicType: 0, hierarchyType: 0, parentCharacteristic: 5],
														[characteristicIndex: 7, numberOfValues: 0, characteristicType: 0, hierarchyType: 10, parentCharacteristic: 0],
														[characteristicIndex: 8, numberOfValues: 0, characteristicType: 0, hierarchyType: 0, parentCharacteristic: 7]
													]
												]])

			def content = new AqdefWriter().writeToString(objectModel)
			def expectedContent = getClass().getResourceAsStream("characteristicHierarchy_characteristicOrder.dfq").text

		then:
			content == expectedContent
	}

}
