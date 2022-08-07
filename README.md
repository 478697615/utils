# 工具

实现了一些可用于提高编码效率的工具类，在此进行分享。

## 特性

- JUnit5 参数化测试工具，支持从 excel 文件读取测试用例

## 快速开始

构建：执行 ./gradlew build 命令构建 jar 包，再导入本地项目。要求 Java 8 及以上版本。

使用方法：在参数化测试方法上添加 @SmartJsonExcelFileSource 注解，填入 excel 文件地址，程序会自动读取文件内容并将其转换成测试参数。

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

excel 文件内容：

![文件内容](/src/test/resources/org/sin/util/unittest/testResult_readFromJsonExcelFile.png)
