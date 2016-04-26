package net.smartcosmos.dao.objects.repository;

import java.util.Optional;
import java.util.UUID;

import net.smartcosmos.dao.objects.domain.ObjectEntity;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author voor
 */
public interface IObjectRepository extends JpaRepository<ObjectEntity, UUID> {

    Optional<ObjectEntity> findByObjectUrn(String objectUrn);
}
