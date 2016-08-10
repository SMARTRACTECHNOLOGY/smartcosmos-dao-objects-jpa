package net.smartcosmos.dao.things.converter;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;

abstract class ConversionServiceAwareConverter<S, T> implements Converter<S, T> {

    @Autowired
    private ConversionService conversionService;

    protected ConversionService conversionService() {

        return conversionService;
    }

    @PostConstruct
    private void register() {

        if (conversionService instanceof ConverterRegistry) {
            ((ConverterRegistry) conversionService).addConverter(this);
        } else {
            throw new IllegalStateException("Can't register Converter to ConverterRegistry");
        }
    }
}
