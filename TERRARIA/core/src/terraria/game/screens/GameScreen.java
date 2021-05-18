package terraria.game.screens;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import terraria.game.actors.entities.Entity;
import terraria.game.actors.entities.EntityLoader;
import terraria.game.actors.world.GameMap;
import terraria.game.actors.world.GeneratorMap.MapLoader;
import terraria.game.actors.world.ParallaxBackground;
import terraria.game.actors.world.TileType;
import java.util.ArrayList;


public class GameScreen extends ScreenAdapter {

    private final Game game;
    private Stage stage;
    private OrthographicCamera camera;


    //Acteurs//
    ParallaxBackground parallaxBackground;
    GameMap gameMap;
    ImageButton exitButton;
    Boolean isMenuShow = false;

    protected ArrayList<Entity> entities;
    Entity player;

    public GameScreen(final Game game) {
        this.game = game;
        //Initialisation du stage et de la camera//
        stage = new Stage(new ScreenViewport());
        camera = (OrthographicCamera) stage.getViewport().getCamera();

        TextureRegion exit = new TextureRegion(new Texture(Gdx.files.internal("background/exit.png")));
        TextureRegion exitPressed = new TextureRegion(new Texture(Gdx.files.internal("background/exitPressed.png")));
        exitButton = new ImageButton( new TextureRegionDrawable(exit), new TextureRegionDrawable(exitPressed));
        exitButton.setPosition(stage.getViewport().getScreenWidth()/2,(stage.getViewport().getScreenHeight()/2)-exit.getRegionHeight(), Align.center);
        exitButton.addListener(new ActorGestureListener() {

            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                super.tap(event, x, y, count, button);
                dispose();
                EntityLoader.saveEntities("test", entities);
                MapLoader.saveMap(gameMap.getId(), gameMap.getName(), gameMap.getMap(), gameMap.getStartingPoint());
                game.setScreen(new MainMenuScreen(game));
            }
        });

        //Initalisation des acteurs//

        /*Initialisation de l'arrière plan*/

        Array<Texture> texturesParallax = new Array<Texture>();
        for(int i = 1; i < 4;i++){
            texturesParallax.add(new Texture(Gdx.files.internal("parallax/img"+i+".png")));
            texturesParallax.get(texturesParallax.size-1).setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        }

        this.parallaxBackground = new ParallaxBackground(texturesParallax, false);
        parallaxBackground.setSize(0,0);
        parallaxBackground.setSpeed(1);

        /*Initialisation de la map*/
        this.gameMap = new GameMap(this);


        entities = new ArrayList<Entity>();
        entities = EntityLoader.loadEntities("test", gameMap, this);


        //On ajoute nos acteurs//

        stage.addActor(parallaxBackground);
        stage.addActor(gameMap);


        for(Entity entity : entities ){
            stage.addActor(entity);
        }
        stage.addActor(exitButton);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        for (Entity entity : entities) {
            entity.update(delta, -9.8f, camera);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            EntityLoader.saveEntities("test", entities);
            MapLoader.saveMap(gameMap.getId(), gameMap.getName(), gameMap.getMap(), gameMap.getStartingPoint());
        }

        if (Gdx.input.justTouched()) {
            if (!isMenuShow)
                blocAction();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (isMenuShow) {
                isMenuShow = false;
            } else {
                isMenuShow = true;
            }
        }
        if (isMenuShow) {
            exitButton.setPosition(camera.position.x,camera.position.y, Align.center);
        } else {
            exitButton.setPosition(0,0, Align.center);
        }


        this.parallaxBackground.update(camera, stage);
        this.gameMap.update(camera, stage);
        stage.act(delta);
        stage.draw();

    }

    public void blocAction() {
        Vector3 pos = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        Vector3 coordinate = gameMap.getTileCoordinateByLocation(1,pos.x, pos.y);
        if (coordinate != null) {
            if (gameMap.presentTile(coordinate) ) {
                gameMap.destroyTile(coordinate);
            } else {
                gameMap.addTile(coordinate);
                if (DoesRectCollideWithMap(entities.get(0).getX(), entities.get(0).getY(), (int) entities.get(0).getWidth(), (int) entities.get(0).getHeight())) {
                    gameMap.destroyTile(coordinate);
                }
            }
        }
    }


    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width,height,true );
        parallaxBackground.setSize(stage.getViewport().getWorldWidth(),stage.getViewport().getWorldHeight());
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    public void dispose() {
        stage.dispose();
    }

    /**
     * vrais si la case de coordonnée x,y est un obstacle, faut sinon
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public boolean DoesRectCollideWithMap(float x, float y, int width, int height){
        if (x < 0 || y < 0 || x + width > getPixelWidth() || y + height > getPixelHeight()){
            return true;
        }
        for (int row = (int) (y / TileType.TILE_SIZE); row < Math.ceil((y + height ) / TileType.TILE_SIZE); row++) {
            for (int col = (int) (x / TileType.TILE_SIZE); col < Math.ceil((x +width ) / TileType.TILE_SIZE); col++) {
                for (int layer = 0; layer < getLayers(); layer++) {
                    TileType type = getTileTypeByCoordinate(layer, col, row);
                    if (type != null && type.isCollidable()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * retourne le type d'une case en fonction de ses coordonnées
     * @param layer
     * @param col
     * @param row
     * @return
     */
    public TileType getTileTypeByCoordinate(int layer, int col, int row) {
        if (col < 0 || col >= getWidth() || row < 0 || row >= getHeight())
            return null;

        int id = gameMap.getMap()[layer][getHeight() - row - 1][col];
        if(id == 0){return null;}
        return TileType.getTileTypeById(id);
    }


    public int getWidth() {return gameMap.getMap()[0][0].length; }
    public int getHeight() {return gameMap.getMap()[0].length;}
    public int getLayers() {return gameMap.getMap().length;}
    public int getPixelWidth(){return this.getWidth() * TileType.TILE_SIZE;}
    public int getPixelHeight(){return this.getHeight() * TileType.TILE_SIZE; }
}
