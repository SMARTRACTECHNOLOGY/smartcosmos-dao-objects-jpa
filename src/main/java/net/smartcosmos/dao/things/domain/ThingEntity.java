package net.smartcosmos.dao.things.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.UUID;

@Entity(name = "thing")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@EntityListeners({ AuditingEntityListener.class })
@Table(name = "thing", uniqueConstraints = @UniqueConstraint(columnNames = { "id", "tenantId" }) )
public class ThingEntity implements Serializable {

    private static final int UUID_LENGTH = 16;
    private static final int TYPE_LENGTH = 255;

    /*
        Without setting an appropriate Hibernate naming strategy, the column names specified in the @Column annotations below will be converted
        from camel case to underscore, e.g.: systemUuid -> system_uuid

        To avoid that, select the "old" naming strategy org.hibernate.cfg.EJB3NamingStrategy in your configuration (smartcosmos-ext-objects-rdao.yml):
        jpa.hibernate.naming_strategy: org.hibernate.cfg.EJB3NamingStrategy
     */

    @Id
    @Type(type = "uuid-binary")
    @Column(name = "id", length = UUID_LENGTH)
    private UUID id;

    @NotEmpty
    @Size(max = TYPE_LENGTH)
    @Column(name = "type", length = TYPE_LENGTH, nullable = false, updatable = false)
    private String type;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = "tenantId", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID tenantId;

    @CreatedDate
    @Column(name = "createdTimestamp", insertable = true, updatable = false)
    private Long created;

    @LastModifiedDate
//    @Column(name = "lastModifiedTimestamp", insertable = false, updatable = true) // lastModified only set on update, might be used later
    // lastModified already set on create (v2 compatibility)
    @Column(name = "lastModifiedTimestamp", nullable = false, insertable = true, updatable = true)
    private Long lastModified;

    @Basic
    @NotNull
    @Column(name="active", nullable = false)
    private Boolean active;

    /*
        Lombok's @Builder is not able to deal with field initialization default values. That's a known issue which won't get fixed:
        https://github.com/rzwitserloot/lombok/issues/663

        We therefore provide our own AllArgsConstructor that is used by the generated builder and takes care of field initialization.
     */
    @Builder
    @ConstructorProperties({"id", "type", "tenantId", "created", "lastModified", "active"})
    protected ThingEntity(UUID id,
                          String type,
                          UUID tenantId,
                          Long created,
                          Long lastModified,
                          Boolean active)
    {
        this.id = id;
        this.type = type;
        this.tenantId = tenantId;
        this.created = created;
        this.lastModified = lastModified;
        this.active = active != null ? active : true;
    }
}
