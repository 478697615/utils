package org.sin.util.unittest;

import com.alibaba.excel.EasyExcel;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ChengTian
 * @date 2022/7/14
 */
public class TestCaseReader {

    public static Stream<Arguments> readJsonArgumentsFromExcelTable(String filePath) {
        return readJsonArgumentsFromExcelTableInStringFormat(filePath).stream()
                .map(Collection::toArray)
                .map(Arguments::of);
    }

    public static List<List<String>> readJsonArgumentsFromExcelTableInStringFormat(String filePath) {
        final List<List<String>> table = new ArrayList<>();
        Consumer<List<Map<Integer, String>>> dataConsumer = excelTable -> {
            List<List<String>> excelPartialTable = excelTable.stream()
                    .map(map -> new ArrayList<>(map.values()))
                    .collect(Collectors.toList());
            table.addAll(excelPartialTable);
        };
        EasyExcel.read(TestCaseReader.class.getResourceAsStream(filePath), new NoModelDataListener(dataConsumer)).sheet().doRead();
        return table;
    }
}
