package com.chefsitos.uamishop.ventas.repository;

import org.springframework.data.repository.CrudRepository;

import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;

public interface CarritoRepository extends CrudRepository<Carrito, CarritoId> {

}
