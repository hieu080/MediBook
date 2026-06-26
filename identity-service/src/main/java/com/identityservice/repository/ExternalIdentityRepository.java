package com.identityservice.repository;

import com.identityservice.entity.ExternalIdentity;
import com.identityservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExternalIdentityRepository extends JpaRepository<ExternalIdentity, Long> {
    Optional<ExternalIdentity> findByProviderAndExternalSubject(String provider, String externalSubject);

    Optional<ExternalIdentity> findByProviderAndUser(String provider, User user);
}
