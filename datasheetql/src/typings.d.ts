declare type NonEmptyArray<T> = [T, ...T[]]

declare type Optional<T, K extends keyof T> = Pick<Partial<T>, K> & Omit<T, K>
