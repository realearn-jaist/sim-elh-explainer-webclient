package sim.explainer.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import sim.explainer.web.service.*;
import sim.explainer.web.service.KRSSMeasurement.KRSSDynamicProgrammingService;
import sim.explainer.web.service.OWLMeasurement.OWLDynamicProgrammingSimService;
import sim.explainer.web.service.OWLMeasurement.OWLServiceContext;
import sim.explainer.web.service.OWLMeasurement.OWLTopDownProgrammingSimService;
import sim.explainer.web.exception.JSimPiException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Controller
public class WebController {

    private final OWLServiceContext owlServiceContext = new OWLServiceContext();

    private final SimilarityService similarityService = new SimilarityService();
    private final ValidationService validationService = new ValidationService(owlServiceContext);

    private final OWLSimilarityController owlSimilarityController = new OWLSimilarityController(validationService, similarityService);
    private final KRSSSimilarityController krssSimilarityController = new KRSSSimilarityController(similarityService);

    private final OWLDynamicProgrammingSimService owlDynamicProgrammingSimService = new OWLDynamicProgrammingSimService(owlServiceContext, owlSimilarityController, validationService, similarityService);

    // runchana:2023-10-21 Display the main page
    @GetMapping("/")
    public String mainPage() {
        return "mainPage";
    }

    // runchana:2023-10-12 Display a list of concept names when users upload a file
    @PostMapping("/")
    public String submitFile(@RequestParam("formFile") MultipartFile file, HttpSession session, Model model) throws IOException {
        if (!file.isEmpty()){
            File tempFile = File.createTempFile("temp-file", ".tmp");
            file.transferTo(tempFile);

            if(!SimilarityService.checkOWLandKRSSFile(Objects.requireNonNull(file.getOriginalFilename()))) { // KRSS

                model.addAttribute("columnSize", "col-md-5");

                session.setAttribute("fileName", file.getOriginalFilename());
                session.setAttribute("file", tempFile);

                return "mainPageResult";
            }

            else {
                // runchana:2023-10-12 Retrieve a list of concept names and roles from SimilarityService
                List<String> conceptList = SimilarityService.retrieveConceptName(tempFile);
                Set<String> propertyList = SimilarityService.getProcessedProperties();

                String formattedConceptList = String.join("\n", conceptList);

                // runchana:2023-10-12 Display a list of concept names with its file name
                model.addAttribute("fileName", file.getOriginalFilename());
                model.addAttribute("conceptList", formattedConceptList);
                model.addAttribute("propertyList", propertyList);

                // runchana:2023-10-12 Adjust a size of column if the list of property cannot be retrieved.
                if (propertyList.isEmpty()) {
                    model.addAttribute("columnSize", "col-md-5");
                } else {
                    model.addAttribute("columnSize", "col-md-14");
                }

                // runchana:2023-10-12 Store the file name, concept names, and temporary file path in the session
                session.setAttribute("fileName", file.getOriginalFilename());
                session.setAttribute("conceptList", formattedConceptList);
                session.setAttribute("propertyList", propertyList);
                session.setAttribute("tempFilePath", tempFile.getAbsolutePath());
                session.setAttribute("file", tempFile);
            }

        }
        return "mainPage";
    }

    @GetMapping("/simMeasurement")
    public String simMeasurement() {
        return "redirect:/";
    }
    @GetMapping("/simResult")
    public String simResult() {
        return "redirect:/";
    }

    @PostMapping("/simMeasurement")
    public String simMeasurement(@RequestParam("approach") String optionVal, @RequestParam("profile") String profile, HttpSession session, Model model) {

        String filePath = (String) session.getAttribute("tempFilePath");
        String fileName = (String) session.getAttribute("fileName");
        String conceptList = (String) session.getAttribute("conceptList");

        Set<String> propertyList = (Set<String>) session.getAttribute("propertyList");

        if (!filePath.isEmpty()){

            getFilename(model, fileName, conceptList, propertyList);

            if (optionVal != null) {
                model.addAttribute("optionVal", optionVal);
                session.setAttribute("optionVal", optionVal);
            }
        }

        model.addAttribute("redirectToAnchor", true);

        return "mainPageResult";
    }

    // runchana:2023-10-17 Display a similarity function for computation
    @PostMapping("/simResult")
    public String simMeasurementResult(
            @RequestParam("conceptnameInput") String conceptnameInput,
            HttpSession session,
            Model model) throws IOException {
        String filePath = (String) session.getAttribute("tempFilePath");
        String fileName = (String) session.getAttribute("fileName");
        String optionVal = (String) session.getAttribute("optionVal");
        String conceptList = (String) session.getAttribute("conceptList");

        File inputFile = (File) session.getAttribute("file");

        StringBuilder simResult = new StringBuilder();

        Set<String> propertyList = (Set<String>) session.getAttribute("propertyList");

        if (!filePath.isEmpty()) {
            getFilename(model, fileName, conceptList, propertyList);

            if (optionVal != null) {
                model.addAttribute("optionVal", optionVal); // method selection

                ExplanationService.explanation.setLength(0); // clear explanation

                if (SimilarityService.checkOWLandKRSSFile(fileName)) { // owl
                    if (optionVal.equals("dynamic")) { // dynamic computation
                        OWLDynamicProgrammingSimService.readInputOWLOntology(inputFile, conceptnameInput);

                        simResult = OWLDynamicProgrammingSimService.computeDynamicProgramming();
                    } else if (optionVal.equals("topdown")) { // topdown computation
                        OWLTopDownProgrammingSimService.readInputOWLOntology(inputFile, conceptnameInput);

                        simResult = OWLTopDownProgrammingSimService.computeTopDownProgramming();
                    }
                } else {
                    KRSSDynamicProgrammingService.readInputKRSSOntology(inputFile, conceptnameInput);

                    simResult = KRSSDynamicProgrammingService.computeDynamicProgramming();
                }

            }
        }

        model.addAttribute("simResult", simResult); // result of measurement
        model.addAttribute("redirectToAnchor", true); // redirect to the computation part
        return "mainPageResult";
    }

    // runchana:2023-10-17 handle error exception, e.g., invalid concept names
    @ExceptionHandler(JSimPiException.class)
    public String handleCustomException(JSimPiException e, Model model) {
        String errorMessage = "An error occurred: " + e.getMessage();
        model.addAttribute("popupMessage", errorMessage);
        return "errorPage";
    }

    /**
     * runchana:2023-10-17 retrieve the file name of the uploaded file
     * @param model
     * @param fileName
     * @param conceptList
     * @param propertyList
     */
    private void getFilename(Model model, String fileName, String conceptList, Set<String> propertyList) {
        model.addAttribute("fileName", fileName);
        model.addAttribute("conceptList", conceptList);
        model.addAttribute("propertyList", propertyList);

        if (propertyList.isEmpty() || propertyList == null) {
            model.addAttribute("columnSize", "col-md-5");
        } else {
            model.addAttribute("columnSize", "col-md-14");
        }
    }

}
