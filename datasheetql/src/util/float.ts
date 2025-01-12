
export function numberEqual(a: number, b: number, epsilon: number = 1e-15): boolean {
  return Math.abs(a - b) < epsilon
}