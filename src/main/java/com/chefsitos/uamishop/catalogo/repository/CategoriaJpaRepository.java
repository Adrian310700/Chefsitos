package com.chefsitos.uamishop.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chefsitos.uamishop.catalogo.domain.entity.Categoria;
import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;

@Repository
public interface CategoriaJpaRepository extends JpaRepository<Categoria, CategoriaId> {
}
