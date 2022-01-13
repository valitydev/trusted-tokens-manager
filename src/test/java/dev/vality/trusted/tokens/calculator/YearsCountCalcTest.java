package dev.vality.trusted.tokens.calculator;

import dev.vality.trusted.tokens.model.CardTokenData;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

class YearsCountCalcTest {

    @Test
    void getCountYears() {
        HashMap<Integer, CardTokenData.YearsData> years = new HashMap<>();
        years.put(2021, CardTokenData.YearsData.builder()
                .yearSum(1000L)
                .yearCount(3)
                .months(new HashMap<>())
                .build());
        YearsCountCalc.getCountYears(years, 2);
    }
}
