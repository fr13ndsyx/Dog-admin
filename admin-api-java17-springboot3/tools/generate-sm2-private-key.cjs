const crypto = require('node:crypto');

const SM2_CURVE_ORDER = BigInt('0xFFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123');
const PRIVATE_KEY_BYTES = 32;

let privateKey = 0n;
while (privateKey === 0n || privateKey >= SM2_CURVE_ORDER) {
  privateKey = BigInt('0x' + crypto.randomBytes(PRIVATE_KEY_BYTES).toString('hex'));
}

process.stdout.write('新生成的 SM2 私钥（填入后端环境变量 API_ENCRYPT_PRIVATE_KEY，不要发给别人，不要提交进 git）：\n\n');
process.stdout.write(privateKey.toString(16).padStart(PRIVATE_KEY_BYTES * 2, '0') + '\n\n');
