package org.sin.util.unittest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ChengTian
 * @date 2022/8/7
 */
public class SmartJsonExcelFileArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<SmartJsonExcelFileSource> {

    private static final String LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private SmartJsonExcelFileSource smartJsonExcelFileSource;
    private ObjectMapper objectMapper;

    @Override
    public void accept(SmartJsonExcelFileSource smartJsonExcelFileSource) {
        this.smartJsonExcelFileSource = smartJsonExcelFileSource;
        this.objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new CustomDateDeserializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_PATTERN)));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // mustn't change registering order, or custom localDateTimeDeserializer will fail
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(module);
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        // 从指定文件路径读取文件
        // 设置日期格式
        // 读取文件
        // 格式转换
        String filepath = smartJsonExcelFileSource.value();
        List<List<String>> table = TestCaseReader.readJsonArgumentsFromExcelTableInStringFormat(filepath);
        return convertTableArguments(table, context.getRequiredTestMethod().getParameters());
    }

    private Stream<? extends Arguments> convertTableArguments(List<List<String>> table, Parameter[] parametersMeta) {
        List<JavaType> parameterTypes = Arrays.stream(parametersMeta)
                .map(Parameter::getParameterizedType)
                .map(parameterizedType -> objectMapper.getTypeFactory().constructType(parameterizedType))
                .collect(Collectors.toList());

        // 参数如果是基本格式，则直接使用value不需要转换
        BiFunction<List<String>, List<JavaType>, List<Pair<String, JavaType>>> getCellValueTypePairs = (rowValues, targetParameterTypes) -> {
            validate(rowValues, targetParameterTypes);
            List<Pair<String, JavaType>> rowValueTypePairs = new ArrayList<>();
            for (int i = 0; i < rowValues.size(); i++) {
                Pair<String, JavaType> cellValueTypePair = ImmutablePair.of(rowValues.get(i), targetParameterTypes.get(i));
                rowValueTypePairs.add(cellValueTypePair);
            }
            return rowValueTypePairs;
        };

        Function<List<String>, Object[]> convertTableRowValues = tableRowValues -> {
            List<Pair<String, JavaType>> cellValueTypePairs = getCellValueTypePairs.apply(tableRowValues, parameterTypes);
            return cellValueTypePairs.stream()
                    .map(cellValueTypePair -> convertCellValue(cellValueTypePair.getLeft(), cellValueTypePair.getRight()))
                    .toArray();
        };

        return table.stream()
                .map(convertTableRowValues)
                .map(Arguments::of);
    }

    private Object convertCellValue(String cellValue, JavaType targetType) {
        CellValueParser cellValueParser = getCellValueParser();
        return cellValueParser.parseChain(cellValue, targetType);
    }

    private CellValueParser getCellValueParser() {
        CellValueParser jsonValueParser = new JsonValueParser(null);
        CellValueParser commonUsedTypeParser = new CommonUsedTypeParser(jsonValueParser);
        return new BaseTypeParser(commonUsedTypeParser);
    }

    private void validate(List<String> rowValues, List<JavaType> targetParameterTypes) {
        if (rowValues.size() != targetParameterTypes.size()) {
            String errorMessage = String.format("转换行数据时出错，列数有误。表格列数：%d，测试方法参数个数：%d", rowValues.size(), targetParameterTypes.size());
            throw new JUnitException(errorMessage);
        }
    }

    private class CustomDateDeserializer extends StdDeserializer<Date> {

        private static final long serialVersionUID = -7511013933621828883L;

        public CustomDateDeserializer() {
            this(null);
        }

        public CustomDateDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Date deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) throws IOException {
            return convertDate(p.getText());
        }
    }

    private Date convertDate(String cellValue) {
        try {
            return DateUtils.parseDate(cellValue, smartJsonExcelFileSource.supportedDatePatterns());
        } catch (ParseException e) {
            String errorMessage = String.format("日期转换错误。日期值：%s，支持转换的日期格式列表：%s", cellValue, Arrays.toString(smartJsonExcelFileSource.supportedDatePatterns()));
            throw new JUnitException(errorMessage, e);
        }
    }

    private static abstract class CellValueParser {

        private final CellValueParser nextParser;

        public CellValueParser(CellValueParser nextParser) {
            this.nextParser = nextParser;
        }

        public Object parseChain(String cellValue, JavaType targetType) {
            Object result = parse(cellValue, targetType);
            if (result == null && nextParser != null) {
                result = nextParser.parseChain(cellValue, targetType);
            }
            return result;
        }

        public abstract Object parse(String cellValue, JavaType targetType);
    }

    private static class BaseTypeParser extends CellValueParser {

        public BaseTypeParser(CellValueParser nextParser) {
            super(nextParser);
        }

        @Override
        public Object parse(String cellValue, JavaType targetType) {
            Class<?> clazz = targetType.getRawClass();
            if (Boolean.class == clazz) {
                return Boolean.parseBoolean(cellValue);
            }
            if (Byte.class == clazz) {
                return Byte.parseByte(cellValue);
            }
            if (Short.class == clazz) {
                return Short.parseShort(cellValue);
            }
            if (Integer.class == clazz) {
                return Integer.parseInt(cellValue);
            }
            if (Long.class == clazz) {
                return Long.parseLong(cellValue);
            }
            if (Float.class == clazz) {
                return Float.parseFloat(cellValue);
            }
            if (Double.class == clazz) {
                return Double.parseDouble(cellValue);
            }
            if (Character.class == clazz) {
                return cellValue.charAt(0);
            }
            if (String.class == clazz) {
                return cellValue;
            }
            return null;
        }
    }

    private class CommonUsedTypeParser extends CellValueParser {

        public CommonUsedTypeParser(CellValueParser nextParser) {
            super(nextParser);
        }

        @Override
        public Object parse(String cellValue, JavaType targetType) {
            Class<?> clazz = targetType.getRawClass();
            if (Date.class == clazz) {
                return convertDate(cellValue);
            }
            if (LocalDateTime.class == clazz) {
                return LocalDateTime.parse(cellValue, DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_PATTERN));
            }
            if (LocalDate.class == clazz) {
                return LocalDate.parse(cellValue, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            return null;
        }
    }

    private class JsonValueParser extends CellValueParser {

        public JsonValueParser(CellValueParser nextParser) {
            super(nextParser);
        }

        @Override
        public Object parse(String cellValue, JavaType targetType) {
            try {
                return objectMapper.readValue(cellValue, targetType);
            } catch (IOException e) {
                String errorMessage = String.format("json转换失败。原值：%s，转换类型：%s", cellValue, targetType.getGenericSignature());
                throw new JUnitException(errorMessage, e);
            }
        }
    }
}
