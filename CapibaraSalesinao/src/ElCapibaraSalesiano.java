import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;

public class ElCapibaraSalesiano extends JPanel implements KeyListener, MouseListener {

    // ---------- Fuentes y estado ----------
    private String gameState = "welcome"; // welcome, login, register, selectLevel, playing, gameOver, ranking
    private Font titleFont = new Font("Monospaced", Font.BOLD, 50);
    private Font textFont = new Font("Monospaced", Font.BOLD, 26);

    // ---------- Botones y áreas ----------
    private Rectangle startButton = new Rectangle(450, 400, 300, 80);
    private Rectangle registerButton = new Rectangle(450, 500, 300, 80);
    private Rectangle confirmButton = new Rectangle(600, 460, 180, 60);
    private Rectangle userBox = new Rectangle(480, 260, 400, 50);
    private Rectangle passBox = new Rectangle(480, 340, 400, 50);
    private Rectangle easyButton = new Rectangle(300, 250, 200, 80);
    private Rectangle mediumButton = new Rectangle(550, 250, 200, 80);
    private Rectangle hardButton = new Rectangle(800, 250, 200, 80);
    private Rectangle[] answerButtons = {
            new Rectangle(150, 400, 450, 60),
            new Rectangle(150, 480, 450, 60),
            new Rectangle(700, 400, 450, 60),
            new Rectangle(700, 480, 450, 60)
    };
    private Rectangle finishButton = new Rectangle(400, 400, 200, 60);
    private Rectangle rankingButton = new Rectangle(700, 400, 200, 60);
    private Rectangle backButton = new Rectangle(20, 20, 150, 50);

    // ---------- Login / registro ----------
    private String usernameInput = "";
    private String passwordInput = "";
    private boolean typingUsername = true;
    private String currentUser = "";

    // ---------- Juego ----------
    private int puntajeJugador = 0;
    private String currentLevel = ""; // "easy","medium","hard"
    private int currentQuestion = 0;
    private int vidas = 3;

    // ---------- Preguntas (las mismas que tenías) ----------
    private String[][] questionsEasy = {
            { "¿Quién fue tragado por un gran pez?", "Jonás", "Pedro", "Pablo", "Isaías" },
            { "¿Quién liberó al pueblo de Israel de Egipto?", "Moisés", "David", "Salomón", "Abraham" },
            { "¿Qué animal habló en la Biblia?", "Perro", "Gato", "Burro", "León" },
            { "¿Quién construyó el arca?", "Moisés", "Noé", "Abraham", "Adán" },
            { "¿Quién fue el primer hombre?", "Adán", "Caín", "Moisés", "Noé" }
    };
    private int[] correctEasy = { 0, 0, 2, 1, 0 };

    private String[][] questionsMedium = {
            { "¿Qué es la teoría de la probabilidad?", "Organizar datos", "Formaliza la incertidumbre",
                    "Experimento determinístico", "N/A" },
            { "Un experimento es aleatorio cuando...", "Resultado determinado", "No se puede predecir con exactitud",
                    "No se puede repetir", "N/A" },
            { "¿Cómo se llama el conjunto de todos los resultados posibles?", "Evento seguro", "Espacio muestral (Ω)",
                    "Evento simple", "N/A" },
            { "Lanzar una moneda hasta cara produce un espacio...", "Discreto infinito", "Continuo finito",
                    "Discreto finito", "N/A" },
            { "La intersección A∩B consiste en...", "A o B o ambos", "Comunes a A y B", "No están en A", "N/A" }
    };
    private int[] correctMedium = { 1, 1, 1, 0, 1 };

    private String[][] questionsHard = {
            { "¿Dónde está 'El Señor es mi pastor'?", "Proverbios", "Salmos", "Juan", "Hebreos" },
            { "¿Quién escribió la mayoría de cartas del NT?", "Pedro", "Juan", "Pablo", "Santiago" },
            { "Qué profeta confrontó al rey Acab?", "Elías", "Isaías", "Jeremías", "Oseas" },
            { "Último libro del AT?", "Malaquías", "Zacarías", "Sofonías", "Ageo" },
            { "Cuál discípulo fue 'el gemelo'?", "Pedro", "Juan", "Tomás", "Mateo" }
    };
    private int[] correctHard = { 1, 2, 0, 0, 2 };

    // ---------- Imágenes y animación ----------
    private Image background;
    private Image capibaraImg;
    private int backgroundX = 0;
    private int backgroundSpeed = 2;
    private Timer backgroundTimer;

    // ---------- Mensajes del capibara ----------
    private String[] insultosCapibara = {
            "¡Ay, ni mi abuela fallaba eso!",
            "¿En serio? ¡Eso era fácil!",
            "¡Capibara decepcionado!",
            "Intenta leer la Biblia, por favor.",
            "¡Esa dolió en el alma capibaril!",
            "¿Eso fue en serio o una broma?",
            "¡Hasta un pez sabe esa respuesta!"
    };
    private String[] motivacionesCapibara = {
            "¡Eso sí, crack!",
            "¡Eres un genio bíblico!",
            "¡Capibara orgulloso!",
            "¡Bien hecho!",
            "¡Sigue así!"
    };
    private String mensajeCapibara = "";
    private long tiempoMensaje = 0L;

    // ---------- Constructor ----------
    public ElCapibaraSalesiano() {
        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true);
        requestFocusInWindow();

        try {
            background = new ImageIcon(new URL(
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQffbE-29FZtlCdYMxV1QAXvpeLKjx71MuvcA&s"))
                    .getImage();
            capibaraImg = new ImageIcon(
                    new URL("https://ih1.redbubble.net/image.4112491081.9580/st,small,507x507-pad,600x600,f8f8f8.jpg"))
                    .getImage();
        } catch (Exception e) {
            System.out.println("Error al cargar imágenes: " + e.getMessage());
        }

        backgroundTimer = new Timer(30, e -> {
            backgroundX -= backgroundSpeed;
            if (backgroundX <= -getWidth())
                backgroundX = 0;
            repaint();
        });
        backgroundTimer.start();
    }

    // ========================= INPUT =========================
    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (gameState.equals("login") || gameState.equals("register")) {
            if (typingUsername) {
                if (Character.isLetterOrDigit(c) || c == ' ' || "áéíóúÁÉÍÓÚñÑ".indexOf(c) >= 0) {
                    usernameInput += c;
                    usernameInput = corregirNombreAvanzado(usernameInput);
                } else if (c == '\b' && usernameInput.length() > 0) {
                    usernameInput = usernameInput.substring(0, usernameInput.length() - 1);
                    usernameInput = corregirNombreAvanzado(usernameInput);
                }
            } else {
                if (c == '\b' && passwordInput.length() > 0)
                    passwordInput = passwordInput.substring(0, passwordInput.length() - 1);
                else if (!Character.isISOControl(c))
                    passwordInput += c;
            }
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    // ========================= MOUSE (flujo del juego) =========================
    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();

        switch (gameState) {
            case "welcome" -> {
                if (startButton.contains(p)) {
                    gameState = "login";
                    usernameInput = "";
                    passwordInput = "";
                    typingUsername = true;
                } else if (registerButton.contains(p)) {
                    gameState = "register";
                    usernameInput = "";
                    passwordInput = "";
                    typingUsername = true;
                }
            }

            case "login" -> {
                if (userBox.contains(p))
                    typingUsername = true;
                else if (passBox.contains(p))
                    typingUsername = false;
                else if (confirmButton.contains(p)) {
                    if (verificarUsuarioBD(usernameInput, passwordInput)) {
                        currentUser = corregirNombreAvanzado(usernameInput);
                        usernameInput = "";
                        passwordInput = "";
                        puntajeJugador = 0;
                        vidas = 3;
                        currentQuestion = 0;
                        gameState = "selectLevel";
                    } else {
                        JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos");
                    }
                } else if (backButton.contains(p)) {
                    gameState = "welcome";
                }
            }

            case "register" -> {
                if (userBox.contains(p))
                    typingUsername = true;
                else if (passBox.contains(p))
                    typingUsername = false;
                else if (confirmButton.contains(p)) {
                    if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Completa nombre y contraseña");
                    } else {
                        if (guardarUsuarioBD(usernameInput, passwordInput)) {
                            JOptionPane.showMessageDialog(this, "Registro exitoso. Inicia sesión.");
                            usernameInput = "";
                            passwordInput = "";
                            gameState = "login";
                        } else {
                            JOptionPane.showMessageDialog(this, "El usuario ya existe o hubo un error.");
                        }
                    }
                } else if (backButton.contains(p)) {
                    gameState = "welcome";
                }
            }

            case "selectLevel" -> {
                if (easyButton.contains(p))
                    startGame("easy");
                else if (mediumButton.contains(p))
                    startGame("medium");
                else if (hardButton.contains(p))
                    startGame("hard");
                else if (backButton.contains(p))
                    gameState = "welcome";
            }

            case "playing" -> {
                // respuestas
                for (int i = 0; i < answerButtons.length; i++) {
                    if (answerButtons[i].contains(p)) {
                        manejarRespuesta(i);
                        break;
                    }
                }
            }

            case "gameOver" -> {
                if (finishButton.contains(p)) {
                    // volver a seleccionar nivel
                    gameState = "selectLevel";
                } else if (rankingButton.contains(p)) {
                    gameState = "ranking";
                } else if (backButton.contains(p)) {
                    gameState = "welcome";
                }
            }

            case "ranking" -> {
                if (backButton.contains(p)) {
                    gameState = "selectLevel";
                }
            }
        }

        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    // ========================= LÓGICA DEL JUEGO =========================
    private void startGame(String nivel) {
        currentLevel = nivel;
        puntajeJugador = 0;
        vidas = 3;
        currentQuestion = 0;
        mensajeCapibara = "";
        tiempoMensaje = 0;
        gameState = "playing";
    }

    private void manejarRespuesta(int indiceRespuesta) {
        String[][] preguntas = getPreguntasPorNivel();
        int[] correct = getCorrectArrayPorNivel();

        if (preguntas == null || currentQuestion >= preguntas.length)
            return;

        int correcto = correct[currentQuestion];
        if (indiceRespuesta == correcto) {
            puntajeJugador += 10;
            mensajeCapibara = motivacionesCapibara[(int) (Math.random() * motivacionesCapibara.length)];
        } else {
            vidas--;
            mensajeCapibara = insultosCapibara[(int) (Math.random() * insultosCapibara.length)];
        }
        tiempoMensaje = System.currentTimeMillis();

        currentQuestion++;
        // terminar si se acabaron preguntas o vidas
        if (vidas <= 0 || currentQuestion >= preguntas.length) {
            // guardar puntaje en BD
            if (currentUser == null || currentUser.isEmpty())
                currentUser = "Invitado";
            guardarPuntaje(currentUser, puntajeJugador, currentLevel);
            gameState = "gameOver";
        }
    }

    private String[][] getPreguntasPorNivel() {
        return switch (currentLevel) {
            case "easy" -> questionsEasy;
            case "medium" -> questionsMedium;
            case "hard" -> questionsHard;
            default -> null;
        };
    }

    private int[] getCorrectArrayPorNivel() {
        return switch (currentLevel) {
            case "easy" -> correctEasy;
            case "medium" -> correctMedium;
            case "hard" -> correctHard;
            default -> null;
        };
    }

    // ========================= BD: registrar usuario / login / registro (ranking)
    // =========================
    private String hashPassword(String input) {
        if (input == null)
            return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean guardarUsuarioBD(String nombre, String pass) {
        String sqlCheck = "SELECT * FROM Usuarios WHERE nombre = ?";
        String sqlInsert = "INSERT INTO Usuarios (nombre, passwordHash) VALUES (?, ?)";
        try (Connection cn = ConexionBD.conectar()) {
            if (cn == null)
                return false;
            PreparedStatement psCheck = cn.prepareStatement(sqlCheck);
            psCheck.setString(1, nombre);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next())
                return false;
            PreparedStatement psInsert = cn.prepareStatement(sqlInsert);
            psInsert.setString(1, nombre);
            psInsert.setString(2, hashPassword(pass));
            psInsert.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean verificarUsuarioBD(String nombre, String pass) {
        String sql = "SELECT passwordHash FROM Usuarios WHERE nombre = ?";
        try (Connection cn = ConexionBD.conectar()) {
            if (cn == null)
                return false;
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashBD = rs.getString("passwordHash");
                return hashBD.equals(hashPassword(pass));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void guardarPuntaje(String usuario, int puntaje, String nivel) {
        String sql = "INSERT INTO Registro (usuario, nivel, puntaje) VALUES (?, ?, ?)";
        try (Connection cn = ConexionBD.conectar()) {
            if (cn == null)
                return;
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, usuario);
            ps.setString(2, nivel);
            ps.setInt(3, puntaje);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private java.util.List<String> obtenerRanking(String nivel) {
        java.util.List<String> lista = new ArrayList<>();
        String sql = "SELECT TOP 10 usuario, puntaje, fecha FROM Registro WHERE nivel = ? ORDER BY puntaje DESC, fecha ASC";
        try (Connection cn = ConexionBD.conectar()) {
            if (cn == null)
                return lista;
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, nivel);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("usuario") + " - " + rs.getInt("puntaje") + " pts (" + rs.getTimestamp("fecha")
                        + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ========================= Nombres: capitalizar =========================
    private String corregirNombreAvanzado(String input) {
        if (input == null || input.isEmpty())
            return "";
        input = input.trim().replaceAll("\\s+", " ");
        StringBuilder nombreCorregido = new StringBuilder();
        for (String palabra : input.split(" ")) {
            if (!palabra.isEmpty()) {
                nombreCorregido.append(palabra.substring(0, 1).toUpperCase())
                        .append(palabra.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return nombreCorregido.toString().trim();
    }

    // ========================= DIBUJO =========================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // fondo movible
        if (background != null) {
            int imgWidth = getWidth();
            int imgHeight = getHeight();
            g2d.drawImage(background, backgroundX, 0, imgWidth, imgHeight, this);
            g2d.drawImage(background, backgroundX + imgWidth, 0, imgWidth, imgHeight, this);
        } else {
            g2d.setColor(new Color(200, 230, 255));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        g2d.setFont(textFont);
        g2d.setColor(Color.BLACK);

        // mostrar mensajes del capibara si hay
        long now = System.currentTimeMillis();
        boolean mostrarMensaje = (mensajeCapibara != null && !mensajeCapibara.isEmpty()
                && (now - tiempoMensaje) < 2000);

        switch (gameState) {
            case "welcome" -> {
                g2d.setFont(titleFont);
                drawOutlinedText(g2d, "¡Bienvenido a El Capibara Salesiano!", 150, 200, Color.WHITE, Color.BLACK);
                drawButton(g2d, startButton, "INICIAR", new Color(150, 200, 100));
                drawButton(g2d, registerButton, "REGISTRARSE", new Color(100, 180, 250));
            }

            case "login" -> {
                g2d.setFont(titleFont.deriveFont(40f));
                drawOutlinedText(g2d, "INICIO DE SESIÓN", 420, 180, Color.WHITE, Color.BLACK);
                g2d.setFont(textFont);
                g2d.drawString("Nombre de jugador:", 230, 295);
                drawTextBox(g2d, userBox, usernameInput, typingUsername);
                g2d.drawString("Contraseña:", 230, 375);
                drawTextBox(g2d, passBox, "*".repeat(passwordInput.length()), !typingUsername);
                drawButton(g2d, confirmButton, "ENTRAR", new Color(120, 200, 250));
                drawButton(g2d, backButton, "VOLVER", new Color(220, 180, 180));
            }

            case "register" -> {
                g2d.setFont(titleFont.deriveFont(36f));
                drawOutlinedText(g2d, "REGISTRO NUEVO USUARIO", 260, 180, Color.WHITE, Color.BLACK);
                g2d.setFont(textFont);
                g2d.drawString("Nombre de usuario:", 230, 295);
                drawTextBox(g2d, userBox, usernameInput, typingUsername);
                g2d.drawString("Contraseña:", 230, 375);
                drawTextBox(g2d, passBox, "*".repeat(passwordInput.length()), !typingUsername);
                drawButton(g2d, confirmButton, "REGISTRAR", new Color(120, 250, 200));
                drawButton(g2d, backButton, "VOLVER", new Color(220, 180, 180));
            }

            case "selectLevel" -> {
                g2d.setFont(titleFont.deriveFont(36f));
                drawOutlinedText(g2d, "SELECCIONA NIVEL - Jugador: " + currentUser, 120, 160, Color.WHITE, Color.BLACK);
                drawButton(g2d, easyButton, "FÁCIL", new Color(180, 240, 180));
                drawButton(g2d, mediumButton, "MEDIO", new Color(240, 230, 140));
                drawButton(g2d, hardButton, "DIFÍCIL", new Color(250, 160, 160));
                drawButton(g2d, backButton, "SALIR", new Color(220, 180, 180));
            }

            case "playing" -> {
                String[][] preguntas = getPreguntasPorNivel();
                if (preguntas == null)
                    return;
                g2d.setFont(titleFont.deriveFont(28f));
                drawOutlinedText(g2d,
                        "Nivel: " + currentLevel.toUpperCase() + "   Vidas: " + vidas + "   Puntaje: " + puntajeJugador,
                        30, 60, Color.WHITE, Color.BLACK);

                g2d.setFont(textFont.deriveFont(28f));
                if (currentQuestion < preguntas.length) {
                    String[] q = preguntas[currentQuestion];
                    drawOutlinedText(g2d, "Pregunta " + (currentQuestion + 1) + ": " + q[0], 50, 150, Color.WHITE,
                            Color.BLACK);

                    g2d.setFont(textFont);
                    for (int i = 0; i < 4; i++) {
                        drawButton(g2d, answerButtons[i], (i + 1) + ". " + q[i + 1], new Color(240, 240, 240));
                    }
                } else {
                    drawOutlinedText(g2d, "No hay más preguntas.", 300, 300, Color.WHITE, Color.BLACK);
                }

                if (mostrarMensaje) {
                    g2d.setFont(textFont.deriveFont(22f));
                    drawOutlinedText(g2d, mensajeCapibara, 300, getHeight() - 80, Color.YELLOW, Color.BLACK);
                }
            }

            case "gameOver" -> {
                g2d.setFont(titleFont.deriveFont(36f));
                drawOutlinedText(g2d, "FIN DEL JUEGO", 460, 160, Color.WHITE, Color.BLACK);
                g2d.setFont(textFont);
                drawOutlinedText(g2d, "Jugador: " + currentUser, 480, 230, Color.WHITE, Color.BLACK);
                drawOutlinedText(g2d, "Nivel: " + currentLevel.toUpperCase(), 480, 270, Color.WHITE, Color.BLACK);
                drawOutlinedText(g2d, "Puntaje: " + puntajeJugador, 480, 310, Color.WHITE, Color.BLACK);

                drawButton(g2d, finishButton, "JUGAR OTRA", new Color(180, 240, 180));
                drawButton(g2d, rankingButton, "VER RANKING", new Color(120, 200, 250));
                drawButton(g2d, backButton, "SALIR", new Color(220, 180, 180));
            }

            case "ranking" -> {
                g2d.setFont(titleFont.deriveFont(30f));
                drawOutlinedText(g2d, "RANKING - Nivel: " + currentLevel.toUpperCase(), 260, 100, Color.WHITE,
                        Color.BLACK);

                java.util.List<String> list = obtenerRanking(currentLevel);
                g2d.setFont(textFont.deriveFont(20f));
                int y = 160;
                if (list.isEmpty()) {
                    g2d.drawString("No hay registros aún para este nivel.", 220, y);
                } else {
                    for (String s : list) {
                        g2d.drawString(s, 120, y);
                        y += 36;
                    }
                }
                drawButton(g2d, backButton, "VOLVER", new Color(220, 180, 180));
            }
        }

        // dibujar capibara pequeña en esquina
        if (capibaraImg != null) {
            g2d.drawImage(capibaraImg, getWidth() - 140, getHeight() - 140, 120, 120, this);
        }
    }

    // ===== helpers gráficos =====
    private void drawButton(Graphics2D g2d, Rectangle rect, String text, Color color) {
        g2d.setColor(color);
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2d.drawString(text, rect.x + (rect.width - textWidth) / 2, rect.y + (rect.height + textHeight) / 2 - 5);
    }

    private void drawTextBox(Graphics2D g2d, Rectangle rect, String text, boolean selected) {
        g2d.setColor(selected ? new Color(200, 230, 255) : new Color(240, 240, 240));
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
        g2d.drawString(text, rect.x + 10, rect.y + 35);
    }

    private void drawOutlinedText(Graphics2D g2d, String text, int x, int y, Color fill, Color outline) {
        g2d.setColor(outline);
        g2d.drawString(text, x - 1, y);
        g2d.drawString(text, x + 1, y);
        g2d.drawString(text, x, y - 1);
        g2d.drawString(text, x, y + 1);
        g2d.setColor(fill);
        g2d.drawString(text, x, y);
    }

    // ========================= MAIN =========================
    public static void main(String[] args) {
        JFrame frame = new JFrame("El Capibara Salesiano");
        ElCapibaraSalesiano game = new ElCapibaraSalesiano();
        frame.add(game);
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
