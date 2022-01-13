package dev.vality.trusted.tokens.handler;

import dev.vality.trusted.tokens.ConditionTemplate;
import dev.vality.trusted.tokens.ConditionTemplateRequest;
import dev.vality.trusted.tokens.TrustedTokensSrv;
import dev.vality.trusted.tokens.calculator.ConditionTrustedResolver;
import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.repository.CardTokenRepository;
import dev.vality.trusted.tokens.service.TemplateService;
import dev.vality.trusted.tokens.validator.ConditionTemplateValidator;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrustedTokensHandler implements TrustedTokensSrv.Iface {

    private final TemplateService templateService;
    private final ConditionTemplateValidator conditionTemplateValidator;
    private final CardTokenRepository cardTokenRepository;
    private final ConditionTrustedResolver conditionTrustedResolver;

    @Override
    public boolean isTokenTrusted(String cardToken, ConditionTemplate conditionTemplate)
            throws TException {
        conditionTemplateValidator.validate(conditionTemplate);
        CardTokenData cardTokenData = cardTokenRepository.get(cardToken);
        return conditionTrustedResolver.isTrusted(cardTokenData, conditionTemplate);
    }

    @Override
    public boolean isTokenTrustedByConditionTemplateName(String cardToken, String conditionTemplateName)
            throws TException {
        ConditionTemplate conditionTemplate = templateService.getConditionTemplate(conditionTemplateName);
        CardTokenData cardTokenData = cardTokenRepository.get(cardToken);
        return conditionTrustedResolver.isTrusted(cardTokenData, conditionTemplate);
    }

    @Override
    public void createNewConditionTemplate(ConditionTemplateRequest conditionTemplateRequest)
            throws TException {
        conditionTemplateValidator.validate(conditionTemplateRequest.getTemplate());
        templateService.createTemplate(conditionTemplateRequest);
    }
}
