package br.com.sge.launcher;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public final class SgeDesktopLauncher {

    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_PORT_SCAN = 50;
    private static final Pattern TOMCAT_PORT_PATTERN =
            Pattern.compile("Tomcat started on port (\\d+)");
    private static final String BACKEND_JAR = "sge-backend-0.0.1-SNAPSHOT.jar";

    private final JTextArea logArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("Parado");
    private final JButton startButton = new JButton("Iniciar app");
    private final JButton stopButton = new JButton("Finalizar app");
    private final JButton openBrowserButton = new JButton("Abrir no navegador");

    private JFrame mainFrame;
    private Process serverProcess;
    private Thread logThread;
    private volatile boolean browserOpened;
    private volatile boolean shuttingDown;
    private volatile int activePort = DEFAULT_PORT;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        System.setProperty("sun.java2d.d3d", "false");
        Toolkit.getDefaultToolkit();

        SgeDesktopLauncher launcher = new SgeDesktopLauncher();
        Runtime.getRuntime().addShutdownHook(new Thread(launcher::stopServerSilently, "sge-shutdown-hook"));

        try {
            SwingUtilities.invokeAndWait(launcher::show);
            launcher.shutdownLatch.await();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao abrir o SGE:\n" + ex.getMessage(),
                    "SGE",
                    JOptionPane.ERROR_MESSAGE);
            Runtime.getRuntime().halt(1);
        }
    }

    private void show() {
        mainFrame = new JFrame("SGE — Sistema de Gestao Escolar");
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                SwingUtilities.invokeLater(SgeDesktopLauncher.this::requestShutdown);
            }
        });

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
        openBrowserButton.addActionListener(e -> openBrowser());

        stopButton.setEnabled(false);
        openBrowserButton.setEnabled(false);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(statusLabel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(openBrowserButton);
        buttons.add(startButton);
        buttons.add(stopButton);
        top.add(buttons, BorderLayout.EAST);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);

        mainFrame.getContentPane().add(top, BorderLayout.NORTH);
        mainFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        mainFrame.setMinimumSize(new Dimension(720, 480));
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);

        appendLog("Bem-vindo ao SGE.");
        appendLog("Clique em \"Iniciar app\" para subir o sistema.");
        appendLog("Quando estiver pronto, o navegador abrira automaticamente.");
        appendLog("Use \"Finalizar app\" ou feche a janela para encerrar tudo.");
        appendLog("");
    }

    private void startServer() {
        if (serverProcess != null && serverProcess.isAlive()) {
            return;
        }

        try {
            RuntimePaths paths = resolveRuntimePaths();
            activePort = findAvailablePort(DEFAULT_PORT);
            browserOpened = false;
            setRunningState(true);
            appendLog("Iniciando servidor...");
            if (activePort != DEFAULT_PORT) {
                appendLog("Porta " + DEFAULT_PORT + " ocupada. Usando porta " + activePort + ".");
            } else {
                appendLog("Usando porta " + activePort + ".");
            }

            ProcessBuilder builder = new ProcessBuilder(
                    paths.javaExecutable().toString(),
                    "-Dspring.profiles.active=standalone",
                    "-Dfile.encoding=UTF-8",
                    "-Dsge.desktop.launcher=true",
                    "-Dserver.port=" + activePort,
                    "-jar",
                    paths.backendJar().toString());
            builder.directory(paths.workingDirectory().toFile());
            builder.redirectErrorStream(true);
            serverProcess = builder.start();

            logThread = new Thread(() -> readProcessOutput(serverProcess), "sge-log-reader");
            logThread.setDaemon(true);
            logThread.start();

            appendLog("Servidor em execucao. Aguarde a mensagem de pronto nos logs.");
        } catch (Exception ex) {
            setRunningState(false);
            appendLog("Erro ao iniciar: " + ex.getMessage());
            JOptionPane.showMessageDialog(
                    null,
                    "Nao foi possivel iniciar o SGE.\n\n" + ex.getMessage(),
                    "Erro ao iniciar",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopServer() {
        if (serverProcess == null) {
            setRunningState(false);
            return;
        }

        appendLog("Encerrando servidor...");
        terminateProcess(serverProcess);
        serverProcess = null;
        activePort = DEFAULT_PORT;
        setRunningState(false);
        appendLog("Servidor finalizado.");
    }

    private void stopServerSilently() {
        if (serverProcess != null && serverProcess.isAlive()) {
            terminateProcess(serverProcess);
            serverProcess = null;
        }
    }

    private void terminateProcess(Process process) {
        if (process == null || !process.isAlive()) {
            return;
        }

        process.destroy();
        try {
            if (!process.waitFor(8, TimeUnit.SECONDS)) {
                if (isWindows()) {
                    killProcessTree(process.pid());
                } else {
                    process.destroyForcibly();
                }
                process.waitFor(3, TimeUnit.SECONDS);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }

    private void killProcessTree(long pid) {
        try {
            new ProcessBuilder("taskkill", "/PID", Long.toString(pid), "/T", "/F")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor(5, TimeUnit.SECONDS);
        } catch (Exception ex) {
            appendLog("Aviso: nao foi possivel encerrar todos os processos filhos.");
        }
    }

    private void readProcessOutput(Process process) {
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String currentLine = line;
                SwingUtilities.invokeLater(() -> handleLogLine(currentLine));
            }
        } catch (IOException ex) {
            if (!shuttingDown) {
                SwingUtilities.invokeLater(() -> appendLog("Leitura de logs interrompida: " + ex.getMessage()));
            }
        }

        try {
            int exitCode = process.waitFor();
            SwingUtilities.invokeLater(() -> {
                if (serverProcess == process) {
                    serverProcess = null;
                    setRunningState(false);
                    if (!shuttingDown) {
                        appendLog("Servidor encerrou (codigo " + exitCode + ").");
                    }
                }
            });
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleLogLine(String line) {
        Matcher tomcatPort = TOMCAT_PORT_PATTERN.matcher(line);
        if (tomcatPort.find()) {
            activePort = Integer.parseInt(tomcatPort.group(1));
            statusLabel.setText("Em execucao (porta " + activePort + ")");
        }

        appendLog(line);
        if (!browserOpened
                && (line.contains("Started SgeApplication")
                        || line.contains("Tomcat started on port"))) {
            browserOpened = true;
            openBrowserButton.setEnabled(true);
            openBrowser();
        }
    }

    private void openBrowser() {
        String appUrl = appUrl();
        if (!Desktop.isDesktopSupported()) {
            appendLog("Abra manualmente: " + appUrl);
            return;
        }
        try {
            Desktop.getDesktop().browse(URI.create(appUrl));
            appendLog("Navegador aberto em " + appUrl);
        } catch (Exception ex) {
            appendLog("Nao foi possivel abrir o navegador. Acesse: " + appUrl);
        }
    }

    private void requestShutdown() {
        if (shuttingDown) {
            return;
        }

        if (serverProcess != null && serverProcess.isAlive()) {
            int choice = JOptionPane.showConfirmDialog(
                    null,
                    "O servidor ainda esta em execucao.\nDeseja finalizar o SGE antes de fechar?",
                    "Finalizar SGE",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
            stopServer();
        }

        shuttingDown = true;
        stopServerSilently();
        if (mainFrame != null) {
            mainFrame.setVisible(false);
            mainFrame.dispose();
            mainFrame = null;
        }
        shutdownLatch.countDown();
        new Thread(
                        () -> {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                            Runtime.getRuntime().halt(0);
                        },
                        "sge-exit")
                .start();
    }

    private void setRunningState(boolean running) {
        startButton.setEnabled(!running);
        stopButton.setEnabled(running);
        statusLabel.setText(running ? "Em execucao (porta " + activePort + ")" : "Parado");
        if (!running) {
            openBrowserButton.setEnabled(false);
        }
    }

    private String appUrl() {
        return "http://localhost:" + activePort;
    }

    private static int findAvailablePort(int preferredPort) throws IOException {
        for (int port = preferredPort; port < preferredPort + MAX_PORT_SCAN; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        throw new IOException(
                "Nenhuma porta livre entre " + preferredPort + " e " + (preferredPort + MAX_PORT_SCAN - 1));
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private void appendLog(String message) {
        logArea.append(message + System.lineSeparator());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private static RuntimePaths resolveRuntimePaths() throws IOException {
        Path javaHome = Path.of(System.getProperty("java.home"));
        Path javaExecutable = javaHome.resolve("bin").resolve(isWindows() ? "java.exe" : "java");
        if (!Files.exists(javaExecutable)) {
            javaExecutable = Path.of("java");
        }

        Path jpackageBase = javaHome.getParent();
        Path jpackageAppDir = jpackageBase.resolve("app");
        Path jpackageBackendJar = jpackageAppDir.resolve(BACKEND_JAR);
        if (Files.isRegularFile(jpackageBackendJar)) {
            return new RuntimePaths(jpackageBase, javaExecutable, jpackageBackendJar);
        }

        Path devBackendJar = Path.of("..", "sge-backend", "target", BACKEND_JAR).normalize().toAbsolutePath();
        if (Files.isRegularFile(devBackendJar)) {
            return new RuntimePaths(devBackendJar.getParent().getParent().getParent(), javaExecutable, devBackendJar);
        }

        Path cwdBackendJar = Path.of("sge-backend", "target", BACKEND_JAR).normalize().toAbsolutePath();
        if (Files.isRegularFile(cwdBackendJar)) {
            return new RuntimePaths(Path.of("").toAbsolutePath(), javaExecutable, cwdBackendJar);
        }

        throw new IOException("JAR do backend nao encontrado: " + BACKEND_JAR);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private record RuntimePaths(Path workingDirectory, Path javaExecutable, Path backendJar) {}
}
