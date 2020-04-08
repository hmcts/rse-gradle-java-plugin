package hmcts.example;

import java.util.ArrayList;
import java.util.List;

public class Library {
    // This code causes the DataflowAnomalyAnalysis check to fail, flagging bar as unreferenced
    // even though it is used in the foreach loop, a known issue.
    // This is a known issue - https://sourceforge.net/p/pmd/bugs/1383/
    // The rule should be disabled.
    public String mustNotGeneratePmdWarning() {
        List<String> strings = new ArrayList<>();
        String bar = "bar";
        for (String string : strings) {
            bar = bar.replace(bar, string);
        }

        return bar;
    }
}
