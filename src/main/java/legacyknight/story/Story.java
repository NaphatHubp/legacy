package legacyknight.story;

/**
 * เนื้อเรื่องทั้งหมดของเกม รวมไว้ที่เดียว อยากแก้บทพูดให้มาแก้ไฟล์นี้
 *
 *  - prologue()    ฉากเปิดเรื่อง (ขึ้นครั้งแรกที่กด START)
 *  - intro(i)      ฉากก่อนเข้าด่านที่ i (นับจาก 0)
 *  - outro(i)      ข้อความตอนผ่านด่านที่ i (แสดงบนหน้า WIN)
 *  - epilogue()    ฉากจบเกม (หลังชนะบอส)
 */
public final class Story {

    private Story() { }

    private static final Scene PROLOGUE = new Scene("The Legacy Knight",
            "In the age of castles and banners, the kingdom of Nightfield stood for a thousand years, "
                    + "guarded by knights who passed the Legacy Sword from parent to child.",
            "Then Dead, the Reaper, came for the crown. He broke the royal seal, sent slimes and "
                    + "crystal-shelled beasts into the fields, and set the Demon King, his herald, to hold the high road.",
            "Every knight of the Order has fallen. You alone remain, the last bearer of the Legacy Sword. "
                    + "Ride for the castle, and face Death himself.");

    private static final Scene[] INTRO = {
            new Scene("Chapter 1 - Slime Meadow",
                    "The meadow outside the castle gate, where you took your knight's oath, "
                            + "is crawling with slimes: green ones that hop, blue ones that spit.",
                    "Cut them down, find the key, and open the old gate. Every road to the throne starts here."),
            new Scene("Chapter 2 - Spike Valley",
                    "Beyond the gate lies the valley road, lined with the spikes of a broken palisade. "
                            + "Red brutes charge from the dark, and crystal-shelled Ramses stand guard.",
                    "The gatekeeper's key hangs from a high ledge. Dodge the rush, strike when they tire, "
                            + "and mind your tail."),
            new Scene("Chapter 3 - Archer's Perch",
                    "At the cliff pass above the castle, the Demon King waits: Death's herald, "
                            + "armoured in flame, hurling fireballs from his perch.",
                    "Raise your shield when he winds up, and be ready when he cracks and mutates. "
                            + "Beat him and the throne room is open."),
            new Scene("Final Chapter - Legacy Throne",
                    "Dead, the Reaper, drifts above the throne your ancestors once guarded, "
                            + "his scythe trailing crescents of fire.",
                    "Wound him badly enough and he will wake into something far worse, "
                            + "vanishing and reappearing at your side. Hold your shield high, little knight. "
                            + "The kingdom is counting on you.")
    };

    private static final String[] OUTRO = {
            "The gate creaks open. Beyond it, the road runs into Spike Valley.",
            "You pocket the key. Above the castle, a horn sounds: the Demon King has seen you coming.",
            "The Demon King falls to one knee, his flames dying. The throne room doors swing open, and the air turns cold.",
            "Dead's scythe falls silent. Dawn spills over Nightfield for the first time in a hundred nights."
    };

    private static final Scene EPILOGUE = new Scene("The Legacy Continues",
            "The seal is rebuilt. The slimes drift home, the banners rise again, "
                    + "and the people of Nightfield sleep soundly once more.",
            "You hang up the Legacy Sword, for now. Someday a new squire will pick it up, "
                    + "and the legacy will go on.",
            "THE END  -  thank you for playing!");

    public static Scene prologue() {
        return PROLOGUE;
    }

    public static Scene intro(int levelIndex) {
        return INTRO[levelIndex];
    }

    public static String outro(int levelIndex) {
        return OUTRO[levelIndex];
    }

    public static Scene epilogue() {
        return EPILOGUE;
    }
}
