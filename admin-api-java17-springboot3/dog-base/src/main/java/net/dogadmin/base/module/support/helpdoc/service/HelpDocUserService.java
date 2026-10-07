package net.dogadmin.base.module.support.helpdoc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.dogadmin.base.common.domain.PageResult;
import net.dogadmin.base.common.domain.RequestUser;
import net.dogadmin.base.common.domain.ResponseDTO;
import net.dogadmin.base.common.util.DogBeanUtil;
import net.dogadmin.base.common.util.DogPageUtil;
import net.dogadmin.base.module.support.helpdoc.dao.HelpDocDao;
import net.dogadmin.base.module.support.helpdoc.domain.entity.HelpDocEntity;
import net.dogadmin.base.module.support.helpdoc.domain.form.HelpDocViewRecordQueryForm;
import net.dogadmin.base.module.support.helpdoc.domain.vo.HelpDocDetailVO;
import net.dogadmin.base.module.support.helpdoc.domain.vo.HelpDocVO;
import net.dogadmin.base.module.support.helpdoc.domain.vo.HelpDocViewRecordVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HelpDocUserService {

    @Resource
    private HelpDocDao helpDocDao;


    /**
     * 查询全部 帮助文档
     *
     * @return
     */
    public ResponseDTO<List<HelpDocVO>> queryAllHelpDocList() {
        return ResponseDTO.ok(helpDocDao.queryAllHelpDocList());
    }


    /**
     * 查询我的 待查看的 帮助文档清单
     *
     * @return
     */
    public ResponseDTO<HelpDocDetailVO> view(RequestUser requestUser, Long helpDocId) {
        HelpDocEntity helpDocEntity = helpDocDao.selectById(helpDocId);
        if (helpDocEntity == null) {
            return ResponseDTO.userErrorParam("帮助文档不存在");
        }

        HelpDocDetailVO helpDocDetailVO = DogBeanUtil.copy(helpDocEntity, HelpDocDetailVO.class);
        long viewCount = helpDocDao.viewRecordCount(helpDocId, requestUser.getUserId());
        if (viewCount == 0) {
            helpDocDao.insertViewRecord(helpDocId, requestUser.getUserId(), requestUser.getUserName(), requestUser.getIp(), requestUser.getUserAgent(), 1);
            helpDocDao.updateViewCount(helpDocId, 1, 1);
            helpDocDetailVO.setPageViewCount(helpDocDetailVO.getPageViewCount() + 1);
            helpDocDetailVO.setUserViewCount(helpDocDetailVO.getUserViewCount() + 1);
        } else {
            helpDocDao.updateViewRecord(helpDocId, requestUser.getUserId(), requestUser.getIp(), requestUser.getUserAgent());
            helpDocDao.updateViewCount(helpDocId, 0, 1);
            helpDocDetailVO.setPageViewCount(helpDocDetailVO.getPageViewCount() + 1);
        }

        return ResponseDTO.ok(helpDocDetailVO);
    }


    /**
     * 分页查询  查看记录
     *
     * @param helpDocViewRecordQueryForm
     * @return
     */
    public PageResult<HelpDocViewRecordVO> queryViewRecord(HelpDocViewRecordQueryForm helpDocViewRecordQueryForm) {
        Page<?> page = DogPageUtil.convert2PageQuery(helpDocViewRecordQueryForm);
        List<HelpDocViewRecordVO> noticeViewRecordVOS = helpDocDao.queryViewRecordList(page, helpDocViewRecordQueryForm);
        return DogPageUtil.convert2PageResult(page, noticeViewRecordVOS);
    }
}
