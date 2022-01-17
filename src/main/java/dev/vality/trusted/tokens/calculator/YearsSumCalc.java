package dev.vality.trusted.tokens.calculator;

import dev.vality.trusted.tokens.model.CardTokenData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class YearsSumCalc {

    public static long getSumYears(Map<Integer, CardTokenData.YearsData> years, Integer yearsOffset) {
        Integer lastYearToCalc = LocalDateTime.now().getYear() - yearsOffset;
        CardTokenData.YearsData yearsData = years.get(lastYearToCalc);
        if (yearsData == null) {
            return 0;
        }
        return yearsOffset == 0
                ? years.get(LocalDateTime.now().getYear()).getYearSum()
                : calculateFullYearsSum(years, lastYearToCalc) + calculateMonthsSum(yearsData.getMonths());
    }

    private static long calculateFullYearsSum(Map<Integer, CardTokenData.YearsData> years, Integer lastYearToCalc) {
        return years.entrySet().stream()
                .filter(year -> year.getKey() > lastYearToCalc)
                .mapToLong(year -> year.getValue().getYearSum())
                .sum();
    }

    private static long calculateMonthsSum(Map<Integer, CardTokenData.MonthsData> months) {
        Integer currentMonth = LocalDateTime.now().getMonthValue();
        return months.entrySet().stream()
                .filter(month -> month.getKey() > currentMonth)
                .mapToLong(month -> month.getValue().getMonthSum())
                .sum();
    }
}
