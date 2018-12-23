/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engine.things;

import java.util.*;

import engine.Engine;
import engine.Terminal;
import engine.TwoParamFunc;
import engine.things.Player;
import engine.things.Entity;
import engine.things.Object;

/**
 *
 * @author testtube24
 */
public class Quest {
    public String name;
    public String description;
    public Object target;
    public TwoParamFunc<Engine, Entity> reward;
    public Entity giver;
    public boolean completed;
    public boolean found;
    public boolean given;
    
    public Quest(String nam, String desc, Object tar, Entity givr, TwoParamFunc<Engine, Entity> rew) {
        description = desc;
        target = tar;
        reward = rew;
        giver = givr;
        name = nam;
        
        completed = false;
        found = false;
        given = false;
    }
    
    public void giveTo(Player p) {
        if (!given) {
            p.quests.add(this);
            given = true;
            Terminal.println("Quest added.");
        } else {
            Terminal.println("Quest already added.");
        }
    }
    
    public void run(Engine t, boolean print) {
        if (!found) {
            f1:
            for (Object o: t.protag.inventory) {
                if (o == target) {
                    found = true;
                    break;
                }
                for (Object Obj: o.container) {
                    if (Obj == target) {
                        found = true;
                        break;
                    }
                }
            }

            if (found && print)
                Terminal.println(" Return the " + target.accessor + " to the " + giver.accessor + " to receive a reward.");
        }
    }
    
    public void gaveObj(Engine t, Entity e, Object o) {
        if (!found) {
            run(t, false);
        }
        if (found && e == giver && o == target) {
            target = null;
            if (reward != null) {
                reward.accept(t, e);
            } else {
                Terminal.println("You receive nothing but gratitude for your troubles.");
            }
            completed = true;
        }
    }
    
    public String toString() {
        return name + (completed? " [COMPLETE]": "") + ":\n" + description;
    }
}
