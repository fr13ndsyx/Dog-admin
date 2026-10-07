package net.dogadmin.base.module.support.feedback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.dogadmin.base.common.domain.PageResult;
import net.dogadmin.base.common.domain.RequestUser;
import net.dogadmin.base.common.domain.ResponseDTO;
import net.dogadmin.base.common.util.DogBeanUtil;
import net.dogadmin.base.common.util.DogPageUtil;
import net.dogadmin.base.module.support.feedback.dao.FeedbackDao;
import net.dogadmin.base.module.support.feedback.domain.FeedbackAddForm;
import net.dogadmin.base.module.support.feedback.domain.FeedbackEntity;
import net.dogadmin.base.module.support.feedback.domain.FeedbackQueryForm;
import net.dogadmin.base.module.support.feedback.domain.FeedbackVO;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class FeedbackService {

    @Resource
    private FeedbackDao feedbackDao;

    /**
     * 分页查询
     *
     */
    public ResponseDTO<PageResult<FeedbackVO>> query(FeedbackQueryForm queryForm) {
        Page page = DogPageUtil.convert2PageQuery(queryForm);
        List<FeedbackVO> list = feedbackDao.queryPage(page, queryForm);
        PageResult<FeedbackVO> pageResultDTO = DogPageUtil.convert2PageResult(page, list);
        if (pageResultDTO.getEmptyFlag()) {
            return ResponseDTO.ok(pageResultDTO);
        }
        return ResponseDTO.ok(pageResultDTO);
    }

    /**
     * 新建
     */
    public ResponseDTO<String> add(FeedbackAddForm addForm, RequestUser requestUser) {
        FeedbackEntity feedback = DogBeanUtil.copy(addForm, FeedbackEntity.class);
        feedback.setUserType(requestUser.getUserType().getValue());
        feedback.setUserId(requestUser.getUserId());
        feedback.setUserName(requestUser.getUserName());
        feedbackDao.insert(feedback);
        return ResponseDTO.ok();
    }
}
