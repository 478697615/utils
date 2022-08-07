package org.sin.util.unittest;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ChengTian
 * @date 2022/8/7
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(SmartJsonExcelFileArgumentsProvider.class)
public @interface SmartJsonExcelFileSource {
    /**
     * excel文件路径，相较于resource目录
     * @return excel文件路径
     */
    String value();

    /**
     * 支持自动转换的日期格式，如果excel文件中出现其他格式的日期，则转换失败
     * @return 支持自动转换的日期格式
     */
    String[] supportedDatePatterns() default {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss"};
}
