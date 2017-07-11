package cz.diribet.aqdef.model;

import static org.hamcrest.Matchers.*

import cz.diribet.aqdef.KKey
import cz.diribet.aqdef.model.CharacteristicIndex
import cz.diribet.aqdef.model.AqdefObjectModel
import cz.diribet.aqdef.model.PartIndex
import cz.diribet.aqdef.model.ValueIndex
import spock.lang.Specification

class AqdefObjectModelNormalizationTest extends Specification {

	def "part entries have correct entry index after normalization" () {
		given:
			AqdefObjectModel model = new AqdefObjectModel()
			model.putPartEntry(KKey.of("K1001"), PartIndex.of(0), "spolecne cislo dilu")
			model.putPartEntry(KKey.of("K1002"), PartIndex.of(1), "nazev dilu 1")
			model.putPartEntry(KKey.of("K1002"), PartIndex.of(2), "nazev dilu 2")

		when:
			model.normalize()

			def entries0 = model.getPartEntries(0)
			def entries1 = model.getPartEntries(1)
			def entries2 = model.getPartEntries(2)

		then:
			entries0 == null

			entries1.get(KKey.of("K1001"))?.getIndex()?.getIndex() == 1
			entries1.get(KKey.of("K1001"))?.getValue() == "spolecne cislo dilu"
			entries1.get(KKey.of("K1002"))?.getIndex()?.getIndex() == 1
			entries1.get(KKey.of("K1002"))?.getValue() == "nazev dilu 1"

			entries2.get(KKey.of("K1001"))?.getIndex()?.getIndex() == 2
			entries2.get(KKey.of("K1001"))?.getValue() == "spolecne cislo dilu"
			entries2.get(KKey.of("K1002"))?.getIndex()?.getIndex() == 2
			entries2.get(KKey.of("K1002"))?.getValue() == "nazev dilu 2"
	}

	def "if there are only /0 k-keys for part, default part (/1) will be created" () {
		given:
			AqdefObjectModel model = new AqdefObjectModel()
			model.putPartEntry(KKey.of("K1001"), PartIndex.of(0), "spolecne cislo dilu")

		when:
			model.normalize()

			def entries0 = model.getPartEntries(0)
			def entries1 = model.getPartEntries(1)

		then:
			entries0 == null

			entries1?.get(KKey.of("K1001"))?.getIndex()?.getIndex() == 1
			entries1?.get(KKey.of("K1001"))?.getValue() == "spolecne cislo dilu"
	}


	def "characteristic entries have correct entry index after normalization" () {
		given:
			AqdefObjectModel model = new AqdefObjectModel()
			model.putPartEntry(KKey.of("K1001"), PartIndex.of(1), "dil1")

			model.putCharacteristicEntry(KKey.of("K2001"), CharacteristicIndex.of(1, 0), "spolecne cislo znaku")
			model.putCharacteristicEntry(KKey.of("K2003"), CharacteristicIndex.of(0, 0), "spolecny popis")
			model.putCharacteristicEntry(KKey.of("K2002"), CharacteristicIndex.of(1, 1), "nazev znaku 1")
			model.putCharacteristicEntry(KKey.of("K2002"), CharacteristicIndex.of(1, 2), "nazev znaku 2")

		when:
			model.normalize()

			def entries_0_0 = model.getCharacteristicEntries(0, 0)
			def entries_1_0 = model.getCharacteristicEntries(1, 0)
			def entries_1_1 = model.getCharacteristicEntries(1, 1)
			def entries_1_2 = model.getCharacteristicEntries(1, 2)

		then:
			entries_0_0 == null
			entries_1_0 == null

			entries_1_1.get(KKey.of("K2001"))?.getIndex()?.getCharacteristicIndex() == 1
			entries_1_1.get(KKey.of("K2001"))?.getValue() == "spolecne cislo znaku"
			entries_1_1.get(KKey.of("K2002"))?.getIndex()?.getCharacteristicIndex() == 1
			entries_1_1.get(KKey.of("K2002"))?.getValue() == "nazev znaku 1"
			entries_1_1.get(KKey.of("K2003"))?.getIndex()?.getCharacteristicIndex() == 1
			entries_1_1.get(KKey.of("K2003"))?.getValue() == "spolecny popis"

			entries_1_2.get(KKey.of("K2001"))?.getIndex()?.getCharacteristicIndex() == 2
			entries_1_2.get(KKey.of("K2001"))?.getValue() == "spolecne cislo znaku"
			entries_1_2.get(KKey.of("K2002"))?.getIndex()?.getCharacteristicIndex() == 2
			entries_1_2.get(KKey.of("K2002"))?.getValue() == "nazev znaku 2"
			entries_1_2.get(KKey.of("K2003"))?.getIndex()?.getCharacteristicIndex() == 2
			entries_1_2.get(KKey.of("K2003"))?.getValue() == "spolecny popis"
	}

	def "value entries have correct entry index after normalization" () {
		given:
			AqdefObjectModel model = new AqdefObjectModel()
			model.putPartEntry(KKey.of("K1001"), PartIndex.of(1), "dil1")

			model.putCharacteristicEntry(KKey.of("K2001"), CharacteristicIndex.of(1, 1), "znak1")
			model.putCharacteristicEntry(KKey.of("K2001"), CharacteristicIndex.of(1, 2), "znak2")

			model.putValueEntry(KKey.of("K0001"), ValueIndex.of(1, 1, 1), 1.0)
			model.putValueEntry(KKey.of("K0001"), ValueIndex.of(1, 1, 2), 1.0)
			model.putValueEntry(KKey.of("K0001"), ValueIndex.of(1, 2, 1), 2.0)
			model.putValueEntry(KKey.of("K0001"), ValueIndex.of(1, 2, 2), 2.0)

			model.putValueEntry(KKey.of("K0053"), ValueIndex.of(0, 0, 1), "spolecna zakazka 1")
			model.putValueEntry(KKey.of("K0053"), ValueIndex.of(0, 0, 2), "spolecna zakazka 2")

		when:
			model.normalize()

			def entries_0_0_1 = model.getValueEntries(0, 0, 1)
			def entries_0_0_2 = model.getValueEntries(0, 0, 2)
			def entries_1_1_1 = model.getValueEntries(1, 1, 1)
			def entries_1_1_2 = model.getValueEntries(1, 1, 2)
			def entries_1_2_1 = model.getValueEntries(1, 2, 1)
			def entries_1_2_2 = model.getValueEntries(1, 2, 2)

		then:
			entries_0_0_1 == null
			entries_0_0_2 == null

			entries_1_1_1.get(KKey.of("K0001"))?.getIndex()?.getValueIndex() == 1
			entries_1_1_1.get(KKey.of("K0001"))?.getValue() == 1.0
			entries_1_1_1.get(KKey.of("K0053"))?.getIndex()?.getValueIndex() == 1
			entries_1_1_1.get(KKey.of("K0053"))?.getValue() == "spolecna zakazka 1"

			entries_1_2_1.get(KKey.of("K0001"))?.getIndex()?.getValueIndex() == 1
			entries_1_2_1.get(KKey.of("K0001"))?.getValue() == 2.0
			entries_1_2_1.get(KKey.of("K0053"))?.getIndex()?.getValueIndex() == 1
			entries_1_2_1.get(KKey.of("K0053"))?.getValue() == "spolecna zakazka 1"

			entries_1_1_2.get(KKey.of("K0001"))?.getIndex()?.getValueIndex() == 2
			entries_1_1_2.get(KKey.of("K0001"))?.getValue() == 1.0
			entries_1_1_2.get(KKey.of("K0053"))?.getIndex()?.getValueIndex() == 2
			entries_1_1_2.get(KKey.of("K0053"))?.getValue() == "spolecna zakazka 2"

			entries_1_2_2.get(KKey.of("K0001"))?.getIndex()?.getValueIndex() == 2
			entries_1_2_2.get(KKey.of("K0001"))?.getValue() == 2.0
			entries_1_2_2.get(KKey.of("K0053"))?.getIndex()?.getValueIndex() == 2
			entries_1_2_2.get(KKey.of("K0053"))?.getValue() == "spolecna zakazka 2"

	}

}
