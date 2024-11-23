package puppy.code;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public interface Ataque {
    void realizarAtaque(SpriteBatch batch, float x, float y, Array<Bullet> disparos); // Método para realizar el ataque
    int getDanio(); // Método para obtener el daño del ataque
    void dispose(); // Liberar recursos
}
