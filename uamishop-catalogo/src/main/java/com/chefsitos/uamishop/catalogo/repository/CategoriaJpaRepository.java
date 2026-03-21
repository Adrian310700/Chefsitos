package com.chefsitos.uamishop.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chefsitos.uamishop.catalogo.domain.entity.Categoria;
import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;

public interface CategoriaJpaRepository extends JpaRepository<Categoria, CategoriaId> {
}
