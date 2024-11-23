package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import java.util.Random;

public class DemonOjo extends Monstruo {
    private Texture texturaIzquierda, texturaDerecha, texturaFrontal;
    private Sound sonidoDanio, sonidoMuerte;
    private float tiempoMovimiento = 0f;
    private boolean moviendoDerecha = true;
    private static final float VELOCIDAD_HORIZONTAL = 50f;
    private float direccionX = 0;
    private float direccionY = 0;
    private float intervaloMovimiento = 1; // Inicialmente 1 segundo

    public DemonOjo(Texture texturaIzquierda, Texture texturaDerecha, Texture texturaFrontal, 
                    float x, float y, int vidas, Sound sonidoDanio, Sound sonidoMuerte, 
                    Ataque estrategiaAtaque) {
        super(texturaFrontal, x, y, vidas, estrategiaAtaque); // Llama al constructor de Monstruo con estrategia
        this.texturaIzquierda = texturaIzquierda;
        this.texturaDerecha = texturaDerecha;
        this.texturaFrontal = texturaFrontal;
        this.sonidoDanio = sonidoDanio;
        this.sonidoMuerte = sonidoMuerte;

        // Inicializar el hitbox basado en la posición y el tamaño de la textura
        this.hitbox = new Rectangle(x, y, texturaFrontal.getWidth(), texturaFrontal.getHeight());
    }

    @Override
    public void mover(float delta) {
        Random random = new Random();

        // Actualizar el temporizador
        tiempoMovimiento += delta;

        // Cambiar dirección si el intervalo ha terminado
        if (tiempoMovimiento >= intervaloMovimiento) {
            // Generar nueva dirección aleatoria
            direccionX = (random.nextFloat() - 0.5f) * VELOCIDAD_HORIZONTAL * 2; // Aleatorio entre -VELOCIDAD_HORIZONTAL y VELOCIDAD_HORIZONTAL
            direccionY = (random.nextFloat() - 0.5f) * VELOCIDAD_HORIZONTAL * 2; // Aleatorio entre -VELOCIDAD_HORIZONTAL y VELOCIDAD_HORIZONTAL

            // Asignar una nueva duración aleatoria para este movimiento
            intervaloMovimiento = 1 + random.nextInt(4); // Aleatorio entre 1 y 4 segundos
            tiempoMovimiento = 0; // Reiniciar el tiempo para la nueva dirección

            // Cambiar textura según la dirección
            if (direccionX > 0) {
                textura = texturaDerecha;
            } else if (direccionX < 0) {
                textura = texturaIzquierda;
            } else {
                textura = texturaFrontal;
            }
        }

        // Desplazarse en la dirección actual
        x += direccionX * delta;
        y += direccionY * delta;

        // Asegurarse de que no se salga de los límites de la pantalla
        if (x < 0) x = 0;
        if (x > 1170) x = 1170;
        if (y < 50) y = 50; // Altura mínima
        if (y > 750) y = 750; // Altura máxima

        // Actualizar el hitbox
        hitbox.setPosition(x, y);
    }

    @Override
    public void actualizar(float delta, SpriteBatch batch) {
        // Llama al método base para manejar disparos y ataques
        super.actualizar(delta, batch);

        // Puedes agregar lógica específica de DemonOjo aquí si es necesario
    }

    @Override
    protected void morir() {
        System.out.println("DemonOjo ha muerto");
        sonidoMuerte.play();
    }

    public void restarVida(int valor) {
        this.vidas -= valor;
        if (this.vidas > 0) {
            sonidoDanio.play();
        } else {
            morir();
        }
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

    @Override
    public Rectangle getHitbox() {
        return hitbox; // Retornar el hitbox actualizado para colisiones
    }
}




/*package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import java.util.Random;

public class DemonOjo extends Monstruo {
    private Texture texturaIzquierda, texturaDerecha, texturaFrontal;
    private Sound sonidoDanio, sonidoMuerte;
    private float tiempoMovimiento = 0f;
    private boolean moviendoDerecha = true;
    private static final float VELOCIDAD_HORIZONTAL = 50f;
    private float direccionX = 0;
    private float direccionY = 0;
    private float intervaloMovimiento = 1; // Inicialmente 1 segundo
    private Ataque estrategiaAtaque; // Estrategia actual de ataque



    public DemonOjo(Texture texturaIzquierda, Texture texturaDerecha, Texture texturaFrontal, 
                    float x, float y, int vidas, Sound sonidoDanio, Sound sonidoMuerte, 
                    Ataque estrategiaAtaque) {
        super(texturaFrontal, x, y, vidas, estrategiaAtaque); // Llama al constructor de Monstruo con estrategia
        this.texturaIzquierda = texturaIzquierda;
        this.texturaDerecha = texturaDerecha;
        this.texturaFrontal = texturaFrontal;
        this.sonidoDanio = sonidoDanio;
        this.sonidoMuerte = sonidoMuerte;

        // Inicializar el hitbox basado en la posición y el tamaño de la textura
        this.hitbox = new Rectangle(x, y, texturaFrontal.getWidth(), texturaFrontal.getHeight());
    }

    @Override
    public void mover(float delta) {
        Random random = new Random();

        // Actualizar el temporizador
        tiempoMovimiento += delta;

        // Cambiar dirección si el intervalo ha terminado
        if (tiempoMovimiento >= intervaloMovimiento) {
            // Generar nueva dirección aleatoria
            direccionX = (random.nextFloat() - 0.5f) * VELOCIDAD_HORIZONTAL * 2; // Aleatorio entre -VELOCIDAD_HORIZONTAL y VELOCIDAD_HORIZONTAL
            direccionY = (random.nextFloat() - 0.5f) * VELOCIDAD_HORIZONTAL * 2; // Aleatorio entre -VELOCIDAD_HORIZONTAL y VELOCIDAD_HORIZONTAL

            // Asignar una nueva duración aleatoria para este movimiento
            intervaloMovimiento = 1 + random.nextInt(4); // Aleatorio entre 1 y 4 segundos
            tiempoMovimiento = 0; // Reiniciar el tiempo para la nueva dirección

            // Cambiar textura según la dirección
            if (direccionX > 0) {
                textura = texturaDerecha;
            } else if (direccionX < 0) {
                textura = texturaIzquierda;
            } else {
                textura = texturaFrontal;
            }
        }

        // Desplazarse en la dirección actual
        x += direccionX * delta;
        y += direccionY * delta;

        // Asegurarse de que no se salga de los límites de la pantalla
        if (x < 0) x = 0;
        if (x > 1170) x = 1170;
        if (y < 50) y = 50; // Altura mínima
        if (y > 750) y = 750; // Altura máxima

        // Actualizar el hitbox
        hitbox.setPosition(x, y);
    }




    @Override
    protected void morir() {
        System.out.println("DemonOjo ha muerto");
        sonidoMuerte.play();
    }

    public void restarVida(int valor) {
        this.vidas -= valor;
        if (this.vidas > 0) {
            sonidoDanio.play();
        } else {
            morir();
        }
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

    @Override
    public Rectangle getHitbox() {
        return hitbox; // Retornar el hitbox actualizado para colisiones
    }
}*/
