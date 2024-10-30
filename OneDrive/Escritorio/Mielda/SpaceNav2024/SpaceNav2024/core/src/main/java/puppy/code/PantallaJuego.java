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
               3
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
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, 1200, 800);

        // Fondo de estrellas en movimiento
        starsX -= 50 * delta;
        batch.setColor(0.2f, 0.2f, 0.5f, 1f);
        batch.draw(starsTexture, starsX, 0, 1200, 800);
        batch.draw(starsTexture, starsX + 1200, 0, 1200, 800);
        if (starsX <= -1200) starsX = 0;
        batch.setColor(Color.WHITE);

        dibujaEncabezado();

        // Control y renderizado para la ronda 3
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
        } else {
            // Actualizar y dibujar asteroides y balas de la nave solo si no es la ronda 3
            actualizarBalasYAsteroides(delta);
        }

        // Botón de pausa en la esquina superior izquierda
        batch.draw(pauseButtonTexture, btnPausa.x, btnPausa.y, btnPausa.width, btnPausa.height);
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
            nave.draw(batch, this);
        }

        // Dibujar asteroides solo si no es la ronda 3
        if (ronda != 3) {
            for (Ball2 meteorito : balls1) {
                meteorito.draw(batch);
            }
        }

        // Dibujar balas de la nave
        for (Bullet b : balas) {
            b.draw(batch);
        }

        batch.end();

        // Condición para iniciar nueva ronda
        if (!gameOver && balls1.isEmpty() && ronda != 3) {
            iniciarNuevaRonda();
        }

        // Mostrar posiciones de demonios si están inicializados
        if (demonOjo != null) {
            System.out.println("DemonOjo posición: x=" + demonOjo.getX() + ", y=" + demonOjo.getY());
        }
        if (demonNave != null) {
            System.out.println("DemonNave posición: x=" + demonNave.getX() + ", y=" + demonNave.getY());
        }
    }

    private void actualizarBalasYAsteroides(float delta) {
        // Actualizar posición y verificar colisiones de cada bala
        for (int i = 0; i < balas.size(); i++) {
            Bullet b = balas.get(i);
            b.update();  // Asegura que cada bala se actualice en todas las rondas
            System.out.println("Bala actualizada en posición: " + b.getHitbox().getY());

            // Verificar colisión entre bala y meteorito solo si no es la ronda 3
            if (ronda != 3) {
                for (int j = 0; j < balls1.size(); j++) {
                    Ball2 meteorito = balls1.get(j);
                    if (b.getHitbox().overlaps(meteorito.getArea())) { // Usar overlaps para verificar colisión
                        System.out.println("Bala impacta meteorito");
                        explosionSound1.play();
                        balls1.remove(meteorito);
                        score += 10;
                        b.setDestroyed(true); // Marca la bala como destruida
                        break;
                    }
                }
            } else {
                // En la ronda 3, verifica colisiones entre balas y demonios
                if (demonOjo != null && b.getHitbox().overlaps(demonOjo.getHitbox())) {
                    System.out.println("Bala impacta DemonOjo");
                    demonOjo.restarVida(1);
                    b.setDestroyed(true);
                }
                if (demonNave != null && b.getHitbox().overlaps(demonNave.getHitbox())) {
                    System.out.println("Bala impacta DemonNave");
                    demonNave.restarVida(1);
                    b.setDestroyed(true);
                }
            }

            // Eliminar balas destruidas
            if (b.isDestroyed()) {
                balas.remove(i--);
            }
        }

        // Actualizar meteoritos solo si no es la ronda 3
        if (ronda != 3) {
            for (Ball2 ball : balls1) {
                ball.update();
            }
            for (int i = 0; i < balls1.size(); i++) {
                Ball2 meteorito = balls1.get(i);
                if (nave.getHitbox().overlaps(meteorito.getArea())) {
                    System.out.println("Meteorito impacta nave");
                    balls1.remove(meteorito);
                    nave.restarVida(1);
                    if (nave.getVidas() <= 0) {
                        handleGameOver();
                        break;
                    }
                }
            }
        }

        // Actualizar y manejar los disparos de DemonOjo y DemonNave en la ronda 3
        if (ronda == 3) {
            for (int i = 0; i < demonOjo.getDisparos().size; i++) {
                Bullet disparo = demonOjo.getDisparos().get(i);
                disparo.update();
                if (disparo.getHitbox().overlaps(nave.getHitbox())) {
                    System.out.println("Disparo de DemonOjo impacta nave");
                    nave.restarVida(demonOjo.getDanio());
                    disparo.setDestroyed(true);
                }
                if (disparo.isDestroyed() || disparo.getY() < 0) {
                    demonOjo.getDisparos().removeIndex(i--);
                }
            }
            for (int i = 0; i < demonNave.getDisparos().size; i++) {
                Bullet disparo = demonNave.getDisparos().get(i);
                disparo.update();
                if (disparo.getHitbox().overlaps(nave.getHitbox())) {
                    System.out.println("Disparo de DemonNave impacta nave");
                    nave.restarVida(demonNave.getDanio());
                    disparo.setDestroyed(true);
                }
                if (disparo.isDestroyed() || disparo.getY() < 0) {
                    demonNave.getDisparos().removeIndex(i--);
                }
            }
        }
    }



    



 

   




    private void handleGameOver() {
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
        }
    }
}













/*package puppy.code;

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

        // Generar asteroides al iniciar el juego
        generarAsteroides();

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
            Ball2 nuevoAsteroide = new Ball2(xPos, yPos, tamañoMeteorito, velXAsteroides + r.nextInt(4), velYAsteroides + r.nextInt(4),
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
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, 1200, 800);

        // Fondo de estrellas en movimiento
        starsX -= 50 * delta;
        batch.setColor(0.2f, 0.2f, 0.5f, 1f);
        batch.draw(starsTexture, starsX, 0, 1200, 800);
        batch.draw(starsTexture, starsX + 1200, 0, 1200, 800);
        if (starsX <= -1200) starsX = 0;
        batch.setColor(Color.WHITE);

        dibujaEncabezado();

        // Botón de pausa en la esquina superior izquierda
        batch.draw(pauseButtonTexture, btnPausa.x, btnPausa.y, btnPausa.width, btnPausa.height);
        // Dentro del método render de PantallaJuego, en la parte donde se activa el menú de pausa:
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || 
            (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && btnPausa.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()))) {
            paused = true;
            gameMusic.pause();  // Pausar la música al activar el menú de pausa
            game.setScreen(new PantallaPausa(game, this));
        }
    
        // Lógica de juego solo si no está en pausa o game over
        if (!paused && !gameOver) {
            actualizarBalasYAsteroides(delta);
            nave.draw(batch, this);
        }

        // Dibujar asteroides
        for (Ball2 meteorito : balls1) {
            meteorito.draw(batch);
        }

        // Dibujar balas
        for (Bullet b : balas) {
            b.draw(batch);
        }

        batch.end();

        // Condición para iniciar nueva ronda
        if (!gameOver && balls1.isEmpty()) {
            iniciarNuevaRonda();
        }
    }

    private void actualizarBalasYAsteroides(float delta) {
        for (int i = 0; i < balas.size(); i++) {
            Bullet b = balas.get(i);
            b.update();
            for (int j = 0; j < balls1.size(); j++) {
                Ball2 meteorito = balls1.get(j);
                if (b.checkCollision(meteorito)) {
                    explosionSound1.play();
                    balls1.remove(meteorito);
                    score += 10;
                    break;
                }
            }
            if (b.isDestroyed()) {
                balas.remove(i--);
            }
        }

        for (Ball2 ball : balls1) {
            ball.update();
        }

        // Colisiones entre asteroides y la nave
        for (int i = 0; i < balls1.size(); i++) {
            Ball2 meteorito = balls1.get(i);
            if (nave.checkCollision(meteorito)) {
                balls1.remove(meteorito);
                if (nave.getVidas() <= 0) {
                    handleGameOver();
                    break;
                }
            }
        }
    }

    private void handleGameOver() {
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
    @Override public void dispose() {
        explosionSound1.dispose();
        explosionSound2.dispose();
        gameMusic.dispose();
        deathSound.dispose();
        nuevaRondaSound.dispose(); // Liberar el recurso del sonido de nueva ronda
        backgroundTexture.dispose();
        starsTexture.dispose();
        pauseButtonTexture.dispose();
    }
}
*/