package net.smartcosmos.dao.objects;

import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author voor
 */
@EnableJpaRepositories
@EnableJpaAuditing
@EntityScan
@ComponentScan
@Configuration
public class ObjectPersistenceConfig {
}
