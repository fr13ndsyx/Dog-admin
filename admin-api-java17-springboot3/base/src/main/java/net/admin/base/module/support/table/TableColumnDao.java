package net.admin.base.module.support.table;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.admin.base.module.support.table.domain.TableColumnEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TableColumnDao extends BaseMapper<TableColumnEntity> {

    TableColumnEntity selectByUserIdAndTableId(@Param("userId") Long userId, @Param("userType") Integer userType, @Param("tableId") Integer tableId);

    void deleteTableColumn(@Param("userId") Long userId, @Param("userType") Integer userType, @Param("tableId") Integer tableId);
}
