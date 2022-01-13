package dev.vality.trusted.tokens.utils;

import dev.vality.trusted.tokens.model.CardTokenData;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CardTokenDataUtils {

    public static CardTokenData createCardTokenData() {
        CardTokenData cardTokenData = new CardTokenData();
        cardTokenData.setPayments(createCurrencyData(ConditionTemplateUtils.PAYMENT));
        cardTokenData.setWithdrawals(createCurrencyData(ConditionTemplateUtils.WITHDRAWAL));
        return cardTokenData;
    }

    private static Map<String, CardTokenData.CurrencyData> createCurrencyData(String transactionType) {
        int currentYear = LocalDateTime.now().getYear();
        CardTokenData.CurrencyData currencyData = new CardTokenData.CurrencyData();
        Map<Integer, CardTokenData.YearsData> yearsDataMap = new HashMap<>();
        for (int i = currentYear; i > currentYear - 4; i--) {
            CardTokenData.YearsData yearsData = new CardTokenData.YearsData();
            Map<Integer, CardTokenData.MonthsData> monthsDataMap = new HashMap<>();
            int yearCount = 0;
            long yearSum = 0;
            for (int j = 1; j <= 12; j++) {
                CardTokenData.MonthsData monthsData = new CardTokenData.MonthsData();
                monthsData.setMonthCount(j);
                if (transactionType.equals(ConditionTemplateUtils.PAYMENT)) {
                    monthsData.setMonthSum(j * 1000L);
                    yearSum += monthsData.getMonthSum();
                }
                monthsDataMap.put(j, monthsData);
                yearCount += monthsData.getMonthCount();
            }
            yearsData.setMonths(monthsDataMap);
            yearsData.setYearCount(yearCount);
            if (transactionType.equals(ConditionTemplateUtils.PAYMENT)) {
                yearsData.setYearSum(yearSum);
            }
            yearsDataMap.put(i, yearsData);
        }
        currencyData.setYears(yearsDataMap);
        Map<String, CardTokenData.CurrencyData> currencyDataMap = new HashMap<>();
        currencyDataMap.put("RUB", currencyData);
        currencyDataMap.put("EUR", currencyData);
        currencyDataMap.put("USD", currencyData);
        return currencyDataMap;
    }

}
