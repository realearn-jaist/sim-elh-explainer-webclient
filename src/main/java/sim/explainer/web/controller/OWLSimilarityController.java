package sim.explainer.web.controller;

import org.springframework.stereotype.Controller;
import sim.explainer.web.service.SimilarityService;
import sim.explainer.web.service.ValidationService;
import sim.explainer.web.enumeration.TypeConstant;
import sim.explainer.web.exception.ErrorCode;
import sim.explainer.web.exception.JSimPiException;

import java.io.IOException;
import java.math.BigDecimal;

@Controller
public class OWLSimilarityController {

    private ValidationService validationService;

    private SimilarityService similarityService;

    public OWLSimilarityController(ValidationService validationService, SimilarityService similarityService) {
        this.validationService = validationService;
        this.similarityService = similarityService;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void validateInputs(String conceptName1, String conceptName2) {
        if (!validationService.validateIfOWLClassNamesExist(conceptName1, conceptName2)) {
            throw new JSimPiException("Unable to measure similarity with OWL sim as conceptName1["
                    + conceptName1 + "] and conceptName2[" + conceptName2 + "] are invalid names.", ErrorCode.OwlSimilarityController_InvalidConceptNames);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * runchana:2023-31-07
     * Measure a similarity degree from given concepts with a specified concept and measurement types.
     *
     * @param conceptName1
     * @param conceptName2
     * @param type  concept type, i.e., KRSS or OWL
     * @param conceptType measurement type, i.e., dynamic/top down and sim/simpi
     * @return similarity degree of that concept pair
     * @return
     * @throws IOException
     */
    public BigDecimal measureSimilarity(String conceptName1, String conceptName2, TypeConstant type, String conceptType) {
        if(conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with " + type.getDescription() + " as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.OwlSimilarityController_IllegalArguments);
        }

        validateInputs(conceptName1, conceptName2);

        return similarityService.measureConceptWithType(conceptName1, conceptName2, type, conceptType);
    }

}
