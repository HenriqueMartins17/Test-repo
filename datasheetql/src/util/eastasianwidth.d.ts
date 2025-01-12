declare module "eastasianwidth" {
  declare function eastAsianWidth(char: string): 'F' | 'H' | 'W' | 'Na' | 'A' | 'N'
}