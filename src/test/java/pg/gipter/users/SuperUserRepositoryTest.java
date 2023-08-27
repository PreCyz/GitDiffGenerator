package pg.gipter.users;

import org.junit.jupiter.api.Test;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.services.SecurityService;

import static org.assertj.core.api.Assertions.assertThat;

class SuperUserRepositoryTest {

    @Test
    void givenCipherDetails_whenSuperUser_thenReturnProperUser() {

        SuperUser superUser = new SuperUser();
        String ed = encrypt("password");
        superUser.setPassword(ed);
        ed = encrypt("username");
        superUser.setUsername(ed);
        superUser.setCipherDetails(getCipherDetails());

        SuperUserService.getInstance().superUser = superUser;

        assertThat(SuperUserService.getInstance().getUserName()).isEqualTo("username");
        assertThat(SuperUserService.getInstance().getPassword()).isEqualTo("password");
    }

    private String encrypt(String value) {
        return SecurityService.getInstance().encrypt(value, getCipherDetails());
    }

    private static CipherDetails getCipherDetails() {
        CipherDetails cipher = new CipherDetails();
        cipher.setCipherName("PBEWithMD5AndDES");
        cipher.setIterationCount(2);
        cipher.setKeySpecValue("97742dd8-9e89-492a-a2cd-15fee66e64da");
        cipher.setSaltValue("jorle-juhu");
        return cipher;
    }
}