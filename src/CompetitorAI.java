import java.util.Collection;
import java.util.List;

import org.bonzai.api.gamestate.GameState;
import org.bonzai.api.position.Position;
import org.bonzai.cli.BonzaiTeamsCommand;
import org.bonzai.elements.api.ElementsFilters;
import org.bonzai.elements.api.Elements;
import org.bonzai.elements.api.ai.ElementsAI;
import org.bonzai.elements.api.gameobject.element.Earth;
import org.bonzai.elements.api.gameobject.Elemental;
import org.bonzai.elements.api.gameobject.element.Air;
import org.bonzai.elements.api.gameobject.structure.Base;
import org.bonzai.elements.api.gameobject.structure.Crystal;
import org.bonzai.elements.api.gameobject.structure.Mountain;
import org.bonzai.elements.api.gameobject.structure.Tree;
import org.bonzai.elements.api.gamestate.ElementsGameState;
import org.bonzai.api.attribute.GameObject;
import org.bonzai.api.attribute.Tile;
import org.bonzai.api.gamestate.GameState;
import org.bonzai.api.team.TeamInfo;
import org.bonzai.elements.api.tile.Grass;
import org.bonzai.elements.api.tile.Lava;
import org.bonzai.elements.api.tile.Mud;
import org.bonzai.elements.api.tile.Water;

@TeamInfo(name="IllegalSkillsException")
public class CompetitorAI implements ElementsAI {

    //initialize collections
    private Collection<Crystal> crystals;
    private Collection<Water> waters;
    private Collection<Mountain> mountains;
    private Collection<Lava> lavas;
    private Collection<Grass> grasses;
    private Collection<Mud> muds;
    private Collection<Tree> trees;
    private ElementsGameState gameState;
    //TODO uncomment false
    private boolean setup = true;//false;

    @Override
    public void takeTurn(ElementsGameState localGameState) {

        gameState = localGameState;
        trees = gameState.world().get(Elements.TREE);
        waters = gameState.world().get(Elements.WATER);
        mountains = gameState.world().get(Elements.MOUNTAIN);
        lavas = gameState.world().get(Elements.LAVA);
        grasses = gameState.world().get(Elements.GRASS);
        muds = gameState.world().get(Elements.MUD);
        crystals = gameState.world().get(Elements.CRYSTAL);
//        if (setup) {
//            setup = setUpBase();
//        }
        if (setup) {
            if (!gameState.spawnEarth()) {
                /*
                 * If you cannot spawn an Earth try to spawn an Air.
                 */
                if (!gameState.spawnAir()) {
                    // Looks like we can't spawn an Air or an Earth this turn.
                }
            }

            gameState.my().base().shout(crystals.size() + " crystals left!");

            if (!gameState.spawnEarth()) {
                /*
                 * The Game API organizes different sets of data by teams.  To easily access your
                 * own teams data, try gameState.my().
                 *
                 * To access data of rival teams, try gameState.rivals() or gameState.leadingRival().
                 */
                collect(crystals);
            }
        }
    }
    /**
     * Changes air elementals into blazes, then attacks enemies
     * @param air elemental
     * @param gameState
     */
    public void blazeAttack(Elemental element, ElementsGameState gameState) {
    	
    	element.shout("Blaze attack !!");
    	if (!element.hasFire()) {
    		element.pickUpFire();
    	}
    	
    	Position enemyPosition = element.pathfinding().findNearest(gameState.leadingRival().earths()).getPosition();
    	
    	while ( element.getPosition() != enemyPosition ) {
    		element.move(element.pathfinding().findNearest(gameState.leadingRival().earths()));
    	}
    	
    	element.activate(gameState.rivals().get(Elements.BASE).get(0).getPosition());
    }
    
    /** Changes air elementals into typhoons, then attacks enemies
    * @param air elemental
    * @param gameState
    */
    public void typhoonAttack(Elemental element, ElementsGameState gameState) {
	
    	element.shout("water attack !!");
    	
    }

    public void collect(Collection<Crystal> crystals) {
        gameState.my().earths().stream().filter(Elemental::hasWater).forEach(Earth::pickUpWater);
        gameState.my().earths().stream().filter(earth -> earth.hasCrystal()).forEach(Earth::depositCrystal);
        gameState.my().earths().stream().filter(earth -> !earth.hasCrystal()).forEach(Earth::pickUpCrystal);
    }

    /*
    public boolean setUpBase() {
        if (lavas.isEmpty() && waters.isEmpty()) {
            return true;
        } else if (lavas.isEmpty()) {
            Base base = gameState.my().base();
            if (base.pathfinding().findNearest(waters).getPosition().getMinimumDistance(base.getPosition()) > base.pathfinding().findNearest().getPosition().getMinimumDistance(base.getPosition()));

            return true;
        } else if (waters.isEmpty()) {

        }
    }
    */
}