package cz.diribet.aqdef.convert

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DateKKeyValueConverterTest extends Specification {

	@Shared
	def expectedDate = Date.parse("dd.MM.yyyy HH:mm", "9.5.2018 10:30")

	@Shared
	def expectedDateWithSeconds = Date.parse("dd.MM.yyyy HH:mm:ss", "9.5.2018 10:30:59")

	@Unroll
	def "Date text '#inputDate' is parsed correctly"() {
		given:
			def dateKKeyValueConverter = new DateKKeyValueConverter()

		when:
			def date = dateKKeyValueConverter.convert(inputDate)

		then:
			date == result

		where:
			inputDate 					|| result

			// dd.MM.yy/HH:mm:ss
			"09.05.18/10:30:59"			|| expectedDateWithSeconds
			"09.5.18/10:30:59"			|| expectedDateWithSeconds
			"9.05.18/10:30:59"			|| expectedDateWithSeconds
			"9.5.18/10:30:59"			|| expectedDateWithSeconds
			"9.5.2018/10:30:59"			|| expectedDateWithSeconds

			// MM/dd/yy/HH:mm:ss
			"05/09/18/10:30:59"			|| expectedDateWithSeconds
			"05/9/18/10:30:59"			|| expectedDateWithSeconds
			"5/09/18/10:30:59"			|| expectedDateWithSeconds
			"5/9/18/10:30:59"			|| expectedDateWithSeconds
			"5/9/2018/10:30:59"			|| expectedDateWithSeconds


			// yy-MM-dd/HH:mm:ss
			"18-05-09/10:30:59"			|| expectedDateWithSeconds
			"18-5-09/10:30:59"			|| expectedDateWithSeconds
			"18-05-9/10:30:59"			|| expectedDateWithSeconds
			"18-5-9/10:30:59"			|| expectedDateWithSeconds
			"2018-5-9/10:30:59"			|| expectedDateWithSeconds


			// dd.MM.yy/HH:mm
			"09.05.18/10:30"			|| expectedDate
			"09.5.18/10:30"				|| expectedDate
			"9.05.18/10:30"				|| expectedDate
			"9.5.18/10:30"				|| expectedDate
			"9.5.2018/10:30"			|| expectedDate

			// MM/dd/yy/HH:mm
			"05/09/18/10:30"			|| expectedDate
			"05/9/18/10:30"				|| expectedDate
			"5/09/18/10:30"				|| expectedDate
			"5/9/18/10:30"				|| expectedDate
			"5/9/2018/10:30"			|| expectedDate

			// yy-MM-dd/HH:mm:ss
			"18-05-09/10:30"			|| expectedDate
			"18-05-9/10:30"				|| expectedDate
			"18-5-09/10:30"				|| expectedDate
			"18-5-9/10:30"				|| expectedDate
			"2018-5-9/10:30"			|| expectedDate

			// dd.MM.yy HH:mm:ss
			"09.05.18/10:30:59"			|| expectedDateWithSeconds
			"09.5.18/10:30:59"			|| expectedDateWithSeconds
			"9.05.18/10:30:59"			|| expectedDateWithSeconds
			"9.5.18/10:30:59"			|| expectedDateWithSeconds
			"9.5.2018/10:30:59"			|| expectedDateWithSeconds

			// MM/dd/yy HH:mm:ss
			"05/09/18/10:30:59"			|| expectedDateWithSeconds
			"05/9/18/10:30:59"			|| expectedDateWithSeconds
			"5/09/18/10:30:59"			|| expectedDateWithSeconds
			"5/9/18/10:30:59"			|| expectedDateWithSeconds
			"5/9/2018/10:30:59"			|| expectedDateWithSeconds


			// yy-MM-dd HH:mm:ss
			"18-05-09 10:30:59"			|| expectedDateWithSeconds
			"18-5-09 10:30:59"			|| expectedDateWithSeconds
			"18-05-9 10:30:59"			|| expectedDateWithSeconds
			"18-5-9 10:30:59"			|| expectedDateWithSeconds
			"2018-5-9 10:30:59"			|| expectedDateWithSeconds


			// dd.MM.yy HH:mm
			"09.05.18 10:30"			|| expectedDate
			"09.5.18 10:30"				|| expectedDate
			"9.05.18 10:30"				|| expectedDate
			"9.5.18 10:30"				|| expectedDate
			"9.5.2018 10:30"			|| expectedDate

			// MM/dd/yy HH:mm
			"05/09/18 10:30"			|| expectedDate
			"05/9/18 10:30"				|| expectedDate
			"5/09/18 10:30"				|| expectedDate
			"5/9/18 10:30"				|| expectedDate
			"5/9/2018 10:30"			|| expectedDate

			// yy-MM-dd HH:mm:ss
			"18-05-09 10:30"			|| expectedDate
			"18-05-9 10:30"				|| expectedDate
			"18-5-09 10:30"				|| expectedDate
			"18-5-9 10:30"				|| expectedDate
			"2018-5-9 10:30"			|| expectedDate
	}

}
