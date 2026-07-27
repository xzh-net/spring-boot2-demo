package net.xzh.generator.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import net.xzh.generator.service.GeneratorService;

/**
 * GenUtils测试用例
 *
 * @date 2019/5/10
 */
@SpringBootTest
public class GenUtilsTest {

    @Autowired
    private GeneratorService generatorService;

    @Test
    public void testGeneratorCodeAndSaveToDisk() throws IOException {
        byte[] data = generatorService.generatorCode(new String[]{"sys_user"});
        Assertions.assertThat(data).isNotEmpty();
        System.out.println("生成的代码包大小: " + data.length + " bytes");

        Path outputDir = Paths.get("D:\\generator_output");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        System.out.println("输出目录: " + outputDir.toAbsolutePath());

        Path outputFile = outputDir.resolve("generator.zip");
        Files.deleteIfExists(outputFile);
        Files.write(outputFile, data);

        System.out.println("代码已成功生成并保存到: " + outputFile.toAbsolutePath());
        Assertions.assertThat(Files.exists(outputFile)).isTrue();
    }
}