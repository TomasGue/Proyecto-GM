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

public class PantallaJuego implements Screen {

    private SpaceNavigation game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Sound explosionSound1, explosionSound2;
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
        this.ronda = ronda;
        this.score = score;
        this.velXAsteroides = velXAsteroides;
        this.velYAsteroides = velYAsteroides;
        this.cantAsteroides = cantAsteroides;

        batch = game.getBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1200, 800);

        explosionSound1 = Gdx.audio.newSound(Gdx.files.internal("explosion1.wav"));
        explosionSound2 = Gdx.audio.newSound(Gdx.files.internal("explosion2.wav"));
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("SonidoPantallaJuego.mp3"));
        deathSound = Gdx.audio.newMusic(Gdx.files.internal("SonidoMuerteFinal.mp3"));
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) &&
                btnPausa.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()))) {
            paused = true;
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
                game.setScreen(new PantallaGameOver(game));
                dispose();
            }
        }, 3);
    }

    private void iniciarNuevaRonda() {
        int nuevosAsteroides = Math.min(15, cantAsteroides + 2);
        int nuevaVelX = Math.min(15, velXAsteroides + 1);
        int nuevaVelY = Math.min(15, velYAsteroides + 1);

        game.setScreen(new PantallaJuego(game, ronda + 1, nave.getVidas(), score, nuevaVelX, nuevaVelY, nuevosAsteroides));
        dispose();
    }

    public boolean agregarBala(Bullet bb) {
        return balas.add(bb);
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
        backgroundTexture.dispose();
        starsTexture.dispose();
        pauseButtonTexture.dispose();
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
        this.ronda = ronda;
        this.score = score;
        this.velXAsteroides = velXAsteroides;
        this.velYAsteroides = velYAsteroides;
        this.cantAsteroides = cantAsteroides;

        batch = game.getBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1200, 800);

        explosionSound1 = Gdx.audio.newSound(Gdx.files.internal("explosion1.wav"));
        explosionSound2 = Gdx.audio.newSound(Gdx.files.internal("explosion2.wav"));
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("SonidoPantallaJuego.mp3"));
        deathSound = Gdx.audio.newMusic(Gdx.files.internal("SonidoMuerteFinal.mp3"));
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
    }

    private void generarAsteroides() {
        Random r = new Random();
        balls1.clear();  // Limpiar lista de asteroides para asegurarnos que están vacíos al iniciar
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) &&
                btnPausa.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()))) {
            paused = true;
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
                game.setScreen(new PantallaGameOver(game));
                dispose();
            }
        }, 3);
    }

    private void iniciarNuevaRonda() {
        int nuevosAsteroides = Math.min(15, cantAsteroides + 2);
        int nuevaVelX = Math.min(15, velXAsteroides + 1);
        int nuevaVelY = Math.min(15, velYAsteroides + 1);

        game.setScreen(new PantallaJuego(game, ronda + 1, nave.getVidas(), score, nuevaVelX, nuevaVelY, nuevosAsteroides));
        dispose();
    }

    public boolean agregarBala(Bullet bb) {
        return balas.add(bb);
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
        backgroundTexture.dispose();
        starsTexture.dispose();
        pauseButtonTexture.dispose();
    }
}*/






































/*package puppy.code;

import java.util.ArrayList;
import java.util.Random;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class PantallaJuego implements Screen {

    private SpaceNavigation game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Sound explosionSound1, explosionSound2;
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
    private ArrayList<Ball2> balls2 = new ArrayList<>();
    private ArrayList<Bullet> balas = new ArrayList<>();
    private Random random = new Random();

    private boolean gameOver = false;
    private boolean gamePaused = false;

    // Área de detección del botón de pausa
    private Rectangle pauseButtonBounds;

    public PantallaJuego(SpaceNavigation game, int ronda, int vidas, int score,
                         int velXAsteroides, int velYAsteroides, int cantAsteroides) {
        this.game = game;
        this.ronda = ronda;
        this.score = score;
        this.velXAsteroides = velXAsteroides;
        this.velYAsteroides = velYAsteroides;
        this.cantAsteroides = cantAsteroides;

        batch = game.getBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 640);

        explosionSound1 = Gdx.audio.newSound(Gdx.files.internal("explosion1.wav"));
        explosionSound2 = Gdx.audio.newSound(Gdx.files.internal("explosion2.wav"));
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("SonidoPantallaJuego.mp3"));
        deathSound = Gdx.audio.newMusic(Gdx.files.internal("SonidoMuerteFinal.mp3"));

        gameMusic.setLooping(true);
        gameMusic.setVolume(0.5f);
        gameMusic.play();

        backgroundTexture = new Texture("fondoPantallaJuego.png");
        starsTexture = new Texture("parallax-space-stars.png");
        pauseButtonTexture = new Texture("pausa.png");

        pauseButtonBounds = new Rectangle(20, Gdx.graphics.getHeight() - 60, 40, 40);

        nave = new Nave4(Gdx.graphics.getWidth() / 2 - 50, 30, new Texture(Gdx.files.internal("MainShip3.png")),
                Gdx.audio.newSound(Gdx.files.internal("hurt.ogg")),
                new Texture(Gdx.files.internal("Rocket2.png")),
                Gdx.audio.newSound(Gdx.files.internal("SonidoDisparoNave.mp3")));
        nave.setVidas(vidas);

        Random r = new Random();
        for (int i = 0; i < cantAsteroides; i++) {
            boolean colisiona;
            Ball2 nuevoAsteroide = null;

            do {
                colisiona = false;
                int tamañoMeteorito = Math.max(10, 20 + r.nextInt(10));
                int xPos = r.nextInt((int) Gdx.graphics.getWidth());
                int yPos = 50 + r.nextInt((int) Gdx.graphics.getHeight() - 50);
                nuevoAsteroide = new Ball2(xPos, yPos, tamañoMeteorito, velXAsteroides + r.nextInt(4), velYAsteroides + r.nextInt(4),
                        new Texture(Gdx.files.internal("aGreyMedium4.png")));

                for (Ball2 asteroideExistente : balls1) {
                    float distancia = (float) Math.sqrt(Math.pow(asteroideExistente.getX() - nuevoAsteroide.getX(), 2) +
                            Math.pow(asteroideExistente.getY() - nuevoAsteroide.getY(), 2));
                    if (distancia < (asteroideExistente.getRadius() + nuevoAsteroide.getRadius())) {
                        colisiona = true;
                        break;
                    }
                }
            } while (colisiona);

            balls1.add(nuevoAsteroide);
            balls2.add(nuevoAsteroide);
        }
    }

    public void dibujaEncabezado() {
        CharSequence str = "Vidas: " + nave.getVidas() + " Ronda: " + ronda;
        game.getFont().getData().setScale(2f);
        game.getFont().draw(batch, str, 10, 30);
        game.getFont().draw(batch, "Score: " + this.score, Gdx.graphics.getWidth() - 150, 30);
        game.getFont().draw(batch, "HighScore: " + game.getHighScore(), Gdx.graphics.getWidth() / 2 - 100, 30);
    }
    private void iniciarNuevaRonda() {
    int nuevosAsteroides = Math.min(15, cantAsteroides + 2);
    int nuevaVelX = Math.min(15, velXAsteroides + 1);
    int nuevaVelY = Math.min(15, velYAsteroides + 1);

    // Crear la nueva instancia de PantallaJuego con la nueva ronda y el puntaje actualizado
    Screen nuevaRonda = new PantallaJuego(game, ronda + 1, nave.getVidas(), score, nuevaVelX, nuevaVelY, nuevosAsteroides);
    game.setScreen(nuevaRonda);
    dispose();  // Liberar recursos de la ronda actual
}


    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (Gdx.input.isTouched() && pauseButtonBounds.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()))) {
            gamePaused = !gamePaused;
        }

        if (!gamePaused) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            batch.begin();
            batch.draw(backgroundTexture, 0, 0, 1200, 800);

            starsX -= 50 * delta;
            batch.setColor(0.2f, 0.2f, 0.5f, 1f);
            batch.draw(starsTexture, starsX, 0, 1200, 800);
            batch.draw(starsTexture, starsX + 1200, 0, 1200, 800);
            if (starsX <= -1200) starsX = 0;
            batch.setColor(Color.WHITE);

            dibujaEncabezado();
            batch.draw(pauseButtonTexture, pauseButtonBounds.x, pauseButtonBounds.y, pauseButtonBounds.width, pauseButtonBounds.height);

            if (!gameOver) {
                if (!nave.estaHerido()) {
                    for (int i = 0; i < balas.size(); i++) {
                        Bullet b = balas.get(i);
                        b.update();
                        for (int j = 0; j < balls1.size(); j++) {
                            Ball2 meteorito = balls1.get(j);
                            if (b.checkCollision(meteorito)) {
                                if (random.nextBoolean()) {
                                    explosionSound1.play();
                                } else {
                                    explosionSound2.play();
                                }
                                balls1.remove(meteorito);
                                balls2.remove(meteorito);
                                j--;
                                score += 10;
                                break;
                            }
                        }
                        if (b.isDestroyed()) {
                            balas.remove(i);
                            i--;
                        }
                    }

                    for (Ball2 ball : balls1) {
                        ball.update();
                    }

                    for (int i = 0; i < balls1.size(); i++) {
                        Ball2 ball1 = balls1.get(i);
                        for (int j = i + 1; j < balls1.size(); j++) {
                            Ball2 ball2 = balls1.get(j);
                            ball1.checkCollision(ball2);
                        }
                    }
                }

                for (Bullet b : balas) {
                    b.draw(batch);
                }

                nave.draw(batch, this);

                for (int i = 0; i < balls1.size(); i++) {
                    Ball2 meteorito = balls1.get(i);
                    meteorito.draw(batch);
                    if (nave.checkCollision(meteorito)) {
                        balls1.remove(meteorito);
                        balls2.remove(meteorito);
                        i--;
                        if (nave.getVidas() <= 0) {
                            handleGameOver();
                            break;
                        }
                    }
                }
            }
            batch.end();

            if (!gameOver && balls1.isEmpty()) {
                iniciarNuevaRonda();
            }
        } else {
            Gdx.gl.glClearColor(0, 0, 0, 0.5f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            batch.begin();
            game.getFont().draw(batch, "Juego Pausado", Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2 + 100);
            game.getFont().draw(batch, "Presione ESC o el boton de Pausa para continuar", Gdx.graphics.getWidth() / 2 - 200, Gdx.graphics.getHeight() / 2);
            batch.end();
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
                game.setScreen(new PantallaGameOver(game));
                dispose();
            }
        }, 3);
    }

    public boolean agregarBala(Bullet bb) {
        return balas.add(bb);
    }

    @Override
    public void show() {
        gameMusic.play();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        explosionSound1.dispose();
        explosionSound2.dispose();
        gameMusic.dispose();
        deathSound.dispose();
        backgroundTexture.dispose();
        starsTexture.dispose();
        pauseButtonTexture.dispose();
    }
}*/
