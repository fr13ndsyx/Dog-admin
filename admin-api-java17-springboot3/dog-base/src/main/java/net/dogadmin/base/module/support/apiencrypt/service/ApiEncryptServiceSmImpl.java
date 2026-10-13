package net.dogadmin.base.module.support.apiencrypt.service;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import lombok.extern.slf4j.Slf4j;
import net.dogadmin.base.common.constant.StringConst;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;


@Slf4j
@Service
public class ApiEncryptServiceSmImpl implements ApiEncryptService {

    private final SM2 sm2;

    private final String publicKey;

    public ApiEncryptServiceSmImpl(@Value("${api-encrypt.sm2-private-key}") String privateKeyHex) {
        ECPrivateKeyParameters privateKey = BCUtil.toSm2Params(privateKeyHex);
        ECDomainParameters domain = privateKey.getParameters();
        BigInteger d = privateKey.getD();
        if (d.signum() <= 0 || d.compareTo(domain.getN()) >= 0) {
            throw new IllegalArgumentException("API_ENCRYPT_PRIVATE_KEY 不是有效的 SM2 私钥");
        }
        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(domain.getG().multiply(d).normalize(), domain);
        this.sm2 = new SM2(privateKey, publicKeyParameters);
        this.publicKey = HexUtil.encodeHexStr(publicKeyParameters.getQ().getEncoded(false));
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public String encrypt(String data) {
        return sm2.encryptHex(data, KeyType.PublicKey);
    }

    @Override
    public String decrypt(String data) {
        try {
            return sm2.decryptStr(data, KeyType.PrivateKey);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return StringConst.EMPTY;
        }
    }

}
