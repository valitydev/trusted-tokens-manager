package dev.vality.trusted.tokens.calculator;

import dev.vality.trusted.tokens.model.CardTokenData;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

class YearsSumCalcTest {

    @Test
    void getSumYears() {
        HashMap<Integer, CardTokenData.YearsData> years = new HashMap<>();
        years.put(2021, CardTokenData.YearsData.builder()
                .yearSum(1000L)
                .yearCount(3)
                .months(new HashMap<>())
                .build());
        YearsSumCalc.getSumYears(years, 2);
    }
}
