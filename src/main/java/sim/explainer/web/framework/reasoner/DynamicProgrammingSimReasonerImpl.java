package sim.explainer.web.framework.reasoner;

import org.apache.commons.lang3.math.NumberUtils;
import sim.explainer.web.enumeration.ReasonerParameters;
import sim.explainer.web.framework.descriptiontree.BreadthFirstTreeIterator;
import sim.explainer.web.framework.descriptiontree.Tree;
import sim.explainer.web.framework.descriptiontree.TreeNode;
import sim.explainer.web.util.TimeUtils;
import sim.explainer.web.exception.ErrorCode;
import sim.explainer.web.exception.JSimPiException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component("dynamicProgrammingSimReasonerImpl")
public class DynamicProgrammingSimReasonerImpl extends TopDownSimReasonerImpl {

    private static final Logger logger = LoggerFactory.getLogger(DynamicProgrammingSimReasonerImpl.class);

    @Autowired
    private Environment env;

    private Map<Integer, Map<Integer, BigDecimal>> nodePairHdValMap = new HashMap<Integer, Map<Integer, BigDecimal>>();

    private List<DateTime> markedTime = new ArrayList<DateTime>();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void addNodePairHdValMap(Integer node1Id, Integer node2Id, BigDecimal hdVal) {
        if (node1Id == null || node2Id == null || hdVal == null) {
            throw new JSimPiException("Unable to add node pair hd val map as node1Id["
                    + node1Id + "], node2Id[" + node2Id + "], and hdVal[" + hdVal + "] are null.", ErrorCode.DynamicProgrammingSimReasonerImpl_IllegalArguments);
        }

        Map<Integer, BigDecimal> node2IdHdValMap = nodePairHdValMap.get(node1Id);
        if (node2IdHdValMap == null) {
            node2IdHdValMap = new HashMap<Integer, BigDecimal>();
        }
        node2IdHdValMap.put(node2Id, hdVal);
        this.nodePairHdValMap.put(node1Id, node2IdHdValMap);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Protected ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected BigDecimal eHd(TreeNode<Set<String>> node1, TreeNode<Set<String>> node2) {
        if (node1 == null || node2 == null) {
            throw new JSimPiException("Unable to ehd as node1[" + node1 + "] and node2[" + node2 + "] are null.", ErrorCode.DynamicProgrammingSimReasonerImpl_IllegalArguments);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("eHd - node1[" + node1.getData().toString() + "] node2[" + node2.getData().toString() + "].");
        }

        BigDecimal gammaValue = gamma(node1.getEdgeToParent(), node2.getEdgeToParent());

        BigDecimal nu;

        String nuPropertyStr = env.getProperty(ReasonerParameters.SIM_CONSTANT_NU.getStr());
        if (NumberUtils.isNumber(nuPropertyStr)) {
            nu = new BigDecimal(nuPropertyStr);
        }

        else {
            // teeradaj@20160324: By default, nu is set to 0.4
            nu = new BigDecimal("0.4");
        }

        BigDecimal nuPrime = BigDecimal.ONE.subtract(nu);

        BigDecimal simSubTree = nodePairHdValMap.get(node1.getId()).get(node2.getId());
        if (simSubTree == null) {
            simSubTree = BigDecimal.ZERO;
        }

        return nuPrime.multiply(simSubTree).add(nu).multiply(gammaValue);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public BigDecimal measureDirectedSimilarity(Tree<Set<String>> tree1, Tree<Set<String>> tree2) {
        if (tree1 == null || tree2 == null) {
            throw new JSimPiException("Unable to measure directed similarity as tree1[" + tree1 + "] and tree2[" + tree2 + "] are null.", ErrorCode.DynamicProgrammingSimReasonerImpl_IllegalArguments);
        }

        markedTime.clear();

        markedTime.add(DateTime.now());
        BreadthFirstTreeIterator<Set<String>> breadthFirstTree1 = (BreadthFirstTreeIterator<Set<String>>) tree1.iterator(0);
        markedTime.add(DateTime.now());

        markedTime.add(DateTime.now());
        BreadthFirstTreeIterator<Set<String>> breadthFirstTree2 = (BreadthFirstTreeIterator<Set<String>>) tree2.iterator(0);
        markedTime.add(DateTime.now());

        int heightTree1 = breadthFirstTree1.getNodesOnEachLevel().size();

        if (logger.isDebugEnabled()) {
            logger.debug("measureDirectedSimilarity - height[" + heightTree1 + "].");
        }

        markedTime.add(DateTime.now());
        for (int i = heightTree1 - 1; i >= 0; i--) { // from the lowest level to the root
            List<TreeNode<Set<String>>> list1 = breadthFirstTree1.getNodesOnEachLevel().get(i);
            List<TreeNode<Set<String>>> list2 = breadthFirstTree2.getNodesOnEachLevel().get(i);

            for (TreeNode<Set<String>> treeNode1 : list1) {

                for (int j = 0; list2 != null && j < list2.size(); j++) {
                    TreeNode<Set<String>> treeNode2 = list2.get(j);

                    BigDecimal phd = phd(treeNode1, treeNode2);

                    if (i == heightTree1 - 1) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("measureDirectedSimilarity - i: " + i + " treeNode1 ID[" + treeNode1.getId() + "] treeNode2 ID[" + treeNode2.getId() + "] phd[" + phd.toPlainString() + "].");
                        }

                        this.addNodePairHdValMap(treeNode1.getId(), treeNode2.getId(), phd);
                    }

                    else {

                        if (logger.isDebugEnabled()) {
                            logger.debug("measureDirectedSimilarity - i: " + i + " treeNode1 ID[" + treeNode1.getId() + "] treeNode2 ID[" + treeNode2.getId() + "] phd[" + phd.toPlainString() + "].");
                        }

                        BigDecimal mu = mu(treeNode1);
                        BigDecimal eSetHd = eSetHd(treeNode1, treeNode2);
                        BigDecimal primitiveOperations = mu.multiply(phd);
                        BigDecimal edgeOperations = BigDecimal.ONE.subtract(mu).multiply(eSetHd);
                        BigDecimal hdVal = primitiveOperations.add(edgeOperations);
                        this.addNodePairHdValMap(treeNode1.getId(), treeNode2.getId(), hdVal);
                    }

                }
            }
        }
        BigDecimal value = nodePairHdValMap.get(0).get(0);
        markedTime.add(DateTime.now());

        return value;
    }

    @Override
    public List<String> getExecutionTimes() {
        List<String> results = new LinkedList<String>();

        for (int i = 0; i < markedTime.size(); i = i + 2) {
            results.add(TimeUtils.getTotalTimeDifferenceStringInMillis(markedTime.get(i), markedTime.get(i + 1)));
        }

        return results;
    }
}
