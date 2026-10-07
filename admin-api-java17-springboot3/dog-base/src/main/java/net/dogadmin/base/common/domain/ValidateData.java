package net.dogadmin.base.common.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidateData<T> {

    @NotNull(message = "数据不能为空哦")
    private T data;
}