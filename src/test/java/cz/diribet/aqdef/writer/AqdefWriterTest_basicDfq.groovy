package cz.diribet.aqdef.writer

import java.text.SimpleDateFormat

import cz.diribet.aqdef.model.builder.AqdefObjectModelBuilder
import spock.lang.Specification

class AqdefWriterTest_basicDfq extends Specification {

	final static BASE_DATE_FOR_VALUES = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse("1.1.2013 15:18:31")

	def createDfqObjectModel(partTemplates) {
		def builder = new AqdefObjectModelBuilder()

		for (partTemplate in partTemplates) {
			def partIndex = partTemplate.partIndex

			builder.createPartEntry("K1000", partIndex)
			builder.createPartEntry("K1001", "<part_number_${partIndex}>".toString())
			builder.createPartEntry("K1002", "<part_title_${partIndex}>".toString())
			builder.createPartEntry("K1082", "<machine_title${partIndex}>".toString())

			for (characteristicTemplate in partTemplate.characteristics) {
				def characteristicIndex = characteristicTemplate.characteristicIndex

				builder.createCharacteristicEntry("K2000", characteristicIndex)
				builder.createCharacteristicEntry("K2001", "<characteristic_code_${characteristicIndex}>".toString())
				builder.createCharacteristicEntry("K2110", characteristicIndex * 1.0)
				builder.createCharacteristicEntry("K2111", characteristicIndex * 2.0)
				def nominal = characteristicIndex * 1.5
				builder.createCharacteristicEntry("K2101", nominal)

				if (characteristicTemplate.numberOfValues > 0) {
					for (valueIndex in 1..characteristicTemplate.numberOfValues) {
						builder.createValueEntry("K0001", nominal + (valueIndex / 10))
						builder.createValueEntry("K0004", BASE_DATE_FOR_VALUES + (valueIndex - 1))
						builder.nextValue()
					}
				}

				builder.nextCharacteristic()
			}

			builder.nextPart()
		}

		return builder.build()
	}

	def "create DFQ for single part with 3 characteristics"() {
		when:
			def objectModel =
				createDfqObjectModel([[
									partIndex: 1,
									characteristics:[
											[characteristicIndex: 1, numberOfValues: 3],
											[characteristicIndex: 2, numberOfValues: 3],
											[characteristicIndex: 3, numberOfValues: 3]
										]
									]])
			def content = new AqdefWriter().writeToString(objectModel)
			def expectedContent = getClass().getResourceAsStream("basicDfq_singlePart_threeCharacteristics.dfq").text

		then:
			content == expectedContent
	}

	def "create DFQ for 3 parts each with different number of characteristics"() {
		when:
			def objectModel =
				createDfqObjectModel([[
									partIndex: 1,
									characteristics:[
											[characteristicIndex: 1, numberOfValues: 8]
										]
									],[
									partIndex: 2,
									characteristics:[
											[characteristicIndex: 1, numberOfValues: 1],
											[characteristicIndex: 2, numberOfValues: 1],
											[characteristicIndex: 3, numberOfValues: 1]
										]
									],[
									partIndex: 3,
									characteristics:[
											[characteristicIndex: 1, numberOfValues: 3],
											[characteristicIndex: 2, numberOfValues: 3],
											[characteristicIndex: 3, numberOfValues: 3],
											[characteristicIndex: 4, numberOfValues: 3],
											[characteristicIndex: 5, numberOfValues: 3]
										]
									]])

			def content = new AqdefWriter().writeToString(objectModel)
			def expectedContent = getClass().getResourceAsStream("basicDfq_threeParts_differentNumberOfCharacteristics.dfq").text
		then:
			content == expectedContent

	}

	def "create DFQ for single part without characteristics and values"() {
		when:
			def objectModel =
				createDfqObjectModel([[
									partIndex: 1,
									characteristics:[]
									]])

			def content = new AqdefWriter().writeToString(objectModel)
			def expectedContent = getClass().getResourceAsStream("basicDfq_singlePart_noCharacteristics.dfq").text
		then:
			content == expectedContent

	}

	def "create DFQ for single part with single characteristic without values "() {
		when:
			def objectModel =
				createDfqObjectModel([[
									partIndex: 1,
									characteristics:[
											[characteristicIndex: 1, numberOfValues: 0]
										]
									]])

			def content = new AqdefWriter().writeToString(objectModel)
			def expectedContent = getClass().getResourceAsStream("basicDfq_singlePart_singleCharacteristic_noValues.dfq").text
		then:
			content == expectedContent

	}

}
