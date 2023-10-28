package sim.explainer.web.framework.unfolding;

import sim.explainer.web.enumeration.OWLConstant;
import sim.explainer.web.util.OWLOntologyUtil;
import sim.explainer.web.util.ParserUtils;
import sim.explainer.web.exception.ErrorCode;
import sim.explainer.web.exception.JSimPiException;
import sim.explainer.web.service.OWLMeasurement.OWLServiceContext;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component("superRoleUnfolderManchesterSyntax")
public class SuperRoleUnfolderManchesterSyntax implements IRoleUnfolder {

    @Autowired
    private OWLServiceContext OWLServiceContext;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Set<String> unfold(OWLObjectProperty owlObjectProperty, Set<String> roles) {
        Set<OWLObjectPropertyExpression> owlObjectPropertyExpressions = owlObjectProperty.getSuperProperties(OWLServiceContext.getOwlOntology());

        // When a role has no defined hierarchy.
        if (owlObjectPropertyExpressions.isEmpty()) {
            roles.add(owlObjectProperty.getIRI().getFragment());
        }

        else {
            roles.add(ParserUtils.generateFreshName(owlObjectProperty.getIRI().getFragment()));

            for (OWLObjectPropertyExpression propertyExpression : owlObjectPropertyExpressions) {
                OWLObjectProperty superObjectProperty = propertyExpression.asOWLObjectProperty();

                unfold(superObjectProperty, roles);
            }
        }

        return roles;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Set<String> unfoldRoleHierarchy(String roleName) {
        if (roleName == null) {
            throw new JSimPiException("Unable to unfold role hierarchy as roleName is null.", ErrorCode.SuperRoleUnfolderManchesterSyntax_IllegalArguments);
        }

        Set<String> roles = new HashSet<String>();
        if (roleName.equals(OWLConstant.TOP_ROLE.getOwlSyntax())) {
            return roles;
        }

        OWLObjectProperty owlObjectProperty = OWLOntologyUtil.getOWLObjectProperty(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), roleName);

        return unfold(owlObjectProperty, roles);
    }
}
