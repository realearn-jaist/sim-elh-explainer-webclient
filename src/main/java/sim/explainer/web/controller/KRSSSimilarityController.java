package sim.explainer.web.controller;

import sim.explainer.web.service.SimilarityService;
import org.springframework.stereotype.Controller;
import sim.explainer.web.enumeration.TypeConstant;
import sim.explainer.web.exception.ErrorCode;
import sim.explainer.web.exception.JSimPiException;
import sim.explainer.web.service.ValidationService;

import java.io.IOException;

@Controller
public class KRSSSimilarityController {
    SimilarityService similarityService;

    public KRSSSimilarityController(SimilarityService similarityService) {
        this.similarityService = similarityService;
    }


    /**
     * runchana:2023-31-07
     * Measure a similarity degree from given concepts with a specified concept and measurement types.
     *
     * @param conceptName1
     * @param conceptName2
     * @param type         concept type, i.e., KRSS or OWL
     * @param conceptType  measurement type, i.e., dynamic/top down and sim/simpi
     * @return similarity degree of that concept pair
     * @return
     * @throws IOException
     */
    public void measureSimilarity(String conceptName1, String conceptName2, TypeConstant type, String conceptType) throws IOException {
        if(conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with " + type.getDescription() + " as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.OwlSimilarityController_IllegalArguments);
        }


       similarityService.measureConceptWithType(conceptName1, conceptName2, type, conceptType);
    }


}
