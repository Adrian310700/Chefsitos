package com.chefsitos.uamishop.ventas.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.chefsitos.uamishop.ventas.domain.EstadoCarrito;
import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ClienteId;

@Repository
public interface CarritoRepository extends CrudRepository<Carrito, CarritoId> {

    Optional<Carrito> findByClienteIdAndEstado(ClienteId clienteId, EstadoCarrito estado);

}
