import _ from 'lodash';
import { App } from 'vue';
import { FLAG_NUMBER_ENUM } from '/@/constants/common-const';
import { DogEnum, DogEnumWrapper, DogEnumItem, DogEnumPlugin } from '/@/types/dog-enum';

export default {
  install: <T>(app: App, dogEnumWrapper: DogEnumWrapper<T>): void => {
    const dogEnumPlugin = {} as DogEnumPlugin;
    /**
     * 根据枚举值获取描述
     * @param {*} constantName 枚举名
     * @param {*} value          枚举值
     * @returns
     */
    dogEnumPlugin.getDescByValue = function (constantName: string, value: any): string {
      if (!dogEnumWrapper || !Object.prototype.hasOwnProperty.call(dogEnumWrapper, constantName)) {
        console.error('无法找到变量名称：' + constantName + '，请检查 /constants/index.js 文件中是否引入此变量！');
        return '';
      }
      // boolean类型需要做特殊处理
      if (constantName === 'FLAG_NUMBER_ENUM' && !_.isUndefined(value) && typeof value === 'boolean') {
        value = value ? FLAG_NUMBER_ENUM.TRUE.value : FLAG_NUMBER_ENUM.FALSE.value;
      }

      let dogEnum: DogEnum<T> = dogEnumWrapper[constantName];
      for (let item in dogEnum) {
        if (dogEnum[item].value === value) {
          return dogEnum[item].desc;
        }
      }
      return '';
    };
    /**
     * 根据枚举名获取对应的描述键值对[{value:desc}]
     * @param {*} constantName 枚举名
     * @returns
     */
    dogEnumPlugin.getValueDescList = function <T>(constantName: string): DogEnumItem<T>[] {
      if (!Object.prototype.hasOwnProperty.call(dogEnumWrapper, constantName)) {
        console.error('无法找到变量名称：' + constantName + '，请检查 /constants/index.js 文件中是否引入此变量！');
        return [];
      }
      const result: DogEnumItem<T>[] = [];
      let targetDogEnum = dogEnumWrapper[constantName];
      for (let item in targetDogEnum) {
        result.push(targetDogEnum[item] as any);
      }
      return result;
    };

    /**
     * 根据枚举名获取对应的value描述键值对{value:desc}
     * @param {*} constantName 枚举名
     * @returns
     */
    dogEnumPlugin.getValueDesc = function (constantName: string): { [key: string]: string } {
      if (!Object.prototype.hasOwnProperty.call(dogEnumWrapper, constantName)) {
        console.error('无法找到变量名称：' + constantName + '，请检查 /constants/index.js 文件中是否引入此变量！');
        return {};
      }
      let dogEnum = dogEnumWrapper[constantName];
      let result = {} as { [key: string]: string };
      for (let item in dogEnum) {
        let key: string = dogEnum[item].value + '';
        result[key] = dogEnum[item].desc;
      }
      return result;
    };

    app.config.globalProperties.$dogEnumPlugin = dogEnumPlugin;
    app.provide('dogEnumPlugin', dogEnumPlugin);
  },
};
