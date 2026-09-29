package co.edu.ue.gestorNotas.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // 10.0.2.2 es como el EMULADOR ve el localhost de tu computador
    private static final String BASE_URL = "http://localhost:8080/";

    private static Retrofit retrofit;
    private static String token;

    public static void setToken(String nuevoToken) {
        token = nuevoToken;
    }

    public static ApiService getService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    // Agrega el token a TODAS las peticiones automáticamente
                    .addInterceptor(chain -> {
                        Request.Builder builder = chain.request().newBuilder();
                        if (token != null) {
                            builder.addHeader("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(builder.build());
                    })
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}