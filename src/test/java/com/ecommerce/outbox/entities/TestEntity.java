package com.ecommerce.outbox.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table
public class TestEntity {

    @Id
    private UUID id;
    @Column
    private String contextId;

    public TestEntity(UUID id, String contextId) {
        this.id = id;
        this.contextId = contextId;
    }

    public TestEntity() {
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof TestEntity that)) return false;
        return id.equals(that.id) && Objects.equals(contextId, that.contextId);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + Objects.hashCode(contextId);
        return result;
    }

    @Override
    public String toString() {
        return "TestEntity{" +
                "id=" + id +
                ", contextId='" + contextId + '\'' +
                '}';
    }

}
