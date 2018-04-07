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
import org.bonzai.game.Game;

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



        if (!gameState.spawnEarth()) {
            /*
             * If you cannot spawn an Earth try to spawn an Air.
             */
            Earth earth = new Earth();

            if(!gameState.spawnAir()) {
                // Looks like we can't spawn an Air or an Earth this turn.
            }
        }
        

        Collection<Crystal> allCrystals = gameState.world().get(Elements.CRYSTAL);
        gameState.my().base().shout(allCrystals.size() + " crystals left!");

        /*
         * The Game API organizes different sets of data by teams.  To easily access your
         * own teams data, try gameState.my().
         * 
         * To access data of rival teams, try gameState.rivals() or gameState.leadingRival().
         */
        Collection<Earth> earthsWithCrystals = gameState.my().get(Elements.EARTH, ElementsFilters.HAS_CRYSTAL);
        earthsWithCrystals.forEach(earth -> {
            /* 
             * Some commands have default behaviors if they cannot be carried out immediately.
             * For instance, deposit crystal will move the earth closer to the crystal if it can't
             * pick it up this turn.
             */
            earth.depositCrystal();
        });
        
        Collection<Earth> earthsWithoutCrystals = gameState.my().get(Elements.EARTH, ElementsFilters.HAS_CRYSTAL.negate());
        earthsWithoutCrystals.forEach(earth -> {
            /*
             * There are several different path finding methods that will come in handy as
             * you navigate the elementals through the world.
             */
            Crystal closestCrystal = earth.pathfinding().findNearest(Crystal.class);
            if (closestCrystal != null) {
                earth.pickUpCrystal(closestCrystal);
            }
        });
        
        /* 
         * You don't need to know functional programming to compete in BonzAI, but we highly
         * recommend learning it if you get a chance.
         */
        gameState.my().airs().forEach(Air::explore);
    }
    
    /**
     * Changes air elementals into blazes, then attacks enemies
     * @param air elemental
     * @param gameState
     */
    public void attack(Elemental element, ElementsGameState gameState ) {
    	
    	if (!element.hasFire()) {
    		element.pickUpFire();
    	}
    	
    	element.move(element.pathfinding().findNearest(gameState.leadingRival().earths()));
    	
    }

}