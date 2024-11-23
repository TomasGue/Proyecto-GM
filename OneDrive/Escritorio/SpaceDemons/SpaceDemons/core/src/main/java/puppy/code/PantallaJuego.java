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
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;


public class PantallaJuego implements Screen {

    private SpaceNavigation game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Sound explosionSound1, explosionSound2;
    private Sound nuevaRondaSound;
    private Music gameMusic, deathSound;
    private int score;
    private int ronda;
    private int velXAsteroides;
    private int velYAsteroides;
    private int cantAsteroides;
    private long ultimoCambioEstrategiaOjo;
    private long ultimoCambioEstrategiaNave;
    private static final int INTERVALO_CAMBIO = 5000; // 5 segundos entre cambios de estrategia

    private final GameManager gameManager;
    private Texture backgroundTexture;
    private Texture starsTexture;
    private Texture pauseButtonTexture;
    private float starsX = 0;

    private Nave4 nave;
    private ArrayList<Ball2> balls1 = new ArrayList<>();
    private ArrayList<Bullet> balas = new ArrayList<>();
    private Random random = new Random();
    private long inicioRonda3;


    private boolean gameOver = false;
    private boolean paused = false;

    private BitmapFont fontPantallaJuego;
    private final GlyphLayout layout;
    private Rectangle btnPausa;

    private ArrayList<DemonOjo> demonOjos;
    private DemonOjo demonOjo;
    private DemonNave demonNave;
    private long ultimoAtaqueDemonOjo;

    // Agregar referencia a la fábrica
    private final DemonioFactory demonioFactory;

    public PantallaJuego(SpaceNavigation game, int ronda, int vidas, int score,
                         int velXAsteroides, int velYAsteroides, int cantAsteroides) {
        this.game = game;
        this.gameManager = GameManager.getInstance();

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
        nuevaRondaSound = Gdx.audio.newSound(Gdx.files.internal("nuevaRonda.mp3"));
        gameMusic.setLooping(true);
        gameMusic.setVolume(0.5f);
        gameMusic.play();

        backgroundTexture = new Texture("fondoPantallaJuego.png");
        starsTexture = new Texture("parallax-space-stars.png");
        pauseButtonTexture = new Texture("pausa.png");

        fontPantallaJuego = new BitmapFont(Gdx.files.internal("letraPantallaJuego.fnt"));
        layout = new GlyphLayout();
        btnPausa = new Rectangle(10, 750, 50, 50);

        nave = new Nave4(Gdx.graphics.getWidth() / 2 - 50, 30, 
                new Texture(Gdx.files.internal("MainShip3.png")),
                Gdx.audio.newSound(Gdx.files.internal("hurt.ogg")),
                new Texture(Gdx.files.internal("Rocket2.png")),
                Gdx.audio.newSound(Gdx.files.internal("SonidoDisparoNave.mp3")));
        nave.setVidas(vidas);

        // Inicialización de la fábrica
        demonioFactory = new DemonioFactoryConcreta();

        // Inicialización de monstruos usando la fábrica
        if (ronda == 3) {
            inicioRonda3 = TimeUtils.millis(); // Marca el inicio de la ronda
            demonOjos = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                DemonOjo nuevoDemonOjo = demonioFactory.crearDemonOjo();
                nuevoDemonOjo.setPosition(random.nextInt(Gdx.graphics.getWidth() - 100), 
                                          random.nextInt(Gdx.graphics.getHeight() - 200) + 100);
                demonOjos.add(nuevoDemonOjo);
            }
            demonNave = demonioFactory.crearDemonNave();
            ultimoCambioEstrategiaOjo = TimeUtils.millis();
            ultimoCambioEstrategiaNave = TimeUtils.millis();
            balls1.clear();
        }
        else {
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
            if (demonOjos != null) {
                for (int i = 0; i < demonOjos.size(); i++) {
                    DemonOjo demonOjo = demonOjos.get(i);
                    demonOjo.actualizar(delta, batch); // Centraliza toda la lógica de cada DemonOjo
                    if (demonOjo.getVidas() <= 0) {
                        demonOjo.morir();
                        demonOjos.remove(i--); // Eliminar demonios muertos
                    }
                }
            }

            if (demonNave != null) {
                demonNave.actualizar(delta, batch); // Centraliza toda la lógica del DemonNave
            }

            // Alternar estrategias de ataque si ya pasó el tiempo de espera
            if (TimeUtils.timeSinceMillis(inicioRonda3) > 3000) {
                if (TimeUtils.timeSinceMillis(ultimoCambioEstrategiaOjo) > INTERVALO_CAMBIO) {
                    for (DemonOjo demonOjo : demonOjos) {
                        if (demonOjo.getEstrategiaAtaque() instanceof AtaqueSimple) {
                            demonOjo.setEstrategiaAtaque(new AtaqueTriple(new Texture("DemonOjoAtaque.png"), 1, -10));
                        } else {
                            demonOjo.setEstrategiaAtaque(new AtaqueSimple(new Texture("DemonOjoAtaque.png"), 1, -10));
                        }
                    }
                    ultimoCambioEstrategiaOjo = TimeUtils.millis();
                }

                if (demonNave != null && TimeUtils.timeSinceMillis(ultimoCambioEstrategiaNave) > INTERVALO_CAMBIO) {
                    if (demonNave.getEstrategiaAtaque() instanceof AtaqueTriple) {
                        demonNave.setEstrategiaAtaque(new AtaqueSimple(new Texture("disparoSangre.png"), 3, -10));
                    } else {
                        demonNave.setEstrategiaAtaque(new AtaqueTriple(new Texture("disparoSangre.png"), 3, -10));
                    }
                    ultimoCambioEstrategiaNave = TimeUtils.millis();
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

        // Dibujar la nave
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
        if (!gameOver && ((balls1.isEmpty() && ronda != 3) || (ronda == 3 && demonOjos.isEmpty() && demonNave == null))) {
            iniciarNuevaRonda();
        }
    }

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
                for (int j = 0; j < demonOjos.size(); j++) {
                    DemonOjo demonOjo = demonOjos.get(j);
                    if (b.getHitbox().overlaps(demonOjo.getHitbox())) {
                        demonOjo.restarVida(1);
                        if (demonOjo.getVidas() <= 0) {
                            demonOjo.morir();
                            demonOjos.remove(j--); // Eliminar demonio si se queda sin vida
                        }
                        b.setDestroyed(true);
                        break;
                    }
                }

                if (demonNave != null && b.getHitbox().overlaps(demonNave.getHitbox())) {
                    demonNave.restarVida(1);
                    if (demonNave.getVidas() <= 0) {
                        demonNave.morir();
                        demonNave = null;
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

        // Actualizar y verificar colisiones de los disparos enemigos en ronda 3 solo si pasó el retraso inicial
        if (ronda == 3 && TimeUtils.timeSinceMillis(inicioRonda3) > 3000) {
            for (DemonOjo demonOjo : demonOjos) {
                for (int i = 0; i < demonOjo.getDisparos().size; i++) {
                    Bullet disparo = demonOjo.getDisparos().get(i);
                    disparo.update();
                    if (disparo.getHitbox().overlaps(nave.getHitbox())) {
                        nave.restarVida(demonOjo.getEstrategiaAtaque().getDanio());

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

            if (demonNave != null) {
                for (int i = 0; i < demonNave.getDisparos().size; i++) {
                    Bullet disparo = demonNave.getDisparos().get(i);
                    disparo.update();
                    if (disparo.getHitbox().overlaps(nave.getHitbox())) {
                        nave.restarVida(demonNave.getEstrategiaAtaque().getDanio());

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


    public Array<Bullet> getBalas() {
        Array<Bullet> balasArray = new Array<>();
        for (Bullet bala : balas) {
            balasArray.add(bala); // Agregar cada bala manualmente
        }
        return balasArray;
    }
    
        public boolean agregarBala(Bullet bb) {
        balas.add(bb); // Usar Array de libGDX
        return true;
    }
    public Array<Bullet> convertirABalasArray() {
        Array<Bullet> balasArray = new Array<>();
        for (Bullet bala : balas) {
            balasArray.add(bala);
        }
        return balasArray;
    }




    private void handleGameOver() {
        if (!gameOver) {
            gameOver = true;

            // Detener la música específica de la pantalla
            if (gameMusic != null && gameMusic.isPlaying()) {
                gameMusic.stop();
            }

            // Reproducir el sonido de muerte
            deathSound.play();

            Timer.schedule(new Task() {
                @Override
                public void run() {
                    // Actualizar la puntuación más alta en GameManager
                    if (score > GameManager.getInstance().getHighScore()) {
                        GameManager.getInstance().setHighScore(score);
                    }

                    // Cambiar a la pantalla de Game Over
                    game.setScreen(new PantallaGameOver(game, score, GameManager.getInstance().getHighScore()));
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
            demonOjo.getEstrategiaAtaque().dispose();

        }

        if (demonNave != null) {
            demonNave.getTextura().dispose();
            demonNave.getDisparoTextura().dispose();
            demonNave.dispose(); // Llama al dispose() de demonNave para liberar sus recursos
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
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;


public class PantallaJuego implements Screen {

    private SpaceNavigation game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Sound explosionSound1, explosionSound2;
    private Sound nuevaRondaSound;
    private Music gameMusic, deathSound;
    private int score;
    private int ronda;
    private int velXAsteroides;
    private int velYAsteroides;
    private int cantAsteroides;
    private long ultimoCambioEstrategiaOjo;
    private long ultimoCambioEstrategiaNave;
    private static final int INTERVALO_CAMBIO = 5000; // 5 segundos entre cambios de estrategia

    private final GameManager gameManager;
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

    private BitmapFont fontPantallaJuego;
    private final GlyphLayout layout;
    private Rectangle btnPausa;

    private DemonOjo demonOjo;
    private DemonNave demonNave;
    private long ultimoAtaqueDemonOjo;

    // Agregar referencia a la fábrica
    private final DemonioFactory demonioFactory;

    public PantallaJuego(SpaceNavigation game, int ronda, int vidas, int score,
                         int velXAsteroides, int velYAsteroides, int cantAsteroides) {
        this.game = game;
        this.gameManager = GameManager.getInstance();

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
        nuevaRondaSound = Gdx.audio.newSound(Gdx.files.internal("nuevaRonda.mp3"));
        gameMusic.setLooping(true);
        gameMusic.setVolume(0.5f);
        gameMusic.play();

        backgroundTexture = new Texture("fondoPantallaJuego.png");
        starsTexture = new Texture("parallax-space-stars.png");
        pauseButtonTexture = new Texture("pausa.png");

        fontPantallaJuego = new BitmapFont(Gdx.files.internal("letraPantallaJuego.fnt"));
        layout = new GlyphLayout();
        btnPausa = new Rectangle(10, 750, 50, 50);

        nave = new Nave4(Gdx.graphics.getWidth() / 2 - 50, 30, 
                new Texture(Gdx.files.internal("MainShip3.png")),
                Gdx.audio.newSound(Gdx.files.internal("hurt.ogg")),
                new Texture(Gdx.files.internal("Rocket2.png")),
                Gdx.audio.newSound(Gdx.files.internal("SonidoDisparoNave.mp3")));
        nave.setVidas(vidas);

        // Inicialización de la fábrica
        demonioFactory = new DemonioFactoryConcreta();

        // Inicialización de monstruos usando la fábrica
        if (ronda == 3) {
            demonOjo = demonioFactory.crearDemonOjo();
            demonNave = demonioFactory.crearDemonNave();
            ultimoCambioEstrategiaOjo = TimeUtils.millis();
            ultimoCambioEstrategiaNave = TimeUtils.millis();
            balls1.clear();
        } else {
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
                demonOjo.actualizar(delta, batch); // Centraliza toda la lógica del DemonOjo
            }

            if (demonNave != null) {
                demonNave.actualizar(delta, batch); // Centraliza toda la lógica del DemonNave
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
        
        if (ronda == 3) {
            if (demonOjo != null && TimeUtils.timeSinceMillis(ultimoCambioEstrategiaOjo) > INTERVALO_CAMBIO) {
                // Alternar estrategia de DemonOjo
                if (demonOjo.getEstrategiaAtaque() instanceof AtaqueSimple) {
                    demonOjo.setEstrategiaAtaque(new AtaqueTriple(new Texture("DemonOjoAtaque.png"), 1, -10));
                } else {
                    demonOjo.setEstrategiaAtaque(new AtaqueSimple(new Texture("DemonOjoAtaque.png"), 1, -10));
                }
                ultimoCambioEstrategiaOjo = TimeUtils.millis(); // Actualizar el tiempo del último cambio
            }

            if (demonNave != null && TimeUtils.timeSinceMillis(ultimoCambioEstrategiaNave) > INTERVALO_CAMBIO) {
                // Alternar estrategia de DemonNave
                if (demonNave.getEstrategiaAtaque() instanceof AtaqueTriple) {
                    demonNave.setEstrategiaAtaque(new AtaqueSimple(new Texture("disparoSangre.png"), 3, -10));
                } else {
                    demonNave.setEstrategiaAtaque(new AtaqueTriple(new Texture("disparoSangre.png"), 3, -10));
                }
                ultimoCambioEstrategiaNave = TimeUtils.millis(); // Actualizar el tiempo del último cambio
            }
        }
    }

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
                        nave.restarVida(demonOjo.getEstrategiaAtaque().getDanio());

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
                        nave.restarVida(demonNave.getEstrategiaAtaque().getDanio());

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
    public Array<Bullet> getBalas() {
        Array<Bullet> balasArray = new Array<>();
        for (Bullet bala : balas) {
            balasArray.add(bala); // Agregar cada bala manualmente
        }
        return balasArray;
    }
    
        public boolean agregarBala(Bullet bb) {
        balas.add(bb); // Usar Array de libGDX
        return true;
    }
    public Array<Bullet> convertirABalasArray() {
        Array<Bullet> balasArray = new Array<>();
        for (Bullet bala : balas) {
            balasArray.add(bala);
        }
        return balasArray;
    }




    private void handleGameOver() {
        if (!gameOver) {
            gameOver = true;

            // Detener la música específica de la pantalla
            if (gameMusic != null && gameMusic.isPlaying()) {
                gameMusic.stop();
            }

            // Reproducir el sonido de muerte
            deathSound.play();

            Timer.schedule(new Task() {
                @Override
                public void run() {
                    // Actualizar la puntuación más alta en GameManager
                    if (score > GameManager.getInstance().getHighScore()) {
                        GameManager.getInstance().setHighScore(score);
                    }

                    // Cambiar a la pantalla de Game Over
                    game.setScreen(new PantallaGameOver(game, score, GameManager.getInstance().getHighScore()));
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
            demonOjo.getEstrategiaAtaque().dispose();

        }

        if (demonNave != null) {
            demonNave.getTextura().dispose();
            demonNave.getDisparoTextura().dispose();
            demonNave.dispose(); // Llama al dispose() de demonNave para liberar sus recursos
        }
    }
}*/
