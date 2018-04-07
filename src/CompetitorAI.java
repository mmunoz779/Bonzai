import java.util.Collection;

import org.bonzai.elements.api.ElementsFilters;
import org.bonzai.elements.api.Elements;
import org.bonzai.elements.api.ai.ElementsAI;
import org.bonzai.elements.api.gameobject.element.Earth;
import org.bonzai.elements.api.gameobject.element.Air;
import org.bonzai.elements.api.gameobject.structure.Crystal;
import org.bonzai.elements.api.gamestate.ElementsGameState;
import org.bonzai.api.team.TeamInfo;

@TeamInfo(name="My Team Name")
public class CompetitorAI implements ElementsAI {

    @Override
    public void takeTurn(ElementsGameState gameState) {
        /* 
         * The first step is to spawn.  Either Elements.EARTH or Elements.AIR
         */
        if (!gameState.spawnEarth()) {
            /*
             * If you cannot spawn an Earth try to spawn an Air.
             */
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

}