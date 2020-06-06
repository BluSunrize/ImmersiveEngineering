##### Version 0.12-98 - BUILT
- Added combat for XLFood to the cloche (LeoBeliik)
- Added Albedo combat for the flueorescent tube (Pabilo8)
- Added steel hoe (BluSunrize)
- Fixed chutes crashing on dedicated servers (Malte)
- Fixed the Skyhook crashing due to Optifine (Malte)
- Fixed division by zero error in multiblock processes (Thonikum)
- Fixed fluid evaporation in the nether when using the Jerrycan (BluSunrize)
- Fixed windmills being stopped by snow (BluSunrize)
- Fixed interaction between Sheetmetal Tanks and RFTools screens (BluSunrize)
- Translations Added/Updated: ko_kr (yor42), jp_jp (karakufire), zh_cn (mcBegins2Snow), ru_ru (Shellyoung)

##### Version 0.12-92 - BUILT
- Requires Forge version 14.23.5.2820 or higher (BluSunrize)
- Added Steel Armor, crafted from steel plates! (BluSunrize)
- Added a second MultiblockFormEvent to the API, to fire *after* the structure has been checked (BluSunrize)
- Fixed client only translation reference crashing on servers (BluSunrize)
- Fixed client only references in Chutes (BluSunrize)
- Fixed Coresamples without coordinates resulting in a client crash (BluSunrize)
- Fixed transformers on posts not accepting wires properly (BluSunrize)

##### Version 0.12-91 - BUILT
- Added the Gunsmith villager, who sells ammunition, blueprints and revolver pieces (BluSunrize)
- Fixed a crash with vertical conveyors on dedicated servers (Malte)
- Fixed two negative luck perks combining into a positive (BluSunrize)

##### Version 0.12-90 - BUILT
- Added a config option to disable fancy Blueprint rendering (BluSunrize)
- Added chutes, they drop entites straight down and sometimes to the side! (BluSunrize)
- Added creosote buckets (and other containers) as valid fuel for a furnace (BluSunrize)
- Added an option to not have coordinates display on a CoreSample (BluSunrize)
- Added a "removeAll" function to IE's crafttweaker integration (BluSunrize)
- Added a config option for the transferrates of the Fluid Pipe (BluSunrize)
- Added the ability to Sneak + Scroll to cycle the revolver manually (DaveArndt)
- Added perks to the Revolver!
  - You can find components (barrel, drum, hammer) with perks in crates or on villagers
  - Crafting a revolver with these components gives the perks to the revolver
  - This augments its fire-rate, the noise it makes and the luck (for opening chests) of the wielder
- Changed CoreSamples to draw a chunk overlay like the drill (BluSunrize)
- Changed Wires to return only half an ingot worth of material when recycled (BluSunrize)
- Changed Dragonsbreath cartridges to not fire an obscene amount of bullets anymore (BluSunrize)
- Changed pick-block to cycle through all possible wires for a connector (MalkContent)
- Changed Chemthrower to be able to affect teammates with beneficial effects (BluSunrize)
- Items on conveyors now despawn after the usual time when stuck against a block (Malte)
- Fixed wires sometimes attaching to the wrong parts of transformers (Malte)
- Fixed wires connecting to the transformer when clicking on the lower blocks (Malte)
- Fixed some ghostloading issues (Malte, with help from Barteks2x)
- Fixed the assembler not crafting recipes added using CraftTweaker (Malte)
- Fixed Alloy Smelter consuming extra coal when no further processing is necessary (BluSunrize)
- Fixed Blueprint recipes using the Wirecutter locking up after 1 crafting operation (BluSunrize)
- Fixed ArcFurnace not consuming the correct number of ingredients (BluSunrize)
- Fixed pipes disconnecting visually (MalkContent)
- Fixed pipes consuming scaffolding to cover when in creative mode (MalkContent)
- Fixed some visual glitches (BluSunrize)
- Fixed Crafttweaker handler for Blueprints ignoring NBT (BluSunrize)
- Fixed the "Aquire"-button for shaders not working outside of creative mode (Malte)
- Probably fixed the floodlight not working below 0 and above 255 with Cubic Chunks (Malte)
- Fixed coresamples not rendering when Optfine shaders are enabled (Malte)
- Fixed UraniumSlabs combining into a Copper-looking block (BluSunrize)
- Fixed a variety of issues with JEI integration (BluSunrize)
- Fixed a potential memory leak (BluSunrize)
- Fixed a TCon API error propagating (BluSunrize)
- Translations Added/Updated: jp_jp (karakufire), de_de(d-haus)

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
