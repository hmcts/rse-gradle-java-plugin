package hmcts.example;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Checkstyle 8.39+ flags this annotation array indentation which is widely used at HMCTS.
 * Consequently we include it in our tests to avoid pushing a large scale refactoring onto
 * many teams.
 */
public class AnnotationIndentationTest {
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Success",
            response = String.class
        ),
        @ApiResponse(
            code = 400,
            message = ""
        ),
        @ApiResponse(
            code = 404,
            message = ""
        )
    })
    public void foo() {
    }
}
