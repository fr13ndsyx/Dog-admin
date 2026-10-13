
import { Enum } from '/@/types/enum';

export const GENDER_ENUM: Enum<number> = {
  UNKNOWN: {
    value: 0,
    desc: '未知',
  },
  MAN: {
    value: 1,
    desc: '男',
  },
  WOMAN: {
    value: 2,
    desc: '女',
  },
}

export default {
  GENDER_ENUM,
}
