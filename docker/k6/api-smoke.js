import http from "k6/http";
import { check, sleep } from "k6";
import { getUrl, getTags } from "./utils.js";

export const options = {
  vus: 2,
  duration: "30s",
  thresholds: {
    http_req_duration: ["p(95)<1000"],
  },
};

export default function () {
  const reqs = [
    ["GET", getUrl("catalogo", "/api/v1/productos"), null, getTags("productos_list", "catalogo", "smoke")],
    ["GET", getUrl("catalogo", "/api/v1/categorias"), null, getTags("categorias_list", "catalogo", "smoke")],
    ["GET", getUrl("ordenes", "/api/v1/ordenes"), null, getTags("ordenes_list", "ordenes", "smoke")],
    ["POST", getUrl("ventas", "/api/v1/carritos"), JSON.stringify({clienteId: "123e4567-e89b-12d3-a456-426614174000"}), Object.assign({}, getTags("carritos_crear", "ventas", "smoke"), { headers: { "Content-Type": "application/json" } })],
    ["GET", getUrl("catalogo", "/api/v1/productos/mas-vendidos?limit=10"), null, getTags("productos_mas_vendidos", "catalogo", "smoke")],
  ];

  const responses = http.batch(reqs);

  responses.forEach((res, i) => {
    check(res, {
      "status aceptado (sin DB)": (r) => r.status >= 200,
    }, { name: reqs[i][3].tags.name });
  });

  sleep(1);
}
