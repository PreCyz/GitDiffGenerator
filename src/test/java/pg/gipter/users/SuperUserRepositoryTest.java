package pg.gipter.users;

import org.junit.jupiter.api.Test;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.services.SecurityService;

import static org.assertj.core.api.Assertions.assertThat;

class SuperUserRepositoryTest {

    @Test
    void name() {

        SuperUser superUser = new SuperUser();
        String ed = encrypt("ZV#gGt?QSN_&aJqX9PsUvDRc7x%uk1hw$mE28Yn!T-4H35KbyMB6A");
        System.out.println(ed);
        superUser.setPassword(ed);

        ed = encrypt("su-netc-goto-ncscopy");
        System.out.println(ed);
        superUser.setUsername(ed);

        SuperUserService.getInstance().superUser = superUser;

        assertThat(SuperUserService.getInstance().getUserName()).isEqualTo("su-netc-goto-ncscopy");
        assertThat(SuperUserService.getInstance().getPassword()).isEqualTo("ZV#gGt?QSN_&aJqX9PsUvDRc7x%uk1hw$mE28Yn!T-4H35KbyMB6A");
    }

    private String encrypt(String value) {

        CipherDetails cipher = new CipherDetails();
        cipher.setCipherName("PBEwithSHA1AndDESede");
        cipher.setIterationCount(10);
        cipher.setKeySpecValue("97742dd8-9e89-492a-a2cd-15fee66e64da");
        cipher.setSaltValue("1689777695012");

        return SecurityService.getInstance().encrypt(value, cipher);
    }
}