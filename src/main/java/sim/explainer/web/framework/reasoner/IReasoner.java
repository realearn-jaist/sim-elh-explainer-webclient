package sim.explainer.web.framework.reasoner;

import sim.explainer.web.framework.descriptiontree.Tree;
import sim.explainer.web.framework.unfolding.IRoleUnfolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface IReasoner {

    BigDecimal measureDirectedSimilarity(Tree<Set<String>> tree1, Tree<Set<String>> tree2);

    void setRoleUnfoldingStrategy(IRoleUnfolder iRoleUnfolder);

    List<String> getExecutionTimes();

}
