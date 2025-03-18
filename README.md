```java
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Handles the completion of each test step within a scenario by recording step details and any data tables.
 *
 * @param event the event indicating the completion of a test step.
 */
private void handleTestStepFinished(TestStepFinished event) {
    var qAPStep = new QAPSteps();

    // Extract and set the step name
    String stepName = exHelper.extractStepName(event.getTestStep());
    qAPStep.setName(stepName);

    // Extract and set the status
    String status = event.getResult().getStatus().name();
    qAPStep.setStatus(status);

    // If there is an error, set both the error message and the full stack trace
    Throwable error = event.getResult().getError();
    if (error != null) {
        qAPStep.setErrorMessage(error.getMessage());
        qAPStep.setStackTrace(ExceptionUtils.getStackTrace(error));
    }

    // Extract data table (if any) from the step
    String dataTable = exHelper.extractDataTable(event.getTestStep());
    qAPStep.setDataTable(dataTable);

    // Finally, add this step to the current scenario
    currentScenario.get().addStep(qAPStep);
}
```
