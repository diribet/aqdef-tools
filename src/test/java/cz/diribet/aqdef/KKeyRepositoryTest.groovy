package cz.diribet.aqdef;

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import cz.diribet.aqdef.KKey
import cz.diribet.aqdef.KKeyRepository
import spock.lang.Specification

class KKeyRepositoryTest extends Specification {

	def "k-key is correctly mapped to column name"() {
		when:
			def column = KKeyRepository.getInstance().getMetadataFor(KKey.of("K0001"))
		then:
			column.columnName == "WVWERT"
	}

}
