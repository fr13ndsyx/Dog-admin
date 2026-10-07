package net.dogadmin.base.module.support.codegenerator.service.variable.backend;

import net.dogadmin.base.module.support.codegenerator.domain.form.CodeGeneratorConfigForm;
import net.dogadmin.base.module.support.codegenerator.service.variable.CodeGenerateBaseVariableService;

import java.util.HashMap;
import java.util.Map;

public class MenuVariableService extends CodeGenerateBaseVariableService {

    @Override
    public boolean isSupport(CodeGeneratorConfigForm form) {
        return true;
    }

    @Override
    public Map<String, Object> getInjectVariablesMap(CodeGeneratorConfigForm form) {
        return new HashMap<>(2);
    }

}
