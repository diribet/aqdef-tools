package cz.diribet.aqdef.convert

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DateKKeyValueConverterTest extends Specification {

	@Shared
	def expectedDateWithoutSeconds = Date.parse("dd.MM.yyyy HH:mm", "9.5.2018 10:05")

	@Shared
	def expectedDateWithSeconds = Date.parse("dd.MM.yyyy HH:mm:ss", "9.5.2018 10:05:59")

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

			// d.M.yy(yy)/H:m(:s)
			"09.05.18/10:05:59"			|| expectedDateWithSeconds
			"09.5.18/10:5:59"			|| expectedDateWithSeconds
			"9.05.18/10:05:59"			|| expectedDateWithSeconds
			"9.5.18/10:05:59"			|| expectedDateWithSeconds
			"9.5.18/10:05"				|| expectedDateWithoutSeconds
			"9.5.2018/10:05:59"			|| expectedDateWithSeconds

			// M/d/yy(yy)/H:m(:s)
			"05/09/18/10:05:59"			|| expectedDateWithSeconds
			"05/9/18/10:05:59"			|| expectedDateWithSeconds
			"5/09/18/10:05:59"			|| expectedDateWithSeconds
			"5/9/18/10:05:59"			|| expectedDateWithSeconds
			"5/9/18/10:05"				|| expectedDateWithoutSeconds
			"5/9/2018/10:05:59"			|| expectedDateWithSeconds

			// yy(yy)-M-d/H:m(:s)
			"18-05-09/10:05:59"			|| expectedDateWithSeconds
			"18-5-09/10:5:59"			|| expectedDateWithSeconds
			"18-05-9/10:05:59"			|| expectedDateWithSeconds
			"18-5-9/10:05:59"			|| expectedDateWithSeconds
			"18-5-9/10:05"				|| expectedDateWithoutSeconds
			"2018-5-9/10:05:59"			|| expectedDateWithSeconds

			// d.M.yy(yy) H:m(:s)
			"09.05.18/10:05:59"			|| expectedDateWithSeconds
			"09.5.18/10:5:59"			|| expectedDateWithSeconds
			"9.05.18/10:05:59"			|| expectedDateWithSeconds
			"9.5.18/10:05:59"			|| expectedDateWithSeconds
			"9.5.18/10:05"				|| expectedDateWithoutSeconds
			"9.5.2018/10:05:59"			|| expectedDateWithSeconds

			// M/d/yy(yy) H:m(:s)
			"05/09/18/10:05:59"			|| expectedDateWithSeconds
			"05/9/18/10:5:59"			|| expectedDateWithSeconds
			"5/09/18/10:05:59"			|| expectedDateWithSeconds
			"5/9/18/10:05:59"			|| expectedDateWithSeconds
			"5/9/18/10:05"				|| expectedDateWithoutSeconds
			"5/9/2018/10:05:59"			|| expectedDateWithSeconds

			// yy(yy)-M-d H:m(:s)
			"18-05-09 10:05:59"			|| expectedDateWithSeconds
			"18-5-09 10:5:59"			|| expectedDateWithSeconds
			"18-05-9 10:05:59"			|| expectedDateWithSeconds
			"18-5-9 10:05:59"			|| expectedDateWithSeconds
			"18-5-9 10:05"				|| expectedDateWithoutSeconds
			"2018-5-9 10:05:59"			|| expectedDateWithSeconds
	}

	@Unroll
	def "Date text '#inputDate' is not supported"() {
		given:
			def dateKKeyValueConverter = new DateKKeyValueConverter()

		when:
			dateKKeyValueConverter.convert(inputDate)

		then:
			thrown(KKeyValueConversionException)

		where:
			inputDate 									| _
			/* d/M/yyyy/H:m:s */ "20/05/2018/18:05:10"	| _
			/* d-M-yyyy/H:m:s */ "20-05-2018/18:05:10"	| _
			/* M.d.yyyy/H:m:s */ "05.20.2018/18:05:10"	| _
			/* M-d-yyyy/H:m:s */ "05-20-2018/18:05:10"	| _
			/* yyyy.M.d/H:m:s */ "2018.05.20/18:05:10"	| _
			/* yyyy/M/d/H:m:s */ "2018/05/20/18:05:10"	| _
	}

}
