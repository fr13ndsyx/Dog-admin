import _ from 'lodash';
import { App } from 'vue';
import { FLAG_NUMBER_ENUM } from '/@/constants/common-const';
import { Enum, EnumWrapper, EnumItem, EnumPlugin } from '/@/types/enum';

export default {
  install: <T>(app: App, enumWrapper: EnumWrapper<T>): void => {
    const enumPlugin = {} as EnumPlugin;
    /**
     * 根据枚举值获取描述
     * @param {*} constantName 枚举名
     * @param {*} value          枚举值
     * @returns
     */
    enumPlugin.getDescByValue = function (constantName: string, value: any): string {
      if (!enumWrapper || !Object.prototype.hasOwnProperty.call(enumWrapper, constantName)) {
        console.error('无法找到变量名称：' + constantName + '，请检查 /constants/index.js 文件中是否引入此变量！');
        return '';
      }
      // boolean类型需要做特殊处理
      if (constantName === 'FLAG_NUMBER_ENUM' && !_.isUndefined(value) && typeof value === 'boolean') {
        value = value ? FLAG_NUMBER_ENUM.TRUE.value : FLAG_NUMBER_ENUM.FALSE.value;
      }

      let enumObject: Enum<T> = enumWrapper[constantName];
      for (let item in enumObject) {
        if (enumObject[item].value === value) {
          return enumObject[item].desc;
        }
      }
      return '';
    };
    /**
     * 根据枚举名获取对应的描述键值对[{value:desc}]
     * @param {*} constantName 枚举名
     * @returns
     */
    enumPlugin.getValueDescList = function <T>(constantName: string): EnumItem<T>[] {
      if (!Object.prototype.hasOwnProperty.call(enumWrapper, constantName)) {
        console.error('无法找到变量名称：' + constantName + '，请检查 /constants/index.js 文件中是否引入此变量！');
        return [];
      }
      const result: EnumItem<T>[] = [];
      let targetEnum = enumWrapper[constantName];
      for (let item in targetEnum) {
        result.push(targetEnum[item] as any);
      }
      return result;
    };

    /**
     * 根据枚举名获取对应的value描述键值对{value:desc}
     * @param {*} constantName 枚举名
     * @returns
     */
    enumPlugin.getValueDesc = function (constantName: string): { [key: string]: string } {
      if (!Object.prototype.hasOwnProperty.call(enumWrapper, constantName)) {
        console.error('无法找到变量名称：' + constantName + '，请检查 /constants/index.js 文件中是否引入此变量！');
        return {};
      }
      let enumObject = enumWrapper[constantName];
      let result = {} as { [key: string]: string };
      for (let item in enumObject) {
        let key: string = enumObject[item].value + '';
        result[key] = enumObject[item].desc;
      }
      return result;
    };

    app.config.globalProperties.$enumPlugin = enumPlugin;
    app.provide('enumPlugin', enumPlugin);
  },
};
