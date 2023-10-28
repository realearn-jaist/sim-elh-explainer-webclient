package sim.explainer.web.framework.unfolding;

import java.util.Set;

public interface IRoleUnfolder {

    Set<String> unfoldRoleHierarchy(String roleName);
}
