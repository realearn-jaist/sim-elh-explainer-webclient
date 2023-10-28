document.addEventListener('DOMContentLoaded', function() {
    // runchana:2023-10-15 create a new input group
    function createInputGroup() {
        var newInputGroup = document.createElement('div');
        newInputGroup.classList.add('input-group', 'mb-3');

        var input1 = document.createElement('input');
        input1.type = 'text';
        input1.classList.add('form-control');
        input1.classList.add('conceptname');
        input1.placeholder = 'Concept 1';
        input1.setAttribute('aria-label', 'concept1');

        var input2 = document.createElement('input');
        input2.type = 'text';
        input2.classList.add('form-control');
        input2.classList.add('conceptname');
        input2.placeholder = 'Concept 2';
        input2.setAttribute('aria-label', 'concept2');

        var deleteButton = document.createElement('button');
        deleteButton.classList.add('btn', 'btn-outline-danger', 'delete');
        deleteButton.textContent = 'Delete';

        newInputGroup.appendChild(input1);
        newInputGroup.appendChild(input2);
        newInputGroup.appendChild(deleteButton);

        // runchana:2023-10-15 add event listener for the delete button
        deleteButton.addEventListener('click', function() {
            newInputGroup.remove();
        });

        return newInputGroup;
    }

    // runchana:2023-10-15 handle click on "Add Concept" button
    function handleAddConceptClick() {
        var addConceptButton = document.getElementById('addConcept');
        var newInputGroup = createInputGroup();

        addConceptButton.parentElement.insertBefore(newInputGroup, addConceptButton);
    }

    function handleSubmit() {
        var allConceptInputs = document.querySelectorAll('.conceptname');
        var newString = '';

        allConceptInputs.forEach(function(input, index) {
            newString += input.value;

            if ((index + 1) % 2 === 0) {
                newString += '\n'; // Add newline after every pair of concepts
            } else {
                newString += '\t'; // Add tab after Concept 1
            }
        });

        if (validateInputs(allConceptInputs)) {
                // runchana:2023-10-15 Set the value of the input field with the name "conceptname"
                var conceptNameInput = document.querySelector('[name="conceptnameInput"]');
                conceptNameInput.value = newString;

                // runchana:2023-10-15 Submit the form
                var form = conceptNameInput.form;
                form.submit();
            }
    }

    // runchana:2023-10-15 Check if values are provided or not
    function validateInputs(conceptInputs) {
        var isValid = true;

        conceptInputs.forEach(function(input) {
            if (input.value.trim() === '') {
                isValid = false;
                return;
            }
        });

        if (!isValid) { // popup message
            alert('Please provide concept names.');
        }
        return isValid;
    }

    // runchana:2023-10-15 Attach event listener to the "Add Concept" button
    var addConceptButton = document.getElementById('addConcept');
    addConceptButton.addEventListener('click', handleAddConceptClick);

    // runchana:2023-10-15 Measure similarity with explanation
    var submitButton = document.getElementById("measureButton")
    submitButton.addEventListener("click", handleSubmit);

});

// runchana:2023-10-12 check if the uploaded file is large or not, if it is larger than 10MB, notice the user that it will take a long time to upload
function checkFileSize() {
    var fileInput = document.getElementById('formFile');
    var fileSize = fileInput.files[0].size;
    var maxSize = 10 * 1024 * 1024; // 10MB in bytes

    if (fileSize > maxSize) {
        alert("The file size is more than 10MB, so the list of concept names will not be shown since it will take a long time.");
    }
}

// runchana:2023-10-26 download output by retrieving the explanation and computation from the textarea
function downloadOutput() {

    var simResult = document.getElementById('floatingTextarea2').value;

    var blob = new Blob([simResult], { type: 'text/plain' });

    var a = document.createElement('a');
    a.href = window.URL.createObjectURL(blob);
    a.download = 'explanation.txt';

    document.body.appendChild(a);

    a.click();

    document.body.removeChild(a);
}
