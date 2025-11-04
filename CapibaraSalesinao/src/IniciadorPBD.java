import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class IniciadorPBD {

    public static void main(String[] args) {

        // Crear ventana
        JWindow splash = new JWindow();
        splash.setSize(900, 250);
        splash.setLocationRelativeTo(null);
        splash.setBackground(Color.BLACK); // Fondo negro

        // ASCII Art
        String asciiArt = """
                ██████╗░██╗███████╗███╗░░██╗██╗░░░██╗███████╗███╗░░██╗██╗██████╗░░█████╗░░██████╗
                ██╔══██╗██║██╔════╝████╗░██║██║░░░██║██╔════╝████╗░██║██║██╔══██╗██╔══██╗██╔════╝
                ██████╦╝██║█████╗░░██╔██╗██║╚██╗░██╔╝█████╗░░██╔██╗██║██║██║░░██║██║░░██║╚█████╗░
                ██╔══██╗██║██╔══╝░░██║╚████║░╚████╔╝░██╔══╝░░██║╚████║██║██║░░██║██║░░██║░╚═══██╗
                ██████╦╝██║███████╗██║░╚███║░░╚██╔╝░░███████╗██║░╚███║██║██████╔╝╚█████╔╝██████╔╝
                ╚═════╝░╚═╝╚══════╝╚═╝░░╚══╝░░░╚═╝░░░╚══════╝╚═╝░░╚══╝╚═╝╚═════╝░░╚════╝░╚═════╝░
                """;

        JTextArea ascii = new JTextArea(asciiArt);
        ascii.setEditable(false);
        ascii.setOpaque(false);
        ascii.setForeground(Color.WHITE);
        ascii.setFont(new Font("Consolas", Font.BOLD, 18));

        // Barra de carga
        JTextArea loadingBar = new JTextArea("");
        loadingBar.setEditable(false);
        loadingBar.setOpaque(false);
        loadingBar.setForeground(Color.WHITE);
        loadingBar.setFont(new Font("Consolas", Font.BOLD, 25));

        // Panel principal con margen de 1 cm
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(48, 38, 38, 38)); // margen de 1 cm
        panel.add(ascii);
        panel.add(Box.createVerticalStrut(10));
        panel.add(loadingBar);

        splash.add(panel);
        splash.setVisible(true);

        // Animación de carga
        final int totalBlocks = 60;
        final int duration = 2000; // 2 segundos
        final int delay = duration / totalBlocks;

        Timer loadTimer = new Timer(delay, null);
        loadTimer.addActionListener(e -> {
            loadingBar.append("█");
            if (loadingBar.getText().length() >= totalBlocks) {
                loadTimer.stop();
                splash.dispose();
                abrirVentanaPrincipal();
            }
        });
        loadTimer.start();
    }

    private static void abrirVentanaPrincipal() {
        JFrame frame = new JFrame("El Capibara Salesiano");
        ElCapibaraSalesiano game = new ElCapibaraSalesiano();
        frame.add(game);
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        game.requestFocusInWindow();
    }
}
