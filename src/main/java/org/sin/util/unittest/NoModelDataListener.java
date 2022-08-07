package org.sin.util.unittest;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NoModelDataListener extends AnalysisEventListener<Map<Integer, String>> {

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5;
    private List<Map<Integer, String>> cachedDataList = new ArrayList<>();
    private final Consumer<List<Map<Integer, String>>> dataConsumer;

    public NoModelDataListener(Consumer<List<Map<Integer, String>>> dataConsumer) {
        this.dataConsumer = dataConsumer;
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            consumeData();
            cachedDataList = new ArrayList<>();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        consumeData();
    }

    /**
     * 加上存储数据库
     */
    private void consumeData() {
        dataConsumer.accept(cachedDataList);
    }
}