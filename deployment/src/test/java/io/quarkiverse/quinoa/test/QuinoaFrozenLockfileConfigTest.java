package io.quarkiverse.quinoa.test;

import static io.quarkiverse.quinoa.test.QuinoaPackageManagerNPMConfigTest.prepareLockFiles;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class QuinoaFrozenLockfileConfigTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setAllowTestClassOutsideDeployment(true)
            .setBeforeAllCustomizer(() -> {
                System.setProperty("CI", "true");
                prepareLockFiles("yarn.lock");
            })
            .setAfterAllCustomizer(() -> {
                System.clearProperty("CI");
                QuinoaPackageManagerNPMConfigTest.deleteLockFiles();
            })
            .overrideConfigKey("quarkus.quinoa.ui-dir", "src/test/webui")
            .overrideConfigKey("quarkus.quinoa.always-install", "true")
            .setLogRecordPredicate(log -> true)
            .assertLogRecords(l -> {
                assertThat(l).anySatisfy(s -> {
                    assertThat(s.getMessage()).isEqualTo("Running Quinoa package manager install command: %s");
                    assertThat(s.getParameters()[0]).isEqualTo("yarn install --frozen-lockfile");
                });
            });

    @Test
    public void testQuinoa() {
        assertThat(Path.of("src/test/webui/node_modules/installed")).isRegularFile()
                .hasContent("hello");
    }
}
