
package co.umariana.dao;

import co.umariana.conexion.ConexionBD;
import co.umariana.modelo.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object).
 *
 * Esta clase se encarga de realizar las operaciones CRUD
 * sobre la tabla "estudiantes" de la base de datos.
 *
 * CRUD significa:
 * C: Create  -> Registrar estudiantes.
 * R: Read    -> Consultar y listar estudiantes.
 * U: Update  -> Actualizar estudiantes.
 * D: Delete  -> Eliminar estudiantes.
 *
 * Las consultas se realizan mediante JDBC y PreparedStatement.
 */
public class EstudianteDAO {

    // =========================================================
    // ETAPA 1. REGISTRAR ESTUDIANTE (INSERT)
    // =========================================================

    /**
     * Registra un estudiante nuevo en la base de datos.
     *
     * El ID no se incluye en el INSERT porque lo genera
     * automáticamente la base de datos.
     *
     * @param estudiante objeto con nombre, correo y semestre.
     * @return true si se registra; false si ocurre un error.
     */
    public boolean registrar(Estudiante estudiante) {

        // Consulta SQL con tres parámetros.
        String sql = "INSERT INTO estudiantes (nombre, correo, semestre) VALUES (?, ?, ?)";

        // Abre la conexión y prepara la consulta.
        // try-with-resources cierra los recursos automáticamente.
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asigna los valores a los signos de interrogación.
            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getCorreo());
            ps.setInt(3, estudiante.getSemestre());

            // Ejecuta el INSERT.
            // Devuelve true si se insertó al menos una fila.
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            // Captura errores como un correo duplicado.
            System.out.println("Error al registrar: " + e.getMessage());

            // No lanza la excepción al código que llamó al DAO.
            return false;
        }
    }


    // =========================================================
    // ETAPA 2. LISTAR ESTUDIANTES (SELECT)
    // =========================================================

    /**
     * Consulta todos los estudiantes ordenados por ID.
     *
     * @return lista de estudiantes; lista vacía si ocurre un error
     *         o no existen registros.
     */
    public List<Estudiante> listar() {

        // Lista donde guardaremos los estudiantes consultados.
        List<Estudiante> estudiantes = new ArrayList<>();

        // Consulta todos los campos necesarios, ordenados por ID.
        String sql = "SELECT id, nombre, correo, semestre "
                   + "FROM estudiantes ORDER BY id";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // rs.next() avanza a la siguiente fila del resultado.
            // El ciclo termina cuando no quedan más filas.
            while (rs.next()) {

                // Construye un objeto con los datos de la fila actual.
                Estudiante estudiante = new Estudiante(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("correo"),
                    rs.getInt("semestre")
                );

                // Agrega el objeto a la lista.
                estudiantes.add(estudiante);
            }

        } catch (SQLException e) {

            // Informa el error sin lanzar la excepción.
            System.out.println("Error al listar: " + e.getMessage());
        }

        // Devuelve la lista con los registros encontrados.
        // Si no hubo resultados o ocurrió un error, estará vacía.
        return estudiantes;
    }


    // =========================================================
    // ETAPA 3. BUSCAR ESTUDIANTE POR ID (SELECT WHERE)
    // =========================================================

    /**
     * Busca un estudiante por su identificador.
     *
     * @param id identificador del estudiante que se busca.
     * @return el estudiante encontrado o null si no existe
     *         o se presenta un error.
     */
    public Estudiante buscarPorId(int id) {

        // El signo ? representa el ID que se consultará.
        String sql = "SELECT id, nombre, correo, semestre "
                   + "FROM estudiantes WHERE id = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asigna el ID al parámetro de la consulta.
            ps.setInt(1, id);

            // Ejecuta la consulta y obtiene el resultado.
            try (ResultSet rs = ps.executeQuery()) {

                // Si existe una fila, se construye el objeto.
                if (rs.next()) {

                    return new Estudiante(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getInt("semestre")
                    );
                }
            }

        } catch (SQLException e) {

            // Informa el error de consulta.
            System.out.println("Error al buscar por ID: " + e.getMessage());
        }

        // Si no se encontró el estudiante o hubo un error.
        return null;
    }


    // =========================================================
    // ETAPA 4. ACTUALIZAR ESTUDIANTE (UPDATE)
    // =========================================================

    /**
     * Actualiza los datos de un estudiante existente.
     *
     * El ID determina qué estudiante se modificará.
     *
     * @param estudiante objeto con ID y nuevos datos.
     * @return true si se modificó una fila; false si no se
     *         actualizó ningún registro o hubo un error.
     */
    public boolean actualizar(Estudiante estudiante) {

        // Actualiza los datos del estudiante cuyo ID coincida.
        String sql = "UPDATE estudiantes "
                   + "SET nombre = ?, correo = ?, semestre = ? "
                   + "WHERE id = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asigna los nuevos datos en el orden de los parámetros.
            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getCorreo());
            ps.setInt(3, estudiante.getSemestre());

            // El último parámetro indica qué estudiante se actualiza.
            ps.setInt(4, estudiante.getId());

            // Devuelve true si la base de datos informa
            // que se modificó al menos una fila.
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            // También captura errores como un correo UNIQUE duplicado.
            System.out.println("Error al actualizar: " + e.getMessage());

            return false;
        }
    }


    // =========================================================
    // ETAPA 5. ELIMINAR ESTUDIANTE (DELETE)
    // =========================================================

    /**
     * Elimina un estudiante mediante su ID.
     *
     * @param id identificador del estudiante que se eliminará.
     * @return true si se eliminó una fila; false si no existe
     *         el registro o se presenta un error.
     */
    public boolean eliminar(int id) {

        // La consulta elimina únicamente el registro cuyo ID coincida.
        String sql = "DELETE FROM estudiantes WHERE id = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asigna el ID al parámetro de la consulta.
            ps.setInt(1, id);

            // Ejecuta el DELETE.
            // Si se eliminó al menos una fila, devuelve true.
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            // Captura e informa errores de eliminación.
            System.out.println("Error al eliminar: " + e.getMessage());

            return false;
        }
    }
}
