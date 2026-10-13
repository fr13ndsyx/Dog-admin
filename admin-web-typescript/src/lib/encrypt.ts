import CryptoSM from 'sm-crypto';
import { getRequest, request } from '/@/lib/axios';

const SM2_CIPHER_MODE_C1C3C2 = 1;
const SM2_UNCOMPRESSED_POINT_PREFIX = '04';

function object2string(data) {
  if (typeof data === 'object') {
    return JSON.stringify(data);
  }

  let str = JSON.stringify(data);
  if (str.startsWith("'") || str.startsWith('"')) {
    str = str.substring(1);
  }
  if (str.endsWith("'") || str.endsWith('"')) {
    str = str.substring(0, str.length - 1);
  }
  return str;
}

export async function encryptData(data: unknown): Promise<string | null> {
  if (!data) {
    return null;
  }
  const publicKeyResponse = await getRequest('/login/getEncryptPublicKey');
  return SM2_UNCOMPRESSED_POINT_PREFIX + CryptoSM.sm2.doEncrypt(object2string(data), publicKeyResponse.data, SM2_CIPHER_MODE_C1C3C2);
}

export async function postEncryptRequest(url: string, data: unknown): ReturnType<typeof request> {
  return request({
    data: { encryptData: await encryptData(data) },
    url,
    method: 'post',
  });
}
