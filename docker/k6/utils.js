// docker/k6/utils.js

const MODE = __ENV.ROUTING_MODE || "gateway";
const GATEWAY_URL = __ENV.BASE_URL || "http://uamishop-gateway:8080";

const SERVICES = {
  catalogo: "http://uamishop-catalogo:8081",
  ordenes: "http://uamishop-ordenes:8082",
  ventas: "http://uamishop-ventas:8083"
};

/**
 * Retorna la URL apuntando al microservicio directo o al API gateway.
 */
export function getUrl(domain, path) {
  if (MODE === "direct") {
    return `${SERVICES[domain]}${path}`;
  }
  return `${GATEWAY_URL}${path}`;
}

/**
 * Retorna un objeto con las etiquetas estructuradas
 */
export function getTags(name, domain, type) {
  return { tags: { name, domain, test_type: type } };
}
