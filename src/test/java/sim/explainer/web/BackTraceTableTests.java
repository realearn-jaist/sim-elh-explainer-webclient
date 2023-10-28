package sim.explainer.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sim.explainer.web.framework.BackTraceTable;
import sim.explainer.web.framework.descriptiontree.Tree;
import sim.explainer.web.framework.descriptiontree.TreeNode;
import sim.explainer.web.framework.unfolding.IConceptUnfolder;
import sim.explainer.web.service.OWLMeasurement.OWLDynamicProgrammingSimService;
import sim.explainer.web.service.OWLMeasurement.OWLServiceContext;
import sim.explainer.web.service.SimilarityService;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sim.explainer.web.enumeration.TypeConstant.DYNAMIC_SIM;

@SpringBootTest
public class BackTraceTableTests {

    @Autowired
    private SimilarityService similarityService;

    @Autowired
    private OWLServiceContext owlServiceContext;

    @Autowired
    BackTraceTable backTraceTable;

    @Autowired
    private OWLDynamicProgrammingSimService owlDynamicProgrammingSimService;

    @Autowired
    private IConceptUnfolder conceptDefinitionUnfolderManchesterSyntax;

    private final String owlFilePath = "src/test/resources/family.owl"; // dummy ontology for testing

    // runchana:2023-10-20 verify that the concepts have been set correctly
    @Test
    public void testInputConceptName() {
        String concept1 = "Female";
        String concept2 = "Male";

        backTraceTable.inputConceptName(concept1, concept2);

        assertEquals(concept1, backTraceTable.getCnPair()[0]);
        assertEquals(concept2, backTraceTable.getCnPair()[1]);
    }

    /**
     * runchana:2023-10-20 Check the list of concepts in BackTraceTable, which will be used for explanation extraction.
     */
    @Test
    public void testTreeNodeMapBackTraceTable() {
        File owlFile = new File(owlFilePath);

        owlServiceContext.init(owlFile);
        owlDynamicProgrammingSimService.readInputOWLOntology(owlFile, "Female Man");

        IConceptUnfolder conceptT = conceptDefinitionUnfolderManchesterSyntax; // OWL

        // Populate description trees of Female and Man directly
        Tree<Set<String>> tree1 = similarityService.unfoldAndConstructTree(conceptT, "Female");
        Tree<Set<String>> tree2 = similarityService.unfoldAndConstructTree(conceptT, "Man");

        Map<Integer, TreeNode<Set<String>>> tree1_concepts = tree1.getNodes();
        Map<Integer, TreeNode<Set<String>>> tree2_concepts = tree2.getNodes();

        int index = 0;

        BigDecimal result1 = similarityService.measureConceptWithType("Female", "Man", DYNAMIC_SIM, "OWL").setScale(5, BigDecimal.ROUND_HALF_UP);

        for (Map.Entry<Map<Integer, String[]>, Map<String, Map<Tree<Set<String>>, BigDecimal>>> backtrace : similarityService.backTraceTable.getBackTraceTable().entrySet()) {
            Map<String, Map<Tree<Set<String>>, BigDecimal>> valueMap = backtrace.getValue();

            for (Map.Entry<String, Map<Tree<Set<String>>, BigDecimal>> treeNode : valueMap.entrySet()) {
                for (Map.Entry<Tree<Set<String>>, BigDecimal> treeEntry : treeNode.getValue().entrySet()) {
                    Tree<Set<String>> tree = treeEntry.getKey();
                    if (index == 0) {
                        assertEquals("Female tree", tree.getLabel()); // check concept tree
                        assertTreeNodesEqual(tree.getNodes(), tree1_concepts); // check a list of its concept names
                    } else if (index == 1) {
                        assertEquals("Man tree", tree.getLabel());
                        assertTreeNodesEqual(tree.getNodes(), tree2_concepts);
                    }
                    assertEquals(treeEntry.getValue().setScale(5, BigDecimal.ROUND_HALF_UP), result1); // check homomorphism degree

                }
                index++;
            }
        }
    }

    private void assertTreeNodesEqual(Map<Integer, TreeNode<Set<String>>> tree1, Map<Integer, TreeNode<Set<String>>> tree2) {
        assertEquals(tree1.size(), tree2.size());

        for (Map.Entry<Integer, TreeNode<Set<String>>> entry1 : tree1.entrySet()) {
            int key = entry1.getKey();
            TreeNode<Set<String>> node1 = entry1.getValue();
            TreeNode<Set<String>> node2 = tree2.get(key);

            assertEquals(node1.getEdgeToParent(), node2.getEdgeToParent());
            assertEquals(node1.getData(), node2.getData());
        }
    }

}
