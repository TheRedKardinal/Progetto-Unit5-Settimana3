package com.example.be.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Year;

/**
 * Salva java.time.Year come INTEGER.
 */
@Converter(autoApply = true)
public class YearAttributeConverter implements AttributeConverter<Year, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Year year) {
        return year == null ? null : year.getValue();
    }

    @Override
    public Year convertToEntityAttribute(Integer value) {
        return value == null ? null : Year.of(value);
    }
}
