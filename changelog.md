#####Version 0.12-67 - BUILT
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

#####Version 0.12-66 - BUILT
- fixed the missing dustCharcoal reference in recipes

#####Version 0.12-65 - BUILT
- changed crafting recipes to the new json based system!
- fixed ClientProxy trying to subscribe on the Server side
- fixed arm sleeves on players not matching arm position. Nothing I can do about armor, I'm afraid.

#####Version 0.12-64 - BUILT
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

#####Version 0.11-63 - BUILT
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

#####Version 0.11-62 - BUILT
- official 1.11.2 release, same changelog as build 59

#####Version 0.11-59 - UNRELEASED
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

#####Version 0.10-58 - BUILT
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

#####Version 0.10-57 - BUILT
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

#####Version 0.10-56 - BUILT
- fixed duplication of stacks of items dropped onto the bottling machine
- fixed EntityDataManager registration (thanks mconwa01)
- fixed manual entry of the fluidpump citing the use of "wirecutters" instead of "hammer"
- fixed manual typo in the fermenter entry

#####Version 0.10-55 - BUILT
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

#####Version 0.10-54 - BUILT
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

#####Version 0.10-53 - BUILT
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

#####Version 0.10-52 - BUILT
- added the ability for Dropping Conveyors to drop through trapdoors
- added a new shader
- fixed Nullpointer for Connectors accepting FUs

#####Version 0.10-51 - BUILT
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

#####Version 0.10-50 - BUILT
- added versioncheck for TConstruct to prevent crashes with ranged weapons
- added Icons for all IE machines to JEI and fixed recipe displays
- fixed furnace heater constantly updating chunks
- improved UV maps on the revolver, added a new shader

#####Version 0.10-49 - BUILT
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

#####Version 0.10-48 - BUILT
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

#####Version 0.10-47 - BUILT
- moved all clientside references from the config

#####Version 0.10-46 - BUILT
- fixed serverside crash due to abstract proxy
- fixed mcmod.info file

#####Version 0.10-45 - BUILT
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

#####Version 0.10-44 - BUILT
- fixed clientside exception

#####Version 0.10-43 - BUILT
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

#####Version 0.10-42 - BUILT
- fixed NPE when throwign items into the crusher
- attempted fix for render crashes with TESRs and their blockstates

#####Version 0.10-41 - BUILT
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

#####Version 0.10-40 - BUILT
- fixed machines crashing
- fixed sheetmetal tank not outputting

#####Version 0.10-39 - BUILT
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

#####Version 0.10-38 - BUILT
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

#####Version 0.10-37 - BUILT
- added the new IBullet system!
    - bullets are no longer based on metadata, but use NBT tags to differentiate types, their abilities are handled externally
    - updated manual accordingly
- added recipe for leaded concrete
- changed Conveyors to have a better placement system
- fixed crashes when shiftclicking in revolver gui
- fixed slots not assignign properly in revolver gui
- fixed a bunch of manual things

#####Version 0.10-36 - BUILT
- added Hemp Seeds to Forestry's bags
- changed Windmill and Improved Windmill to have models with 90% less polys. PERFORMANCE!
- removed unnecesary debug outprints
- fixed NPE in manual
- fixed issues on the manual's mineral pages
- fixed models for coresamples

#####Version 0.10-35 - BUILT
- re-added Open Computers compat
- cahnged waterwheel and windmills to use FastTESR
- fixed NPE in ShapedIngredientRecipe
- fixed Treated Wood display in the TCon Book

#####Version 0.10-34 - BUILT
- added TCon alloy recipes
- fixed crash in earmuff recipe, using a client access method
- fixed arc alloy recipes having too little output
- prepping for FastTESR hopefully soon!

#####Version 0.10-33 - BUILT
- fixed crashes when break smart models, parsed texture into baked builder
- fixed ItemRenders for multiblocks
- fixed rendering of Wind- and Watermills
- fixed entrylsits in the manual being short
- fixed the forming of multiblocks scrambling the player's inventory
- fixed treated wood recipe consuming buckets (Forge Bug, really...)
- fixed machines crashign when fillign buckets internally

#####Version 0.10-31 - BUILT
- added Redstone Wire connector
- cleaned up recipes and manual a little
- fixed wire rendering
- fixed crash with cart shaders

#####Version 0.10-30 - BUILT
- UPDATE TO 1.10.2

#####Version 0.9-29 - BUILT
- UPDATE TO 1.9.4
- mostly port, movign stuff to new fluid systems, capabilities, etc
- moved 3D Maneuver gear. It didn't fit the style of hte mod and was incredibly glitchy

#####Version 0.8-28 - BUILT
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

#####Version 0.8-27 - BUILT
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


#####Version 0.8-26 - BUILT
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
 

#####Version 0.8-25 - BUILT
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


#####Version 0.8-24 - BUILT
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


#####Version 0.8-23 - BUILT
I lost all my changelog before this point. Whoops.
basically: Initial git push with a lot of the machines and tools implemented. But not done yet.