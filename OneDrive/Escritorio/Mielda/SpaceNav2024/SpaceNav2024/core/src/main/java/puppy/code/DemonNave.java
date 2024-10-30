package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.audio.Sound;

public class DemonNave extends Monstruo implements Ataque {
    private Texture disparoTextura;
    private long ultimoDisparo;
    private boolean moviendoDerecha = true;
    private final int VELOCIDAD = 100;
    private final int VELOCIDAD_DISPARO = -10; // Velocidad del disparo hacia abajo
    private final int INTERVALO_DISPARO = 5000; // Intervalo de 5 segundos entre disparos
    private Array<Bullet> disparos = new Array<>(); // Almacena los disparos de la nave enemiga
    private Rectangle hitbox;
    private Sound sonidoDaño;
    private Sound sonidoMuerte;

    public DemonNave(Texture textura, Texture disparoTextura, float x, float y, int vidas, Sound sonidoDaño, Sound sonidoMuerte) {
        super(textura, x, y, vidas);
        this.disparoTextura = disparoTextura;
        this.ultimoDisparo = TimeUtils.millis();
        this.hitbox = new Rectangle(x, y, textura.getWidth(), textura.getHeight()); // Inicializar el hitbox
        this.sonidoDaño = sonidoDaño;
        this.sonidoMuerte = sonidoMuerte;
    }

    @Override
    public void mover(float delta) {
        // Movimiento horizontal
        if (moviendoDerecha) {
            x += VELOCIDAD * delta;
            if (x > 1200 - textura.getWidth()) {
                moviendoDerecha = false;
            }
        } else {
            x -= VELOCIDAD * delta;
            if (x < 0) {
                moviendoDerecha = true;
            }
        }

        // Actualizar hitbox
        hitbox.setPosition(x, y);
    }

    @Override
    public void realizarAtaque(SpriteBatch batch) {
        if (TimeUtils.timeSinceMillis(ultimoDisparo) > INTERVALO_DISPARO) {
            Bullet disparo = new Bullet((int)(x + textura.getWidth() / 2 - disparoTextura.getWidth() / 2), (int)y, 0, VELOCIDAD_DISPARO, disparoTextura);
            disparos.add(disparo);
            ultimoDisparo = TimeUtils.millis();
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
        realizarAtaque(batch);

        // Dibujar y actualizar disparos activos
        for (int i = 0; i < disparos.size; i++) {
            Bullet disparo = disparos.get(i);
            disparo.update(); // Actualizar la posición del disparo

            // Eliminar el disparo si sale de la pantalla
            if (disparo.getY() < 0 || disparo.isDestroyed()) {
                disparos.removeIndex(i--);
            } else {
                disparo.draw(batch); // Dibujar el disparo si sigue en pantalla
            }
        }
    }

    @Override
    public int getDanio() {
        return 3; // Daño completo para eliminar todas las vidas de la nave
    }

    @Override
    protected void morir() {
        System.out.println("DemonNave ha sido destruido.");
    }

    public Array<Bullet> getDisparos() {
        return disparos;
    }

    public void restarVida(int cantidad) {
        this.vidas -= cantidad;
        if (this.vidas > 0) {
            sonidoDaño.play(); // Reproduce el sonido de daño
        } else if (this.vidas <= 0) {
            sonidoMuerte.play(); // Reproduce el sonido de muerte
            morir(); // Llama al método morir después de reproducir el sonido
        }
    }

    @Override
    public Rectangle getHitbox() {
        return hitbox; // Retorna el hitbox para verificar colisiones
    }

    // Métodos para obtener las texturas, necesarios para liberarlas en PantallaJuego
    public Texture getTextura() {
        return textura;
    }

    public Texture getDisparoTextura() {
        return disparoTextura;
    }

    // Método para liberar los recursos
    public void dispose() {
        disparoTextura.dispose();
        if (sonidoDaño != null) sonidoDaño.dispose();
        if (sonidoMuerte != null) sonidoMuerte.dispose();
    }
}
