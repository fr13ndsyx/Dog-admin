import { postRequest, getRequest } from '/@/lib/axios';

export const dictApi = {

  getAllDict: () => {
    return getRequest('/support/dict/getAllDict');
  },

  getAllDictData: () => {
    return getRequest('/support/dict/getAllDictData');
  },

  queryDict: (param) => {
    return postRequest('/support/dict/queryPage', param);
  },

  addDict: (param) => {
    return postRequest('/support/dict/add', param);
  },

  updateDict: (param) => {
    return postRequest('/support/dict/update', param);
  },

  batchDeleteDict: (dictIdList) => {
    return postRequest('/support/dict/batchDelete', dictIdList);
  },

  updateDisabled: (dictId) => {
    return getRequest(`/support/dict/updateDisabled/${dictId}`);
  },

  // ------------- 查询字典数据 -------------

  queryDictData: (dictId) => {
    return getRequest(`/support/dict/dictData/queryDictData/${dictId}`);
  },

  addDictData: (param) => {
    return postRequest('/support/dict/dictData/add', param);
  },

  updateDictData: (param) => {
    return postRequest('/support/dict/dictData/update', param);
  },

  batchDeleteDictData: (dictDataIdList) => {
    return postRequest('/support/dict/dictData/batchDelete', dictDataIdList);
  },

  updateDictDataDisabled: (dictDataId) => {
    return getRequest(`/support/dict/dictData/updateDisabled/${dictDataId}`);
  },
};
