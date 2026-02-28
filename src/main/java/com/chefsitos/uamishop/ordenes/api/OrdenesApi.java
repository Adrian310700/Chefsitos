package com.chefsitos.uamishop.ordenes.api;

import java.util.UUID;
import com.chefsitos.uamishop.ordenes.api.dto.OrdenDTO;

public interface OrdenesApi {

  OrdenDTO buscarPorId(UUID id);

}
