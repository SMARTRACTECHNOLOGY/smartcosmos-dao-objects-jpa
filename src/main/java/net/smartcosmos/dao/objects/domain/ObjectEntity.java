package net.smartcosmos.dao.objects.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
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

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "systemUuid", columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name="objectUrn", nullable = false, updatable = false)
    private String objectUrn;

    @NotNull
    private String type;

    @NotNull
    @Column(name = "accountUuid")
    private UUID accountUrn;

    @CreatedDate
    @Column(name = "created", insertable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @Column(name = "lastModifiedTimestamp", insertable = false, updatable = true)
    private Date lastModified;

    private String moniker;

    private String name;

    private String description;

    @Column(name="activeFlag")
    private Boolean activeFlag;
}
