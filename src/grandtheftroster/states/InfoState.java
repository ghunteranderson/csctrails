package grandtheftroster.states;

import grandtheftroster.elements.GlyphFont;
import grandtheftroster.handlers.GameStateManager;
import grandtheftroster.handlers.MyInput;
import grandtheftroster.main.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;


public class InfoState extends GameState {

	BitmapFont font;
	GlyphFont gfont;
	
	public InfoState(GameStateManager gsm) {
		super(gsm, "Game Won");
		font = new BitmapFont();
		gfont = new GlyphFont("res/images/retro font.png", 8);
	}

	@Override
	public void handleInput() {
		if(MyInput.isPressed(MyInput.BUTTON_ENTER)) gsm.setState(GameStateManager.MAIN_MENU);
	}

	@Override
	public void update(float dt) {
		handleInput();

	}

	@Override
	public void render() {
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		sb.setProjectionMatrix(camera.combined);
		sb.begin();
			gfont.draw(sb, "Information Screen!", GlyphFont.COLOR_WHITE, Game.V_WIDTH/2-100, Game.V_HEIGHT/2);
		sb.end();

	}

}