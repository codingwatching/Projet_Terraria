package terraria.game.actors.Inventory;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import terraria.game.TerrariaGame;

public class Inventory extends Actor {

    private TerrariaGame game;
    private static final int SLOTINVENTORYBAR = 10;     //Nombre d'objet dans la barre d'inventaire
    private static final int SIZEINVENTORY = 50;        //Nombre d'objet total de l'inventaire
    private static int currentItems = 0;                //Numéro de l'items actuellement sélectionné
    private ArrayList<Items> itemsList;                 //La liste des objets de l'inventaire
    private ArrayList<ItemsGraphic> itemsGraphic;       //La liste la classe qui gère les textures des objets de l'inventaire
    private boolean inventoryShow;                      //Boolean qui détermine si l'inventaire est affiché ou non
    private TextureRegion[][] slot;                     //Texture de chaque slot d'inventaire
    private TextureRegion[][] hoverTexture;                  //Texture quand on passe la souris sur un slot en drag & drop

    private DragAndDrop dragAndDrop;                    //Drag & Drop de l'inventaire
    private float ScreenX, ScreenY,ScreenWidth,ScreenHeight;     //Taille de l'écran
    private int  width = 50, height = 50;                //Taille d'un slot

    public Inventory(TerrariaGame game) {
        this.game = game;
        this.itemsList = new ArrayList<>();
        this.itemsGraphic = new ArrayList<>();

        this.dragAndDrop = new DragAndDrop();
        this.inventoryShow = false;
        this.slot = TextureRegion.split(game.getAssetManager().get("inventory/slot.png", Texture.class), width, height);
        this.hoverTexture = TextureRegion.split(game.getAssetManager().get("inventory/hover.png", Texture.class), width, height);
        //On crée les items de l'inventaire
        for (int i = 0; i < SIZEINVENTORY; i++) {
            itemsList.add(new Items(i));
            itemsGraphic.add(new ItemsGraphic(game, itemsList.get(i), this, dragAndDrop));

        }
    }

    public void update(Camera camera, Stage stage){
        Vector3 vec = camera.position;
        ScreenX =  vec.x + stage.getViewport().getScreenWidth()/2 - SLOTINVENTORYBAR * width;
        ScreenY = vec.y - stage.getViewport().getScreenHeight()/2;
        ScreenWidth =   stage.getViewport().getScreenWidth();
        ScreenHeight = stage.getViewport().getScreenHeight();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        int nbItems = 0;
        //On dessine les slots de la barre d'inventaire
        for (int i = 0; i < SLOTINVENTORYBAR; i++){
            if (currentItems == i) {
                batch.draw(slot[0][1], ScreenX + width *  i - (width/2), ScreenY + ScreenHeight - (height + height/2));
            } else {
                batch.draw(slot[0][0], ScreenX + width *  i - (width/2), ScreenY + ScreenHeight - (height + height/2));
            }
            if (itemsGraphic.get(i).isHover())
                batch.draw(hoverTexture[0][0], ScreenX + width *  itemsGraphic.get(i).getYPosition() - (width/2), ScreenY + ScreenHeight - (itemsGraphic.get(i).getXPosition()*height+height+height/2));
            nbItems++;
        }

        //Si l'inventaire est affiché
        if (inventoryShow) {
            for (int i = 1; i < 5; i++) {
                for (int j = 0; j < SLOTINVENTORYBAR; j++) {
                    batch.draw(slot[0][0], ScreenX + width * j - (width / 2), ScreenY + ScreenHeight - (i*height + height + height / 2));
                    if (itemsGraphic.get(nbItems).isHover())
                        batch.draw(hoverTexture[0][0], ScreenX + width *  j - (width/2), ScreenY + ScreenHeight - (i*height+height+height/2));
                    nbItems++;
                }
            }
        }


    }

    /**
     * Ajoute un élément à un slot, s'il existe un slot contenant déjà un élément
     * du même type et qu'il y a moins de 64 éléments dans ce slot. Dans le cas contraire
     * on va chercher s'il existe un slot vide pouvant acceuilir cet élement. 
     * @param idTile
     * @return
     */
    public boolean addTileInInventory(int idTile) {
        for(Items t : itemsList) {
            if(idTile == t.getIdTile() && t.getAmount() < 64) {
                t.incrAmount();
                return true;
            }
        }
        for(Items t2 : itemsList) {
            if(t2.getIdTile() == 0) {
                t2.setIdTile(idTile);
                t2.incrAmount();
                return true;
            }
        }

        return false;
    }


    /**
     * Enlève un élément de l'inventaire et supprime complètement l'élément du slot
     * quand le nombre d'élement descend à 0.
     */
    public void delTileInInventory() {
        Items currentSlot = itemsList.get(currentItems);
        if(currentSlot.getAmount() == 1) {
            currentSlot.lastElement();
        } else {
            currentSlot.decrAmount();
        }
    }

    public void fillInventory(ArrayList<Items> inv) {
        int indice = 0;
        if (inv != null) {
            for(Items i : inv) {
                this.itemsList.set(indice, i);
                this.getGraphicItems().set(indice, new ItemsGraphic(game, this.itemsList.get(indice),this, dragAndDrop));
                indice++;
            }
        }
    }

    public int getCurrentItems() {
        return currentItems;
    }

    public static void setCurrentItems(int tile) {
        currentItems = tile;
    }

    public ArrayList<Items> getItemsList() {
        return itemsList;
    }

    public ArrayList<ItemsGraphic> getGraphicItems() {
        return itemsGraphic;
    }

    public boolean isInventoryOpen() {
        return this.inventoryShow;
    }

    public boolean isInventoryShow() {
        return inventoryShow;
    }

    public void setInventoryShow(boolean inventoryShow) {
        this.inventoryShow = inventoryShow;
    }

    public int getWidthTile() {
        return width;
    }

    public void setWidthTile(int width) {
        this.width = width;
    }

    public int getHeightTile() {
        return height;
    }

    public void setHeightTile(int height) {
        this.height = height;
    }
}