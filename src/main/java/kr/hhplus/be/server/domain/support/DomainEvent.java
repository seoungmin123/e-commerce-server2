package kr.hhplus.be.server.domain.support;

public interface DomainEvent  {
    String eventType();
    Long entityId();
}