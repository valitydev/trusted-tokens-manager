package dev.vality.trusted.tokens.service;

import dev.vality.trusted.tokens.ConditionTemplate;
import dev.vality.trusted.tokens.ConditionTemplateAlreadyExists;
import dev.vality.trusted.tokens.ConditionTemplateNotFound;
import dev.vality.trusted.tokens.ConditionTemplateRequest;
import dev.vality.trusted.tokens.converter.RowConverter;
import dev.vality.trusted.tokens.model.Row;
import dev.vality.trusted.tokens.repository.ConditionTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateService {

    private final ConditionTemplateRepository conditionTemplateRepository;
    private final RowConverter rowConverter;

    public void createTemplate(ConditionTemplateRequest conditionTemplateRequest)
            throws ConditionTemplateAlreadyExists {
        ConditionTemplate conditionTemplate = conditionTemplateRepository.get(conditionTemplateRequest.getName());
        if (conditionTemplate != null) {
            throw new ConditionTemplateAlreadyExists();
        }
        Row row = rowConverter.convert(conditionTemplateRequest.getName(),
                conditionTemplateRequest.getTemplate());
        conditionTemplateRepository.create(row);
    }

    public ConditionTemplate getConditionTemplate(String conditionTemplateName) throws TException {
        ConditionTemplate conditionTemplate = conditionTemplateRepository.get(conditionTemplateName);
        if (conditionTemplate == null) {
            throw new ConditionTemplateNotFound();
        }
        return conditionTemplate;
    }

}
