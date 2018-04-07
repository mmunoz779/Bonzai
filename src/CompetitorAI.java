import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bonzai.api.gamestate.GameState;
import org.bonzai.api.position.Path;
import org.bonzai.api.position.Position;
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

    private int turnCounter = 0;
    private int pathsCleared = 0;
    
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
        
        turnCounter++;
    }
    
    /**
     * Finds first clearable obstacle in path to crystal
     * @param path to be checked
     * @return Position of first tree
     * @return null if no obstacles in path exist
     */
    @SuppressWarnings("unlikely-arg-type")
	public Position inaccessibleType(Path path) {
    	for ( int i = 0; i < path.size(); i++ ) {
    		if ( path.get(i).equals(gameState.world().get(Elements.TREE))) {
    			return path.get(i);
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Converts air elementals into blazes, then clears most expensive paths to crystals during setup stage
     * @param air elementals
     * @param collection of crystals
     */
    public boolean blazeClear(Collection<Air> airs, Collection<Crystal> crystals) {
    	
    	Path nearestCrystal = gameState.my().base().pathfinding().getPathTo(gameState.my().base().pathfinding().findNearest(crystals));
    	Position beginningOfPath = inaccessibleType(nearestCrystal);
    	
    	airs.forEach(air -> {
    		if (!air.hasFire()) {
    			air.pickUpFire();
    		} else {
    			air.shout("blazin the forest");
    			if ( beginningOfPath != null ) {
    				air.move(beginningOfPath);
    				if ( air.getPosition() == beginningOfPath ) {
    					air.activate(nearestCrystal);
    				}
    			}
    		}
    	});
    	
    	pathsCleared++;
    	return (pathsCleared >= 3);
    }
    
    /**
     * Changes air elementals into blazes, then attacks enemies
     * @param air elementals
     */
    public void blazeAttack(Collection<Air> airs) {
    	
        airs.forEach(air -> {
            if (!air.hasFire()) {
                air.pickUpFire();
            } else {
                air.shout("4 the fire nation !!");
                Base enemyBase = gameState.rivals().get(Elements.BASE).get(0);
                gameState.leadingRival().earths();
                Position enemyPosition = air.pathfinding().findNearest(gameState.leadingRival().earths()).getPosition();
                air.move(enemyPosition);
                if (air.getPosition().equals(enemyPosition)) {
                    air.activate(enemyBase.getPosition());
                }
            }
        });
    }
    
    /** Changes air elementals into typhoons, then attacks enemies
    * @param air elementals
    */
    public void typhoonAttack(Collection<Air> airs) {
	
    	airs.forEach(air -> {
            if (!air.hasWater()) {
                air.pickUpWater();
            } else {
                air.shout("squirt squirt");
                Base enemyBase = gameState.rivals().get(Elements.BASE).get(0);
                gameState.leadingRival().earths();
                Position enemyPosition = air.pathfinding().findNearest(gameState.leadingRival().earths()).getPosition();
                air.move(enemyPosition);
                if (air.getPosition().equals(enemyPosition)) {
                    air.activate(enemyBase.getPosition());
                }
            }
        });
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