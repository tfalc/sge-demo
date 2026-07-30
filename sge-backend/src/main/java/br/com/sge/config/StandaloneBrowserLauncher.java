package br.com.sge.config;

import java.awt.Desktop;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("standalone")
@ConditionalOnProperty(name = "sge.desktop.launcher", havingValue = "false", matchIfMissing = true)
public class StandaloneBrowserLauncher {

    private static final Logger log = LoggerFactory.getLogger(StandaloneBrowserLauncher.class);

    @Value("${server.port:8080}")
    private int serverPort;

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        if (!Desktop.isDesktopSupported()) {
            log.info("Abra o navegador em http://localhost:{}", serverPort);
            return;
        }

        try {
            Desktop.getDesktop().browse(URI.create("http://localhost:" + serverPort));
        } catch (Exception ex) {
            log.info("Abra o navegador em http://localhost:{}", serverPort);
        }
    }
}
