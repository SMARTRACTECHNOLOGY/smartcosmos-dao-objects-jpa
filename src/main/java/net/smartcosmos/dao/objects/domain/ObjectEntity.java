package net.smartcosmos.dao.objects.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author voor
 */
@Entity(name = "object")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Builder
// Need this because Builder will otherwise use the empty constructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners({ AuditingEntityListener.class })
@Table(name = "object", uniqueConstraints = @UniqueConstraint(columnNames = { "objectUrn", "accountUuid" }) )
public class ObjectEntity implements Serializable {

    private static final int UUID_LENGTH = 16;
    private static final int OBJECT_URN_LENGTH = 767;
    private static final int TYPE_LENGTH = 255;
    private static final int MONIKER_LENGTH = 2048;
    private static final int NAME_LENGTH = 255;
    private static final int DESCRIPTION_LENGTH = 1024;

    /*
        Without setting an appropriate Hibernate naming strategy, the column names specified in the @Column annotations below will be converted
        from camel case to underscore, e.g.: systemUuid -> system_uuid

        To avoid that, select the "old" naming strategy org.hibernate.cfg.EJB3NamingStrategy in your configuration (smartcosmos-ext-objects-rdao.yml):
        jpa.hibernate.naming_strategy: org.hibernate.cfg.EJB3NamingStrategy
     */

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-binary")
    @Column(name = "systemUuid", length = UUID_LENGTH)
    private UUID id;

    @NotNull
    @Size(min = 1, max = OBJECT_URN_LENGTH)
    @Column(name="objectUrn", length = OBJECT_URN_LENGTH, nullable = false, updatable = false)
    private String objectUrn;

    @NotNull
    @Size(min = 1, max = TYPE_LENGTH)
    @Column(name = "type", length = TYPE_LENGTH, nullable = false)
    private String type;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = "accountUuid", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID accountUrn;

    @CreatedDate
    @Column(name = "createdTimestamp", insertable = true, updatable = false)
    private Long created;

    @LastModifiedDate
    @Column(name = "lastModifiedTimestamp", insertable = false, updatable = true)
    private Long lastModified;

    @Size(max = MONIKER_LENGTH)
    @Column(name = "moniker", length = MONIKER_LENGTH, nullable = true, updatable = true)
    private String moniker;

    @NotNull
    @Size(min = 1, max = NAME_LENGTH)
    @Column(name = "name", length = NAME_LENGTH, nullable = false)
    private String name;

    @Size(max = DESCRIPTION_LENGTH)
    @Column(name = "description", length = DESCRIPTION_LENGTH)
    private String description;

    @Basic
    @NotNull
    @Column(name="activeFlag", nullable = false)
    private Boolean activeFlag;
}
