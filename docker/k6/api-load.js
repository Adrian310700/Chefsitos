import http from "k6/http";
import { check, sleep } from "k6";
import { getUrl, getTags } from "./utils.js";

export const options = {
  stages: [
    { duration: "30s", target: 10 },
    { duration: "1m", target: 25 },
    { duration: "2m", target: 40 },
    { duration: "30s", target: 0 },
  ],
  thresholds: {
    http_req_duration: ["p(95)<1200"],
  },
};

export default function () {
  const r = Math.random();

  let res, tagName;
  if (r < 0.3) {
    tagName = "productos_list";
    res = http.get(getUrl("catalogo", "/api/v1/productos"), getTags(tagName, "catalogo", "load"));
  } else if (r < 0.5) {
    tagName = "categorias_list";
    res = http.get(getUrl("catalogo", "/api/v1/categorias"), getTags(tagName, "catalogo", "load"));
  } else if (r < 0.7) {
    tagName = "ordenes_list";
    res = http.get(getUrl("ordenes", "/api/v1/ordenes"), getTags(tagName, "ordenes", "load"));
  } else if (r < 0.85) {
    tagName = "carritos_crear";
    res = http.post(getUrl("ventas", "/api/v1/carritos"), JSON.stringify({clienteId: "123e4567-e89b-12d3-a456-426614174000"}), Object.assign({}, getTags(tagName, "ventas", "load"), { headers: { "Content-Type": "application/json" } }));
  } else {
    tagName = "productos_mas_vendidos";
    res = http.get(getUrl("catalogo", "/api/v1/productos/mas-vendidos?limit=10"), getTags(tagName, "catalogo", "load"));
  }

  check(res, {
    "status aceptado (sin DB)": (r) => r.status >= 200,
  }, { name: tagName });

  sleep(1);
}
