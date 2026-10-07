package net.dogadmin.base.module.support.helpdoc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.dogadmin.base.common.domain.PageResult;
import net.dogadmin.base.common.domain.ResponseDTO;
import net.dogadmin.base.common.util.DogBeanUtil;
import net.dogadmin.base.common.util.DogPageUtil;
import net.dogadmin.base.module.support.helpdoc.dao.HelpDocDao;
import net.dogadmin.base.module.support.helpdoc.domain.entity.HelpDocEntity;
import net.dogadmin.base.module.support.helpdoc.domain.form.HelpDocAddForm;
import net.dogadmin.base.module.support.helpdoc.domain.form.HelpDocQueryForm;
import net.dogadmin.base.module.support.helpdoc.domain.form.HelpDocUpdateForm;
import net.dogadmin.base.module.support.helpdoc.domain.vo.HelpDocDetailVO;
import net.dogadmin.base.module.support.helpdoc.domain.vo.HelpDocVO;
import net.dogadmin.base.module.support.helpdoc.manager.HelpDocManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HelpDocService {

    @Resource
    private HelpDocDao helpDocDao;

    @Resource
    private HelpDocManager helpDaoManager;


    /**
     * 查询 帮助文档
     *
     * @param queryForm
     * @return
     */
    public PageResult<HelpDocVO> query(HelpDocQueryForm queryForm) {
        Page<?> page = DogPageUtil.convert2PageQuery(queryForm);
        List<HelpDocVO> list = helpDocDao.query(page, queryForm);
        return DogPageUtil.convert2PageResult(page, list);
    }

    /**
     * 添加
     *
     * @param addForm
     * @return
     */
    public ResponseDTO<String> add(HelpDocAddForm addForm) {
        HelpDocEntity helpDaoEntity = DogBeanUtil.copy(addForm, HelpDocEntity.class);
        helpDaoManager.save(helpDaoEntity, addForm.getRelationList());
        return ResponseDTO.ok();
    }


    /**
     * 更新
     *
     * @param updateForm
     * @return
     */
    public ResponseDTO<String> update(HelpDocUpdateForm updateForm) {
        // 更新
        HelpDocEntity helpDaoEntity = DogBeanUtil.copy(updateForm, HelpDocEntity.class);
        helpDaoManager.update(helpDaoEntity, updateForm.getRelationList());
        return ResponseDTO.ok();
    }


    /**
     * 删除
     *
     * @param helpDocId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> delete(Long helpDocId) {
        HelpDocEntity helpDaoEntity = helpDocDao.selectById(helpDocId);
        if (helpDaoEntity != null) {
            helpDocDao.deleteById(helpDocId);
            helpDocDao.deleteRelation(helpDocId);
        }
        return ResponseDTO.ok();
    }

    /**
     * 获取详情
     *
     * @param helpDocId
     * @return
     */
    public HelpDocDetailVO getDetail(Long helpDocId) {
        HelpDocEntity helpDaoEntity = helpDocDao.selectById(helpDocId);
        HelpDocDetailVO detail = DogBeanUtil.copy(helpDaoEntity, HelpDocDetailVO.class);
        if (detail != null) {
            detail.setRelationList(helpDocDao.queryRelationByHelpDoc(helpDocId));
        }
        return detail;
    }

    /**
     * 获取详情
     *
     * @param relationId
     * @return
     */
    public List<HelpDocVO> queryHelpDocByRelationId(Long relationId) {
        return helpDocDao.queryHelpDocByRelationId(relationId);
    }
}
