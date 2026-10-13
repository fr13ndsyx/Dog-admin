
import { Enum } from '/@/types/enum';

export const LOGIN_DEVICE_ENUM: Enum<number> = {
  PC: {
    value: 1,
    desc: '电脑端',
  },
  ANDROID: {
    value: 2,
    desc: '安卓',
  },
  APPLE: {
    value: 3,
    desc: '苹果',
  },
  H5: {
    value: 3,
    desc: 'H5',
  },
};

export default {
  LOGIN_DEVICE_ENUM,
};
