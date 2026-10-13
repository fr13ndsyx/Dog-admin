package net.dogadmin.base.module.support.apiencrypt.service;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiEncryptServiceSmImplTest {

    private static final String SM2_CURVE_ORDER_HEX = "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123";

    private static final String TEST_ONLY_PRIVATE_KEY = "313304c3991095a05b94776c384dbb3445a5c1b25acd4815d12a557463dcf0ce";

    private static final String SM_CRYPTO_FRONTEND_CIPHER = "045f7f5deee8a467611291df75259f595545aff2edac9c9231d2c9e66381c025fdcdf522eed21e83804c22f48c5db5f8de98e52ae0bc0998db390e0d409d8e928c51bc88bc695aa48704e4515185eeaf05e0c28db26daff0d20bb763ee8160992b113a184573bd67831457aa3e7392800f20274baabe389ac403";

    @Test
    void decryptsCipherProducedByFrontendSmCrypto() {
        assertEquals("dogadmin 中文密码 123", new ApiEncryptServiceSmImpl(TEST_ONLY_PRIVATE_KEY).decrypt(SM_CRYPTO_FRONTEND_CIPHER));
    }

    @Test
    void decryptsWhatPublicKeyHolderEncrypts() {
        SM2 keyPair = new SM2();
        ApiEncryptServiceSmImpl service = new ApiEncryptServiceSmImpl(keyPair.getDHex());
        assertEquals(HexUtil.encodeHexStr(keyPair.getQ(false)), service.getPublicKey());

        SM2 browser = new SM2(null, service.getPublicKey());
        for (String plain : new String[]{"dogadmin login", "中文密码验证", "{\"loginName\":\"admin\"}"}) {
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
