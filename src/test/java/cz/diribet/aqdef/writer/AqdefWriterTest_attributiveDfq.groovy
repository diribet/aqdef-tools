package cz.diribet.aqdef.writer

import java.text.SimpleDateFormat

import cz.diribet.aqdef.model.builder.AqdefObjectModelBuilder
import spock.lang.Specification

class AqdefWriterTest_attributiveDfq extends Specification {

	final static BASE_DATE_FOR_VALUES = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse("1.1.2013 15:18:31")

	def "create DFQ with single attributive characteristic and sigle error log sheet characteristic"() {
		given:
			def builder = new AqdefObjectModelBuilder()

			def partIndex = 1

			builder.createPartEntry("K1000", partIndex)
			builder.createPartEntry("K1001", "<part_number_${partIndex}>".toString())
			builder.createPartEntry("K1002", "<part_title_${partIndex}>".toString())


			def characteristicIndex = 1;
			builder.createCharacteristicEntry("K2000", characteristicIndex)
			builder.createCharacteristicEntry("K2001", "<characteristic_code_${characteristicIndex}>".toString())
			builder.createCharacteristicEntry("K2004", 1) // attribute characteristic type

			builder.createValueEntry("K0020", 1)
			builder.createValueEntry("K0021", 1)
			builder.createValueEntry("K0004", BASE_DATE_FOR_VALUES)

			builder.nextCharacteristic()

			characteristicIndex++;

			builder.createCharacteristicEntry("K2000", characteristicIndex)
			builder.createCharacteristicEntry("K2001", "<characteristic_code_${characteristicIndex}>".toString())
			builder.createCharacteristicEntry("K2004", 6) // error log sheet characteristic type

			builder.createValueEntry("K0020", 155)
			builder.createValueEntry("K0021", 8)
			builder.createValueEntry("K0004", BASE_DATE_FOR_VALUES)

			def objectModel = builder.build()

		when:
			def content = new AqdefWriter().writeToString(objectModel)
			def expectedContent = getClass().getResourceAsStream("attributiveDfq_attributiveCharacteristic_and_errorLogSheet.dfq").text

		then:
			content == expectedContent
	}

}
