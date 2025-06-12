##### Version 1.21.1-12.3.2-pre
- Add support for chance-based output to the cloche (BluSunrize) 
- Simplify a lot of recipe JSONs (BluSunrize)
    - The wrapping in "basePredicate" is now not needed for normal items anymore, and "count" on item stacks is optional too
- Fix old electrode blueprints crashing in the workbench (BluSunrize)
- Fix rendering of blueprints in item frames (BluSunrize)
- Fix missing particles on noteblocks over resonanz engineering blocks (BluSunrize)
- Fix resonanz observer not forming when the sculk sensor is active (BluSunrize)
- Fix tanks and silos always dropping iron sheetmetal for the first block broken (BluSunrize)

##### Version 1.21.1-12.3.1-189
- Add the Basic Engineering Block (BluSunrize)
    - It's crafted from treated wood and iron
    - Used in various recipes that previously used treated wood - these recipes got cheaper!
- Add an item model for the resonanz observer (BluSunrize)
- Re-add Jade compatibility (BluSunrize)
- Overhaul item routers! (BluSunrize)
    - They no longer allow items out an unfiltered side *if* a filtered side for that item exists
    - Tag filtering now allows selecting a specific tag with the scroll-wheel.
    - Replace text references to "NBT Data" with "Data Components" instead
- Overhaul graphite! (BluSunrize)
    - HV Accumulators now use graphite plates instead of ingots
    - Those ingots are fully deprecated, they can be crushed to dust but have no other uses
    - Graphite electrodes for the Arc Furnace no longer require a blueprint!
    - Instead, both electrodes and plates are now made in the bottling machine, using dust + creosote oil!
- Change recipes for dropping and vertical conveyors to be a bit cheaper (BluSunrize)
- Fix desync of item router buttons on multiplayer servers (BluSunrize)
- Fix resonanz observer not rendering the completed structure in the manual (BluSunrize)

##### Version 1.21.1-12.3.0-188
- Add the Resonanz Observer multiblock (BluSunrize)
    - It's a chunkloader! IE has chunkloading now! You can keep your excavator running far away!!
    - Its construction require a trip to the ancient cities, to acquire an echo shard...
    - Also, this machine eats paper by the minute!
- Overhaul targeting for homing bullets (BluSunrize)
    - They won't shoot your wolves (or other animals tamed by the shooter anymore)
    - They won't target teammates or animals tamed by teammates
    - This does *not* apply to bullets fired by turrets 
- Change Wolfpack Cartridges to apply a buff to nearby tamed wolves when used! (BluSunrize)
    - Your wolves (and your allies' wolves) gain speed and strength
    - Your wolves will focus on the hit target, if they don't already have a mob they are targeting
- Change Sheetmetal Tanks & Silos to be formed from *any* Sheetmetal! (BluSunrize)
    - This allows making them with whatever metal you have on hand
    - They memorize their structure and will return those blocks again
    - Their colour can not be adjusted with this, because that would require a massive rework of their models
- Change turrets to use fake players, allowing them to drop blaze rods and other player-only items (BluSunrize)
- Re-add offset for subtitles when rendering displays for held items (BluSunrize)
- Update manual entry for gunpowder barrels, to better explain their intended use (voidsong-dragonfly)
- Fix error for mods like JustEnoughResources querying IE's villager trades (BluSunrize)
- Fix errors when saving IE's projectile entities (BluSunrize)
- Fix wooden supports on sheetmetal tanks having the wrong bounding box (BluSunrize)
- Fix revolver perks not combining correctly on crafting (BluSunrize)
- Fix JEI recipe transfer not working in the Engineer's Crafting Table (BluSunrize)
- Fix inverted rendering for the buzzsaw, drill and chemthrower (BluSunrize)
- Fix overlay for buzzsaw, drill and chemthrower not displaying on the left hand side (BluSunrize)
- Fix shaders not being removed from tools properly (BluSunrize)
- Fix gunpowder barrels not having sound and particles (BluSunrize)
- Fix gunpowder barrels not being triggered by other explosions (BluSunrize)
- Fix multimeter overlay boxes being possibly too small on non-ascii languages (BluSunrize)
- Fix zoom and ammo swapping on the railgun occurring at the same time (BluSunrize)
- Fix conveyor covers getting consumed in creative mode (BluSunrize)
- Fix herbicide destroying Farmer's Delight farmland (BluSunrize)
- Translations Added/Updated: zh_cn (JustAlkaid)

##### Version 1.21.1-12.2.0-187
- Adjust the Automaton Wolves (BluSunrize)
    - They can no longer be healed with meat, this also prevents breeding them.
    - Instead, they can be repaired with metal plates and a hammer
- Add a manual entry for the Automaton Wolves (BluSunrize)
    - This entry is only unlocked when picking up their blueprint!
- Add wall blocks for hempcrete, concrete and their variations!
- Add splash & lingering potion recipes to the mixer & bottling machine (BluSunrize)
- Change Fluid Pumps to allow inverting their redstone control with a screwdriver (BluSunrize)
- Change IE to use NeoForge's native fluid ingredients in recipes (BluSunrize)
    - This is a breaking change for any addons, but there isn't any released so we should be safe
    - This also finally allows Create potions to be used in the Mixer & Bottling Machine
- Allow vanilla wall blocks to connect to windows (BluSunrize)
- Fix circuit table closing when inventory key is pressed (BluSunrize)
- Fix assembler not saving its stored patterns with the world (BluSunrize)
- Fix JEI pattern button crashing when used in the assembler (BluSunrize)
- Fix fluid pipes causing forced chunkloads when building their network (BluSunrize)
- Fix signs losing their content when moved by Create contraptions (BluSunrize)
- Fix broken link in the manual (BluSunrize)

##### Version 1.21.1-12.1.1-186
- Add Automaton Wolves! (BluSunrize)
    - They are spawned with a new item, which requires a blueprint that can only drop from Trial Chamber Vaults
    - They spawn with extra health and increased damage but are otherwise just subtype of wolves and behave the same
- Remove mixer recipes for mundane and thick potions again (BluSunrize)
    - They conflict with redstone acid and are never used anyway
- Add support to configure the Fluid Router with Potion Bottles (Léa Gris)
- Add name field for circuits in the workbench (BluSunrize)
- Add colouration to the circuit tooltips (BluSunrize)
- Add breeze rods as railgun ammo, they can hit breezes! (BluSunrize)
- Change JEI recipes for filling buckets to not use fluid tags (BluSunrize)
- Change the skyhook to actually use the same logic as the mace (BluSunrize)
    - Previously it was approximating it, due to missing methods in vanilla
- Overhaul multimeter interaction with redstone blocks (BluSunrize)
    - Fix multimeter not showing text when hovering over redstone blocks
    - Fix multimeter not working when used on redstone connectors
    - Show the stored value and output channel when checking state cells with the multimeter
    - Show the in and output signals when checking probe connectors with the multimeter
- Fix waterwheels not updating blocked in many cases (voidsong-dragonfly)
- Fix fluid router GUI crashing (Léa Gris)
- Fix "Superior Weaponry" achievement not triggering, caused by various factors: (BluSunrize)
    - Fix bullets not assigning their owner, thus all damage being considered turret damage
    - Fix bullets and railgun shots not counting as projectiles
    - Fix chemthrower not correctly attributing kills to the player
- Fix magnetic glove for the shield not working (BluSunrize)
- Fix display of items on wooden crates in inventory (BluSunrize)
- Fix rockcutter not properly silk-touching blocks (BluSunrize)
- Fix blueprint shelf showing "invalid_blueprint" on empty slots (BluSunrize)
- Fix auto-clickers breaking when middle clicking on multiblocks (BluSunrize)
- Fix "obstructed connection" overlay being in the wrong place (BluSunrize)
- Fix accumulator backpack not being attachable to armor (BluSunrize)
- Fix invalid blueprints causing a render crash in the workbench (BluSunrize)
- Fix squeezer, fermenter and mixer not allowing users to take items out again (BluSunrize)
- Fix switchboards not transmitting signals when only connected to other switchboards (BluSunrize)
- Fix Diesel Generator burning fuel for too short of time (Diesel Thomas)
- Fix crash when saving configured Item or Fluid routers in inventories (BluSunrize)
- Fix accumulators having broken side configs after being broken and replaced (BluSunrize)
- Fix wirecutters taking damage in creative mode (BluSunrize)
- Fix recipe for cagelamps not working (BluSunrize)
- Fix broken lighting on the charging station (BluSunrize)

##### Version 1.21.1-12.1.0-185
- All changes from 1.20.4-11.7.0
- Add HOP graphite ingots to general ingots tag (Malte)
- Add builtin compat with Repurposed Structrues (TelepathicGrunt)
- Add backup options for tag-based villager trades, so that broken tags don't cause broken trades (BluSunrize)
- Add support for Pitcher Plants in the cloche (BluSunrize)
    - This comes with a new render function for datapacks to handle double-high crops, which is also used by hemp
- Add treated wood, steel and aluminium signs (BluSunrize)
    - They work like vanilla signs, not much else to say
- Add the ability to make warning signs glow with glow ink sacks (BluSunrize)
- Add recipe for crushing coarse dirt into dirt (voidsong-dragonfly)
- Remove CustomParticleManager (IMS)
- Change High Explosive cartridges to return shells on use (voidsong-dragonfly)
- Change hemp plants to use the same "half" property as vanilla double flowers (BluSunrize)
    - This results in existing hemp looking weird, just break and replace it!
- Change mouse sensitivity based on current zoom level with railgun or revolver (BluSunrize)
    - Also hide revolver's & railgun's first-person render while zoomed
- Change coke oven to only burn the same logs as a vanilla furnace can (voidsong-dragonfly)
- Improve readability of fluid name in the chemthrower HUD (BluSunrize)
- Fix crash with dual codecs (Malte)
- Fix crash when placing toolbox (Malte)
- Fix sealed crates deleting items (Malte)
- Fix incorrect sprite for coke oven flame (Malte)
- Fix crash with assembler (Malte)
- Fix crash due to invalid blueprints (Malte)
- Fix condition recipe encoding/decoding (Malte)
- Fix chemthrower not "noticing" its main fluid (Malte)
- Fix hempseeds not being compostable (voidsong-dragonfly)
- Fix broken armor values on steel armor (BluSunrize)
- Fix angled chutes not having proper collision boxes (BluSunrize)
- Fix shader banners in villages not rendering (BluSunrize)
    - Unfortunately only affects newly generated structures 
- Fix bugged colors on shader banners, resulting from mixups of ARGB and ABGR in composited textures (BluSunrize)
- Fix missing item model for the radio tower (BluSunrize)
- Fix firework cartridges crashing when crafted (BluSunrize)
- Fix firework cartridges not accessing vanilla components (BluSunrize)
- Fix arc recycling recipes not having their own JEI tab (BluSunrize)
- Fix arc recycling recipes not showing adjusted outputs in JEI (BluSunrize)
- Fix floodlights deleting their own fake lightsources immediately (voidsong-dragonfly)
- Fix the scope for railgun and revolver just not rendering at all (BluSunrize)
- Fix zooming not working when the weapon is in offhand (BluSunrize)
- Fix multiblock inventories stacking to 99 (voidsong-dragonfly)
- Fix charging station not charging the accumulator backpack (BluSunrize)
- Translations Added/Updated: zh_cn (Cactusstudent, SlimeSB, mc-kaishixiaxue, JustAlkaid), tr_tr (RuyaSavascisi)

##### Version 1.21.1-12.0.0-182
- First release for 1.21.1
- **All** of the porting work and bugfixing were done by Malte

##### Version 1.20.4-11.6.1-181
- Fix pipe valves causing a crash when used in a loop of pipes (BluSunrize)

##### Version 1.20.4-11.6.0-180
- "The Redstone Update!"
    - Add the radio tower, a way to wirelessly send signals! (BluSunrize)
    - Add the redstone state cell for storing signals (BluSunrize)
    - Add the redstone timer for emitting regular signals (BluSunrize)
    - Add the redstone switchboard for remapping redstone signals between networks (BluSunrize)
    - Add the warning siren to make loud noises and send villagers to their homes (BluSunrize)
    - Move redstone components to a separate category of the manual (BluSunrize)
- Add recipe for crushing slag bricks into slag gravel (BluSunrize)
- Add recipes for wetting vanilla concrete powder in the bottling machine (BluSunrize)
- Add progress tooltip to items in the arc furnace (BluSunrize)
- Allow revolver pieces to be recycled in the arc furnace (BluSunrize)
- Change recipe for catwalk stairs to be cheaper (BluSunrize)
- Change luck attribute on revolvers to apply always, instead of just when held (BluSunrize)
- Improve performance of waterwheels (voidsong-dragonfly)
- Improve performance of thermoelectric generators (voidsong-dragonfly)
- Improve performance of floodlights (voidsong-dragonfly)
- Fix waterlogged posts not dropping items (BluSunrize)
- Fix treated windows and feedthrough insulators letting rain through (BluSunrize, voidsong-dragonfly)
- Fix crates not syncing their names to the client (BluSunrize)
- Fix shader based particles breaking on servers (BluSunrize)
- Fix various button tooltips sticking around after being clicked (BluSunrize)
- Fix treated trapdoors erroneously having metal properties (BluSunrize)
- Fix arms on posts dropping blocks when broken (BluSunrize)
- Fix crate minecarts crashing when inserted into with a hopper (BluSunrize)
- Fix barrel minecarts crashing when right-clicked (BluSunrize)
- Fix villagers trying to pathfind through multiblocks (BluSunrize)
- Fix disconnect due to shader list in the manual (BluSunrize)
- Translations Added/Updated: de_de (AriaElidove)

##### Version 1.20.4-11.5.0-179
- Add warning signs (BluSunrize)
    - Textures kindly provided by AriaElidove, thank you!
    - You can also make a banner pattern with the signs and put (some) of the warnings on banners and shields!
- Add fence gates for treated wood, steel and aluminium (BluSunrize)
- Add colored sheetmetal chutes (BluSunrize)
- Add a new light: the cage lamp (BluSunrize)
    - Model & texture kindly provided by sinsinewave, thank you!
- Remove raw ore blocks from the list of blocks dug by the grinding disk  (voidsong-dragonfly)
- Change workbench to be able to handle blueprints with more than 9 recipes (BluSunrize)
- Change villager trades for revolver pieces and mineral deposit maps to re-roll (BluSunrize)
    - After buying the item, the trade will be replaced with a new one
    - This only works on villagers acquired AFTER this update!
- Change revolvers to use the vanilla cooldown system (BluSunrize)
    - This does unfortunately make dualwielding less powerful, because cooldowns affect all revolvers
- Change transformers nto not be worse than accumulators (voidsong-dragonfly)
    - They now limit the energy flow properly, making them technically higher on throughput and reducing wire burnout
- Prevent crates from being opened after being sealed (BluSunrize)
- Fix villagers being unable to interact with IE doors (BluSunrize)
- Fix steel doors dropping twice (BluSunrize)
- Fix blueprint shelves not dropping blueprints when broken (BluSunrize)
- Fix conveyors moving players while flying (BluSunrize)
- Fix models for drill and buzzsaw not using translucent as a rendertype (BluSunrize)
- Fix revolvers not combining their perks correctly when crafted (BluSunrize)
- Fix revolver shots having impact sounds like an arrow (BluSunrize)
- Fix models for drill and buzzsaw not using translucent as a rendertype (BluSunrize)

##### Version 1.20.4-11.4.3-178
- Change scaffolding blocks to have more descriptive names (AriaElidove)
- Change translations for fluid blocks so Jade shows them properly (AriaElidove)
- Fix arc recycling preventing joining multiplayer servers (BluSunrize)
- Fix wooden crates closing their GUI when using your inventory key in their name (BluSunrize)

##### Version 1.20.4-11.4.2-177
- Add treated wood & steel doors (BluSunrize)
    - Steel doors can only be opened by hand and will be locked by an applied redstone signal
    - There's also matching trapdoors
- Add the redstone-controlled pipe valve from Engineer's Decor (voidsong-dragonfly)
    - Thank you wilechaote for giving us permission to include these blocks in the mod <3
- Add a banner pattern for the screwdriver (BluSunrize)
    - Also give the banner patterns each unique item textures
- Add a recipe for armor-piercing cartridges using netherite nuggets (BluSunrize)
- Add the ability to open scaffold covered ladders on the side (voidsong-dragonfly)
- Add lots more block to be dug by the grinding disk, rock cutter and engineer's hammer (voidsong-dragonfly)
- Remove duplicate villager trade (voidsong-dragonfly)
- Change language files to partially use datageneration (BluSunrize)
    - This way we don't forget about localizing the countless banner variations anymore
- Change mixer to always place mixed fluid on bottom (voidsong-dragonfly)
- Change fluids to actually have density (voidsong-dragonfly)
- Change the survey tools to work on even more overworld blocks (voidsong-dragonfly)
- Change rendering for two-block high flowers in the cloche (voidsong-dragonfly)
- Change reinforced crates to require steel (BluSunrize)
- Change arc furnace recycling (voidsong-dragonfly)
    - Add additional items to recycle
    - Improve JEI displays
- Fix creative tab not being translatable (voidsong-dragonfly)
- Fix diesel generator producing energy on low fluid amounts (voidsong-dragonfly)
- Fix Toolboxes getting emptied when replacing grass blocks (voidsong-dragonfly)
- Fix windmill crashing on servers (voidsong-dragonfly)
- Fix Diesel generator not switching to active properly (voidsong-dragonfly)
- Fix post transformers being placeable on top of posts (voidsong-dragonfly)

##### Version 1.20.4-11.4.1-176
- Add a netherite nugget (BluSunrize)
    - This is to allow recycling outputs of nuggets
- Fix incorrect output on high-cetane diesel recipe (BluSunrize)

##### Version 1.20.4-11.4.0-175
- Add upgrades for the Engineering's Skyhook (BluSunrize)
    - High Torque Motor makes it faster to go up slopes
    - Insulated Grip allows riding live wires without getting zapped
    - Heavy Impact Hooks deal bonus damage with a falling attack, much like the mace in 1.21
- Allow Phial Cartridges to be filled in the Bottling Machine (BluSunrize)
- Allow survey tools to be used on more blocks (voidsong-dragonfly)
- Add cloche recipes for ferns and grass (voidsong-dragonfly)
- Add crushing recipes for stone and deepslate (voidsong-dragonfly)
- Add the blueprint shelf, a block that can store up to 9 blueprints! (BluSunrize)
- Add a banner pattern for the wire cutter (BluSunrize)
- Add sub-designs for the banner patterns: hammer, wire cutter & wolf (BluSunrize)
    - This allows for individual coloured grip + head of the tool
    - It also allows all three wolf designs to be stored in one pattern
- Allow barrel and crate minecarts to be automatically loaded & unloaded (voidsong-dragonfly)
    - You can use a fluid pump to load and unload the barrel carts!
- Add High-Cetane Biodiesel (BluSunrize)
    - It's made from biodiesel and potion of strength in the refinery
    - Its burntime is 25% longer than normal biodiesel!
- Add catwalk blocks, heavily inspired by Engineer's Decor (BluSunrize)
    - Thank you wilechaote for giving us permission to include these blocks in the mod <3
    - Also includes catwalks stairs!
- Change the recipes for revolver cartridges to be cheaper overall (BluSunrize)
- Change Nickel and Uranium ores to have more easily identifiable textures (BluSunrize)
- Change titles and descriptions of Advancements to more closely match Mojang's pattern (voidsong-dragonfly)
- Change wire loss to be exponential rather than linear, allowing HV lines to run much further and still be viable! (voidsong-dragonfly)
- Change storage crates in their behavior (BluSunrize)
    - They will only keep their items if they are sealed before being broken
    - To seal a crate you'll need to use an engineer's hammer on it for an extended duration
    - This way, they're not longer as good as Shulker Boxes
    - To make up for it, the name of a crate can now be edited in its GUI and will display when hovering the cursor over the block
- Change waterwheels in their behavior (voidsong-dragonfly)
    - Water source blocks directly in wheel paddles now have higher 'resistance' and penalize wheels
    - Fluid blocks of lower height now provide less torque to waterwheels - higher flow is better
    - Water will no longer spill/flow off the 'side' of waterwheels, their models have been adjusted to symbolize this
    - Breastshot wheels are given a small buff to make millrace setups like large 17th century mills profitable
    - The maximum power for an optimized overshot wheel is the same as before!
    - Also both the waterwheel and windmill now have sounds!
- Fix crusher not accepting multiple stacks of the same ore (BluSunrize)
- Fix stone multiblocks being washed away by water (BluSunrize)
- Fix potential race condition with wires damaging entities (BluSunrize)
- Fix shader banners and potion fluids using the wrong colors (BluSunrize)
- Fix accumulator backpack getting rendered as full-bright when worn (BluSunrize)
- Fix IE's tools rendering inverted in offhand (BluSunrize)
- Fix crusher and excavator particles behaving weirdly (BluSunrize)
- Fix inconsistent breaking sounds with stone multiblocks (BluSunrize)

##### Version 1.20.4-11.3.0-174
- Re-add CC-Tweaked integration (Malte)
- Add missing redstone acid recipe to the manual (BluSunrize)
- Add the windows from Engineer's Decor (BluSunrize)
    - Thank you wilechaote for giving us permission to include these blocks in the mod <3
- Add Reinforced Concrete, crafted with Netherite Rods (BluSunrize)
    - It's witherproof!
- Add Panzerglass windows, which are also witherproof, in a similar way to the Engineer's Decor block! (BluSunrize)
- Change Leaded Concrete to be less resistant to explosions and block teleporting instead
- Fix fractions not rendering properly in the manual (BluSunrize)
- Fix Machine Interface crashing on servers (BluSunrize)
- Fix Toolbox being movable while opened (BluSunrize)

##### Version 1.20.4-11.2.0-173
- Overhaul the Engineering's Manual (voidsong-dragonfly)
    - Splitting various long entries into multiple smaller ones (fluid transport, logic circuits, ...)
    - Adding new pages for undocumented features (gunpower barrel, minecarts, ...)
    - Sorting everything into a new list of more comprehensible categories
    - As a result, all translations of the manual are deprecated and need to be updated by community input
- Add the Machine Interfact, a block designed to read a variety of data from multiblock machines (BluSunrize)
- Change recipe for hempcrete to make it cheaper to mass produce (voidsong-dragonfly)
- Fix covered conveyors crashing the game (BluSunrize)
- Fix multiblocks being broken by flowing water (BluSunrize)
- Fix buttons in the circuit table not behaving correctly with keyboard inputs (BluSunrize)

##### Version 1.20.4-11.1.0-172
- First release for 1.20.4
- Includes all features from 10.1.0
- **All** of the porting work and bugfixing courtesy of Malte
- Overhauled Mineral Veins (BluSunrize, voidsong-dragonfly)
    - Mineral veins now use biome tags as conditions, this replaces dimension keys and allows more precise filtering
    - New Vein: **Banded Iron**, a primary iron deposit because it's IE's most common resource
    - New Vein: **Lazulitic Intrusion**, comprised of lapis and gold, making it ideal for decoration and enchanting
    - New Vein: **Alluvial Sift**, only found in river biomes, this allows the excavator to mine for diamonds
    - New Vein: **Rich Auricupride**, only found in mesa biomes, high in gold and some copper
    - Changed **Pentlandite** to be primarily a nickel vein since its essential for IE's mid- and lategame architecture
    - Changed **Wolframite** to include tin when present
    - Changed all veins to output less sulfur
    - Changed a variety of other veins to prioritze secondary ores that IE relies on (lead, silver)
    - Changed weight and failchance on various veins, this makes "necessary" resources more common
    - Changed various veins to only show up in certain biomes, making "decorative" veins like Silt less common
    - Actually increased the weight of "rare" veins such as **Beryl** and **Uraninite**, because they are now limited by biome
    - Allow survey tools to be used on netherrack and soul sand, to make searching in the Nether viable

##### Version 1.20.1-10.1.0-171
- Includes all features from 9.2.4
- Add sounds for Arc Furnace, Excavator, and Automated Workbench (voidsong-dragonfly)
- Add burn times for the diesel generator to the manual (BluSunrize)
- Add support for armor trims on steel armor (BluSunrize)
- Add additional uses for lead & nickel (voidsong-dragonfly)
    - Lead can be crafted into red and white dyes
    - Radiators now use constantan instead of copper
    - Tinted glass can be made in larger quantities by using lead
- Add randomized textures for various hempcrete & concrete blocks (voidsong-dragonfly)
- Add the electromagnet block (BluSunrize)
    - It attracts nearby items when given power! 
- Add the portable electromagnet! (BluSunrize)
    - Install it as an upgrade on your accumulator backpack to have a magnet on the go!
- Add various blocks from Engineer's Decor (voidsong-dragonfly)
    - Thank you wilechaote for giving us permission to include these blocks in the mod <3
- Add additional fertilizers for the cloche (voidsong-dragonfly)
    - This should allow people to use up their surplus suflur!
- Add ability to place Engineer's Manual in a chiseled bookshelf (BluSunrize)
- Add sawmill recipes for bamboo (BluSunrize)
- Change hemp blocks to notify their neighbours when they grow (jrtc27)
- Change Jade Tooltips for multiblocks to work when looking at any part of the machine (HermitOwO)
- Fix conveyor rendering in the manual (Malte)
- Fix broken translation keys (Malte & BluSunrize)
- Fix turret GUI textfield not being editable (BluSunrize)
- Fix taiga villager houses being surrounded by air blocks (BluSunrize)
- Fix redstone control not working on the assembler (BluSunrize)
- Fix villager names not being translated in JER and EMI (BluSunrize)
- Fix error related to deprecated unicode font (BluSunrize)
- Fix bottling machine deleting items on the conveyor (BluSunrize)
- Fix bottling machine not rendering bucket-filling recipes properly (BluSunrize)
- Fix hemp replacing blocks above it when growing (BluSunrize)
- Fix fluid pipes losing their colour because the chunk wasn't marked as dirty (BluSunrize)
- Fix transparency issues with the shield and a few other item models (BluSunrize)
- Fix spawn interdiction from lanterns not working (BluSunrize)
- Fix rare crash in arc recycling calculation (BluSunrize)
- Fix possible crash in conveyor rendering (BluSunrize)
- Translations Added/Updated: ja_jo (karakufire)

##### Version 1.19.2-9.2.4-170
- Add new excavator veins for decoration blocks (voidsong-dragonfly)
  - Amethyst Crevasse, a geode vein
  - Hardened Claypan, a red sand & terracotta vein
  - Ancient Seabed, a dead coral & dripstone vein
- Add cloche recipes to grow flowers (voidsong-dragonfly)
- Add crusher recipes to make dyes (voidsong-dragonfly)
- Add items directly to inventory when: (voidsong-dragonfly)
  - Cutting wires
  - Picking up fluorescent tubes, core samples or the toolbox
- Add the plated shield to the "forge:tools/shields" tag (BluSunrize)
- Change turrets to accept generic entity terms like "Villager" for their black/whitelist (BluSunrize)
- Change text rendering in GUIs to be more readable (BluSunrize)
- Change assembler to handle buckets in recipes better (BluSunrize)
- Fix issues with multiblocks being accessed before being full formed (Malte)
- Fix items with obj renders breaking in the AE2 inscriber
- Fix drill overlay highlighting too many blocks (Malte)
- Fix external heater breaking when dealing with faster furnace recipes (TeamSpen210)
- Fix stairs and slabs not receiving item tags (BluSunrize)
- Fix potential crash with the accumulator backpack (BluSunrize)
- Fix output positions on the improved blastfurnace being swapped around (BluSunrize)
- Fix synchronization errors in the garden clocke (voidsong-dragonfly, Malte)
- Fix induction charging on the accumulator backpack not working (BluSunrize)
- Fix multiblock sounds not triggering subtitles continuously (BluSunrize)
- Fix hemp seeds being tagged as "rods" (BluSunrize)
- Fix railgun rods applying damage like a normal arrow (BluSunrize)
- Fix villager houses for structural engineer and machinist having the wrong workstation (BluSunrize)
- Fix blueprint crafting with split up ingredients (BluSunrize)
- Translations Added/Updated: cs_cz (RomanPlayer22)

##### Version 1.20.1-10.0.0-169
- First release for 1.20.1, now supporting NeoForged!
- Includes all features from 9.4.1
- Add sawmill support for stripped wood (BluSunrize)
- Add sawmill support for cherry wood (BluSunrize)
- Add cloche support for torchflowers (BluSunrize)
- Change hemp blocks to extend vanilla's CropBlock (BluSunrize)

##### Version 1.19.4-9.4.1-168
- All features from 9.2.3
- Fix localization of perks on revolver parts (BluSunrize)
- Fix potential crashes when supplying power to arc furnace or excavator with other mod's cables (BluSunrize)
- Fix metal press not rendering items in the correct direction (BluSunrize)

##### Version 1.19.2-9.2.3-167
- Change "Superior Weaponry" advancement to hint at its potential to spawn new illagers in raids (BluSunrize)
- Change item renders for Railgun, Chemthrower and Shield to be dynamic, to allow use of dynamic shaders (Drullkus)
- Fix erroneous capability check on empty FluidStacks (Malte)
- Fix sound issues with the Skyhook (Malte)
- Fix IE projectiles failing to despawn (Malte)
- Fix handling of stacked items on the Bottling Machine (Malte)
- Fix error in recycling recipe generation (Malte)
- Fix minor typo in External Heater manual page (Logging4J)
- Fix steel sword not using the "forge:tools/swords" tag (BluSunrize)
- Fix hemp plants not using the "minecraft/crops" tag (BluSunrize)
- Fix steel and faraday armor not using "armor" tags (BluSunrize)
- Fix broken animations for revolvers in the left hand (BluSunrize)
- Fix revolver perks trying to localize at "tier0" (BluSunrize)
- Translations Added/Updated: cs_cz (RomanPlayer22), ja_jp (karakufire)

##### Version 1.19.4-9.4.0-166
- First release for 1.19.4!
- Includes all features from 9.2.2

##### Version 1.19.2-9.2.2-165
- Add Engineer Illagers (BluSunrize)
    - The Fusilier, a ranged illager carrying a railgun
    - The Commando, using a shield to protect themselves when not firing their revolver
    - The Bulwark, a tank covered in steel armor, carrying a chemthrower and a heavy plated shield
    - These enemies can join raids after the player has proven themselves dangerous and technologically advanced...
- Change Buckshot to have a chance of disabling shields (BluSunrize)
    - Works like vanilla axes
    - Was added especially to combat shield-carrying illagers
- Fix crash with gunpowder barrels (Malte)
    - Also makes their explosion speed more consistent
- Refresh balloon hover text when the placement offset is changed (Malte)
- Fix particle rendering in cloche and charging station (Malte)
- Fix fluid pipe covers not updating (Malte)
- Fix infinite sulfur exploit from cycling blazerods (BluSunrize)
- Fix dropping conveyor trying to "drop" items into blocks that have inventories, resulting in flinging (BluSunrize)
- Translations Added/Updated: uk_ua (SKZGx)

##### Version 1.19.2-9.2.1-164
- Add the ability to apply dye and scaffolds to pipes by holding them in your offhand (Malte)
- Add Jade plugin for showing fill of sheetmetal tank (BluSunrize)
- Add Concrete and Hempcrete bricks, pillars and chiseled variants (voidsong-dragonfly)
- Add sawmill recipes for treated wood (TeamSpen210) 
- Cache connection rendering at the section level rather than individual segments (Malte)
- Improve chunk building performance by not marking all IE blocks as "dynamicShape" (Malte)
- Overhaul villager trades (BluSunrize)
    - Structural Engineer:
        - Requests more raw materials (treated wood, metal rods, concrete) and sells finished products (scaffolding, leaded concrete)
        - Requires more interesting items like insulated glass and duroplast, to make "speedrunning" the mineral vein maps a little harder
        - Mineral Vein maps now focus on rare veins within 16 chunks of the villager and they can sell 2 of them
    - Machinist:
        - Blueprint for crafting components is an early trade that gives good XP
        - Mostly focused on emerald-based sales of drill heads & upgrades
        - Still has the blueprint for Arc Furnace electrodes as a master trade
    - Electrician:
        - Reduced cost of tools & added screwdriver to the options
        - Removed railgun and revolver upgrades from the list
        - Experts will purchase vacuum tubes and sell electronic components
        - Master trades are tesla coil & cheap circuit backplanes
    - Outfitter:
        - Can now sell epic grabbags as well
        - Will buy tough fabric, silver and gold grit 
        - Can sell some of IE's banner patterns
- Change Chemthrower to have a broader spread by default, making the effect of the Focused Nozzle more noticeable (BluSunrize)
- Change Mixer to allow faster processing, provided it has enough power (BluSunrize)
    - Can increase its speed by up to 700%!
- Change Revolver Speedloader recipe to use Duroplast, making it cheaper and more enticing to use (BluSunrize)
- Change Revolver ammunition to be much cheaper. This is an experimental change and may need more tuning in the future (BluSunrize)
- Fix crash with the Engineer's Workbench when VBOs are disabled (Malte)
- Fix z-fighting issues on the mod workbench with rubidium (Malte)
- Fix visual issues when inverting powered lanterns (Malte)
- Fix shift-clicking an output item in the mod workbench adding to the output stack (Malte)
- Fix global sound of mixer as a result of stereo audio file (Malte)
- Fix assemblers not outputting revolver speedloaders, because they were considered ingredients in their own recipe (BluSunrize)
- Fix Jade plugin not displaying translated text for hemp growth (BluSunrize)
- Fix sawblade projectiles not rendering and having a very broken piercing-functionality (BluSunrize)
- Fix sawblade projectiles not using the config damage multiplier for the railgun (BluSunrize)
    - In exchange, they no longer count as armorpiercing (they shouldn't have been to begin with)
- Fix mixer not rendering its contained fluid (BluSunrize)
- Fix heavy plated shield being raised when using mainhand items (Malte)
- Fix toolbox being allowed to be placed in partial blocks, deleting them (BluSunrize)
- Fix possible crash with Vanilla Tweaks and hemp seeds (BluSunrize)
- Translations Added/Updated: zh_cn (RevenXXX-a, mc-kaishixiaxue), uk_ua (SKZGx, hnufelka)

##### Version 1.19.3-9.3.0-163
- All features from 9.2.0
- Fix cloche GUI not being transparent (BluSunrize)
- Fix cloche farmland having a missing texture (BluSunrize)

##### Version 1.19.2-9.2.0-162
- All features from 8.4.0
- Add recipes to cut wooden slabs on the sawmill (BluSunrize)
- Change mineral veins to generate without needing to tag a biome for them (Malte)
- Add a warning when using a version of Optifine that breaks the PoseStack (Malte)
- Change Revolvers to trigger Sculk sensors! (BluSunrize)
    - By default, it uses the event for fired projectiles
    - If your revolver has the "Noise" perk of -60% or better, it will not trigger any events
    - If your revolver has the "Noise" perk with a positive value, the sensors will treat it like an explosion instead
- Add recipes for new 1,19 items (BluSunrize)
    - Mangroves can go in the sawmill
    - Mud can be made in the bottling machine
    - Moss can be grown on cobblestone in the cloche
- Fix rendering issues with Railgun, Chemthrower and Shield (Malte)
- Fix IO buttons in the circuit table (Malte)
- Fix conveyor models to work properly with stained glass, eliminate clipping and fix depthbuffer issues (BluSunrize)

##### Version 1.18.2-8.4.0-161
- Overhaul the Accumulator Backpack (BluSunrize)
    - Now uses a fancy OBJ model
    - It's now crafted as an empty frame, to put an accumulator in!
    - Existing packs will automatically migrate to an LV accumulator.
    - May be equipped with a shader or banner, to proudly display over your shoulder
    - The Charging Antenna upgrade will charge the pack by attaching to overhead wires
    - The Miniaturized Tesla Coil will electrocute your attackers
    - The Induction Charger will charge your whole inventory
    - There may be a new secret advancement too ;)
- Change IEOBJs to be used from the API (Malte)
- Change recipe display in the manual to allow specifying IDs instead of outputs (Malte)
- Add a bisexual pride shader (BluSunrize)
- Add the Rock Softening Acid upgrade for the Mining Drill, giving it Fortune 3! (BluSunrize)
- Increase the speed of rockcutter and grinding disk (BluSunrize)
- Fix some issues with models being mirrored (Malte)
- Fix shaders and potion buckets not showing up in JEI (Malte)
- Fix arc recycling accidentally modifying the empty ItemStack constant (Malte)
- Fix tooltips on capacitors not showing energy stored (BluSunrize)
- Cleanup and optimize Blueprint rendering (Malte)
- Fix bucketwheel not querying block particles properly (Malte)
- Fix config settings not being applied to crafttweaker recipes (BluSunrize)
- Fix tool GUIs being shifted ro the left by subtitles, this isn't necessary since we ove subtitles upwards instead (BluSunrize)
- Fix crusher not animating or making noise when crushing entities (BluSunrize)

##### Version 1.19.3-9.2.0-160
- First alpha release for 1.19.3
- All features from 8.4.0 (not yet released)

##### Version 1.19.2-9.1.2-159
- Fix broken villager workstations

##### Version 1.19.2-9.1.1-158
- All features from 8.3.1
- Fix crashing when opening the manual with Lithium installed (Malte)
- Fix mineral veins only generating in the overworld (BluSunrize)
- Fix switched filters and buffers in item batcher GUI (Malte)

##### Version 1.18.2-8.3.1-157
- Add pride shaders! (BluSunrize)
- Change workstations for Villagers to stop using vanilla blocks (BluSunrize)
    - Structural Engineer -> Turntable, was Engineer's Crafting Table
    - Machinist -> Engineer's Crafting Table, was Anvil
- Fix multiblock preview in the manual when Radium is installed (Malte)
- Fix issue related to shaderbag rarities (Malte)
- Fix sawmill sounds and document that removing a spinning blade is painful (Silfryi)
- Fix crash with arc furnace recipes loading (Kanzaji)
- Fix hoppers being able to insert crates into other crates (Malte)
- Fix multiple issues with IE's shaders (BluSunrize)

##### Version 1.19.2-9.1.0-156
- All features from 8.3.0
- Allow dynamic addition of Blueprints, not possible on 1.18 (Malte)
- Fix issue with chat messages (Malte)
- Fix sound attenuation issues (Malte)
- Fix syncing of shaders on minecarts (Malte)
- Fix render issues, and an optifine related crash (Malte)

##### Version 1.18.2-8.3.0-155
- Add process indicator to the Cloche's GUI (BluSunrize)
- Add sounds for Assembler, Bottling Machine, Coke Oven, Fermenter, Mixer, Preheaters, Refinery and Sawmill (Silfryi)
- Add additional items to be recycled by the Arc Furnace (Silfryi)
- Update and fix computer compat (Malte)
- Overhaul of particles (Silfryi)
    - Add and expand particles for Crusher, Sample Drill & Excavator
    - Stop Sawmill blade when machine is off
    - Increase particles on the Mixer and Diesel Generator
    - Add smoke to Arc and Blast Furnaces
    - Update Arc Furnace texture
- Update Crusher model to have interlocking wheels (TeamSpen210)
- Update manual entry for the Tesla coil to note use of the screwdriver (gorberto)
- Change bounding boxes on Silo and Tank to be stepped at the top (BluSunrize)
- Increase chances for Lead, Silver and Deepslate Nickel Ores to spawn (BluSunrize)
- Fix CraftTweaker integration for the Cloche (Witixin1512)
- Fix rounding errors in comparator output (Malte)
- Fix some JEI issues (Malte)
- Fix data loss in toolboxes (Malte)
- Fix issues in mixer data loading (Malte)
- Fix revolvers not acquiring perks from all their components (gorberto)
- Fix diesel generator running infinitely on too little fuel (BluSunrize)
- Fix issues with Spanish translations (Quezler)
- Fix issues with Italian translations (Skeevert)
- Fix manual not opening when on a lectern (Malte)
- Fix very large multiblocks not displaying well in the manual (Malte)
- Fix shader bag conversion recipe (Malte)
- Fix incorrect item router behavior (Malte)
- Translations Added/Updated: zh_cn (IdealNightOcean), ja_jp (karakufire)

##### Version 1.18.2-8.2.2-154
- Fix world loading crash with Create Crafts & Additions installed (BluSunrize)

##### Version 1.19.2-9.0.0-153
- Initial release for 1.19.2

##### Version 1.18.2-8.2.1-152
- Add the Collapsible Glider, an early game, less powerful Elytra
    - Sound for the glider taking damage are by Iain McCurdy, licensed under CC BY 4.0
- Add spoil to the Excavator, instead of empty buckets, it collects cobble and gravel instead (Silfryi)
    - In the nether you get netherack, gravel and basalt
- Add new mineral veins in the nether! (Silfryi)
    - Archaic Digsite now contains various blackstone blocks
    - Cooled Lava Tubes provide magma, obsidian and smooth basalt
    - Soul Silt provides soul sand, soul soil and gravel
- Add recipes to strip insulation from LV and MV wires again (Silfryi)
- Fix fluid pump showing as 2x in the required materials of the bottling machine (BluSunrize)
- Fix sawblade accidentally silktouching leaves (BluSunrize)
- Fix iron golems dropping shader bags reserved for bosses (BluSunrize)
- Fix diesel generator running endlessly without particles and animation (BluSunrize)
- Fix error being thrown in log when Pneumatricraft reads our villager trades (BluSunrize)
- Fix crusher not accelerating if it is given enough power (BluSunrize)

##### Version 1.18.2-8.2.0-151
- Overhauled the Bottling Machine (BluSunrize)
    - Had its internals adjusted to match other multiblocks
    - Now allows for multiple inputs in its recipes
    - Allows holding items until full
    - Improved documentation in the manual!
- Updated recipe for empty shells for revolver cartridges (BluSunrize)
    - Can now be made with resin in the bottling machine
    - In exchange, the normal recipe outputs a little less
- Change mineral survey tools to send positive messages to chat, makes it easier to look back on them (BluSunrize)
- Change a bunch of recipes! (BluSunrize & Silfryi)
    - Various tool upgrades got cheaper
    - Revolver recipe more closes matches the pattern of other tools
    - Many electrical blocks (transformers, tesla coils, external heaters) had their recipes changed
    - Connectors and relays now accept all colours of terracotta
    - Insulating glass is now made in the alloy kiln
    - Metal wallmounts are now crafted in larger amounts
- Add the Grinding Disk for the Buzzsaw, allowing it to quickly cut through metal (BluSunrize)
    - Also behaves like an axe when rightclicking, so it can strip woods and clean copper
- Add a blacklist for buzzsaw treecapitation (BluSunrize)
    - This is done with the block tag "immersiveengineering:buzzsaw/tree_blacklist"
    - By default, this blacklists Dynamic Trees, because they have their own mechanic for felling
- Add Ersatz Leather, a leather alternative crafted with hemp fabric and beeswax or plant oil (BluSunrize)
- Add recipes to break down prismarine blocks and press blaze powder into rods (BluSunrize)
- Add additional uses for slag (BluSunrize, Silfryi)
    - Slag glass blocks 50% of light passing through it and works as an insulator for HV relays
    - Slag gravel speeds up water plants growing on it (kelp) and can be crushed into sand
    - Slag recycling is now documented in the manual
- Improve the railgun (BluSunrize)
    - End rods are now valid projectiles, they are super effective against Endermen
    - All railgun projectiles have perfect accuracy now (but are still affected by gravity)
    - Tridents now have greatly increased speed (and thus range) when fired from a railgun
    - The manual now documents the various types of ammunition in more detail
- Change assembler to only use a single tank for a fluid, to allow better "filtering" (BluSunrize)
- Change Diesel Generator consume fuel every 10 ticks, allowing for more granular burntime configurations (Silfryi)
- Fix assembler creating buckets from nowhere for recipes that would consume them (BluSunrize)
- Fix advancements for upgrading drill, railgun, buzzsaw and revolver to being triggered (BluSunrize)
- Fix windmill sails not being saved properly (BluSunrize)
- Fix crash with CraftTweaker (BluSunrize)
- Fix crash with Minecolonies pathfinding (BluSunrize, Raycoms)
- Translations Added/Updated: ja_jp (karakufire)

##### Version 1.18.2-8.1.0-150
- The Plastics update! (BluSunrize)
    - New fluids:
        - Acetaldehyde, made from ethanol in the refinery with a silver plate catalyst
        - Phenolic Resin, made from ethanol and creosote in the refinery
    - New material: Duroplast, made from filling molds with resin in the bottling machine!
    - New crating components: Electronic components
        - Replaces circuit boards in a bunch of recipes, made some things cheaper, some more expensive
        - Shifts the tech tree a bit, places turrets and railgun behind the creation of duroplast
        - Textures courtesy of SteelBlue8!
    - Fiberboard blocks
        - Made from sawdust and resin, they can be used like wooden planks in all recipes!
- The Manual got an update! (BluSunrize)
    - Early furnaces and workbenches have their own category
    - Some entries got re-written / clarified
    - Bottling and Mixing recipes now display in the manual
- The Advancements got overhauled! (BluSunrize)
    - IE now has 54 different advancements
    - Some boring ones were removed
    - Some cool new ones were added
    - A new *secret* advancement got added too
    - Advancements have been split into 3 different tabs to make them more readable
- Add IMPLY and NIMPLY logic circuits (sashafiesta)
- Add a bunch of extra tools to the toolbox (BluSunrize & Silfryi)
    - Spyglass, Clock, Compass, Fishing Rod
    - Engineer's Manual, various wrenches from other mods
- Remove unused config options (Malte)
- Add a new tag to blacklist certain armor for attaching the Accumulator Backpack (Malte)
    - "immersiveengineering:powerpack/forbid_attach"
- Add new tags to control which mods drop shader grabbags (BluSunrize)
    - By default, any mob with 100 or more HP (Wither, Ender Dragon, Warden) will drop a bag
    - "immersiveengineering:shaderbag/blacklist" is for EntityTypes that shouldn't drop a bag despite having so much health
        - The Wither is on that blacklist by default, but it can be removed from it with a data pack
    - "immersiveengineering:shaderbag/whitelist" is for EntityTypes that should drop a bag even if their health is lower than 100
        - This list is empty by default, can be added to with a data pack
- Allow items to be added to the toolbox by right-clicking, just like vanilla bundles (BluSunrize)
- Use ToolActions to identify tools for the toolbox, adding support for TConstruct items (BluSunrize)
- Add a recipe to crush amethyst blocks into crystals (BluSunrize)
- Add a new recipe type for adding biome-based modifiers to the windmill (BluSunrize)
    - By default, windmills now spin 15% faster in ocean biomes, as an example implementation
    - The rest is up to data pack makers!
- Fix crashes as a result of empty potion recipes (Malte)
- Fix crashes triggered by wires burning (Malte)
- Reduce size of share tag for wooden crates (Malte)
- Fix arc recycling tags accidentally using "forge" namespace instead of "immersiveengineering" (BluSunrize)
- Fix stackable fluid containers duplicating when crafting treated wood (BluSunrize)
- Fix dupe bug when repairing hammers or wirecutters (BluSunrize)
- Fix revolver pieces from villager trades not showing their perks (BluSunrize)
- Fix "inventory" label being poorly positioned in various GUIs (BluSunrize)
- Fix OC2 compat using an old name (leedagee)
- Translations Added/Updated: ru_ru (Smollet777)

##### Version 1.18.2-8.0.2-149
- Re-add compat for Craftweaker (Malte)
- Add coal coke to the "minecraft:coals" tag, allowing it to be used in crafting campfires (BluSunrize)
    - Unfortunately, vanilla torches don't use that tag, so they can't be made with coke. It's very silly.
- Allow the hammer, wirecutters and faraday suit to be repaired with their respective materials (BluSunrize)
- Add new tags to interact with arc furnace recycling: (BluSunrize)
    - "immersiveengineering:recycling/whitelist" -> Items to be made recycle-able
    - "immersiveengineering:recycling/blacklist" -> Items to exclude from recycling
    - "immersiveengineering:recycling/ignored_components" -> Items to ignore as components (such as tools used in crafting)
- Restructure some manual entries for better flow and no weird page breaks (BluSunrize)
- Change Fluid & Item Routers to keep their configuration when broken, so they can be moved more easily (BluSunrize)
- Fix display of fluid tanks in JEI (BluSunrize)
- Fix container items being lost in the alloy kiln (Malte)
- Fix desync of the blueprint in the automated workbench (Malte)
- Fix rotation issues with transformers and razor wire (Malte)
- Fix crash with other mods adding new levels of Rarity for shaders (Malte)
- Fix suggestions for the mineral command crashing clients in multiplayer (Malte)
- Fix crash on the core sample tooltip (Malte)
- Fix crafting table not dropping its contents (Malte)
- Fix hammer-crushing recipes only working once until the game is restarted (BluSunrize)
- Fix shaders not unlocking in the manual (BluSunrize)
- Fix engineer villager houses being too common now since 5 unique variations were added (BluSunrize)
- Fix some recipes having an unachievable unlock condition (BluSunrize)
- Correctly tag slabs, stairs and tools (Malte)

##### Version 1.18.2-8.0.1-147
- Add signal-threshold function for probe connectors (BluSunrize)
- Add cloche growing for glow berries (BluSunrize)
- Add ethanol fermentation for honey, glow berries and sweet berries (BluSunrize)
- Add beds to all engineer's villager houses (BluSunrize)
    - Also, all houses are now guaranteed to have a workstation in them
- Add sneak + right-click feature to swap mining drill to single-block mode (BluSunrize)
- Add new textures for copper items (Silfryi)
- Add the Multimeter (MalkContent)
    - It replaces the Voltmeter and can now read Redstone levels!
    - It can be put in the "tools" area of the toolbox now!
- Add bundle tooltips for storage crates and the toolbox (BluSunrize)
- Change recycling code to be a little less hacky (Malte)
- Change sounds for metal press and diesel gen to be mono (ArchieBeepBoop)
    - This fixes them not reducing in volume over distance
- Change hemp fiber to be compostable with a 15% chance (BluSunrize)
- Fix some hard incompatabilities with Optifine (Malte)
- Fix engineer's crafting table losing its inventory (Malte)
- Fix placement and colour of various "Inventory" texts (BluSunrize)
- Fix faraday suit incorrectly accepting enchantments in the anvil (BluSunrize)
- Translations Added/Updated: ru_ru (Michael-Soyka)

##### Version 1.18.2-8.0.0-146
- Re-add compat for OpenComputers2 (Malte)
- Add support for running gametests in development (Malte)
- Change herbicide recipe to use sulfur instead of nitrate (BluSunrize)
- Clean up wire collision code (Malte)
- Change wire rendering to render wires as part of the chunks they pass through (Malte)
- Change crates to extend vanilla loot containers (Malte)
- Change JEI GUI for Blast Furnace recipes to show the process time (BluSunrize)
- Fix in-hand GUI for Drill & Buzzsaw rendering their head/blade too dark (BluSunrize)
- Fix sync issues and dupe bug in the buzzsaw (BluSunrize)
- Fix incorrect collision box on Concrete Chunk (BluSunrize)
- Fix circuit table not dropping its contents (BluSunrize)
- Fix JEI not showing refinery recipes when checking the uses of a catalyst (BluSunrize)
- Fix manual not resetting when reloading resources (Malte)
- Fix silo item handler not fully complying with method contracts (Malte)
- Fix floodlight not using its active texture (Malte)
- Fix tags for storage blocks and ores (Malte)
- Fix fluid pipes voiding fluid on chunks (un-)loading (Malte)
- Fix turrets not facing the way they are placed (Malte)
- Fix dupe bugs in the Engineer's Crafting Table (Malte)
- Fix incorrect outputs in the logic unit when inverting gates are involved (Malte)
- Fix left-hand rendering for drill, chemthrower, buzzsaw (Malte)
- Fix selection box of covered extracting conveyor (BluSunrize)
- Fix alloy smelter not handling swapped input slots (BluSunrize)
- Mirror models in code rather than using separate OBJ files (Malte)

##### Version 1.18.1-7.1.0-145
- The "Chemical Engineering" update! (BluSunrize)
    - Refineries now use a catalyst, Biodiesel uses Nitrate Dust
    - Add a manual entry for Nitrate and Sulfur to explain where to get them and what they are for
- Add some fake JEI recipes for filling buckets in the bottling machine (BluSunrize)
- Add a new API interface that allows pressurized fluid in- and output above normal levels (BluSunrize)
- Add fermentation recipe to turn beetroots into ethanol (BluSunrize)
- Add arc smelting recipe for raw metal blocks (BluSunrize)
- Change Electrician villager workstation to be the Circuit Table (BluSunrize)
- Update JEI handlers for Coke Oven, Blast Furnace and Alloy Smelter to have some animated doodads (BluSunrize)
- Update manual entry for fluid pipes to document that they can be dyed (BluSunrize)
- Fix Lightning Rod crashing when built (BluSunrize)
- Fix texture glitches on Refinery model (BluSunrize)
- Fix pipes and pumps transferring fluids much slower than planned, because pressurization didn't work (BluSunrize)
- Fix manual entries referring to "Engineer's Hammer" when "Engineer's Screwdriver" is the correct tool (Vapaman)
- Fix banner patterns crashing in the loom (BluSunrize)
- Fix sawmill not updating its visuals while processing (BluSunrize)
- Fix accumulator backpack not being equippable with rightclick (BluSunrize)
- Fix tooltips for tool upgrades being white instead of grey (BluSunrize)

##### Version 1.18.1-7.0.1-144
- Overhaul liquid concrete (BluSunrize)
    - Only dries into full blocks or slabs
    - Dries from teh outside inwards so it doesn't kill its source first
    - Dries much slower so it has more time to fill an area
    - Partial concrete blocks are made in the stonecutter
- Change "Capacitors" to "Accumulators" (BluSunrize)
    - They now require redstone acid, which has some interesting interaction with copper
    - The lore changed a bit!
- Allow Circuit Table to edit the color configuration of existing circuits (BluSunrize)
- Fix missing normals on models, making them look too dark (Malte)
- Fix various JEI integrations (BluSunrize)
    - Move "show recipes" button in the mixer
    - Fix ghost slot handling in the assembler
- Fix crash when trying to use a manual on a lectern (BluSunrize)
- Fix incorrect placement for conveyors (BluSunrize)
- Fix assembler not consuming fluids correctly (BluSunrize)
- Fix rendering for the bucket wheel not showing its contents (BluSunrize)
- Translations Added/Updated: ru_ru (Bytegm), ja_jp (karakufire)

##### Version 1.18.1-7.0.0-142
- First (beta) release for 1.18.1
- Fix ALL the issues that came with these updates (Malte)
- Add deepslate ores, raw ores and update textures for IE ores (BluSunrize & Malte)
    - Raw ores crush into grit, with a 33% chance for double output
    - Raw ores smelt in the arc furnace with a 50% chance for double output
    - Ore blocks can still be crushed for guaranteed double output (and secondary grits)
    - IE oregen is still highly configurable
- Remove copper ore, since vanilla adds it (Malte)
- Change recipes for the precision scope to use the vanilla spyglass (BluSunrize)
    - The scope can also be mounted on the revolver now
- Add snow to the thermoelectric generator, buff ice a little as a result (BluSunrize)
- Add manual entry for Villagers (BluSunrize)
- Add chance-based outputs for the Arc Furnace (Malte)
- Add full tag support for the toolbox slots (Malte)
- Add expanded recycling support for vanilla metal items & IE blocks (BluSunrize) 
- Overhaul villager trades (BluSunrize)
    - Inventory of the structural engineer changed quite a bit
    - Other trades had their XP values and max uses changed
    - Price modifiers where changed resulting in less drastic discounts
- Fix issues with the hammer, allowing it to break scaffolding quicker and be enchanted at an anvil (BluSunrize)

##### Version 1.16.5-5.0.6-141
- Re-add the fan animation on blast furnace preheaters (Malte)
- Add compat for Jade (Malte)
- Allow drill- and generator fuels to be added using datapacks (Malte)
- Allow water wheels to work when placed facing away from the dynamo (Malte)
- Fix earmuffs stacking to 64 (Malte)
- Fix wire rendering on the capacitor backpack (Malte)
- Fix blocks with side configuration rendering their model 7 times (Malte)
- Fix broken workbench GUI rendering (Malte)
- Fix shift-click placing items in output slots (MalkContent)
- Fix redstone interface connectors not resetting their output when broken (Malte)
- Fix Xaero's world map breaking post models (Malte)
- Fix lag with very full crushers (Malte)
- Fix desync of item stack sizes on entities (Malte)
- Fix dupe bug with the metal press (Malte)
- Fix some recipes not showing in the manual (BluSunrize)
- Potentially fix rare wire crashes (Malte)
- Re-enable Chisels&Bits compat (Malte)
- Translations Added/Updated: ko_kr (PixVoxel, FreddyYJ), it_it (maicol07), ja_jp (karakufire), fr_fr (Juknum)

##### Version 1.16.5-5.0.5-140
- Fix comparators not working on IE blocks (Malte)
- Fix rendering of wires connected to razor wire (Malte)
- Fix rendering of colored conveyor stripes (Malte)
- Fix additional open spot on chutes with upward-sloping conveyors (Malte)
- Reduce redstone wire network update spam (Malte)
- Fix wrong branch being used for update JSON (Malte)
- Add wires to forge:wires tag (Malte)
- Add potion effect descriptions for compat with Just Enough Effect Descriptions (BeepSterr)
- Translations Added/Updated: ko_kr (Taki_B, PixelQuest), es_* (FrannDzs), en_us (Tiyth, hechtiQ)

##### Version 1.16.5-5.0.4-139
- Improve behavior of conveyors near unloaded/non-ticking chunks (Malte)
- Fix inconsistent redstone behavior of strip curtains (Malte)
- Fix redstone connectors not working on the main input of comparators (Malte)
- Fix logic units not clearing registers between cycles (Malte)
- Fix issues when adding or removing boards from the logic unit (Malte)
- Fix rare crash involving redstone wires (Malte)
- Fix race condition when addons add config getters (Malte)
- Fix issues with the workbench UI (Malte)
- Fix item entity output location for the auto-workbench (Malte)
- Fix wire desync (Malte)
- Fix render layer for diesel generator (Malte)
- Fix revolver bullet desync (Malte)
- Fix invisible fluid splash particles (Malte)
- Fix crash when building an excavator without a vein (Malte)
- Fix some recipes damaging instead of consuming their ingredients (Malte)
- Fix pump vanishing when the bottling machine is broken (Malte)
- Reduce block updates on multiblocks (Malte)
- Fix Spanish translation for lightning rods (TwistedGate)
- Fix recipes for graphite dust not loading with newer versions of JEI (BluSunrize)

##### Version 1.16.5-5.0.3-138
- Add Nether Fungi to the sawmill's recipes (BluSunrize)
- Change Engineer's Workbench, Circuit Table and Crafting Table to be waterloggable (Malte)
- Change Transformers to make attaching wires easier (Malte)
- Fix crash when breaking a feedthrough (Malte)
- Fix crash with empty ingredients in JEI (Malte)
- Fix shaders not working on minecarts (Malte)
- Fix table rendering in the manual (Malte)
- Fix comparator updates on multiblocks (Malte)
- Various performance improvements, crash protection and logging (Malte)
- Translations Added/Updated: es_* (docanuto)

##### Version 1.16.5-5.0.2-137
- Fix dedicated server crash with charging stations and cloches (Malte)

##### Version 1.16.5-5.0.1-136
- Move potion buckets to vanilla brewing tab (Malte)
- Store connections in vanilla structure/template files (Malte)
- Allow connections to be cut mid-air even when a block is highlighted (Malte)
- Re-add particles for overloaded wires (Malte)
- Changed brass recipe to require less copper and match Create (BluSunrize)
- Fix villager houses not generating (Malte)
- Fix inconsistent behavior of the fluid router (Malte)
- Fix the maintenance kit not configuring earmuffs and fluorescent tubes (Malte)
- Fix the bottling machine not breaking the pump correctly when disassembled (Malte)
- Fix particles for the charging station partially not rendering (Malte)
- Fix rockcutter blade not working for the first block after switching blades (Malte)

##### Version 1.16.5-5.0.0-135
- Clean up the API (Malte)
    - THIS IS A BREAKING CHANGE!
    - Don't expect addons to work without being updated!
- Add buckets for potion fluids (Malte)
- Add ComputerCraft compat (Malte)
- Add Redstone Logic system (BluSunrize)
    - New Block: Circuit Table, use it to make logic circuits!
    - New Block: Logic Unit, it plugs into redstone wires to do logic operations!
    - Changed recipes for circuit boards! 
- Add missing death message for punching someone into a live wire (BluSunrize)
- Add crusher recipe for netherwart blocks (BluSunrize)
- Add TConstruct compat, allowing Hemp to be harvested with a kama (BluSunrize)
- Change Item Routers: (BluSunrize)
    - Allow items to pass into unfiltered outputs if the filtered location is full
    - Change "OreDictionary" to "Tags", and update icons & manual documentation
- Improve documentation for Feedthrough Insulators (IKnewOne)
- Improve container logic for the Workbench (MalkContent)
- Adjust internal handling of Cloche recipes, allowing more dynamic recipes for mod compat (InfinityRaider) 
- Fix servers kicking players for flying when using the skyhook or standing on conveyors (Malte)
- Fix the skyhook moving players to NaN coordinates in some cases (Malte)
- Fix particles indicating garden cloche power state (Malte)
- Fix issues with arc recycling recipe generation (Malte)
- Fix mineral veins not being generated in some areas (Malte)
- Fix structural arms not using their blockstate property (Malte)
    - This made them near unusable in structure files
- Fix dupe bug when interacting with barrels with stacked fluid containers (Malte)
- Fix duplicate files being included in the IE jar (Malte)
- Fix inconsistent wire length checks (Malte)
- Fix drills breaking stone without heads or fuel (Malte)
- Fix the metal press dropping two molds when broken with a drill (Malte)
- Fix the sawmill not dropping its sawblade when broken (Malte)
- Fix IE recipes involving water buckets not working in some scenarios (Malte)
- Fix issues with multithreaded loading (Malte)
- Fix material list for the combined excavator (Malte)
- Fix registering of potion fluid and recipes (Malte)
- Fix drill speed exploit in water (Malte)
- Fix issues related to removed mineral veins (Malte)
- Fix multiblocks not forming when blocks are waterlogged (Malte)
- Fix visual disconnects on fluid pipes (Malte)
- Fix workbench deleting adjacent blocks (BluSunrize)
- Fix powered lanterns not being flipable (BluSunrize)
- Fix villager requiring IE steel ingots instead of config preference (BluSunrize)
- Fix turret GUI not allowing unfired bullets to be removed (BluSunrize)
- Fix transparency render glitch with razor wire (BluSunrize)
- Fix drill being usable without fuel (BluSunrize)
- Fix dupe bug with capacitor backpacks (BluSunrize)
- Fix craftweaker compat allowing invalid fluid inputs (BluSunrize)
    - They now crash during Craftweaker loading, causing an appropriate error in the CT log
- Fix posts having different bounding boxes on different levels, making them hard to climb (TwistedGate)
- Fix performance issue with empty fluid outlets (Malte)
- Potentially fix lanterns and floodlights with OptiFine (Malte)
- Translations Added/Updated: de_de (astrutz)

##### Version 1.16.5-4.2.4-134
- Fix issues with the manual (BluSunrize)

##### Version 1.16.5-4.2.3-133
- Add Curios compat for the ear defenders (BluSunrize)
- Remove unused option for crushing (BluSunrize)
- Change teslacoil to apply anti-teleport debuff before applying damage (BluSunrize) 
- Fix a crash in recycling, occured with Silent Gear (BluSunrize)
- Fix drill and buzzsaw not working (Malte)
- Fix issues with Russian and Chinese translations (BluSunrize)
- Fix capacitor backpack not working when equipped as a Curios (BluSunrize)
- Fix metal ladders not being climable on newer Forge versions (BluSunrize)

##### Version 1.16.5-4.2.2-132
- Added/updated compatability for Tinkers Construct (BluSunrize)
- Added Curios compatability for the capacitor backpack (BluSunrize)
- Added a manual entry for Herbicide, describing what it does and how to get it (BluSunrize)
- Changed villagers to respect the ore preference config for trades (BluSunrize)
- Changed fluid outlet to allow inverting the redstone signal (BluSunrize)
    - Also fixed buggy area detection
- Replaced config option for arc furnace recycling with documentation on how to use a datapack instead (BluSunrize)
    - It was broken anyway, the option didn't work. Using configs for recipes is an outdated practice.
    - Datapacks are the future (right now anyway), so deal with it.
- Fixed crash related to fluid pipe extraction (Malte)
- Fixed crusher not accepting items in top layer (Malte)
- Fixed crashes related to tag loading (Malte)
- Fixed missing particles on some blocks (Malte)
- Fixed errors when using the buzzsaw as a weapon (Malte)
- Fixed potion bullets crashing when fired from turrets (BluSunrize)
- Fixed crash related to using a railgun offhanded (BluSunrize)
- Fixed coresample drill incorrectly starting its animation (BluSunrize)
- Fixed flipped faces on metal press model (BluSunrize)
- Fixed arc furnace particle crash (Malte)
- Fixed refinery wasting fluid (Malte)
- Fixed localization of Minecart entities (SkySom)
- Fixed crash with Mekanism in fluid recipes (Malte)
- Fixed jerrycan (and other things) crashing on Forge minor version 40+ (BluSunrize)
- Fixed jerrycan disappearing when used in crafting (ConductiveFoam)
- Fixed manual not scaling its GUI up, making it hard to read (BluSunrize)
    - Also adds a client config option for manual scale
- Made post connections more reliable to floating-point errors in voxel shapes (Malte)
- Fixed formatting on links in the manual (BluSunrize)
- Translations Added/Updated: es_es (FrannDzs), zh_cn (frank89722), ru_ru (DonorTrap, Alepod)

##### Version 1.16.5-4.2.1-131
- Added missing mixer recipe for herbicide (BluSunrize)
- Added Craftweaker compatability for Mineral Veins, improved documentation (Jared)
- Added compatability for TheOneProbe (RobustProgram)
- Fixed crash related right-click events (Malte)
- Fixed missing particle textures for waterwheel, windmill and excavator (Malte)
- Fixed current transformers calculating wrong values (Malte)
- Fixed tanks and silos being set to output by default (BluSunrize)

##### Version 1.16.5-4.2.0-130
- First 1.16.5 release
- Allowed CC:Tweaked computers to emit bundled redstone signals using interface connectors (Malte)
- Allowed IE projectiles to trigger target blocks (BluSunrize)
- Allowed the Engineer's Manual to be placed on a lectern (BluSunrize)
- Added full Craftweaker compatability (kindlich, Jared, SkySom)
- Changed redstone connectors to no longer power other connectors of the same color on the same network (BluSunrize)
- Changed redstone connectors to not emit weak signals through their "tip" (BluSunrize)
- Changed redstone connectors GUI to be closed with the "Inventory" keybind (BluSunrize)
- Changed common config to explain that all the options are in world configs now (BluSunrize)
- No longer crash the game if manual loading fails (Malte)
- Fixed crash related to the drill (Malte)
- Fixed logged exceptions when attacking entities with a drill with a shader (Malte)
- Fixed coke oven etc showing progress if the GUI is reopened after the process is done (Malte)
- Fixed CME with villager registration code (Malte)
- Fixed tesla coil destroying blocks when placed sideways (Malte)
- Added tags to determine what items can go into crates (Malte)
    - These also contain crate minecarts now
- Fixed crash when opening the manual when a key is bound to \ or $ (Malte)
- Fixed crash when crafting torches from creosote (Malte)
- Fixed broken special characters (Malte)
- Fixed some performance issues with pipes (Malte)
- Fixed water wheel placement (Malte)
- Fixed inverting of breaker switches (Malte)
- Fixed crash with posts and smooth lighting (Malte)
- Fixed crash with invalid multiblock selection tags on the hammer (Malte)
- Fixed modded ores sometimes being used instead of the vanilla ones (Malte)
- Fixed crash related to turrets (Malte)
- Fixed rotation of the external heater to match description in the manual (BluSunrize)
- Fixed crashes when the time modifier for refinery recipes was changed to <1 (BluSunrize)
- Fixed spamming error in the shield magnet feature (BluSunrize)
- Fixed potential infinite loop in workbench renderer (BluSunrize)
- Fixed rotation issue with bottling machine renderer (BluSunrize)
- Fixed rare crash when breaking some blocks with a drill (BluSunrize)
- Fixed crushers not accepting input from hoppers at the top (BluSunrize)
- Translations Added/Updated: zh_cn (Fodoth-jinzi89, RMSCA), ja_jp (karakufire, koh-gh)

##### Version 1.16.4-4.1.2-129
- Re-added client side commands to clear render caches and reset the manual (Malte)
- Replaced JS coremods and access transformers with Mixins (Malte)
- Changed Dropping Conveyors to be able to drop *into* other blocks, if collision allows (TeamSpen)
- Changed Sawblades and Drillheads to not allow enchantments (ConductiveFoam)
- Changed Sawmill to output a comparator signal based on the integrity of the saw (BluSunrize)
- Limit creosote-burning in furnaces to buckets to avoid issues with other containers (Malte)
- Fixed issues with crafting recipes involving fluid and tanks from other mods (Malte)
- Fixed crashes on login related to multiblock rendering (Malte)
- Fixed obscure crash when holding the voltmeter in empty chunk sections (Malte)
- Fixed the front part of the floodlight not rendering (Malte)
- Fixed missing page in excavator manual entry (Malte)
- Fixed some pathfinding issues with MineColonies NPCs (Malte)
- Fixed concurrency issues with the manual (Malte)
- Fixed connectors and multiblock parts being picked up by Mekanism cardboard boxes (Malte)
- Fixed overflow issue with mineral veins far away from the player (Malte)
- Fixed item batchers not dropping their contents when broken (Malte)
- Fixed a crash with the tesla coil (Malte)
- Fixed a bug with conveyor belts and structure blocks (Malte)
- Fixed scrolling in the turret GUI (Malte)
- Fixed default mineral dimension blacklist in the config (Malte)
- Fixed crash related to projectiles fired by turrets (Malte)
- Fixed CraftTweaker's `addJSONRecipe` for IE recipes (SkySom)
- Fixed buzzsaw not updating its enchantments until a block is broken (Malte)
- Fixed mineral veins crashing on world load (SkySom)
- Many minor performance improvements (Malte)

##### Version 1.16.4-4.1.1-128
- First 1.16.4 release
- Includes the changes from 1.16.3-4.1.1-127

##### Version 1.16.3-4.1.1-127
- Fixed Arc Recycling accessing Tags too early (Malte)
- Fixed null calls in IE's config for item max damage (BluSunrize)
- Fixed electrodes not taking and displaying damage properly (BluSunrize)
- Fixed retrogen crashing (BluSunrize)
- Also includes the changes from 1.16.1-4.1.1-126

##### Version 1.16.1-4.1.1-126
- Added Basalt and Blackstone to the blocks that the survey tool works on (BluSunrize)
- Changed Ancient Debris to produce twice the netherite scrap in the Arc Furnace (BluSunrize)
- Fixed steel hoes doing too much damage. As a result of this fix, all other steel tools now match diamond in damage (BluSunrize)
- Fixed missing localization for metal barrel minecart (BluSunrize)
- Fixed transparency issues with banners (BluSunrize)
- Fixed redstone connector GUI opening for everyone on a LAN world (BluSunrize)
- Also includes the changes from 1.15.2-4.1.1-125

##### Version 1.15.2-4.1.1-125
- Added sawdust flooring! It's a snow-like block made from sawdust! (BluSunrize)
- Added indicators for attached Preheaters to the Blast Furnace GUI (BluSunrize)
- Changed recipe for Pressurized Air Tank to use blue dye instead of lapis (BluSunrize)
- Changed item and fluid routers to not output to unfiltered sides, if a filter exists (BluSunrize)
    - This behavior now matches what is described in the manual
    - Existing systems using routers may now derp up a little
- Changed drill overlay to not show all targeted blocks when sneaking (BluSunrize)
- Fixed spawn interdiction code for lanterns (Malte)
- Fixed flickering when the bucket wheel is formed (MalkContent)
- Fixed dispensers not being able to use IE buckets (BluSunrize)
- Fixed missing backwards faces for the windmill (BluSunrize)
- Fixed output amounts for structural arm recipes (BluSunrize)
- Fixed sawmill dropping the wrong item when broken (BluSunrize)
- Fixed firework and wolfpack cartridges crashing in turrets (BluSunrize)
- Fixed dispensers not being able to use IE buckets (BluSunrize)
- Fixed various issues with gunpowder barrels (BluSunrize)
- Fixed additional crashes with connectors loading (Malte)
- Fixed secondary output on the sawmill (Malte)
- Fixed derpy splitting of energy on the dieselgen (BluSunrize)
- Fixed multiple crashes due to Forge making breaking changes to a running version (Malte)
- Fixed assembler not handling certain Craftweaker recipes properly (Malte)
- Fixed cacti and sugarcane not growing on red sand in the cloche (MalkContent)
- Fixed red sandstone crushing into normal sand (MalkContent)
- Fixed a crash in turret GUIs (TwistedGate)
- Translations Added/Updated: pt_br (felipeboff), zh_cn (AethLi)

##### Version 1.16.3-4.1.0-124
- Fixed crash with gunpowder barrels (BluSunrize)
- Also includes the changes from 1.16.1-4.1.0-123

##### Version 1.16.1-4.1.0-123
- Fixed further issues with wires loading (Malte)
- Fixed issues with ItemStack NBTs, caused problems with Mekanism (Malte)
- Also includes the changes from 1.15.2-4.1.0-122

##### Version 1.15.2-4.1.0-pre
- Added the sawmill! It strips logs and cuts them into planks! (BluSunrize)
- Added blue ice to be used by the Thermoelectric Generator, nerfed packed ice in response (BluSunrize)
- Experimental recipe changes: (BluSunrize)
    - Doubled the burntime of Diesel in the generator
    - Allowed creosote to be very inefficient generator fuel
    - Increase sulfur output from coal, lapis and quartz
    - Increase gunpowder output from sulfur & saltpeter recipe
    - Reduced ethanol output from melon slices
- Allowed multiblock structures to be changed by data packs (Malte)
- Changed the renders for waterwheels, windmills and bucket wheel to use VBOs (Malte)
- Fixed GUI model for the alloy smelter (BluSunrize)
- Fixed crash with mineral veins being saved (BluSunrize)
- Fixed infinite crafting of firework rockets in the assembler (Malte)
- Fixed lighting in the assembler GUI (Malte)
- Fixed connectors being pushed by pistons if quark is installed (Malte)
- Fixed connectors accepting too much energy sometimes (Malte)
- Fixed recipes for steel, electrum and constantan dust (Malte)

##### Version 1.16.3-4.0.1-121
- Initial release for 1.16.3

##### Version 1.16.1-4.0.1-120
- Fixed crashes with wires under certain conditions (Malte)
- Also includes the changes from 1.15.2-4.0.1-119

##### Version 1.15.2-4.0.1-119
- Added Enderpeals as valid Railgun projectiles! (BluSunrize)
- Changed recipes for mixer and arc furnace to be synced properly (Malte)
- Fixed model issues with the metal press and refinery (Malte)
- Fixed the metal press piston vanishing at certain angles (Malte)
- Fixed wires rendering too many segments near chunk boundaries (Malte)
- Fixed broken lighting on split models (Malte)
- Fixed each block of the garden cloche rotating on its own (Malte)
- Fixed soil blocks not rendering in the garden cloche (Malte)
- Fixed the hemp plant blocks appearing in the creative menu (Malte)
- Fixed blueprints not showing up in the creative menu and JEI (Malte)
- Fixed turrets being rotated incorrectly (Malte)
- Fixed mixers voiding one tick worth of output (Malte)
- Fixed screwdriver not being able to sneak+rightclick blocks (BluSunrize)
- Fixed heavy shield not being repairable with a steel ingot (BluSunrize)
- Fixed rendering issues with CTM (Malte)
- Fixed rendering issues with IE's shaders & model caching (Malte)
- Fixed slabs dropping only one item (Malte)
- Fixed Arc Furnace not syncing its electrode render (Malte)

##### Version 1.16.1-4.0.0-118
- Re-added custom banner patterns. Craft the blueprint, make patterns, then use them on the loom! (BluSunrize)
- Added a warning when switching to "fabulous" graphics while stencil is enabled (Malte)
- Fixed log spam caused by wire sync issues (Malte)
- Fixed manual page for shaders (BluSunrize)
- Also includes the changes from 1.15.2-4.0.0-117

##### Version 1.15.2-4.0.0-117
- Changed rendering of multiblocks to split models into blocks;
  - This should address weird issues with shadows and disappearing models (Malte)
- Fixed broken rendering of components on the Automated Workbench (BluSunrize)
- Fixed rendering of mirrored multiblocks (Malte)
- Fixed rendering of conveyors in the manual; this caused confusion when building multiblocks (Malte)
- Also includes the changes from 1.14.4-4.0.0-116

##### Version 1.14.4-4.0.0-116
- Added dyed sheetmetal & slabs, textured by Rorax! (BluSunrize)
- Changed various recipes: (BluSunrize)
  - Engineering blocks got cheaper
  - MV architecture now uses electrum
  - Gunpowder has more output
  - Press molds need less steel 
- Changed the Railgun (BluSunrize)
  - New model!
  - New types of ammo!
  - More energy storage!
  - New advancements!
- Changed the pump to be configured for input on the bottom by default (Malte)
- Changed nugget to ingot/ingot to block recipes to only trigger when an IE ingot is used in the middle slot (Malte)
  - If more mods adopt this convention it will mean that crafting a storage block from a specific mod will be possible
- Changed ItemHandler for the Silo to hopefully cooperate better with Refined Storage (BluSunrize)
- Fixed continued depletion of already depleted mineral veins (BluSunrize)
- Fixed RTFM advancement not being announced (BluSunrize)
- Fixed crashes with Building Gadgets & JEI (BluSunrize)
- Fixed incorrect waterlogging of dummy blocks for pump, posts and others (BluSunrize)
- Fixed mineral survey tools not breaking when exceeding their damage (BluSunrize)
- Fixed recipe list positions in squeezer & fermenter GUI (BluSunrize)
- Fixed some threading issues with recipes (BluSunrize)
- Fixed missing details for the IKELOS shader (BluSunrize)
- Fixed more concurrency exceptions with mineral veins (BluSunrize)
- Fixed alloy grit recipes outputting too little (BluSunrize)

##### Version 1.16.1-3.2.0-115
- Fixed some derpy rendering in the manual (BluSunrize)
- Also includes the changes from 1.15.2-3.2.0-114

##### Version 1.15.2-3.2.0-114
- Fixed mirrored machines rendering incorrectly (Malte)
- Fixed bucket wheels in mirrored excavators rendering incorrectly (Malte)
- Fixed crash when holding a drill with a damaged head (Malte)
- Also includes the changes from 1.14.4-3.2.0-113

##### Version 1.14.4-3.2.0-113
- Added waterlogging to various IE blocks (BluSunrize)
- Added processing compat for Fluorite, Cobalt and Ardite (BluSunrize)
- Re-Added recipe for patrons & contributers to cycle through revolver skins (BluSunrize)
- Fixed various issues with the manual (Malte)
- Fixed CMEs while loading dynamic models (Malte)
- Fixed warning about missing command argument serializer (Malte)
- Fixed multiblocks not forming when using the wrong steel blocks (Malte)
- Fixed priority system for recipe outputs not working (Malte)
- Fixed recipes sometimes not getting added to JEI on servers (Malte)
- Fixed fences obstructing connections in counterintuitive ways (Malte)
- Fixed crashes with the manual (Malte)
- Fixed assembler logic once again to properly handle concrete recipes (BluSunrize)
- Fixed flare cartridge colours to match the dye they were crafted with (BluSunrize)
- Fixed assembler connecting to pipes in wrong places (BluSunrize)
- Translations Added/Updated: zh_zn (masakitenchi)

##### Version 1.16.1-3.1-112
- Require Forge version 32.0.99 or later
- Moved to new version numbering system
- Fixed a crash with railgun & chemthrower projectiles (BluSunrize)
- Fixed manual rendering for blueprint recipes (BluSunrize)
- Fixed scaffolding causing a suffocation overlay (BluSunrize)
- Fixed crashes with the drill & sawblade overlay (BluSunrize)
- Fixed crashes with wires (Malte)
- Fixed crashes with explosions (Malte)
- Also includes the changes from 1.15.2-3.1-111

##### Version 1.15.2-3.1-111
- Moved to new version numbering system
- Fixed broken HUD of the Heavy Plated Shield upgrades (BluSunrize)
- Fixed crash when bottling potions (Malte)
- Fixed rotation for structural connectors (BluSunrize)
- Also includes the changes from 1.14.4-3.1-110

##### Version 1.14.4-3.1-110
- Moved to new version numbering system
- Automatically load all manual entries listed in a central JSON (Malte)
  - This allows resource packs to add entries to the manual
- Added "Hero of the Village" rewards to villagers (BluSunrize)
- Fixed various errors with wires (Malte)
- Fixed crash with fluid pipes (BluSunrize)
- Fixed bugs with cached redstone values (Malte)
- Fixed crash related to shader banners (TeamSpen210)
- Fixed Hemp seeds not being plantable by various autoplanters (BluSunrize)
- Fixed Tesla Coils crashing when trying to zap blocks with no bounding boxes (BluSunrize)
- Fixed rare concurrency exception with mineral veins (BluSunrize)
- Fixed snow buildup stopping windmills (BluSunrize)
- Fixed one of the rendering issues with transformers (BluSunrize)

##### Version 0.16-109 - BUILT
- Requires Forge version 32.0.67 or later (BluSunrize)
- Fixed formatting not persisting across linebreaks in the manual (Malte)
- Fixed tooltips crashing in the manual (Malte)
- Fixed recipes not loading on dedicated servers (BluSunrize)
- Fixed fluid tank backgrounds being wrongly offset in JEI (BluSunrize)
- Fixed revolver causing a crash with its attribute map (BluSunrize)
- Also includes the changes from 0.14-107

##### Version 0.15-108 - BUILT
- Also includes the changes from 0.14-107

##### Version 0.14-107 - BUILT
- Changed conveyor behavior; any conveyor can only have 3 stacks of items on it (Malte)
- Changed keybindings to their have their own category "Immersive Engineering" (BluSunrize)
- Fixed dropping conveyors requiring a high redstone signal to work (BluSunrize)
- Fixed calculation of expected yield on coresamples (BluSunrize)
- Fixed potential crash on loading mineral veins (BluSunrize)
- Fixed diesel generator forgetting about its redstone signal inversion (BluSunrize)
- Fixed shader dupe bug with banners (BluSunrize)

##### Version 0.16-106 - BUILT
- Initial port to 1.16
- Added mineral vein, crusher & arc furnace integration for the new Nether materials
- Also includes all changes from 0.15-105

##### Version 0.15-105 - BUILT
- Fixed invisible connectors with Optifine (Malte)
- Fixed metal ladders rendering as solid (Malte)
- Fixed shield rendering in 3rd person (BluSunrize)
- Fixed incorrect sounds on certain blocks (BluSunrize)
- Also includes the changes from 0.14-104

##### Version 0.14-104 - BUILT
- Added the new Mineral System! (BluSunrize)
    - Minerals are no longer chunk based
    - Survey Tools help discover veins
    - Veins can overlap and produce multiple results!
- Added JSON "recipes" for Cloche Fertilizer (BluSunrize)
- Added IE's metal items to various generic tags (Malte)
- Added holding animation for the buzzsaw (BluSunrize)
- Added the ability for the chemthrower to hydrate vanilla concrete powder (BluSunrize)
- Added the ability for fluidpipes to extract from tanks (experimental, might break stuff) (BluSunrize)
- Added mouseover for maps showing mineral locations (only works with NEW maps!) (BluSunrize)
- Added comparator output for the crusher (BluSunrize)
- Cleaned up some wire network code (Malte)
- Changed hammer to allow rotating Redstone Repeaters (experimental, might break stuff) (BluSunrize)
- Changed buzzsaw to take less damage when breaking leaves (BluSunrize)
- Changed various blocks to have their redstone interaction configured by the Screwdriver instead of the Hammer (BluSunrize)
- Changed herbicide to kill tall grass and crops (BluSunrize)
- Fixed feedthroughs not dropping the middle block when it's broken (Malte)
- Fixed IE allowing clients to join servers with mismatched IE versions (Malte)
- Fixed diesel generators still not allowing levers to be placed in all cases (Malte)
- Fixed toolbox facing the wrong way when placed (Malte)
- Fixed transforms for non-covered metal ladders (Malte)
- Fixed translation of the middle block of feedthroughs (Malte)
- Fixed coresamples not marking maps (Malte)
- Fixed "pick block" not working correctly on hemp (Malte)
- Fixed mineral commands not working with specified coordinates (Malte)
- Fixed item duplication when repairing hammers or wirecutters (Malte)
- Fixed rotating and inverting the breaker switch with a hammer (Malte)
- Fixed wires not updating correctly when rotating a redstone breaker (Malte)
- Fixed desyncing in the workbench (Malte)
- Fixed crash related to dyeing earmuffs (Malte)
- Fixed dupebug related to wire recycling (BluSunrize)
- Fixed coke oven desync when not processing (BluSunrize)
- Fixed shears not counting as tools for the toolbox (BluSunrize)
- Fixed rendering and boundingboxes on vertical conveyors (BluSunrize)
- Fixed IE hud elements overlapping with subtitles, but this time better (BluSunrize)
- Fixed height calculations for tables in the manual (Malte)
- Fixed strip curtains not storing their strong/weak signal setting (Malte)
- Fixed crash when transferring energy through very-high-loss connections (Malte)
- Fixed issues with hemp seeds not dropping (Malte)
- Fixed crash with chemthrower (Malte)
- Fixed crash with Mekanism (Malte)
- Implemented a few minor performance improvements (Malte)
- Translations Added/Updated: ru_ru (Sergo 467), pt_pt (Codified-Vexing)

##### Version 0.15-103 - BUILT
- Fixed wires no longer transferring energy after release 101 (Malte)

##### Version 0.14-102 - BUILT
- Fixed wires no longer transferring energy after release 100 (Malte)

##### Version 0.15-101 - BUILT
 - Fixed hemp rendering as solid (Malte)
 - Fixed Optifine shader packs crashing with the IEOBJ item renderer (Vaelzan)
 - Fixed crashes when using the buzzsaw or drill (Malte)
 - Fixed missing connectors on feedthrough models (Malte)
 - Fixed missing tooltips in various GUIs (Malte)
 - Also includes the changes from 0.14-100

##### Version 0.14-100 - BUILT
- Added different sounds for blocks of different materials (BluSunrize)
- Added "forge:ores" tag to all IE ores (Malte)
- Re-Added packing and unpacking recipes for the metal press (BluSunrize)
- Added cloche recipe to grow bamboo (BluSunrize)
- Added composting for hempseeds (BluSunrize)
- Added missing recipe for the "Common Projectiles" blueprint (BluSunrize)
- Re-Added homing cartridges, these use eyes of ender now (BluSunrize)
- Re-Added a recipe for wolfpack cartridges; also buffed their damage (requires a fresh config file) (BluSunrize)
- Added a mixer recipe for concrete, using slag (BluSunrize)
- Changed textures on kinetic dynamo to better represent its ability to output on all sides (BluSunrize)
- Changed recipe for blast bricks to use magma blocks instead of blaze powder, making it available on peaceful (BluSunrize)
- Changed redstone and probe connectors to be configured with the Engineer's Screwdriver instead of Hammer (BluSunrize)
- Changed plated shield to store more energy and consume less of it for its upgrades (BluSunrize)
- Fixed bounding boxes for vertical covered conveyors (Malte)
- Fixed metal press not forming when mirrored (Malte)
- Fixed extracting conveyors not saving their extract direction (BluSunrize)
- Fixed manual freezing when dealing with long tokens (Malte)
- Fixed bounding boxes for some multiblocks (Malte)
- Fixed visual connection of fluid pipes on refinery (BluSunrize)
- Fixed silver bullets not doing extra damage to undead (BluSunrize)
- Fixed missing recipes for the "Crafting Components" blueprint in the manual (BluSunrize)
- Fixed crashes with coresamples created before IE 0.14-96 (Malte)
- Fixed diesel generator not allowing levers to be attached (Malte)
- Fixed turrets not firing at entities (BluSunrize)
- Fixed a crash when clicking the list of names in the turret GUI (BluSunrize)
- Fixed vertical and splitting conveyors not inserting correctly (BluSunrize)
- Fixed concrete recipe consuming the water bucket (BluSunrize)
- Fixed shader bags causing registry mismatches (Malte)
- Fixed assembler ignoring redstone signals (Malte)
- Fixed mining levels for ores and storage blocks (Malte)
- Fixed fluids not applying effects to entities in them (Malte)
- Fixed IE hud elements overlapping with subtitles (BluSunrize)
- Improved performance of wire-damage code (Malte)

##### Version 0.15-99 - BUILT
- Initial alpha release for Minecraft 1.15.2
- Highlight the trigger block for multiblocks in the manual

##### Version 0.14-97 - BUILT
- Added fuel gauge for the buzzsaw (BluSunrize)
- Added a covered version of the splitting conveyor (BluSunrize)
- Added a steel hoe (BluSunrize)
- Changed buzzsaw blade quiver to keep the sawblades when removed (BluSunrize)
- Fixed a crash triggered by other mod's automatic recipe unlocks (BluSunrize)
- Fixed a crash with the wirecoil tooltips (Malte)
- Fixed a crash when rendering coresamples with Optifine installed (Malte)
- Fixed buzzsaw upgrade achievement not triggering (BluSunrize)
- Fixed buzzsaw and drill not taking damage or requiring fuel when used as weapons (BluSunrize)
- Fixed wrong transformations for steel tools (Malte)
- Fixed fluid gauge for the drill going over (Malte)
- Fixed broken recipe syncing (Malte)

##### Version 0.14-96 - BUILT
- All IE recipes are now JSON based! (BluSunrize)
    - This includes the excavator's mineral veins. ALL EXISTING VEINS WILL BE BROKEN. The last release was an Alpha, so
      this has to be expected
    - Mineral veins have also been changed and rebalanced
- Overhauled the turntable, to allow side-based redstone input to produce different rotations (MalkContent)
- Added furnace fuel values for coke coal and coke coal blocks (Malte)
- Re-Added automatic changelog generation (Malte)
- Re-Added Graphite Electrode blueprints (BluSunrize)
- Changed and improved Multiblock bounding boxes (Malte)
- Changed dropping conveyors to not drop downwards when given redstone signal (MalkContent)
- Changed hammers and wirecutters to allow repairing (BluSunrize)
- Changed high-explosive cartridge to not destroy blocks (BluSunrize)
- Fixed IE ores not generating in non-vanilla biomes (Malte)
- Fixed IE ores generating in blacklisted dimensions (Malte)
- Fixed refinery not accepting input fluids when only one tank is filled  (Malte)
- Fixed crashes when using railguns or revolvers with shaders on a dedicated server (Malte)
- Fixed some texture and model loading errors (Malte)
- Fixed scaffolding not acting as a ladder (Malte)
- Fixed fontrendering issues with Optifine (Malte)
- Fixed refinery accepting fluid pipes from all sides (BluSunrize)
- Fixed Voltmeter being broken (Malte)
- Fixed renders for Bottling Machine and Mixer (Malte)
- Fixed collision on diagonal covered conveyors (BluSunrize)
- Fixed recipes for sheetmetal not accepting other mods' plates (BluSunrize)

##### Version 0.14-95 - BUILT
- Added the Buzzsaw! It cuts Trees! (BluSunrize)
- Added full support for the new Villages! (BluSunrize)
    - Different houses based on biome
    - 5 different IE Villagers and their workstations:
        - Structural Engineer, using the new Engineer's Crafting Table
        - Machinist, using an Anvil
        - Electrician, using a Current Transformer
        - Gunsmith, using the Engineer's Workbench
        - Outfitter, using IE's Shader Banner
    - All IE villagers vary their outfit based on profession and biome 
- Re-Added Revolver Perks (BluSunrize)
- Re-Added the Fluid Outlet (Malte)
- Added the Firework Cartridge for the Revolver (BluSunrize)
- Re-Added IE's Advancements (BluSunrize)
- Re-Added the Maintenance Kit (BluSunrize)
- Added the Engineer's Screwdriver (BluSunrize)
- Added herbicide fluid. It kills leaves and grass (BluSunrize)
- Re-Added Chutes (BluSunrize)
- Added the Redstone Controlled conveyor (BluSunrize)
    - Normal conveyors now ignore redstone
    - This special one can be disabled with a signal
- Added the Item Batcher! It collects items until a specified amount is reached, then sends them on (BluSunrize)
- Added the Redstone Interface Connector! It allows blocks (like the Item Batcher) to send bundled redstone signals (BluSunrize)
- Re-Added Ore Retrogeneration (phit / Malte)
- Added "Igneous Rock" mineral vein which contains Granite, Diorite, Andesite and Obsidian (BluSunrize)
- Changed Ear Protectors and Capacitor Backpack to no longer provide armor (BluSunrize)
- Changed Dieselgen to emit more appropriate smoke particles (TwistedGate)
- Changed Quarzite vein to only show up in the Nether (BluSunrize)
- Enabled text overlay (previously colourblind mode) by default (BluSunrize)
- Enabled metal blocks to be used as Beacon bases (BluSunrize)
- Expanded available methods for addons (Malte)
- Fixed countless bits of rendering (Malte & BluSunrize)
- Fixed a few recipes (BluSunrize)
- Fixed rendering for revolver cartridges (BluSunrize)
- Fixed bug in Shaderbag distribution (BluSunrize)
- Fixed faulty ore gen (Malte)
- Fixed sync issues (Malte)
- Fixed worldgen lag (Malte)
- Fixed recipes for Crusher (Malte)
- Fixed Probe Connectors (Malte)
- Fixed Windmills's obstruction code (Malte)
- Fixed collision on slabs (Malte)
- Fixed Conveyors crashing on servers (Malte)
- Fixed connections on Current Transformers (Malte)
- Fixed missing particles (Malte)
- Fixed furnace heater being oriented wrong (Malte)
- Fixed crafting of treated wood with jerrycans (Malte)
- Fixed Manual entries for Shaders, and other entries (BluSunrize)
- Fixed wires not being craftable in metal press (Malte)
- Fixed crashes in the Manual (Malte)
- Fixed wrong sound when breaking hemp (gyroplast)
- Fixed glitches with the Skyhook (Malte)
- Fixed missing recipes for String and Torches (gyroplast)
- Fixed drops from metal press breaking (Malte)
- Fixed recipes not being added properly (Malte)
- Fixed crash with razorwire (Malte)
- Fixed distance limit on Dieselgen Sounds (BluSunrize)
- Fixed conveyors crashing when dyed (BluSunrize)
- Fixed in- & output on the assembler, and bugged recipe handling (BluSunrize)
- Fixed various issues with the wire network (Malte)
- Fixed an issue where some blocks allowed too many wires to be attached (Malte)
- Fixed workbench, cloche and turret not dropping their inventory when their dummy blocks are broken (BluSunrize)

##### Version 0.14-94 - BUILT
- Added Minecarts!
    - Wooden crates, reinforced crates, wooden barrels and metal barrels can be put in carts
    - IE's conveyors can load and unload (extracting conveyor) Minecarts (including vanilla ones!)
- Re-Added Steel Armor
- Added vanilla recycling (1 nugget!) for steel tools, steel armor and the faraday suit
- Change minimum Forge version to 28.2.3
- Fixed crash due to Minecart render
- Fixed various rendering glitches
- Fixed buckets not working
- Fixed cloche and other dummy-constructed blocks replacing existing blocks
- Fixed links in the manual's search function
- Fixed extracting conveyors not being able to rotate their input points
- Fixed missing vanilla materials in excavator minerals

##### Version 0.14-93 - BUILT
- Initial alpha release for Minecraft 1.14.4

##### Version 0.12-90 - BUILT
- Items on conveyors now despawn after the usual time when stuck against a block (Malte)
- Fixed wires sometimes attaching to the wrong parts of transformers (Malte)
- Fixed wires connecting to the transformer when clicking on the lower blocks (Malte)
- Fixed some ghostloading issues (Malte, with help from Barteks2x)
- Fixed the assembler not crafting recipes added using CraftTweaker (Malte)

##### Version 0.12-89 - BUILT
- Added a covered dropping conveyor (BluSunrize)
- Added recipes to break down Clay blocks (crusher) and Melon blocks (unpacking mold) (BluSunrize)
- Added Cinnabar vein, containing Redstone, Cinnabar, Ruby and Sulfur (BluSunrize)
- Changed Config to feature range values (Malte)
- Changed Compat Modules to register recipes on the registry event (BluSunrize)
- Fixed crash when placing blocks that shouldn't be used as a grid on lightning rods (Malte)
- Fixed Balloon shaders (Malte)
- Fixed Steel Axe having overly high damage (BluSunrize)
- Fixed potential crashes related to the new blueprint renders (BluSunrize)
- Fixed coresample markers disappearing off maps after restarting (BluSunrize)
- Fixed snow collecting on partial concrete blocks (Voidi)
- Fixed Extracting Conveyors not resetting their cooldowns when toggled by redstone (MalkContent)
- Translations Added/Updated: pt_br (chico3434), es_es(rogama25), zh_cn (mcBegins2Snow)

##### Version 0.12-88 - BUILT
- Changed Multiblocks to accept redstone-ignoring conveyors as well as default ones. They will revert to normal ones when the multiblock is broken. (BluSunrize)
- Fixed crashes when opening a toolbox or a revolver GUI (Malte)
- Fixed side solidity on conveyors, prevents snow on places it shouldn't be (BluSunrize)

##### Version 0.12-87 - BUILT
- Added a "Distribute Inputs" button to the ArcFurnace GUI. It keeps splitting the biggest stacks in the input if there is space available (BluSunrize)
- Added the Maintenance Kit, an on-the-go option to modify tools, like changing drillheads or configuring the Ear Defenders (BluSunrize)
- Changed the skyhook in multiple ways (Malte)
  - The speed now depends on gravity
  - The skyhook will not drag players into blocks any more
  - Many other smaller changes
- Changed connectors to accept multiple "bursts" of energy if they (in total) don't exceed the max IO rate (Malte)
- Changed internal logic of the Assembler to be a lot more straightforward, allowing bucket-like items for fluid ingredients (BluSunrize)
- Changed the Engineer's Workbench around (BluSunrize)
    - Lots of internal fixes to improve reliability and performance, as well as code style
    - Fancy new render features, showing the blueprints on the table and the ingredients being used
- Changed Fluid Pipes to no longer automatically connect to pipes that have been dyed a different colour (BluSunrize)
- Changed recipe for Revolver Barrel to be (hopefully) less conflicting (BluSunrize)
- Changed Excavator to work over void (BluSunrize)
- Removed clickable JEI areas from the Assembler because they were blocking the output slots and were unnecessary anyway (BluSunrize)
- Fixed wires not showing particles when burning (Malte)
- Fixed recipes for structural arms conflicting with stairs (Malte)
- Fixed structural arms being invisible due to a conflict with CTM (Malte)
- Fixed minor issues where certain enemy network setups made powered lanterns flicker (Malte)
- Fixed Dragon's Breath shader using hte wrong description in the manual (BluSunrize)
- Fixed connectors not rendering as part of manual multiblock previews (Malte)
- Fixed Ingredient Recipe not transposing the index correctly, causing loss of items (BluSunrize)
- Fixed Item Router voiding items when used to extract (BluSunrize)
- Fixed an annoying consolespam with the Cartographer trades (BluSunrize)
- Fixed various GUIs still using "RF" instead of "IF" (BluSunrize)
- Fixed Lightningrod resulting in a crash on very specific setups (BluSunrize)
- Fixed dupebug with ArcFurnace recycling (BluSunrize)
- Fixed the Garden Cloche accepting Fluids it can't work with (BluSunrize)
- Fixed connectors duping small amounts of energy (Malte)
- Fixed lighting on capacitors (Malte)
- Fixed excessive render latency in JEI (Malte)
- Fixed OpenComputers integration (sargunv)
- Fixed Covered Extracting Conveyor not applying magnet protection (BluSunrize)
- Translations Added/Updated: ja_jp (karakufire), es_es(Dorzar & rogama25), zh_cn (mcBegins2Snow)

##### Version 0.12-86 - BUILT
- Added support for JEI ghost ingredients to the assembler and routers (Malte)
- Re-Added razor wires hurting players trying to break them without wirecutters (Malte)
- Re-Added coloured pipes (Malte)
- Re-Added structural arms (Malte)
- Added clickable areas for JEI to the IE machine GUIs (TeamSpen210)
- Added the ability to apply Shaders to banners! (BluSunrize)
- Added a Silt vein for the Excavator, contains clay, sand and gravel (BluSunrize)
- Removed the additional http request for the changelog in the manual (Malte)
- Changed Mixer to allow the use of other mods' sheetmetal in its construction (tgstyle)
- Changed the way wire & connector models work internally (Malte)
- Feedthrough insulators can now be formed while wires are attached to the connectors (Malte)
- Fixed arc recycling calculations failing in rare cases (Malte)
- Fixed lag with certain setups of routers (Malte)
- Fixed conveyors sometimes showing the wrong model (Malte)
- Fixed crashes with recent Forge versions (Malte)
- Fixed the arc furnace not recycling damaged tools (BluSunrize)
- Fixed a crash with recent Forge versions (Malte)
- Fixed Wooden Posts connecting to Vines (BluSunrize)
- Translations Added/Updated: ru_ru (gri3229), ko_kr (mindy15963)

##### Version 0.12-85 - BUILT
- Re-Added Thaumcraft compat (BluSunrize)
    - Added hammering of plates for Brass, Thaumium and Void Metal 
    - Golems can farm hemp
    - Purifying Fluid and Liquid Death work in the Chemthrower
    - Made the External Heater work with the Essentia Smelter
- Added a tooltip-display to Speedloaders and Revolvers to display their currently loaded ammo (BluSunrize)
- Re-Added the "Super Secret BluPrintz" easteregg (BluSunrize)
- Added the ability to use the Capacitor Backpack as a Bauble (BluSunrize) 
- Fixed up the skyhook, including one crash (Malte)
- Fixed some rare crashes when closing a world (Malte)
- Fixed redstone connectors not closing doors using weak signals (Malte)
- Fixed the mixer crashing if the recipe is trying to process too much fluid (Malte)
- Fixed Machinist selling iron and steel drills in the wrong order (BluSunrize)
- Fixed balloon colouring on dedicated servers (Malte)
- Fixed a dupe bug (Malte)
- Fixed a crash involving metal ladders (Malte)
- Fixed Crafttweaker Integration to allow removing Potion mixing recipes  (BluSunrize)
- Fixed an edgecase crash with Cartographer Maps (BluSunrize)
- Fixed ArcFurnace requiring additional inputs to start a recipe (BluSunrize)
- Fixed upgrades vanishing in the Workbench (Malte)
- Fixed Lightningrod not playing nice with other mods' energy transport (BluSunrize)

##### Version 0.12-84 - BUILT
- Changed handling of Potions in the mixer: (BluSunrize)
    - Now supports MixPredicates as well as custom brewing recipes
    - Shows properly in JEI, based on Potion output, rather than input
    - Bottling recipes are consistent, anything you can mix, you can bottle (ideally)
    - Added tooltips to Potion fluids to show which mod added the PotionType
    - Added compat for KnightMiner's Inspirations, allowing their Splash and Lingering bottles to be filled
- Added a separate JEI Handler for Arc-Recycling (BluSunrize)
- Added a particle trail to Minecarts that use fancy shaders (e.g. IKELOS) (BluSunrize)
- Added the ability to pop Balloons by hitting them with a Projectile (BluSunrize)
- Added Shader Particles to the Railgun and Drill (BluSunrize)
- Added a "Noise" component to the revolver, which will make it attract nearby mobs when fired (BluSunrize)
    - Adding a bit of a stealth/survival aspect
    - Probably not super impactful in normal singleplayer, but could be fun for pack makers
    - This will play into a larger customization system for the Revolver, to be implemented in the future
- Re-Added some shader textures that got lost in the 1.11 update (BluSunrize)
- Changed Arc-Recycling to run in init and be overall cleaner (Malte)
- Changed the recipe for Vacuum Tubes to use Nickel Plates (BluSunrize)
- Fixed compatability with ActuallyAdditions and AttainedDrops (Shadows-of-Fire)
- Fixed out-of-thread call to JEI functions causing crashes (BluSunrize)
- Fixed depth-rendering issues in JEI handlers (BluSunrize)
- Fixed shiftclicking on IE's containers (BluSunrize)
- Translations Added/Updated: zh_cn (DYColdWind)

##### Version 0.12-83 - BUILT
- Added the Extracting Conveyor. It pulls from inventories like a Hopper (BluSunrize)
- Added even fancier Shaders with pulsing colours and dynamic rendering. Quite open for addon devs, too! (BluSunrize)
    - Added the IKELOS shader to make use of this
- Added maps to the vanilla Catographer which lead to Mineral Veins (BluSunrize)
- Added a config option to disable all use of the stencil buffer for old Intel GPUs (Malte)
- Added normal tools (Axe, Pick, Shovel, Sword) made from Steel and Treated Wood. Good durability & efficiency, iron harvest level (BluSunrize)
- Added a new rarity, "Masterwork". Currently only for Shaders (grabbags gained from secret advancements) but will have more use in the future! (BluSunrize)
- Changed ItemRouters to allow pulling as well, allowing for filtered extraction (BluSunrize)
- Changed BlastFurnace to not start using a new piece of coke if there is nothing left to smelt (Malte)
- Changed the Excavator's Mineral Veins to consider the chunks in a radius of two around them, to avoid duplicate veins (BluSunrize)
- Changed Chemical Thrower to factor in player-momentum to projectiles (TeamSpen210)
- Fixed wires sometimes leaving behind the damage sources when broken (Malte)
- Fixed the TConstruct compat module failing (Malte)
- Fixed Hammers and Wirecutters to be properly repairable (Malte)
- Fixed Wirecutters to be enchantable (Malte)
- Fixed texture and model errors being spammed (Malte)
- Fixed dropping conveyor spawning countless fake items in certain setups (Malte)
- Fixed conveyors causing infinite loops in rare cases (Malte)
- Fixed a crash when breaking a connector after dying (Malte)
- Fixed wires connected to transformers breaking when they shouldn't (Malte)
- Fixed horizotal wires on the breaker switch (Malte)
- Fixed crashes with the Core Sample Drill in some dimensions (Malte)
- Fixed pipes accessing the world from wrong threads (Malte)
- Fixed a dupe bug (Malte)
- Fixed the skyhook for vertical wires (Malte)
- Fixed placing blocks against pipes not working as expected (Malte)
- Fixed the garden cloche accepting any item into the fertilizer slot (Malte)
- Fixed the workbench creating excess items (codewarrior0)
- Fixed obstructed connections not rendering properly in LAN worlds (JamiesWhiteShirt)
- Fixed Razor Wire not having collision on upper wall section (TeamSpen210)
- Fixed Arc Furnace recycling being broken (BluSunrize)
- Fixed Squeezer and Fermenter not updating the GUI when filling buckets (BluSunrize)
- Improved log output when a compat module fails (Malte)
- Translations Added/Updated: ja_jp (iceink001), ru_ru (kellixon ), zh_tw (vongola12324)

##### Version 0.12-82 - BUILT
- Added compat for farming Hemp to ActuallyAddition's farmer (BluSunrize)
- Fixed Wirecutter getting consumed when cutting plates (BluSunrize)
- Fixed Alloy Kiln consuming fuelsource container items (BluSunrize)
- Fixed uncovered FluidPipes allowing mobspawns (BluSunrize)
- Fixed spawn interdictors (Electric Lanterns, etc) not being removed on chunk unload (codewarrior0)
- Fixed Metal Press animations and sounds with customized recipe times (codewarrior0)
- Fixed some potential crashes in networking and reduced packet size (JamiesWhiteShirt)
- Fixed 'removeRecipesForInput' in Crusher Craftweaker compat not working (BluSunrize)
- Reduced the amount of ItemStacks being copied in rendering and recipe checking (Malte)
- Translations Added/Updated: ja_jp (iceink001), zh_CN (DYColdWind)

##### Version 0.12-81 - BUILT
- Added the functionality for the Faraday Suit to protect against live wiring (Malte)
- Added the ability to use Pick Block to take a connector's wire by sneaking (BluSunrize)
- Added Magma blocks to the list of heatsources for the Thermoelectric generator (BluSunrize)
- Added Efficiency, Unbreaking and Mending enchantments to the Engineer's Hammer and Wirecutter (BluSunrize)
    - Made those tools take damage when used to break blocks
- Added compatibility for Forge's update checker (Malte)
- Added support for Forge's memory Maven repo, this makes deleting old coremod versions unnecessary in Forge 2656+ (latest) (Malte)
- Changed fences to allow torches on top and prevent mobspawns (Malte)
- Changed animated item models to use Item-TESRs rather than rebaking (Malte)
    - Made the Fluorescent tube to be really fancy again! (Malte)
- Changed renderdistance config to work as described (Malte)
- Changed the images of wiring in the manual to use up-to-date pictures and feature connectors and relays (BluSunrize)
- Shift-clicking is no longer needed to connect wires to breaker switches etc (Malte)
- Failed wire connection attempts no longer spam the chat (Malte)
- Changed Thermoelectric generator to cache its output values, improving performance (BluSunrize)
- Fixed wires being invisible when logging into a server for the first time (Malte)
- Fixed localization for treated wood banners (Malte)
- Fixed the mineral command not workign with Vein names that contain a space (Malte)
- Fixed shield having a really trippy enchantment glow (Malte)
- Fixed conveyor recipe (Malte)
- Fixed Skyhook crashing servers (Malte)
- Fixed lag with redstone connectors (Malte)
- Fixed blocks somtimes taking longer to break with the hammer (Malte)
- Fixed a divide-by-zero issue in the ExcavatorHandler for blacklisted dimensions (BluSunrize)

##### Version 0.12-80 - BUILT
- Fixed multiblock rendering in the manual with Optifine and texture packs (Malte)
- Fixed pre-IE-79 multiblocks not working on IE 79 (Malte)
- Fixed FluidContainer slots not accepting empty containers (BluSunrize)
- Fixed Villager trades for Shader bags being inverted (BluSunrize)
- Fixed Multiblocks dropping themselves as illegal items (BluSunrize)
- Fixed ugly high-rez textures on Engineering blocks when CTM isn't loaded (BluSunrize)
- Translations Added/Updated: ja_jp (iceink001), zh_CN (DYColdWind)

##### Version 0.12-79 - BUILT
- Added the ability for Wirecutters to cut wires from anywhere (Malte)
- Added Craftweaker support for the Thermolelectric Generator (BluSunrize)
- Added documentation for metal ladders (BluSunrize)
- Added Squeezer recipe for beetroot seeds (pap1723)
- Re-Enabled OpenComputers compat (Malte)
- Changed Feedthrough Connectors to break like normal multiblocks (Malte)
- Changed Feedthrough Connectors to form regardless of connector face clicked (BluSunrize)
- Changed codebase for wire coils to allow easier extension (AntiBlueQuirk)
- Changed Multiblocks to only move FluidContainers to the output slots when they are full (BluSunrize)
- Made the IE config react to changes from the in-game GUI
- Fixed a crash with Optifine and feedthrough connectors (Malte)
- Fixed wire rendering in edgecases (Malte)
- Fixed issues with the Skyhook (Malte)
- Fixed missing textures on conveyors (Malte)
- Fixed dupebug with Jerrycans (Malte)
- Fixed stackoverflow with routers (Malte)
- Fixed Feedthrough Connectors functionality (Malte)
- Fixed Coresamples not dropping when broken (Malte)
- Fixed missing revolver sounds (BluSunrize)
- Fixed broken audio-subtitles (lgthibault)
- Fixed crash with windmills (lgthibault)
- Fixed crash with metal ladders (lgthibault)
- Fixed Craftweaker removal function of MetalPress (BluSunrize)
- Translations Added/Updated: ja_jp (iceink001), zh_CN (DYColdWind)

##### Version 0.12-78 - BUILT
- Added a preliminary connection render to debug obstructions (Malte)
- Added Craftweaker function to remove Crusher recipes by input (BluSurize)
- Fixed crash when breaking middle block of Feedthrough connector (Malte)
- Fixed errors related to floating point accuracy (Malte)
- Fixed Blastfurnace not working at all (Malte)
- Fixed raytracing on vertical connections (Malte)
- Fixed Sampledrill render (Malte)
- Fixed J8U25 message showing on dimension change (Malte)
- Fixed Feedthrough connectors connecting to themselves (Malte)
- Fixed wires dropping in the wrong location (Malte)

##### Version 0.12-77 - BUILT
- Various changes to the wiring system (Malte):
  - Uninsulated energy wires cause damage now
  - Wires don't need clear line-of-sight but a clear path along the wire as it is rendered
  - Placing blocks that obstruct wires will cause the wire to break/drop
  - The skyhook can attach to the middle of connections, rather than just the endpoints
  - Added a feedthrough insulator as a way to get wires through walls
  - Addons using wires will have to adapt to these changes!
- IE now prints a message to chat if Java 8 update 25 is used since it causes unfixable crashes (Malte)
- Changed Drill to have the "Shovel" tool class (primetoxinz)
- Changed Excavator to show Cobblestone instead of missing texture when digging items instead of blocks (BluSunrize)
- Fixed the drill not properly accepting its head (Malte)
- Fixed broken deserialization of boolean properties, this caused some potential issues with Buildcraft (Malte)
- Fixed some TESR blockstate crashes (Malte)
- Fixed IE projectiles (chemthrower, railgun) not working (Malte)
- Fixed wire loss being higher than intended in some cases (Malte)
- Fixed fluid pipes losing fluid and not accepting small amounts (Malte)
- Non-IE wires can no longer be connected to IE connectors (Malte)
- Fixed arc furnace particles rendering when the arc furnace isn't active (Malte)
- Fixed some capability crashes (Malte)
- Fixed mobs trying to jump over IE fences (Malte)
- Re-fixed the blast furnace using slightly more coke coal than it should (Malte)
- Fixed Vertical Conveyor recipes returning too little (BluSunrize)
- Fixed Nullpointer on Redstone Probes (BluSunrize)
- Fixed Output slots not being blocked (BluSunrize)
- Translations Added/Updated: en_ud (The-Fireplace), ja_jp (iceink001)

##### Version 0.12-76 - BUILT
- added various config options for the Belljar, to adjust growthspeeds and fertilizer effects
- re-added partivles and animation for the Arc Furnace (thanks Malte)
- added TOP compat for the Sheetmetal Tank (thanks SirWindfield)
- added Metal Ladders and Scaffold covered versions 
- fixed wires connected to razorwire not rendering (thanks Malte)
- fixed missing comparator output on the Current Transformer (thanks Malte)
- fixed the drill accidentally modifying its NBT on sharing (thanks Malte)
- fixed the Bucket Wheel having rotation issues (thanks Malte)
- fixed crashes related to newer Forge versions (thanks Malte)
- fixed rendering issues with the Floodlight and improved its performance (thanks Malte)
- fixed Carrots and Potoes not working in the Cloche (thanks SirWindfield)
- fixed crash when the Teslacoil destroys a piece of Faraday armor
- fixed Chisel compat
- fixed AA compat, Canola in the squeezer
- fixed TCon compat, mixup of Constantan and Uranium (thanks tgstyle)
- fixed TCon compat, Arc Furnace for alloying
- fixed a crash with the Slippery potion
- fixed derpyness with Mineral Veins (at least for the most part), regarding CraftTweaker
- fixed links to Biodiesel in the manual pointing to a nonexistant page
- fixed a minor texture warning in the console
- Translations Added/Updated: zh_CN (DYColdWind)

##### Version 0.12-75 - BUILT
- Relicensed IE, because it was about time. It's not perfect but it's better than uncertainty
- added a basic description to the mcmodinfo (thanks carstorm)
- added a configurable, global, modifier for the Cloche's Fertilizer effectiveness
- cleaned up internal BlockState logic a bit (thanks Malte)
- changed Chisel compat to use proper IMC keys
- changed Stone Multiblocks to be immovable to pistons
- changed RedtoneBreakers to not visually connect to non-direct redstonedust
- fixed broken rendering for blocks in non-standard layers (thanks Malte)
- fixed config checks for @Mapped values causing issues (thanks Malte)
- fixed issues with invalid block rotations (thanks Malte)
- fixed Autoworkbench not disassembling properly (thanks Malte)
- fixed crashes caused by empty ItemStacks (thanks Malte)
- fixed Multiblocks not firing visual updates properly (thanks Malte)
- fixed Eletrode Blueprints not being craftable
- fixed Fluid Pump and Sheetmetal Tank not preserving NBT data
- fixed Storage Crates writing empty enchantment tags on pickup
- fixed Wire connections not synchronizing properly when switching between worlds (thanks Malte)
- fixed Assembler consuming too much fluid in edgecases (thanks Malte)
- fixed desync issues with the Workbench in multiplayer (thanks Malte)
- fixed waterwheel rotation issues (thanks Malte)
- fixed shooting non-living entities with the Chemthrower (thanks Malte)
- fixed Mixer recpies not showing tooltips in JEI
- fixed Cloche voiding seeds

##### Version 0.12-74 - BUILT
- changed IE to ACTUALLY load on all 1.12 subversions

##### Version 0.12-73 - BUILT
- added whitelist configs for the toolbox
- re-enabled WAILA compat
- re-enabled OpenComputers compat (not sure if it works)
- changed IE to load on all 1.12 subversions
- changed Metal Press render to have items sink a bit down to hide blocks
- changed IE blocks to allow states for directional setting (thanks Malte)
- changed Crushers to have a limited size queue to avoid login crashes
- fixed toolbox crashing when placed (thanks Malte)
- fixed lighting of the chemthrower particles (thanks Malte)
- fixed drill and revolver looking derpy when fancy animations are disabled
- fixed the Miló Shader's name (thanks Malte)
- fixed client-access exceptions with TOP handler
- fixed Foresty compat errors
- fixed cycle animations for the gunturret
- fixed issues with Botania compat

##### Version 0.12-72 - BUILT
- added a recipe to fill the Speedloader in a crafting table
- added Packing and Unpacking molds to the Metal Press
- added Jar Signing to comply with Forge's coremod guidelines (thanks Malte)
- added support for amounts on OreDict ingredients in Crafttweaker compat (thanks MatrixN1)
- added Crafttweaker compat for the Dieselgenerator + Drill (thanks Faxn)
- changed IE's items with internal storage to use ItemHandler capabilities (thanks Malte)
- changed IE's OBJ models to generate less comparative objects (thanks Malte)
- changed IE to register its recipes on the proper event, fixing Crafttweaker compat
- changed Alloy Kiln and Blastfurnace to tick down their active fuel even when not smelting (like vanilla furnaces)
- changed Pumpkin and Melon seeds to have reduce oil outputs, and reduced their growthrate
- changed Metal Press molds to be crafted in the Workbench
- changed Metal Barrels to not output fluid when receiving a redstone signal, to work as valves (thanks Berinhardt)
- changed dummies on Multiblocks to be removed from the list of ticking tiles (thanks Malte)
- changed Windmills to rotate in in accordance to wind from the north or west, rather than fixed clockwise
- fixed issues with dummyblocks spawning outside world borders (thanks Malte)
- fixed crates and toolboxes losing their inventory (thanks Malte)
- fixed various desync issues with the Engineer's Workbench (thanks Malte)
- fixed render issues with multiblock TESRs (thanks Malte)
- fixed Botania compat
- fixed various desync issues with the Revolver & Speedloader HUD displays (thanks Malte)
- fixed Multiblocks not dropping their contents
- fixed Diesel Generators not allowing levers to be attached
- fixed tooltip localisation for Alloy Kiln and Automatic Workbench
- fixed Stripcurtains not notifying the strong signal to turn off
- fixed Railgun projectiles not colliding with entities properly
- fixed Fluid Pipes not showing their covers with CTM installed
- fixed inventory models for Engineering blocks with CTM installed

##### Version 0.12-71 - BUILT
- added missing recipes for Scaffold Slabs & Stairs
- added Albedo compat for Flare Cartridges
- added a recipe to make torches out of wool and creosote
- added the "Ancient" shader
- added the Covered Vertical Conveyor
- added the Chemthrower Multitank upgrade
- re-added the Hemp to String recipe
- changed logging to use separate logger rather than FMLLog (thanks Malte)
- changed Garden Coche to allow Podzol as a mushroom soil
- changed the Cloche to run at a reduced speed
- changed all multi- and dummyblocks to be immovable by Pistons, because Quark
- fixed Core Sample Drill animation (thanks Malte)
- fixed a bunch of Capability errors (thanks Malte)
- fixed turrets causing issues in target acquisition (thanks Malte)
- fixed client disconnecting due to NBT overflow in crates and toolboxes (thanks Malte)
- fixed broken remapper (thanks Malte)
- fixed conveyor recipes requiring single inputs (thanks Malte)
- fixed Tesla Coil animation not getting reset (thanks Malte)
- fixed Chisel compat conflicting with CTM
- fixed HarvestCraft compat not handling water and milk in the assembler properly
- fixed auto-breakers causing issues with Storage Crates and others
- fixed shiftclicking being broken in the Cloche GUI
- fixed Manual not reopening on the selected page
- fixed Railgun not working on Ender Dragon, Crystals and non-living entities (thanks Malte)
- fixed Bullets not loading the damage from the config properly
- fixed deprecated uses of getDrops
- fixed Splitting Conveyor being derpy after being rotated

##### Version 0.12-70 - BUILT
- added Shader-capabilities to the Heavy Plated Shield
- added Scaffolding Slabs and Stairs
- added a tiny bit of Albedo compat. Makes fancy lights happen on the Tesla Coils!
- changed Mystical Agriculture compat to work for Inferium seeds
- changed bullets to have increased speed
- changed HE cartridges to travel in an arc and make a different sound
    - API update, cartridges can define custom sounds
- changed, updated and improved the CTM compatability (thanks InsomniaKitten)
- fixed Treated Wood recipes not consuming creosote
- fixed up fence-connections for some IE blocks
- fixed duplication glitch within the Refinery
- fixed particle issues with some Multiblocks

##### Version 0.12-69 - BUILT
- fixed TConstruct dependency
- fixed Assembler not properly handling Fluid Ingredients from IE and CoFH
- fixed Revolver GUI not properly syncing
- fixed model loading error (thanks PaleoCrafter)

##### Version 0.12-68 - BUILT
- re-added TConstruct compat
- added Fertilizer compat for ActuallyAdditions, BetterWithMods and IC2
- changed player arms for certain IE items to also affect armor
- changed shield to use Forge's new provided shield method
- changed Mystical Agriculture compat to not output seeds anymore
- changed Arc Furnace, Squeezer and Fermenter to only output comparator signals for their input
- changed revolver GUI to allow display of dual-wielded revolvers
- fixed cloche not accepting power from the bottom block
- fixed speedloader recipe mistakenly crafting a revolver
- fixed gunpowder recipe being active even when there isn't any charcoal dust
- fixed shield only equipping on the clientside with the magnet glove
- fixed shield flickering when recharging in offhand
- fixed back button in the manual failing
- fixed shaders resulting in invisible revolver grips
- fixed crafttweaker integration
- fixed various AABBs errors causign projectiles to miss
- fixed cloche having incorrect item handling (leading to trouble with EIO conduits)
- fixed Forestry compat not loading
- fixed Fluid Router allowing for an infinite loop
- fixed Assembler not handling some recipes properly

##### Version 0.12-67 - BUILT
- added the Probe Connector. It can read Comparator Overrides on Inventories and stuff!
- added fancy animations for the reload with a speeloader and opening the revolver GUI
- added the Circuit Board material, requires for Probe Connector and Turrets
- added Comparator interfacing to a lot of IE multiblocks (using the RS controlpanel)
- re-added animations for the drill. It rotates again \o/
- changed TE OreDict comapt to run in init phase
- changed Revolvers to aim at enemies properly
- changed re-equip animations for Revolver to look better
- changed the Speedloader to a separate item, added a unique reload sound
- changed the link system in the manual to allow linking to specific crafting recipes
- fixed missing recipes for Metal plates
- fixed Uranium Blocks + Slabs not having a recipe
- fixed IE's fluids to always stitch into the sheet and have buckets load correctly
- fixed missing Hammer Crushing recipes
- fixed IE recipes using fluids not emptying containers properly & duplicating them
- fixed recipes for Aluminium and Steel wires

##### Version 0.12-66 - BUILT
- fixed the missing dustCharcoal reference in recipes

##### Version 0.12-65 - BUILT
- changed crafting recipes to the new json based system!
- fixed ClientProxy trying to subscribe on the Server side
- fixed arm sleeves on players not matching arm position. Nothing I can do about armor, I'm afraid.

##### Version 0.12-64 - BUILT
- Updated to Minecraft 1.12, fixed a lot of the issues resulting from this update
    - NOTE: Currently, IE's vanilla crafting recipes are not yet JSONs. This will probably change in future releases.
    - NOTE ALSO: Update may be a bit unstable. Feel free to report issues on github.
- added Advancements to replace Achievements, some even give rewards!
- added a custom trigger to detect formation of multiblocks
- added two new Shaders: WAAAGH! and Lusus Naturae
- added a super fancy spinning animation to Revolvers that use a specific skin.
    - atm limited to two exclusive skins, may be expanded in future.
- changed holding animations for Revolver, Drill, Chemthrower and Railgun
    - has a config option in case it conflicts with other animation mods
- changed certain Mineral Veins to output Sulfur Dust
- changed OneProbe integration a tiny bit, showing SideConfigs on Barrels, Capacitors, etc.
- fixed Minecart shaders crashing with layer counts over the default

##### Version 0.11-63 - BUILT
- added the Heavy Plated Shield!
    - it protects like a normal shield
    - it has awesome upgrades
- added Vacuum Tubes as a new mid-game crafting material
- added a manual entry for the new components
- added Crafttweaker integration for Blueprint recipes
- changed/fixed HUD displays for Railgun, Chemthrower and Revolver
- changed Turrets to be interactable from the top block (thanks Malte)
- changed Arc Furance to distribute inputs evenly again (thanks Malte)
- changed Sulfur and Saltpeter textures to be Hazards instead of my terrible ones
- changed recipes for Mechanical components, added Bluepritns for reduced cost
- changed JEI integration to use newer JEI methods
- changed Engineer's Workbench to no longer be part of IE's main achievement progression
- fixed Redstone connectors not properly accepting signals (thanks Malte)
- fixed OC compat not loading due to incorrect mod ID (thanks Malte)
- fixed Capacitor Backpack potentially charging itself
- fixed crashes with Capacitor Backpack and Railgun interacting with capabilities (thanks Malte)
- fixed localization file getting screwed up to ASCII
- fixed up interacton between Silos and RefinedStorage (thanks Malte)
- fixed ghostloading messing up randomizers for a lot of stuff (thanks Malte)
- fixed a plethora of ghostloading issues (thanks Malte)
- fixed Capacitor Backpacks being all-around derpy when crafting (thanks Malte)
- fixed broken Pipe renders (thanks Malte)
- fixed Turntable not reacting to redstone signals
- fixed Fluid Outlet outputtign through closed sides
- fixed render issues with the charging station (thanks Malte)
- fixed (possibly) Wooden Crates in villages losing their contents on pickup

##### Version 0.11-62 - BUILT
- official 1.11.2 release, same changelog as build 59

##### Version 0.11-59 - UNRELEASED
- Updated to Minecraft 1.11.2 and all the bugfixes that came with it! (thanks AtomicBlom and Malte for all the help <3)
- added a migration system to allow loading 1.10 worlds into MC 1.11.2! (thanks Malte)
- added special chemthrower behaviour for fluid concrete. It's the GLOO cannon from Prey! :D
- added compat for Forestry's fertilizer to be used in the cloche
- added the Alloy Kiln!
    - a small multiblock that allows earlygame alloying of metals
    - also features Crafttweaker support (thanks primetoxinz)
- added new textures for the Dynamo, copper ingots, wirecoils and coil blocks (thanks Mr. Hazard)
- added the Capacitor Backpack! Supplies your items with power!
- added Thermal Foundation's Phyto-Grow as a valid Cloche fertilizer
- added Sulfur & Saltpeter dust to make gunpower. Better texture and more uses soon!
- changed balance on Drills: Heads have more durability, lubrication reduces wear, augers boost speed
- changed Turrets to use the improved Tile Renders (thanks Malte)
- changed outputsize on Coke and Blastbrick recipes, reducing earlygame resource costs
- changed the Windmills!
    - new model (courtesy of Mr. Hazard)
    - instead of having two types, they are now "upgraded" with sails
- changed produce&seed output for wheat and beetroot in the Cloche
- changed Toolboxes and Wooden Crates to keep their enchantments on placement
- changed Railgun to store less energy (so the Capacitor Backpack actually has a reason to exist), buffed its damage by 50%
- changed Redstone Connectors to properly connect to Redstone Dust (thanks Malte)
- fixed recipes for redstone ignoring conveyers
- fixed render-crash with the Cloche
- fixed nested configs not generating
- fixed inventory texture for the LV connector
- fixed cross-mod compat for the Cloche
- fixed soil-texture getter for the cloche
- fixed Covered Conveyors not protecting inserted items against pickup
- fixed Improved Blast Furnace consuming too much fuel (thanks Malte)
- fixed Multiblock disassemly code, specifically regarding multi-break-tools like Drills or TCon Hammers (thanks Malte)
- fixed possible NBT overflows with toolboxes, crates, shulkerboxes, etc (thanks Malte)
- fixed Fluid Pipe covers not initializing properly (thanks Malte)
- fixed recipe-wildcard handling in the manual (thanks Landmaster)
- fixed logic and effects for the Grunt Birthday Party achievement
- fixed missing sounds for Metal Press and other things
- fixed JEI display for the Crusher to show secondary output percentages
- fixed Jerrycan/Sheetmetal Tank interaction (thanks Malte)
- fixed Arc Furnace not recycling wire coils because wire was an unknown resource

##### Version 0.10-58 - BUILT
- added the Fluid Router. It does exactly what the Item Router does, but for fluids.
- added Crafttweaker integration for the Mixer
- added the Garden Cloche! It grows a variety of crops!
    - added farming compatability for Actually Additions
    - added farming compatability for Attained Drops
    - added farming compatability for Mystical Agriculture
    - added farming compatability for Harvestcraft
    - added farming compatability for Better With Mods
    - added farming compatability for Extra Utils
- added Chemthrower compatability for ThermalFoundation fluids
- added carpet, quarter and threequarter thicknesses for Concrete to make the fluid dry to more reasonable levels
- added a toggleable option to the Mixer to output all fluids instead of just the bottom one
- added placeable Toolboxes, they are fully accessible while placed
- added placeable Coresamples, these can be rightclicked with a map to set a marker to their origin
- added a visible chunk boundary when holding or looking at the Sample Drill
- added Redstone Wires to the API (thanks Malte)
- added a render reset hook to the API (thanks Malte)
- added the Covered Conveyor, preventing players from picking up items from it
- added a Conveyor unaffected by redstone
- added the Fluid Outlet. It puts fluids in the world!
- added OC compat for Mixer and BottlingMachine (thanks GuyRunningSouth)
- added a Villager Job to sell Shader Bags
- added a Crusher recipe to turn slag into sand
- added a special ItemFrame render for Blueprints
- added the ability to flip Powered Lanterns using the hammer
- added the option for Strip Curtains to output a strong RS signal
- added a sound to the Breaker Switch - Clearly the best feature of this update
- changed Razorwire to render extended wooden boards when stacked
- changed the Conductive debuff to apply to Teslacoil and Electro-Razorwire damage
- changed metal plates and Sheetmetal to be craftable for any IE-used metal
- changed Crafttweaker functions for Squeezer and Fermenter to feature input-based removal
- changed dropping Conveyors to use Iron Trapdoors rather than Hoppers in the recipe
- changed fluid potions to have reduced durations in the chemthrower
- fixed Concrete blocks not applying suffocation damage
- fixed some cartridges being allowed in turrets, fixes crash with Immersive Floofs
- fixed energy drain on eletrified bullets, reduced drain for multii-projectile cartridges
- fixed ownerless, tameable entities causing crashes in Turret targeting
- fixed item in- and outputs on Turrets being unavailable
- fixed manual entry for Razorwire
- fixed NullPointer on FluidStacks
- fixed Turntables and Hammers being able to rotate extended pistons, chests, beds, end portals and skulls
- fixed the Drill taking more damage than intended (thanks Malte)
- fixed the display of double arrays in the manual (thanks Malte)
- fixed lang file as per suggestions on github
- fixed Balloons and Redstone Connectors being invisible in the End
- fixed Chemthrower projectiles being set on fire even when the fluid isn't flammable
- fixed invalid connections being made possible due to splitting stacks (thanks Malte)
- fixed bounding boxes and wire offsets for transformers (thanks Malte)
- fixed transformers mistakenly having TESRs and accepting LV+MV wires in combination (thanks Malte)
- fixed the Potion fluid not having textures (thanks Malte)
- fixed ConcurrentModificationException on Crafttweaker reloads (thanks Malte)
- fixed the Mixer not working when its internal tank is filled (thanks Malte)
- fixed IE slag not being usable in ThermalExpansion recipes
- fixed the Turrets losing its inventory (thanks Malte)
- fixed NBT sensitive fluids not outputting from barrels properly (thanks Malte)
- fixed a dupebug of empty fluid containers in the Assembler
- fixed Assembler outputting damageable craftign items
- fixed missing sounds on IE explosions
- fixed Refinery dividing by 0 if timer config is adjusted downwards
- fixed AutoWorkbench not exposing the right slots to its item handler and rendering the wrong items
- fixed projectiles ignoring PvP protection within player teams
- fixed wires not being removed properly if TileDrops are off (thanks Malte)
- fixed floating Engineer's houses
- fixed broken Energy Storage tooltips
- fixed Metal Press not dropping its mold when broken
- fixed various double-size blocks deleting things above them
- fixed stuttering on Metal Press and Watermill (thanks Malte)
- fixed Relays connecting to energy accepting things, to make their role more obvious (thanks Malte)
- fixed resource reloading on TESRs (thanks Malte)
- fixed various Turret projectile issues (thanks Malte)
- fixed a Blast Furnace processing speed bug (thanks Malte)
- fixed Skyhook being a stupidly powerful weapon (thanks Malte)
- fixed the Crusher outputting to the wrong side
- Translations Added/Updated: ru_RU (lex1975) (this time for real, since I forgot it last time), zh_TW (xaxa123)

##### Version 0.10-57 - BUILT
- added RazorWire! It pricks and slows, and you can electrify it.
- added Fluid Concrete. Slows entities down and immobilizes them when it dries
- added a Potion fluid. Effects vary on NBT data. Works in the Chemthrower, can be bottled in Bottling Machine.
- added blocks for (almost) all IE Fluids
- added the Mixer! It mixes solid components into fluids to make concrete or various potions!
- added Turrets! Configurable targeting, currently allowing for fluids (Chemthrower) or bullets (Revolver) as ammo
- changed Floodlights to allow inversion of redstone control
- changed Charging Station to allow charging Forge Energy items as well
- fixed Bottling Machine recipes not consuming fluid and duping items (thanks Malte)
- fixed Redstone Wires illegally connecting to relays
- fixed ArcFurnace recipes causing NPEs when added through Minetweaker
- fixed JEI handler preventing Draconic Evolution for loading theirs
- fixed OneProbe showing components of Multiblocks rather than the MB itself
- fixed derped texture on Metal Press
- fixed side-solidity for Stone Multiblocks
- fixed the density of Creosote Oil. A certain Lemming notified me that it's in fact, denser than water.
- Translations Added/Updated: ru_RU (lex1975)

##### Version 0.10-56 - BUILT
- fixed duplication of stacks of items dropped onto the bottling machine
- fixed EntityDataManager registration (thanks mconwa01)
- fixed manual entry of the fluidpump citing the use of "wirecutters" instead of "hammer"
- fixed manual typo in the fermenter entry

##### Version 0.10-55 - BUILT
- re-added the Bottling Machine! It bottles things up, like fluids and emotions!
- added indicative scrollbars to the manual
- added nullcheck to guard against malformed sound events
- added a login check to synchronizing Excavator data
- added a config option for hte drop weight of Hemp Seeds
- changed IC2 compat to no longer load with IC2 classic
- changed permission levels on comamnds to allow "/cie resetrenders" for everyone
- fixed Minetweaker and JEI integration. Arc Furnace reduced to a single categroy, I'm afraid.
- fixed Automatic Workbench using Pair from JavaFX
- fixed null-master exceptions for Multiblocks
- fixed rotations of wooden blocks (Gunpowder Barrel, Item Router)
- fixed GUI of the Item Router
- fixed SideOnly exceptions triggered by Chisel&Bits
- fixed Withers dropping Lootbags. They really shouldn't anymore.
- fixed world loading caused by connection persistence and validation (thanks Dogboy21)
- fixed Crusher not collecting drops from low-health entities
- fixed Assembler and Metal Press not reorienting conveyors when broken

##### Version 0.10-54 - BUILT
- added the Automated Engineer's Workbench, allowing automated handling of Blueprint Recipes
- added the Turntable, a block that allows rotating of other blocks
- added the "Dominator" shader
- changed Wirecoils to change their distance display to red when too far from their linking point
- changed Graphite Electrodes to stack to 16 to make them more viable as Railgun ammo
- changed Assembler to allow crafting IC2 recipes (thanks Malte)
- fixed issues with lowercasing without locale
- fixed ThermoElectric Gens, Dynamos and Wire Connectors not handling Forge Energy
- fixed Shaderbags having their conversion recipes crash
- fixed duplicate rod recipes in the Metal Press
- fixed TConstruct book not showing Hemp item
- fixed multithreading issues (thanks Malte)
- fixed crash with unknown recipes in the Assembler (thanks Malte)
- fixed resource overhead with Bucket Wheel rendering (thanks Malte)
- fixed Manual not handling scrolling lists properly
- fixed Sheetmetal Tanks not rendering fluids with colour
- fixed Chemthrower not dropping player-only items (Blazerods)
- fixed Buckshot and Dragonsbreath having the wrong return cartridges and the wrong DamageSource
- fixed needless chunkupdates caused by Blastfurnace Preheaters
- fixed Splitting Conveyors flipping directions too early

##### Version 0.10-53 - BUILT
- added new shaders
- fixed Sample Drill crashing upon energy insertion
- fixed Tesla Coil crashing upon energy insertion
- fixed Revolver rendering on Tiny Potatoes
- fixed Refinery not allowing extraction
- fixed achievement for crafting the Mining Drill not firing
    - retro-actively firing crafting achievements when modifying them in the workbench
- fixed Lightning Rod not building its fence-net properly
- fixed bounds and disassembly of the Lightning Rod
- fixed Splitting Conveyors sometimes not clearing temporary NBT data from entities
- fixed a bit of log spam by invalid/missing models
- fixed Blueprints not taking unstacked resources
- fixed Vertical Conveyors not rendering walls on their base properly
- fixed a dupebug with InventorySorter
- fixed Fluids not updating to existing entries when failing to register

##### Version 0.10-52 - BUILT
- added the ability for Dropping Conveyors to drop through trapdoors
- added a new shader
- fixed Nullpointer for Connectors accepting FUs

##### Version 0.10-51 - BUILT
- re-added Festive Crates! Happy Holidays!
- re-added Botania compat for shaders, Relic rarity lootbags + shaders
- added the ability for Strip Curtains to emit redstone when an entity passes through
- added compatability for Forge Energy / Forge Units. End of an era, I guess.
- fixed a nullpointer on JumpCushions
- fixed Splitting and Dropping Conveyor not inserting into inventories
- fixed crashes in the manual due to incomplete tesselation
- fixed load-cycle with TCon
- fixed HarvestCraft recipes usign water/milk not working in the assembler
- fixed Furnace Heater constantly updating the block when insufficiently powered
- IE now has a Maven (thanks SkySom)

##### Version 0.10-50 - BUILT
- added versioncheck for TConstruct to prevent crashes with ranged weapons
- added Icons for all IE machines to JEI and fixed recipe displays
- fixed furnace heater constantly updating chunks
- improved UV maps on the revolver, added a new shader

##### Version 0.10-49 - BUILT
- rewrote the shader system
    - improved texture performance
    - changed API integration
    - added listing in the manual, including re-ordering
    - removed the recipe to turn shaders back into bags
- added the ability to rename wooden crates
- added IRotationAcceptor interface for dynamos and the things that drive them (thanks Malte)
- re-added the config for Excavator fail chance (thanks Malte)
- re-added the ability to dye balloons (thanks Malte)
- re-added the ability to add shaders to balloons
- changed the drill's hardness checks to easier dig ores (thanks Malte)
- fixed duped fluidcontainers on treated wood recipes
- fixed incompat issue with new versions of TConstruct
- fixed CraftTweaker compat (thanks mezz)
- fixed Crusher recipe for sand requiring grass rather than gravel (thanks MalkContent)
- fixed NPE on inserting items
- fixed Wirecutters consuming durability and not working properly on transformers
- fixed various client issues (thanks Malte)
- fixed Structural Connectors and Redstone Connectors not displaying properly (thanks Malte)
- fixed Conveyors throwing NPEs on Servers (thanks Malte)
- fixed dupebug with Charging Station
- fixed a few missing sounds
- fixed a concurrent modification in Tesla Coil sounds (thanks Malte)

##### Version 0.10-48 - BUILT
- added CTM support to Engineering Blocks and Scaffolds when Chisel is installed (in cooperation with Drullkus)
- changed Breakers to be rotatable (thanks Malte)
- changed Conveyor Belts showing walls when having their sides towards a metal press or assembler
- fixed missing comments in the config file
- fixed breaker switches crashing on servers (thanks Malte)
- fixed IE models causing lag on chunk updates (thanks Malte)
- fixed Breaker Switches not outputting redstone signals (thanks Malte)
- fixed Redstone Breakers ignoring their inversion (thanks Malte)
- fixed Ear Defenders being broken (thanks Malte)
- fixed Squeezers and Fermenters not outputting Items automatically
- fixed Silo not auto-outputting when supplied with a redstone signal
- fixed Railgun not shooting alu, iron and steel sticks from other mods
- fixed Manual on Railguns crashing without Railcraft installed (thanks Malte)

##### Version 0.10-47 - BUILT
- moved all clientside references from the config

##### Version 0.10-46 - BUILT
- fixed serverside crash due to abstract proxy
- fixed mcmod.info file

##### Version 0.10-45 - BUILT
- change over to Forge's new config system:
    - DELETE YOUR IE CONFIG
    - Or don't, but you'll get a load of duplicate and unnecessary options.
    - If an old config breaks anything, you were warned.
- added WAILA support for Tesla Coils
- added The One Probe Compat for machines and energy storage
- added a config option to disable villagers and their houses
- added a config option to disable OreDict names in Advanced Tooltips
- re-added config option for Railgun damage
- re-added support for Railcrafts rails and rebar to be used as projectiles
- re-added shader support for Railcraft carts
- re-added the placement options for Redstone Breakers (thanks Malte)
- changed blocks with configurable sides (capacitors, barrels) to use unlisted properties and lazy loading. Reduces IE's model count at startup by >75%
- change Refinery model to have a big power input like Fermenter and Squeezer
- fixed Refinery not properly accepting fluids
- fixed crash when crafting cetain items (thanks Malte)
- fixed Conveyors dropping invalid items (thanks Malte)
- fixed Pumps not working due to miscalculated XORs (thanks Malte)
- fixed Watermills having too small models
- fixed wonky wire rendering (thanks Malte)
- fixed material list for the Lightning Rod
- fixed Faraday suit not blocking Tesla damage
- fixed Floodlight having broken rendering and expanded computer support (thanks Malte)
- fixed Buckshot projectiles not applying full damage
- fixed Scaffolding not rendering properly on the inside
- fixed breakerswitch not emitting redstone
- fixed force chunkloading through redstone connectors
- fixed Toolbox voiding items
- fixed Pipes resetting connection configs (thanks Malte)
- fixed Silo voiding items and not having Comparator output
- fixed Pipes not dropping their covers
- fixed placement logic for multiblock placements (posts, transformers, drill, teslacoil)
- fixed CraftTweaker integration for the Refinery
- fixed stair rendering
- fixed explosion resistance for leaded concrete stairs and slabs
- fixed Crusher overloading its process queue
- fixed multiple minor issues

##### Version 0.10-44 - BUILT
- fixed clientside exception

##### Version 0.10-43 - BUILT
- added version dependency for TCon to prevent further reports of that
- added Banners! They look awesome! Shoutouts to Tris for making all the textures!
- added support for TConstructs resourcepack based materials
- added Strip Curtains. Purely decorative, dyeable, pretty nice for factories!
- re-added the ability to climb scaffolding on pipes
- re-added the Lightning Rod!
- expanded the "allow" and "interdict" functions on the Hammer in regards to multiblocks
- expanded the API on wires for an addon
- changed manualpages for Multiblocks to be better at everything (thanks boni)
- greatly improved renderperformance on waterwheels and windmills (thanks Malte)
- fixed wooden barrels not being toggleable
- fixed wooden barrels acceptign invalid fluids
- fixed side-solidity on pumps
- fixed changelog display in the Engineer's Manual
- fixed jerrycans not getting filled properly and beign lost in crafting
- fixed broken villager trades
- fixed glitches in the toolbox (hopefully)
- fixed <ERROR> overlays when WAWLA is installed (thanks Malte)
- fixed WAILA display for EnergyProviders
- fixed crash with Phial Cartridges
- fixed Homing Cartridges not doing damage
- fixed CraftTweaker method name
- fixed Balloons not emitting light
- fixed crash during Arc Furnace recycling calculations (thanks Malte)
- fixed Bucket Wheel continuing to rotate when the Excavator is broken and desyncing (thanks Malte)
- fixed LootEntry manipulation (3rd time's the charm?) (thanks Malte)
- fixed crash with Railcraft (thanks Malte)

##### Version 0.10-42 - BUILT
- fixed NPE when throwign items into the crusher
- attempted fix for render crashes with TESRs and their blockstates

##### Version 0.10-41 - BUILT
- added the function to dye conveyor belts!
- added the splitting conveyor belt, it alternates left and right!
- re-added the ability to pull recipes from JEI into the assembler
- re-added charge counter to railgun
- re-added some missing achievements
- re-added the ability to cover pipes in scaffold. IMC messages an define additional whitelisted blocks
- re-added Crafttweaker/Minetweaker integration
- updated TCon compat to latest TCon
- removed Fluid Bottles. People can shut up about those now >_>
- fixed Assembler not crafting and occasionally crashing
- fixed Arc Furnace rendering, output rates and dupebugs
- fixed slab rendering
- fixed wooden crates not updating comparators
- fixed floodlight being terrible (thanks Malte)
- fixed lots of bugs with wire models (thanks Malte)
- fixed crashes with projectiles
- fixed SkyHook being derpy
- fixed Transformers not attaching on posts
- fixed IE blocks not having placement sounds
- fixed selection and colision bounds on multiple blocks
- fixed OC comapt on the crusher being borked (thanks Malte)
- fixed lootentry access error (thanks Malte)
- fixed wires sometimes not rendering on servers
- fixed a manual derp (thanks Malte)

##### Version 0.10-40 - BUILT
- fixed machines crashing
- fixed sheetmetal tank not outputting

##### Version 0.10-39 - BUILT
- added Bloodmagic compat:
    - demon will bullets. 'nuff said.
- re-added Chisel Compat. Won't work till Chisel actually brigns IMC integration back in
- re-added Botania compat:
    - conveyor belts are immune to magnets
    - terrasteel bullets are back
    - armed the potatoes
- re-added Wolfpack Cartridges!
- changed Water- and Windmills to use normal TESR again after all
- changed Lanterns to be rotateable with the hammer
- changed a load of Assembler functionality. Fixed fluid crafting and container items
- changed multiblocks in the manual to indicate whether the components are in your inventory
- changed arcfurnace to output slag faster
- fixed lighting on wires and connectors (thanks Malte)
- fixed wire rendering accross chunks; they split into two renders to at least give indication now (thanks Malte)
- fixed recipes for Faraday Armor
- fixed incorrect checks for empty lists when creating ingredient stacks
- fixed Universal Bucket being initialized too late
- fixed crashes relating to missign Railgun ammo
- fixed Wooden Posts not extending arms
- fixed derpy looking railgun and missing sounds on the Revolver
- fixed windmills not recognizing walls properly
- fixed JEI rendering for Metal Press and Crusher
- fixed multiple capability issues on IE tiles
- fixed conveyors shooting items out to the sides when inserted into
- fixed floodlights crashing on placement
- fixed balloons not being consumed when placed in the air and suffocating players
- fixed conveyors from the creative menu being unplaceable
- fixed dieselgen crashing for invalid fuel
- fixed rotations on the bucket wheel
- fixed broken renderign on the ear defenders
- fixed drill and chemthrower losing fluid when modifying
- fixed shiftclicking bullets into the revolver
- fixed dieselgen and refinery not accepting fluids when mirrored
- fixed hemp being plantable on non-farmland
- fixed arcfurnace consuming too many additives
- various other small fixes!

##### Version 0.10-38 - BUILT
- added the new IConveyorBelt system
    - fully dynamic conveyor system, with custom models and all
    - addon mods can easily add their own!
    - features magnet prevention. Works on EIO, in future BM and Reliquary (?)
- added Crusher recipe for coke blocks
- re-added EIO compat, mostly Arc Recipes
- fixed Assembler not assembling (thanks Alex)
- fixed Chemthrower Projectiles making arrow noises
- possible fix for the black rendering wires
- fixed (finally, hopefully) the desync caused by forming Coke Oven and Blast Furnace

##### Version 0.10-37 - BUILT
- added the new IBullet system!
    - bullets are no longer based on metadata, but use NBT tags to differentiate types, their abilities are handled externally
    - updated manual accordingly
- added recipe for leaded concrete
- changed Conveyors to have a better placement system
- fixed crashes when shiftclicking in revolver gui
- fixed slots not assignign properly in revolver gui
- fixed a bunch of manual things

##### Version 0.10-36 - BUILT
- added Hemp Seeds to Forestry's bags
- changed Windmill and Improved Windmill to have models with 90% less polys. PERFORMANCE!
- removed unnecesary debug outprints
- fixed NPE in manual
- fixed issues on the manual's mineral pages
- fixed models for coresamples

##### Version 0.10-35 - BUILT
- re-added Open Computers compat
- cahnged waterwheel and windmills to use FastTESR
- fixed NPE in ShapedIngredientRecipe
- fixed Treated Wood display in the TCon Book

##### Version 0.10-34 - BUILT
- added TCon alloy recipes
- fixed crash in earmuff recipe, using a client access method
- fixed arc alloy recipes having too little output
- prepping for FastTESR hopefully soon!

##### Version 0.10-33 - BUILT
- fixed crashes when break smart models, parsed texture into baked builder
- fixed ItemRenders for multiblocks
- fixed rendering of Wind- and Watermills
- fixed entrylsits in the manual being short
- fixed the forming of multiblocks scrambling the player's inventory
- fixed treated wood recipe consuming buckets (Forge Bug, really...)
- fixed machines crashign when fillign buckets internally

##### Version 0.10-31 - BUILT
- added Redstone Wire connector
- cleaned up recipes and manual a little
- fixed wire rendering
- fixed crash with cart shaders

##### Version 0.10-30 - BUILT
- UPDATE TO 1.10.2

##### Version 0.9-29 - BUILT
- UPDATE TO 1.9.4
- mostly port, movign stuff to new fluid systems, capabilities, etc
- moved 3D Maneuver gear. It didn't fit the style of hte mod and was incredibly glitchy

##### Version 0.8-28 - BUILT
- re-added the Floodlight back in
- re-added the transformers that can attach to posts (thanks Malte)
- re-added the ability for the pump to not create cobblestone and improved the pump (thanks Malte)
- added the Faraday Suit, an armor to survive tesla coils and allow for lightshows! (code by Malte, art by Mr. Hazard)
- added sound and nicer animations to the metal press
- added new textures for the Capacitors! Celebrating IE's one year of public release! :D
- changed recipes for WireCoil. They now take wire which is made out of plate with shears/wirecutters or with the metal press
- changed wirecutters to take durability damage when removing wires from connectors
- added fluorescent tubes. They glow when near a tesla coil! (thanks Malte)
- added a "Bad Eyesight" config to make the manual render in darker, bold font. Can mess up formatting
- fixed a crash and dupe bug with the toolbox (thanks Malte)
- fixed weird render issues and crashes with multiblocks (thanks Malte)
- fixed silo voiding items (thanks Malte)
- fixed a crash when exploding bullets hit reactive creatures like endermen (thanks Malte)
- fixed a crash when pumping fluids out of sheetmetal tanks (thanks Malte)
- fixed rotation bug on the bucketwheel (thanks Malte)
- fixed a bug where houses try to spawn the nonexistent villager
- fixed most IE blocks letting through light when they shouldn't (thanks Malte)

##### Version 0.8-27 - BUILT
- re-added documentation on biodiesel (thanks Malte)
- re-added the Assembler! For all your autocrafting needs!
- changed the plantoil value for hemp seeds back to what it was in 1.7.10
- changed internal rendering of connectors to use vflip again rather than flipped textures (thanks Malte)
- changed conveyor belts to halt their animation when red
- added comparator support to the current transformer (thanks Malte)
- added a hacky workaround for WAILA making wires look bad (WAILA should fix this, but we'll manage) (thanks Malte)
- added a manual page for Uranium Ore
- added a proper sound to the revolver firing
- added the Tesla Coil! It will shock you!
- added the ability to allow Tinkers Tools to the toolbox, as well as a better API to allow adding them (thanks Malte)
- fixed sheetmetal tanks showing the wrong structure in the manual
- fixed recipe for leaded concrete stairs
- fixed gunpowder barrels exploding when punched
- fixed pipes not visually connecting to refinery
- fixed the arc furnace only processing one item at a time (thanks Malte)
- fixed the arc furnace voiding items if the first output slot is full (thanks Malte)
- fixed TESR render crash (I still totally blame Vanilla/Forge for that) (thanks Malte)
- fixed invisible molten metals in Tinkers compat (thanks Malte)
- fixed pipes not transferring properly (thanks Malte)
- fixed invisible wires on balloons (thanks Malte)
- fixed balloons not being placeable in mid air (thanks Malte)
- fixed bottles not getting filled in squeezer/fermenter (thanks Malte)
- fixed Skyhook pulling people into walls (thanks Malte)
- fixed Dropping Conveyors not dropping (thanks Malte)
- fixed entities getting stuck on uphill conveyors (thanks Malte)
- fixed scaffolding not beign climbable (thanks Malte)
- fixed the crusher still hurting entities even when turned off (thanks Malte)
- fixed IE blocks not being chiselable (thanks Malte)
- fixed maneuver gear dupe bug (thanks Malte)
- fixed furnaces not lighting up when using external heater (thanks Malte)
- fixed scaffolding not being placeable while jumping (thanks Malte)
- fixed preheaters not consuming power correctly and not having a manual entry
- fixed crusher crushing entities on collision


##### Version 0.8-26 - BUILT
- IE REQUIRES JAVA 8 NOW!
- added a load of Tinkers Construct compat:
	- Treated Wood tool material. Similar to normal wood, but no splinters
	- Constantan tool material. Additional weapon effects based on biome temperature
	- Slime Fluids works in the chemthrower again
	- fixed smeltery alloy recipe for Constantan
- re-added InvTweaks compat to the Wooden Crates
- re-added Railgun and Chemthrower documentation to the manual
- buffed the default damage of the Railgun
- changed silver bullets to do extra damage to undead (Witchery isn't updated, so they'd be useless otherwise)
- fixed Multiblocks looking derpy when formed (thanks Malte)
- fixed connection offsets on transformers (thanks Malte)
- improved connections on transformers (thanks Malte)
- fixed connections on pipes looking weird and broken (thanks Malte)
 

##### Version 0.8-25 - BUILT
- wires can transfer across unloaded chunks now! :D (thanks Malte)
- added reinforced (creeper-proof) storage crates
- changed voltmeter to allow measuring between two non-output points (thanks Malte)
- fixed an NPE in rendering
- fixed fluid pipes not rendering properly
- fixed invalidly added recipe to create dusts where no ignots exist
- fixed missing coke dust recipe
- fixed Arc Furnace not having solid sides
- fixed NPE in Improve Blast Furnace's capabilities
- fixed Crash with the Mining Drill (thanks Malte)
- fixed watermill outputting less than it should because of broken math (thanks Malte)
- fixed extremely high losses on wires (thanks Malte)
- fixed insulating glass being opaque (thanks Malte)
- ported miscellaneous other fixes from 1.7 (thanks Malte)
- added zh_CN localization (thanks to 3TUSK, Amamiya-Nagisa, bakaxyf, CannonFotter, crafteverywhere, IamAchang, Joccob, LYDfalse, UUUii)


##### Version 0.8-24 - BUILT
- massive shoutout to Malte for figuring out some proper wire rendering! :D
- added compat for DenseOres (crushing/smelting) 
- fixed lanterns not emitting light
- fixed silver bullets causing a crash
- fixed gunpowder barrels crashing because of a duplicate datamarker
- fixed gunpowder barrels dropping from explosions
- fixed CokeOvens not interacting with hoppers
- fixed wires not outputting to RF-only receivers (thanks Malte)
- fixed connections on HV relays (thanks Malte)
- fixed a crash caused by hammering the top/bottom of a wooden post
- fixed the stackoverflow in sheetmetal tanks
- fixed the themoelectric gen crashing (thanks Malte)
- fixed the waterwheels breaking more other wheels (thanks Malte)
- fixed textures on stone slabs (thanks Malte)


##### Version 0.8-23 - BUILT
I lost all my changelog before this point. Whoops.
basically: Initial git push with a lot of the machines and tools implemented. But not done yet.
