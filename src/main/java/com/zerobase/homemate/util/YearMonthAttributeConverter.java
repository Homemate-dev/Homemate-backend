package com.zerobase.homemate.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.YearMonth;

@Converter (autoApply = true)
public class YearMonthAttributeConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth yearMonth) {
        return yearMonth == null ? null : yearMonth.toString();
    }

    @Override
    public YearMonth convertToEntityAttribute(String s) {
        return s == null ? null : YearMonth.parse(s);
    }
}
