package grandtheftroster.states;


import static grandtheftroster.elements.B2DVars.PPM;
import grandtheftroster.elements.Boss;
import grandtheftroster.elements.GlyphFont;
import grandtheftroster.elements.Model;
import grandtheftroster.elements.ModelLoader;
import grandtheftroster.elements.Thrower;
import grandtheftroster.elements.Thrown;
import grandtheftroster.handlers.GameStateManager;
import grandtheftroster.handlers.Lvl1ContactListener;
import grandtheftroster.handlers.MyInput;
import grandtheftroster.main.Game;
import grandtheftroster.player.Player;
import grandtheftroster.utilities.Configuration;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;



/**
 * PlayState is the game-state where most of the game play takes
 * place. It is ran using the event listeners declared by
 * its super class, GameState to run the game-state. See
 * in-line comment headers for more details
 */
public class Lvl1State extends GameState {
	
	private static Configuration cfg;
	
	static{
		cfg = new Configuration();
		cfg.loadConfiguration("res/config/paths/");
	}
	
	//Box2D Fields
	private World world;
	@SuppressWarnings("unused")
	private Box2DDebugRenderer b2dDebugRenderer;
	private OrthographicCamera b2dCamera;
	private Lvl1ContactListener cl;
	
	//Model Fields
	private Thrower thrower;
	private Player player;

	//tiled
	private TiledMap map;
	private OrthogonalTiledMapRenderer tmr;


	public Lvl1State(GameStateManager gsm) {
		super(gsm, "Play");
		
		//Box2D World
		world = new World(new Vector2(0f, -3f), false);
		cl = new Lvl1ContactListener();
		b2dDebugRenderer = new Box2DDebugRenderer(); // Used to render Box2D world when developing - gha 15.9.20
		world.setContactListener(cl);
		b2dCamera = new OrthographicCamera();
		b2dCamera.setToOrtho(false, Game.V_WIDTH*Game.SCALE/PPM, Game.V_HEIGHT*Game.SCALE/PPM);
		
		
		
		//Load TileMap
		if(cfg.hasProperty("LEVEL_1@PATHS:MAPS")){
			map = new TmxMapLoader().load(cfg.getProperty("LEVEL_1@PATHS:MAPS"));
			//Load Tile Map Tile Layers
			TiledMapTileLayer tmPlatformLeft = (TiledMapTileLayer) map.getLayers().get("platforms_left");
			TiledMapTileLayer tmPlatformRight = (TiledMapTileLayer) map.getLayers().get("platforms_right");
			TiledMapTileLayer tmLadders = (TiledMapTileLayer) map.getLayers().get("ladders");
			//Load Models
			ModelLoader.tiledMapLoader(tmPlatformLeft, world, "MODEL:PLATFORM_GROUND", "left,ground");
			ModelLoader.tiledMapLoader(tmPlatformLeft, world, "MODEL:PLATFORM_CEILING", "left,ceiling");
			ModelLoader.tiledMapLoader(tmPlatformRight, world, "MODEL:PLATFORM_GROUND", "right,ground");
			ModelLoader.tiledMapLoader(tmPlatformRight, world, "MODEL:PLATFORM_CEILING", "right,ceiling");
			ModelLoader.tiledMapLoader(tmLadders, world, "MODEL:LADDER", "");
	
		} else{
			map = new TiledMap();
		}
		tmr = new OrthogonalTiledMapRenderer(map);
		tmr.setView(camera);
		
		//Boundaries
		new Model(world, "MODEL:BOUNDARY_SIDES");
		new Model(world, "MODEL:BOUNDARY_BOTTOM");
		//Key
		models.add(new Model(world, "MODEL:ROSTER", 16*28, 16*33));
		//Boss
		Boss b = new Boss(world, Boss.PUSHINGLEFT, Game.V_WIDTH-16*13, Game.V_HEIGHT-16*7-24);
		models.add(b);
		b.update((2.5f)/9*5);
		models.add(new Model(world, "MODEL:CHAIR",Game.V_WIDTH-16*12, Game.V_HEIGHT-16*9-7));
		//Player
		player = new Player(world, "MODEL:PLAYER", 16*5, 16*37);
		models.add(player);
		//Thrower
		thrower = new Thrower(world, 16, 2.5f);
		thrower.setPosistion(Game.V_WIDTH-16*14, 16*30);
		models.add(thrower.throwObject(Game.V_WIDTH-16*14, 16*30));
		models.add(thrower.throwObject(16*24, 16*11));
		models.add(thrower.throwObject(16*14, 16*11));
		models.add(thrower.throwObject(16*24, 16*15));
		models.add(thrower.throwObject(16*14, 16*15));
		models.add(thrower.throwObject(16*24, 16*19));
		models.add(thrower.throwObject(16*14, 16*19));
		models.add(thrower.throwObject(16*24, 16*23));
		models.add(thrower.throwObject(16*14, 16*23));
		models.add(thrower.throwObject(16*24, 16*28));
		models.add(thrower.throwObject(16*14, 16*28));
		models.add(thrower.throwObject(16*14, 16*32));
		
		
		//Load and begin music
		playlist.play("Level 1");

	}

				
	public void handleInput() {	
		if(MyInput.isPressed(MyInput.BUTTON_ESC)) {
			game.shutdown();
		}
	}
	
	public void update(float dt) {
		//dt is the time since update was last ran - gha 15.9.25
		handleInput();
		world.step(dt, 6, 2);
		hud.modifyTime(dt);
		handleInput();
		
		//Clean up inactive models
		ArrayList<Model> modelsToDestroy = Model.getDestoryList();
		if(modelsToDestroy.size() > 0){
			for(Model model:modelsToDestroy){
				if(model.destory() && models.contains(model)){ models.remove(model); }
			}
			Model.clearDestoryList();
		}
		
		for(Model m:models){
			m.update(dt);
		}
		
		//Try to Throw Something
		Thrown t = thrower.update(dt);
		if(t != null){ models.add(t); }
		
		//Has Player won? if so add 1 to the classes score
		if(cl.getGameWon()){
			hud.modifyClassScore(+1);
			//gsm.setState(GameStateManager.GAME_WON); 
			gsm.setState(GameStateManager.LEVEL_TWO);
		}
		
		//See if player has died and if so adds one to the anky score
		if(!player.isAlive() && hud.getLives()>1){
			player.revive();
			hud.modifyLives(-1);
			player.getBody().setTransform(16*5/PPM, 16*7/PPM, 0);
			player.getBody().setLinearVelocity(0, 0);
		}
		else if(!player.isAlive() && hud.getLives()<=1){
			hud.modifyAnky(+1);
			gsm.setState(GameStateManager.GAME_OVER);
			
		}

	}
	
	public void render() {
		// Clear previous screen
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);

		tmr.render();
		
		//SpriteBatch to camera
		sb.setProjectionMatrix(camera.combined);
		sb.begin();		
		for(Model i:models){
			i.draw(sb);
		}
		sb.end();
		
		//SpriteBatch to hudCam
		sb.setProjectionMatrix(hudCam.combined);
		sb.begin();
		gfont16.draw("Time " + (int) hud.getTime(), GlyphFont.COLOR_WHITE, 8+64, Game.V_HEIGHT-20-64); //-10 originally
		gfont16.draw(hud.getScore(), GlyphFont.COLOR_WHITE, 64+16*13, Game.V_HEIGHT-20-64); // -10 originally
		gfont16.draw("Lives " + hud.getLives(), GlyphFont.COLOR_WHITE, Game.V_WIDTH-64-16*8, Game.V_HEIGHT-20-64); // -10 originally
		sb.draw(cabFrame, 0, 0);
		sb.end();
				
		// Render Box2d world - development purposes only
		//b2dDebugRenderer.render(world, b2dCamera.combined);
	}
	
	public void dispose(){
		playlist.stop("Level 1");
	}
}









