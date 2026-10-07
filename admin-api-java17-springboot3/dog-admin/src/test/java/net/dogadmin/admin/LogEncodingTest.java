package net.dogadmin.admin;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogEncodingTest {

    @Test
    void chineseLogsMatchConsoleEncodingAndKeepFilesUtf8() throws Exception {
        String message = "【dog-admin】服务已成功启动，当前启动环境：dev，开发环境，示例任务";
        var event = Log4jLogEvent.newBuilder()
                .setLoggerName(LogEncodingTest.class.getName())
                .setLevel(Level.WARN)
                .setMessage(new SimpleMessage(message))
                .build();
        var parser = DocumentBuilderFactory.newInstance();
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        List<Path> configurations;
        try (var resources = Files.walk(Path.of("src/main/resources"))) {
            configurations = resources.filter(path -> path.getFileName().toString().equals("log4j2-spring.xml")).toList();
        }
        assertFalse(configurations.isEmpty());
        String[] propertyNames = {"stdout.encoding", "sun.stdout.encoding"};
        String[] originalValues = {System.getProperty(propertyNames[0]), System.getProperty(propertyNames[1])};
        String[][] runtimeEncodings = {{"ms936", "GBK"}, {"UTF-8", "GBK"}, {"", "GBK"}, {"", ""}};
        Charset[] expectedConsoleEncodings = {Charset.forName("ms936"), StandardCharsets.UTF_8, Charset.forName("GBK"), StandardCharsets.UTF_8};
        try {
            for (int scenario = 0; scenario < runtimeEncodings.length; scenario++) {
                for (int index = 0; index < propertyNames.length; index++) {
                    if (runtimeEncodings[scenario][index].isEmpty()) {
                        System.clearProperty(propertyNames[index]);
                    } else {
                        System.setProperty(propertyNames[index], runtimeEncodings[scenario][index]);
                    }
                }
                var logConfiguration = new DefaultConfiguration();
                for (Path configuration : configurations) {
                    var layouts = parser.newDocumentBuilder().parse(configuration.toFile()).getElementsByTagName("PatternLayout");
                    assertTrue(layouts.getLength() > 0, configuration.toString());
                    for (int index = 0; index < layouts.getLength(); index++) {
                        var element = (Element) layouts.item(index);
                        boolean isConsole = element.getParentNode().getNodeName().equals("Console");
                        Charset expected = isConsole ? expectedConsoleEncodings[scenario] : StandardCharsets.UTF_8;
                        String configuredEncoding = logConfiguration.getStrSubstitutor().replace(element.getAttribute("charset"));
                        assertEquals(expected, Charset.forName(configuredEncoding), configuration.toString());
                        var layout = PatternLayout.newBuilder()
                                .withPattern(element.getAttribute("pattern"))
                                .withCharset(Charset.forName(configuredEncoding))
                                .withDisableAnsi(true)
                                .build();
                        assertTrue(new String(layout.toByteArray(event), expected).contains(message), configuration.toString());
                        if (isConsole) {
                            var output = new ByteArrayOutputStream();
                            PrintStream originalOutput = System.out;
                            try (var capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
                                System.setOut(capturedOutput);
                                var console = ConsoleAppender.newBuilder()
                                        .setName("EncodingCheck")
                                        .setTarget(ConsoleAppender.Target.SYSTEM_OUT)
                                        .setFollow(true)
                                        .setLayout(layout)
                                        .build();
                                console.start();
                                try {
                                    console.append(event);
                                } finally {
                                    console.stop();
                                }
                            } finally {
                                System.setOut(originalOutput);
                            }
                            assertArrayEquals(layout.toByteArray(event), output.toByteArray(), configuration.toString());
                        }
                    }
                }
            }
        } finally {
            for (int index = 0; index < propertyNames.length; index++) {
                if (originalValues[index] == null) {
                    System.clearProperty(propertyNames[index]);
                } else {
                    System.setProperty(propertyNames[index], originalValues[index]);
                }
            }
        }
    }
}
