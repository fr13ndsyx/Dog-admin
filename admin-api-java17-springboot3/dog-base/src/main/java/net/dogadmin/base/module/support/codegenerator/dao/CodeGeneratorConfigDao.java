package net.dogadmin.base.module.support.codegenerator.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.dogadmin.base.module.support.codegenerator.domain.entity.CodeGeneratorConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
public interface CodeGeneratorConfigDao extends BaseMapper<CodeGeneratorConfigEntity> {

}