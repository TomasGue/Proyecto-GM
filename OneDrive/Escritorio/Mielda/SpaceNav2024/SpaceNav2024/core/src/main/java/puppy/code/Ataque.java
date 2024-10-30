package puppy.code;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Ataque {
    void realizarAtaque(SpriteBatch batch); // Método para realizar el ataque
    int getDanio(); // Método para obtener el daño del ataque
}
