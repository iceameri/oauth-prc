package com.jw.authorizationserver.convention;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.jw.authorizationserver")
public class ServiceConventionTest {

    @ArchTest
    static final ArchRule application_dto_naming =
            classes()
                    .that().resideInAPackage("..dto..")
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .should().haveSimpleNameEndingWith("Response")
                    .orShould().haveSimpleNameEndingWith("Request")
            ;
}
