package com.chefsitos.uamishop.ordenes.repository;

import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;
import com.chefsitos.uamishop.ordenes.domain.valueObject.OrdenId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenJpaRepository extends JpaRepository<Orden, OrdenId> {
}
