# sim-elh-explainer-webclient

## Features
Sim-elh-explainer is a web application that measures the similarity of OWL-based class expressions based on the logic-based semantics in the Description Logic ELH. This project allows users to input the .owl or .krss file, which coincides with the *description logic ELH* or the *OWL EL profile*, to measure the similarity of a pair of concept names.

The project can be run through WebApplication.java, using [Spring Boot Application](https://spring.io/guides/gs/spring-boot/), or access via [Similarity ELH Explainer Website](https://sim-elh-explainer-webclient-9ce19f74cc4c.herokuapp.com/).
* In case that the project is running locally, the port on IntelliJ should be 8080.

## How to Use sim-elh-explainer-webclient

1. Upload a file in .krss or .owl format. Please be aware that if the file size exceeds 10MB, this web application will not display a list of concept names on the webpage. You may download the demo data named family.owl from this repository as well.
[![image.png](https://i.postimg.cc/g2HKzbJz/image.png)](https://postimg.cc/5XjzgGLT)
2. After successfully uploading the file, review the list of concept names and select a computation method. You have the option to choose between dynamic programming and top-down. Please note that the preference profile input is currently under development, so you may leave it blank. [![image.png](https://i.postimg.cc/0jZwgPnF/image.png)](https://postimg.cc/vxgBfRpt)
3. Enter a pair of concept names that you would like to measure the similarity for. If you want to measure more than one pair, you can click on the "Add More Concept Name" button. You can also delete any added rows. It is important to ensure that the pair of concept names are entered in their entirety; leaving them blank is not permitted. [![Screenshot-2023-10-28-105922.png](https://i.postimg.cc/sXThq3w8/Screenshot-2023-10-28-105922.png)](https://postimg.cc/sQZxZCF9)
4. Click on the "Measure" button to initiate the computation. The result of the computation, along with its explanation, will be displayed in the textbox below. Additionally, you have the option to download the output as a .txt file. [![image.png](https://i.postimg.cc/dtbTzHjq/image.png)](https://postimg.cc/hf0jm0Yw)

## Publications

* Teeradaj Racharak, On Approximation of Concept Similarity Measure in Description Logic ELH with Pre-trained Word Embedding, In IEEE Access, vol. 9, pp. 61429-61443, 2021. DOI: 10.1109/ACCESS.2021.3073730
* Teeradaj Racharak, Boontawee Suntisrivaraporn, and Satoshi Tojo, Personalizing a Concept Similarity Measure in the Description Logic ELH with Preference Profile, In Computing and Informatics vol. 37, no. 3, pp. 581-613, 2018. DOI: 10.4149/cai_2018_3_581