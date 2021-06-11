package cz.diribet.aqdef.convert

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

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

			// d.M.yy(yy).H:m(:s)
			"09.05.18.10:05:59"			|| expectedDateWithSeconds
			"09.5.18.10:5:59"			|| expectedDateWithSeconds
			"9.05.18.10:05:59"			|| expectedDateWithSeconds
			"9.5.18.10:05:59"			|| expectedDateWithSeconds
			"9.5.18.10:05"				|| expectedDateWithoutSeconds
			"9.5.2018.10:05:59"			|| expectedDateWithSeconds

			// M/d/yy(yy).H:m(:s)
			"05/09/18.10:05:59"			|| expectedDateWithSeconds
			"05/9/18.10:05:59"			|| expectedDateWithSeconds
			"5/09/18.10:05:59"			|| expectedDateWithSeconds
			"5/9/18.10:05:59"			|| expectedDateWithSeconds
			"5/9/18.10:05"				|| expectedDateWithoutSeconds
			"5/9/2018.10:05:59"			|| expectedDateWithSeconds

			// yy(yy)-M-d.H:m(:s)
			"18-05-09.10:05:59"			|| expectedDateWithSeconds
			"18-5-09.10:5:59"			|| expectedDateWithSeconds
			"18-05-9.10:05:59"			|| expectedDateWithSeconds
			"18-5-9.10:05:59"			|| expectedDateWithSeconds
			"18-5-9.10:05"				|| expectedDateWithoutSeconds
			"2018-5-9.10:05:59"			|| expectedDateWithSeconds

			// ISO 8601 (with local date time support)
			"2018-05-09T10:05:59"		|| expectedDateWithSeconds
			"2018-05-09T10:05"			|| expectedDateWithoutSeconds
			"2018-05-09T10:05:59+02:00"	|| toOffsetDate(expectedDateWithSeconds, "+02:00")
			"2018-05-09T10:05:59-01:00"	|| toOffsetDate(expectedDateWithSeconds, "-01:00")
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
			/* yyyy/d/M/H:m:s */ "2018.03.20/18:13:10"	| _
	}

	def "Date is formatted correctly"() {
		given:
			def dateKKeyValueConverter = new DateKKeyValueConverter()
			def inputDate = createDate(5, 10, 2018, 15, 5, 10)

		when:
			def formatedDate = dateKKeyValueConverter.toString(inputDate)

		then:
			formatedDate == "05.10.2018/15:05:10"
	}

	Date createDate(int day, int month, int year, int hour, int minutes, int seconds) {
		def localDateTime = LocalDateTime.of(year, month, day, hour, minutes, seconds)
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
	}

	Date toOffsetDate(Date date, String offset) {
		def localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
		return Date.from(OffsetDateTime.of(localDateTime, ZoneOffset.of(offset)).toInstant())
	}

}

