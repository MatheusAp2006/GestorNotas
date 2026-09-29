package co.edu.ue.gestorNotas.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.PATCH;

public interface ApiService {

    // ---------- AUTH ----------
    @POST("api/auth/registro")
    Call<AuthResponse> registro(@Body RegistroRequest body);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest body);

    // ---------- MATERIAS ----------
    @GET("api/materias")
    Call<List<MateriaResponse>> listarMaterias();

    @POST("api/materias")
    Call<MateriaResponse> crearMateria(@Body MateriaRequest body);

    @PUT("api/materias/{id}")
    Call<MateriaResponse> actualizarMateria(@Path("id") long id, @Body MateriaRequest body);

    @DELETE("api/materias/{id}")
    Call<Void> eliminarMateria(@Path("id") long id);

    // ---------- TAREAS ----------
    @GET("api/materias/{idMateria}/tareas")
    Call<List<TareaResponse>> listarTareas(@Path("idMateria") long idMateria);

    @POST("api/tareas")
    Call<TareaResponse> crearTarea(@Body TareaRequest body);

    @PUT("api/tareas/{id}")
    Call<TareaResponse> actualizarTarea(@Path("id") long id, @Body TareaRequest body);

    @PATCH("api/tareas/{id}/estado")
    Call<TareaResponse> cambiarEstado(@Path("id") long id, @Body EstadoRequest body);

    @DELETE("api/tareas/{id}")
    Call<Void> eliminarTarea(@Path("id") long id);
}