import { useSpinStore } from "/@/store/modules/system/spin";

export const DogLoading = {
  show: () => {
    useSpinStore().show();
  },

  hide: () => {
    useSpinStore().hide();
  },
};
