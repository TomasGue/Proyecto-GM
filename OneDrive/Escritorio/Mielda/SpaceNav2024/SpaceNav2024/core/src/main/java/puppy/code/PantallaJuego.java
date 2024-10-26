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
	
	private Nave4 nave;
	private  ArrayList<Ball2> balls1 = new ArrayList<>();
	private  ArrayList<Ball2> balls2 = new ArrayList<>();
	private  ArrayList<Bullet> balas = new ArrayList<>();


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
		//inicializar assets; musica de fondo y efectos de sonido
		explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.ogg"));
		explosionSound.setVolume(1,0.5f);
		gameMusic = Gdx.audio.newMusic(Gdx.files.internal("piano-loops.wav")); //
		
		gameMusic.setLooping(true);
		gameMusic.setVolume(0.5f);
		gameMusic.play();
		
	    // cargar imagen de la nave, 64x64   
	    nave = new Nave4(Gdx.graphics.getWidth()/2-50,30,new Texture(Gdx.files.internal("MainShip3.png")),
	    				Gdx.audio.newSound(Gdx.files.internal("hurt.ogg")), 
	    				new Texture(Gdx.files.internal("Rocket2.png")), 
	    				Gdx.audio.newSound(Gdx.files.internal("pop-sound.mp3"))); 
        nave.setVidas(vidas);
        //crear asteroides
        // Crear asteroides con verificación de superposición
        Random r = new Random();
            for (int i = 0; i < cantAsteroides; i++) {
                boolean colisiona;
                Ball2 nuevoAsteroide = null;

                // Repetir hasta que el nuevo asteroide no colisione con los existentes
                do {
                    colisiona = false; // Asumimos que no colisiona hasta comprobar

                    // Generar un asteroide en una posición aleatoria
                    int tamañoMeteorito = Math.max(10, 20 + r.nextInt(10)); // Generar tamaño de meteorito
                    int xPos = r.nextInt((int)Gdx.graphics.getWidth());
                    int yPos = 50 + r.nextInt((int)Gdx.graphics.getHeight() - 50);
                    nuevoAsteroide = new Ball2(xPos, yPos, tamañoMeteorito, velXAsteroides + r.nextInt(4), velYAsteroides + r.nextInt(4), 
                                               new Texture(Gdx.files.internal("aGreyMedium4.png")));

                    // Comprobar si colisiona con algún asteroide ya existente
                    for (Ball2 asteroideExistente : balls1) {
                        float distancia = (float) Math.sqrt(Math.pow(asteroideExistente.getX() - nuevoAsteroide.getX(), 2) +
                                                            Math.pow(asteroideExistente.getY() - nuevoAsteroide.getY(), 2));
                        if (distancia < (asteroideExistente.getRadius() + nuevoAsteroide.getRadius())) {
                            colisiona = true; // Si están demasiado cerca, volver a generar posición
                            break;
                        }
                    }
                } while (colisiona);

                // Agregar el nuevo asteroide una vez que esté en una posición válida
                balls1.add(nuevoAsteroide);
                balls2.add(nuevoAsteroide);
            }
        }
       
        /*Random r = new Random();
	    for (int i = 0; i < cantAsteroides; i++) {
	        Ball2 bb = new Ball2(r.nextInt((int)Gdx.graphics.getWidth()),
	  	            50+r.nextInt((int)Gdx.graphics.getHeight()-50),
	  	            20+r.nextInt(10), velXAsteroides+r.nextInt(4), velYAsteroides+r.nextInt(4), 
	  	            new Texture(Gdx.files.internal("aGreyMedium4.png")));	   
	  	    balls1.add(bb);
	  	    balls2.add(bb);
	  	}
	}*/
    
	public void dibujaEncabezado() {
            // Depuración: Verificar inicialización de objetos
            if (game == null) {
                System.out.println("Error: El objeto 'game' es null.");
                return;
            }
            if (batch == null) {
                System.out.println("Error: El objeto 'batch' es null.");
                return;
            }
            if (game.getFont() == null) {
                System.out.println("Error: La fuente de 'game' es null.");
                return;
            }
            if (nave == null) {
                System.out.println("Error: El objeto 'nave' es null.");
                return;
            }

            // Continuar con el dibujo si todo está inicializado
            CharSequence str = "Vidas: " + nave.getVidas() + " Ronda: " + ronda;
            game.getFont().getData().setScale(2f);
            game.getFont().draw(batch, str, 10, 30);
            game.getFont().draw(batch, "Score: " + this.score, Gdx.graphics.getWidth() - 150, 30);
            game.getFont().draw(batch, "HighScore: " + game.getHighScore(), Gdx.graphics.getWidth() / 2 - 100, 30);



		/*CharSequence str = "Vidas: "+nave.getVidas()+" Ronda: "+ronda;
		game.getFont().getData().setScale(2f);		
		game.getFont().draw(batch, str, 10, 30);
		game.getFont().draw(batch, "Score:"+this.score, Gdx.graphics.getWidth()-150, 30);
		game.getFont(*/
	}
	@Override
        public void render(float delta) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
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
                            balls1.remove(meteorito); // Eliminar por referencia
                            balls2.remove(meteorito); // Eliminar por referencia
                            j--; // Ajustar el índice después de la eliminación
                            score += 10;
                            break; // Salimos después de eliminar el meteorito
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
                        ball1.checkCollision(ball2); // Colisiones entre meteoritos
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
                    balls1.remove(meteorito); // Eliminar por referencia
                    balls2.remove(meteorito); // Eliminar por referencia
                    i--; // Ajustar el índice después de la eliminación
                    if (nave.estaDestruido()) {
                        break; // Salir si la nave está destruida
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
                // Incrementa en 2 meteoritos por ronda, con un límite máximo de 15 meteoritos
                int nuevosAsteroides = Math.min(15, cantAsteroides + 2); // Limitar el incremento a 15 meteoritos
                int nuevaVelX = Math.min(15, velXAsteroides + 1); // Limitar la velocidad en X a un máximo de 15
                int nuevaVelY = Math.min(15, velYAsteroides + 1); // Limitar la velocidad en Y a un máximo de 15

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
		// TODO Auto-generated method stub
		gameMusic.play();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		this.explosionSound.dispose();
		this.gameMusic.dispose();
	}
   
}
