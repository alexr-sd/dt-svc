package com.logistics.common;

import com.logistics.entity.Region;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToRegionConverter implements Converter<String, Region> {

    @Override
    public Region convert(String source) {
        try {
            return Region.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid region: '" + source + "'");
        }
    }
}
