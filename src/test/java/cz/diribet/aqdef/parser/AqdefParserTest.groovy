package cz.diribet.aqdef.parser;

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import cz.diribet.aqdef.model.AqdefObjectModel
import cz.diribet.aqdef.model.CharacteristicIndex
import cz.diribet.aqdef.model.NodeIndex
import cz.diribet.aqdef.model.PartIndex
import cz.diribet.aqdef.model.AqdefObjectModel.CharacteristicEntries
import cz.diribet.aqdef.model.AqdefObjectModel.PartEntries
import cz.diribet.aqdef.model.AqdefObjectModel.ValueEntries
import spock.lang.Specification

class AqdefParserTest extends Specification {

	def "K-key entries of type String are parsed correctly" () {
		when:
			AqdefObjectModel model = parse(dfqWithString)
			PartEntries entries = model.getPartEntries(1)

		then:
			entries.getValue("K1001") == "part"
	}

	def "K-key entries of type Integer are parsed correctly" () {
		when:
			AqdefObjectModel model = parse(dfqWithInteger)
			PartEntries entries = model.getPartEntries(1)

		then:
			entries.getValue("K1010") == 1i
	}

	def "K-key entries of type Date are parsed correctly" () {
		when:
			AqdefObjectModel model = parse(dfqWithDate)
			ValueEntries entries = model.getValueEntries(1, 1, 1)

		then:
			entries.getValue("K0004") == Date.parse("dd.MM.yyyy HH:mm:ss", "1.1.2014 10:30:59")
	}

	def "empty K-key entries are parsed correctly" () {
		when:
			AqdefObjectModel model = parse(dfqWithEmptyKeys)
			PartEntries entries = model.getPartEntries(1)

		then:
			entries.getValue("K1001") == "part"
			entries.getValue("K1002") == null
			entries.getValue("K1003") == null
	}

	def "non-indexed entries are parsed correctly" () {
		when:
			AqdefObjectModel model = parse(dfqWithUnindexedPart)
			PartEntries entries = model.getPartEntries(1)

		then:
			entries != null
			entries.getValue("K1001") == "part"
			entries.getValue("K1002") == "title"
	}

	def "/0 part entries are applied to all parts" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllParts)

			PartEntries entries1 = model.getPartEntries(1)
			PartEntries entries2 = model.getPartEntries(1)

			PartEntries entries0 = model.getPartEntries(0)

		then:
			entries1.getValue("K1002") == "common title"
			entries2.getValue("K1002") == "common title"
			entries0 == null
	}

	def "/0 characteristic entries are applied to all characteristics" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllCharacteristics)

			CharacteristicEntries entries1 = model.getCharacteristicEntries(1, 1)
			CharacteristicEntries entries2 = model.getCharacteristicEntries(1, 2)

			CharacteristicEntries entries0 = model.getCharacteristicEntries(1, 0)

		then:
			entries1.getValue("K2002") == "common title"
			entries2.getValue("K2002") == "common title"
			entries0 == null
	}

	def "/0 characteristic entries are applied to all characteristics when there are multiple parts" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllCharacteristicsOnMultipleParts)

			CharacteristicEntries part1char1 = model.getCharacteristicEntries(1, 1)
			CharacteristicEntries part1char2 = model.getCharacteristicEntries(1, 2)

			CharacteristicEntries part2char1 = model.getCharacteristicEntries(2, 3)
			CharacteristicEntries part2char2 = model.getCharacteristicEntries(2, 4)

			CharacteristicEntries part1char0 = model.getCharacteristicEntries(1, 0)
			CharacteristicEntries part2char0 = model.getCharacteristicEntries(2, 0)

		then:
			part1char1.getValue("K2002") == "common title part2"
			part1char2.getValue("K2002") == "common title part2"
			part2char1.getValue("K2002") == "common title part2"
			part2char2.getValue("K2002") == "common title part2"
			part1char0 == null
			part2char0 == null
	}

	def "/0 characteristic entries are applied to all characteristics of single part when there are multiple parts" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllCharacteristicsOnOneOfMultipleParts)

			CharacteristicEntries part1char1 = model.getCharacteristicEntries(1, 1)
			CharacteristicEntries part1char2 = model.getCharacteristicEntries(1, 2)

			CharacteristicEntries part2char1 = model.getCharacteristicEntries(2, 3)
			CharacteristicEntries part2char2 = model.getCharacteristicEntries(2, 4)

		then:
			part1char1.getValue("K2002") == "common title"
			part1char2.getValue("K2002") == "common title"
			part2char1.getValue("K2002") == "common title"
			part2char2.getValue("K2002") == "common title"
	}

	def "/0 value entry is applied to first value set when the common entry is written before values" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllValuesBeforeValue)

			ValueEntries entries1 = model.getValueEntries(1, 1, 1)
			ValueEntries entries2 = model.getValueEntries(1, 1, 2)

			ValueEntries entries0 = model.getValueEntries(1, 0, 0)

		then:
			entries1.getValue("K0014") == "identifier"
			entries2.getValue("K0014") == null
			entries0 == null
	}

	def "/0 value entry is applied to a single value" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToSingleValueSet)

			ValueEntries entries1 = model.getValueEntries(1, 1, 1)
			ValueEntries entries2 = model.getValueEntries(1, 1, 2)

			ValueEntries entries0 = model.getValueEntries(1, 0, 0)

		then:
			entries1.getValue("K0014") == "identifier"
			entries2.getValue("K0014") == null
			entries0 == null
	}

	def "/0 value entry is applied to multiple values" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToEachValueSet)

			ValueEntries entries1 = model.getValueEntries(1, 1, 1)
			ValueEntries entries2 = model.getValueEntries(1, 1, 2)

			ValueEntries entries0 = model.getValueEntries(1, 0, 0)

		then:
			entries1.getValue("K0014") == "identifier 1"
			entries2.getValue("K0014") == "identifier 2"
			entries0 == null
	}

	def "/0 value entries are applied to multiple values" () {
		when:
			AqdefObjectModel model = parse(dfqWithMultipleKeysAppliedToAllValues)

			ValueEntries char1Value1 = model.getValueEntries(1, 1, 1)
			ValueEntries char1Value2 = model.getValueEntries(1, 1, 2)

			ValueEntries char2Value1 = model.getValueEntries(1, 2, 1)
			ValueEntries char2Value2 = model.getValueEntries(1, 2, 2)

			ValueEntries entries0 = model.getValueEntries(1, 0, 0)

		then:
			char1Value1.getValue("K0004") == Date.parse("dd.MM.yyyy HH:mm:ss", "1.1.2014 10:30:59")
			char1Value1.getValue("K0010") == 153i
			char1Value1.getValue("K0014") == "common identifier"

			char1Value2.getValue("K0004") == Date.parse("dd.MM.yyyy HH:mm:ss", "1.1.2014 10:31:00")
			char1Value2.getValue("K0010") == 154i
			char1Value2.getValue("K0014") == "common identifier 2"

			char2Value1.getValue("K0004") == Date.parse("dd.MM.yyyy HH:mm:ss", "1.1.2014 10:30:59")
			char2Value1.getValue("K0010") == 153i
			char2Value1.getValue("K0014") == "common identifier"

			char2Value2.getValue("K0004") == Date.parse("dd.MM.yyyy HH:mm:ss", "1.1.2014 10:31:00")
			char2Value2.getValue("K0010") == 154i
			char2Value2.getValue("K0014") == "common identifier 2"

			entries0 == null
	}

	def "/0 entries don't overwrites explicitly defined entries" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllPartsSomeDefinedExplicitly)

			PartEntries entries1 = model.getPartEntries(1)
			PartEntries entries2 = model.getPartEntries(2)

		then:
			entries1.getValue("K1002") == "explicit title"
			entries2.getValue("K1002") == "common title"
	}

	def "values in binary format are parsed correctly - values are at the end" () {
		when:
			AqdefObjectModel model = parse(dfqWithTwoPartsWithBinaryValuesAtTheEnd)

			def values1 = model.getValueEntries(CharacteristicIndex.of(1, 1))
			def values2 = model.getValueEntries(CharacteristicIndex.of(2, 2))

		then:
			values1.size() == 2
			values2.size() == 2
	}

	def "values in binary format are parsed correctly - values are after each part" () {
		when:
			AqdefObjectModel model = parse(dfqWithTwoPartsWithBinaryValuesAfterEachPart)

			def values1 = model.getValueEntries(CharacteristicIndex.of(1, 1))
			def values2 = model.getValueEntries(CharacteristicIndex.of(2, 2))

		then:
			values1.size() == 2
			values2.size() == 0
	}

	def "values in K-key format are parsed correctly - values are at the end" () {
		when:
			AqdefObjectModel model = parse(dfqWithTwoPartsWithBinaryValuesAtTheEnd)

			def values1 = model.getValueEntries(CharacteristicIndex.of(1, 1))
			def values2 = model.getValueEntries(CharacteristicIndex.of(2, 2))

		then:
			values1.size() == 2
			values2.size() == 2
	}

	def "values in K-key format are parsed correctly - values are after each part" () {
		when:
			AqdefObjectModel model = parse(dfqWithTwoPartsWithValuesAfterEachPart)

			def values1 = model.getValueEntries(CharacteristicIndex.of(1, 1))
			def values2 = model.getValueEntries(CharacteristicIndex.of(2, 2))

		then:
			values1.size() == 2
			values2.size() == 2
	}

	def "error is thrown when there are values for non-existing characteristic" () {
		when:
			AqdefObjectModel model = parse(dfqWithMoreValuesThanCharacteristics)

		then:
			thrown(Exception)
	}

	def "there are no /0 entries after aqdef object model normalization" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllPartsCharacteristicsAndValues)

			def parts = model.partEntries[PartIndex.of(0)]
			def characteristics = model.characteristicEntries.get(PartIndex.of(0))
			def values = model.valueEntries[PartIndex.of(0)]

		then:
			parts == null
			characteristics == null
			values == null
	}

	def "hierarchy si parsed correctly" () {
		when:
			AqdefObjectModel model = parse(dfqWithHierarchy)

			def nodeDefinitions = model.hierarchy.nodeDefinitions
			def partBindings = model.hierarchy.nodeBindings.get(NodeIndex.of(1))
			def characteristicBindings = model.hierarchy.nodeBindings.get(NodeIndex.of(3))
			def groupBindings = model.hierarchy.nodeBindings.get(NodeIndex.of(8))

		then:
			nodeDefinitions.size() == 8
			partBindings.size() == 3
			characteristicBindings.size() == 2
			groupBindings.size() == 2
	}

	def "hierarchy could be parsed even when there are both empty simple hierarchy and normal hierarchy" () {
		when:
			AqdefObjectModel model = parse(dfqWithEmptySimpleAndNormalHierarchy)

			def nodeDefinitions = model.hierarchy.nodeDefinitions
			def partBindings = model.hierarchy.nodeBindings.get(NodeIndex.of(1))

		then:
			nodeDefinitions.size() == 3
			partBindings.size() == 2
			model.hierarchy.nodeBindings.size() == 1
	}

	def "hierarchy could not be parsed when there are both simple and normal hierarchy (both non-empty)" () {
		when:
			AqdefObjectModel model = parse(dfqWithSimpleAndNormalHierarchy)

		then:
			thrown(RuntimeException)
	}


	def parse(String dfq) {
		def parser = new AqdefParser()
		return parser.parse(dfq)
	}

	def dfqWithString = """
		K0100 1
		K1001/1 part
	"""

	def dfqWithInteger = """
		K0100 1
		K1010/1 1
	"""

	def dfqWithDate = """
		K0100 1
		K1001/1 part
		K2001/1 characteristic
		K0001/1 1
		K0004/1 1.1.2014/10:30:59
	"""

	def dfqWithEmptyKeys = """
		K0100 1
		K1001/1 part
		K1002
		K1003/1
	"""

	def dfqWithUnindexedPart = """
		K0100 1
		K1001 part
		K1002 title
	"""

	def dfqWithKeyAppliedToAllParts = """
		K0100 2
		K1002/0 common title
		K1001/1 part1
		K1001/2 part2
	"""

	def dfqWithKeyAppliedToAllCharacteristics = """
		K0100 2
		K1001/1 part
		K2002/0 common title
		K2001/1 characteristic1
		K2001/2 characteristic2
	"""

	def dfqWithKeyAppliedToAllCharacteristicsOnMultipleParts = """
		K0100 4
		K1001/1 part1
		K2002/0 common title part1
		K2001/1 part1 - characteristic1
		K2001/2 part1 - characteristic2

		K1001/2 part2
		K2002/0 common title part2
		K2001/3 part2 - characteristic1
		K2001/4 part2 - characteristic2
	"""

	def dfqWithKeyAppliedToAllCharacteristicsOnOneOfMultipleParts = """
		K0100 4
		K1001/1 part1
		K2002/0 common title
		K2001/1 part1 - characteristic1
		K2001/2 part1 - characteristic2

		K1001/2 part2
		K2001/3 part2 - characteristic1
		K2001/4 part2 - characteristic2
	"""

	/**
	 * This behavior is not supported by qs-STAT but we can read such AQDEF.
	 */
	def dfqWithKeyAppliedToAllValuesBeforeValue = """
		K0100 1
		K1001/1 part
		K2001/1 characteristic1
		K0014/0 identifier
		K0001/1 1
		K0001/1 2
	"""

	def dfqWithKeyAppliedToSingleValueSet = """
		K0100 1
		K1001/1 part
		K2001/1 characteristic1
		K0001/1 1
		K0014/0 identifier
		K0001/1 2
	"""

	def dfqWithKeyAppliedToEachValueSet = """
		K0100 1
		K1001/1 part
		K2001/1 characteristic1
		K0001/1 1
		K0014/0 identifier 1
		K0001/1 2
		K0014/0 identifier 2
	"""

	def dfqWithMultipleKeysAppliedToAllValues = """
		K0100 2
		K1001/1 part
		K2001/1 characteristic1
		K2001/2 characteristic2
		K0001/1 1
		K0001/2 3
		K0004/0 1.1.2014/10:30:59
		K0010/0 153
		K0014/0 common identifier
		K0001/1 2
		K0001/2 4
		K0004/0 1.1.2014/10:31:00
		K0010/0 154
		K0014/0 common identifier 2
	"""

	def dfqWithKeyAppliedToAllPartsSomeDefinedExplicitly = """
		K0100 2
		K1002/0 common title
		K1001/1 part1
		K1002/1 explicit title
		K1001/2 part2
	"""

	def dfqWithTwoPartsWithBinaryValuesAtTheEnd = """
		K0100 2
		K1001/1 part1
		K2001/1 characteristic1
		K2004/0 0
		K1001/2 part2
		K2001/2 characteristic2
		1001.01.2014/00:00:002001.01.2014/00:00:00
		1.1001.01.2014/00:00:002.1001.01.2014/00:00:00
	"""

	/**
	 * Both values are for characteristic1 of part1.
	 * (value sets could be written after each part but all characteristics are always considered when assigning values to characteristics)
	 */
	def dfqWithTwoPartsWithBinaryValuesAfterEachPart = """
		K0100 2
		K1001/1 part1
		K2001/1 characteristic1
		K2004/0 0
		1001.01.2014/00:00:00
		K1001/2 part2
		K2001/2 characteristic2
		2001.01.2014/00:00:00
	"""

	def dfqWithTwoPartsWithValuesAtTheEnd = """
		K0100 2
		K1001/1 part1
		K2001/1 characteristic1
		K2004/0 0
		K1001/2 part2
		K2001/2 characteristic2
		K0001/1 1
		K0001/1 1.1
		K0001/2 2
		K0001/2 2.1
	"""

	def dfqWithTwoPartsWithValuesAfterEachPart = """
		K0100 2
		K1001/1 part1
		K2001/1 characteristic1
		K2004/0 0
		K0001/1 1
		K0001/1 1.1
		K1001/2 part2
		K2001/2 characteristic2
		K0001/2 2
		K0001/2 2.1
	"""

	/**
	 * Values are for 3 characteristics, but only 2 are defined
	 */
	def dfqWithMoreValuesThanCharacteristics = """
		K0100 2
		K1001/1 part1
		K2001/1 characteristic1
		K2001/2 characteristic2
		1001.01.2014/00:00:002001.01.2014/00:00:003001.01.2014/00:00:00
	"""

	def dfqWithKeyAppliedToAllPartsCharacteristicsAndValues = """
		K0100 2
		K1001/0 common title
		K1001/1 part
		K2002/0 common title
		K2001/1 characteristic
		K0014/0 common identifier
		K0001/1 1
	"""

	def dfqWithHierarchy = """
		K0100 2
		K1001/1 part
		K2001/1 characteristic 1
		K2001/2 characteristic 2
		K2001/3 characteristic 3
		K2001/4 characteristic 4
		K2001/5 characteristic 5
		K2001/6 characteristic 6
		K5001/1 group 1
		K5001/1 group 1
		K5111/1 1
		K5112/2 1
		K5112/3 2
		K5112/4 3
		K5112/5 4
		K5112/6 5
		K5112/7 6
		K5113/8 1
		K5102/1 1
		K5103/1 3
		K5102/3 3
		K5102/3 4
		K5103/1 8
		K5102/8 5
		K5102/8 6
	"""

	def dfqWithEmptySimpleAndNormalHierarchy = """
		K0100 2
		K1001/1 part
		K2001/1 characteristic 1
		K2030/1 0
		K2031/1 0
		K2001/2 characteristic 2
		K2030/2 0
		K2031/2 0
		K5111/1 1
		K5112/2 1
		K5112/3 2
		K5102/1 1
		K5102/1 2
	"""

	def dfqWithSimpleAndNormalHierarchy = """
		K0100 2
		K1001/1 part
		K2001/1 characteristic 1
		K2030/1 1
		K2031/1 0
		K2001/2 characteristic 2
		K2030/2 0
		K2031/2 1
		K5111/1 1
		K5112/2 1
		K5112/3 2
		K5102/1 1
		K5102/1 2
	"""
}
