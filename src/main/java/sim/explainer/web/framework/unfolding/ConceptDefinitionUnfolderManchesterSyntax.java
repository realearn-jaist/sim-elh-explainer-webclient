package sim.explainer.web.framework.unfolding;

import org.apache.commons.lang3.StringUtils;
import sim.explainer.web.enumeration.OWLConstant;
import sim.explainer.web.util.OWLConceptDefinitionUtil;
import sim.explainer.web.util.ParserUtils;
import sim.explainer.web.exception.ErrorCode;
import sim.explainer.web.exception.JSimPiException;
import sim.explainer.web.service.OWLMeasurement.OWLServiceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("conceptDefinitionUnfolderManchesterSyntax")
public class ConceptDefinitionUnfolderManchesterSyntax implements IConceptUnfolder {

    private static final Logger logger = LoggerFactory.getLogger(ConceptDefinitionUnfolderManchesterSyntax.class);

    @Autowired
    private OWLServiceContext OWLServiceContext;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String unfold(String conceptName) {
        int beginIndex = 0;
        int lastIndex = conceptName.length();

        while (beginIndex < lastIndex) {
            if(logger.isDebugEnabled()) {
                logger.debug("Begin index: " + beginIndex + " and Last index: " + lastIndex);
            }

            if (conceptName.charAt(beginIndex) == ParserUtils.OPEN_PARENTHESIS_CHAR || conceptName.charAt(beginIndex) == ParserUtils.CLOSE_PARENTHESIS_CHAR) {
                beginIndex++;
                continue;
            }

            int readingIndex;
            int nextWhitespaceIndex = conceptName.indexOf(StringUtils.SPACE, beginIndex);
            int nextCloseParenthesisIndex = conceptName.indexOf(ParserUtils.CLOSE_PARENTHESIS_STR, beginIndex);

            // Determine a value of the readingIndex
            // If there exist both nextWhitespaceIndex and nextCloseParenthesisIndex
            if (nextWhitespaceIndex > -1 && nextCloseParenthesisIndex > -1) {
                if (nextWhitespaceIndex <= nextCloseParenthesisIndex) {
                    readingIndex = nextWhitespaceIndex;
                }

                else {
                    readingIndex = nextCloseParenthesisIndex;
                }
            }

            // If there exists only nextWhitespaceIndex
            else if (nextWhitespaceIndex > -1) {
                readingIndex = nextWhitespaceIndex;
            }

            // If there exists only nextCloseParenthesisIndex
            else if (nextCloseParenthesisIndex > -1) {
                readingIndex = nextCloseParenthesisIndex;
            }

            // By default, readingIndex is set to the length of conceptName
            else {
                readingIndex = conceptName.length();
            }

            String subConcept = StringUtils.substring(conceptName, beginIndex, readingIndex);

            if(logger.isDebugEnabled()) {
                logger.debug("Current subConcept is " + subConcept);
            }

            // If subConcept is unfoldable,
            String subConceptDefinition = OWLConceptDefinitionUtil.generateFullConceptDefinitionManchesterSyntax(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), subConcept);

            if(logger.isDebugEnabled()) {
                logger.debug("Current subConceptDefinition of " + subConcept + " is " + subConceptDefinition);
            }

            if(subConceptDefinition != null) {
                String beforeSubConceptIncludingSelf = StringUtils.substring(conceptName, 0, beginIndex);
                String afterSubConceptIncludingSelf = StringUtils.substring(conceptName, beginIndex, conceptName.length());

                StringBuilder subDefinitionBuilder = new StringBuilder(ParserUtils.OPEN_PARENTHESIS_STR);
                subDefinitionBuilder.append(subConceptDefinition);
                subDefinitionBuilder.append(ParserUtils.CLOSE_PARENTHESIS_STR);
                afterSubConceptIncludingSelf = StringUtils.replaceOnce(afterSubConceptIncludingSelf, subConcept, subDefinitionBuilder.toString());

                StringBuilder entireStringBuilder = new StringBuilder(beforeSubConceptIncludingSelf);
                entireStringBuilder.append(afterSubConceptIncludingSelf);
                conceptName = entireStringBuilder.toString();

                lastIndex = conceptName.length();
            }

            // Otherwise,
            else {
                beginIndex += subConcept.length() + 1;
            }

            if(logger.isDebugEnabled()) {
                logger.debug("Current conceptName is " + conceptName);
            }
        }

        return conceptName;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String unfoldConceptDefinitionString(String conceptName) {
        if (conceptName == null) {
            throw new JSimPiException("Unable to unfold concept string due to conceptName is null.", ErrorCode.ConceptUnfolderManchesterSyntax_IllegalArguments);
        }

        // Just return if it is the top concept.
        if (conceptName.equals(OWLConstant.TOP_CONCEPT_1.getOwlSyntax()) || conceptName.equals(OWLConstant.TOP_CONCEPT_2.getOwlSyntax())) {
            return conceptName;
        }

        String fullDefinition = OWLConceptDefinitionUtil.generateFullConceptDefinitionManchesterSyntax(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), conceptName);

        return (fullDefinition != null) ? unfold(fullDefinition) : conceptName;
    }
}
