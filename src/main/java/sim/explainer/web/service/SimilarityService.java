package sim.explainer.web.service;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import sim.explainer.web.enumeration.TypeConstant;
import sim.explainer.web.exception.ErrorCode;
import sim.explainer.web.exception.JSimPiException;
import sim.explainer.web.framework.BackTraceTable;
import sim.explainer.web.framework.descriptiontree.Tree;
import sim.explainer.web.framework.descriptiontree.TreeBuilder;
import sim.explainer.web.framework.reasoner.IReasoner;
import sim.explainer.web.framework.unfolding.ConceptDefinitionUnfolderManchesterSyntax;
import sim.explainer.web.framework.unfolding.IConceptUnfolder;
import sim.explainer.web.framework.unfolding.IRoleUnfolder;
import sim.explainer.web.util.MyStringUtils;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class SimilarityService {

    private static final BigDecimal TWO = new BigDecimal("2");

    private static Set<String> processedProperties = new HashSet<>();

    private static ExplanationService explanationService = new ExplanationService();

    public static BackTraceTable backTraceTable = new BackTraceTable();

    private static IReasoner topDownSimReasonerImpl;
    private static IReasoner topDownSimPiReasonerImpl;
    private static IReasoner dynamicProgrammingSimReasonerImpl;
    private static IReasoner dynamicProgrammingSimPiReasonerImpl;

    private static IConceptUnfolder conceptDefinitionUnfolderManchesterSyntax;
    private static IConceptUnfolder conceptDefinitionUnfolderKRSSSyntax;
    private static IRoleUnfolder superRoleUnfolderManchesterSyntax;
    private static IRoleUnfolder superRoleUnfolderKRSSSyntax;

    private static TreeBuilder treeBuilder;
    @Autowired
    public SimilarityService(
            IReasoner topDownSimReasonerImpl,
            IReasoner topDownSimPiReasonerImpl,
            IReasoner dynamicProgrammingSimReasonerImpl,
            IReasoner dynamicProgrammingSimPiReasonerImpl,
            IConceptUnfolder conceptDefinitionUnfolderManchesterSyntax,
            IConceptUnfolder conceptDefinitionUnfolderKRSSSyntax,
            IRoleUnfolder superRoleUnfolderManchesterSyntax,
            IRoleUnfolder superRoleUnfolderKRSSSyntax,
            TreeBuilder treeBuilder) {
        SimilarityService.topDownSimReasonerImpl = topDownSimReasonerImpl;
        SimilarityService.topDownSimPiReasonerImpl = topDownSimPiReasonerImpl;
        SimilarityService.dynamicProgrammingSimReasonerImpl = dynamicProgrammingSimReasonerImpl;
        SimilarityService.dynamicProgrammingSimPiReasonerImpl = dynamicProgrammingSimPiReasonerImpl;
        SimilarityService.conceptDefinitionUnfolderManchesterSyntax = conceptDefinitionUnfolderManchesterSyntax;
        SimilarityService.conceptDefinitionUnfolderKRSSSyntax = conceptDefinitionUnfolderKRSSSyntax;
        SimilarityService.superRoleUnfolderManchesterSyntax = superRoleUnfolderManchesterSyntax;
        SimilarityService.superRoleUnfolderKRSSSyntax = superRoleUnfolderKRSSSyntax;
        SimilarityService.treeBuilder = treeBuilder;
    }

    public SimilarityService() {

    }

    /**
     * runchana:2023-17-10 Prepare OWL file
     * @param owlFile
     * @return
     */
    public static OWLOntology prepareOWLFile(File owlFile) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(IRI.create(owlFile));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        return ontology;
    }

    // runchana:2023-10-12 Retrieve concept names from the given .owl file
    public static List<String> retrieveConceptName(File owlFile) {

        processedProperties.clear();

        List<String> conceptNames = new ArrayList<>();
        OWLOntology ontology = prepareOWLFile(owlFile);

        ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

        for (OWLClass owlClass1 : ontology.getClassesInSignature()) {
            String className1 = shortFormProvider.getShortForm(owlClass1);

            addObjectProperties(ontology, owlClass1, shortFormProvider);

            if (!className1.equals("Thing")){
                conceptNames.add(className1);
            }
        }

        return conceptNames;
    }

    /**
     * runchana:2023-10-15 Find object properties from a given OWL Class.
     * Note: It may not work with some OWL syntax.
     * @param ontology
     * @param owlClass
     * @param shortFormProvider
     */
    public static void addObjectProperties(OWLOntology ontology, OWLClass owlClass, ShortFormProvider shortFormProvider) {

        for (OWLIndividual individual : owlClass.getIndividuals(ontology)) {
            for (OWLObjectPropertyAssertionAxiom axiom : ontology.getObjectPropertyAssertionAxioms(individual)) {
                OWLObjectProperty property = axiom.getProperty().asOWLObjectProperty();

                String propertyName = shortFormProvider.getShortForm(property);

                processedProperties.add(propertyName);
            }
        }
    }

    public static Set<String> getProcessedProperties() {
        return processedProperties;
    }

    public Tree<Set<String>> unfoldAndConstructTree(IConceptUnfolder iConceptUnfolder, String conceptName1) {
        String unfoldConceptName1 = iConceptUnfolder.unfoldConceptDefinitionString(conceptName1);

        if (iConceptUnfolder instanceof ConceptDefinitionUnfolderManchesterSyntax) {
            return treeBuilder.constructAccordingToManchesterSyntax(MyStringUtils.generateTreeLabel(conceptName1), unfoldConceptName1);
        }

        else {
            return treeBuilder.constructAccordingToKRSSSyntax(MyStringUtils.generateTreeLabel(conceptName1), unfoldConceptName1);
        }
    }

    private BigDecimal computeSimilarity(IReasoner iReasoner, IRoleUnfolder iRoleUnfolder, Tree<Set<String>> tree1, Tree<Set<String>> tree2) {
        iReasoner.setRoleUnfoldingStrategy(iRoleUnfolder);

        BigDecimal forwardDistance = iReasoner.measureDirectedSimilarity(tree1, tree2);
        BigDecimal backwardDistance = iReasoner.measureDirectedSimilarity(tree2, tree1);

        return forwardDistance.add(backwardDistance).divide(TWO);
    }

    /**
     * runchana:2023-31-07
     * Measure a similarity degree from given concepts with a specified concept and measurement types.
     *
     * @param conceptName1
     * @param conceptName2
     * @param type  concept type, i.e., KRSS or OWL
     * @param conceptType  measurement type, i.e., dynamic/top down and sim/simpi
     * @return similarity degree of that concept pair
     * @throws IOException
     */
    public BigDecimal measureConceptWithType(String conceptName1, String conceptName2, TypeConstant type, String conceptType) {

        IConceptUnfolder conceptT = null;

        IRoleUnfolder roleUnfolderT = null;

        IReasoner reasonerT = null;

        String measurementType = type.getDescription();

        BigDecimal result = null;

        if (conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable measure with " + measurementType + " as conceptName1[" + conceptName1 + "] and " +
                    "conceptName2[" + conceptName2 + "] are null.", ErrorCode.OWLSimService_IllegalArguments);
        }

        if (conceptType.equals("KRSS")) {
            conceptT = conceptDefinitionUnfolderKRSSSyntax;
            roleUnfolderT = superRoleUnfolderKRSSSyntax;
        } else if (conceptType.equals("OWL")) {
            conceptT = conceptDefinitionUnfolderManchesterSyntax;
            roleUnfolderT = superRoleUnfolderManchesterSyntax;
        }

        if (measurementType.equals("dynamic programming Sim")) {
            reasonerT = dynamicProgrammingSimReasonerImpl;
        } else if (measurementType.equals("dynamic programming SimPi")) {
            reasonerT = dynamicProgrammingSimPiReasonerImpl;
        } else if (measurementType.equals("top down Sim")) {
            reasonerT = topDownSimReasonerImpl;
        } else {
            reasonerT = topDownSimPiReasonerImpl;
        }

        Tree<Set<String>> tree1 = unfoldAndConstructTree(conceptT, conceptName1);
        Tree<Set<String>> tree2 = unfoldAndConstructTree(conceptT, conceptName2);

        result = computeSimilarity(reasonerT, roleUnfolderT, tree1, tree2);

        // runchana:2023-31-07 store computation inside a backTraceTable class
        backTraceTable.inputConceptName(conceptName1, conceptName2);
        backTraceTable.inputTreeNodeValue(tree1, result, 1);
        backTraceTable.inputTreeNodeValue(tree2, result, 2);

        explanationService.explainSimilarity(backTraceTable);
        return result;
    }

    /**
     * runchana:2023-17-10 Check whether the given file is OWL or KRSS
     * @param fileName
     * @return
     */
    public static Boolean checkOWLandKRSSFile(String fileName) {
        if (fileName.endsWith(".owl")) {
            return true;
        }
        return false;
    }

}
