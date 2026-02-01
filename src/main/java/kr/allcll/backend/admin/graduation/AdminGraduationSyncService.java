package kr.allcll.backend.admin.graduation;

import java.util.ArrayList;
import java.util.List;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredArea;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredAreaExclusion;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredAreaExclusionRepository;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredCourseAreaMap;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredCourseAreaMapRepository;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredRule;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredRuleRepository;
import kr.allcll.backend.domain.graduation.certification.ClassicCertCriterion;
import kr.allcll.backend.domain.graduation.certification.ClassicCertCriterionRepository;
import kr.allcll.backend.domain.graduation.certification.CodingCertCriterion;
import kr.allcll.backend.domain.graduation.certification.CodingCertCriterionRepository;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.certification.EnglishCertCriterion;
import kr.allcll.backend.domain.graduation.certification.EnglishCertCriterionRepository;
import kr.allcll.backend.domain.graduation.certification.EnglishTargetType;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRule;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleRepository;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleType;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.domain.graduation.credit.CreditCriterion;
import kr.allcll.backend.domain.graduation.credit.CreditCriterionRepository;
import kr.allcll.backend.domain.graduation.credit.RequiredCourse;
import kr.allcll.backend.domain.graduation.credit.RequiredCourseRepository;
import kr.allcll.backend.domain.graduation.department.DeptGroup;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.support.sheet.GraduationSheetTable;
import kr.allcll.backend.support.sheet.GraduationSheetsFetcher;
import kr.allcll.backend.support.sheet.GraduationSheetsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminGraduationSyncService {

    private final GraduationSheetsFetcher graduationSheetsFetcher;
    private final RequiredCourseRepository requiredCourseRepository;
    private final CreditCriterionRepository creditCriterionRepository;
    private final GraduationSheetsProperties graduationSheetsProperties;
    private final GraduationCertRuleRepository graduationCertRuleRepository;
    private final BalanceRequiredRuleRepository balanceRequiredRuleRepository;
    private final CodingCertCriterionRepository codingCertCriterionRepository;
    private final ClassicCertCriterionRepository classicCertCriterionRepository;
    private final EnglishCertCriterionRepository englishCertCriterionRepository;
    private final GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;
    private final BalanceRequiredCourseAreaMapRepository balanceRequiredCourseAreaMapRepository;
    private final BalanceRequiredAreaExclusionRepository balanceRequiredAreaExclusionRepository;

    @Transactional
    public void syncGraduationRules() {
        syncCreditCriteria();
        syncRequiredCourses();

        syncBalanceRequiredRule();
        syncBalanceRequiredCourseAreaMap();
        syncBalanceRequiredAreaExclusion();

        syncGraduationCertRule();
        syncEnglishCertCriteria();
        syncCodingCertCriteria();
        syncClassicCertCriteria();

        syncGraduationDepartmentInfo();
    }

    private void syncCreditCriteria() {
        String tabName = graduationSheetsProperties.tabs().get("credit-criteria");
        GraduationSheetTable graduationSheetTable = graduationSheetsFetcher.fetchAsTable(tabName);

        List<CreditCriterion> creditCriterionList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            CreditCriterion creditCriterion = new CreditCriterion(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getEnum(row, "major_type", MajorType.class),
                graduationSheetTable.getString(row, "dept_cd"),
                graduationSheetTable.getEnum(row, "major_scope", MajorScope.class),
                graduationSheetTable.getEnum(row, "category_type", CategoryType.class),
                graduationSheetTable.getInt(row, "required_credits"),
                graduationSheetTable.getBoolean(row, "enabled"),
                graduationSheetTable.getString(row, "note")
            );
            creditCriterionList.add(creditCriterion);
        }

        creditCriterionRepository.deleteAllInBatch();
        creditCriterionRepository.saveAll(creditCriterionList);
    }

    private void syncRequiredCourses() {
        String tabName = graduationSheetsProperties.tabs().get("required-courses");
        GraduationSheetTable graduationSheetTable = graduationSheetsFetcher.fetchAsTable(tabName);

        List<RequiredCourse> requiredCourseList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            RequiredCourse requiredCourse = new RequiredCourse(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getString(row, "dept_cd"),
                graduationSheetTable.getEnum(row, "category_type", CategoryType.class),
                graduationSheetTable.getString(row, "curi_no"),
                graduationSheetTable.getString(row, "curi_nm"),
                graduationSheetTable.getString(row, "alt_group"),
                graduationSheetTable.getBoolean(row, "required"),
                graduationSheetTable.getString(row, "note")
            );
            requiredCourseList.add(requiredCourse);
        }

        requiredCourseRepository.deleteAllInBatch();
        requiredCourseRepository.saveAll(requiredCourseList);
    }

    private void syncBalanceRequiredRule() {
        String tabName = graduationSheetsProperties.tabs().get("balance-required-rules");
        GraduationSheetTable graduationSheetTable = graduationSheetsFetcher.fetchAsTable(tabName);

        List<BalanceRequiredRule> balanceRequiredRuleList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            BalanceRequiredRule balanceRequiredRule = new BalanceRequiredRule(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getInt(row, "required_areas_cnt"),
                graduationSheetTable.getInt(row, "required_credits"),
                graduationSheetTable.getString(row, "note")
            );
            balanceRequiredRuleList.add(balanceRequiredRule);
        }

        balanceRequiredRuleRepository.deleteAllInBatch();
        balanceRequiredRuleRepository.saveAll(balanceRequiredRuleList);
    }

    private void syncBalanceRequiredCourseAreaMap() {
        String tabName = graduationSheetsProperties.tabs().get("balance-required-course-area-map");
        GraduationSheetTable graduationSheetTable = graduationSheetsFetcher.fetchAsTable(tabName);

        List<BalanceRequiredCourseAreaMap> balanceRequiredCourseAreaMapList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            BalanceRequiredCourseAreaMap balanceRequiredCourseAreaMap = new BalanceRequiredCourseAreaMap(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getString(row, "curi_no"),
                graduationSheetTable.getString(row, "curi_nm"),
                graduationSheetTable.getEnum(row, "balance_required_area", BalanceRequiredArea.class)
            );
            balanceRequiredCourseAreaMapList.add(balanceRequiredCourseAreaMap);
        }

        balanceRequiredCourseAreaMapRepository.deleteAllInBatch();
        balanceRequiredCourseAreaMapRepository.saveAll(balanceRequiredCourseAreaMapList);
    }

    private void syncBalanceRequiredAreaExclusion() {
        String tabName = graduationSheetsProperties.tabs().get("balance-required-area-exclusions");
        GraduationSheetTable graduationSheetTable = graduationSheetsFetcher.fetchAsTable(tabName);

        List<BalanceRequiredAreaExclusion> balanceRequiredAreaExclusionList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            BalanceRequiredAreaExclusion balanceRequiredAreaExclusion = new BalanceRequiredAreaExclusion(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getEnum(row, "dept_group", DeptGroup.class),
                graduationSheetTable.getEnum(row, "balance_required_area", BalanceRequiredArea.class)
            );
            balanceRequiredAreaExclusionList.add(balanceRequiredAreaExclusion);
        }

        balanceRequiredAreaExclusionRepository.deleteAllInBatch();
        balanceRequiredAreaExclusionRepository.saveAll(balanceRequiredAreaExclusionList);
    }

    private void syncGraduationCertRule() {
        String tabName = graduationSheetsProperties.tabs().get("graduation-cert-rules");
        GraduationSheetTable graduationSheetTable = graduationSheetsFetcher.fetchAsTable(tabName);

        List<GraduationCertRule> graduationCertRuleList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            GraduationCertRule graduationCertRule = new GraduationCertRule(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getEnum(row, "graduation_cert_rule_type", GraduationCertRuleType.class),
                graduationSheetTable.getInt(row, "required_pass_count"),
                graduationSheetTable.getBoolean(row, "enable_english"),
                graduationSheetTable.getBoolean(row, "enable_classic"),
                graduationSheetTable.getBoolean(row, "enable_coding"),
                graduationSheetTable.getString(row, "note")
            );
            graduationCertRuleList.add(graduationCertRule);
        }

        graduationCertRuleRepository.deleteAllInBatch();
        graduationCertRuleRepository.saveAll(graduationCertRuleList);
    }

    private void syncEnglishCertCriteria() {
        String tabName = graduationSheetsProperties.tabs().get("english-cert-criteria");
        GraduationSheetTable graduationSheetTable = graduationSheetsFetcher.fetchAsTable(tabName);

        List<EnglishCertCriterion> englishCertCriterionList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            EnglishCertCriterion englishCertCriterion = new EnglishCertCriterion(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getEnum(row, "english_target_type", EnglishTargetType.class),
                graduationSheetTable.getInt(row, "toeic_min_score"),
                graduationSheetTable.getInt(row, "toefl_ibt_min_score"),
                graduationSheetTable.getInt(row, "teps_min_score"),
                graduationSheetTable.getInt(row, "new_teps_min_score"),
                graduationSheetTable.getString(row, "opic_min_level"),
                graduationSheetTable.getString(row, "toeic_speaking_min_level"),
                graduationSheetTable.getInt(row, "gtelp_level"),
                graduationSheetTable.getInt(row, "gtelp_min_score"),
                graduationSheetTable.getInt(row, "gtelp_speaking_level"),
                graduationSheetTable.getString(row, "alt_course_name"),
                graduationSheetTable.getInt(row, "alt_course_credit"),
                graduationSheetTable.getString(row, "note")
            );
            englishCertCriterionList.add(englishCertCriterion);
        }

        englishCertCriterionRepository.deleteAllInBatch();
        englishCertCriterionRepository.saveAll(englishCertCriterionList);
    }

    private void syncCodingCertCriteria() {
        String tabName = graduationSheetsProperties.tabs().get("coding-cert-criteria");
        GraduationSheetTable graduationSheetTable = graduationSheetsFetcher.fetchAsTable(tabName);

        List<CodingCertCriterion> codingCertCriterionList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            CodingCertCriterion codingCertCriterion = new CodingCertCriterion(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getEnum(row, "coding_target_type", CodingTargetType.class),
                graduationSheetTable.getInt(row, "tosc_min_level"),
                graduationSheetTable.getString(row, "alt1_curi_no"),
                graduationSheetTable.getString(row, "alt1_min_grade"),
                graduationSheetTable.getString(row, "alt2_curi_no"),
                graduationSheetTable.getString(row, "alt2_min_grade"),
                graduationSheetTable.getString(row, "note")
            );
            codingCertCriterionList.add(codingCertCriterion);
        }

        codingCertCriterionRepository.deleteAllInBatch();
        codingCertCriterionRepository.saveAll(codingCertCriterionList);
    }

    private void syncClassicCertCriteria() {
        String tabName = graduationSheetsProperties.tabs().get("classic-cert-criteria");
        GraduationSheetTable graduationSheetTable = graduationSheetsFetcher.fetchAsTable(tabName);

        List<ClassicCertCriterion> classicCertCriterionList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            ClassicCertCriterion classicCertCriterion = new ClassicCertCriterion(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getInt(row, "total_required_count"),
                graduationSheetTable.getInt(row, "required_count_western"),
                graduationSheetTable.getInt(row, "required_count_eastern"),
                graduationSheetTable.getInt(row, "required_count_eastern_and_western"),
                graduationSheetTable.getInt(row, "required_count_science"),
                graduationSheetTable.getString(row, "note")
            );
            classicCertCriterionList.add(classicCertCriterion);
        }

        classicCertCriterionRepository.deleteAllInBatch();
        classicCertCriterionRepository.saveAll(classicCertCriterionList);
    }

    private void syncGraduationDepartmentInfo() {
        String tabName = graduationSheetsProperties.tabs().get("graduation-department-info");
        GraduationSheetTable graduationSheetTable = graduationSheetsFetcher.fetchAsTable(tabName);

        List<GraduationDepartmentInfo> graduationDepartmentInfoList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            GraduationDepartmentInfo graduationDepartmentInfo = new GraduationDepartmentInfo(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getString(row, "dept_nm"),
                graduationSheetTable.getString(row, "dept_cd"),
                graduationSheetTable.getString(row, "college_nm"),
                graduationSheetTable.getEnum(row, "dept_group", DeptGroup.class),
                graduationSheetTable.getEnum(row, "english_target_type", EnglishTargetType.class),
                graduationSheetTable.getEnum(row, "coding_target_type", CodingTargetType.class),
                graduationSheetTable.getString(row, "note")
            );
            graduationDepartmentInfoList.add(graduationDepartmentInfo);
        }

        graduationDepartmentInfoRepository.deleteAllInBatch();
        graduationDepartmentInfoRepository.saveAll(graduationDepartmentInfoList);
    }
}
