package com.example.kwu_graduation.domain.simulation.service;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.CheckItem;
import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.CheckStatus;
import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.hakbun2526.engine.RequirementChecker;
import com.example.kwu_graduation.domain.requirements.hakbun2526.spec.Department;
import com.example.kwu_graduation.domain.requirements.hakbun2526.spec.GraduationRequirement;
import com.example.kwu_graduation.domain.requirements.hakbun2526.spec.RequirementSpecLoader;
import com.example.kwu_graduation.domain.simulation.dto.GraduationSimulationRequest;
import com.example.kwu_graduation.domain.simulation.dto.GraduationSimulationResponse;
import com.example.kwu_graduation.domain.simulation.dto.GraduationSummaryResponse;
import com.example.kwu_graduation.domain.simulation.dto.RequirementGapResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GraduationSimulationService {

    private final SimulationKlasGradeReader klasGradeReader;
    private final GraduationCreditCalculator creditCalculator;
    private final RequirementSpecLoader specLoader;
    private final RequirementChecker requirementChecker;
    private final RequirementGapCalculator gapCalculator;

    public GraduationSimulationResponse simulate(String cookie, GraduationSimulationRequest request) {
        validateRequest(request);

        Department department = parseDepartment(request.department());
        GraduationRequirement requirement = loadRequirement(request.admissionYear(), department);
        List<KlasSemesterGradeResponse> semesters = klasGradeReader.readSemesters(cookie);
        CreditSummaryResponse credits = creditCalculator.calculateCredits(
                semesters,
                request.includeInProgressCoursesOrDefault()
        );
        GraduationSummaryResponse summary = creditCalculator.calculate(
                semesters,
                request.includeInProgressCoursesOrDefault()
        );
        RequirementCheckResponse checkResponse = requirementChecker.check(
                requirement,
                credits,
                flattenSubjects(semesters),
                toRequirementCheckRequest(request)
        );
        List<RequirementGapResponse> gaps = gapCalculator.calculate(checkResponse.items());

        return new GraduationSimulationResponse(
                checkResponse.admissionYear(),
                checkResponse.department(),
                checkResponse.graduatable(),
                checkResponse.totalRequired(),
                checkResponse.totalEarned(),
                summary,
                gaps,
                warningsFrom(checkResponse.items()),
                checkResponse.items()
        );
    }

    private void validateRequest(GraduationSimulationRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청 본문은 필수입니다.");
        }
        if (request.admissionYear() < 2023 || request.admissionYear() > 2026) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "admissionYear는 2023~2026이어야 합니다.");
        }
        if (request.department() == null || request.department().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "department는 필수입니다.");
        }
        if (request.currentGradeYear() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "currentGradeYear는 1 이상이어야 합니다.");
        }
        if (request.currentSemester() != 1 && request.currentSemester() != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "currentSemester는 1 또는 2여야 합니다.");
        }
    }

    private Department parseDepartment(String department) {
        try {
            return Department.fromCode(department);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    private GraduationRequirement loadRequirement(int admissionYear, Department department) {
        try {
            return specLoader.load(admissionYear, department);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    private List<KlasSubjectGradeResponse> flattenSubjects(List<KlasSemesterGradeResponse> semesters) {
        return safeSemesters(semesters).stream()
                .flatMap(semester -> semester.sungjukList() == null
                        ? Stream.empty()
                        : semester.sungjukList().stream())
                .toList();
    }

    private List<KlasSemesterGradeResponse> safeSemesters(List<KlasSemesterGradeResponse> semesters) {
        return semesters == null ? List.of() : semesters;
    }

    private RequirementCheckRequest toRequirementCheckRequest(GraduationSimulationRequest request) {
        return new RequirementCheckRequest(
                request.multiMajorCompleted(),
                request.graduationProjectCompleted(),
                null,
                request.engineeringProgram(),
                request.topcitCompleted(),
                request.subMajor()
        );
    }

    private List<String> warningsFrom(List<CheckItem> items) {
        List<String> warnings = safeItems(items).stream()
                .filter(item -> item.status() == CheckStatus.NEEDS_INPUT)
                .map(item -> "%s: %s".formatted(item.name(), item.message()))
                .toList();
        if (!warnings.isEmpty()) {
            return warnings;
        }
        return List.of();
    }

    private List<CheckItem> safeItems(List<CheckItem> items) {
        return items == null ? List.of() : items;
    }
}
