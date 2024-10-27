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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
    private float starsX = 0;

    private Nave4 nave;
    private ArrayList<Ball2> balls1 = new ArrayList<>();
    private ArrayList<Ball2> balls2 = new ArrayList<>();
    private ArrayList<Bullet> balas = new ArrayList<>();
    private Random random = new Random();
    
    // Variable de control para pausar el juego
    private boolean gameOver = false;

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

        // Inicializar assets
        explosionSound1 = Gdx.audio.newSound(Gdx.files.internal("explosion1.wav"));
        explosionSound2 = Gdx.audio.newSound(Gdx.files.internal("explosion2.wav"));
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("SonidoPantallaJuego.mp3"));
        deathSound = Gdx.audio.newMusic(Gdx.files.internal("SonidoMuerteFinal.mp3"));
        
        gameMusic.setLooping(true);
        gameMusic.setVolume(0.5f);
        gameMusic.play();

        // Cargar fondo y estrellas
        backgroundTexture = new Texture("fondoPantallaJuego.png");
        starsTexture = new Texture("parallax-space-stars.png");

        // Cargar nave
        nave = new Nave4(Gdx.graphics.getWidth() / 2 - 50, 30, new Texture(Gdx.files.internal("MainShip3.png")),
                        Gdx.audio.newSound(Gdx.files.internal("hurt.ogg")), 
                        new Texture(Gdx.files.internal("Rocket2.png")), 
                        Gdx.audio.newSound(Gdx.files.internal("SonidoDisparoNave.mp3"))); 
        nave.setVidas(vidas);

        // Crear asteroides con verificación de superposición
        Random r = new Random();
        for (int i = 0; i < cantAsteroides; i++) {
            boolean colisiona;
            Ball2 nuevoAsteroide = null;

            // Repetir hasta que el nuevo asteroide no colisione con los existentes
            do {
                colisiona = false;

                // Generar un asteroide en una posición aleatoria
                int tamañoMeteorito = Math.max(10, 20 + r.nextInt(10));
                int xPos = r.nextInt((int)Gdx.graphics.getWidth());
                int yPos = 50 + r.nextInt((int)Gdx.graphics.getHeight() - 50);
                nuevoAsteroide = new Ball2(xPos, yPos, tamañoMeteorito, velXAsteroides + r.nextInt(4), velYAsteroides + r.nextInt(4), 
                                           new Texture(Gdx.files.internal("aGreyMedium4.png")));

                // Comprobar si colisiona con algún asteroide ya existente
                for (Ball2 asteroideExistente : balls1) {
                    float distancia = (float) Math.sqrt(Math.pow(asteroideExistente.getX() - nuevoAsteroide.getX(), 2) +
                                                        Math.pow(asteroideExistente.getY() - nuevoAsteroide.getY(), 2));
                    if (distancia < (asteroideExistente.getRadius() + nuevoAsteroide.getRadius())) {
                        colisiona = true;
                        break;
                    }
                }
            } while (colisiona);

            // Agregar el nuevo asteroide
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

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.begin();

        // Dibujar el fondo estático
        batch.draw(backgroundTexture, 0, 0, 1200, 800);

        // Dibujar estrellas con parallax y color más oscuro
        starsX -= 50 * delta; // Velocidad de desplazamiento de las estrellas
        batch.setColor(0.2f, 0.2f, 0.5f, 1f); // Color oscuro para las estrellas
        batch.draw(starsTexture, starsX, 0, 1200, 800);
        batch.draw(starsTexture, starsX + 1200, 0, 1200, 800); // Segunda copia para bucle
        if (starsX <= -1200) starsX = 0; // Reiniciar posición
        batch.setColor(Color.WHITE); // Restaurar el color a blanco

        // Dibujar encabezado
        dibujaEncabezado();

        // Si estamos en "game over", no actualizamos los objetos
        if (!gameOver) {
            if (!nave.estaHerido()) {
                // Colisiones entre balas y asteroides y su destrucción
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
                            j--; // Ajustar el índice después de la eliminación
                            score += 10;
                            break;
                        }
                    }

                    // Si la bala está destruida, la eliminamos
                    if (b.isDestroyed()) {
                        balas.remove(i);
                        i--; // Ajustar el índice después de la eliminación
                    }
                }

                // Actualizar movimiento de asteroides dentro del área
                for (Ball2 ball : balls1) {
                    ball.update();
                }

                // Colisiones entre asteroides y sus rebotes
                for (int i = 0; i < balls1.size(); i++) {
                    Ball2 ball1 = balls1.get(i);
                    for (int j = i + 1; j < balls1.size(); j++) {
                        Ball2 ball2 = balls1.get(j);
                        ball1.checkCollision(ball2);
                    }
                }
            }

            // Dibujar balas
            for (Bullet b : balas) {
                b.draw(batch);
            }

            // Dibujar la nave
            nave.draw(batch, this);

            // Dibujar asteroides y manejar colisiones con la nave
            for (int i = 0; i < balls1.size(); i++) {
                Ball2 meteorito = balls1.get(i);
                meteorito.draw(batch);
                if (nave.checkCollision(meteorito)) {
                    balls1.remove(meteorito);
                    balls2.remove(meteorito);
                    i--; // Ajustar el índice después de la eliminación
                    if (nave.getVidas() <= 0) {
                        handleGameOver();
                        break;
                    }
                }
            }
        }

        batch.end();

        // Nivel completado
        if (!gameOver && balls1.size() == 0) {
            int nuevosAsteroides = Math.min(15, cantAsteroides + 2);
            int nuevaVelX = Math.min(15, velXAsteroides + 1);
            int nuevaVelY = Math.min(15, velYAsteroides + 1);

            Screen ss = new PantallaJuego(game, ronda + 1, nave.getVidas(), score, nuevaVelX, nuevaVelY, nuevosAsteroides);
            ss.resize(1200, 800);
            game.setScreen(ss);
            dispose();
        }
    }

    private void handleGameOver() {
        gameOver = true; // Detener el juego
        gameMusic.stop();
        deathSound.play();
        Timer.schedule(new Task() {
            @Override
            public void run() {
                if (score > game.getHighScore()) {
                    game.setHighScore(score);
                }
                Screen ss = new PantallaGameOver(game);
                ss.resize(1200, 800);
                game.setScreen(ss);
                dispose();
            }
        }, 3); // Espera de 3 segundos para terminar el sonido de muerte antes de la pantalla de Game Over
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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;

public class PantallaJuego implements Screen {

	private SpaceNavigation game;
	private OrthographicCamera camera;	
	private SpriteBatch batch;
	private Sound explosionSound;
	private Music gameMusic;
	private int score;
	private int ronda;
	private int velXAsteroides; 
	private int velYAsteroides; 
	private int cantAsteroides;
	
	private Texture backgroundTexture;
	private Texture starsTexture;
	private float starsX = 0;

	private Nave4 nave;
	private ArrayList<Ball2> balls1 = new ArrayList<>();
	private ArrayList<Ball2> balls2 = new ArrayList<>();
	private ArrayList<Bullet> balas = new ArrayList<>();

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

		// Inicializar assets
		explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.ogg"));
		explosionSound.setVolume(1,0.5f);
		gameMusic = Gdx.audio.newMusic(Gdx.files.internal("SonidoPantallaJuego.mp3"));
		gameMusic.setLooping(true);
		gameMusic.setVolume(0.5f);
		gameMusic.play();

		// Cargar fondo y estrellas
		backgroundTexture = new Texture("fondoPantallaJuego.png");
		starsTexture = new Texture("parallax-space-stars.png");

		// Cargar nave
	    nave = new Nave4(Gdx.graphics.getWidth()/2-50, 30, new Texture(Gdx.files.internal("MainShip3.png")),
	    				Gdx.audio.newSound(Gdx.files.internal("hurt.ogg")), 
	    				new Texture(Gdx.files.internal("Rocket2.png")), 
	    				Gdx.audio.newSound(Gdx.files.internal("pop-sound.mp3"))); 
        nave.setVidas(vidas);

        // Crear asteroides con verificación de superposición
        Random r = new Random();
        for (int i = 0; i < cantAsteroides; i++) {
            boolean colisiona;
            Ball2 nuevoAsteroide = null;

            // Repetir hasta que el nuevo asteroide no colisione con los existentes
            do {
                colisiona = false;

                // Generar un asteroide en una posición aleatoria
                int tamañoMeteorito = Math.max(10, 20 + r.nextInt(10));
                int xPos = r.nextInt((int)Gdx.graphics.getWidth());
                int yPos = 50 + r.nextInt((int)Gdx.graphics.getHeight() - 50);
                nuevoAsteroide = new Ball2(xPos, yPos, tamañoMeteorito, velXAsteroides + r.nextInt(4), velYAsteroides + r.nextInt(4), 
                                           new Texture(Gdx.files.internal("aGreyMedium4.png")));

                // Comprobar si colisiona con algún asteroide ya existente
                for (Ball2 asteroideExistente : balls1) {
                    float distancia = (float) Math.sqrt(Math.pow(asteroideExistente.getX() - nuevoAsteroide.getX(), 2) +
                                                        Math.pow(asteroideExistente.getY() - nuevoAsteroide.getY(), 2));
                    if (distancia < (asteroideExistente.getRadius() + nuevoAsteroide.getRadius())) {
                        colisiona = true;
                        break;
                    }
                }
            } while (colisiona);

            // Agregar el nuevo asteroide
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

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.begin();

        // Dibujar el fondo estático
        batch.draw(backgroundTexture, 0, 0, 1200, 800);

        // Dibujar estrellas con parallax y color más oscuro
        starsX -= 50 * delta; // Velocidad de desplazamiento de las estrellas
        batch.setColor(0.2f, 0.2f, 0.5f, 1f); // Color oscuro para las estrellas
        batch.draw(starsTexture, starsX, 0, 1200, 800);
        batch.draw(starsTexture, starsX + 1200, 0, 1200, 800); // Segunda copia para bucle
        if (starsX <= -1200) starsX = 0; // Reiniciar posición
        batch.setColor(Color.WHITE); // Restaurar el color a blanco

        // Dibujar encabezado
        dibujaEncabezado();

        if (!nave.estaHerido()) {
            // Colisiones entre balas y asteroides y su destrucción
            for (int i = 0; i < balas.size(); i++) {
                Bullet b = balas.get(i);
                b.update();
                for (int j = 0; j < balls1.size(); j++) {
                    Ball2 meteorito = balls1.get(j);
                    if (b.checkCollision(meteorito)) {
                        explosionSound.play();
                        balls1.remove(meteorito);
                        balls2.remove(meteorito);
                        j--; // Ajustar el índice después de la eliminación
                        score += 10;
                        break;
                    }
                }

                // Si la bala está destruida, la eliminamos
                if (b.isDestroyed()) {
                    balas.remove(i);
                    i--; // Ajustar el índice después de la eliminación
                }
            }

            // Actualizar movimiento de asteroides dentro del área
            for (Ball2 ball : balls1) {
                ball.update();
            }

            // Colisiones entre asteroides y sus rebotes
            for (int i = 0; i < balls1.size(); i++) {
                Ball2 ball1 = balls1.get(i);
                for (int j = i + 1; j < balls1.size(); j++) {
                    Ball2 ball2 = balls1.get(j);
                    ball1.checkCollision(ball2);
                }
            }
        }

        // Dibujar balas
        for (Bullet b : balas) {
            b.draw(batch);
        }

        // Dibujar la nave
        nave.draw(batch, this);

        // Dibujar asteroides y manejar colisiones con la nave
        for (int i = 0; i < balls1.size(); i++) {
            Ball2 meteorito = balls1.get(i);
            meteorito.draw(batch);
            if (nave.checkCollision(meteorito)) {
                balls1.remove(meteorito);
                balls2.remove(meteorito);
                i--; // Ajustar el índice después de la eliminación
                if (nave.estaDestruido()) {
                    break;
                }
            }
        }

        // Manejar el game over si la nave está destruida
        if (nave.estaDestruido()) {
            if (score > game.getHighScore()) {
                game.setHighScore(score);
            }
            Screen ss = new PantallaGameOver(game);
            ss.resize(1200, 800);
            game.setScreen(ss);
            dispose();
        }

        batch.end();

        // Nivel completado
        if (balls1.size() == 0) {
            int nuevosAsteroides = Math.min(15, cantAsteroides + 2);
            int nuevaVelX = Math.min(15, velXAsteroides + 1);
            int nuevaVelY = Math.min(15, velYAsteroides + 1);

            Screen ss = new PantallaJuego(game, ronda + 1, nave.getVidas(), score, nuevaVelX, nuevaVelY, nuevosAsteroides);
            ss.resize(1200, 800);
            game.setScreen(ss);
            dispose();
        }
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
        explosionSound.dispose();
        gameMusic.dispose();
        backgroundTexture.dispose();
        starsTexture.dispose();
    }
}*/


