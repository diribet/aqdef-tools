package cz.diribet.aqdef.writer

import cz.diribet.aqdef.model.builder.AqdefObjectModelBuilder
import spock.lang.Specification

import java.text.SimpleDateFormat

class AqdefWriterMultiplePartsTest extends Specification {

	def "two parts, each with single characteristic and value"() {
		def expectedContent = getClass().getResourceAsStream("multipleParts.dfq").text

		def valueDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse("1.1.2025 00:00:00")

		def builder = new AqdefObjectModelBuilder()

		builder.createPartEntry("K1001", "part 1")
		builder.createCharacteristicEntry("K2001", "characteristic 1")
		builder.createValueEntry("K0001", 7.1)
		builder.createValueEntry("K0004", valueDate)

		builder.nextPart()
		builder.nextCharacteristic()
		builder.nextValue()

		builder.createPartEntry("K1001", "part 2")
		builder.createCharacteristicEntry("K2001", "characteristic 2")
		builder.createValueEntry("K0001", 7.2)
		builder.createValueEntry("K0004", valueDate)

		def objectModel = builder.build()

		when:
			def content = new AqdefWriter().writeToString(objectModel)

		then:
			content == expectedContent
	}

}
