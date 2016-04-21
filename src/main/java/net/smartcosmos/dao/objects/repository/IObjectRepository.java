package net.smartcosmos.dao.objects.repository;

import net.smartcosmos.dao.objects.domain.ObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author voor
 */
public interface IObjectRepository extends JpaRepository<ObjectEntity, UUID> {

    Optional<ObjectEntity> findByObjectUrn(String objectUrn);
}
