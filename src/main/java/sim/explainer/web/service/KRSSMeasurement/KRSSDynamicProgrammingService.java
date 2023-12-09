package sim.explainer.web.service.KRSSMeasurement;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import sim.explainer.web.controller.KRSSSimilarityController;
import sim.explainer.web.enumeration.TypeConstant;
import sim.explainer.web.framework.descriptiontree.TreeBuilder;
import sim.explainer.web.framework.reasoner.DynamicProgrammingSimPiReasonerImpl;
import sim.explainer.web.framework.reasoner.DynamicProgrammingSimReasonerImpl;
import sim.explainer.web.framework.reasoner.TopDownSimPiReasonerImpl;
import sim.explainer.web.framework.reasoner.TopDownSimReasonerImpl;
import sim.explainer.web.framework.unfolding.ConceptDefinitionUnfolderKRSSSyntax;
import sim.explainer.web.framework.unfolding.ConceptDefinitionUnfolderManchesterSyntax;
import sim.explainer.web.framework.unfolding.SuperRoleUnfolderKRSSSyntax;
import sim.explainer.web.framework.unfolding.SuperRoleUnfolderManchesterSyntax;
import sim.explainer.web.service.ExplanationService;
import sim.explainer.web.service.OWLMeasurement.OWLServiceContext;
import sim.explainer.web.service.PreferenceProfile;
import sim.explainer.web.service.SimilarityService;
import sim.explainer.web.service.ValidationService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Import(value= {KRSSSimilarityController.class, ValidationService.class,
        TopDownSimReasonerImpl.class, TopDownSimPiReasonerImpl.class,
        DynamicProgrammingSimReasonerImpl.class, DynamicProgrammingSimPiReasonerImpl.class,
        ConceptDefinitionUnfolderManchesterSyntax.class, ConceptDefinitionUnfolderKRSSSyntax.class,
        TreeBuilder.class, OWLServiceContext.class, KRSSServiceContext.class,
        SimilarityService.class, SuperRoleUnfolderManchesterSyntax.class,
        SuperRoleUnfolderKRSSSyntax.class, PreferenceProfile.class
})
@Service
public class KRSSDynamicProgrammingService {

    private static List<String> concept1sToMeasure = new ArrayList<>();
    private static List<String> concept2sToMeasure = new ArrayList<>();

    private static KRSSServiceContext krssServiceContext;
    private static KRSSSimilarityController krssSimilarityController;
    private static ValidationService validationService;
    private static SimilarityService similarityService;

    @Autowired
    public KRSSDynamicProgrammingService(
            KRSSServiceContext krssServiceContext,
            KRSSSimilarityController krssSimilarityController,
            ValidationService validationService,
            SimilarityService similarityService) {
        KRSSDynamicProgrammingService.krssServiceContext = krssServiceContext;
        KRSSDynamicProgrammingService.krssSimilarityController = krssSimilarityController;
        KRSSDynamicProgrammingService.validationService = validationService;
        KRSSDynamicProgrammingService.similarityService = similarityService;
    }

    public static void readInputKRSSOntology(File krssFile, String conceptnameInput) {

        krssServiceContext.init(krssFile);

        concept1sToMeasure = new ArrayList<>();
        concept2sToMeasure = new ArrayList<>();

        String[] lines =  StringUtils.split(conceptnameInput, "\n");

        for (String eachLine : lines) {
            String[] concepts = StringUtils.split(eachLine);
            concept1sToMeasure.add(concepts[0]);
            concept2sToMeasure.add(concepts[1]);
        }
    }

    public static StringBuilder computeDynamicProgramming() throws IOException {
        StringBuilder dynamicProgrammingSimResult = new StringBuilder();

        for (int i = 0; i < concept1sToMeasure.size(); i++) {
            krssSimilarityController.measureSimilarity(concept1sToMeasure.get(i), concept2sToMeasure.get(i), TypeConstant.DYNAMIC_SIM, "KRSS");
        }

        // runchana:2023-17-10 invoke explanation from explanation service
        dynamicProgrammingSimResult = ExplanationService.explanation;

        return dynamicProgrammingSimResult;
    }

}
