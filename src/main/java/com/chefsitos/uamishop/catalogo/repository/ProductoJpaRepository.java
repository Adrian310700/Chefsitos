package com.chefsitos.uamishop.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chefsitos.uamishop.catalogo.domain.aggregate.Producto;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;

public interface ProductoJpaRepository extends JpaRepository<Producto, ProductoId> {
}
