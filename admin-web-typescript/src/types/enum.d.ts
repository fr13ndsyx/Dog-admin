export interface EnumWrapper<T> {
  [key: string]: Enum<T>;
}

export interface Enum<T> {
  [key: string]: EnumItem<T>;
}

interface EnumItem<T> {
  value: T;
  desc: string;
}

interface EnumPlugin {
  getDescByValue(constantName: string, value: string | number | undefined): string;

  getValueDescList(constantName: string): EnumItem[];

  getValueDesc(constantName: string): { [key: string]: string };
}
