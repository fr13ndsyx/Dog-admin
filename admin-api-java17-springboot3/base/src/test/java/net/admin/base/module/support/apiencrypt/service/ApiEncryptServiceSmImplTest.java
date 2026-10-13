package net.admin.base.module.support.apiencrypt.service;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiEncryptServiceSmImplTest {

    private static final String SM2_CURVE_ORDER_HEX = "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123";

    private static final String TEST_ONLY_PRIVATE_KEY = "313304c3991095a05b94776c384dbb3445a5c1b25acd4815d12a557463dcf0ce";

    private static final String SM_CRYPTO_FRONTEND_CIPHER = "04c1071e6b8c669aa336fd9e27e0a3a3682d0efc941cb5d15daed824a6157c5546158f4e96cae7fdd4718e93397a4cf99285de22d6e4f802dc9597b43e44c620c9eb659f6d533fb1f0aac1431c0b2c2cc67432925edb727831eac261488d408bb59d1118b7e08c6ccac6009bf5309444777f4c0909a81b";

    @Test
    void decryptsCipherProducedByFrontendSmCrypto() {
        assertEquals("admin 中文密码 123", new ApiEncryptServiceSmImpl(TEST_ONLY_PRIVATE_KEY).decrypt(SM_CRYPTO_FRONTEND_CIPHER));
    }

    @Test
    void decryptsWhatPublicKeyHolderEncrypts() {
        SM2 keyPair = new SM2();
        ApiEncryptServiceSmImpl service = new ApiEncryptServiceSmImpl(keyPair.getDHex());
        assertEquals(HexUtil.encodeHexStr(keyPair.getQ(false)), service.getPublicKey());

        SM2 browser = new SM2(null, service.getPublicKey());
        for (String plain : new String[]{"admin login", "中文密码验证", "{\"loginName\":\"admin\"}"}) {
            assertEquals(plain, service.decrypt(browser.encryptHex(plain, KeyType.PublicKey)));
            assertEquals(plain, service.decrypt(service.encrypt(plain)));
        }
    }

    @Test
    void otherKeyCannotDecrypt() {
        ApiEncryptServiceSmImpl service = new ApiEncryptServiceSmImpl(new SM2().getDHex());
        ApiEncryptServiceSmImpl other = new ApiEncryptServiceSmImpl(new SM2().getDHex());
        assertEquals("", other.decrypt(service.encrypt("secret")));
        assertEquals("", service.decrypt("not-a-ciphertext"));
    }

    @Test
    void rejectsInvalidPrivateKey() {
        for (String invalid : new String[]{"00", SM2_CURVE_ORDER_HEX}) {
            assertThrows(IllegalArgumentException.class, () -> new ApiEncryptServiceSmImpl(invalid));
        }
    }
}
