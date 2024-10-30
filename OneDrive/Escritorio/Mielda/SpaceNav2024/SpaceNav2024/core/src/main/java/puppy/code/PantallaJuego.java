package puppy.code;

import java.util.ArrayList;
import java.util.Random;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;


public class PantallaJuego implements Screen {

    private SpaceNavigation game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Sound explosionSound1, explosionSound2;
    private Sound nuevaRondaSound; // Sonido para nueva ronda
    private Music gameMusic, deathSound;
    private int score;
    private int ronda;
    private int velXAsteroides;
    private int velYAsteroides;
    private int cantAsteroides;

    private Texture backgroundTexture;
    private Texture starsTexture;
    private Texture pauseButtonTexture;
    private float starsX = 0;

    private Nave4 nave;
    private ArrayList<Ball2> balls1 = new ArrayList<>();
    private ArrayList<Bullet> balas = new ArrayList<>();
    private Random random = new Random();

    private boolean gameOver = false;
    private boolean paused = false;

    // Fuente para texto y diseño
    private BitmapFont fontPantallaJuego;
    private final GlyphLayout layout;
    private Rectangle btnPausa;
    
    // Declaraciones de monstruos
    private DemonOjo demonOjo;
    private DemonNave demonNave;
    private long ultimoAtaqueDemonOjo; // Variable para controlar el tiempo entre ataques de DemonOjo



    public PantallaJuego(SpaceNavigation game, int ronda, int vidas, int score,
                        int velXAsteroides, int velYAsteroides, int cantAsteroides) {
       this.game = game;

       // Asignar valores iniciales basados en el estado de la partida
       this.ronda = (ronda == 1) ? 1 : ronda;
       this.score = (ronda == 1) ? 0 : score;
       this.velXAsteroides = (ronda == 1) ? 1 : velXAsteroides;
       this.velYAsteroides = (ronda == 1) ? 1 : velYAsteroides;
       this.cantAsteroides = (ronda == 1) ? 3 : cantAsteroides;

       batch = game.getBatch();
       camera = new OrthographicCamera();
       camera.setToOrtho(false, 1200, 800);

       explosionSound1 = Gdx.audio.newSound(Gdx.files.internal("explosion1.wav"));
       explosionSound2 = Gdx.audio.newSound(Gdx.files.internal("explosion2.wav"));
       gameMusic = Gdx.audio.newMusic(Gdx.files.internal("SonidoPantallaJuego.mp3"));
       deathSound = Gdx.audio.newMusic(Gdx.files.internal("SonidoMuerteFinal.mp3"));
       nuevaRondaSound = Gdx.audio.newSound(Gdx.files.internal("nuevaRonda.mp3")); // Cargar sonido de nueva ronda
       gameMusic.setLooping(true);
       gameMusic.setVolume(0.5f);
       gameMusic.play();

       backgroundTexture = new Texture("fondoPantallaJuego.png");
       starsTexture = new Texture("parallax-space-stars.png");
       pauseButtonTexture = new Texture("pausa.png");

       fontPantallaJuego = new BitmapFont(Gdx.files.internal("letraPantallaJuego.fnt"));
       layout = new GlyphLayout();
       btnPausa = new Rectangle(10, 750, 50, 50);

       nave = new Nave4(Gdx.graphics.getWidth() / 2 - 50, 30, new Texture(Gdx.files.internal("MainShip3.png")),
               Gdx.audio.newSound(Gdx.files.internal("hurt.ogg")),
               new Texture(Gdx.files.internal("Rocket2.png")),
               Gdx.audio.newSound(Gdx.files.internal("SonidoDisparoNave.mp3")));
       nave.setVidas(vidas);

       // Inicialización de monstruos y eliminación de asteroides en la ronda 3
       if (ronda == 3) {
           demonOjo = new DemonOjo(
               new Texture("DemonOjoMovIzq.png"),   // Textura de movimiento hacia la izquierda
               new Texture("DemonOjoMovDer.png"),   // Textura de movimiento hacia la derecha
               new Texture("DemonOjoMovFront.png"), // Textura de movimiento frontal
               new Texture("DemonOjoAtaque.png"),   // Textura de ataque
               500, 500, 
               3, 
               Gdx.audio.newSound(Gdx.files.internal("DemonDeath2.wav")), 
               Gdx.audio.newSound(Gdx.files.internal("DemonDeath3.wav"))
           );

           demonNave = new DemonNave(
               new Texture("naveEnemiga.png"), 
               new Texture("disparoSangre.png"), 
               600, 700, 
               20,
               Gdx.audio.newSound(Gdx.files.internal("demonNaveDaño.mp3")),
               Gdx.audio.newSound(Gdx.files.internal("demonNaveDeath.wav"))
 
           );

           // Limpiar asteroides en la Ronda 3
           balls1.clear();
       } else {
           // Generar asteroides con velocidad reducida
           generarAsteroides();
       }

       // Reproducir sonido de nueva ronda solo si se trata de una nueva partida
       if (ronda == 1) {
           nuevaRondaSound.play();
       }
   }


    private void generarAsteroides() {
        Random r = new Random();
        balls1.clear();
        for (int i = 0; i < cantAsteroides; i++) {
            int tamañoMeteorito = Math.max(10, 20 + r.nextInt(10));
            int xPos = r.nextInt((int) Gdx.graphics.getWidth());
            int yPos = 50 + r.nextInt((int) Gdx.graphics.getHeight() - 50);

            // Reducir la velocidad inicial de los meteoritos
            int velXReducida = Math.max(1, velXAsteroides - 1); // Reducir velocidad en X
            int velYReducida = Math.max(1, velYAsteroides - 1); // Reducir velocidad en Y

            Ball2 nuevoAsteroide = new Ball2(xPos, yPos, tamañoMeteorito, velXReducida + r.nextInt(2), velYReducida + r.nextInt(2),
                    new Texture(Gdx.files.internal("aGreyMedium4.png")));
            balls1.add(nuevoAsteroide);
        }
    }



    public void dibujaEncabezado() {
        layout.setText(fontPantallaJuego, "Vidas: " + nave.getVidas() + " Ronda: " + ronda);
        fontPantallaJuego.draw(batch, layout, 10, 30);

        layout.setText(fontPantallaJuego, "Score: " + score);
        fontPantallaJuego.draw(batch, layout, Gdx.graphics.getWidth() - 150, 30);

        layout.setText(fontPantallaJuego, "HighScore: " + game.getHighScore());
        fontPantallaJuego.draw(batch, layout, Gdx.graphics.getWidth() / 2 - 100, 30);
    }
    @Override
    public void render(float delta) {
        // Limpiar la pantalla antes de dibujar
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Iniciar el batch
        batch.begin();

        // Dibujar el fondo y estrellas en movimiento
        batch.draw(backgroundTexture, 0, 0, 1200, 800);
        starsX -= 50 * delta;
        batch.setColor(0.2f, 0.2f, 0.5f, 1f);
        batch.draw(starsTexture, starsX, 0, 1200, 800);
        batch.draw(starsTexture, starsX + 1200, 0, 1200, 800);
        if (starsX <= -1200) starsX = 0;
        batch.setColor(Color.WHITE);

        // Dibujar encabezado de vidas, ronda y puntuación
        dibujaEncabezado();

        // Control y renderizado para la ronda 3 (demonios y sus disparos)
        if (ronda == 3) {
            if (demonOjo != null) {
                demonOjo.mover(delta);
                demonOjo.render(batch);

                // Ataque de DemonOjo cada 3 segundos
                if (TimeUtils.timeSinceMillis(ultimoAtaqueDemonOjo) > 3000) {
                    demonOjo.realizarAtaque(batch);
                    ultimoAtaqueDemonOjo = TimeUtils.millis();
                }

                // Actualizar y dibujar los disparos de DemonOjo
                for (int i = 0; i < demonOjo.getDisparos().size; i++) {
                    Bullet disparo = demonOjo.getDisparos().get(i);
                    disparo.update();
                    if (disparo.isDestroyed() || disparo.getY() < 0) {
                        demonOjo.getDisparos().removeIndex(i--); // Eliminar disparo si sale de la pantalla
                    } else {
                        disparo.draw(batch);
                    }
                }
            }

            if (demonNave != null) {
                demonNave.mover(delta);
                demonNave.render(batch);

                // Ataque de DemonNave cada 5 segundos
                demonNave.realizarAtaque(batch);

                // Actualizar y dibujar los disparos de DemonNave
                for (int i = 0; i < demonNave.getDisparos().size; i++) {
                    Bullet disparo = demonNave.getDisparos().get(i);
                    disparo.update();
                    if (disparo.isDestroyed() || disparo.getY() < 0) {
                        demonNave.getDisparos().removeIndex(i--); // Eliminar disparo si sale de la pantalla
                    } else {
                        disparo.draw(batch);
                    }
                }
            }
        }

        // Actualizar y verificar colisiones de balas y asteroides
        actualizarBalasYAsteroides(delta);

        // Dibujar balas de la nave en todas las rondas
        for (Bullet b : balas) {
            b.draw(batch);
        }

        // Dibujar asteroides solo si no es la ronda 3
        if (ronda != 3) {
            for (Ball2 meteorito : balls1) {
                meteorito.draw(batch);
            }
        }

        // Dibujar la nave (asegúrate de que solo se llama una vez dentro de batch.begin())
        nave.draw(batch, this);

        // Dibujar botón de pausa
        batch.draw(pauseButtonTexture, btnPausa.x, btnPausa.y, btnPausa.width, btnPausa.height);

        // Finalizar el batch
        batch.end();

        // Manejo de pausa
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || 
            (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && btnPausa.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()))) {
            paused = true;
            gameMusic.pause();
            game.setScreen(new PantallaPausa(game, this));
        }

        // Lógica de juego si no está en pausa o game over
        if (!paused && !gameOver) {
            if (ronda != 3) {
                actualizarBalasYAsteroides(delta);
            }
        }

        // Condición para iniciar nueva ronda
        if (!gameOver && ((balls1.isEmpty() && ronda != 3) || (ronda == 3 && demonOjo == null && demonNave == null))) {
            iniciarNuevaRonda();
        }
    }

    
    
    
    
    
    /*@Override
    public void render(float delta) {
        // Limpiar la pantalla antes de dibujar
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Iniciar el batch
        batch.begin();

        // Dibujar el fondo y estrellas en movimiento
        batch.draw(backgroundTexture, 0, 0, 1200, 800);
        starsX -= 50 * delta;
        batch.setColor(0.2f, 0.2f, 0.5f, 1f);
        batch.draw(starsTexture, starsX, 0, 1200, 800);
        batch.draw(starsTexture, starsX + 1200, 0, 1200, 800);
        if (starsX <= -1200) starsX = 0;
        batch.setColor(Color.WHITE);

        // Dibujar encabezado de vidas, ronda y puntuación
        dibujaEncabezado();

        // Control y renderizado para la ronda 3 (demonios y sus disparos)
        if (ronda == 3) {
            if (demonOjo != null) {
                demonOjo.mover(delta);
                demonOjo.render(batch);

                // Ataque de DemonOjo cada 3 segundos
                if (TimeUtils.timeSinceMillis(ultimoAtaqueDemonOjo) > 3000) {
                    demonOjo.realizarAtaque(batch);
                    ultimoAtaqueDemonOjo = TimeUtils.millis();
                }

                // Actualizar y dibujar los disparos de DemonOjo
                for (int i = 0; i < demonOjo.getDisparos().size; i++) {
                    Bullet disparo = demonOjo.getDisparos().get(i);
                    disparo.update();
                    if (disparo.isDestroyed() || disparo.getY() < 0) {
                        demonOjo.getDisparos().removeIndex(i--); // Eliminar disparo si sale de la pantalla
                    } else {
                        disparo.draw(batch);
                    }
                }
            }

            if (demonNave != null) {
                demonNave.mover(delta);
                demonNave.render(batch);

                // Ataque de DemonNave cada 5 segundos
                demonNave.realizarAtaque(batch);

                // Actualizar y dibujar los disparos de DemonNave
                for (int i = 0; i < demonNave.getDisparos().size; i++) {
                    Bullet disparo = demonNave.getDisparos().get(i);
                    disparo.update();
                    if (disparo.isDestroyed() || disparo.getY() < 0) {
                        demonNave.getDisparos().removeIndex(i--); // Eliminar disparo si sale de la pantalla
                    } else {
                        disparo.draw(batch);
                    }
                }
            }
        }

        // Actualizar y verificar colisiones de balas y asteroides
        actualizarBalasYAsteroides(delta);

        // Dibujar balas de la nave en todas las rondas
        for (Bullet b : balas) {
            b.draw(batch);
        }

        // Dibujar asteroides solo si no es la ronda 3
        if (ronda != 3) {
            for (Ball2 meteorito : balls1) {
                meteorito.draw(batch);
            }
        }

        // Dibujar la nave (asegúrate de que solo se llama una vez dentro de batch.begin())
        nave.draw(batch, this);

        // Dibujar botón de pausa
        batch.draw(pauseButtonTexture, btnPausa.x, btnPausa.y, btnPausa.width, btnPausa.height);

        // Finalizar el batch
        batch.end();

        // Manejo de pausa
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || 
            (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && btnPausa.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()))) {
            paused = true;
            gameMusic.pause();
            game.setScreen(new PantallaPausa(game, this));
        }

        // Lógica de juego si no está en pausa o game over
        if (!paused && !gameOver) {
            if (ronda != 3) {
                actualizarBalasYAsteroides(delta);
            }
        }

        // Condición para iniciar nueva ronda
        if (!gameOver && balls1.isEmpty() && ronda != 3) {
            iniciarNuevaRonda();
        }
    }*/

    private void actualizarBalasYAsteroides(float delta) {
        // Actualizar posición y verificar colisiones de cada bala
        for (int i = 0; i < balas.size(); i++) {
            Bullet b = balas.get(i);
            b.update();

            if (ronda != 3) {
                // Colisión de balas con asteroides en rondas normales
                for (int j = 0; j < balls1.size(); j++) {
                    Ball2 meteorito = balls1.get(j);
                    if (b.getHitbox().overlaps(meteorito.getArea())) {
                        explosionSound1.play();
                        balls1.remove(meteorito);
                        score += 10;
                        b.setDestroyed(true);
                        break;
                    }
                }
            } else {
                // Colisión de balas con demonios en la ronda 3
                if (demonOjo != null && b.getHitbox().overlaps(demonOjo.getHitbox())) {
                    demonOjo.restarVida(1);
                    if (demonOjo.getVidas() <= 0) {
                        demonOjo.morir();
                        demonOjo = null; // Eliminar demonOjo de la pantalla
                    }
                    b.setDestroyed(true);
                }
                if (demonNave != null && b.getHitbox().overlaps(demonNave.getHitbox())) {
                    demonNave.restarVida(1);
                    if (demonNave.getVidas() <= 0) {
                        demonNave.morir();                   
                        demonNave = null; // Eliminar demonNave de la pantalla
                    }
                    b.setDestroyed(true);
                }
            }

            if (b.isDestroyed()) {
                balas.remove(i--);
            }
        }

        // Movimiento y colisión de asteroides con la nave en rondas normales
        if (ronda != 3) {
            for (int i = 0; i < balls1.size(); i++) {
                Ball2 meteorito = balls1.get(i);
                meteorito.update();  // Asegura que cada asteroide se mueva

                if (!nave.estaHerido() && nave.getHitbox().overlaps(meteorito.getArea())) {
                    explosionSound1.play();
                    balls1.remove(meteorito);
                    nave.restarVida(1);
                    if (nave.getVidas() <= 0) {
                        handleGameOver();
                        break;
                    }
                }
            }
        }

        // Actualizar y verificar colisiones de los disparos enemigos en ronda 3
        if (ronda == 3) {
            if (demonOjo != null) { // Verificación de que demonOjo no es null
                for (int i = 0; i < demonOjo.getDisparos().size; i++) {
                    Bullet disparo = demonOjo.getDisparos().get(i);
                    disparo.update();
                    if (disparo.getHitbox().overlaps(nave.getHitbox())) {
                        nave.restarVida(demonOjo.getDanio());
                        disparo.setDestroyed(true);
                        if (nave.getVidas() <= 0) {
                            handleGameOver();
                        }
                    }
                    if (disparo.isDestroyed() || disparo.getY() < 0) {
                        demonOjo.getDisparos().removeIndex(i--);
                    }
                }
            }

            if (demonNave != null) { // Verificación de que demonNave no es null
                for (int i = 0; i < demonNave.getDisparos().size; i++) {
                    Bullet disparo = demonNave.getDisparos().get(i);
                    disparo.update();
                    if (disparo.getHitbox().overlaps(nave.getHitbox())) {
                        nave.restarVida(demonNave.getDanio());
                        disparo.setDestroyed(true);
                        if (nave.getVidas() <= 0) {
                            handleGameOver();
                        }
                    }
                    if (disparo.isDestroyed() || disparo.getY() < 0) {
                        demonNave.getDisparos().removeIndex(i--);
                    }
                }
            }
        }
    }







    



 

   




    /*private void handleGameOver() {
        gameOver = true;
        gameMusic.stop();
        deathSound.play();
        Timer.schedule(new Task() {
            @Override
            public void run() {
                if (score > game.getHighScore()) {
                    game.setHighScore(score);
                }
                game.setScreen(new PantallaGameOver(game, score, game.getHighScore()));
                dispose();
            }
        }, 3);
    }*/
    private void handleGameOver() {
        if (!gameOver) {
            gameOver = true;
            gameMusic.stop();
            deathSound.play();
            Timer.schedule(new Task() {
                @Override
                public void run() {
                    if (score > game.getHighScore()) {
                        game.setHighScore(score);
                    }
                    game.setScreen(new PantallaGameOver(game, score, game.getHighScore()));
                    dispose();
                }
            }, 3); // Esperar 3 segundos antes de ir a la pantalla de Game Over
        }
    }


    private void iniciarNuevaRonda() {
        int nuevosAsteroides = Math.min(15, cantAsteroides + 2);
        int nuevaVelX = Math.min(15, velXAsteroides + 1);
        int nuevaVelY = Math.min(15, velYAsteroides + 1);

        // Configurar la nueva pantalla de juego con la siguiente ronda
        PantallaJuego nuevaPantalla = new PantallaJuego(game, ronda + 1, nave.getVidas(), score, nuevaVelX, nuevaVelY, nuevosAsteroides);

        game.setScreen(nuevaPantalla);

        // Reproducir el sonido de nueva ronda después de que se cambie la pantalla
        nuevaPantalla.reproducirSonidoNuevaRonda();

        // Liberar recursos de la pantalla actual
        dispose();
    }
        public void reproducirSonidoNuevaRonda() {
        nuevaRondaSound.play();
    }


    
    public boolean agregarBala(Bullet bb) {
        return balas.add(bb);
    }
    public void reanudarMusica() {
        if (!gameMusic.isPlaying()) {
            gameMusic.play();
        }
    }
    public Music getGameMusic() {
    return gameMusic;
}




    @Override public void show() { 
        paused = false;
        gameMusic.play(); 
    }
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        explosionSound1.dispose();
        explosionSound2.dispose();
        gameMusic.dispose();
        deathSound.dispose();
        nuevaRondaSound.dispose();
        backgroundTexture.dispose();
        starsTexture.dispose();
        pauseButtonTexture.dispose();
        fontPantallaJuego.dispose();
        

        // Liberar recursos específicos de los monstruos
        if (demonOjo != null) {
            demonOjo.getTexturaIzquierda().dispose();
            demonOjo.getTexturaDerecha().dispose();
            demonOjo.getTexturaFrontal().dispose();
            demonOjo.getTexturaAtaque().dispose();
        }

        if (demonNave != null) {
            demonNave.getTextura().dispose();
            demonNave.getDisparoTextura().dispose();
            demonNave.dispose(); // Llama al dispose() de demonNave para liberar sus recursos
        }
    }
}
