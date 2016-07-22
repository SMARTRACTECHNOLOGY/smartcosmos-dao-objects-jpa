package net.smartcosmos.dao.things.domain;

import java.io.Serializable;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ThingId implements Serializable {

    private UUID id;
    private String type;
    private UUID tenantId;
}
