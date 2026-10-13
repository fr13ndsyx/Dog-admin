import menu from './system/menu-const';
import { LOGIN_DEVICE_ENUM } from './system/login-device-const';
import { FLAG_NUMBER_ENUM, GENDER_ENUM, USER_TYPE_ENUM } from './common-const';
import { LAYOUT_ENUM } from './layout-const';
import file from './support/file-const';
import loginLog from './support/login-log-const';
import changeLogConst from './support/change-log-const';
import dictConst from './support/dict-const';

export default {
  FLAG_NUMBER_ENUM,
  LOGIN_DEVICE_ENUM,
  GENDER_ENUM,
  USER_TYPE_ENUM,
  LAYOUT_ENUM,
  ...loginLog,
  ...menu,
  ...file,
  ...changeLogConst,
  ...dictConst,
};
