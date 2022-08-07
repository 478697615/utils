package org.sin.util.unittest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JsonExcelTestCaseReaderTest {

    @ParameterizedTest
    @SmartJsonExcelFileSource("/org/sin/util/unittest/readFromJsonExcelFile.xlsx") // read arguments from excel file, located by annotation's value property, which is relative to resource folder
    void readFromJsonExcelFile(Integer expectedInteger
            , Character expectedCharacter
            , String expectedString
            , List<String> expectedList
            , Map<String, String> expectedMap
            , Date expectedDate // supported patterns: [yyyy-MM-dd, yyyy-MM-dd HH:mm:ss], controlled by @SmartJsonExcelFileSource.supportedDatePatterns
            , LocalDate expectedLocalDate // fixed pattern: yyyy-MM-dd
            , LocalDateTime expectedLocalDateTime // fixed pattern: yyyy-MM-dd HH:mm:ss, specified by SmartJsonExcelFileArgumentsProvider.LOCAL_DATE_TIME_PATTERN
            , String expectedPojoSubName
            , TestPojo testPojo) {

        Assertions.assertThat(testPojo)
                .returns(expectedInteger, TestPojo::getInteger)
                .returns(expectedCharacter, TestPojo::getCharacter)
                .returns(expectedString, TestPojo::getString)
                .returns(expectedList, TestPojo::getList)
                .returns(expectedMap, TestPojo::getMap)
                .returns(expectedDate, TestPojo::getDate)
                .returns(expectedLocalDate, TestPojo::getLocalDate)
                .returns(expectedLocalDateTime, TestPojo::getLocalDateTime)
                .returns(expectedPojoSubName, mainPojo -> mainPojo.getSub().getSubName());
    }
}
