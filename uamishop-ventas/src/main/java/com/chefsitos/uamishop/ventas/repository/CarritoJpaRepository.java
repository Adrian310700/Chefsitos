package com.chefsitos.uamishop.ventas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.shared.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.shared.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.shared.enumeration.EstadoCarrito;

@Repository
public interface CarritoJpaRepository extends JpaRepository<Carrito, CarritoId> {
  // Busca carrito por clienteId y estado
  Optional<Carrito> findByClienteIdAndEstado(ClienteId clienteId, EstadoCarrito estado);

}
