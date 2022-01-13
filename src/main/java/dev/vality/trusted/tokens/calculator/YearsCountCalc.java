package dev.vality.trusted.tokens.calculator;

import dev.vality.trusted.tokens.model.CardTokenData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class YearsCountCalc {

    public static int getCountYears(Map<Integer, CardTokenData.YearsData> years, Integer yearsOffset) {
        Integer lastYearToCalc = LocalDateTime.now().getYear() - yearsOffset;
        CardTokenData.YearsData yearsData = years.get(lastYearToCalc);
        if (yearsData == null) {
            return 0;
        }
        return yearsOffset == 0
                ? years.get(LocalDateTime.now().getYear()).getYearCount()
                : calculateFullYearsCount(years, lastYearToCalc) + calculateMonthsCount(yearsData.getMonths());
    }

    private static int calculateFullYearsCount(Map<Integer, CardTokenData.YearsData> years, Integer lastYearToCalc) {
        return years.entrySet().stream()
                .filter(year -> year.getKey() > lastYearToCalc)
                .mapToInt(year -> year.getValue().getYearCount())
                .sum();
    }

    private static int calculateMonthsCount(Map<Integer, CardTokenData.MonthsData> months) {
        Integer currentMonth = LocalDateTime.now().getMonthValue();
        return months.entrySet().stream()
                .filter(month -> month.getKey() > currentMonth)
                .mapToInt(month -> month.getValue().getMonthCount())
                .sum();
    }
}
