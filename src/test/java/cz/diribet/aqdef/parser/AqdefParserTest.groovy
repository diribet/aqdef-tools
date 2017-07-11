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

	def "spravne se parsuji klice typu String" () {
		when:
			AqdefObjectModel model = parse(dfqWithString)
			PartEntries entries = model.getPartEntries(1)

		then:
			entries.getValue("K1001") == "dil"
	}

	def "spravne se parsuji klice typu Integer" () {
		when:
			AqdefObjectModel model = parse(dfqWithInteger)
			PartEntries entries = model.getPartEntries(1)

		then:
			entries.getValue("K1010") == 1i
	}

	def "spravne se parsuji klice typu Date" () {
		when:
			AqdefObjectModel model = parse(dfqWithDate)
			ValueEntries entries = model.getValueEntries(1, 1, 1)

		then:
			entries.getValue("K0004") == Date.parse("dd.MM.yyyy HH:mm:ss", "1.1.2014 10:30:59")
	}

	def "spravne se parsuji prazdne klice" () {
		when:
			AqdefObjectModel model = parse(dfqWithEmptyKeys)
			PartEntries entries = model.getPartEntries(1)

		then:
			entries.getValue("K1001") == "dil"
			entries.getValue("K1002") == null
			entries.getValue("K1003") == null
	}

	def "spravne se parsuji neindexovane klice" () {
		when:
			AqdefObjectModel model = parse(dfqWithUnindexedPart)
			PartEntries entries = model.getPartEntries(1)

		then:
			entries != null
			entries.getValue("K1001") == "dil"
			entries.getValue("K1002") == "popis"
	}

	def "spravne preneseni /0 klicu na vsechny dily" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllParts)

			PartEntries entries1 = model.getPartEntries(1)
			PartEntries entries2 = model.getPartEntries(1)

			PartEntries entries0 = model.getPartEntries(0)

		then:
			entries1.getValue("K1002") == "spolecny popis"
			entries2.getValue("K1002") == "spolecny popis"
			entries0 == null
	}

	def "spravne preneseni /0 klicu na vsechny znaky" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllCharacteristics)

			CharacteristicEntries entries1 = model.getCharacteristicEntries(1, 1)
			CharacteristicEntries entries2 = model.getCharacteristicEntries(1, 2)

			CharacteristicEntries entries0 = model.getCharacteristicEntries(1, 0)

		then:
			entries1.getValue("K2002") == "spolecny popis"
			entries2.getValue("K2002") == "spolecny popis"
			entries0 == null
	}

	def "spravne preneseni /0 klicu na vsechny znaky u vice dilu" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllCharacteristicsOnMultipleParts)

			CharacteristicEntries part1char1 = model.getCharacteristicEntries(1, 1)
			CharacteristicEntries part1char2 = model.getCharacteristicEntries(1, 2)

			CharacteristicEntries part2char1 = model.getCharacteristicEntries(2, 3)
			CharacteristicEntries part2char2 = model.getCharacteristicEntries(2, 4)

			CharacteristicEntries part1char0 = model.getCharacteristicEntries(1, 0)
			CharacteristicEntries part2char0 = model.getCharacteristicEntries(2, 0)

		then:
			part1char1.getValue("K2002") == "spolecny popis dil2"
			part1char2.getValue("K2002") == "spolecny popis dil2"
			part2char1.getValue("K2002") == "spolecny popis dil2"
			part2char2.getValue("K2002") == "spolecny popis dil2"
			part1char0 == null
			part2char0 == null
	}

	def "spravne preneseni /0 klicu na vsechny znaky jendoho z vice dilu" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllCharacteristicsOnOneOfMultipleParts)

			CharacteristicEntries part1char1 = model.getCharacteristicEntries(1, 1)
			CharacteristicEntries part1char2 = model.getCharacteristicEntries(1, 2)

			CharacteristicEntries part2char1 = model.getCharacteristicEntries(2, 3)
			CharacteristicEntries part2char2 = model.getCharacteristicEntries(2, 4)

		then:
			part1char1.getValue("K2002") == "spolecny popis"
			part1char2.getValue("K2002") == "spolecny popis"
			part2char1.getValue("K2002") == "spolecny popis"
			part2char2.getValue("K2002") == "spolecny popis"
	}

	def "spravne preneseni /0 klicu na vsechny hodnoty kdyz je spolecny klic zapsany pred hodnotama" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllValuesBeforeValue)

			ValueEntries entries1 = model.getValueEntries(1, 1, 1)
			ValueEntries entries2 = model.getValueEntries(1, 1, 2)

			ValueEntries entries0 = model.getValueEntries(1, 0, 0)

		then:
			entries1.getValue("K0014") == "identifikator"
			entries2.getValue("K0014") == null
			entries0 == null
	}

	def "spravne preneseni /0 klicu na jednu sadu hodnot" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToSingleValueSet)

			ValueEntries entries1 = model.getValueEntries(1, 1, 1)
			ValueEntries entries2 = model.getValueEntries(1, 1, 2)

			ValueEntries entries0 = model.getValueEntries(1, 0, 0)

		then:
			entries1.getValue("K0014") == "identifikator"
			entries2.getValue("K0014") == null
			entries0 == null
	}

	def "spravne preneseni /0 klicu s ruznyma hodnotama na vice sad hodnot" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToEachValueSet)

			ValueEntries entries1 = model.getValueEntries(1, 1, 1)
			ValueEntries entries2 = model.getValueEntries(1, 1, 2)

			ValueEntries entries0 = model.getValueEntries(1, 0, 0)

		then:
			entries1.getValue("K0014") == "identifikator 1"
			entries2.getValue("K0014") == "identifikator 2"
			entries0 == null
	}

	def "spravne preneseni vice /0 klicu na vsechny hodnoty danych sad hodnot" () {
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
			char1Value1.getValue("K0014") == "spolecny identifikator"

			char1Value2.getValue("K0004") == Date.parse("dd.MM.yyyy HH:mm:ss", "1.1.2014 10:31:00")
			char1Value2.getValue("K0010") == 154i
			char1Value2.getValue("K0014") == "spolecny identifikator 2"

			char2Value1.getValue("K0004") == Date.parse("dd.MM.yyyy HH:mm:ss", "1.1.2014 10:30:59")
			char2Value1.getValue("K0010") == 153i
			char2Value1.getValue("K0014") == "spolecny identifikator"

			char2Value2.getValue("K0004") == Date.parse("dd.MM.yyyy HH:mm:ss", "1.1.2014 10:31:00")
			char2Value2.getValue("K0010") == 154i
			char2Value2.getValue("K0014") == "spolecny identifikator 2"

			entries0 == null
	}

	def "/0 klice neprepisuji explicitne definovane klice" () {
		when:
			AqdefObjectModel model = parse(dfqWithKeyAppliedToAllPartsSomeDefinedExplicitly)

			PartEntries entries1 = model.getPartEntries(1)
			PartEntries entries2 = model.getPartEntries(2)

		then:
			entries1.getValue("K1002") == "explicitne definovany popis"
			entries2.getValue("K1002") == "spolecny popis"
	}

	def "spravne se parsuji hodnoty v binarnim formatu, ktere jsou na konci soubru" () {
		when:
			AqdefObjectModel model = parse(dfqWithTwoPartsWithBinaryValuesAtTheEnd)

			def values1 = model.getValueEntries(CharacteristicIndex.of(1, 1))
			def values2 = model.getValueEntries(CharacteristicIndex.of(2, 2))

		then:
			values1.size() == 2
			values2.size() == 2
	}

	def "spravne se parsuji hodnoty v binarnim formatu, ktere jsou za kazdym dilem" () {
		when:
			AqdefObjectModel model = parse(dfqWithTwoPartsWithBinaryValuesAfterEachPart)

			def values1 = model.getValueEntries(CharacteristicIndex.of(1, 1))
			def values2 = model.getValueEntries(CharacteristicIndex.of(2, 2))

		then:
			values1.size() == 2
			values2.size() == 0
	}

	def "spravne se parsuji hodnoty v k-klic formatu, ktere jsou na konci soubru" () {
		when:
			AqdefObjectModel model = parse(dfqWithTwoPartsWithBinaryValuesAtTheEnd)

			def values1 = model.getValueEntries(CharacteristicIndex.of(1, 1))
			def values2 = model.getValueEntries(CharacteristicIndex.of(2, 2))

		then:
			values1.size() == 2
			values2.size() == 2
	}

	def "spravne se parsuji hodnoty v k-klic formatu, ktere jsou za kazdym dilem" () {
		when:
			AqdefObjectModel model = parse(dfqWithTwoPartsWithValuesAfterEachPart)

			def values1 = model.getValueEntries(CharacteristicIndex.of(1, 1))
			def values2 = model.getValueEntries(CharacteristicIndex.of(2, 2))

		then:
			values1.size() == 2
			values2.size() == 2
	}

	def "pokud jsou v souboru hodnoty pro neexistujici znaky, tak parser vyhodi exception" () {
		when:
			AqdefObjectModel model = parse(dfqWithMoreValuesThanCharacteristics)

		then:
			thrown(Exception)
	}

	def "po normalizaci object modelu neexistuji /0 klice" () {
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

	def "spravne se parsuje hierarchie" () {
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


	def parse(String dfq) {
		def parser = new AqdefParser()
		return parser.parse(dfq)
	}

	def dfqWithString = """
		K0100 1
		K1001/1 dil
	"""

	def dfqWithInteger = """
		K0100 1
		K1010/1 1
	"""

	def dfqWithDate = """
		K0100 1
		K1001/1 dil
		K2001/1 znak
		K0001/1 1
		K0004/1 1.1.2014/10:30:59
	"""

	def dfqWithEmptyKeys = """
		K0100 1
		K1001/1 dil
		K1002
		K1003/1
	"""

	def dfqWithUnindexedPart = """
		K0100 1
		K1001 dil
		K1002 popis
	"""

	def dfqWithKeyAppliedToAllParts = """
		K0100 2
		K1002/0 spolecny popis
		K1001/1 dil1
		K1001/2 dil2
	"""

	def dfqWithKeyAppliedToAllCharacteristics = """
		K0100 2
		K1001/1 dil
		K2002/0 spolecny popis
		K2001/1 znak1
		K2001/2 znak2
	"""

	def dfqWithKeyAppliedToAllCharacteristicsOnMultipleParts = """
		K0100 4
		K1001/1 dil1
		K2002/0 spolecny popis dil1
		K2001/1 dil1 - znak1
		K2001/2 dil1 - znak2

		K1001/2 dil2
		K2002/0 spolecny popis dil2
		K2001/3 dil2 - znak1
		K2001/4 dil2 - znak2
	"""

	def dfqWithKeyAppliedToAllCharacteristicsOnOneOfMultipleParts = """
		K0100 4
		K1001/1 dil1
		K2002/0 spolecny popis
		K2001/1 dil1 - znak1
		K2001/2 dil1 - znak2

		K1001/2 dil2
		K2001/3 dil2 - znak1
		K2001/4 dil2 - znak2
	"""

	/**
	 * Takto podle qs-statu nelze zapisovat spolecne klice pro hodnoty - klic musi byt za kazdou sadou hodnot.
	 * To je ale podle me spatne chovani - u nas se to da takto zapsat. K0014 se pak aplikuje na prvni sadu hodnot.
	 */
	def dfqWithKeyAppliedToAllValuesBeforeValue = """
		K0100 1
		K1001/1 dil
		K2001/1 znak1
		K0014/0 identifikator
		K0001/1 1
		K0001/1 2
	"""

	def dfqWithKeyAppliedToSingleValueSet = """
		K0100 1
		K1001/1 dil
		K2001/1 znak1
		K0001/1 1
		K0014/0 identifikator
		K0001/1 2
	"""

	def dfqWithKeyAppliedToEachValueSet = """
		K0100 1
		K1001/1 dil
		K2001/1 znak1
		K0001/1 1
		K0014/0 identifikator 1
		K0001/1 2
		K0014/0 identifikator 2
	"""

	def dfqWithMultipleKeysAppliedToAllValues = """
		K0100 2
		K1001/1 dil
		K2001/1 znak1
		K2001/2 znak2
		K0001/1 1
		K0001/2 3
		K0004/0 1.1.2014/10:30:59
		K0010/0 153
		K0014/0 spolecny identifikator
		K0001/1 2
		K0001/2 4
		K0004/0 1.1.2014/10:31:00
		K0010/0 154
		K0014/0 spolecny identifikator 2
	"""

	def dfqWithKeyAppliedToAllPartsSomeDefinedExplicitly = """
		K0100 2
		K1002/0 spolecny popis
		K1001/1 dil1
		K1002/1 explicitne definovany popis
		K1001/2 dil2
	"""

	def dfqWithTwoPartsWithBinaryValuesAtTheEnd = """
		K0100 2
		K1001/1 dil1
		K2001/1 znak1
		K2004/0 0
		K1001/2 dil2
		K2001/2 znak2
		1001.01.2014/00:00:002001.01.2014/00:00:00
		1.1001.01.2014/00:00:002.1001.01.2014/00:00:00
	"""

	/**
	 * Obe hodnoty patri k znaku/dilu 1
	 * (nezavisi na tom za kterym dilem jsou hodnoty zapsane - pro indexaci hodnot se vzdy berou v potaz vsechny znaky)
	 */
	def dfqWithTwoPartsWithBinaryValuesAfterEachPart = """
		K0100 2
		K1001/1 dil1
		K2001/1 znak1
		K2004/0 0
		1001.01.2014/00:00:00
		K1001/2 dil2
		K2001/2 znak2
		2001.01.2014/00:00:00
	"""

	def dfqWithTwoPartsWithValuesAtTheEnd = """
		K0100 2
		K1001/1 dil1
		K2001/1 znak1
		K2004/0 0
		K1001/2 dil2
		K2001/2 znak2
		K0001/1 1
		K0001/1 1.1
		K0001/2 2
		K0001/2 2.1
	"""

	def dfqWithTwoPartsWithValuesAfterEachPart = """
		K0100 2
		K1001/1 dil1
		K2001/1 znak1
		K2004/0 0
		K0001/1 1
		K0001/1 1.1
		K1001/2 dil2
		K2001/2 znak2
		K0001/2 2
		K0001/2 2.1
	"""

	/**
	 * Hodnoty jsou pro 3 znaky, ale definovane jsou jen 2
	 */
	def dfqWithMoreValuesThanCharacteristics = """
		K0100 2
		K1001/1 dil1
		K2001/1 znak1
		K2001/2 znak2
		1001.01.2014/00:00:002001.01.2014/00:00:003001.01.2014/00:00:00
	"""

	def dfqWithKeyAppliedToAllPartsCharacteristicsAndValues = """
		K0100 2
		K1001/0 spolecny popis
		K1001/1 dil
		K2002/0 spolecny popis
		K2001/1 znak
		K0014/0 spolecny identifikator
		K0001/1 1
	"""

	def dfqWithHierarchy = """
		K0100 2
		K1001/1 dil
		K2001/1 znak 1
		K2001/2 znak 2
		K2001/3 znak 3
		K2001/4 znak 4
		K2001/5 znak 5
		K2001/6 znak 6
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
}
