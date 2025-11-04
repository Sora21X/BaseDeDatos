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
    private Rectangle registerButton = new Rectangle(425, 500, 350, 80);
    private Rectangle confirmButton = new Rectangle(550, 480, 180, 60);
    private Rectangle userBox = new Rectangle(480, 260, 520, 50);
    private Rectangle passBox = new Rectangle(480, 340, 520, 50);
    private Rectangle easyButton = new Rectangle(270, 250, 200, 80);
    private Rectangle mediumButton = new Rectangle(520, 250, 200, 80);
    private Rectangle hardButton = new Rectangle(770, 250, 200, 80);
    private Rectangle[] answerButtons = {
            new Rectangle(150, 300, 500, 60),
            new Rectangle(150, 380, 500, 60),
            new Rectangle(700, 300, 500, 60),
            new Rectangle(700, 380, 500, 60)
    };
    private Rectangle finishButton = new Rectangle(350, 340, 200, 60);
    private Rectangle rankingButton = new Rectangle(650, 340, 200, 60);
    private Rectangle backButton = new Rectangle(20, 20, 150, 50);

    // ---------- Login / registro ----------
    private String usernameInput = "";
    private String passwordInput = "";
    private boolean typingUsername = true;
    private String currentUser = "";
    private boolean startHover = false;
    private boolean registerHover = false;
    private boolean backHover = false;
    private boolean confirmHover = false;
    // Botones de difucultad
    private boolean easyHover = false;
    private boolean mediumHover = false;
    private boolean hardHover = false;
    // Para los botones de respuestas en la pantalla de juego
    private boolean[] answerHover = new boolean[4];
    // Botones de la pantalla Game Over
    private boolean finishHover = false;
    private boolean rankingHover = false;

    // ---------- Juego ----------
    private int puntajeJugador = 0;
    private String currentLevel = ""; // "easy","medium","hard"
    private int currentQuestion = 0;
    private int vidas = 3;

    // ---------- Preguntas ----------
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
            background = new ImageIcon(
                    new URL("https://img.freepik.com/vector-premium/fondo-juegos-aplicaciones-moviles-bosque_273525-243.jpg "))
                    .getImage();
            capibaraImg = new ImageIcon(
                    new URL("https://static.vecteezy.com/system/resources/thumbnails/054/013/801/small/cute-cartoon-capybara-illustration-free-vector.jpg"))
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

                // ---------- Panel semitransparente detrás del título ----------
                g2d.setColor(new Color(0, 0, 0, 150)); // negro semitransparente
                g2d.fillRoundRect(60, 150, getWidth() - 120, 150, 25, 25);

                // ---------- Título con sombra ----------
                g2d.setFont(titleFont);
                g2d.setColor(Color.BLACK);
                g2d.drawString("¡Bienvenido a El Capibara Salesiano!", 92, 202); // sombra sutil
                g2d.setColor(Color.WHITE);
                g2d.drawString("¡Bienvenido a El Capibara Salesiano!", 90, 200);

                // ---------- Mensaje debajo del título ----------
                g2d.setFont(textFont);
                g2d.setColor(Color.WHITE);
                g2d.drawString("¡Presiona INICIAR para comenzar la aventura!", 115, 270);

                // ---------- Botones con sombra y efecto hover ----------
                // INICIAR
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(startButton.x + 3, startButton.y + 3, startButton.width, startButton.height, 15, 15);
                drawButton(g2d, startButton, "INICIAR", startHover ? new Color(255, 215, 0) : new Color(255, 165, 0));

                // REGISTRARSE
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(registerButton.x + 3, registerButton.y + 3, registerButton.width,
                        registerButton.height, 15, 15);
                drawButton(g2d, registerButton, "REGISTRARSE",
                        registerHover ? new Color(0, 255, 255) : new Color(0, 180, 255));
            }

            case "login" -> {

                // ---------- Panel semitransparente ----------
                g2d.setColor(new Color(0, 0, 0, 150)); // negro semitransparente
                g2d.fillRoundRect(150, 150, 950, 450, 25, 25);

                // ---------- Título con sombra ----------
                g2d.setFont(titleFont.deriveFont(40f));
                drawOutlinedText(g2d, "INICIO DE SESIÓN", 430, 220, Color.WHITE, Color.CYAN);

                // ---------- Etiquetas ----------
                g2d.setFont(textFont);
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawString("Nombre de jugador:", 190, 295);
                g2d.drawString("Contraseña:", 190, 375);

                // ---------- Cuadros de texto ----------
                // Usuario
                g2d.setColor(typingUsername ? Color.CYAN : Color.WHITE);
                g2d.drawRoundRect(userBox.x, userBox.y, userBox.width, userBox.height, 15, 15);
                drawTextBox(g2d, userBox, usernameInput, typingUsername);

                // Contraseña
                g2d.setColor(!typingUsername ? Color.CYAN : Color.WHITE);
                g2d.drawRoundRect(passBox.x, passBox.y, passBox.width, passBox.height, 15, 15);
                drawTextBox(g2d, passBox, "*".repeat(passwordInput.length()), !typingUsername);

                // ---------- Botones con hover ----------
                drawButton(g2d, confirmButton, "ENTRAR",
                        confirmHover ? new Color(80, 180, 250) : new Color(120, 200, 250));
                drawButton(g2d, backButton, "VOLVER", backHover ? new Color(250, 150, 150) : new Color(220, 180, 180));
            }

            case "register" -> {

                // ---------- Panel semitransparente ----------
                g2d.setColor(new Color(0, 0, 0, 150)); // negro semitransparente
                g2d.fillRoundRect(150, 150, 950, 450, 25, 25);

                // ---------- Título con sombra ----------
                g2d.setFont(titleFont.deriveFont(36f));
                drawOutlinedText(g2d, "REGISTRAR USUARIO", 460, 220, Color.WHITE, Color.CYAN);

                // ---------- Etiquetas ----------
                g2d.setFont(textFont);
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawString("Nombre de usuario:", 190, 295);
                g2d.drawString("Contraseña:", 190, 375);

                // ---------- Cuadros de texto ----------
                // Usuario
                if (typingUsername) {
                    g2d.setColor(new Color(80, 200, 255, 120));
                    g2d.fillRoundRect(userBox.x - 3, userBox.y - 3, userBox.width + 6, userBox.height + 6, 20, 20);
                }
                g2d.setColor(typingUsername ? Color.CYAN : Color.WHITE);
                g2d.drawRoundRect(userBox.x, userBox.y, userBox.width, userBox.height, 15, 15);
                drawTextBox(g2d, userBox, usernameInput, typingUsername);

                // Contraseña
                if (!typingUsername) {
                    g2d.setColor(new Color(80, 200, 255, 120));
                    g2d.fillRoundRect(passBox.x - 3, passBox.y - 3, passBox.width + 6, passBox.height + 6, 20, 20);
                }
                g2d.setColor(!typingUsername ? Color.CYAN : Color.WHITE);
                g2d.drawRoundRect(passBox.x, passBox.y, passBox.width, passBox.height, 15, 15);
                drawTextBox(g2d, passBox, "*".repeat(passwordInput.length()), !typingUsername);

                // ---------- Botones con hover y sombra ----------
                // Botón REGISTRAR
                g2d.setColor(new Color(0, 0, 0, 100)); // sombra
                g2d.fillRoundRect(confirmButton.x + 3, confirmButton.y + 3, confirmButton.width, confirmButton.height,
                        15, 15);
                drawButton(g2d, confirmButton, "REGISTRAR",
                        confirmHover ? new Color(80, 250, 200) : new Color(120, 250, 200));

                // Botón VOLVER
                g2d.setColor(new Color(0, 0, 0, 100)); // sombra
                g2d.fillRoundRect(backButton.x + 3, backButton.y + 3, backButton.width, backButton.height, 15, 15);
                drawButton(g2d, backButton, "VOLVER", backHover ? new Color(250, 150, 150) : new Color(220, 180, 180));
            }

            case "selectLevel" -> {
                // ---------- Panel semitransparente detrás de botones ----------
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRoundRect(150, 140, 900, 400, 25, 25);

                // ---------- Título con sombra ----------
                g2d.setFont(titleFont.deriveFont(36f));
                drawOutlinedText(g2d, "SELECCIONA NIVEL - Jugador: " + currentUser, 220, 200, Color.WHITE, Color.CYAN);

                // ---------- Botones con efecto hover y sombra ----------
                // FÁCIL

                g2d.setColor(new Color(0, 0, 0, 100)); // sombra
                g2d.fillRoundRect(easyButton.x + 3, easyButton.y + 3, easyButton.width, easyButton.height, 15, 15);
                drawButton(g2d, easyButton, "FÁCIL", easyHover ? new Color(140, 220, 140) : new Color(180, 240, 180));

                // MEDIO
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(mediumButton.x + 3, mediumButton.y + 3, mediumButton.width, mediumButton.height, 15,
                        15);
                drawButton(g2d, mediumButton, "MEDIO",
                        mediumHover ? new Color(200, 200, 100) : new Color(240, 230, 140));

                // DIFÍCIL
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(hardButton.x + 3, hardButton.y + 3, hardButton.width, hardButton.height, 15, 15);
                drawButton(g2d, hardButton, "DIFÍCIL", hardHover ? new Color(220, 120, 120) : new Color(250, 160, 160));

                // SALIR
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(backButton.x + 3, backButton.y + 3, backButton.width, backButton.height, 15, 15);
                drawButton(g2d, backButton, "SALIR", backHover ? new Color(250, 150, 150) : new Color(220, 180, 180));
            }

            case "playing" -> {
                String[][] preguntas = getPreguntasPorNivel();
                if (preguntas == null)
                    return;

                // ---------- Panel semitransparente detrás de nivel/vidas/puntaje ----------
                int panelX = 20;
                int panelY = 40;
                int panelWidth = getWidth() - 40;
                int panelHeight = 60;
                g2d.setColor(new Color(0, 0, 0, 180)); // negro semitransparente
                g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);

                // ---------- Título con sombra ----------
                g2d.setFont(titleFont.deriveFont(28f));
                drawOutlinedText(g2d,
                        "Nivel: " + currentLevel.toUpperCase() + "   Vidas: " + vidas + "   Puntaje: " + puntajeJugador,
                        30, 80, Color.WHITE, Color.CYAN);

                // ---------- Panel semitransparente detrás de las preguntas ----------
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRoundRect(30, 140, getWidth() - 60, getHeight() - 150, 25, 25);

                // ---------- Pregunta ----------
                g2d.setFont(textFont.deriveFont(28f));
                if (currentQuestion < preguntas.length) {
                    String[] q = preguntas[currentQuestion];
                    drawOutlinedText(g2d, "Pregunta " + (currentQuestion + 1) + ": " + q[0], 50, 210, Color.WHITE,
                            Color.CYAN);

                    // ---------- Botones de respuestas con efecto hover y sombra ----------
                    g2d.setFont(textFont);
                    for (int i = 0; i < 4; i++) {
                        g2d.setColor(new Color(0, 0, 0, 100));
                        g2d.fillRoundRect(answerButtons[i].x + 3, answerButtons[i].y + 3,
                                answerButtons[i].width, answerButtons[i].height, 15, 15);

                        Color baseColor = new Color(240, 240, 240);
                        if (answerHover[i])
                            baseColor = new Color(200, 200, 255);
                        drawButton(g2d, answerButtons[i], (i + 1) + ". " + q[i + 1], baseColor);
                    }
                } else {
                    drawOutlinedText(g2d, "No hay más preguntas.", 300, 300, Color.WHITE, Color.RED);
                }

                // ---------- Mensaje del Capibara ----------
                if (mostrarMensaje) {
                    g2d.setFont(textFont.deriveFont(22f));
                    drawOutlinedText(g2d, mensajeCapibara, 400, getHeight() - 80, Color.YELLOW, Color.BLACK);
                }
            }

            case "gameOver" -> {

                // ---------- Panel semitransparente grande detrás de todo ----------
                int panelX = 300;
                int panelY = 140;
                int panelWidth = 600;
                int panelHeight = 300;
                g2d.setColor(new Color(0, 0, 0, 180)); // negro semitransparente
                g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 25, 25);

                // ---------- Título con sombra ----------
                g2d.setFont(titleFont.deriveFont(36f));
                drawOutlinedText(g2d, "FIN DEL JUEGO", 460, 180, Color.WHITE, Color.CYAN);

                // ---------- Información del jugador ----------
                g2d.setFont(textFont);
                drawOutlinedText(g2d, "Jugador: " + currentUser, 480, 230, Color.WHITE, Color.CYAN);
                drawOutlinedText(g2d, "Nivel: " + currentLevel.toUpperCase(), 480, 270, Color.WHITE, Color.CYAN);
                drawOutlinedText(g2d, "Puntaje: " + puntajeJugador, 480, 310, Color.WHITE, Color.CYAN);

                // ---------- Botones con sombra y efecto hover ----------
                // JUGAR OTRA
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(finishButton.x + 3, finishButton.y + 3, finishButton.width, finishButton.height, 15,
                        15);
                drawButton(g2d, finishButton, "JUGAR OTRA",
                        finishHover ? new Color(140, 220, 140) : new Color(180, 240, 180));

                // VER RANKING
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(rankingButton.x + 3, rankingButton.y + 3, rankingButton.width, rankingButton.height,
                        15, 15);
                drawButton(g2d, rankingButton, "VER RANKING",
                        rankingHover ? new Color(80, 180, 250) : new Color(120, 200, 250));

                // SALIR
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(backButton.x + 3, backButton.y + 3, backButton.width, backButton.height, 15, 15);
                drawButton(g2d, backButton, "SALIR", backHover ? new Color(250, 150, 150) : new Color(220, 180, 180));
            }

            case "ranking" -> {

                // ---------- Panel semitransparente grande ----------
                int panelX = 80;
                int panelY = 60;
                int panelWidth = 1120;
                int panelHeight = 600;
                g2d.setColor(new Color(0, 0, 0, 180)); // negro semitransparente
                g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 25, 25);

                // Borde y sombra ligera
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 25, 25);

                // ---------- Título con sombra dentro del panel ----------
                g2d.setFont(titleFont.deriveFont(34f));
                drawOutlinedText(g2d, "RANKING - Nivel: " + currentLevel.toUpperCase(), 400, 110, Color.WHITE,
                        Color.CYAN);

                // ---------- Lista del ranking dentro del panel ----------
                java.util.List<String> list = obtenerRanking(currentLevel);
                g2d.setFont(textFont.deriveFont(22f));
                int y = 160;

                if (list.isEmpty()) {
                    g2d.setColor(Color.WHITE);
                    g2d.drawString("No hay registros aún para este nivel.", 150, y);
                } else {
                    int index = 1;
                    for (String s : list) {
                        // Tarjeta individual con sombra
                        g2d.setColor(new Color(245, 245, 245));
                        g2d.fillRoundRect(130, y - 28, 1020, 55, 18, 18);

                        g2d.setColor(new Color(0, 0, 0, 60));
                        g2d.drawRoundRect(130, y - 28, 1020, 55, 18, 18);

                        // Número de posición
                        g2d.setColor(new Color(60, 80, 180));
                        g2d.setFont(textFont.deriveFont(24f));
                        g2d.drawString("#" + index, 150, y);

                        // Información del jugador
                        g2d.setColor(Color.BLACK);
                        g2d.setFont(textFont.deriveFont(20f));
                        g2d.drawString(s, 220, y);

                        y += 65;
                        index++;
                    }
                }

                // ---------- Botón volver con sombra y hover ----------
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(backButton.x + 3, backButton.y + 3, backButton.width, backButton.height, 15, 15);
                drawButton(g2d, backButton, "VOLVER", backHover ? new Color(250, 150, 150) : new Color(220, 180, 180));
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