package net.admin.base.module.support.mail;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.admin.base.module.support.mail.domain.MailTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
public interface MailTemplateDao extends BaseMapper<MailTemplateEntity> {

}
