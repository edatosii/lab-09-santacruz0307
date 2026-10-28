
package co.umariana.servlets;

import co.umariana.dao.EstudianteDAO;
import co.umariana.modelo.Estudiante;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador del CRUD de estudiantes.
 *
 * Responsabilidades:
 * 1. Recibir las peticiones HTTP.
 * 2. Obtener y validar los datos del formulario.
 * 3. Llamar al DAO para consultar o modificar estudiantes.
 * 4. Elegir la vista que se debe mostrar o redirigir al usuario.
 */
@WebServlet("/estudiantes")
public class EstudianteServlet extends HttpServlet {

    // Rutas de las vistas JSP.
    // WEB-INF impide que se acceda directamente a estas vistas desde el navegador.
    static final String VISTA_LISTAR = "/WEB-INF/vistas/listar.jsp";
    static final String VISTA_FORMULARIO = "/WEB-INF/vistas/formulario.jsp";

    // Objeto encargado de las operaciones con la base de datos.
    private final EstudianteDAO dao = new EstudianteDAO();


    // =========================================================
    // VALIDACIÓN DE DATOS
    // =========================================================

    /**
     * Comprueba que el nombre, el correo y el semestre sean válidos.
     *
     * @return mensaje de error si los datos son incorrectos;
     *         null si todos son válidos.
     */
    static String validar(String nombre, String correo, String semestre) {

        // El nombre no puede ser nulo ni estar vacío.
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre es obligatorio";
        }

        // Comprueba que el correo tenga una estructura básica válida.
        if (correo == null
                || !correo.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "El correo no es válido";
        }

        // Intenta convertir el semestre a entero y verifica su rango.
        try {
            int s = Integer.parseInt(semestre.trim());

            if (s < 1 || s > 10) {
                return "El semestre debe ser un número entre 1 y 10";
            }

        } catch (NumberFormatException | NullPointerException e) {

            // Se ejecuta si el semestre no es un número válido
            // o si el parámetro no fue enviado.
            return "El semestre debe ser un número entre 1 y 10";
        }

        // Si ninguna validación falla, los datos son válidos.
        return null;
    }


    // =========================================================
    // ETAPA 6. MOSTRAR INFORMACIÓN (GET)
    // =========================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Lee el parámetro "accion" de la URL.
        String accion = request.getParameter("accion");


        // -----------------------------------------------------
        // ACCIÓN: NUEVO
        // Muestra un formulario vacío para registrar un estudiante.
        // -----------------------------------------------------
        if ("nuevo".equals(accion)) {

            request.getRequestDispatcher(VISTA_FORMULARIO)
                   .forward(request, response);

            return;
        }


        // -----------------------------------------------------
        // ACCIÓN: LISTAR
        // También se ejecuta cuando no se especifica una acción.
        // Ejemplos:
        // /estudiantes
        // /estudiantes?accion=listar
        // -----------------------------------------------------
        if (accion == null || "listar".equals(accion)) {

            // Consulta los estudiantes mediante el DAO.
            request.setAttribute("estudiantes", dao.listar());

            // Envía la solicitud a la vista que muestra la lista.
            request.getRequestDispatcher(VISTA_LISTAR)
                   .forward(request, response);

            return;
        }


        // -----------------------------------------------------
        // ACCIÓN: EDITAR
        // Busca el estudiante y carga sus datos en el formulario.
        // Ejemplo: /estudiantes?accion=editar&id=5
        // -----------------------------------------------------
        if ("editar".equals(accion)) {

            // Obtiene el ID enviado en la URL.
            String idParametro = request.getParameter("id");

            int id;

            // Convierte el ID a entero.
            // Si no es numérico, responde con error 404.
            try {
                id = Integer.parseInt(idParametro);

            } catch (NumberFormatException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Busca el estudiante correspondiente al ID.
            Estudiante estudiante = dao.buscarPorId(id);

            // Si no existe, devuelve error 404.
            if (estudiante == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Envía el estudiante encontrado a la vista.
            request.setAttribute("estudiante", estudiante);

            // Abre el formulario con los datos existentes.
            request.getRequestDispatcher(VISTA_FORMULARIO)
                   .forward(request, response);

            return;
        }


        // -----------------------------------------------------
        // ACCIÓN DESCONOCIDA
        // Si la acción no está contemplada, devuelve error 400.
        // -----------------------------------------------------
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }


    // =========================================================
    // ETAPA 7. CREAR, ACTUALIZAR Y ELIMINAR (POST)
    // =========================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // Interpreta correctamente caracteres como tildes y ñ
        // en los parámetros enviados por el formulario.
        request.setCharacterEncoding("UTF-8");

        // Obtiene la operación que se quiere realizar.
        String accion = request.getParameter("accion");


        // -----------------------------------------------------
        // ACCIÓN: CREAR
        // Registra un estudiante nuevo.
        // -----------------------------------------------------
        if ("crear".equals(accion)) {

            // Lee los datos del formulario.
            String nombre = request.getParameter("nombre");
            String correo = request.getParameter("correo");
            String semestre = request.getParameter("semestre");

            // Valida los datos antes de acceder al DAO.
            String error = validar(nombre, correo, semestre);

            if (error == null) {

                // Crea el objeto con ID 0 porque la base de datos
                // genera automáticamente el identificador.
                Estudiante estudiante = new Estudiante(
                    0,
                    nombre.trim(),
                    correo.trim(),
                    Integer.parseInt(semestre.trim())
                );

                // Intenta registrar el estudiante.
                if (dao.registrar(estudiante)) {

                    // Redirige a la lista después de guardar.
                    response.sendRedirect("estudiantes");
                    return;
                }

                // Si falla el registro, puede tratarse de un correo
                // duplicado u otro error de base de datos.
                error = "No se pudo guardar el estudiante. "
                      + "Verifique que el correo no esté repetido.";
            }

            // Conserva el mensaje para mostrarlo en el formulario.
            request.setAttribute("error", error);

            // Muestra nuevamente el formulario.
            request.getRequestDispatcher(VISTA_FORMULARIO)
                   .forward(request, response);

            return;
        }


        // -----------------------------------------------------
        // ACCIÓN: ACTUALIZAR
        // Modifica los datos de un estudiante existente.
        // -----------------------------------------------------
        if ("actualizar".equals(accion)) {

            // Obtiene el ID y los nuevos datos del formulario.
            String idParametro = request.getParameter("id");
            String nombre = request.getParameter("nombre");
            String correo = request.getParameter("correo");
            String semestre = request.getParameter("semestre");

            int id;

            // Primero comprueba que el ID sea numérico.
            try {
                id = Integer.parseInt(idParametro);

            } catch (NumberFormatException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Verifica que el estudiante exista antes de validar
            // los demás datos, como exige el contrato.
            Estudiante existente = dao.buscarPorId(id);

            if (existente == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Valida los datos nuevos.
            String error = validar(nombre, correo, semestre);

            if (error == null) {

                // Construye el objeto con el ID del estudiante
                // que se está modificando.
                Estudiante estudiante = new Estudiante(
                    id,
                    nombre.trim(),
                    correo.trim(),
                    Integer.parseInt(semestre.trim())
                );

                // Intenta guardar los cambios.
                if (dao.actualizar(estudiante)) {

                    // Si la actualización funciona, vuelve a la lista.
                    response.sendRedirect("estudiantes");
                    return;
                }

                // Si falla, puede ser por un correo duplicado
                // o por otro error de base de datos.
                error = "No se pudo actualizar el estudiante. "
                      + "Verifique que el correo no esté repetido.";
            }

            // Envía el mensaje y el estudiante a la vista.
            // Esto permite conservar los datos del registro editado.
            request.setAttribute("error", error);
            request.setAttribute("estudiante", existente);

            // Regresa al formulario para corregir los datos.
            request.getRequestDispatcher(VISTA_FORMULARIO)
                   .forward(request, response);

            return;
        }


        // -----------------------------------------------------
        // ACCIÓN: ELIMINAR
        // Elimina el estudiante identificado por su ID.
        // -----------------------------------------------------
        if ("eliminar".equals(accion)) {

            String idParametro = request.getParameter("id");

            int id;

            // Comprueba que el ID sea un número válido.
            try {
                id = Integer.parseInt(idParametro);

            } catch (NumberFormatException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // El DAO devuelve false si no se eliminó ningún registro
            // o si se produjo un error de base de datos.
            if (!dao.eliminar(id)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Si se eliminó correctamente, vuelve a la lista.
            response.sendRedirect("estudiantes");
            return;
        }


        // -----------------------------------------------------
        // ACCIÓN DESCONOCIDA
        // Devuelve error 400 si el POST solicita una acción inválida.
        // -----------------------------------------------------
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
}