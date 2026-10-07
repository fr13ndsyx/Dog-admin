import { postRequest, getRequest } from '/@/lib/axios';

export const level3ProtectApi = {
  getConfig: () => {
    return getRequest('/support/protect/level3protect/getConfig');
  },

  updateConfig: (form) => {
    return postRequest('/support/protect/level3protect/updateConfig', form);
  },
};
