import { defineStore } from 'pinia';
import { appDefaultConfig } from '/@/config/app-config';
import localStorageKeyConst from '/@/constants/local-storage-key-const';
import { dogSentry } from '/@/lib/dog-sentry';
import { AppConfig } from '/@/types/config';
import { localRead } from '/@/utils/local-util';

let state = {
  ...appDefaultConfig
};

let appConfigStr = localRead(localStorageKeyConst.APP_CONFIG);
let language = appDefaultConfig.language;
if (appConfigStr) {
  try {
    state = { ...appDefaultConfig, ...JSON.parse(appConfigStr) };
    language = state.language;
  } catch (e) {
    dogSentry.captureError(e);
  }
}

if (typeof state.websiteName === 'string' && /^dogadmin\b/i.test(state.websiteName.trim())) {
  state.websiteName = appDefaultConfig.websiteName;
}
state.layout = 'side';
state.pageTagLocation = 'center';
state.pageTagStyle = 'chrome';
delete (state as Partial<AppConfig> & { footerFlag?: boolean }).footerFlag;

/**
 * 获取初始化的语言
 */
export const getInitializedLanguage = function () {
  return language;
};

export const useAppConfigStore = defineStore({
  id: 'appConfig',
  state: (): AppConfig => ({
    // 读取config下的默认配置
    ...state,
    // 全屏
    fullScreenFlag: false,
  }),
  actions: {
    reset() {
      for (const k in appDefaultConfig) {
        this[k] = appDefaultConfig[k];
      }
    },
    showHelpDoc() {
      this.helpDocExpandFlag = true;
    },
    hideHelpDoc() {
      this.helpDocExpandFlag = false;
    },
    startFullScreen() {
      this.fullScreenFlag = true;
    },
    exitFullScreen() {
      this.fullScreenFlag = false;
    },
  },
});
