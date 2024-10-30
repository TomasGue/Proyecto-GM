package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Rectangle;

public class DemonOjo extends Monstruo implements Ataque {
    private Texture texturaIzquierda, texturaDerecha, texturaFrontal, texturaAtaque;
    private Sound sonidoDanio, sonidoMuerte;
    private int contadorDanio = 0;
    private final int danioAtaque = 1;
    private float tiempoMovimiento = 0f;
    private boolean moviendoDerecha = true;
    private static final float VELOCIDAD_HORIZONTAL = 50f;
    private static final float VELOCIDAD_DISPARO = -10f;

    private Array<Bullet> disparos = new Array<>(); // Almacena los disparos del DemonOjo
    private Rectangle hitbox;

    public DemonOjo(Texture texturaIzquierda, Texture texturaDerecha, Texture texturaFrontal, Texture texturaAtaque,
                    float x, float y, int vidas, Sound sonidoDanio, Sound sonidoMuerte) {
        super(texturaFrontal, x, y, vidas);
        this.texturaIzquierda = texturaIzquierda;
        this.texturaDerecha = texturaDerecha;
        this.texturaFrontal = texturaFrontal;
        this.texturaAtaque = texturaAtaque;
        this.sonidoDanio = sonidoDanio;
        this.sonidoMuerte = sonidoMuerte;

        // Inicializar el hitbox basado en la posición y el tamaño de la textura
        this.hitbox = new Rectangle(x, y, texturaFrontal.getWidth(), texturaFrontal.getHeight());
    }

    @Override
    public void mover(float delta) {
        // Alternar entre movimientos horizontales y verticales para darle un efecto "flotante"
        tiempoMovimiento += delta;
        if (tiempoMovimiento < 1) { 
            // Movimiento horizontal
            if (moviendoDerecha) {
                x += VELOCIDAD_HORIZONTAL * delta;
                textura = texturaDerecha; // Cambiar a la textura de movimiento a la derecha
                if (x > 1170) { // Límite derecho de la pantalla
                    moviendoDerecha = false;
                }
            } else {
                x -= VELOCIDAD_HORIZONTAL * delta;
                textura = texturaIzquierda; // Cambiar a la textura de movimiento a la izquierda
                if (x < 0) { // Límite izquierdo de la pantalla
                    moviendoDerecha = true;
                }
            }
        } else if (tiempoMovimiento < 2) { 
            // Movimiento vertical "flotante"
            y += Math.sin(tiempoMovimiento * 3) * 20 * delta;
            textura = texturaFrontal; // Cambiar a la textura de movimiento frontal
        } else {
            tiempoMovimiento = 0; // Reiniciar el tiempo de movimiento
        }

        // Actualizar hitbox
        hitbox.setPosition(x, y);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(textura, x, y);

        // Dibujar disparos activos
        for (Bullet disparo : disparos) {
            disparo.draw(batch);
        }
    }

    @Override
    public void realizarAtaque(SpriteBatch batch) {
        // Crear un nuevo disparo desde la posición del DemonOjo
        Bullet disparo = new Bullet((int)(x + textura.getWidth() / 2), (int)y, 0, (int)VELOCIDAD_DISPARO, texturaAtaque);
        disparos.add(disparo);
    }

    @Override
    public void recibirDanio(int danio) {
        contadorDanio += danio;
        if (contadorDanio < 3) {
            sonidoDanio.play();
        } else {
            sonidoMuerte.play();
            morir();
        }
    }

    @Override
    protected void morir() {
        System.out.println("DemonOjo ha muerto");
        contadorDanio = 0; // Resetear el daño acumulado al morir
    }

    @Override
    public int getDanio() {
        return danioAtaque;
    }

    public Array<Bullet> getDisparos() {
        return disparos;
    }

    // Métodos para obtener las texturas, necesarios para liberarlas en PantallaJuego
    public Texture getTexturaIzquierda() {
        return texturaIzquierda;
    }

    public Texture getTexturaDerecha() {
        return texturaDerecha;
    }

    public Texture getTexturaFrontal() {
        return texturaFrontal;
    }

    public Texture getTexturaAtaque() {
        return texturaAtaque;
    }

    @Override
    public Rectangle getHitbox() {
        return hitbox; // Retornar el hitbox actualizado para colisiones
    }

    public void restarVida(int valor) {
        contadorDanio += valor;
        if (contadorDanio >= 3) {
            morir();
        } else {
            sonidoDanio.play();
        }
    }
}



