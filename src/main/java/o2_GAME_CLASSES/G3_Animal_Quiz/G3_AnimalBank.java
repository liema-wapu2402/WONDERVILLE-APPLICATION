package o2_GAME_CLASSES.G3_Animal_Quiz;

import java.util.ArrayList;
import java.util.List;

/** Animal data for the Animal Quiz: habitats and the animals (picture file + display name) in each. */
public final class G3_AnimalBank {
    private G3_AnimalBank() {}

    public enum Habitat {
        LAND("L", "Land"), WATER("W", "Water"), AIR("A", "Air");
        public final String code, label;
        Habitat(String code, String label) { this.code = code; this.label = label; }
        public String levelGif() { return "/G3/G3_LEVEL_" + code + ".gif"; }
        public String panelGif() { return "/G3/G3_PANEL_" + code + ".gif"; }
    }

    public static final class Animal {
        public final String file, name;           // file = picture file name (without .png), name = what the child must pick/read
        Animal(String file, String name) { this.file = file; this.name = name; }
        public String easyImage() { return "/G3/G3_ANIMALS/EASY/" + file + ".png"; }
        public String hardImage() { return "/G3/G3_ANIMALS/HARD/REF/" + file + ".png"; }
    }

    private static Animal a(String file, String name) { return new Animal(file, name); }
    private static Animal a(String file) {
        StringBuilder sb = new StringBuilder();
        for (String w : file.toLowerCase().split(" ")) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        return new Animal(file, sb.toString().trim());
    }

    public static List<Animal> easy(Habitat h) {
        List<Animal> l = new ArrayList<Animal>();
        switch (h) {
            case LAND:  l.add(a("SNAKE")); l.add(a("PIG")); l.add(a("HORSE")); l.add(a("DOG")); l.add(a("GOAT")); l.add(a("CARABAO")); break;
            case AIR:   l.add(a("BUTTERFLY")); l.add(a("OWL")); l.add(a("EAGLE")); l.add(a("BAT")); l.add(a("CROW")); l.add(a("PARROT")); break;
            default:    l.add(a("GOLDFISH")); l.add(a("SHARK")); l.add(a("CRAB")); l.add(a("TURTLE")); l.add(a("OCTUPUS", "Octopus")); l.add(a("WHALE")); break;
        }
        return l;
    }

    public static List<Animal> hard(Habitat h) {
        List<Animal> l = new ArrayList<Animal>();
        String[] names;
        switch (h) {
            case LAND:  names = new String[] { "MONKEY", "ELEPHANT", "ZEBRA", "KANGAROO", "DEER", "TIGER", "GIRAFFE", "COW", "HORSE", "PANDA", "LION", "BEAR" }; break;
            case AIR:   names = new String[] { "WILD DUCK", "FLAMINGO", "OWL", "EAGLE", "DRAGONFLY", "WOODPECKER", "PELICAN", "QUAIL", "CRANE", "BEE", "FALCON", "SEAGULL" }; break;
            default:    names = new String[] { "DOLPHIN", "CLAM", "EEL", "PUPPERFISH", "JELLYFISH", "SHRIMP", "ORCA", "SEAL", "SQUID", "STARFISH", "SEAHORSE", "SEASNAKE" }; break;
        }
        for (String n : names) {
            if (n.equals("PUPPERFISH")) l.add(a(n, "Pufferfish"));
            else if (n.equals("SEASNAKE")) l.add(a(n, "Sea Snake"));
            else l.add(a(n));
        }
        return l;
    }
}
