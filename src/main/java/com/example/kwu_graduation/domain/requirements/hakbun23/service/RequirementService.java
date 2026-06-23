package com.example.kwu_graduation.domain.requirements.hakbun23.service;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.requirements.hakbun23.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.hakbun23.dto.RequirementItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequirementService {

    private static final int CULTURE_REQUIRED = 22;
    private static final int MAJOR_SINGLE_REQUIRED = 60;
    private static final int MAJOR_DOUBLE_REQUIRED = 54;
    private static final int TOTAL_REQUIRED = 133;

    public RequirementCheckResponse check(boolean isDoubleMajor, CreditSummaryResponse credit) {
        int majorRequired = isDoubleMajor ? MAJOR_DOUBLE_REQUIRED : MAJOR_SINGLE_REQUIRED;

        List<RequirementItem> items = List.of(
                RequirementItem.of("총 취득학점", credit.chidukHakjum(), TOTAL_REQUIRED),
                RequirementItem.of("전공 취득학점", credit.majorChidukHakjum(), majorRequired),
                RequirementItem.of("교양 취득학점", credit.cultureChidukHakjum(), CULTURE_REQUIRED)
        );

        return RequirementCheckResponse.of(items);
    }
}