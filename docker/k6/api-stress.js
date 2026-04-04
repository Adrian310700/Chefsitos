import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  stages: [
    { duration: "30s", target: 20 },
    { duration: "1m", target: 60 },
    { duration: "2m", target: 120 },
    { duration: "1m", target: 180 },
    { duration: "30s", target: 0 },
  ],
  thresholds: {
    http_req_failed: ["rate<0.10"],
    http_req_duration: ["p(95)<2000"],
  },
};

const BASE_URL = __ENV.BASE_URL || "http://uamishop-gateway:8080";

export default function () {
  const responses = http.batch([
    [
      "GET",
      `${BASE_URL}/api/v1/productos`,
      null,
      {
        tags: {
          name: "productos_list",
          domain: "catalogo",
          test_type: "stress",
        },
      },
    ],
    [
      "GET",
      `${BASE_URL}/api/v1/categorias`,
      null,
      {
        tags: {
          name: "categorias_list",
          domain: "catalogo",
          test_type: "stress",
        },
      },
    ],
    [
      "GET",
      `${BASE_URL}/api/v1/ordenes`,
      null,
      {
        tags: { name: "ordenes_list", domain: "ordenes", test_type: "stress" },
      },
    ],
    [
      "GET",
      `${BASE_URL}/api/v1/productos/mas-vendidos?limit=10`,
      null,
      {
        tags: {
          name: "productos_mas_vendidos",
          domain: "catalogo",
          test_type: "stress",
        },
      },
    ],
  ]);

  for (const res of responses) {
    check(res, {
      "status 2xx o 3xx": (r) => r.status >= 200 && r.status < 400,
    });
  }

  sleep(0.5);
}
