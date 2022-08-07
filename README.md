# 工具整理

整理了开发过程中自己实现的可用于提高编码效率的工具类.

## 特性

- JUnit5 参数化测试工具，支持从 Excel 中导入测试用例并自动转换参数类型

## 快速开始

执行 ./gradlew build 命令构建jar包，再导入本地项目。要求 Java 8 及以上版本。

核心注解：SmartJsonExcelFileArgumentsProvider，示例：

```
public class JsonExcelTestCaseReaderTest {
    @ParameterizedTest
    @SmartJsonExcelFileSource("/com/sin/util/unittest/readFromJsonExcelFile.xlsx") // read arguments from excel file, located by annotation's value property, which is relative to resource folder
    void readFromJsonExcelFile(Integer expectedInteger
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
                .returns(expectedString, TestPojo::getString)
                .returns(expectedList, TestPojo::getList)
                .returns(expectedMap, TestPojo::getMap)
                .returns(expectedDate, TestPojo::getDate)
                .returns(expectedLocalDate, TestPojo::getLocalDate)
                .returns(expectedLocalDateTime, TestPojo::getLocalDateTime)
                .returns(expectedPojoSubName, mainPojo -> mainPojo.getSub().getSubName());
    }
}
```

readFromJsonExcelFile.xlsx 文件内容：

![文件内容](/src/test/resources/org/sin/util/unittest/testResult_readFromJsonExcelFile.png)
