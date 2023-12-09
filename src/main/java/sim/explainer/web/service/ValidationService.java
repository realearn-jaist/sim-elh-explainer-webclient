package sim.explainer.web.service;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.semanticweb.owlapi.model.OWLClass;
import org.springframework.stereotype.Service;
import sim.explainer.web.service.OWLMeasurement.OWLServiceContext;
import sim.explainer.web.util.OWLOntologyUtil;

import java.io.File;

@Service
public class ValidationService {
    private OWLServiceContext owlServiceContext;

    public ValidationService(OWLServiceContext owlServiceContext) {
        this.owlServiceContext = owlServiceContext;
    }

    public boolean validateIfOWLClassNamesExist(String... conceptNames) {
        if (conceptNames == null) {
            return false;
        }

        else {
            for (String conceptName : conceptNames) {
                OWLClass owlClass = OWLOntologyUtil.getOWLClass(owlServiceContext.getOwlDataFactory(), owlServiceContext.getOwlOntologyManager(), owlServiceContext.getOwlOntology(), conceptName);

                if (!OWLOntologyUtil.containClassName(owlServiceContext.getOwlOntology(), owlClass)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean validateIfLatestOWLFile(String owlFilePath) {
        if (owlFilePath == null) {
            return false;
        }

        if (owlServiceContext.getOwlFile() == null) {
            return false;
        }

        LastModifiedFileComparator lastModifiedFileComparator = new LastModifiedFileComparator();
        if (lastModifiedFileComparator.compare(new File(owlFilePath), owlServiceContext.getOwlFile()) > 0) {
            return true;
        }

        return false;
    }

}