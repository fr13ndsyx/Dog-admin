package net.admin.base.module.support.dict.manager;

import jakarta.annotation.Resource;
import net.admin.base.common.util.BeanUtil;
import net.admin.base.constant.CacheKeyConst;
import net.admin.base.module.support.dict.dao.DictDao;
import net.admin.base.module.support.dict.dao.DictDataDao;
import net.admin.base.module.support.dict.domain.entity.DictDataEntity;
import net.admin.base.module.support.dict.domain.entity.DictEntity;
import net.admin.base.module.support.dict.domain.vo.DictDataVO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;



@Service
public class DictManager {

    @Resource
    private DictDao dictDao;

    @Resource
    private DictDataDao dictDataDao;


    /**
     * 获取字典
     */
    @Cacheable(value = CacheKeyConst.Dict.DICT_DATA, key = "#dictCode + '_' + #dataValue")
    public DictDataVO getDictData(String dictCode, String dataValue) {
        DictEntity dictEntity = dictDao.selectByCode(dictCode);
        if (dictEntity == null) {
            return null;
        }

        DictDataEntity dictDataEntity = dictDataDao.selectByDictIdAndValue(dictEntity.getDictId(), dataValue);
        return BeanUtil.copy(dictDataEntity, DictDataVO.class);
    }

}
