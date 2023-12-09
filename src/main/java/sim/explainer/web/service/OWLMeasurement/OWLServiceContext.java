package sim.explainer.web.service.OWLMeasurement;

import sim.explainer.web.enumeration.OWLDocumentFormat;
import sim.explainer.web.exception.ErrorCode;
import sim.explainer.web.exception.JSimPiException;
import sim.explainer.web.util.OWLOntologyUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class OWLServiceContext{

    private static final Logger logger = LoggerFactory.getLogger(OWLServiceContext.class);

    private OWLOntology owlOntology;
    private String owlOntologyDocumentIRI;
    private OWLDataFactory owlDataFactory;
    private OWLOntologyManager owlOntologyManager;

    private File owlFile;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void init(File inputOwlFile) {
        if (inputOwlFile == null) {
            throw new JSimPiException("Unable to init owl service context as inputOwlFile is null.", ErrorCode.OWLServiceContext_IllegalArguments);
        }

        // teeradaj@20160309:
        // http://www.programcreek.com/java-api-examples/index.php?api=org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat

        // 1. Get hold of an ontology manager
        this.owlOntologyManager = OWLManager.createOWLOntologyManager();

        // 2. Let's load an ontology from a owlFilePath. An ontology is loaded from a document IRI.
        owlFile = inputOwlFile;
        IRI documentIRI = IRI.create(owlFile);

        try {
            if(logger.isInfoEnabled()) {
                logger.info("Start loading " + owlFile + "...");
            }

            // teeradaj@20160307: each ontology MAY or MAY NOT have an ontology IRI.
            // Therefore, it suffice to not check if an ontology IRI exists.
            // https://www.w3.org/TR/owl2-syntax/#Ontology_IRI_and_Version_IRI
            this.owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(documentIRI);

            if(logger.isInfoEnabled()) {
                logger.info("Loaded ontology:" + inputOwlFile);
            }

            this.owlOntologyDocumentIRI = this.owlOntology.getOntologyID().getDefaultDocumentIRI().toQuotedString();

            // 3. Save an ontology in Manchester OWL syntax.
            this.owlOntology = OWLOntologyUtil.convertAsOWLDocumentFormat(owlOntologyManager, this.owlOntology, OWLDocumentFormat.MANCHESTER_SYNTAX_DOCUMENT);

//            OWLOntologyUtil.printOutOWLOntologyViaSystemOut(owlOntologyManager, this.owlOntology);

            this.owlDataFactory = owlOntologyManager.getOWLDataFactory();
        }

        catch(OWLOntologyCreationException e) {
            throw new JSimPiException("Unable to init service context due to owl ontology creation exception.",
                    ErrorCode.ServiceContext_OWLOntologyCreationException);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public OWLOntology getOwlOntology() {
        return owlOntology;
    }

    public String getOwlOntologyDocumentIRI() {
        return owlOntologyDocumentIRI;
    }

    public OWLDataFactory getOwlDataFactory() {
        return owlDataFactory;
    }

    public OWLOntologyManager getOwlOntologyManager() {
        return owlOntologyManager;
    }

    public File getOwlFile() {
        return owlFile;
    }
}
