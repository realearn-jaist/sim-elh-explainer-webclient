package sim.explainer.web.service.OWLMeasurement;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import sim.explainer.web.controller.OWLSimilarityController;
import sim.explainer.web.service.*;
import sim.explainer.web.service.KRSSMeasurement.KRSSServiceContext;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Import(value= {OWLSimilarityController.class, ValidationService.class,
        TopDownSimReasonerImpl.class, TopDownSimPiReasonerImpl.class,
        DynamicProgrammingSimReasonerImpl.class, DynamicProgrammingSimPiReasonerImpl.class,
        ConceptDefinitionUnfolderManchesterSyntax.class, ConceptDefinitionUnfolderKRSSSyntax.class,
        TreeBuilder.class, OWLServiceContext.class, KRSSServiceContext.class,
        SimilarityService.class, SuperRoleUnfolderManchesterSyntax.class,
        SuperRoleUnfolderKRSSSyntax.class, PreferenceProfile.class
})
@Service
public class OWLDynamicProgrammingSimService {

    private static List<String> concept1sToMeasure = new ArrayList<>();
    private static List<String> concept2sToMeasure = new ArrayList<>();

    private static OWLServiceContext owlServiceContext;
    private static OWLSimilarityController owlSimilarityController;
    private static ValidationService validationService;
    private static SimilarityService similarityService;

    @Autowired
    public OWLDynamicProgrammingSimService(
            OWLServiceContext owlServiceContext,
            OWLSimilarityController owlSimilarityController,
            ValidationService validationService,
            SimilarityService similarityService) {
        OWLDynamicProgrammingSimService.owlServiceContext = owlServiceContext;
        OWLDynamicProgrammingSimService.owlSimilarityController = owlSimilarityController;
        OWLDynamicProgrammingSimService.validationService = validationService;
        OWLDynamicProgrammingSimService.similarityService = similarityService;
    }

    /**
     * runchana:2023-17-10 Read input concepts from the given file with concept names that the users want to measure
     * @param owlFile
     * @param conceptnameInput
     */
    public static void readInputOWLOntology(File owlFile, String conceptnameInput) {
        owlServiceContext.init(owlFile);

        concept1sToMeasure = new ArrayList<>();
        concept2sToMeasure = new ArrayList<>();

        String[] lines =  StringUtils.split(conceptnameInput, "\n");

        for (String eachLine : lines) {
            String[] concepts = StringUtils.split(eachLine);
            concept1sToMeasure.add(concepts[0]);
            concept2sToMeasure.add(concepts[1]);
        }
    }

    /**
     * runchana:2023-17-10 Compute Dynamic Programming Sim
     * @return dynamicProgrammingSimResult
     */
    public static StringBuilder computeDynamicProgramming() throws IOException {
        StringBuilder dynamicProgrammingSimResult = new StringBuilder();

        for (int i = 0; i < concept1sToMeasure.size(); i++) {
            owlSimilarityController.measureSimilarity(concept1sToMeasure.get(i), concept2sToMeasure.get(i), TypeConstant.DYNAMIC_SIM, "OWL");
        }

        // runchana:2023-17-10 invoke explanation from explanation service
        dynamicProgrammingSimResult = ExplanationService.explanation;

        return dynamicProgrammingSimResult;
    }

}
