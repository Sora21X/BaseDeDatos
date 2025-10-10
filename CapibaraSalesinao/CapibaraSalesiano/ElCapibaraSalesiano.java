import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class ElCapibaraSalesiano extends JPanel implements KeyListener, MouseListener {
    private String gameState = "welcome";
    private Font titleFont = new Font("Serif", Font.BOLD, 50);
    private Font textFont = new Font("Serif", Font.PLAIN, 28);

    // Botones
    private Rectangle startButton = new Rectangle(450, 400, 300, 80);
    private Rectangle confirmButton = new Rectangle(600, 460, 180, 60);
    private Rectangle userBox = new Rectangle(480, 260, 400, 50);
    private Rectangle passBox = new Rectangle(480, 340, 400, 50);
    private Rectangle easyButton = new Rectangle(300, 250, 200, 80);
    private Rectangle mediumButton = new Rectangle(550, 250, 200, 80);
    private Rectangle hardButton = new Rectangle(800, 250, 200, 80);
    private Rectangle[] answerButtons = {
            new Rectangle(150, 400, 400, 60),
            new Rectangle(150, 500, 400, 60),
            new Rectangle(650, 400, 400, 60),
            new Rectangle(650, 500, 400, 60)
    };
    private Rectangle finishButton = new Rectangle(400, 400, 200, 60);
    private Rectangle rankingButton = new Rectangle(700, 400, 200, 60);
    private Rectangle backButton = new Rectangle(20, 20, 150, 50);

    // Login
    private String usernameInput = "";
    private String passwordInput = "";
    private boolean typingUsername = true;
    private final String correctPassword = "4321";

    // Juego
    private int puntajeJugador = 0;
    private String currentLevel = "";
    private int currentQuestion = 0;
    private int vidas = 3;

    // Preguntas
    private String[][] questionsEasy = {
            {"¿Quién fue tragado por un gran pez?", "Jonás", "Pedro", "Pablo", "Isaías"},
            {"¿Quién liberó al pueblo de Israel de Egipto?", "Moisés", "David", "Salomón", "Abraham"},
            {"¿Qué animal habló en la Biblia?", "Perro", "Gato", "Burro", "León"},
            {"¿Quién construyó el arca?", "Moisés", "Noé", "Abraham", "Adán"},
            {"¿Quién fue el primer hombre?", "Adán", "Caín", "Moisés", "Noé"}
    };
    private int[] correctEasy = {0, 0, 2, 1, 0};

    private String[][] questionsMedium = {
            {"¿Qué es la teoría de la probabilidad?", "Organizar datos", "Formaliza la incertidumbre", "Experimento determinístico", "N/A"},
            {"Un experimento es aleatorio cuando...", "Resultado determinado", "No se puede predecir con exactitud", "No se puede repetir", "N/A"},
            {"¿Cómo se llama el conjunto de todos los resultados posibles?", "Evento seguro", "Espacio muestral (Ω)", "Evento simple", "N/A"},
            {"Lanzar una moneda hasta cara produce un espacio...", "Discreto infinito", "Continuo finito", "Discreto finito", "N/A"},
            {"La intersección A∩B consiste en...", "A o B o ambos", "Comunes a A y B", "No están en A", "N/A"}
    };
    private int[] correctMedium = {1, 1, 1, 0, 1};

    private String[][] questionsHard = {
            {"¿Dónde está 'El Señor es mi pastor'?", "Proverbios", "Salmos", "Juan", "Hebreos"},
            {"¿Quién escribió la mayoría de cartas del NT?", "Pedro", "Juan", "Pablo", "Santiago"},
            {"¿Qué profeta confrontó al rey Acab?", "Elías", "Isaías", "Jeremías", "Oseas"},
            {"Último libro del AT?", "Malaquías", "Zacarías", "Sofonías", "Ageo"},
            {"¿Cuál discípulo fue 'el gemelo'?", "Pedro", "Juan", "Tomás", "Mateo"}
    };
    private int[] correctHard = {1, 2, 0, 0, 2};

    private ArrayList<Jugador> rankingEasy = new ArrayList<>();
    private ArrayList<Jugador> rankingMedium = new ArrayList<>();
    private ArrayList<Jugador> rankingHard = new ArrayList<>();

    private Image background;
    private int backgroundX = 0;
    private int backgroundSpeed = 2;
    private Timer backgroundTimer;

    public ElCapibaraSalesiano() {
        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true);
        background = new ImageIcon("MiProyectoresources/paisaje.png").getImage();

        backgroundTimer = new Timer(30, e -> {
            backgroundX -= backgroundSpeed;
            if (background != null && backgroundX <= -background.getWidth(null)) {
                backgroundX = 0;
            }
            repaint();
        });
        backgroundTimer.start();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (gameState.equals("login")) {
            if (typingUsername) {
                if (Character.isLetterOrDigit(c) || c == ' ')
                    usernameInput += c;
                else if (c == '\b' && usernameInput.length() > 0)
                    usernameInput = usernameInput.substring(0, usernameInput.length() - 1);
            } else {
                if (Character.isDigit(c))
                    passwordInput += c;
                else if (c == '\b' && passwordInput.length() > 0)
                    passwordInput = passwordInput.substring(0, passwordInput.length() - 1);
            }
        }
        repaint();
    }

    @Override public void keyPressed(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();

        if (gameState.equals("welcome") && startButton.contains(p))
            gameState = "login";

        else if (gameState.equals("login")) {
            if (userBox.contains(p)) typingUsername = true;
            else if (passBox.contains(p)) typingUsername = false;
            else if (confirmButton.contains(p)) {
                if (passwordInput.equals(correctPassword) && !usernameInput.isEmpty()) {
                    puntajeJugador = 0;
                    vidas = 3;
                    gameState = "selectLevel";
                } else {
                    JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos");
                }
            }
        }

        else if (gameState.equals("selectLevel")) {
            if (easyButton.contains(p)) { currentLevel = "facil"; currentQuestion = 0; gameState = "playing"; }
            else if (mediumButton.contains(p)) { currentLevel = "medio"; currentQuestion = 0; gameState = "playing"; }
            else if (hardButton.contains(p)) { currentLevel = "dificil"; currentQuestion = 0; gameState = "playing"; }
        }

        else if (gameState.equals("playing")) {
            int[] correctAnswers = getCurrentCorrectAnswers();
            for (int i = 0; i < 4; i++) {
                if (answerButtons[i].contains(p)) {
                    if (i == correctAnswers[currentQuestion]) {
                        puntajeJugador += 10;
                    } else {
                        vidas--;
                        if (vidas == 0) {
                            gameState = "gameOver";
                            break;
                        }
                    }

                    if (currentQuestion < getCurrentQuestions().length - 1 && vidas > 0) {
                        currentQuestion++;
                    } else if (vidas > 0) {
                        getCurrentRanking().add(new Jugador(usernameInput, puntajeJugador));
                        guardarRegistro(usernameInput, puntajeJugador, currentLevel);
                        gameState = "gameOver";
                    }
                    break;
                }
            }
        }

        else if (gameState.equals("gameOver")) {
            if (finishButton.contains(p)) {
                usernameInput = "";
                passwordInput = "";
                gameState = "login";
            } else if (rankingButton.contains(p)) {
                gameState = "ranking";
            }
        }

        else if (gameState.equals("ranking") && backButton.contains(p)) {
            gameState = "gameOver";
        }

        repaint();
    }

    // Métodos vacíos
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // ========================= GUARDAR =========================
    private void guardarRegistro(String nombre, int puntaje, String nivel) {
        try (FileWriter fw = new FileWriter("registros.txt", true)) {
            fw.write("Jugador: " + nombre + " | Puntaje: " + puntaje + " | Nivel: " + nivel + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ========================= DIBUJO =========================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (background != null) {
            g2d.drawImage(background, backgroundX, 0, getWidth(), getHeight(), this);
            g2d.drawImage(background, backgroundX + getWidth(), 0, getWidth(), getHeight(), this);
        }

        g2d.setFont(textFont);
        g2d.setColor(Color.BLACK);

        switch (gameState) {
            case "welcome" -> {
                g2d.setFont(titleFont);
                g2d.drawString("¡Bienvenido a El Capibara Salesiano!", 220, 300);
                drawButton(g2d, startButton, "Iniciar", new Color(150, 200, 100));
            }

            case "login" -> {
                g2d.setFont(titleFont.deriveFont(40f));
                g2d.drawString("Inicio de Sesión", 460, 180);
                g2d.setFont(textFont);
                g2d.drawString("Nombre de jugador:", 230, 295);
                drawTextBox(g2d, userBox, usernameInput, typingUsername);
                g2d.drawString("Contraseña:", 230, 375);
                drawTextBox(g2d, passBox, "*".repeat(passwordInput.length()), !typingUsername);
                drawButton(g2d, confirmButton, "Entrar", new Color(120, 200, 250));
            }

            case "selectLevel" -> {
                g2d.drawString("Seleccione el nivel:", 500, 180);
                drawButton(g2d, easyButton, "Fácil", new Color(100, 200, 150));
                drawButton(g2d, mediumButton, "Medio", new Color(255, 200, 100));
                drawButton(g2d, hardButton, "Difícil", new Color(200, 100, 150));
            }

            case "playing" -> {
                String[][] qs = getCurrentQuestions();
                g2d.setFont(textFont.deriveFont(28f));
                g2d.drawString("Pregunta " + (currentQuestion + 1) + ":", 100, 150);
                g2d.drawString(qs[currentQuestion][0], 100, 200);
                Color[] colores = {new Color(220, 250, 255), new Color(255, 245, 220), new Color(230, 255, 230), new Color(255, 230, 240)};
                for (int i = 0; i < 4; i++) {
                    drawButton(g2d, answerButtons[i], qs[currentQuestion][i + 1], colores[i]);
                }
                g2d.drawString("Puntaje: " + puntajeJugador, 1050, 100);
                g2d.drawString("Vidas: " + vidas, 1050, 140);
            }

            case "gameOver" -> {
                g2d.setFont(titleFont);
                g2d.drawString("Juego terminado", 450, 200);
                g2d.setFont(textFont);
                g2d.drawString("Puntaje final: " + puntajeJugador, 520, 300);
                drawButton(g2d, finishButton, "Reiniciar", new Color(150, 200, 100));
                drawButton(g2d, rankingButton, "Ranking", new Color(200, 150, 250));
            }

            case "ranking" -> {
                g2d.setFont(titleFont);
                g2d.drawString("Ranking - " + currentLevel.toUpperCase(), 400, 100);
                drawButton(g2d, backButton, "Volver", new Color(255, 200, 100));

                ArrayList<Jugador> ranking = getCurrentRanking();
                ranking.sort(Comparator.comparingInt(Jugador::getPuntaje).reversed());

                // Fondo del ranking
                g2d.setColor(new Color(240, 250, 255));
                g2d.fillRoundRect(200, 150, 900, 400, 30, 30);
                g2d.setColor(Color.BLACK);
                g2d.drawRoundRect(200, 150, 900, 400, 30, 30);

                int y = 200;
                for (Jugador j : ranking) {
                    g2d.drawString(j.nombre + " - " + j.puntaje + " pts", 450, y);
                    y += 40;
                }
            }
        }
    }

    private void drawButton(Graphics2D g2d, Rectangle rect, String text, Color color) {
        g2d.setColor(color);
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, rect.x + (rect.width - textWidth) / 2, rect.y + 40);
    }

    private void drawTextBox(Graphics2D g2d, Rectangle rect, String text, boolean selected) {
        g2d.setColor(selected ? new Color(200, 230, 255) : new Color(240, 240, 240));
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
        g2d.drawString(text, rect.x + 10, rect.y + 35);
    }

    private String[][] getCurrentQuestions() {
        return switch (currentLevel) {
            case "facil" -> questionsEasy;
            case "medio" -> questionsMedium;
            case "dificil" -> questionsHard;
            default -> questionsEasy;
        };
    }

    private int[] getCurrentCorrectAnswers() {
        return switch (currentLevel) {
            case "facil" -> correctEasy;
            case "medio" -> correctMedium;
            case "dificil" -> correctHard;
            default -> correctEasy;
        };
    }

    private ArrayList<Jugador> getCurrentRanking() {
        return switch (currentLevel) {
            case "facil" -> rankingEasy;
            case "medio" -> rankingMedium;
            case "dificil" -> rankingHard;
            default -> rankingEasy;
        };
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("El Capibara Salesiano");
        ElCapibaraSalesiano panel = new ElCapibaraSalesiano();
        frame.add(panel);
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    static class Jugador {
        String nombre;
        int puntaje;

        public Jugador(String nombre, int puntaje) {
            this.nombre = nombre;
            this.puntaje = puntaje;
        }

        public int getPuntaje() { return puntaje; }
    }
}