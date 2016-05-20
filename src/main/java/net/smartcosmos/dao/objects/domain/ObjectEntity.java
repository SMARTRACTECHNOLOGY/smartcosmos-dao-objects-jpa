package net.smartcosmos.dao.objects.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
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
import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author voor
 */
@Entity(name = "object")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
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

    @NotEmpty
    @Size(max = OBJECT_URN_LENGTH)
    @Column(name="objectUrn", length = OBJECT_URN_LENGTH, nullable = false, updatable = false)
    private String objectUrn;

    @NotEmpty
    @Size(max = TYPE_LENGTH)
    @Column(name = "type", length = TYPE_LENGTH, nullable = false)
    private String type;

    @NotEmpty
    @Type(type = "uuid-binary")
    @Column(name = "accountUuid", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID accountId;

    @CreatedDate
    @Column(name = "createdTimestamp", insertable = true, updatable = false)
    private Long created;

    @LastModifiedDate
//    @Column(name = "lastModifiedTimestamp", insertable = false, updatable = true) // lastModified only set on update, might be used later
    // lastModified already set on create (v2 compatibility)
    @Column(name = "lastModifiedTimestamp", nullable = false, insertable = true, updatable = true)
    private Long lastModified;

    @Size(max = MONIKER_LENGTH)
    @Column(name = "moniker", length = MONIKER_LENGTH, nullable = true, updatable = true)
    private String moniker;

    @NotEmpty
    @Size(max = NAME_LENGTH)
    @Column(name = "name", length = NAME_LENGTH, nullable = false)
    private String name;

    @Size(max = DESCRIPTION_LENGTH)
    @Column(name = "description", length = DESCRIPTION_LENGTH)
    private String description;

    @Basic
    @NotNull
    @Column(name="activeFlag", nullable = false)
    private Boolean activeFlag;

    /*
        Lombok's @Builder is not able to deal with field initialization default values. That's a known issue which won't get fixed:
        https://github.com/rzwitserloot/lombok/issues/663

        We therefore provide our own AllArgsConstructor that is used by the generated builder and takes care of field initialization.
     */
    @Builder
    @ConstructorProperties({"id", "objectUrn", "type", "accountId", "created", "lastModified", "moniker", "name", "description",
        "activeFlag"})
    protected ObjectEntity(UUID id,
                           String objectUrn,
                           String type,
                           UUID accountId,
                           Long created,
                           Long lastModified,
                           String moniker,
                           String name,
                           String description,
                           Boolean activeFlag)
    {
        this.id = id;
        this.objectUrn = objectUrn;
        this.type = type;
        this.accountId = accountId;
        this.created = created;
        this.lastModified = lastModified;
        this.moniker = moniker;
        this.name = name;
        this.description = description;
        this.activeFlag = activeFlag != null ? activeFlag : true;
    }
}
