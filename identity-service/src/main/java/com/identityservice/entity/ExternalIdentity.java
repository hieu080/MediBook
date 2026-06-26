package com.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "external_identities",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_external_identities_provider_subject", columnNames = {"provider", "external_subject"}),
                @UniqueConstraint(name = "uk_external_identities_provider_user", columnNames = {"provider", "user_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "external_subject", nullable = false, length = 150)
    private String externalSubject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_external_identities_user"))
    private User user;

    @Column(name = "username", length = 150)
    private String username;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
