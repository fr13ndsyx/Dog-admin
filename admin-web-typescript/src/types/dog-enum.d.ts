export interface DogEnumWrapper<T> {
  [key: string]: DogEnum<T>;
}

export interface DogEnum<T> {
  [key: string]: DogEnumItem<T>;
}

interface DogEnumItem<T> {
  value: T;
  desc: string;
}

interface DogEnumPlugin {
  getDescByValue(constantName: string, value: string | number | undefined): string;

  getValueDescList(constantName: string): DogEnumItem[];

  getValueDesc(constantName: string): { [key: string]: string };
}
