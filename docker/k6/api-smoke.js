import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: 2,
  duration: "30s",
  thresholds: {
    http_req_failed: ["rate<0.02"],
    http_req_duration: ["p(95)<1000"],
  },
};

const BASE_URL = __ENV.BASE_URL || "http://uamishop-gateway:8080";

function get(url, name) {
  return http.get(url, { tags: { name, test_type: "smoke" } });
}

export default function () {
  const responses = http.batch([
    [
      "GET",
      `${BASE_URL}/api/v1/productos`,
      null,
      { tags: { name: "productos_list", test_type: "smoke" } },
    ],
    [
      "GET",
      `${BASE_URL}/api/v1/categorias`,
      null,
      { tags: { name: "categorias_list", test_type: "smoke" } },
    ],
    [
      "GET",
      `${BASE_URL}/api/v1/ordenes`,
      null,
      { tags: { name: "ordenes_list", test_type: "smoke" } },
    ],
    [
      "GET",
      `${BASE_URL}/api/v1/productos/mas-vendidos?limit=10`,
      null,
      { tags: { name: "productos_mas_vendidos", test_type: "smoke" } },
    ],
  ]);

  for (const res of responses) {
    check(res, {
      "status 2xx o 3xx": (r) => r.status >= 200 && r.status < 400,
    });
  }

  sleep(1);
}
