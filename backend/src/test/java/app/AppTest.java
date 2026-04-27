package app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    @DisplayName("App loads without errors")
    public void contextLoads() {
        Assertions.assertDoesNotThrow(() -> App.main(new String[]{}));
    }

}