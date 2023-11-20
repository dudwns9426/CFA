package com.project.domain.entity;

import java.time.Instant;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@MappedSuperclass
public class BaseTimeEntity {

    private Instant createdDate;
    private Instant modifiedDate;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdDate = now;
        modifiedDate = now;
    }

    @PreUpdate
    public void preUpdate() {
        modifiedDate = Instant.now();
    }
}