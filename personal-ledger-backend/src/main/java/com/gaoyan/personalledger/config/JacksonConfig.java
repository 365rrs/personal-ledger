package com.gaoyan.personalledger.config;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.List;

/**
 * Jackson配置 - 解决Long类型精度丢失问题
 */
//@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                for (BeanPropertyWriter writer : beanProperties) {
                    if ("id".equals(writer.getName()) && 
                        (writer.getType().getRawClass() == Long.class || 
                         writer.getType().getRawClass() == long.class)) {
                        writer.assignSerializer(ToStringSerializer.instance);
                    }
                }
                return beanProperties;
            }
        });
        
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }
}
