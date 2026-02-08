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
import kr.allcll.backend.domain.graduation.credit.CourseReplacement;
import kr.allcll.backend.domain.graduation.credit.CourseReplacementRepository;
import kr.allcll.backend.domain.graduation.credit.CreditCriterion;
import kr.allcll.backend.domain.graduation.credit.CreditCriterionRepository;
import kr.allcll.backend.domain.graduation.credit.DoubleCreditCriterion;
import kr.allcll.backend.domain.graduation.credit.DoubleCreditCriterionRepository;
import kr.allcll.backend.domain.graduation.credit.RequiredCourse;
import kr.allcll.backend.domain.graduation.credit.RequiredCourseRepository;
import kr.allcll.backend.domain.graduation.department.DeptGroup;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.support.sheet.GraduationSheetTable;
import kr.allcll.backend.support.sheet.GraduationSheetFetcher;
import kr.allcll.backend.support.sheet.GraduationSheetProperties;
import kr.allcll.backend.support.sheet.validation.BalanceRequiredAreaExclusionsSheetValidator;
import kr.allcll.backend.support.sheet.validation.BalanceRequiredCourseAreaMapSheetValidator;
import kr.allcll.backend.support.sheet.validation.BalanceRequiredRulesSheetValidator;
import kr.allcll.backend.support.sheet.validation.ClassicCertCriteriaSheetValidator;
import kr.allcll.backend.support.sheet.validation.CodingCertCriteriaSheetValidator;
import kr.allcll.backend.support.sheet.validation.CourseReplacementsSheetValidator;
import kr.allcll.backend.support.sheet.validation.CreditCriteriaSheetValidator;
import kr.allcll.backend.support.sheet.validation.DoubleCreditCriteriaSheetValidator;
import kr.allcll.backend.support.sheet.validation.EnglishCertCriteriaSheetValidator;
import kr.allcll.backend.support.sheet.validation.GraduationCertRulesSheetValidator;
import kr.allcll.backend.support.sheet.validation.GraduationDepartmentInfoSheetValidator;
import kr.allcll.backend.support.sheet.validation.GraduationSheetValidatorRegistry;
import kr.allcll.backend.support.sheet.validation.RequiredCoursesSheetValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminGraduationSyncService {

    private final GraduationSheetFetcher graduationSheetFetcher;
    private final RequiredCourseRepository requiredCourseRepository;
    private final CreditCriterionRepository creditCriterionRepository;
    private final GraduationSheetProperties graduationSheetProperties;
    private final CourseReplacementRepository courseReplacementRepository;
    private final GraduationCertRuleRepository graduationCertRuleRepository;
    private final BalanceRequiredRuleRepository balanceRequiredRuleRepository;
    private final CodingCertCriterionRepository codingCertCriterionRepository;
    private final ClassicCertCriterionRepository classicCertCriterionRepository;
    private final EnglishCertCriterionRepository englishCertCriterionRepository;
    private final DoubleCreditCriterionRepository doubleCreditCriterionRepository;
    private final GraduationSheetValidatorRegistry graduationSheetValidatorRegistry;
    private final GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;
    private final BalanceRequiredCourseAreaMapRepository balanceRequiredCourseAreaMapRepository;
    private final BalanceRequiredAreaExclusionRepository balanceRequiredAreaExclusionRepository;

    @Transactional
    public void syncGraduationRules() {
        log.info("[졸업요건 데이터 동기화] 시작");
        syncCreditCriteria();
        syncDoubleCreditCriteria();
        syncRequiredCourses();
        syncCourseReplacements();

        syncBalanceRequiredRule();
        syncBalanceRequiredCourseAreaMap();
        syncBalanceRequiredAreaExclusion();

        syncGraduationCertRule();
        syncEnglishCertCriteria();
        syncCodingCertCriteria();
        syncClassicCertCriteria();

        syncGraduationDepartmentInfo();
        log.info("[졸업요건 데이터 동기화] 완료");
    }

    private GraduationSheetTable fetchAndValidate(final String tabKey) {
        String tabName = graduationSheetProperties.tabName(tabKey);
        GraduationSheetTable sheetTable = graduationSheetFetcher.fetchAsTable(tabName);

        log.info("[졸업요건 시트 검증 시작] tabKey={}", tabKey);
        graduationSheetValidatorRegistry.get(tabKey).validate(sheetTable);
        log.info("[졸업요건 시트 검증 완료] tabKey={}", tabKey);

        return sheetTable;
    }

    private void syncCreditCriteria() {
        String tabKey = CreditCriteriaSheetValidator.TAB_KEY;
        GraduationSheetTable graduationSheetTable = fetchAndValidate(tabKey);

        List<CreditCriterion> creditCriterionList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            CreditCriterion creditCriterion = new CreditCriterion(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getEnum(row, "major_type", MajorType.class),
                graduationSheetTable.getString(row, "dept_cd"),
                graduationSheetTable.getString(row, "dept_nm"),
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

        log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료", tabKey, creditCriterionList.size());
    }

    private void syncDoubleCreditCriteria() {
        String tabKey = DoubleCreditCriteriaSheetValidator.TAB_KEY;
        GraduationSheetTable graduationSheetTable = fetchAndValidate(tabKey);

        List<DoubleCreditCriterion> doubleCreditCriterionList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            DoubleCreditCriterion doubleCreditCriterion = new DoubleCreditCriterion(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getEnum(row, "major_type", MajorType.class),
                graduationSheetTable.getString(row, "primary_dept_cd"),
                graduationSheetTable.getString(row, "primary_dept_nm"),
                graduationSheetTable.getString(row, "secondary_dept_cd"),
                graduationSheetTable.getString(row, "secondary_dept_nm"),
                graduationSheetTable.getEnum(row, "major_scope", MajorScope.class),
                graduationSheetTable.getEnum(row, "category_type", CategoryType.class),
                graduationSheetTable.getInt(row, "required_credits"),
                graduationSheetTable.getBoolean(row, "enabled"),
                graduationSheetTable.getString(row, "note")
            );
            doubleCreditCriterionList.add(doubleCreditCriterion);
        }

        doubleCreditCriterionRepository.deleteAllInBatch();
        doubleCreditCriterionRepository.saveAll(doubleCreditCriterionList);

        log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료", tabKey, doubleCreditCriterionList.size());
    }

    private void syncRequiredCourses() {
        String tabKey = RequiredCoursesSheetValidator.TAB_KEY;
        GraduationSheetTable graduationSheetTable = fetchAndValidate(tabKey);

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

        log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료", tabKey, requiredCourseList.size());
    }

    private void syncCourseReplacements() {
        String tabKey = CourseReplacementsSheetValidator.TAB_KEY;
        GraduationSheetTable graduationSheetTable = fetchAndValidate(tabKey);

        List<CourseReplacement> courseReplacementList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            CourseReplacement courseReplacement = new CourseReplacement(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getString(row, "legacy_curi_nm"),
                graduationSheetTable.getString(row, "current_curi_no"),
                graduationSheetTable.getString(row, "current_curi_nm"),
                graduationSheetTable.getBoolean(row, "enabled"),
                graduationSheetTable.getString(row, "note")
            );
            courseReplacementList.add(courseReplacement);
        }

        courseReplacementRepository.deleteAllInBatch();
        courseReplacementRepository.saveAll(courseReplacementList);

        log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료", tabKey, courseReplacementList.size());
    }


    private void syncBalanceRequiredRule() {
        String tabKey = BalanceRequiredRulesSheetValidator.TAB_KEY;
        GraduationSheetTable graduationSheetTable = fetchAndValidate(tabKey);

        List<BalanceRequiredRule> balanceRequiredRuleList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            BalanceRequiredRule balanceRequiredRule = new BalanceRequiredRule(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getString(row, "dept_cd"),
                graduationSheetTable.getString(row, "dept_nm"),
                graduationSheetTable.getBoolean(row, "required"),
                graduationSheetTable.getInt(row, "required_areas_cnt"),
                graduationSheetTable.getInt(row, "required_credits"),
                graduationSheetTable.getString(row, "note")
            );
            balanceRequiredRuleList.add(balanceRequiredRule);
        }

        balanceRequiredRuleRepository.deleteAllInBatch();
        balanceRequiredRuleRepository.saveAll(balanceRequiredRuleList);

        log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료", tabKey, balanceRequiredRuleList.size());
    }

    private void syncBalanceRequiredCourseAreaMap() {
        String tabKey = BalanceRequiredCourseAreaMapSheetValidator.TAB_KEY;
        GraduationSheetTable graduationSheetTable = fetchAndValidate(tabKey);

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

        log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료", tabKey, balanceRequiredCourseAreaMapList.size());
    }

    private void syncBalanceRequiredAreaExclusion() {
        String tabKey = BalanceRequiredAreaExclusionsSheetValidator.TAB_KEY;
        GraduationSheetTable graduationSheetTable = fetchAndValidate(tabKey);

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

        log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료", tabKey, balanceRequiredAreaExclusionList.size());
    }

    private void syncGraduationCertRule() {
        String tabKey = GraduationCertRulesSheetValidator.TAB_KEY;
        GraduationSheetTable graduationSheetTable = fetchAndValidate(tabKey);

        List<GraduationCertRule> graduationCertRuleList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            GraduationCertRule graduationCertRule = new GraduationCertRule(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getEnum(row, "graduation_cert_rule_type", GraduationCertRuleType.class)
            );
            graduationCertRuleList.add(graduationCertRule);
        }

        graduationCertRuleRepository.deleteAllInBatch();
        graduationCertRuleRepository.saveAll(graduationCertRuleList);

        log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료}", tabKey, graduationCertRuleList.size());
    }

    private void syncEnglishCertCriteria() {
        String tabKey = EnglishCertCriteriaSheetValidator.TAB_KEY;
        GraduationSheetTable graduationSheetTable = fetchAndValidate(tabKey);

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

        log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료", tabKey, englishCertCriterionList.size());
    }

    private void syncCodingCertCriteria() {
        String tabKey = CodingCertCriteriaSheetValidator.TAB_KEY;
        GraduationSheetTable graduationSheetTable = fetchAndValidate(tabKey);

        List<CodingCertCriterion> codingCertCriterionList = new ArrayList<>();
        for (List<Object> row : graduationSheetTable.getDataRows()) {
            CodingCertCriterion codingCertCriterion = new CodingCertCriterion(
                graduationSheetTable.getInt(row, "admission_year"),
                graduationSheetTable.getInt(row, "admission_year_short"),
                graduationSheetTable.getEnum(row, "coding_target_type", CodingTargetType.class),
                graduationSheetTable.getInt(row, "tosc_min_level"),
                graduationSheetTable.getString(row, "alt1_curi_no"),
                graduationSheetTable.getString(row, "alt1_curi_nm"),
                graduationSheetTable.getString(row, "alt1_min_grade"),
                graduationSheetTable.getString(row, "alt2_curi_no"),
                graduationSheetTable.getString(row, "alt2_curi_nm"),
                graduationSheetTable.getString(row, "alt2_min_grade"),
                graduationSheetTable.getString(row, "note")
            );
            codingCertCriterionList.add(codingCertCriterion);
        }

        codingCertCriterionRepository.deleteAllInBatch();
        codingCertCriterionRepository.saveAll(codingCertCriterionList);

        log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료", tabKey, codingCertCriterionList.size());
    }

    private void syncClassicCertCriteria() {
        String tabKey = ClassicCertCriteriaSheetValidator.TAB_KEY;
        GraduationSheetTable graduationSheetTable = fetchAndValidate(tabKey);

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

        log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료", tabKey, classicCertCriterionList.size());
    }

    private void syncGraduationDepartmentInfo() {
        String tabKey = GraduationDepartmentInfoSheetValidator.TAB_KEY;
        GraduationSheetTable graduationSheetTable = fetchAndValidate(tabKey);

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

        log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료", tabKey, graduationDepartmentInfoList.size());
    }
}
