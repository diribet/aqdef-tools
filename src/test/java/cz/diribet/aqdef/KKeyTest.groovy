package cz.diribet.aqdef;

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import spock.lang.Specification

class KKeyTest extends Specification {

	def "two different K-keys are not equals" () {
		expect:
			KKey.of("K1001") != KKey.of("K1002")
	}

	def "two same K-keys are equals" () {
		expect:
			KKey.of("K1001") == KKey.of("K1001")
	}

	def "two same K-keys are the same instances (they are fetched from cache)" () {
		expect:
			KKey.of("K1001") is KKey.of("K1001")
	}

	def "100 000 instances of K-keys can be created without exception" () {
		when:
			def klice = []
			for (i in 1..100000) {
				klice.add(KKey.of("K" + i))
			}

		then:
			notThrown Throwable
			klice.size == 100000
	}

}
