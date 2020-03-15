package pg.gipter.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticVersioningTest {

    @Test
    void givenNewerMajor_whenIsNewerVersion_thenReturnTrue() {
        SemanticVersioning oldVersion = new SemanticVersioning(0, 0, 0);
        SemanticVersioning newVersion = new SemanticVersioning(1, 0, 0);

        boolean actual = oldVersion.isNewerVersion(newVersion);

        assertThat(actual).isTrue();
    }

    @Test
    void givenNewerMinor_whenIsNewerVersion_thenReturnTrue() {
        SemanticVersioning oldVersion = new SemanticVersioning(1, 0, 0);
        SemanticVersioning newVersion = new SemanticVersioning(1, 1, 0);

        boolean actual = oldVersion.isNewerVersion(newVersion);

        assertThat(actual).isTrue();
    }

    @Test
    void givenNewerPatch_whenIsNewerVersion_thenReturnTrue() {
        SemanticVersioning oldVersion = new SemanticVersioning(1, 1, 0);
        SemanticVersioning newVersion = new SemanticVersioning(1, 1, 1);

        boolean actual = oldVersion.isNewerVersion(newVersion);

        assertThat(actual).isTrue();
    }

    @Test
    void givenNewerAndNullLabel_whenIsNewerVersion_thenReturnTrue() {
        SemanticVersioning oldVersion = new SemanticVersioning(1, 1, 1);
        SemanticVersioning newVersion = new SemanticVersioning(1, 1, 1);
        newVersion.setAdditionalLabel("alpha");

        boolean actual = oldVersion.isNewerVersion(newVersion);

        assertThat(actual).isTrue();
    }

    @Test
    void givenNewerLabel_whenIsNewerVersion_thenReturnTrue() {
        SemanticVersioning oldVersion = new SemanticVersioning(1, 1, 1);
        oldVersion.setAdditionalLabel("alpha");
        SemanticVersioning newVersion = new SemanticVersioning(1, 1, 1);
        newVersion.setAdditionalLabel("beta");

        boolean actual = oldVersion.isNewerVersion(newVersion);

        assertThat(actual).isTrue();
    }

    @Test
    void givenSameVersionWithLabels_whenIsNewerVersion_thenReturnFalse() {
        SemanticVersioning oldVersion = new SemanticVersioning(1, 1, 1);
        oldVersion.setAdditionalLabel("alpha");
        SemanticVersioning newVersion = new SemanticVersioning(1, 1, 1);
        newVersion.setAdditionalLabel("alpha");

        boolean actual = oldVersion.isNewerVersion(newVersion);

        assertThat(actual).isFalse();
    }

    @Test
    void givenSameVersionWithouLabels_whenIsNewerVersion_thenReturnFalse() {
        SemanticVersioning oldVersion = new SemanticVersioning(1, 1, 1);
        SemanticVersioning newVersion = new SemanticVersioning(1, 1, 1);

        boolean actual = oldVersion.isNewerVersion(newVersion);

        assertThat(actual).isFalse();
    }
}