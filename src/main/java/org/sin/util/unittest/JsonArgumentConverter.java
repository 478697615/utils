package org.sin.util.unittest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class JsonArgumentConverter implements ArgumentConverter {


    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        String s = (String) source;
        JavaType type = MAPPER.getTypeFactory().constructType(context.getParameter().getParameterizedType());
        try {
            return MAPPER.readValue(s, type);
        } catch (IOException e) {
            return null;
        }
    }
}