package sim.explainer.web;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import sim.explainer.web.controller.OWLSimilarityController;
import sim.explainer.web.enumeration.OWLDocumentFormat;
import sim.explainer.web.framework.descriptiontree.Tree;
import sim.explainer.web.framework.unfolding.IConceptUnfolder;
import sim.explainer.web.service.OWLMeasurement.OWLDynamicProgrammingSimService;
import sim.explainer.web.service.OWLMeasurement.OWLServiceContext;
import sim.explainer.web.service.OWLMeasurement.OWLTopDownProgrammingSimService;
import sim.explainer.web.service.SimilarityService;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static sim.explainer.web.enumeration.OWLDocumentFormat.MANCHESTER_SYNTAX_DOCUMENT;
import static sim.explainer.web.enumeration.TypeConstant.DYNAMIC_SIM;
import static sim.explainer.web.enumeration.TypeConstant.TOPDOWN_SIM;
import static sim.explainer.web.service.SimilarityService.prepareOWLFile;

@SpringBootTest
public class SimilarityServiceTests {

    @Autowired
    private SimilarityService similarityService;

    @Autowired
    private OWLServiceContext owlServiceContext;

    @Autowired
    private OWLDynamicProgrammingSimService owlDynamicProgrammingSimService;

    @Autowired
    private OWLTopDownProgrammingSimService owlTopDownProgrammingSimService;

    @Autowired
    private IConceptUnfolder conceptDefinitionUnfolderManchesterSyntax;

    private final String owlFilePath = "src/test/resources/family.owl"; // dummy ontology for testing
    private final ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

    // runchana:2023-10-22 Test OWL File preparation
    @Test
    public void testPrepareOWLFile() {

        File owlFile = new File(owlFilePath);

        OWLOntology ontology = prepareOWLFile(owlFile);

        assertNotNull(ontology, "The ontology file should not be null");
    }

    // runchana:2023-10-22 Test concept names retrieval method
    // check if the retrieved concept lists contain owl classes from the direct ontology or not
    @Test
    public void testRetrieveConceptName() {
        File owlFile = new File(owlFilePath);

        OWLOntology ontology = prepareOWLFile(owlFile);

        List<String> conceptList = similarityService.retrieveConceptName(owlFile);

        for (OWLClass owlClass : ontology.getClassesInSignature()) {
            String classShortForm = shortFormProvider.getShortForm(owlClass);
            if (!classShortForm.equals("Thing")) {
                assertTrue(conceptList.contains(classShortForm));
            }
        }
    }

    // runchana:2023-10-22 Test similarity measurement with different technique, dynamic programming and top down.
    @Test
    public void testMeasureConceptWithType() {
        File owlFile = new File(owlFilePath);

        // Dynamic Sim, OWL
        owlDynamicProgrammingSimService.readInputOWLOntology(owlFile, "Man Son");

        BigDecimal result1 = similarityService.measureConceptWithType("Man", "Son", DYNAMIC_SIM, "OWL");

        assertThat(result1.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.90000");

        // TopDown Sim, OWL
        owlTopDownProgrammingSimService.readInputOWLOntology(owlFile, "SonInLaw Son");

        BigDecimal result2 = similarityService.measureConceptWithType("SonInLaw", "Son", TOPDOWN_SIM, "OWL");

        assertThat(result2.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.97000");

    }

}
