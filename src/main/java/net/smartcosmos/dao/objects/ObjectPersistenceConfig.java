package net.smartcosmos.dao.objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Map;

/**
 * @author voor
 */
@EnableJpaRepositories
@EnableJpaAuditing
@EntityScan
@ComponentScan
@Configuration
public class ObjectPersistenceConfig extends WebMvcConfigurerAdapter {

    @Autowired
    Map<String, FormatterRegistrar> formatterRegistrarMap;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        for (FormatterRegistrar registrar : formatterRegistrarMap.values()) {
            registrar.registerFormatters(registry);
        }
    }
}
