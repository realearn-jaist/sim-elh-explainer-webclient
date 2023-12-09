package sim.explainer.web.framework.unfolding;

import org.apache.commons.lang3.StringUtils;
import sim.explainer.web.enumeration.KRSSConstant;
import sim.explainer.web.util.ParserUtils;
import sim.explainer.web.exception.ErrorCode;
import sim.explainer.web.exception.JSimPiException;
import sim.explainer.web.service.KRSSMeasurement.KRSSServiceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("conceptDefinitionUnfolderKRSSSyntax")
public class ConceptDefinitionUnfolderKRSSSyntax implements IConceptUnfolder {

    private static final Logger logger = LoggerFactory.getLogger(ConceptDefinitionUnfolderKRSSSyntax.class);

    private static final int LENGTH_AND = 3;
    private static final int LENGTH_SOME = 4;

    @Autowired
    private KRSSServiceContext krssServiceContext;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private String retrieveConceptDefinition(String concept) {

        String str = StringUtils.strip(concept);

        String definition = krssServiceContext.getFullConceptDefinitionMap().get(str);
        String definitionWithFreshConcept = krssServiceContext.getPrimitiveConceptDefinitionMap().get(str);

        return (definition != null) ? StringUtils.strip(definition) : StringUtils.strip(definitionWithFreshConcept);
    }

    private String unfold(String conceptName) {
        int beginIndex = 0;
        int lastIndex = conceptName.length();

        boolean isPreviousReadingSome = false;
        while (beginIndex < lastIndex) {

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

            if (isPreviousReadingSome) {
                beginIndex += subConcept.length() + 1;
                isPreviousReadingSome = false;
                continue;
            }

            if (subConcept.equals("and")) {
                beginIndex += LENGTH_AND + 1;
                continue;
            }

            else if (subConcept.equals("some")) {
                isPreviousReadingSome = true;
                beginIndex += LENGTH_SOME + 1;
                continue;
            }

            String subConceptDefinition = retrieveConceptDefinition(subConcept);

            if(subConceptDefinition != null) {
                String beforeSubConceptIncludingSelf = StringUtils.substring(conceptName, 0, beginIndex);
                String afterSubConceptIncludingSelf = StringUtils.substring(conceptName, beginIndex, conceptName.length());

                afterSubConceptIncludingSelf = StringUtils.replaceOnce(afterSubConceptIncludingSelf, subConcept, subConceptDefinition);

                StringBuilder entireStringBuilder = new StringBuilder(beforeSubConceptIncludingSelf);
                entireStringBuilder.append(afterSubConceptIncludingSelf);
                conceptName = entireStringBuilder.toString();

                lastIndex = conceptName.length();
            }

            // Otherwise,
            else {
                beginIndex += subConcept.length() + 1;
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
            throw new JSimPiException("Unable to unfold concept definition string as conceptName is null.", ErrorCode.ConceptDefinitionUnfolderKRSSSyntax_IllegalArguments);
        }

        // Just return if it is the top concept.
        if (conceptName.equals(KRSSConstant.TOP_CONCEPT.getStr())) {
            return conceptName;
        }

        String conceptDefinition = retrieveConceptDefinition(conceptName);

        return (conceptDefinition != null) ? unfold(conceptDefinition) : conceptName;
    }
}
