package com.chefsitos.uamishop.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chefsitos.uamishop.catalogo.domain.aggregate.Producto;
import com.chefsitos.uamishop.catalogo.domain.valueObject.ProductoId;

@Repository
public interface ProductoJpaRepository extends JpaRepository<Producto, ProductoId> {

}
