package RogueLike;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Tracks which Memory Fragments the player has found across the
 * WHOLE game. Unlike the dungeon, this does NOT reset when the
 * player dies — memories are permanent.
 */
public class MemoryJournal {

    // The story, told in pieces. Add more any time; nothing else needs to change.
    private static final String[] FRAGMENT_TEXTS = {
        "...the village elder begged for mercy. I remember refusing to raise my blade.",
        "The Shogun's messenger arrived at dawn. His seal bore a mark I had never seen before.",
        "There was a light beneath the shrine. Something ancient stirred when the village burned.",
        "I do not remember dying. Only the cold, and my own name spoken by no one.",
        "The curse is not punishment. It is a chain - and chains bind more than one end.",
        "If the Shogun wears the same mark I saw that night, he is not my judge. He is my fellow prisoner."
    };

    private HashSet<Integer> collected = new HashSet<Integer>();

    public int getTotalCount()     { return FRAGMENT_TEXTS.length; }
    public int getCollectedCount() { return collected.size(); }
    public boolean isCollected(int id) { return collected.contains(id); }
    public String getText(int id)      { return FRAGMENT_TEXTS[id]; }

    // Marks a fragment as found and returns its text.
    public String collect(int id) {
        collected.add(id);
        return FRAGMENT_TEXTS[id];
    }

    // A random fragment ID that hasn't been found yet, or -1 if all are found.
    public int randomUncollectedId(Random rnd, HashSet<Integer> alsoExclude) {
        ArrayList<Integer> options = new ArrayList<Integer>();
        for (int id = 0; id < FRAGMENT_TEXTS.length; id++) {
            if (!collected.contains(id) && !alsoExclude.contains(id)) {
                options.add(id);
            }
        }
        if (options.isEmpty()) {
            return -1;
        }
        return options.get(rnd.nextInt(options.size()));
    }
}
