import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  stages: [
    { duration: "30s", target: 10 },
    { duration: "1m", target: 25 },
    { duration: "2m", target: 40 },
    { duration: "30s", target: 0 },
  ],
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<1200"],
  },
};

const BASE_URL = __ENV.BASE_URL || "http://uamishop-gateway:8080";

export default function () {
  const r = Math.random();

  let res;
  if (r < 0.45) {
    res = http.get(`${BASE_URL}/api/v1/productos`, {
      tags: { name: "productos_list", domain: "catalogo", test_type: "load" },
    });
  } else if (r < 0.65) {
    res = http.get(`${BASE_URL}/api/v1/categorias`, {
      tags: { name: "categorias_list", domain: "catalogo", test_type: "load" },
    });
  } else if (r < 0.85) {
    res = http.get(`${BASE_URL}/api/v1/ordenes`, {
      tags: { name: "ordenes_list", domain: "ordenes", test_type: "load" },
    });
  } else {
    res = http.get(`${BASE_URL}/api/v1/productos/mas-vendidos?limit=10`, {
      tags: {
        name: "productos_mas_vendidos",
        domain: "catalogo",
        test_type: "load",
      },
    });
  }

  check(res, {
    "status 2xx o 3xx": (r) => r.status >= 200 && r.status < 400,
  });

  sleep(1);
}
