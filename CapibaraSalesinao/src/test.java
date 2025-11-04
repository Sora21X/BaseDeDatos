import javax.swing.JOptionPane;

public class test {
    public static void main(String[] args) {
        if (ConexionBD.conectar() != null) {
            JOptionPane.showMessageDialog(null, " Conexión exitosa");
        } else {
            JOptionPane.showMessageDialog(null, " Error al conectar");
        }
    }
}
