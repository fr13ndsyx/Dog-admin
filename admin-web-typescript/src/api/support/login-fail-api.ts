import { postRequest, getRequest } from '/@/lib/axios';

export const loginFailApi = {

  queryPage : (param) => {
    return postRequest('/support/protect/loginFail/queryPage', param);
  },

  batchDelete: (idList) => {
      return postRequest('/support/protect/loginFail/batchDelete', idList);
  },

};
