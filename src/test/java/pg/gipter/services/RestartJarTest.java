package pg.gipter.services;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RestartJarTest {

//    2025-09-21 13:35:45 [ERROR] [JavaFX Application Thread] gipter.services.RestartService:
//    Could not restart application gracefully.
//    Shutting it down. Cannot run program "C:\Install\Gipter\exe\runtime\bin\java":
//    CreateProcess error=2, The system cannot find the file specified

    @Test
    void name() {
        Path java = Path.of("C:", "Install", "Gipter", "exe", "runtime", "bin", "java.dll");
        Path insDir = Path.of("runtime", "bin", "java.dll");
        assertThat(java).endsWith(insDir);
    }
}