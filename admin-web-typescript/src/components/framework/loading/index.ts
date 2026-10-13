import { useSpinStore } from "/@/store/modules/system/spin";

export const Loading = {
  show: () => {
    useSpinStore().show();
  },

  hide: () => {
    useSpinStore().hide();
  },
};
