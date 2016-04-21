package net.smartcosmos.dao.objects.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.*;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@Table(name = "object", uniqueConstraints = @UniqueConstraint(columnNames = { "objectUrn",
        "accountUrn" }) )
public class ObjectEntity implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "systemUuid", columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(nullable = false, updatable = false)
    private String objectUrn;

    @NotNull
    private String type;

    @NotNull
    @Column(name = "accountUrn")
    private String accountUrn;

    @CreatedDate
    @Column(name = "CREATED", insertable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED", insertable = false, updatable = true)
    private Date lastModified;

    private String moniker;

    private String name;

    private String description;

    private Boolean activeFlag;
}
